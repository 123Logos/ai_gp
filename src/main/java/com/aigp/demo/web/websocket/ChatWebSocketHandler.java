package com.aigp.demo.web.websocket;

import com.aigp.demo.service.ChatRealtimePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

	private final ChatRealtimePushService chatRealtimePushService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		Long userId = (Long) session.getAttributes().get(ChatRealtimePushService.ATTR_USER_ID);
		if (userId != null) {
			chatRealtimePushService.register(userId, session);
		}
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
		// 服务端仅下行推送，客户端可发 ping，忽略其它上行
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
		Long userId = (Long) session.getAttributes().get(ChatRealtimePushService.ATTR_USER_ID);
		if (userId != null) {
			chatRealtimePushService.unregister(userId, session);
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
