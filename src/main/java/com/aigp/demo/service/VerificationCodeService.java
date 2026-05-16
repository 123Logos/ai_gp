package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.exception.UnauthorizedException;
import com.aigp.demo.support.mail.VerificationEmailSender;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 验证码生成与进程内校验；若配置了 SMTP，则向邮箱账号发送同一验证码邮件。
 * <p>
 * 手机号仍为内存验证码；生产环境建议中心化存储（Redis 等）并关闭 {@code app.auth.verification-debug-return-code}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

	/** 与 {@link #storageKey} 组合区分业务场景。 */
	public enum Purpose {
		REGISTER,
		PASSWORD_RESET
	}

	private static final SecureRandom RANDOM = new SecureRandom();

	/** purpose|identifier -> 当前有效验证码 */
	private static final ConcurrentHashMap<String, Holder> STORE = new ConcurrentHashMap<>();

	/** purpose|identifier -> 最近一次成功发码时间戳（毫秒），用于简单频控 */
	private static final ConcurrentHashMap<String, Long> LAST_SEND_MS = new ConcurrentHashMap<>();

	private static final long SEND_INTERVAL_MS = 60_000L;

	private final AppProperties appProperties;
	private final VerificationEmailSender verificationEmailSender;

	/**
	 * 生成并保存验证码，返回有效秒数及（可选）调试明文；邮箱账号且已配置 SMTP 时发送邮件。
	 */
	public IssueResult issue(Purpose purpose, String identifier) {
		String key = storageKey(purpose, identifier);
		long now = System.currentTimeMillis();
		Long last = LAST_SEND_MS.get(key);
		if (last != null && now - last < SEND_INTERVAL_MS) {
			throw new IllegalArgumentException("验证码发送过于频繁，请 60 秒后再试");
		}
		int ttl = Math.max(60, appProperties.getAuth().getVerificationTtlSeconds());
		String code = String.format("%06d", RANDOM.nextInt(1_000_000));
		Instant exp = Instant.now().plusSeconds(ttl);
		STORE.put(key, new Holder(code, exp));
		LAST_SEND_MS.put(key, now);
		try {
			if (identifier.contains("@")) {
				if (verificationEmailSender.isMailConfigured()) {
					verificationEmailSender.sendVerificationCode(identifier, purpose, code, ttl);
				} else {
					log.warn(
							"账号为邮箱但未配置 spring.mail（SMTP），验证码仅在服务端内存有效；生产环境请配置 QQ 邮箱等，见 md文档/QQ邮箱SMTP与验证码.md");
				}
			}
		} catch (RuntimeException e) {
			STORE.remove(key);
			LAST_SEND_MS.remove(key);
			throw e;
		}
		String debug = appProperties.getAuth().isVerificationDebugReturnCode() ? code : null;
		return new IssueResult(ttl, debug);
	}

	/**
	 * 校验验证码（成功后会删除该条，防止重复使用）。
	 */
	public void verifyAndConsume(Purpose purpose, String identifier, String inputCode) {
		if (!StringUtils.hasText(inputCode)) {
			throw new UnauthorizedException("验证码错误");
		}
		String key = storageKey(purpose, identifier);
		Holder h = STORE.get(key);
		if (h == null || Instant.now().isAfter(h.expiresAt())) {
			throw new UnauthorizedException("验证码无效或已过期，请重新获取");
		}
		if (!h.code().equals(inputCode.trim())) {
			throw new UnauthorizedException("验证码错误");
		}
		STORE.remove(key);
	}

	private static String storageKey(Purpose purpose, String identifier) {
		return purpose.name() + "|" + identifier;
	}

	private record Holder(String code, Instant expiresAt) {}

	/**
	 * 发码结果：有效时长与（仅调试时）明文验证码。
	 */
	public record IssueResult(int expiresInSeconds, String debugCode) {}
}
