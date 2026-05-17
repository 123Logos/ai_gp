-- 按分到点提醒：加速 OPEN 任务 due_at / due_date 扫描
SET @db = DATABASE();

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_assistant_tasks'
      AND INDEX_NAME = 'idx_open_due_at'
);

SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_open_due_at ON user_assistant_tasks (status, due_at)',
    'SELECT ''idx_open_due_at already exists'' AS info'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
