package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体：账号支持邮箱、手机号，或 16 位对外 uid（以 U 开头，与 {@code users.uid} 一致）。
 */
@Schema(description = "登录请求")
public record LoginRequest(
		@Schema(description = "登录账号：邮箱、手机号，或 16 位对外 uid（U 开头）") @NotBlank String account,
		@Schema(description = "密码") @NotBlank String password) {}
