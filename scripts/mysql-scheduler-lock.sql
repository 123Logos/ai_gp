-- 定时任务分布式锁（多实例部署时避免重复投递提醒）
CREATE TABLE IF NOT EXISTS scheduler_lock (
    lock_name    VARCHAR(64)  NOT NULL PRIMARY KEY,
    locked_until DATETIME(3)  NOT NULL,
    locked_by    VARCHAR(128) NOT NULL,
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调度器互斥锁';
