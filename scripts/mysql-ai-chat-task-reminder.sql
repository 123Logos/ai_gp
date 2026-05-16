-- 助手任务到期提醒字段（在已执行旧版 mysql-ai-chat.sql、无 reminder_sent_at 的库上执行）
-- 若建表脚本已含该列，或列已存在，执行本文件不会报错（可重复执行）

SET @db = DATABASE();
SET @exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_assistant_tasks'
      AND COLUMN_NAME = 'reminder_sent_at'
);
SET @sql = IF(
    @exists = 0,
    'ALTER TABLE user_assistant_tasks ADD COLUMN reminder_sent_at DATETIME DEFAULT NULL COMMENT ''最近一次到期提醒发送时间'' AFTER due_date',
    'SELECT ''skip: reminder_sent_at already exists'' AS migration_note'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
