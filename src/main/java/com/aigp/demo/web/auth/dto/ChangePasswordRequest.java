package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求体（需携带访问令牌）。
 */
@Schema(description = "修改密码请求")
public record ChangePasswordRequest(
		@Schema(description = "当前密码") @NotBlank String oldPassword,
		@Schema(description = "新密码，至少 8 位") @NotBlank String newPassword) {}
