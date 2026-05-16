package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "注册：发送邮箱验证码请求")
public record SendRegisterVerificationCodeRequest(
		@Schema(description = "未注册的有效邮箱") @NotBlank @Email String email) {}
