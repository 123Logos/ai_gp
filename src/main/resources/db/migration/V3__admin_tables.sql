-- Admin users, decomposition review, user tags, subscription preferences

CREATE TABLE admin_user (
  admin_id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'OPERATOR',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (admin_id),
  UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='B-side admin accounts';

CREATE TABLE goal_decomposition_review (
  review_id BIGINT NOT NULL AUTO_INCREMENT,
  goal_id BIGINT NOT NULL,
  score INT NULL COMMENT 'Quality score 1-5',
  issue_type VARCHAR(40) NULL COMMENT 'MISSING_MILESTONE,UNFAIR_DURATION,DEPENDENCY_ERROR,FORMAT_ERROR,OTHER',
  remark VARCHAR(1000) NULL,
  reviewer_admin_id BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (review_id),
  KEY idx_gdr_goal (goal_id),
  CONSTRAINT fk_gdr_goal FOREIGN KEY (goal_id) REFERENCES goal (goal_id) ON DELETE CASCADE,
  CONSTRAINT fk_gdr_admin FOREIGN KEY (reviewer_admin_id) REFERENCES admin_user (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Ops review of AI decomposition quality';

CREATE TABLE user_admin_tag (
  tag_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  tag VARCHAR(32) NOT NULL COMMENT 'e.g. LIMIT_AI',
  reason VARCHAR(500) NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (tag_id),
  KEY idx_uat_user (user_id),
  CONSTRAINT fk_uat_user FOREIGN KEY (user_id) REFERENCES app_user (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_uat_admin FOREIGN KEY (created_by) REFERENCES admin_user (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Manual abnormal user annotations';

CREATE TABLE user_subscription_pref (
  pref_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  daily_reminder TINYINT(1) NOT NULL DEFAULT 1,
  conflict_notify TINYINT(1) NOT NULL DEFAULT 1,
  milestone_celebrate TINYINT(1) NOT NULL DEFAULT 1,
  lag_warning TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (pref_id),
  UNIQUE KEY uk_usp_user (user_id),
  CONSTRAINT fk_usp_user FOREIGN KEY (user_id) REFERENCES app_user (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WeChat subscription toggles per user';
