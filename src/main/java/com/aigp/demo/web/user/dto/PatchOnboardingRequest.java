package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 首次登录 / 画像问卷：部分字段可选，未传的字段保持不变；传空字符串可清空对应文本字段。
 */
@Schema(description = "更新用户首次画像（身份、爱好、探索方向）")
public record PatchOnboardingRequest(
		@Schema(description = "身份或角色简述，如学生、产品经理") @Size(max = 200) String identitySummary,
		@Schema(description = "爱好") @Size(max = 4000) String hobbies,
		@Schema(description = "希望探索的专业方向、领域等") @Size(max = 4000) String explorationInterests,
		@Schema(description = "是否标记为已完成首次画像；未传表示不修改") Boolean onboardingCompleted) {}
