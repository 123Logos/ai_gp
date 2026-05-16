package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "站内通知列表")
public record InAppNotificationListResponse(
		@Schema(description = "条数") int count, @Schema(description = "通知列表") List<InAppNotificationItemResponse> items) {}
