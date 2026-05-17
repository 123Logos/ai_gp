package com.aigp.demo.service.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AiChatIntentSignalsTest {

	@Test
	void negatedToolMentionDoesNotSuggestTools() {
		String hint =
				"用户意图：问候。\n无需调用任务工具如create_task或list_tasks。\n没有上下文指代。";
		assertFalse(AiChatIntentSignals.suggestsTaskTools(hint));
	}

	@Test
	void positiveSuggestionEnablesTools() {
		assertTrue(AiChatIntentSignals.suggestsTaskTools("应调用 create_task 创建提醒，dueAt 必填。"));
	}
}
