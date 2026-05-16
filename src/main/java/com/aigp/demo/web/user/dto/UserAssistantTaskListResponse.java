package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "当前用户的助手任务列表")
public record UserAssistantTaskListResponse(
		@Schema(description = "任务条数") int count, @Schema(description = "任务列表，按最近更新时间倒序") List<UserAssistantTaskItemResponse> tasks) {}
