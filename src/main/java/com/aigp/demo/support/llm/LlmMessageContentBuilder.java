package com.aigp.demo.support.llm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/** 构造 OpenAI 兼容多模态 user 消息 content（文本 + image_url）。 */
public final class LlmMessageContentBuilder {

	private LlmMessageContentBuilder() {}

	public static Object buildUserContent(String text, List<String> imageDataUris) {
		boolean hasText = StringUtils.hasText(text);
		boolean hasImages = imageDataUris != null && !imageDataUris.isEmpty();
		if (!hasImages) {
			return hasText ? text : "";
		}
		if (!hasText && imageDataUris.size() == 1) {
			return List.of(imagePart(imageDataUris.getFirst()));
		}
		List<Map<String, Object>> parts = new ArrayList<>();
		if (hasText) {
			parts.add(textPart(text.trim()));
		}
		for (String uri : imageDataUris) {
			if (StringUtils.hasText(uri)) {
				parts.add(imagePart(uri));
			}
		}
		return parts;
	}

	private static Map<String, Object> textPart(String text) {
		return Map.of("type", "text", "text", text);
	}

	private static Map<String, Object> imagePart(String dataUri) {
		Map<String, Object> imageUrl = new LinkedHashMap<>();
		imageUrl.put("url", dataUri);
		Map<String, Object> part = new LinkedHashMap<>();
		part.put("type", "image_url");
		part.put("image_url", imageUrl);
		return part;
	}
}
