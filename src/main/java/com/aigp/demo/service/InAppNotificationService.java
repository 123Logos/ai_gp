package com.aigp.demo.service;

import com.aigp.demo.domain.chat.AiChatSession;
import com.aigp.demo.domain.chat.UserInAppNotification;
import com.aigp.demo.domain.enums.InAppNotificationType;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.config.AppProperties;
import com.aigp.demo.repository.UserInAppNotificationRepository;
import com.aigp.demo.web.user.dto.InAppNotificationItemResponse;
import com.aigp.demo.web.user.dto.InAppNotificationListResponse;
import com.aigp.demo.web.user.dto.UnreadCountResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InAppNotificationService {

	private final AppProperties appProperties;
	private final UserInAppNotificationRepository userInAppNotificationRepository;
	private final ChatRealtimePushService chatRealtimePushService;
	private final AppUserService appUserService;

	@Transactional
	public UserInAppNotification createAndPush(
			AppUser user,
			InAppNotificationType type,
			String title,
			String body,
			Long taskId,
			AiChatSession session,
			Long messageId) {
		UserInAppNotification n = new UserInAppNotification();
		n.setUser(user);
		n.setType(type);
		n.setTitle(title);
		n.setBody(body);
		n.setTaskId(taskId);
		n.setSession(session);
		n.setMessageId(messageId);
		n = userInAppNotificationRepository.save(n);

		if (appProperties.getTaskReminder().isPushEnabled()) {
			long unread = userInAppNotificationRepository.countByUser_IdAndReadAtIsNull(user.getId());
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("type", type.name());
			payload.put("notificationId", n.getId());
			payload.put("sessionId", session == null ? null : session.getId());
			payload.put("messageId", messageId);
			payload.put("taskId", taskId);
			payload.put("title", title);
			payload.put("body", body);
			payload.put("unreadCount", unread);
			chatRealtimePushService.pushToUser(user.getId(), payload);
		}
		return n;
	}

	public void pushChatReply(Long userId, Long sessionId, Long messageId, String contentPreview) {
		if (!appProperties.getChat().isPushOnReplyEnabled()) {
			return;
		}
		long unread = userInAppNotificationRepository.countByUser_IdAndReadAtIsNull(userId);
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("type", "CHAT_REPLY");
		payload.put("sessionId", sessionId);
		payload.put("messageId", messageId);
		payload.put("contentPreview", preview(contentPreview));
		payload.put("unreadCount", unread);
		chatRealtimePushService.pushToUser(userId, payload);
	}

	private static String preview(String content) {
		if (!StringUtils.hasText(content)) {
			return "";
		}
		String t = content.trim();
		return t.length() <= 120 ? t : t.substring(0, 120);
	}

	@Transactional(readOnly = true)
	public UnreadCountResponse unreadCount(Long userId) {
		appUserService.requireActive(userId);
		long count = userInAppNotificationRepository.countByUser_IdAndReadAtIsNull(userId);
		return new UnreadCountResponse(count);
	}

	@Transactional(readOnly = true)
	public InAppNotificationListResponse list(Long userId, boolean unreadOnly) {
		appUserService.requireActive(userId);
		List<UserInAppNotification> list = unreadOnly
				? userInAppNotificationRepository.findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(userId)
				: userInAppNotificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
		List<InAppNotificationItemResponse> items =
				list.stream().map(InAppNotificationItemResponse::fromEntity).toList();
		return new InAppNotificationListResponse(items.size(), items);
	}

	@Transactional
	public void markRead(Long userId, Long notificationId) {
		UserInAppNotification n = userInAppNotificationRepository
				.findByIdAndUser_Id(notificationId, userId)
				.orElseThrow(() -> new NotFoundException("通知不存在: id=" + notificationId));
		if (n.getReadAt() == null) {
			n.setReadAt(LocalDateTime.now());
			userInAppNotificationRepository.save(n);
		}
	}

	@Transactional
	public void markAllRead(Long userId) {
		appUserService.requireActive(userId);
		userInAppNotificationRepository.markAllReadForUser(userId);
	}
}
