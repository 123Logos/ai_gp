package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 修改用户资料（部分字段可选，未传的字段保持不变）。
 */
@Schema(description = "更新用户资料请求")
public record UpdateProfileRequest(
		@Schema(description = "昵称") @Size(max = 50) String nickname,
		@Schema(description = "头像 URL") @Size(max = 500) String avatarUrl,
		@Schema(description = "每周可投入小时数 0–40") @Min(0) @Max(40) Integer weeklyHours) {}
