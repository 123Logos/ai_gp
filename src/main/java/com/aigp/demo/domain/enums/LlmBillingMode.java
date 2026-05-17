package com.aigp.demo.domain.enums;

/**
 * AI 对话计费/密钥来源：平台统一 Key 或用户自带 Key（BYOK）。
 */
public enum LlmBillingMode {
	/** 使用服务端配置的提供商密钥 */
	PLATFORM,
	/** 使用用户保存的 API Key（可覆盖 baseUrl / model） */
	BYOK;

	public static LlmBillingMode parse(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("billingMode 不能为空");
		}
		try {
			return LlmBillingMode.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("billingMode 仅支持 PLATFORM 或 BYOK");
		}
	}
}
