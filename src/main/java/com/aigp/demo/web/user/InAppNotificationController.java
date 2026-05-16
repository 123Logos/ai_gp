package com.aigp.demo.web.user;

import com.aigp.demo.service.InAppNotificationService;
import com.aigp.demo.web.security.CurrentUser;
import com.aigp.demo.web.security.JwtUserClaims;
import com.aigp.demo.web.user.dto.InAppNotificationListResponse;
import com.aigp.demo.web.user.dto.UnreadCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
@Validated
@RequiredArgsConstructor
@Tag(name = "站内通知", description = "任务提醒等未读通知（配合 WebSocket）")
@SecurityRequirement(name = "bearerAuth")
public class InAppNotificationController {

	private final InAppNotificationService inAppNotificationService;

	@GetMapping("/unread-count")
	@Operation(summary = "未读通知数量")
	public UnreadCountResponse unreadCount(@CurrentUser JwtUserClaims user) {
		return inAppNotificationService.unreadCount(user.userId());
	}

	@GetMapping
	@Operation(summary = "通知列表")
	public InAppNotificationListResponse list(
			@CurrentUser JwtUserClaims user, @RequestParam(defaultValue = "false") boolean unreadOnly) {
		return inAppNotificationService.list(user.userId(), unreadOnly);
	}

	@PatchMapping("/{id}/read")
	@Operation(summary = "标记单条通知已读")
	public ResponseEntity<Void> markRead(@CurrentUser JwtUserClaims user, @PathVariable Long id) {
		inAppNotificationService.markRead(user.userId(), id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PatchMapping("/read-all")
	@Operation(summary = "全部标记已读")
	public ResponseEntity<Void> markAllRead(@CurrentUser JwtUserClaims user) {
		inAppNotificationService.markAllRead(user.userId());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
