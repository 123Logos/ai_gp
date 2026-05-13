package com.aigp.demo.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登出请求体：可选地撤销全部设备会话。
 */
@Schema(description = "登出请求")
public record LogoutRequest(
		@Schema(description = "为 true 时撤销该用户下所有会话，仅保留「全部下线」语义；默认 false 仅撤销当前 JWT 对应会话")
				Boolean allDevices) {

	public boolean allDevicesOrDefault() {
		return Boolean.TRUE.equals(allDevices);
	}
}
