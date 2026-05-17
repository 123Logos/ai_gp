-- 在已有数据库上增量执行（本仓库仅维护此增量脚本 + 全量建库脚本）
-- 执行前请备份。可重复执行时使用下方「若列已存在」类语句需手工判断是否跳过。

-- 用户陪伴记忆表
CREATE TABLE IF NOT EXISTS user_companion_memory (
    user_id                 BIGINT UNSIGNED NOT NULL COMMENT 'users.id',
    memory_text             MEDIUMTEXT      DEFAULT NULL COMMENT '合并后的长期记忆（注入对话 system）',
    week_internal_summary   TEXT            DEFAULT NULL COMMENT '最近一次本周内部总结（供排查）',
    pending_digest_text     TEXT            DEFAULT NULL COMMENT '待周六早推送的用户可见本周回顾',
    summarized_week_key     VARCHAR(16)     DEFAULT NULL COMMENT '已完成总结的周键，如 2026-W20',
    digest_delivered_week_key VARCHAR(16)   DEFAULT NULL COMMENT '已推送回顾的周键',
    last_summarized_at      DATETIME        DEFAULT NULL,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_companion_memory_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户陪伴记忆';

-- MySQL 8.0.12 以下无 IF NOT EXISTS on ADD COLUMN，若报错请手工检查列是否存在
ALTER TABLE user_notification_settings
    ADD COLUMN weekly_companion_digest TINYINT(1) NOT NULL DEFAULT 1
        COMMENT '是否接收每周六陪伴回顾' AFTER daily_task_reminder;
