package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 请求发送短信/邮件验证码（当前为内存验证码，生产需接网关）。
 */
@Schema(description = "发送验证码请求")
public record SendVerificationCodeRequest(
		@Schema(description = "邮箱或 11 位中国大陆手机号") @NotBlank String account) {}
