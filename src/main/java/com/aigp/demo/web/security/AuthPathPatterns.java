package com.aigp.demo.web.security;

import org.springframework.util.AntPathMatcher;

/**
 * 定义无需携带访问令牌的公开路径（与 {@link JwtAuthenticationFilter} 配套）。
 */
public final class AuthPathPatterns {

	private static final String[] PUBLIC_ANT = {
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/error",
		"/api/v1/auth/login",
		"/api/v1/auth/register",
		"/api/v1/auth/register/send-code",
		"/api/v1/auth/password/reset",
		"/api/v1/auth/password/reset/send-code",
		"/api/v1/auth/refresh",
		"/api/v1/public/avatars/**"
	};

	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	private AuthPathPatterns() {}

	/**
	 * 判断请求路径是否属于公开接口。
	 *
	 * @param uriWithoutContext 形如 {@code /api/v1/auth/login}
	 * @return true 表示无需 JWT
	 */
	public static boolean isPublicPath(String uriWithoutContext) {
		for (String pattern : PUBLIC_ANT) {
			if (MATCHER.match(pattern, uriWithoutContext)) {
				return true;
			}
		}
		return false;
	}
}
