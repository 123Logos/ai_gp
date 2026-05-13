package com.aigp.demo.web.security;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 访问令牌（JWT）的签发与校验：使用 HS256，载荷含用户 id、对外 uid 与会话 id。
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

	private static final String CLAIM_UID = "uid";
	private static final String CLAIM_SID = "sid";

	private final AppProperties appProperties;

	/**
	 * 校验配置中的密钥是否满足 HS256 最小长度要求。
	 *
	 * @throws IllegalStateException 当密钥过短或未配置时抛出（应在部署阶段修复配置）
	 */
	public void assertSecretConfigured() {
		String sk = appProperties.getJwt().getSecretKey();
		if (sk == null || sk.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException(
					"app.jwt.secret-key 未配置或过短：HS256 至少需要 256 bit（32 字节），请设置环境变量 JWT_SECRET_KEY");
		}
	}

	/**
	 * 签发新的访问令牌。
	 *
	 * @param userId    用户内部主键
	 * @param uid       对外 uid
	 * @param sessionId 会话 id，可为 null（不推荐）
	 * @return compact JWT 字符串
	 */
	public String createAccessToken(Long userId, String uid, Long sessionId) {
		assertSecretConfigured();
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(appProperties.getJwt().getAccessTokenExpireMinutes() * 60L);
		String jti = UUID.randomUUID().toString();

		var builder = Jwts.builder()
				.id(jti)
				.subject(String.valueOf(userId))
				.claim(CLAIM_UID, uid)
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(signingKey());

		if (sessionId != null) {
			builder.claim(CLAIM_SID, sessionId);
		}
		return builder.compact();
	}

	/**
	 * 解析并校验访问令牌，失败时抛出 {@link UnauthorizedException}。
	 *
	 * @param token Bearer 后的 JWT 字符串
	 * @return 解析后的声明快照
	 */
	public JwtUserClaims parseAccessToken(String token) {
		assertSecretConfigured();
		try {
			Jws<Claims> jws = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
			Claims c = jws.getPayload();
			Long userId = Long.valueOf(c.getSubject());
			String uid = c.get(CLAIM_UID, String.class);
			Long sessionId = c.get(CLAIM_SID, Long.class);
			String jti = c.getId();
			if (uid == null) {
				throw new UnauthorizedException("令牌缺少 uid 声明");
			}
			return new JwtUserClaims(userId, uid, sessionId, jti);
		} catch (JwtException | IllegalArgumentException e) {
			throw new UnauthorizedException("访问令牌无效或已过期");
		}
	}

	private SecretKey signingKey() {
		return Keys.hmacShaKeyFor(appProperties.getJwt().getSecretKey().getBytes(StandardCharsets.UTF_8));
	}
}
