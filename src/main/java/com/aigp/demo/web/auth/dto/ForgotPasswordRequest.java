package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 忘记密码请求体（当前接口统一返回「功能暂未开放」）。
 */
@Schema(description = "忘记密码请求（占位）")
public record ForgotPasswordRequest(
		@Schema(description = "注册时使用的邮箱或手机号") @NotBlank String account) {}
