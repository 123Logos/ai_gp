package com.aigp.demo.web.auth.dto;

import com.aigp.demo.service.VerificationCodeService;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 发码成功响应；{@code debugCode} 仅在 {@code app.auth.verification-debug-return-code=true} 时出现。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "发送验证码响应")
public record SendVerificationCodeResponse(
		@Schema(description = "验证码有效秒数") int expiresInSeconds,
		@Schema(description = "调试明文验证码，仅开发环境配置开启时出现") String debugCode) {

	public static SendVerificationCodeResponse from(VerificationCodeService.IssueResult r) {
		return new SendVerificationCodeResponse(r.expiresInSeconds(), r.debugCode());
	}
}
