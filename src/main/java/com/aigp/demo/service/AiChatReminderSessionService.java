package com.aigp.demo.service;

import com.aigp.demo.domain.chat.AiChatMessage;
import com.aigp.demo.domain.chat.AiChatSession;
import com.aigp.demo.domain.enums.ChatMessageRole;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.repository.AiChatMessageRepository;
import com.aigp.demo.repository.AiChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiChatReminderSessionService {

	public static final String REMINDER_SESSION_TITLE = "任务提醒";

	/** 每周陪伴回顾推送会话（不参与周总结素材） */
	public static final String WEEKLY_DIGEST_SESSION_TITLE = "本周回顾";

	private final AiChatSessionRepository aiChatSessionRepository;
	private final AiChatMessageRepository aiChatMessageRepository;

	@Transactional
	public AiChatSession getOrCreateReminderSession(AppUser user, String provider, String model) {
		return getOrCreateSessionByTitle(user, REMINDER_SESSION_TITLE, provider, model);
	}

	@Transactional
	public AiChatSession getOrCreateWeeklyDigestSession(AppUser user, String provider, String model) {
		return getOrCreateSessionByTitle(user, WEEKLY_DIGEST_SESSION_TITLE, provider, model);
	}

	private AiChatSession getOrCreateSessionByTitle(
			AppUser user, String title, String provider, String model) {
		return aiChatSessionRepository
				.findFirstByUser_IdAndTitle(user.getId(), title)
				.orElseGet(() -> {
					AiChatSession session = new AiChatSession();
					session.setUser(user);
					session.setTitle(title);
					session.setProvider(provider);
					session.setModel(model);
					return aiChatSessionRepository.save(session);
				});
	}

	@Transactional
	public AiChatMessage appendAssistantMessage(AiChatSession session, String content) {
		AiChatMessage msg = new AiChatMessage();
		msg.setSession(session);
		msg.setRole(ChatMessageRole.ASSISTANT);
		msg.setContent(content);
		return aiChatMessageRepository.save(msg);
	}
}
