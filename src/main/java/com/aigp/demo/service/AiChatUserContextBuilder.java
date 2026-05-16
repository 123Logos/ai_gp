package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.IdentityType;
import com.aigp.demo.repository.UserIdentityRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 为 AI 对话组装 system 上下文：用户画像 + 未完成助手任务摘要。
 */
@Component
@RequiredArgsConstructor
public class AiChatUserContextBuilder {

	private static final String SYSTEM_BASE =
			"""
			你是「AI成长计划」中的智能助手。请用简洁、友好的中文回复用户。
			你可以通过工具帮用户管理个人任务（创建、查询、更新、取消），任务与成长计划中的排期任务相互独立。

			【记待办 / 安排】
			- 用户说「帮我记录」「记一下」「安排」「开会」等时，应优先调用 create_task，并在回复中说明已创建的内容。
			- 未说明具体日期时，dueDate 使用下方【当前日期】，不要反复追问「哪一天」。
			- 用户说了具体时刻（如下午9点、21:00）时，用 create_task 的 dueAt，格式 yyyy-MM-dd HH:mm（精确到分）；仅「某天」无时刻时用 dueDate（yyyy-MM-dd）。
			- 用户在上文已问过日期、本轮只回答「今天」「明天」等时，结合对话历史理解并直接创建任务。

			日期格式：yyyy-MM-dd。请结合下方上下文回复；未提供的信息不要编造。
			""";

	private static final String TASK_TOOL_RULES =
			"""

			【本轮须用任务工具】
			能创建就不要只追问；信息够用时立即 create_task，避免与上文重复确认。
			用户要查看/回顾计划或待办时，必须调用 list_tasks（使用 OpenAI 标准 tool_calls），禁止在正文里写 <tool_call> 等 XML。
			用户问「未来几天」「未来N天」时：list_tasks 的 dueFrom 填明天（yyyy-MM-dd），dueTo 按天数填截止日；不要把「今天」算进未来。
			""";

	private final UserIdentityRepository userIdentityRepository;

	public String buildSystemPrompt(
			AppUser user,
			AiChatDataPlan plan,
			String tasksSummary,
			String intentHint,
			String unsupportedHint,
			List<Long> messageImageAssetIds) {
		StringBuilder sb = new StringBuilder(SYSTEM_BASE);
		appendCurrentDate(sb, user);
		if (messageImageAssetIds != null && !messageImageAssetIds.isEmpty()) {
			String ids = messageImageAssetIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
			sb.append("\n【本轮用户上传图片 assetId】")
					.append(ids)
					.append("\n默认不要把图片记入任务；仅当用户明确说「把图/照片记进待办」时，create_task 传 imageAssetIds。\n");
		}
		if (plan != null && StringUtils.hasText(plan.reason())) {
			sb.append("\n【本轮数据规划说明】").append(plan.reason().trim()).append('\n');
		}
		if (StringUtils.hasText(intentHint)) {
			sb.append("\n【内部意图参考（勿原样复述给用户）】\n").append(intentHint.trim()).append('\n');
		}
		if (StringUtils.hasText(unsupportedHint)) {
			sb.append("\n【暂未开放能力（须在回复中说明）】\n").append(unsupportedHint.trim()).append('\n');
		}
		if (plan != null && plan.needTaskTools()) {
			sb.append(TASK_TOOL_RULES);
		}
		if (plan == null || plan.needUserProfile()) {
			sb.append("\n【用户基本信息】\n");
			appendLine(sb, "昵称", user.getNickname());
			appendLine(sb, "对外ID", user.getUid());
			appendLine(sb, "身份/角色", user.getProfileIdentity());
			appendLine(sb, "爱好", user.getProfileHobbies());
			appendLine(sb, "希望探索的方向", user.getProfileExploration());
			if (user.getWeeklyHours() != null) {
				appendLine(sb, "每周可投入小时数", String.valueOf(user.getWeeklyHours()));
			}
			appendLine(sb, "时区", user.getTimezone());
			appendLine(sb, "语言", user.getLanguage());
			appendLine(sb, "已完成首次画像", Boolean.TRUE.equals(user.getOnboardingCompleted()) ? "是" : "否");
			userIdentityRepository
					.findByUser_IdAndIdentityType(user.getId(), IdentityType.email)
					.ifPresent(id -> appendLine(sb, "登录邮箱", id.getIdentifier()));
			userIdentityRepository
					.findByUser_IdAndIdentityType(user.getId(), IdentityType.phone)
					.ifPresent(id -> appendLine(sb, "手机号", maskPhone(id.getIdentifier())));
		}
		if (plan == null || plan.needTaskList()) {
			sb.append("\n【助手任务摘要（详细请用 list_tasks 查询）】\n");
			if (StringUtils.hasText(tasksSummary)) {
				sb.append(tasksSummary.trim());
			} else {
				sb.append("（当前筛选条件下暂无任务）");
			}
		}
		return sb.toString();
	}

	private static void appendCurrentDate(StringBuilder sb, AppUser user) {
		ZoneId zone = resolveZone(user.getTimezone());
		LocalDate today = LocalDate.now(zone);
		sb.append("\n【当前日期】").append(today).append("（时区 ").append(zone.getId()).append("）\n");
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

	private static void appendLine(StringBuilder sb, String label, String value) {
		if (StringUtils.hasText(value)) {
			sb.append("- ").append(label).append("：").append(value.trim()).append('\n');
		}
	}

	private static String maskPhone(String phone) {
		if (phone == null || phone.length() < 11) {
			return phone;
		}
		return phone.substring(0, 3) + "****" + phone.substring(7);
	}
}
