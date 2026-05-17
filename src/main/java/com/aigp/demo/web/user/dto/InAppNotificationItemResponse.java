package com.aigp.demo.web.user.dto;

import com.aigp.demo.domain.chat.UserInAppNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "站内通知条目")
public record InAppNotificationItemResponse(
		@Schema(description = "通知 ID") Long id,
		@Schema(description = "类型，如 TASK_DUE_REMINDER") String type,
		@Schema(description = "标题") String title,
		@Schema(description = "正文摘要") String body,
		@Schema(description = "关联任务 ID") Long taskId,
		@Schema(description = "关联会话 ID") Long sessionId,
		@Schema(description = "关联消息 ID") Long messageId,
		@Schema(description = "已读时间，未读为 null") LocalDateTime readAt,
		@Schema(description = "创建时间") LocalDateTime createdAt) {

	public static InAppNotificationItemResponse fromEntity(UserInAppNotification n) {
		return new InAppNotificationItemResponse(
				n.getId(),
				n.getType().name(),
				n.getTitle(),
				n.getBody(),
				n.getTaskId(),
				n.getSession() == null ? null : n.getSession().getId(),
				n.getMessageId(),
				n.getReadAt(),
				n.getCreatedAt());
	}
}
