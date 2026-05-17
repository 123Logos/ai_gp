-- 站内通知（任务提醒等，配合 WebSocket 推送到聊天框）

CREATE TABLE IF NOT EXISTS user_in_app_notifications (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL,
    type        VARCHAR(32)     NOT NULL COMMENT '如 TASK_DUE_REMINDER',
    title       VARCHAR(200)    NOT NULL,
    body        VARCHAR(2000)   NOT NULL,
    task_id     BIGINT UNSIGNED DEFAULT NULL COMMENT '关联 user_assistant_tasks.id',
    session_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '关联 ai_chat_sessions.id（提醒会话）',
    message_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '关联 ai_chat_messages.id',
    read_at     DATETIME        DEFAULT NULL,
    created_at  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_read_created (user_id, read_at, created_at),
    CONSTRAINT fk_inapp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_inapp_session FOREIGN KEY (session_id) REFERENCES ai_chat_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_inapp_message FOREIGN KEY (message_id) REFERENCES ai_chat_messages(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';
