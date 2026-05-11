-- AI invoke audit, prompts, end-user feedback, model configuration

CREATE TABLE ai_invoke_log (
  log_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  goal_id BIGINT NULL,
  invoke_type VARCHAR(20) NOT NULL COMMENT 'DECOMPOSE,SCHEDULE,FEEDBACK',
  provider VARCHAR(20) NULL COMMENT 'qwen,wenxin,zhipu',
  model VARCHAR(30) NULL,
  prompt TEXT NULL COMMENT 'Redacted or hashed prompt',
  prompt_tokens INT NULL,
  completion_tokens INT NULL,
  cost_cny DECIMAL(12,4) NULL,
  status VARCHAR(10) NOT NULL COMMENT 'SUCCESS,FAILED',
  response_time_ms INT NULL,
  trace_id VARCHAR(64) NULL COMMENT 'Correlates with MDC',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (log_id),
  KEY idx_ai_log_created (created_at),
  KEY idx_ai_log_user_created (user_id, created_at),
  KEY idx_ai_log_type_status (invoke_type, status),
  CONSTRAINT fk_ai_log_user FOREIGN KEY (user_id) REFERENCES app_user (user_id) ON DELETE SET NULL,
  CONSTRAINT fk_ai_log_goal FOREIGN KEY (goal_id) REFERENCES goal (goal_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI API invocation audit';

CREATE TABLE ai_prompt (
  prompt_id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(20) NOT NULL COMMENT 'DECOMPOSE,SCHEDULE,FEEDBACK',
  version VARCHAR(20) NOT NULL,
  provider VARCHAR(20) NULL,
  content TEXT NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 0,
  is_ab_test TINYINT(1) NOT NULL DEFAULT 0,
  ab_test_bucket VARCHAR(32) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (prompt_id),
  KEY idx_ai_prompt_type_active (type, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt templates and versions';

CREATE TABLE ai_feedback (
  feedback_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_id BIGINT NULL,
  feedback_type VARCHAR(30) NOT NULL COMMENT 'EXECUTION_ANALYSIS,PLAN_OPTIMIZATION,MILESTONE_CELEBRATION',
  summary TEXT NULL,
  detail_json JSON NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (feedback_id),
  KEY idx_ai_feedback_user_created (user_id, created_at),
  KEY idx_ai_feedback_goal (goal_id),
  CONSTRAINT fk_ai_feedback_user FOREIGN KEY (user_id) REFERENCES app_user (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_ai_feedback_goal FOREIGN KEY (goal_id) REFERENCES goal (goal_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI feedback history for users';

CREATE TABLE ai_model_config (
  config_id BIGINT NOT NULL AUTO_INCREMENT,
  env VARCHAR(20) NOT NULL COMMENT 'test,prod',
  provider VARCHAR(20) NOT NULL,
  model VARCHAR(64) NOT NULL,
  temperature DECIMAL(4,3) NOT NULL DEFAULT 0.700,
  max_tokens INT NOT NULL DEFAULT 2048,
  is_active TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (config_id),
  KEY idx_ai_model_env_active (env, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM provider and parameter config';
