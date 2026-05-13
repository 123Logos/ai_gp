package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.IdentityType;
import com.aigp.demo.domain.user.UserIdentity;
import com.aigp.demo.domain.user.UserSession;
import com.aigp.demo.exception.FeatureUnavailableException;
import com.aigp.demo.exception.UnauthorizedException;
import com.aigp.demo.repository.UserIdentityRepository;
import com.aigp.demo.repository.UserSessionRepository;
import com.aigp.demo.support.TokenHasher;
import com.aigp.demo.web.security.JwtTokenService;
import com.aigp.demo.web.security.JwtUserClaims;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证相关业务：刷新令牌、修改密码、登出，以及「暂未开放」的登录/注册/找回密码入口。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	public static final String CODE_LOGIN_DISABLED = "AUTH_LOGIN_DISABLED";
	public static final String CODE_REGISTER_DISABLED = "AUTH_REGISTER_DISABLED";
	public static final String CODE_FORGOT_PASSWORD_DISABLED = "AUTH_FORGOT_PASSWORD_DISABLED";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final AppProperties appProperties;
	private final JwtTokenService jwtTokenService;
	private final UserSessionRepository userSessionRepository;
	private final UserSessionService userSessionService;
	private final AppUserService appUserService;
	private final UserIdentityRepository userIdentityRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 登录接口占位：当前产品阶段统一返回「功能不可用」。
	 */
	public void loginDisabled() {
		throw new FeatureUnavailableException(CODE_LOGIN_DISABLED, "登录功能暂时不可用，请稍后再试");
	}

	/**
	 * 注册接口占位：当前产品阶段统一返回「功能不可用」。
	 */
	public void registerDisabled() {
		throw new FeatureUnavailableException(CODE_REGISTER_DISABLED, "注册功能暂时不可用，请稍后再试");
	}

	/**
	 * 忘记密码接口占位：邮件/短信通道未接入前保持关闭。
	 */
	public void forgotPasswordDisabled() {
		throw new FeatureUnavailableException(
				CODE_FORGOT_PASSWORD_DISABLED, "找回密码功能暂未开放，请联系客服或通过其他已绑定方式处理");
	}

	/**
	 * 使用刷新令牌换取新的访问令牌与刷新令牌（滚动更新会话表中的哈希与过期时间）。
	 *
	 * @param refreshTokenPlain 客户端持有的明文刷新令牌
	 * @param deviceId          必须与创建会话时一致，防止令牌被拷贝到其他设备滥用
	 * @param platform          可选，如 ios/android/web
	 * @param deviceName        可选，设备展示名
	 * @param ipAddress         可选，客户端 IP
	 * @return 新令牌对与对外 uid
	 */
	@Transactional
	public IssuedTokens refresh(
			String refreshTokenPlain, String deviceId, String platform, String deviceName, String ipAddress) {
		String hash = TokenHasher.sha256Hex(refreshTokenPlain);
		LocalDateTime now = LocalDateTime.now();
		UserSession session = userSessionRepository
				.findActiveByRefreshTokenHash(hash, now)
				.orElseThrow(() -> new UnauthorizedException("刷新令牌无效或已过期"));

		if (!deviceId.equals(session.getDeviceId())) {
			throw new UnauthorizedException("设备标识与刷新令牌不匹配");
		}

		AppUser user = appUserService.requireActive(session.getUser().getId());

		String newRefreshPlain = randomRefreshToken();
		String newRefreshHash = TokenHasher.sha256Hex(newRefreshPlain);
		LocalDateTime exp = now.plusDays(appProperties.getJwt().getRefreshTokenExpireDays());

		session.setRefreshToken(newRefreshHash);
		session.setExpiresAt(exp);
		session.setPlatform(platform);
		session.setDeviceName(deviceName);
		session.setIpAddress(ipAddress);
		userSessionRepository.save(session);

		String access = jwtTokenService.createAccessToken(user.getId(), user.getUid(), session.getId());
		long expiresInSec = appProperties.getJwt().getAccessTokenExpireMinutes() * 60L;
		return new IssuedTokens(access, newRefreshPlain, "Bearer", expiresInSec, user.getUid());
	}

	/**
	 * 校验原密码后更新哈希；默认保留当前会话，撤销该用户其他设备的刷新会话。
	 *
	 * @param claims      当前访问令牌声明（用于识别要保留的会话 id）
	 * @param oldPassword 原密码
	 * @param newPassword 新密码
	 */
	@Transactional
	public void changePassword(JwtUserClaims claims, String oldPassword, String newPassword) {
		assertPasswordPolicy(newPassword);
		AppUser user = appUserService.requireActive(claims.userId());
		UserIdentity row = findPasswordIdentity(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("当前账号未绑定支持密码登录的邮箱或手机"));

		String stored = row.getCredential();
		if (stored == null || !passwordEncoder.matches(oldPassword, stored)) {
			throw new UnauthorizedException("原密码不正确");
		}
		row.setCredential(passwordEncoder.encode(newPassword));
		userIdentityRepository.save(row);

		revokeOtherSessions(claims.userId(), claims.sessionId());
	}

	/**
	 * 登出：默认仅撤销当前 JWT 对应会话；{@code allDevices=true} 时撤销该用户全部会话。
	 */
	@Transactional
	public void logout(JwtUserClaims claims, boolean allDevices) {
		if (allDevices) {
			userSessionService.revokeAllForUser(claims.userId());
			return;
		}
		Long sid = claims.sessionId();
		if (sid != null) {
			userSessionService.revokeSession(claims.userId(), sid);
		} else {
			// 旧版令牌未携带 sid 时，退化为撤销全部，避免留下「无法定位」的活跃会话
			userSessionService.revokeAllForUser(claims.userId());
		}
	}

	private void revokeOtherSessions(Long userId, Long keepSessionId) {
		LocalDateTime now = LocalDateTime.now();
		List<UserSession> list = userSessionRepository.findByUser_Id(userId);
		for (UserSession s : list) {
			if (s.getRevokedAt() != null) {
				continue;
			}
			if (keepSessionId != null && keepSessionId.equals(s.getId())) {
				continue;
			}
			s.setRevokedAt(now);
		}
	}

	private Optional<UserIdentity> findPasswordIdentity(Long userId) {
		Optional<UserIdentity> email = userIdentityRepository.findByUser_IdAndIdentityType(userId, IdentityType.email);
		if (email.isPresent() && email.get().getCredential() != null) {
			return email;
		}
		Optional<UserIdentity> phone = userIdentityRepository.findByUser_IdAndIdentityType(userId, IdentityType.phone);
		if (phone.isPresent() && phone.get().getCredential() != null) {
			return phone;
		}
		return Optional.empty();
	}

	private static void assertPasswordPolicy(String newPassword) {
		if (newPassword == null || newPassword.length() < 8) {
			throw new IllegalArgumentException("新密码长度至少 8 位");
		}
	}

	private static String randomRefreshToken() {
		byte[] buf = new byte[36];
		SECURE_RANDOM.nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}

	/**
	 * 登录成功或刷新后返回给前端的令牌载体。
	 *
	 * @param accessToken        访问令牌 JWT
	 * @param refreshToken       明文刷新令牌（仅客户端保存，库中存哈希）
	 * @param tokenType          固定为 Bearer
	 * @param expiresInSeconds   访问令牌剩余有效秒数
	 * @param uid                对外用户标识
	 */
	public record IssuedTokens(
			String accessToken, String refreshToken, String tokenType, long expiresInSeconds, String uid) {}
}
