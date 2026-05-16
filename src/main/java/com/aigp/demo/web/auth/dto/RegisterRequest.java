package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册：须先对同一邮箱调用发码接口，再提交本请求；手机号等通过登录后 PATCH 资料绑定。
 */
@Schema(description = "注册请求")
public record RegisterRequest(
		@Schema(description = "与发码时相同的邮箱") @NotBlank @Email String email,
		@Schema(description = "登录密码，至少 8 位") @NotBlank String password,
		@Schema(description = "昵称") @NotBlank @Size(max = 50) String nickname,
		@Schema(description = "邮箱收到的 6 位验证码") @NotBlank String verificationCode) {}
