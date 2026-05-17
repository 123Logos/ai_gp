package com.aigp.demo.support.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * 将模型连通性探测失败转换为用户可理解的保存配置错误提示（不含密钥）。
 */
public final class LlmConnectivityMessages {

	private LlmConnectivityMessages() {}

	public static String fromException(Exception ex, String baseUrl, String model, ObjectMapper objectMapper) {
		if (ex instanceof IllegalArgumentException iae) {
			return iae.getMessage();
		}
		if (ex instanceof RestClientResponseException rce) {
			return fromHttpError(rce, baseUrl, model, objectMapper);
		}
		if (ex.getCause() instanceof RestClientResponseException rce) {
			return fromHttpError(rce, baseUrl, model, objectMapper);
		}
		if (ex instanceof ResourceAccessException) {
			return "无法连接模型服务，请检查 baseUrl 是否可访问（当前："
					+ sanitizeUrl(baseUrl)
					+ "）。若使用 DeepSeek 等第三方，地址一般为 https://api.xxx.com/v1";
		}
		String msg = ex.getMessage();
		if (StringUtils.hasText(msg) && msg.length() <= 300) {
			return "模型连通性校验失败：" + msg;
		}
		return "模型连通性校验失败，请检查 apiKey、baseUrl、model 是否正确";
	}

	private static String fromHttpError(
			RestClientResponseException ex, String baseUrl, String model, ObjectMapper objectMapper) {
		int status = ex.getStatusCode().value();
		String remote = extractRemoteMessage(ex.getResponseBodyAsString(), objectMapper);
		if (status == 401 || status == 403) {
			return "API Key 无效、无权限或已过期，请核对后重试";
		}
		if (status == 404) {
			return "模型接口地址不存在，请确认 baseUrl 为 OpenAI 兼容根路径且一般以 /v1 结尾（当前："
					+ sanitizeUrl(baseUrl)
					+ "）";
		}
		if (status == 400) {
			if (remote != null
					&& (remote.contains("model")
							|| remote.contains("Model")
							|| remote.contains("does not exist"))) {
				return "模型名称不可用或不存在，请核对 model（当前：" + nullToDash(model) + "）"
						+ hintSuffix(remote);
			}
			return "请求被拒绝，请检查 baseUrl、model 与 API Key 是否匹配" + hintSuffix(remote);
		}
		if (status == 429) {
			return "模型服务请求过于频繁或额度不足，请稍后再试" + hintSuffix(remote);
		}
		if (status >= 500) {
			return "模型服务暂时不可用（HTTP " + status + "），请稍后再试";
		}
		return "模型接口返回 HTTP " + status + hintSuffix(remote);
	}

	private static String extractRemoteMessage(String body, ObjectMapper objectMapper) {
		if (!StringUtils.hasText(body)) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode err = root.path("error");
			if (err.isObject()) {
				String m = err.path("message").asText(null);
				if (StringUtils.hasText(m)) {
					return truncate(m, 200);
				}
			}
			String m = root.path("message").asText(null);
			if (StringUtils.hasText(m)) {
				return truncate(m, 200);
			}
		} catch (Exception ignored) {
			// 非 JSON 则忽略
		}
		return truncate(body.trim(), 200);
	}

	private static String hintSuffix(String remote) {
		if (!StringUtils.hasText(remote)) {
			return "";
		}
		return "（上游提示：" + remote + "）";
	}

	private static String sanitizeUrl(String baseUrl) {
		if (!StringUtils.hasText(baseUrl)) {
			return "—";
		}
		String u = baseUrl.trim();
		return u.length() <= 120 ? u : u.substring(0, 120) + "…";
	}

	private static String nullToDash(String model) {
		return StringUtils.hasText(model) ? model.trim() : "—";
	}

	private static String truncate(String s, int max) {
		return s.length() <= max ? s : s.substring(0, max) + "…";
	}
}
