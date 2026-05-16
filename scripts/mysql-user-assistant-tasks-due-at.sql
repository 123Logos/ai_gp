-- 助手任务增加截止时刻（精确到分钟，用户本地时间语义）
SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_assistant_tasks'
      AND COLUMN_NAME = 'due_at'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_assistant_tasks ADD COLUMN due_at DATETIME DEFAULT NULL COMMENT ''截止时刻（用户本地，精确到分）'' AFTER due_date',
    'SELECT ''due_at already exists'' AS info'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
