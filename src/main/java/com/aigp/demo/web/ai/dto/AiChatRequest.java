package com.aigp.demo.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "AI 对话请求")
public record AiChatRequest(
		@Schema(description = "用户本轮输入") @NotBlank @Size(max = 8000) String message,
		@Schema(description = "继续已有会话时传入；不传则新建会话") Long sessionId,
		@Schema(description = "模型提供商：mimo（默认）或 ollama") String provider,
		@Schema(description = "先 POST /media/images 上传后填入的资源 ID，可多张") List<Long> imageAssetIds) {}
