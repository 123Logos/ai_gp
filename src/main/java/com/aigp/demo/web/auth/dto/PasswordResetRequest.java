package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 通过验证码重置登录密码（成功后所有设备需重新登录）。
 */
@Schema(description = "重置密码请求")
public record PasswordResetRequest(
		@Schema(description = "邮箱或手机号") @NotBlank String account,
		@Schema(description = "短信/邮件验证码") @NotBlank String verificationCode,
		@Schema(description = "新密码，至少 8 位") @NotBlank String newPassword) {}
