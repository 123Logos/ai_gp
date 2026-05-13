package com.aigp.demo.web.auth.dto;

import com.aigp.demo.service.AuthService;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OAuth2 风格的令牌响应（访问令牌 + 刷新令牌）。
 */
@Schema(description = "令牌对响应")
public record TokenResponse(
		@Schema(description = "访问令牌 JWT") String accessToken,
		@Schema(description = "新的明文刷新令牌（请安全存储）") String refreshToken,
		@Schema(description = "令牌类型，固定 Bearer") String tokenType,
		@Schema(description = "访问令牌有效秒数") long expiresIn,
		@Schema(description = "用户对外 uid") String uid) {

	public static TokenResponse from(AuthService.IssuedTokens t) {
		return new TokenResponse(t.accessToken(), t.refreshToken(), t.tokenType(), t.expiresInSeconds(), t.uid());
	}
}
