-- =============================================================================
-- 插入 5 个 C 端测试用户（users + user_identities + user_notification_settings）
-- 内部主键 id 使用 100000001–100000005，与 users 自增起点 100000000 策略一致。
--
-- mysql ... < scripts/seed-5-test-users.sql
-- 统一密码（明文）：Test123456
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM user_notification_settings WHERE user_id BETWEEN 100000001 AND 100000005;
DELETE FROM user_identities WHERE user_id BETWEEN 100000001 AND 100000005;
DELETE FROM user_sessions WHERE user_id BETWEEN 100000001 AND 100000005;
DELETE FROM users WHERE id BETWEEN 100000001 AND 100000005;

INSERT INTO users (id, uid, nickname, avatar_url, weekly_hours, status, timezone, language)
VALUES
  (100000001, 'UTEST00000000001', '测试用户01', NULL, 10, 1, 'Asia/Shanghai', 'zh-CN'),
  (100000002, 'UTEST00000000002', '测试用户02', NULL, 10, 1, 'Asia/Shanghai', 'zh-CN'),
  (100000003, 'UTEST00000000003', '测试用户03', NULL, 10, 1, 'Asia/Shanghai', 'zh-CN'),
  (100000004, 'UTEST00000000004', '测试用户04', NULL, 10, 1, 'Asia/Shanghai', 'zh-CN'),
  (100000005, 'UTEST00000000005', '测试用户05', NULL, 10, 1, 'Asia/Shanghai', 'zh-CN');

INSERT INTO user_identities (user_id, identity_type, identifier, credential, is_primary, verified_at, extra_meta)
VALUES
  (100000001, 'email', 'test01@example.com', '$2b$10$rwoAJ4Dkn8wvPZrc9WkcTedz.GiIm44pIK0Fvbx3OCAwtm37Cj88W', 1, NOW(), NULL),
  (100000002, 'email', 'test02@example.com', '$2b$10$rwoAJ4Dkn8wvPZrc9WkcTedz.GiIm44pIK0Fvbx3OCAwtm37Cj88W', 1, NOW(), NULL),
  (100000003, 'email', 'test03@example.com', '$2b$10$rwoAJ4Dkn8wvPZrc9WkcTedz.GiIm44pIK0Fvbx3OCAwtm37Cj88W', 1, NOW(), NULL),
  (100000004, 'email', 'test04@example.com', '$2b$10$rwoAJ4Dkn8wvPZrc9WkcTedz.GiIm44pIK0Fvbx3OCAwtm37Cj88W', 1, NOW(), NULL),
  (100000005, 'email', 'test05@example.com', '$2b$10$rwoAJ4Dkn8wvPZrc9WkcTedz.GiIm44pIK0Fvbx3OCAwtm37Cj88W', 1, NOW(), NULL);

INSERT INTO user_notification_settings (user_id, daily_task_reminder, conflict_alert, milestone_celebration, lagging_warning)
VALUES
  (100000001, 1, 1, 1, 1),
  (100000002, 1, 1, 1, 1),
  (100000003, 1, 1, 1, 1),
  (100000004, 1, 1, 1, 1),
  (100000005, 1, 1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE users AUTO_INCREMENT = 100000006;
