package com.aigp.demo.exception;

/**
 * 业务功能暂未开放（如登录注册临时关闭），映射为 HTTP 503。
 */
public class FeatureUnavailableException extends RuntimeException {

	private final String featureCode;

	public FeatureUnavailableException(String featureCode, String message) {
		super(message);
		this.featureCode = featureCode;
	}

	public String getFeatureCode() {
		return featureCode;
	}
}
