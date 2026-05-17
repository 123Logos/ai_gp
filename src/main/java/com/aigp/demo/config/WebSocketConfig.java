package com.aigp.demo.config;

import com.aigp.demo.web.websocket.ChatWebSocketHandler;
import com.aigp.demo.web.websocket.JwtWebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler chatWebSocketHandler;
	private final JwtWebSocketHandshakeInterceptor jwtWebSocketHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler, "/ws/v1/chat")
				.addInterceptors(jwtWebSocketHandshakeInterceptor)
				.setAllowedOrigins("*");
	}
}
