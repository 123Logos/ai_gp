package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.exception.UnauthorizedException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 进程内验证码存储（注册 / 找回密码），用于在未接入短信网关前的联调与演示。
 * <p>
 * 生产环境应替换为真实短信或邮件发送与中心化存储（Redis 等），并关闭 {@code app.auth.verification-debug-return-code}。
 */
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

	/**
	 * 生成并保存验证码，返回有效秒数及（可选）调试明文。
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
