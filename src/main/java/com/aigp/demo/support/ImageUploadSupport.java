package com.aigp.demo.support;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 图片上传校验与扩展名映射（对话附图、用户头像等共用）。
 */
public final class ImageUploadSupport {

	public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp", "image/gif");

	private ImageUploadSupport() {}

	/**
	 * 校验非空、MIME 白名单与大小上限。
	 *
	 * @param file     上传文件
	 * @param maxBytes 单张最大字节数（与 {@code app.max-image-upload-bytes} 一致）
	 */
	public static void validateImage(MultipartFile file, long maxBytes) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("请选择图片文件");
		}
		String contentType = file.getContentType();
		if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("仅支持 JPEG、PNG、WebP、GIF 图片");
		}
		if (file.getSize() > maxBytes) {
			throw new IllegalArgumentException("图片过大，单张不超过 " + (maxBytes / 1024 / 1024) + "MB");
		}
	}

	/** 按 MIME 返回落盘扩展名（含点）。 */
	public static String extensionFor(String contentType) {
		return switch (contentType) {
			case MediaType.IMAGE_PNG_VALUE -> ".png";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> ".jpg";
		};
	}

	/** 按扩展名推断 Content-Type（读取已存头像时用）。 */
	public static String contentTypeForExtension(String ext) {
		return switch (ext) {
			case ".png" -> MediaType.IMAGE_PNG_VALUE;
			case ".webp" -> "image/webp";
			case ".gif" -> "image/gif";
			default -> MediaType.IMAGE_JPEG_VALUE;
		};
	}
}
