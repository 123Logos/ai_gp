package com.aigp.demo.web.user.dto;

import com.aigp.demo.domain.user.AppUser;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 用户资料响应（不含敏感字段）。
 */
@Schema(description = "用户资料")
public record UserProfileResponse(
		@Schema(description = "对外 uid") String uid,
		@Schema(description = "昵称") String nickname,
		@Schema(description = "头像") String avatarUrl,
		@Schema(description = "每周可投入小时数") Integer weeklyHours,
		@Schema(description = "时区") String timezone,
		@Schema(description = "语言偏好") String language,
		@Schema(description = "账户状态：1 正常 2 禁用 3 注销") int status,
		@Schema(description = "注册时间") LocalDateTime createdAt,
		@Schema(description = "最近更新时间") LocalDateTime updatedAt) {

	public static UserProfileResponse fromEntity(AppUser u) {
		int st = u.getStatus() == null ? 0 : u.getStatus().intValue();
		Integer wh = u.getWeeklyHours() == null ? null : u.getWeeklyHours().intValue();
		return new UserProfileResponse(
				u.getUid(),
				u.getNickname(),
				u.getAvatarUrl(),
				wh,
				u.getTimezone(),
				u.getLanguage(),
				st,
				u.getCreatedAt(),
				u.getUpdatedAt());
	}
}
