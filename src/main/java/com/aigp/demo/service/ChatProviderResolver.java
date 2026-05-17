package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.domain.enums.LlmBillingMode;
import com.aigp.demo.domain.user.UserLlmSettings;
import com.aigp.demo.exception.FeatureUnavailableException;
import com.aigp.demo.repository.UserLlmSettingsRepository;
import com.aigp.demo.support.crypto.SecretStringEncryptor;
import com.aigp.demo.web.user.dto.PutUserLlmSettingsRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 按用户配置与平台配置合并出实际调用大模型用的 {@link AppProperties.ChatProvider}（返回副本，不修改配置 Bean）。
 */
@Service
@RequiredArgsConstructor
public class ChatProviderResolver {

	private final AppProperties appProperties;
	private final UserLlmSettingsRepository userLlmSettingsRepository;
	private final SecretStringEncryptor secretStringEncryptor;

	/** 返回平台已配置的提供商键名白名单。 */
	public Set<String> allowedProviderKeys() {
		return appProperties.getChat().getProviders().keySet();
	}

	/**
	 * 校验并规范化提供商键名。
	 */
	public String normalizeProviderKey(String providerKey) {
		String key = providerKey.trim().toLowerCase(Locale.ROOT);
		if (!allowedProviderKeys().contains(key)) {
			throw new IllegalArgumentException(
					"不支持的 provider，可选: "
							+ String.join(", ", allowedProviderKeys())
							+ "。自带 Key（BYOK）时 provider 仍须填上述键名之一，"
							+ "第三方服务（如 DeepSeek）请通过 apiKey、baseUrl、model 指定");
		}
		return key;
	}

	/**
	 * 按即将保存的请求合并出用于连通性探测的提供商配置（不落库）。
	 */
	public AppProperties.ChatProvider buildEffectiveProviderForSave(
			UserLlmSettings existing, LlmBillingMode mode, String providerKey, PutUserLlmSettingsRequest body) {
		AppProperties.ChatProvider template = requirePlatformTemplate(providerKey, false);
		if (mode == LlmBillingMode.PLATFORM) {
			return requirePlatformTemplate(providerKey, true);
		}
		String apiKey = resolveApiKeyForProbe(existing, body);
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException("BYOK 模式下必须提供 apiKey");
		}
		AppProperties.ChatProvider out = copyProvider(template);
		out.setApiKey(apiKey.trim());
		String baseUrl = pickCustomOrExisting(body.baseUrl(), existing == null ? null : existing.getCustomBaseUrl());
		if (!StringUtils.hasText(baseUrl)) {
			baseUrl = template.getBaseUrl();
		}
		out.setBaseUrl(baseUrl == null ? null : baseUrl.trim());
		String model = pickCustomOrExisting(body.model(), existing == null ? null : existing.getCustomModel());
		if (!StringUtils.hasText(model)) {
			model = template.getModel();
		}
		out.setModel(model == null ? null : model.trim());
		if (!StringUtils.hasText(out.getBaseUrl()) || !StringUtils.hasText(out.getModel())) {
			throw new IllegalArgumentException("BYOK 模式下须配置可访问的 baseUrl 与 model（请求体或沿用已保存值）");
		}
		return out;
	}

	private String resolveApiKeyForProbe(UserLlmSettings existing, PutUserLlmSettingsRequest body) {
		if (body.apiKey() != null && StringUtils.hasText(body.apiKey().trim())) {
			return body.apiKey().trim();
		}
		if (existing != null && StringUtils.hasText(existing.getApiKeyCipher())) {
			return secretStringEncryptor.decrypt(existing.getApiKeyCipher());
		}
		return null;
	}

	private static String pickCustomOrExisting(String fromRequest, String existing) {
		if (fromRequest != null) {
			String t = fromRequest.trim();
			return t.isEmpty() ? null : t;
		}
		return StringUtils.hasText(existing) ? existing.trim() : null;
	}

	/**
	 * 后台任务（周总结等）使用平台默认提供商，不走用户 BYOK。
	 */
	public AppProperties.ChatProvider resolvePlatformForBackgroundJob() {
		String def = appProperties.getChat().getDefaultProvider();
		String key = StringUtils.hasText(def) ? def.trim().toLowerCase(Locale.ROOT) : "mimo";
		if (!allowedProviderKeys().contains(key)) {
			key = allowedProviderKeys().iterator().next();
		}
		return requirePlatformTemplate(key, true);
	}

	/**
	 * 解析对话实际使用的提供商配置（平台 Key 或用户 BYOK）。
	 */
	public AppProperties.ChatProvider resolveForUser(Long userId, String providerKey) {
		String key = normalizeProviderKey(providerKey);
		AppProperties.ChatProvider template = requirePlatformTemplate(key, false);
		UserLlmSettings settings = userLlmSettingsRepository.findByUser_Id(userId).orElse(null);
		// 默认官方代调：仅当用户显式选择 BYOK 且已保存有效 Key 时才用自有 Token
		if (!isActiveByok(settings)) {
			return requirePlatformTemplate(key, true);
		}
		return resolveByok(settings, template);
	}

	/**
	 * 列出平台侧提供商概要（不含密钥），供前端选择。
	 */
	public List<PlatformProviderInfo> listPlatformProviders() {
		List<PlatformProviderInfo> list = new ArrayList<>();
		for (Map.Entry<String, AppProperties.ChatProvider> e :
				appProperties.getChat().getProviders().entrySet()) {
			AppProperties.ChatProvider cfg = e.getValue();
			boolean configured = StringUtils.hasText(cfg.getBaseUrl()) && StringUtils.hasText(cfg.getModel());
			boolean keyReady = !"mimo".equals(e.getKey()) || StringUtils.hasText(cfg.getApiKey());
			list.add(new PlatformProviderInfo(
					e.getKey(),
					cfg.getModel(),
					cfg.getBaseUrl(),
					configured && keyReady));
		}
		return list;
	}

	static boolean isActiveByok(UserLlmSettings settings) {
		return settings != null
				&& settings.getBillingMode() == LlmBillingMode.BYOK
				&& StringUtils.hasText(settings.getApiKeyCipher());
	}

	private AppProperties.ChatProvider resolveByok(UserLlmSettings settings, AppProperties.ChatProvider platform) {
		if (!StringUtils.hasText(settings.getApiKeyCipher())) {
			throw new FeatureUnavailableException(
					"CHAT_BYOK", "已选择自带 API Key，请先在「LLM 设置」中保存有效的 Key");
		}
		String apiKey = secretStringEncryptor.decrypt(settings.getApiKeyCipher());
		AppProperties.ChatProvider out = copyProvider(platform);
		out.setApiKey(apiKey);
		if (StringUtils.hasText(settings.getCustomBaseUrl())) {
			out.setBaseUrl(settings.getCustomBaseUrl().trim());
		}
		if (StringUtils.hasText(settings.getCustomModel())) {
			out.setModel(settings.getCustomModel().trim());
		}
		if (!StringUtils.hasText(out.getBaseUrl()) || !StringUtils.hasText(out.getModel())) {
			throw new FeatureUnavailableException("CHAT_BYOK", "自带 Key 模式下 baseUrl 与 model 不能为空");
		}
		return out;
	}

	/**
	 * @param requirePlatformApiKey 为 true 时校验平台侧已配置密钥（PLATFORM 模式）
	 */
	private AppProperties.ChatProvider requirePlatformTemplate(String providerKey, boolean requirePlatformApiKey) {
		AppProperties.ChatProvider cfg = appProperties.getChat().getProviders().get(providerKey);
		if (cfg == null || !StringUtils.hasText(cfg.getBaseUrl()) || !StringUtils.hasText(cfg.getModel())) {
			throw new FeatureUnavailableException(
					"CHAT_PROVIDER",
					"未配置 AI 对话提供商「" + providerKey + "」，请检查 app.chat.providers");
		}
		if (requirePlatformApiKey && "mimo".equals(providerKey) && !StringUtils.hasText(cfg.getApiKey())) {
			throw new FeatureUnavailableException(
					"CHAT_PROVIDER", "MiMo 未配置 API Key，请设置 MIMO_API_KEY 或 app.chat.providers.mimo.api-key");
		}
		return copyProvider(cfg);
	}

	private static AppProperties.ChatProvider copyProvider(AppProperties.ChatProvider source) {
		AppProperties.ChatProvider copy = new AppProperties.ChatProvider();
		copy.setApiKey(source.getApiKey());
		copy.setBaseUrl(source.getBaseUrl());
		copy.setModel(source.getModel());
		return copy;
	}

	public record PlatformProviderInfo(String key, String model, String baseUrl, boolean platformAvailable) {}
}
