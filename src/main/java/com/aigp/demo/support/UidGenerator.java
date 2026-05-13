package com.aigp.demo.support;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generates {@code users.uid} values (CHAR(16)) aligned with {@code md文档/数据库.md}.
 */
public final class UidGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();

	private UidGenerator() {
	}

	/** 16-character external id, e.g. {@code U0123456789ABCDE}. */
	public static String nextUid() {
		byte[] bytes = new byte[8];
		RANDOM.nextBytes(bytes);
		return "U" + HexFormat.of().formatHex(bytes).substring(0, 15).toUpperCase();
	}
}
