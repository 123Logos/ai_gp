package com.aigp.demo.web.websocket;

import com.aigp.demo.service.ChatRealtimePushService;
import com.aigp.demo.web.security.JwtTokenService;
import com.aigp.demo.web.security.JwtUserClaims;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtTokenService jwtTokenService;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			return false;
		}
		String token = servletRequest.getServletRequest().getParameter("token");
		if (!StringUtils.hasText(token)) {
			String auth = servletRequest.getServletRequest().getHeader("Authorization");
			if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
				token = auth.substring(7).trim();
			}
		}
		if (!StringUtils.hasText(token)) {
			return false;
		}
		try {
			jwtTokenService.assertSecretConfigured();
			JwtUserClaims claims = jwtTokenService.parseAccessToken(token);
			attributes.put(ChatRealtimePushService.ATTR_USER_ID, claims.userId());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception) {}
}
