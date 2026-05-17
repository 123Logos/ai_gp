-- 用户首次登录画像（身份、爱好、探索方向等）。在已有库执行一次，可重复执行。
-- 与 AppUser 字段：profile_identity, profile_hobbies, profile_exploration, onboarding_completed 对应。

SET @db = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_identity') = 0,
    'ALTER TABLE users ADD COLUMN profile_identity VARCHAR(200) DEFAULT NULL COMMENT ''身份/角色简述（首次登录画像）'' AFTER language',
    'SELECT ''skip profile_identity'' AS n'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_hobbies') = 0,
    'ALTER TABLE users ADD COLUMN profile_hobbies TEXT DEFAULT NULL COMMENT ''爱好'' AFTER profile_identity',
    'SELECT ''skip profile_hobbies'' AS n'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_exploration') = 0,
    'ALTER TABLE users ADD COLUMN profile_exploration TEXT DEFAULT NULL COMMENT ''希望探索的专业方向等'' AFTER profile_hobbies',
    'SELECT ''skip profile_exploration'' AS n'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'onboarding_completed') = 0,
    'ALTER TABLE users ADD COLUMN onboarding_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否完成首次画像填写'' AFTER profile_exploration',
    'SELECT ''skip onboarding_completed'' AS n'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;
