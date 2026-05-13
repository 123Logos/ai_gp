package com.aigp.demo.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一错误响应体，便于前端按 {@code code} 做分支处理。
 *
 * @param code    机器可读错误码（如 {@code FEATURE_DISABLED}）
 * @param message 人类可读说明（可对用户展示）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorBody(String code, String message) {}
