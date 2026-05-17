package com.aigp.demo.support.llm;

import java.util.List;

public record ChatCompletionResult(
		String content,
		String reasoningContent,
		List<ToolCallPayload> toolCalls,
		Integer promptTokens,
		Integer completionTokens) {

	public boolean hasToolCalls() {
		return toolCalls != null && !toolCalls.isEmpty();
	}

	public record ToolCallPayload(String id, String name, String argumentsJson) {}
}
