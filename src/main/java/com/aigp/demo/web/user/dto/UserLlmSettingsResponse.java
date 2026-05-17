package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户 LLM 调用配置")
public record UserLlmSettingsResponse(
		@Schema(description = "用户保存的偏好：PLATFORM 官方 / BYOK 自带（未保存 Key 时对话仍走官方）") String billingMode,
		@Schema(description = "提供商键名，如 mimo、ollama") String provider,
		@Schema(description = "展示用 baseUrl") String baseUrl,
		@Schema(description = "展示用 model") String model,
		@Schema(description = "是否已保存用户 API Key") boolean apiKeyConfigured,
		@Schema(description = "Key 尾号脱敏，如 ****abcd") String apiKeyHint,
		@Schema(description = "当前对话实际是否使用平台官方 Key") boolean usingPlatformKey,
		@Schema(description = "当前对话实际是否使用用户自有 Key") boolean usingOwnApiKey,
		@Schema(description = "true 表示从未保存过 LLM 设置（系统默认官方）") boolean neverConfigured) {}
