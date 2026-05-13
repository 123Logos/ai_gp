package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 注册请求体（当前接口统一返回「功能暂未开放」）。
 */
@Schema(description = "注册请求（占位）")
public record RegisterRequest(
		@Schema(description = "邮箱或手机号") @NotBlank String account,
		@Schema(description = "密码") @NotBlank String password,
		@Schema(description = "客户端设备唯一标识") @NotBlank String deviceId,
		@Schema(description = "昵称，可选") String nickname) {}
