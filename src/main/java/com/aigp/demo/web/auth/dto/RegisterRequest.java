package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册：须先调用发码接口，再提交验证码与密码等信息。
 */
@Schema(description = "注册请求")
public record RegisterRequest(
		@Schema(description = "邮箱或 11 位中国大陆手机号") @NotBlank String account,
		@Schema(description = "登录密码") @NotBlank String password,
		@Schema(description = "短信/邮件验证码") @NotBlank String verificationCode,
		@Schema(description = "昵称，可空则默认「新用户」") @Size(max = 50) String nickname) {}
