package com.aigp.demo.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "会话消息列表（按时间升序）")
public record AiChatMessageListResponse(
		@Schema(description = "会话 ID") Long sessionId,
		@Schema(description = "会话标题") String sessionTitle,
		@Schema(description = "条数") int count,
		@Schema(description = "消息列表") List<AiChatMessageItemResponse> messages) {}
