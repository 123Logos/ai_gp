package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "可选 AI 提供商（平台侧）")
public record UserLlmProviderOptionResponse(
		@Schema(description = "键名") String key,
		@Schema(description = "默认模型") String model,
		@Schema(description = "默认 baseUrl") String baseUrl,
		@Schema(description = "平台侧是否已配置可用") boolean platformAvailable) {}
