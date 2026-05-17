package com.aigp.demo.support.speech;

import com.aigp.demo.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 将音频转发至内网 Whisper ASR HTTP 服务（默认兼容 whisper-asr-webservice）。
 */
@Component
@RequiredArgsConstructor
public class LocalSpeechToTextClient {

	private final AppProperties appProperties;
	private final ObjectMapper objectMapper;

	/**
	 * 调用本地 STT，返回识别文本。
	 */
	public String transcribe(MultipartFile file) {
		AppProperties.Speech speech = appProperties.getSpeech();
		RestClient client = buildClient(speech);
		URI uri = buildUri(speech);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		try {
			byte[] bytes = file.getBytes();
			String filename = com.aigp.demo.support.AudioUploadSupport.resolveForwardFilename(file);
			body.add(
					speech.getUpstreamFilePartName(),
					new ByteArrayResource(bytes) {
						@Override
						public String getFilename() {
							return filename;
						}
					});
		} catch (Exception e) {
			throw new IllegalArgumentException("读取语音文件失败", e);
		}

		try {
			String responseBody = client
					.post()
					.uri(uri)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(body)
					.retrieve()
					.body(String.class);
			return parseText(responseBody);
		} catch (ResourceAccessException e) {
			throw new IllegalStateException(
					"无法连接本地语音服务 "
							+ speech.getBaseUrl()
							+ "，请确认 STT 已启动（见 md文档/本地语音转写-STT部署.md）",
					e);
		} catch (RestClientResponseException e) {
			String detail = summarizeRemoteError(e.getResponseBodyAsString());
			throw new IllegalArgumentException("语音转写失败（HTTP " + e.getStatusCode().value() + "）" + detail, e);
		}
	}

	private RestClient buildClient(AppProperties.Speech speech) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofMillis(Math.max(1000, speech.getConnectTimeoutMs())));
		factory.setReadTimeout(Duration.ofMillis(Math.max(5000, speech.getReadTimeoutMs())));
		return RestClient.builder()
				.baseUrl(normalizeBaseUrl(speech.getBaseUrl()))
				.requestFactory(factory)
				.build();
	}

	private static URI buildUri(AppProperties.Speech speech) {
		UriComponentsBuilder builder =
				UriComponentsBuilder.fromPath(speech.getTranscribePath() == null ? "/asr" : speech.getTranscribePath());
		if (StringUtils.hasText(speech.getQueryLanguage())) {
			builder.queryParam("language", speech.getQueryLanguage().trim());
		}
		if (StringUtils.hasText(speech.getQueryOutput())) {
			builder.queryParam("output", speech.getQueryOutput().trim());
		}
		if (speech.isQueryEncode()) {
			builder.queryParam("encode", true);
		}
		if (StringUtils.hasText(speech.getQueryTask())) {
			builder.queryParam("task", speech.getQueryTask().trim());
		}
		return builder.build(true).toUri();
	}

	private String parseText(String responseBody) {
		if (!StringUtils.hasText(responseBody)) {
			throw new IllegalArgumentException("语音服务返回空结果");
		}
		String trimmed = responseBody.trim();
		if (trimmed.startsWith("{")) {
			try {
				JsonNode root = objectMapper.readTree(trimmed);
				JsonNode text = root.path("text");
				if (!text.isMissingNode() && !text.isNull()) {
					return text.asText().trim();
				}
				JsonNode result = root.path("result");
				if (!result.isMissingNode() && !result.isNull()) {
					return result.asText().trim();
				}
			} catch (Exception ignored) {
				// 非 JSON 则按纯文本
			}
		}
		return trimmed;
	}

	private static String summarizeRemoteError(String body) {
		if (!StringUtils.hasText(body)) {
			return "";
		}
		String t = body.trim();
		return t.length() <= 200 ? "：" + t : "：" + t.substring(0, 200) + "…";
	}

	private static String normalizeBaseUrl(String baseUrl) {
		String u = baseUrl == null ? "" : baseUrl.trim();
		while (u.endsWith("/")) {
			u = u.substring(0, u.length() - 1);
		}
		return u;
	}
}
