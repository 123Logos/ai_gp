package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.domain.chat.AiChatMessage;
import com.aigp.demo.domain.chat.AiChatSession;
import com.aigp.demo.domain.chat.UserAssistantTask;
import com.aigp.demo.domain.enums.InAppNotificationType;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.UserNotificationSettings;
import com.aigp.demo.repository.UserAssistantTaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantTaskReminderService {

	private final AppProperties appProperties;
	private final UserAssistantTaskRepository userAssistantTaskRepository;
	private final UserNotificationSettingsService userNotificationSettingsService;
	private final AiChatReminderSessionService aiChatReminderSessionService;
	private final InAppNotificationService inAppNotificationService;

	@Transactional
	public int sendDueReminders() {
		if (!appProperties.getTaskReminder().isEnabled()) {
			return 0;
		}
		if (!appProperties.getTaskReminder().isInAppEnabled()) {
			return 0;
		}
		LocalDate anchor = LocalDate.now(ZoneId.of(appProperties.getTaskReminder().getZone()));
		List<UserAssistantTask> candidates =
				userAssistantTaskRepository.findOpenTasksDueOnOrBefore(
						com.aigp.demo.domain.enums.UserAssistantTaskStatus.OPEN, anchor);
		int sent = 0;
		for (UserAssistantTask task : candidates) {
			if (trySendReminder(task)) {
				sent++;
			}
		}
		if (sent > 0) {
			log.info("助手任务到期提醒已投递 {} 条", sent);
		}
		return sent;
	}

	private boolean trySendReminder(UserAssistantTask task) {
		AppUser user = task.getUser();
		if (user.getStatus() == null || user.getStatus() != 1) {
			return false;
		}
		ZoneId zone = resolveZone(user.getTimezone());
		LocalDate userToday = LocalDate.now(zone);
		if (task.getDueDate() == null || task.getDueDate().isAfter(userToday)) {
			return false;
		}
		if (task.getReminderSentAt() != null && task.getReminderSentAt().toLocalDate().equals(userToday)) {
			return false;
		}
		UserNotificationSettings settings = userNotificationSettingsService.getOrCreate(user);
		if (!settings.isDailyTaskReminder()) {
			return false;
		}

		deliverInAppReminder(user, task, userToday);
		task.setReminderSentAt(LocalDateTime.now(zone));
		userAssistantTaskRepository.save(task);
		return true;
	}

	private void deliverInAppReminder(AppUser user, UserAssistantTask task, LocalDate userToday) {
		String providerKey = resolveDefaultProviderKey();
		AppProperties.ChatProvider providerConfig = resolveProviderConfig(providerKey);
		AiChatSession session = aiChatReminderSessionService.getOrCreateReminderSession(
				user, providerKey, providerConfig.getModel());
		String chatBody = buildChatReminderBody(task, userToday);
		AiChatMessage message = aiChatReminderSessionService.appendAssistantMessage(session, chatBody);
		String title = buildTitle(task, userToday);
		inAppNotificationService.createAndPush(
				user,
				InAppNotificationType.TASK_DUE_REMINDER,
				title,
				truncateForNotification(chatBody),
				task.getId(),
				session,
				message.getId());
	}

	private String resolveDefaultProviderKey() {
		String def = appProperties.getChat().getDefaultProvider();
		return StringUtils.hasText(def) ? def.trim().toLowerCase(Locale.ROOT) : "mimo";
	}

	private AppProperties.ChatProvider resolveProviderConfig(String providerKey) {
		AppProperties.ChatProvider cfg = appProperties.getChat().getProviders().get(providerKey);
		if (cfg == null || !StringUtils.hasText(cfg.getModel())) {
			cfg = new AppProperties.ChatProvider();
			cfg.setModel("system");
		}
		return cfg;
	}

	private static String buildTitle(UserAssistantTask task, LocalDate userToday) {
		if (task.getDueDate().isBefore(userToday)) {
			return "任务已逾期：" + task.getTitle();
		}
		return "今日待办：" + task.getTitle();
	}

	private static String buildChatReminderBody(UserAssistantTask task, LocalDate userToday) {
		String due = AssistantTaskDueParser.formatDueForDisplay(task);
		if (due == null) {
			due = task.getDueDate().toString();
		}
		String line = task.getDueDate().isBefore(userToday)
				? "你有一项任务原定于 " + due + " 完成，目前已逾期。"
				: "今天（" + due + "）有一项待办需要你关注。";
		StringBuilder sb = new StringBuilder();
		sb.append(line).append("\n\n");
		sb.append("📌 ").append(task.getTitle()).append('\n');
		if (StringUtils.hasText(task.getDescription())) {
			sb.append(task.getDescription().trim()).append('\n');
		}
		sb.append("\n在对话里告诉我「完成了」或「改天再做」，我可以帮你更新任务状态。");
		return sb.toString();
	}

	private static String truncateForNotification(String body) {
		if (body == null) {
			return "";
		}
		String t = body.trim();
		return t.length() <= 500 ? t : t.substring(0, 500);
	}

	private static ZoneId resolveZone(String timezone) {
		if (!StringUtils.hasText(timezone)) {
			return ZoneId.of("Asia/Shanghai");
		}
		try {
			return ZoneId.of(timezone.trim());
		} catch (Exception e) {
			return ZoneId.of("Asia/Shanghai");
		}
	}
}
