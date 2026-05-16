package com.aigp.demo.web.user.dto;

import com.aigp.demo.domain.chat.UserAssistantTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "助手任务（AI 对话中创建/管理的待办）")
public record UserAssistantTaskItemResponse(
		@Schema(description = "任务 ID") Long id,
		@Schema(description = "标题") String title,
		@Schema(description = "描述") String description,
		@Schema(description = "状态：OPEN / DONE / CANCELLED") String status,
		@Schema(description = "截止日期 yyyy-MM-dd") LocalDate dueDate,
		@Schema(description = "截止时刻 yyyy-MM-dd HH:mm（用户本地，精确到分）") LocalDateTime dueAt,
		@Schema(description = "任务附图 URL") List<String> imageUrls,
		@Schema(description = "最近一次到期提醒发送时间") LocalDateTime reminderSentAt,
		@Schema(description = "创建时间") LocalDateTime createdAt,
		@Schema(description = "更新时间") LocalDateTime updatedAt) {

	public static UserAssistantTaskItemResponse fromEntity(UserAssistantTask t, List<String> imageUrls) {
		return new UserAssistantTaskItemResponse(
				t.getId(),
				t.getTitle(),
				t.getDescription(),
				t.getStatus().name(),
				t.getDueDate(),
				t.getDueAt(),
				imageUrls == null ? List.of() : imageUrls,
				t.getReminderSentAt(),
				t.getCreatedAt(),
				t.getUpdatedAt());
	}
}
