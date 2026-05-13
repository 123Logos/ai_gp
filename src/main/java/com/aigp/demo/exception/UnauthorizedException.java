package com.aigp.demo.exception;

/**
 * 未登录或令牌无效、权限不足时抛出，映射为 HTTP 401。
 */
public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException(String message) {
		super(message);
	}
}
