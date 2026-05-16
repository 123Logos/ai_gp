-- 用户图片资源（对话附图、任务附图）
CREATE TABLE IF NOT EXISTS user_media_assets (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id      BIGINT UNSIGNED NOT NULL,
    content_type VARCHAR(100)    NOT NULL,
    size_bytes   BIGINT UNSIGNED NOT NULL,
    storage_key  VARCHAR(500)    NOT NULL COMMENT 'upload 根目录下相对路径',
    created_at   DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_created (user_id, created_at),
    CONSTRAINT fk_media_assets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户上传图片';

CREATE TABLE IF NOT EXISTS ai_chat_message_media (
    message_id BIGINT UNSIGNED NOT NULL,
    asset_id   BIGINT UNSIGNED NOT NULL,
    sort_order INT UNSIGNED    NOT NULL DEFAULT 0,
    PRIMARY KEY (message_id, asset_id),
    CONSTRAINT fk_msg_media_message FOREIGN KEY (message_id) REFERENCES ai_chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_media_asset FOREIGN KEY (asset_id) REFERENCES user_media_assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话消息附图';

CREATE TABLE IF NOT EXISTS user_assistant_task_media (
    task_id    BIGINT UNSIGNED NOT NULL,
    asset_id   BIGINT UNSIGNED NOT NULL,
    sort_order INT UNSIGNED    NOT NULL DEFAULT 0,
    PRIMARY KEY (task_id, asset_id),
    CONSTRAINT fk_task_media_task FOREIGN KEY (task_id) REFERENCES user_assistant_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_media_asset FOREIGN KEY (asset_id) REFERENCES user_media_assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='助手任务附图';
