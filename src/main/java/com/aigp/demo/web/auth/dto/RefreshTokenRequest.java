package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 刷新访问令牌请求体。
 */
@Schema(description = "刷新令牌请求")
public record RefreshTokenRequest(
		@Schema(description = "登录时下发的明文 refresh_token") @NotBlank String refreshToken,
		@Schema(description = "与创建会话时一致的 device_id") @NotBlank String deviceId) {}
