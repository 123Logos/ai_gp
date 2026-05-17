-- 用户 AI 对话密钥与计费模式（平台代调 / 自带 Key）
CREATE TABLE IF NOT EXISTS user_llm_settings (
    user_id           BIGINT UNSIGNED NOT NULL,
    billing_mode      VARCHAR(16)     NOT NULL DEFAULT 'PLATFORM' COMMENT 'PLATFORM=平台密钥 BYOK=用户自带',
    provider_key      VARCHAR(32)     NOT NULL DEFAULT 'mimo' COMMENT '对应 app.chat.providers 键名',
    custom_base_url   VARCHAR(500)    DEFAULT NULL,
    custom_model      VARCHAR(128)    DEFAULT NULL,
    api_key_cipher    TEXT            DEFAULT NULL COMMENT 'AES-GCM 密文，仅 BYOK',
    api_key_hint      VARCHAR(16)     DEFAULT NULL COMMENT 'Key 尾号脱敏展示',
    created_at        DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_llm_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户 LLM 调用配置';
