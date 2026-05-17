package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "保存用户 LLM 配置")
public record PutUserLlmSettingsRequest(
		@Schema(description = "PLATFORM=官方（默认） BYOK=自带 Key；不传则 PLATFORM")
				@Pattern(regexp = "PLATFORM|BYOK", message = "billingMode 仅支持 PLATFORM 或 BYOK")
				String billingMode,
		@Schema(description = "提供商：mimo / ollama 等", requiredMode = Schema.RequiredMode.REQUIRED)
				@NotBlank
				@Size(max = 32)
				String provider,
		@Schema(description = "用户 API Key；仅 BYOK 可填；null 表示不修改；空字符串表示清除")
				@Size(max = 512)
				String apiKey,
		@Schema(description = "覆盖 baseUrl；仅 BYOK 生效；null 不修改；空串清除")
				@Size(max = 500)
				String baseUrl,
		@Schema(description = "覆盖 model；仅 BYOK 生效；null 不修改；空串清除")
				@Size(max = 128)
				String model) {

	/** 未传 billingMode 时固定为官方代调。 */
	public String billingModeOrPlatform() {
		if (billingMode == null || billingMode.isBlank()) {
			return "PLATFORM";
		}
		return billingMode.trim();
	}
}
