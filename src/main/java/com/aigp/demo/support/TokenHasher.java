package com.aigp.demo.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 对刷新令牌等敏感串做单向哈希，数据库仅存哈希值以降低泄露风险。
 */
public final class TokenHasher {

	private TokenHasher() {}

	/**
	 * 计算 UTF-8 字符串的 SHA-256 十六进制小写串（64 字符）。
	 *
	 * @param raw 明文
	 * @return 64 位十六进制哈希
	 */
	public static String sha256Hex(String raw) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
