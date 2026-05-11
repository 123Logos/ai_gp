-- Core C-side tables: users, goals, plan versions, tasks

CREATE TABLE app_user (
  user_id BIGINT NOT NULL AUTO_INCREMENT,
  openid VARCHAR(64) NOT NULL COMMENT 'WeChat openid',
  unionid VARCHAR(64) NULL COMMENT 'WeChat unionid',
  nickname VARCHAR(50) NULL,
  avatar_url VARCHAR(500) NULL,
  weekly_hours INT NULL COMMENT 'Weekly available hours 1-40',
  current_career VARCHAR(100) NULL COMMENT 'Current occupation',
  interest_domains JSON NULL COMMENT 'JSON array: SKILLS,CERTIFICATION,LANGUAGE,SOFT_SKILLS,SIDE_HUSTLE',
  last_active_at DATETIME(3) NULL COMMENT 'Last active time for admin listing',
  profile_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Onboarding profile completed',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_app_user_openid (openid),
  KEY idx_app_user_last_active (last_active_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WeChat end users';

CREATE TABLE goal (
  goal_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description VARCHAR(2000) NULL,
  domain VARCHAR(20) NULL COMMENT 'SKILLS,CERTIFICATION,LANGUAGE,SOFT_SKILLS,SIDE_HUSTLE',
  priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' COMMENT 'HIGH,MEDIUM,LOW',
  deadline DATE NULL,
  status VARCHAR(20) NOT NULL COMMENT 'DECOMPOSING,PENDING,ACTIVE,PAUSED,COMPLETED,ABANDONED,NEED_DECISION,DECOMPOSE_FAILED',
  milestones_json JSON NULL COMMENT 'AI decomposition milestones',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (goal_id),
  KEY idx_goal_user_status (user_id, status),
  KEY idx_goal_user_updated (user_id, updated_at),
  CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES app_user (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User goals';

CREATE TABLE plan (
  plan_id BIGINT NOT NULL AUTO_INCREMENT,
  goal_id BIGINT NOT NULL,
  version INT NOT NULL,
  status VARCHAR(10) NOT NULL COMMENT 'ACTIVE,DEPRECATED',
  trigger_reason VARCHAR(30) NULL COMMENT 'FIRST_SCHEDULE,MANUAL_REGENERATE,AUTO_REGENERATE,PROFILE_CHANGE',
  generated_at DATETIME(3) NULL,
  ai_prompt_version VARCHAR(50) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (plan_id),
  KEY idx_plan_goal_version (goal_id, version),
  KEY idx_plan_goal_status (goal_id, status),
  CONSTRAINT fk_plan_goal FOREIGN KEY (goal_id) REFERENCES goal (goal_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Plan versions';

CREATE TABLE task (
  task_id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  goal_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description VARCHAR(1000) NULL,
  scheduled_date DATE NULL,
  estimated_minutes INT NULL,
  actual_minutes INT NULL,
  quality_score INT NULL COMMENT 'Self rating 1-5',
  status VARCHAR(10) NOT NULL COMMENT 'PENDING,COMPLETED,SKIPPED',
  skip_reason VARCHAR(500) NULL,
  completed_at DATETIME(3) NULL,
  milestone_index INT NULL COMMENT 'Index into milestones_json on goal',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Order within same scheduled_date',
  resources_note VARCHAR(2000) NULL COMMENT 'AI suggested learning resources',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (task_id),
  KEY idx_task_user_date (user_id, scheduled_date),
  KEY idx_task_user_status_date (user_id, status, scheduled_date),
  KEY idx_task_goal (goal_id),
  CONSTRAINT fk_task_plan FOREIGN KEY (plan_id) REFERENCES plan (plan_id) ON DELETE CASCADE,
  CONSTRAINT fk_task_goal FOREIGN KEY (goal_id) REFERENCES goal (goal_id) ON DELETE CASCADE,
  CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES app_user (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Scheduled tasks';
