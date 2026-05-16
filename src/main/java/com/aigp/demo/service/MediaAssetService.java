package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.domain.media.AiChatMessageMedia;
import com.aigp.demo.domain.media.UserAssistantTaskMedia;
import com.aigp.demo.domain.media.UserMediaAsset;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.AiChatMessageMediaRepository;
import com.aigp.demo.repository.UserAssistantTaskMediaRepository;
import com.aigp.demo.repository.UserMediaAssetRepository;
import com.aigp.demo.web.media.dto.MediaAssetResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaAssetService {

	private static final Set<String> ALLOWED_TYPES =
			Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp", "image/gif");

	private final AppProperties appProperties;
	private final AppUserService appUserService;
	private final UserMediaAssetRepository userMediaAssetRepository;
	private final AiChatMessageMediaRepository aiChatMessageMediaRepository;
	private final UserAssistantTaskMediaRepository userAssistantTaskMediaRepository;

	@Transactional
	public MediaAssetResponse uploadImage(Long userId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("请选择图片文件");
		}
		String contentType = file.getContentType();
		if (!StringUtils.hasText(contentType) || !ALLOWED_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("仅支持 JPEG、PNG、WebP、GIF 图片");
		}
		if (file.getSize() > appProperties.getMaxImageUploadBytes()) {
			throw new IllegalArgumentException("图片过大，单张不超过 "
					+ (appProperties.getMaxImageUploadBytes() / 1024 / 1024) + "MB");
		}
		AppUser user = appUserService.requireActive(userId);
		String ext = extensionFor(contentType);
		String storageKey = userId + "/" + UUID.randomUUID() + ext;
		Path target = resolveUploadRoot().resolve(storageKey);
		try {
			Files.createDirectories(target.getParent());
			file.transferTo(target);
		} catch (IOException e) {
			throw new IllegalStateException("保存图片失败", e);
		}
		UserMediaAsset asset = new UserMediaAsset();
		asset.setUser(user);
		asset.setContentType(contentType);
		asset.setSizeBytes(file.getSize());
		asset.setStorageKey(storageKey.replace('\\', '/'));
		userMediaAssetRepository.save(asset);
		return toResponse(asset);
	}

	@Transactional(readOnly = true)
	public UserMediaAsset requireOwned(Long userId, Long assetId) {
		return userMediaAssetRepository
				.findByIdAndUser_Id(assetId, userId)
				.orElseThrow(() -> new NotFoundException("图片不存在: id=" + assetId));
	}

	@Transactional(readOnly = true)
	public List<UserMediaAsset> requireOwned(Long userId, List<Long> assetIds) {
		if (assetIds == null || assetIds.isEmpty()) {
			return List.of();
		}
		List<Long> distinct = assetIds.stream().distinct().toList();
		List<UserMediaAsset> found = userMediaAssetRepository.findByIdInAndUser_Id(distinct, userId);
		if (found.size() != distinct.size()) {
			throw new NotFoundException("部分图片不存在或无权访问");
		}
		Map<Long, UserMediaAsset> byId =
				found.stream().collect(Collectors.toMap(UserMediaAsset::getId, a -> a, (a, b) -> a, LinkedHashMap::new));
		List<UserMediaAsset> ordered = new ArrayList<>();
		for (Long id : distinct) {
			ordered.add(byId.get(id));
		}
		return ordered;
	}

	@Transactional(readOnly = true)
	public MediaFile readOwnedFile(Long userId, Long assetId) {
		UserMediaAsset asset = requireOwned(userId, assetId);
		Path path = resolveUploadRoot().resolve(asset.getStorageKey());
		if (!Files.isRegularFile(path)) {
			throw new NotFoundException("图片文件不存在: id=" + assetId);
		}
		try {
			return new MediaFile(Files.readAllBytes(path), asset.getContentType());
		} catch (IOException e) {
			throw new IllegalStateException("读取图片失败", e);
		}
	}

	@Transactional(readOnly = true)
	public String toDataUri(Long userId, UserMediaAsset asset) {
		requireOwned(userId, asset.getId());
		Path path = resolveUploadRoot().resolve(asset.getStorageKey());
		if (!Files.isRegularFile(path)) {
			throw new NotFoundException("图片文件不存在: id=" + asset.getId());
		}
		try {
			String b64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
			return "data:" + asset.getContentType() + ";base64," + b64;
		} catch (IOException e) {
			throw new IllegalStateException("读取图片失败", e);
		}
	}

	@Transactional(readOnly = true)
	public List<String> toDataUris(Long userId, List<Long> assetIds) {
		return requireOwned(userId, assetIds).stream()
				.map(a -> toDataUri(userId, a))
				.toList();
	}

	public String buildPublicUrl(Long assetId) {
		String base = appProperties.getPublicBaseUrl();
		if (!StringUtils.hasText(base)) {
			base = "http://localhost:8000";
		}
		base = base.trim();
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/api/v1/media/" + assetId;
	}

	public List<String> buildPublicUrls(List<Long> assetIds) {
		if (assetIds == null || assetIds.isEmpty()) {
			return List.of();
		}
		return assetIds.stream().map(this::buildPublicUrl).toList();
	}

	@Transactional
	public void linkAssetsToMessage(Long messageId, List<Long> assetIds) {
		if (assetIds == null || assetIds.isEmpty()) {
			return;
		}
		int i = 0;
		for (Long assetId : assetIds) {
			aiChatMessageMediaRepository.save(AiChatMessageMedia.of(messageId, assetId, i++));
		}
	}

	@Transactional
	public void replaceTaskImages(Long taskId, List<Long> assetIds) {
		userAssistantTaskMediaRepository.deleteByTaskId(taskId);
		if (assetIds == null || assetIds.isEmpty()) {
			return;
		}
		int i = 0;
		for (Long assetId : assetIds) {
			userAssistantTaskMediaRepository.save(UserAssistantTaskMedia.of(taskId, assetId, i++));
		}
	}

	@Transactional(readOnly = true)
	public List<Long> findMessageAssetIds(Long messageId) {
		return aiChatMessageMediaRepository.findByMessageIdOrderBySortOrderAsc(messageId).stream()
				.map(AiChatMessageMedia::getAssetId)
				.toList();
	}

	@Transactional(readOnly = true)
	public Map<Long, List<Long>> findMessageAssetIdsByMessageIds(Collection<Long> messageIds) {
		if (messageIds == null || messageIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, List<Long>> out = new LinkedHashMap<>();
		for (AiChatMessageMedia row :
				aiChatMessageMediaRepository.findByMessageIdInOrderByMessageIdAscSortOrderAsc(messageIds)) {
			out.computeIfAbsent(row.getMessageId(), k -> new ArrayList<>()).add(row.getAssetId());
		}
		return out;
	}

	@Transactional(readOnly = true)
	public List<Long> findTaskAssetIds(Long taskId) {
		return userAssistantTaskMediaRepository.findByTaskIdOrderBySortOrderAsc(taskId).stream()
				.map(UserAssistantTaskMedia::getAssetId)
				.toList();
	}

	@Transactional(readOnly = true)
	public Map<Long, List<String>> findTaskImageUrls(Collection<Long> taskIds) {
		if (taskIds == null || taskIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, List<Long>> assetIdsByTask = new LinkedHashMap<>();
		for (UserAssistantTaskMedia row :
				userAssistantTaskMediaRepository.findByTaskIdInOrderByTaskIdAscSortOrderAsc(taskIds)) {
			assetIdsByTask.computeIfAbsent(row.getTaskId(), k -> new ArrayList<>()).add(row.getAssetId());
		}
		Map<Long, List<String>> urls = new LinkedHashMap<>();
		assetIdsByTask.forEach((taskId, ids) -> urls.put(taskId, buildPublicUrls(ids)));
		return urls;
	}

	public MediaAssetResponse toResponse(UserMediaAsset asset) {
		return new MediaAssetResponse(asset.getId(), buildPublicUrl(asset.getId()), asset.getContentType(), asset.getSizeBytes());
	}

	private Path resolveUploadRoot() {
		String configured = appProperties.getUploadPath();
		if (!StringUtils.hasText(configured)) {
			return Paths.get("uploads").toAbsolutePath().normalize();
		}
		Path path = Paths.get(configured.trim());
		if (!path.isAbsolute()) {
			path = Paths.get(System.getProperty("user.dir")).resolve(path);
		}
		return path.normalize();
	}

	private static String extensionFor(String contentType) {
		return switch (contentType) {
			case MediaType.IMAGE_PNG_VALUE -> ".png";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> ".jpg";
		};
	}

	public record MediaFile(byte[] bytes, String contentType) {}
}
