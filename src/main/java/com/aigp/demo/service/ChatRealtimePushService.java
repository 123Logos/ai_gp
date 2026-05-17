package com.aigp.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRealtimePushService {

	public static final String ATTR_USER_ID = "userId";

	private final ObjectMapper objectMapper;

	private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

	public void register(Long userId, WebSocketSession session) {
		userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
	}

	public void unregister(Long userId, WebSocketSession session) {
		Set<WebSocketSession> set = userSessions.get(userId);
		if (set == null) {
			return;
		}
		set.remove(session);
		if (set.isEmpty()) {
			userSessions.remove(userId);
		}
	}

	public void pushToUser(Long userId, Map<String, Object> payload) {
		Set<WebSocketSession> set = userSessions.get(userId);
		if (set == null || set.isEmpty()) {
			return;
		}
		String json;
		try {
			json = objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			log.warn("WebSocket 推送序列化失败 userId={}", userId, e);
			return;
		}
		TextMessage message = new TextMessage(json);
		for (WebSocketSession ws : set) {
			if (ws.isOpen()) {
				try {
					ws.sendMessage(message);
				} catch (IOException e) {
					log.debug("WebSocket 发送失败 userId={} session={}", userId, ws.getId(), e);
				}
			}
		}
	}
}
