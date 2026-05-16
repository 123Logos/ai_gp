package com.aigp.demo.schedule;

import com.aigp.demo.service.AssistantTaskReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantTaskReminderScheduler {

	private final AssistantTaskReminderService assistantTaskReminderService;

	@Scheduled(cron = "${app.task-reminder.cron:0 0 8 * * ?}", zone = "${app.task-reminder.zone:Asia/Shanghai}")
	public void runDailyReminders() {
		try {
			assistantTaskReminderService.sendDueReminders();
		} catch (Exception e) {
			log.error("助手任务定时提醒执行失败", e);
		}
	}
}
