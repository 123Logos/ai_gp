package com.aigp.demo.support.llm;

import com.aigp.demo.config.AppProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AI 对话管道调试：将中间过程追加写入 txt 文件（仅调试，不影响业务返回）。
 * <p>
 * 开关：{@code app.chat.pipeline-debug-log-enabled}；路径：{@code app.chat.pipeline-debug-log-file}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatPipelineDebugLog {

	private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static final int MAX_CONSOLE = 800;
	private static final int MAX_FILE = 20000;
	private static final Object FILE_LOCK = new Object();

	private final AppProperties appProperties;
	private final ObjectMapper objectMapper;

	private final ThreadLocal<String> traceId = new ThreadLocal<>();

	public boolean isEnabled() {
		return appProperties.getChat().isPipelineDebugLogEnabled();
	}

	/**
	 * 单次 /ai/chat 请求开始：在 txt 中写入分隔块与 traceId。
	 */
	public void beginConversationTrace(Long userId, Long sessionId, String userMessage) {
		if (!isEnabled()) {
			return;
		}
		String id = UUID.randomUUID().toString().substring(0, 8);
		traceId.set(id);
		StringBuilder sb = new StringBuilder();
		sb.append("\n").append("=".repeat(80)).append("\n");
		sb.append('[').append(now()).append("] TRACE ").append(id).append(" BEGIN\n");
		sb.append("userId=").append(userId).append(" sessionId=").append(sessionId).append('\n');
		sb.append("userMessage: ").append(userMessage == null ? "" : userMessage).append('\n');
		sb.append("=").append("=".repeat(80)).append("\n");
		writeFile(sb.toString());
	}

	/**
	 * 单次 /ai/chat 请求结束。
	 */
	public void endConversationTrace(boolean success, String summary) {
		if (!isEnabled()) {
			return;
		}
		try {
			StringBuilder sb = new StringBuilder();
			sb.append('[').append(now()).append("] TRACE ").append(traceId.get()).append(" END ");
			sb.append(success ? "OK" : "FAIL").append('\n');
			if (StringUtils.hasText(summary)) {
				sb.append(summary).append('\n');
			}
			sb.append("=").append("=".repeat(80)).append("\n\n");
			writeFile(sb.toString());
		} finally {
			traceId.remove();
		}
	}

	public void step(String phase, String detail, Object... args) {
		if (!isEnabled()) {
			return;
		}
		String text = formatDetail(detail, args);
		log.debug("[AI-CHAT-DBG] [{}] {}", phase, preview(text, MAX_CONSOLE));
		writeFile(line(phase, text));
	}

	public void llmRequest(String phase, String model, List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
		if (!isEnabled()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(line(phase, ">>> LLM 请求 model=" + model + " messages=" + size(messages) + " tools=" + size(tools)));
		sb.append(truncateForFile(truncateJson(messages))).append('\n');
		if (tools != null && !tools.isEmpty()) {
			sb.append("  tools定义:\n").append(truncateForFile(truncateJson(tools))).append('\n');
		}
		writeFile(sb.toString());
	}

	public void llmResponse(String phase, ChatCompletionResult result, String rawResponseJson) {
		if (!isEnabled()) {
			return;
		}
		String reasoningNote = describeReasoningInRaw(rawResponseJson);
		StringBuilder sb = new StringBuilder();
		sb.append(line(
				phase,
				"<<< LLM 响应 content="
						+ preview(result == null ? null : result.content(), MAX_CONSOLE)
						+ " toolCalls="
						+ (result == null || result.toolCalls() == null ? 0 : result.toolCalls().size())
						+ " tokens="
						+ (result == null ? null : result.promptTokens())
						+ "/"
						+ (result == null ? null : result.completionTokens())
						+ reasoningNote));
		if (StringUtils.hasText(rawResponseJson)) {
			sb.append("  raw响应:\n").append(truncateForFile(rawResponseJson)).append('\n');
		}
		writeFile(sb.toString());
	}

	public void toolInvoke(String phase, int round, String toolName, String argumentsJson, String resultJson) {
		if (!isEnabled()) {
			return;
		}
		writeFile(line(
				phase,
				"工具 round="
						+ round
						+ " name="
						+ toolName
						+ "\n  args: "
						+ truncateForFile(argumentsJson)
						+ "\n  => "
						+ truncateForFile(resultJson)));
	}

	private void writeFile(String text) {
		if (!StringUtils.hasText(text)) {
			return;
		}
		String pathStr = appProperties.getChat().getPipelineDebugLogFile();
		if (!StringUtils.hasText(pathStr)) {
			return;
		}
		try {
			Path path = resolvePath(pathStr.trim());
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			synchronized (FILE_LOCK) {
				Files.writeString(
						path,
						text,
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE,
						StandardOpenOption.APPEND);
			}
		} catch (IOException e) {
			log.warn("[AI-CHAT-DBG] 写入调试文件失败 path={}: {}", pathStr, e.getMessage());
		}
	}

	private static Path resolvePath(String pathStr) {
		Path p = Paths.get(pathStr);
		if (p.isAbsolute()) {
			return p;
		}
		return Paths.get(System.getProperty("user.dir", ".")).resolve(p).normalize();
	}

	private String line(String phase, String body) {
		String tid = traceId.get();
		String prefix = tid == null ? "" : " trace=" + tid;
		return "[" + now() + "] [" + phase + "]" + prefix + " " + body + "\n";
	}

	private static String now() {
		return LocalDateTime.now().format(TS);
	}

	private static String formatDetail(String detail, Object[] args) {
		if (detail == null) {
			return "";
		}
		if (args != null && args.length > 0) {
			try {
				return String.format(detail, args);
			} catch (Exception e) {
				return detail;
			}
		}
		return detail;
	}

	private static int size(List<?> list) {
		return list == null ? 0 : list.size();
	}

	private String describeReasoningInRaw(String rawResponseJson) {
		if (!StringUtils.hasText(rawResponseJson)) {
			return "";
		}
		try {
			JsonNode msg = objectMapper.readTree(rawResponseJson).path("choices").path(0).path("message");
			JsonNode rc = msg.path("reasoning_content");
			if (rc.isMissingNode() || rc.isNull()) {
				return "";
			}
			String text = rc.isTextual() ? rc.asText() : rc.toString();
			return "\n  [reasoning_content len="
					+ text.length()
					+ "] "
					+ preview(text, 400)
					+ "\n  (MiMo 思考模式：下一轮请求须原样回传 reasoning_content)";
		} catch (Exception e) {
			return "";
		}
	}

	private String truncateJson(Object value) {
		if (value == null) {
			return "null";
		}
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			return String.valueOf(value);
		}
	}

	private static String truncateForFile(String s) {
		if (s == null) {
			return "";
		}
		String t = s.trim();
		if (t.length() <= MAX_FILE) {
			return t;
		}
		return t.substring(0, MAX_FILE) + "\n…(文件记录已截断，共 " + t.length() + " 字符)";
	}

	private static String preview(String s, int max) {
		if (s == null) {
			return "<null>";
		}
		String t = s.replace('\n', ' ').trim();
		return t.length() <= max ? t : t.substring(0, max) + "…";
	}
}
