package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * 判断用户本地是否处于「周六早上推送本周回顾」的时间窗（默认 08:00，与任务仅日期提醒一致）。
 */
public final class CompanionDigestDeliveryEvaluator {

	private CompanionDigestDeliveryEvaluator() {}

	public static boolean shouldDeliverNow(AppUser user, LocalTime deliveryTime) {
		ZoneId zone = TaskReminderDueEvaluator.resolveZone(user.getTimezone());
		LocalDateTime nowMinute = LocalDateTime.now(zone).truncatedTo(ChronoUnit.MINUTES);
		if (nowMinute.getDayOfWeek() != DayOfWeek.SATURDAY) {
			return false;
		}
		LocalTime target = deliveryTime == null ? LocalTime.of(8, 0) : deliveryTime;
		LocalDateTime slot = LocalDateTime.of(nowMinute.toLocalDate(), target).truncatedTo(ChronoUnit.MINUTES);
		return nowMinute.equals(slot);
	}
}
