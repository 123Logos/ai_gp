package com.aigp.demo.schedule;

import com.aigp.demo.service.AssistantTaskReminderService;
import com.aigp.demo.service.CompanionMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每分钟扫描到期的助手任务并投递提醒（精确到分，依赖 {@code due_at}）；
 * 同一 tick 内投递待发送的本周陪伴回顾（用户本地周六 {@code digest-delivery-time}，默认 08:00）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantTaskReminderScheduler {

	private final AssistantTaskReminderService assistantTaskReminderService;
	private final CompanionMemoryService companionMemoryService;

	@Scheduled(cron = "${app.task-reminder.cron:0 * * * * ?}", zone = "${app.task-reminder.zone:Asia/Shanghai}")
	public void runMinuteReminders() {
		try {
			assistantTaskReminderService.sendDueReminders();
		} catch (Exception e) {
			log.error("助手任务到点提醒执行失败", e);
		}
		try {
			companionMemoryService.deliverPendingDigests();
		} catch (Exception e) {
			log.error("本周陪伴回顾推送失败", e);
		}
	}
}
