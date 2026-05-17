package com.aigp.demo.support;

import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音上传校验（转写接口 multipart 字段 {@code file}）。
 */
public final class AudioUploadSupport {

	public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"audio/webm",
			"audio/wav",
			"audio/x-wav",
			"audio/mpeg",
			"audio/mp3",
			"audio/ogg",
			"audio/mp4",
			MediaType.APPLICATION_OCTET_STREAM_VALUE);

	private AudioUploadSupport() {}

	/**
	 * 校验非空、MIME 白名单（含 octet-stream 时要求有文件名扩展名）与大小上限。
	 */
	public static void validateAudio(MultipartFile file, long maxBytes) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("请选择语音文件");
		}
		String contentType = file.getContentType();
		if (!isAllowedContentType(contentType, file.getOriginalFilename())) {
			throw new IllegalArgumentException("仅支持 webm、wav、mp3、ogg、m4a 等常见音频格式");
		}
		if (file.getSize() > maxBytes) {
			long mb = Math.max(1, maxBytes / 1024 / 1024);
			throw new IllegalArgumentException("语音文件过大，单段不超过 " + mb + "MB");
		}
	}

	private static boolean isAllowedContentType(String contentType, String originalFilename) {
		if (StringUtils.hasText(contentType) && ALLOWED_CONTENT_TYPES.contains(contentType)) {
			if (MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(contentType)) {
				return hasAudioExtension(originalFilename);
			}
			return true;
		}
		return hasAudioExtension(originalFilename);
	}

	private static boolean hasAudioExtension(String name) {
		if (!StringUtils.hasText(name)) {
			return false;
		}
		String lower = name.trim().toLowerCase();
		return lower.endsWith(".webm")
				|| lower.endsWith(".wav")
				|| lower.endsWith(".mp3")
				|| lower.endsWith(".mpeg")
				|| lower.endsWith(".ogg")
				|| lower.endsWith(".m4a")
				|| lower.endsWith(".mp4");
	}

	/** 转发到 STT 时使用的文件名（避免无扩展名）。 */
	public static String resolveForwardFilename(MultipartFile file) {
		if (StringUtils.hasText(file.getOriginalFilename())) {
			return file.getOriginalFilename().trim();
		}
		String ct = file.getContentType();
		if ("audio/webm".equals(ct)) {
			return "speech.webm";
		}
		if ("audio/wav".equals(ct) || "audio/x-wav".equals(ct)) {
			return "speech.wav";
		}
		if ("audio/mpeg".equals(ct) || "audio/mp3".equals(ct)) {
			return "speech.mp3";
		}
		return "speech.bin";
	}
}
