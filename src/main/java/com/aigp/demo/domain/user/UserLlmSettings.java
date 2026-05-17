package com.aigp.demo.domain.user;

import com.aigp.demo.domain.enums.LlmBillingMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 用户 AI 对话密钥配置（一行 per user，主键与 {@code users.id} 相同）。
 */
@Entity
@Table(name = "user_llm_settings")
@Getter
@Setter
@NoArgsConstructor
public class UserLlmSettings {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "billing_mode", nullable = false, length = 16)
	private LlmBillingMode billingMode = LlmBillingMode.PLATFORM;

	@Column(name = "provider_key", nullable = false, length = 32)
	private String providerKey = "mimo";

	@Column(name = "custom_base_url", length = 500)
	private String customBaseUrl;

	@Column(name = "custom_model", length = 128)
	private String customModel;

	@Column(name = "api_key_cipher", columnDefinition = "TEXT")
	private String apiKeyCipher;

	@Column(name = "api_key_hint", length = 16)
	private String apiKeyHint;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
