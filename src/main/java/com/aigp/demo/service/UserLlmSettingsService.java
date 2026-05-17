package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.domain.enums.LlmBillingMode;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.UserLlmSettings;
import com.aigp.demo.repository.UserLlmSettingsRepository;
import com.aigp.demo.support.crypto.SecretStringEncryptor;
import com.aigp.demo.support.llm.OpenAiCompatibleChatClient;
import com.aigp.demo.web.user.dto.PutUserLlmSettingsRequest;
import com.aigp.demo.web.user.dto.UserLlmProviderOptionResponse;
import com.aigp.demo.web.user.dto.UserLlmProvidersResponse;
import com.aigp.demo.web.user.dto.UserLlmSettingsResponse;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户 LLM 设置：查询、保存自带 Key、恢复平台代调。
 */
@Service
@RequiredArgsConstructor
public class UserLlmSettingsService {

	private static final int API_KEY_MIN_LEN = 8;
	private static final int API_KEY_MAX_LEN = 512;

	private final AppProperties appProperties;
	private final AppUserService appUserService;
	private final UserLlmSettingsRepository userLlmSettingsRepository;
	private final ChatProviderResolver chatProviderResolver;
	private final SecretStringEncryptor secretStringEncryptor;
	private final OpenAiCompatibleChatClient openAiCompatibleChatClient;

	@Transactional(readOnly = true)
	public Optional<UserLlmSettings> findByUserId(Long userId) {
		return userLlmSettingsRepository.findByUser_Id(userId);
	}

	/**
	 * 未单独配置时使用的提供商（用户偏好 &gt; 应用默认）。
	 */
	@Transactional(readOnly = true)
	public String resolveEffectiveProviderKey(Long userId) {
		return findByUserId(userId)
				.map(UserLlmSettings::getProviderKey)
				.filter(StringUtils::hasText)
				.map(k -> k.trim().toLowerCase(Locale.ROOT))
				.orElseGet(this::defaultProviderKey);
	}

	@Transactional(readOnly = true)
	public UserLlmSettingsResponse getSettings(Long userId) {
		appUserService.requireActive(userId);
		Optional<UserLlmSettings> row = findByUserId(userId);
		String providerKey = row.map(UserLlmSettings::getProviderKey).orElse(defaultProviderKey());
		LlmBillingMode mode = row.map(UserLlmSettings::getBillingMode).orElse(LlmBillingMode.PLATFORM);
		AppProperties.ChatProvider platform =
				appProperties.getChat().getProviders().get(providerKey);
		boolean usingOwnApiKey = row.map(ChatProviderResolver::isActiveByok).orElse(false);
		String baseUrl;
		String model;
		if (usingOwnApiKey) {
			baseUrl = row.map(UserLlmSettings::getCustomBaseUrl)
					.filter(StringUtils::hasText)
					.orElse(platform != null ? platform.getBaseUrl() : null);
			model = row.map(UserLlmSettings::getCustomModel)
					.filter(StringUtils::hasText)
					.orElse(platform != null ? platform.getModel() : null);
		} else {
			baseUrl = platform != null ? platform.getBaseUrl() : null;
			model = platform != null ? platform.getModel() : null;
		}
		boolean keyConfigured = row.map(UserLlmSettings::getApiKeyCipher).filter(StringUtils::hasText).isPresent();
		String hint = row.map(UserLlmSettings::getApiKeyHint).orElse(null);
		// 未配置行 = 从未改过设置，一律视为官方默认
		boolean neverConfigured = row.isEmpty();
		return new UserLlmSettingsResponse(
				mode.name(),
				providerKey,
				baseUrl,
				model,
				keyConfigured,
				hint,
				!usingOwnApiKey,
				usingOwnApiKey,
				neverConfigured);
	}

	@Transactional(readOnly = true)
	public UserLlmProvidersResponse listProviderOptions(Long userId) {
		appUserService.requireActive(userId);
		List<UserLlmProviderOptionResponse> options = chatProviderResolver.listPlatformProviders().stream()
				.map(p -> new UserLlmProviderOptionResponse(p.key(), p.model(), p.baseUrl(), p.platformAvailable()))
				.toList();
		return new UserLlmProvidersResponse(defaultProviderKey(), options);
	}

	/**
	 * 保存或更新用户 LLM 配置；{@code apiKey} 为 null 表示不修改已存 Key。
	 */
	@Transactional
	public UserLlmSettingsResponse saveSettings(Long userId, PutUserLlmSettingsRequest body) {
		AppUser user = appUserService.requireActive(userId);
		// 未传 billingMode 时默认官方代调，避免误开 BYOK
		LlmBillingMode mode = LlmBillingMode.parse(body.billingModeOrPlatform());
		String providerKey = chatProviderResolver.normalizeProviderKey(body.provider());
		UserLlmSettings existing = userLlmSettingsRepository.findByUser_Id(userId).orElse(null);

		if (appProperties.getChat().isValidateLlmSettingsOnSave()) {
			AppProperties.ChatProvider probe =
					chatProviderResolver.buildEffectiveProviderForSave(existing, mode, providerKey, body);
			openAiCompatibleChatClient.probeConnectivity(probe);
		}

		UserLlmSettings settings = existing != null
				? existing
				: createNewSettings(user);
		settings.setBillingMode(mode);
		settings.setProviderKey(providerKey);
		if (mode == LlmBillingMode.BYOK) {
			if (body.baseUrl() != null) {
				settings.setCustomBaseUrl(blankToNull(body.baseUrl()));
			}
			if (body.model() != null) {
				settings.setCustomModel(blankToNull(body.model()));
			}
		}
		if (body.apiKey() != null) {
			applyApiKey(settings, body.apiKey(), mode);
		} else if (mode == LlmBillingMode.BYOK && !StringUtils.hasText(settings.getApiKeyCipher())) {
			throw new IllegalArgumentException("BYOK 模式下必须提供 apiKey");
		}
		if (mode == LlmBillingMode.PLATFORM) {
			settings.setApiKeyCipher(null);
			settings.setApiKeyHint(null);
			settings.setCustomBaseUrl(null);
			settings.setCustomModel(null);
		}
		userLlmSettingsRepository.save(settings);
		return getSettings(userId);
	}

	/**
	 * 恢复为平台代调并清除已存用户 Key。
	 */
	@Transactional
	public UserLlmSettingsResponse resetToPlatform(Long userId) {
		appUserService.requireActive(userId);
		Optional<UserLlmSettings> row = findByUserId(userId);
		if (row.isEmpty()) {
			return getSettings(userId);
		}
		UserLlmSettings settings = row.get();
		settings.setBillingMode(LlmBillingMode.PLATFORM);
		settings.setApiKeyCipher(null);
		settings.setApiKeyHint(null);
		settings.setCustomBaseUrl(null);
		settings.setCustomModel(null);
		userLlmSettingsRepository.save(settings);
		return getSettings(userId);
	}

	private UserLlmSettings createNewSettings(AppUser user) {
		UserLlmSettings created = new UserLlmSettings();
		created.setUser(user);
		return created;
	}

	private void applyApiKey(UserLlmSettings settings, String apiKeyRaw, LlmBillingMode mode) {
		String trimmed = apiKeyRaw == null ? "" : apiKeyRaw.trim();
		if (trimmed.isEmpty()) {
			settings.setApiKeyCipher(null);
			settings.setApiKeyHint(null);
			return;
		}
		if (mode != LlmBillingMode.BYOK) {
			throw new IllegalArgumentException("仅 BYOK 模式可保存 apiKey");
		}
		if (trimmed.length() < API_KEY_MIN_LEN || trimmed.length() > API_KEY_MAX_LEN) {
			throw new IllegalArgumentException("apiKey 长度须在 " + API_KEY_MIN_LEN + "–" + API_KEY_MAX_LEN + " 之间");
		}
		settings.setApiKeyCipher(secretStringEncryptor.encrypt(trimmed));
		settings.setApiKeyHint(maskKeyHint(trimmed));
	}

	private String defaultProviderKey() {
		String def = appProperties.getChat().getDefaultProvider();
		return StringUtils.hasText(def) ? def.trim().toLowerCase(Locale.ROOT) : "mimo";
	}

	private static String blankToNull(String raw) {
		String t = raw.trim();
		return t.isEmpty() ? null : t;
	}

	static String maskKeyHint(String apiKey) {
		String t = apiKey.trim();
		if (t.length() <= 4) {
			return "****";
		}
		return "****" + t.substring(t.length() - 4);
	}
}
