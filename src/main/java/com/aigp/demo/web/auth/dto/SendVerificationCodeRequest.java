package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 找回密码等场景：邮箱或手机号发码（注册发码请使用 {@link SendRegisterVerificationCodeRequest}）。
 */
@Schema(description = "发送验证码请求（找回密码等）")
public record SendVerificationCodeRequest(
		@Schema(description = "邮箱或 11 位中国大陆手机号") @NotBlank String account) {}
