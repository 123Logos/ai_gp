package com.aigp.demo.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;

/**
 * 陪伴记忆周键：与用户时区对齐的 ISO 周年周（如 {@code 2026-W20}）。
 */
public final class CompanionMemoryWeekKeys {

	private CompanionMemoryWeekKeys() {}

	public static String weekKeyForDate(LocalDate date, ZoneId zone) {
		LocalDate d = date == null ? LocalDate.now(zone) : date;
		WeekFields wf = WeekFields.ISO;
		int week = d.get(wf.weekOfWeekBasedYear());
		int year = d.get(wf.weekBasedYear());
		return String.format("%04d-W%02d", year, week);
	}

	public static String currentWeekKey(ZoneId zone) {
		return weekKeyForDate(LocalDate.now(zone), zone);
	}
}
