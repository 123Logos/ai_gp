package com.aigp.demo.web.auth;

import com.aigp.demo.service.AuthService;
import com.aigp.demo.web.auth.dto.ChangePasswordRequest;
import com.aigp.demo.web.auth.dto.LoginRequest;
import com.aigp.demo.web.auth.dto.LogoutRequest;
import com.aigp.demo.web.auth.dto.PasswordResetRequest;
import com.aigp.demo.web.auth.dto.RefreshTokenRequest;
import com.aigp.demo.web.auth.dto.RegisterRequest;
import com.aigp.demo.web.auth.dto.SendRegisterVerificationCodeRequest;
import com.aigp.demo.web.auth.dto.SendVerificationCodeRequest;
import com.aigp.demo.web.auth.dto.SendVerificationCodeResponse;
import com.aigp.demo.web.auth.dto.TokenResponse;
import com.aigp.demo.web.security.CurrentUser;
import com.aigp.demo.web.security.JwtUserClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关 HTTP 接口：登录、注册（验证码）、找回密码（验证码）、刷新令牌、修改密码、登出。
 */
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "认证", description = "登录态、令牌与密码相关接口")
public class AuthController {

	private final AuthService authService;

	/**
	 * [登录] 邮箱、手机号或 16 位对外 uid（U 开头）+ 密码，成功返回令牌对（会话由服务端绑定内置设备键，换机不影响）。
	 */
	@PostMapping("/login")
	@Operation(summary = "登录")
	public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		var issued = authService.login(request.account(), request.password(), clientIp(httpRequest));
		return TokenResponse.from(issued);
	}

	/**
	 * [注册-发码] 向未注册邮箱发送验证码（配置 SMTP 时走邮件，否则内存 + 可选 debugCode）。
	 */
	@PostMapping("/register/send-code")
	@Operation(summary = "注册：发送邮箱验证码")
	public SendVerificationCodeResponse sendRegisterCode(
			@Valid @RequestBody SendRegisterVerificationCodeRequest request) {
		return SendVerificationCodeResponse.from(authService.sendRegisterVerificationCode(request.email()));
	}

	/**
	 * [注册] 邮箱 + 密码 + 昵称 + 邮箱验证码，成功后自动登录并返回令牌对。
	 */
	@PostMapping("/register")
	@Operation(summary = "注册")
	public TokenResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
		var issued = authService.register(
				request.email(),
				request.password(),
				request.verificationCode(),
				request.nickname(),
				clientIp(httpRequest));
		return TokenResponse.from(issued);
	}

	/**
	 * [找回密码-发码] 向已注册账号发送验证码。
	 */
	@PostMapping("/password/reset/send-code")
	@Operation(summary = "找回密码：发送验证码")
	public SendVerificationCodeResponse sendPasswordResetCode(@Valid @RequestBody SendVerificationCodeRequest request) {
		return SendVerificationCodeResponse.from(authService.sendPasswordResetVerificationCode(request.account()));
	}

	/**
	 * [找回密码] 验证码 + 新密码；成功后撤销全部会话。
	 */
	@PostMapping("/password/reset")
	@Operation(summary = "找回密码：重置密码")
	public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
		authService.resetPasswordWithCode(request.account(), request.verificationCode(), request.newPassword());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * [刷新令牌] 使用 refresh_token + device_id 滚动续期，返回新的访问令牌与刷新令牌。
	 */
	@PostMapping("/refresh")
	@Operation(summary = "刷新访问令牌")
	public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest body, HttpServletRequest request) {
		var issued = authService.refresh(body.refreshToken(), clientIp(request));
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

	private static String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(xff)) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
