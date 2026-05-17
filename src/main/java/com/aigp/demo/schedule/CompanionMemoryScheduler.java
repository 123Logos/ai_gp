package com.aigp.demo.schedule;

import com.aigp.demo.service.CompanionMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 陪伴记忆：每周六凌晨批量总结；推送由 {@link AssistantTaskReminderScheduler} 每分钟顺带投递。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanionMemoryScheduler {

	private final CompanionMemoryService companionMemoryService;

	@Scheduled(
			cron = "${app.companion-memory.summarize-cron:0 0 3 ? * SAT}",
			zone = "${app.companion-memory.zone:Asia/Shanghai}")
	public void runWeeklySummarize() {
		try {
			companionMemoryService.runWeeklySummarizeBatch();
		} catch (Exception e) {
			log.error("陪伴记忆周总结任务失败", e);
		}
	}
}
