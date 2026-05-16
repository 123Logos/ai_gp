package com.aigp.demo.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * 根据用户话术推断助手任务列表的截止日期范围（如「未来几天」不含今天）。
 */
public record AssistantTaskDateFilter(LocalDate dueFromInclusive, LocalDate dueToInclusive) {

	private static final Pattern FUTURE_N_DAYS = Pattern.compile("未来\\s*(\\d+)\\s*天");

	public static AssistantTaskDateFilter fromArgs(String dueFrom, String dueTo) {
		LocalDate from = parseDate(dueFrom);
		LocalDate to = parseDate(dueTo);
		if (from == null && to == null) {
			return null;
		}
		return new AssistantTaskDateFilter(from, to);
	}

	/**
	 * 用户问「未来几天/未来N天」时：从明天起算，不把「今天」算作未来（避免晚间仍列出当日待办）。
	 */
	public static AssistantTaskDateFilter inferFromUserMessage(String userMessage, ZoneId zone) {
		if (!StringUtils.hasText(userMessage)) {
			return null;
		}
		String msg = userMessage.trim();
		if (!msg.contains("未来")) {
			return null;
		}
		LocalDate today = LocalDate.now(zone);
		LocalDate from = today.plusDays(1);
		LocalDate to = today.plusDays(7);
		Matcher m = FUTURE_N_DAYS.matcher(msg);
		if (m.find()) {
			try {
				int days = Integer.parseInt(m.group(1));
				if (days > 0) {
					to = today.plusDays(days);
				}
			} catch (NumberFormatException ignored) {
				// 使用默认 7 天
			}
		}
		return new AssistantTaskDateFilter(from, to);
	}

	public static AssistantTaskDateFilter merge(AssistantTaskDateFilter explicit, AssistantTaskDateFilter inferred) {
		if (explicit == null) {
			return inferred;
		}
		if (inferred == null) {
			return explicit;
		}
		LocalDate from = explicit.dueFromInclusive() != null ? explicit.dueFromInclusive() : inferred.dueFromInclusive();
		LocalDate to = explicit.dueToInclusive() != null ? explicit.dueToInclusive() : inferred.dueToInclusive();
		return new AssistantTaskDateFilter(from, to);
	}

	public boolean matches(LocalDate dueDate) {
		if (dueDate == null) {
			return true;
		}
		if (dueFromInclusive != null && dueDate.isBefore(dueFromInclusive)) {
			return false;
		}
		if (dueToInclusive != null && dueDate.isAfter(dueToInclusive)) {
			return false;
		}
		return true;
	}

	private static LocalDate parseDate(String s) {
		if (!StringUtils.hasText(s)) {
			return null;
		}
		try {
			return LocalDate.parse(s.trim());
		} catch (Exception e) {
			return null;
		}
	}
}
