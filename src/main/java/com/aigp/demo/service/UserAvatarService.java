package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.AppUserRepository;
import com.aigp.demo.support.ImageUploadSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户头像：本地上传、更新 {@code users.avatar_url}、按 uid 公开读取。
 */
@Service
@RequiredArgsConstructor
public class UserAvatarService {

	private static final Pattern UID_PATTERN = Pattern.compile("^U[0-9A-F]{15}$");
	private static final String AVATAR_BASENAME = "avatar";
	private static final List<String> AVATAR_EXTENSIONS = List.of(".jpg", ".png", ".webp", ".gif");

	private final AppProperties appProperties;
	private final AppUserService appUserService;
	private final AppUserRepository appUserRepository;

	/**
	 * 上传头像并写入用户资料（覆盖同用户旧头像文件）。
	 *
	 * @param userId 当前登录用户内部 id
	 * @param file   multipart 字段名 {@code file}
	 * @return 已更新头像 URL 的用户实体
	 */
	@Transactional
	public AppUser uploadAndBindAvatar(Long userId, MultipartFile file) {
		ImageUploadSupport.validateImage(file, appProperties.getMaxImageUploadBytes());
		AppUser user = appUserService.requireActive(userId);
		String contentType = file.getContentType();
		String ext = ImageUploadSupport.extensionFor(contentType);
		Path dir = avatarDir(userId);
		try {
			Files.createDirectories(dir);
			removeOtherAvatarFiles(dir, ext);
			Path target = dir.resolve(AVATAR_BASENAME + ext);
			file.transferTo(target);
		} catch (IOException e) {
			throw new IllegalStateException("保存头像失败", e);
		}
		user.setAvatarUrl(buildPublicAvatarUrl(user.getUid()));
		return appUserRepository.save(user);
	}

	/**
	 * 按对外 uid 读取头像二进制（公开接口，无需 JWT）。
	 */
	@Transactional(readOnly = true)
	public AvatarFile readPublicAvatar(String uid) {
		if (!StringUtils.hasText(uid) || !UID_PATTERN.matcher(uid.trim()).matches()) {
			throw new NotFoundException("头像不存在");
		}
		AppUser user = appUserService.requireByUid(uid.trim());
		if (user.getStatus() == null || user.getStatus() != 1) {
			throw new NotFoundException("头像不存在");
		}
		Path file = findAvatarFile(avatarDir(user.getId()))
				.orElseThrow(() -> new NotFoundException("头像不存在"));
		try {
			String ext = file.getFileName().toString().substring(AVATAR_BASENAME.length());
			String contentType = ImageUploadSupport.contentTypeForExtension(ext);
			return new AvatarFile(Files.readAllBytes(file), contentType);
		} catch (IOException e) {
			throw new IllegalStateException("读取头像失败", e);
		}
	}

	/** 生成写入 {@code users.avatar_url} 的公开访问地址。 */
	public String buildPublicAvatarUrl(String uid) {
		String base = appProperties.getPublicBaseUrl();
		if (!StringUtils.hasText(base)) {
			base = "http://localhost:8000";
		}
		base = base.trim();
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/api/v1/public/avatars/" + uid;
	}

	private Path avatarDir(Long userId) {
		return resolveUploadRoot().resolve("avatars").resolve(String.valueOf(userId));
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

	private static void removeOtherAvatarFiles(Path dir, String keepExt) throws IOException {
		if (!Files.isDirectory(dir)) {
			return;
		}
		try (Stream<Path> entries = Files.list(dir)) {
			for (Path p : entries.toList()) {
				String name = p.getFileName().toString();
				if (!name.startsWith(AVATAR_BASENAME)) {
					continue;
				}
				String ext = name.substring(AVATAR_BASENAME.length());
				if (!keepExt.equals(ext)) {
					Files.deleteIfExists(p);
				}
			}
		}
	}

	private static java.util.Optional<Path> findAvatarFile(Path dir) {
		if (!Files.isDirectory(dir)) {
			return java.util.Optional.empty();
		}
		for (String ext : AVATAR_EXTENSIONS) {
			Path candidate = dir.resolve(AVATAR_BASENAME + ext);
			if (Files.isRegularFile(candidate)) {
				return java.util.Optional.of(candidate);
			}
		}
		return java.util.Optional.empty();
	}

	public record AvatarFile(byte[] bytes, String contentType) {}
}
