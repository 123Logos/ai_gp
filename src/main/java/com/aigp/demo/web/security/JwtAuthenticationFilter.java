package com.aigp.demo.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 从 {@code Authorization: Bearer} 解析 JWT，把当前用户声明写入请求属性：
 * <ul>
 *   <li>{@link #REQUEST_ATTR_CLAIMS} — {@link JwtUserClaims}
 * </ul>
 * 公开路径（见 {@link AuthPathPatterns}）跳过校验；保护路径缺失或非法令牌时返回 401 JSON。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String REQUEST_ATTR_CLAIMS = "com.aigp.demo.security.jwtClaims";

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenService jwtTokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		String cp = request.getContextPath();
		if (org.springframework.util.StringUtils.hasText(cp) && path.startsWith(cp)) {
			path = path.substring(cp.length());
		}
		// 兼容带 context-path 的部署：只取 servletPath 已在 dispatcher 中处理；此处用 path 可能含 context，简化用 uri
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}
		if (AuthPathPatterns.isPublicPath(path)) {
			filterChain.doFilter(request, response);
			return;
		}
		if (!path.startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader("Authorization");
		if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
			writeUnauthorized(response, "缺少 Authorization Bearer 访问令牌");
			return;
		}
		String token = header.substring(BEARER_PREFIX.length()).trim();
		if (!StringUtils.hasText(token)) {
			writeUnauthorized(response, "访问令牌为空");
			return;
		}
		try {
			jwtTokenService.assertSecretConfigured();
			JwtUserClaims claims = jwtTokenService.parseAccessToken(token);
			request.setAttribute(REQUEST_ATTR_CLAIMS, claims);
		} catch (IllegalStateException ex) {
			writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CONFIG_ERROR", ex.getMessage());
			return;
		} catch (Exception ex) {
			writeUnauthorized(response, ex.getMessage());
			return;
		}
		filterChain.doFilter(request, response);
	}

	private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
		writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", message);
	}

	private void writeJson(HttpServletResponse response, int status, String code, String message) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
		response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + escaped + "\"}");
	}
}
