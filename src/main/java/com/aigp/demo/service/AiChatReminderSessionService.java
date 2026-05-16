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

	private final AiChatSessionRepository aiChatSessionRepository;
	private final AiChatMessageRepository aiChatMessageRepository;

	@Transactional
	public AiChatSession getOrCreateReminderSession(AppUser user, String provider, String model) {
		return aiChatSessionRepository
				.findFirstByUser_IdAndTitle(user.getId(), REMINDER_SESSION_TITLE)
				.orElseGet(() -> {
					AiChatSession session = new AiChatSession();
					session.setUser(user);
					session.setTitle(REMINDER_SESSION_TITLE);
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
