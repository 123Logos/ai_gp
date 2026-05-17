package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "平台支持的 AI 提供商列表")
public record UserLlmProvidersResponse(
		@Schema(description = "应用默认提供商") String defaultProvider,
		@Schema(description = "可选项") List<UserLlmProviderOptionResponse> providers) {}
