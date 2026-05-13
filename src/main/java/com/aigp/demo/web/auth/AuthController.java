package com.aigp.demo.web.auth;

import com.aigp.demo.service.AuthService;
import com.aigp.demo.web.auth.dto.ChangePasswordRequest;
import com.aigp.demo.web.auth.dto.ForgotPasswordRequest;
import com.aigp.demo.web.auth.dto.LoginRequest;
import com.aigp.demo.web.auth.dto.LogoutRequest;
import com.aigp.demo.web.auth.dto.RefreshTokenRequest;
import com.aigp.demo.web.auth.dto.RegisterRequest;
import com.aigp.demo.web.auth.dto.TokenResponse;
import com.aigp.demo.web.security.CurrentUser;
import com.aigp.demo.web.security.JwtUserClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关 HTTP 接口：登录/注册/找回密码占位、刷新令牌、修改密码、登出。
 */
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "认证", description = "登录态、令牌与密码相关接口")
public class AuthController {

	private final AuthService authService;

	/**
	 * [登录] 当前产品阶段固定返回 503，占位供前端联调路径与契约。
	 */
	@PostMapping("/login")
	@Operation(summary = "登录（暂不可用）")
	public void login(@Valid @RequestBody LoginRequest request) {
		authService.loginDisabled();
	}

	/**
	 * [注册] 当前产品阶段固定返回 503。
	 */
	@PostMapping("/register")
	@Operation(summary = "注册（暂不可用）")
	public void register(@Valid @RequestBody RegisterRequest request) {
		authService.registerDisabled();
	}

	/**
	 * [忘记密码] 当前产品阶段固定返回 503（邮件/短信通道未接入）。
	 */
	@PostMapping("/forgot-password")
	@Operation(summary = "忘记密码（暂未开放）")
	public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPasswordDisabled();
	}

	/**
	 * [刷新令牌] 使用 refresh_token + device_id 滚动续期，返回新的访问令牌与刷新令牌。
	 */
	@PostMapping("/refresh")
	@Operation(summary = "刷新访问令牌")
	public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest body, HttpServletRequest request) {
		var issued = authService.refresh(
				body.refreshToken(),
				body.deviceId(),
				body.platform(),
				body.deviceName(),
				clientIp(request));
		return TokenResponse.from(issued);
	}

	/**
	 * [修改密码] 需 Bearer 访问令牌；成功后撤销除当前会话外的其他设备会话。
	 */
	@PostMapping("/change-password")
	@Operation(summary = "修改密码")
	@SecurityRequirement(name = "bearerAuth")
	public void changePassword(
			@CurrentUser JwtUserClaims user, @Valid @RequestBody ChangePasswordRequest body) {
		authService.changePassword(user, body.oldPassword(), body.newPassword());
	}

	/**
	 * [登出] 默认撤销当前 JWT 对应会话；{@code allDevices=true} 时撤销该用户全部会话。
	 */
	@PostMapping("/logout")
	@Operation(summary = "登出")
	@SecurityRequirement(name = "bearerAuth")
	public void logout(@CurrentUser JwtUserClaims user, @RequestBody(required = false) LogoutRequest body) {
		boolean all = body != null && body.allDevicesOrDefault();
		authService.logout(user, all);
	}

	/**
	 * 从代理头或直连连接中解析客户端 IP（用于写入会话表审计字段）。
	 */
	private static String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(xff)) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
