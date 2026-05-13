package com.aigp.demo.support.auth;

import com.aigp.demo.domain.user.IdentityType;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 将用户输入的「账号」规范为邮箱或中国大陆手机号，用于注册、发码、找回密码等（不含对外 uid 登录形态）。
 */
public record EmailOrPhoneAccount(IdentityType identityType, String identifier) {

	private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");
	private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+$");

	/**
	 * 解析并规范化账号：邮箱转小写；手机号为连续 11 位数字。
	 *
	 * @param raw 用户输入
	 * @return 身份类型与库表 {@code user_identities.identifier} 一致的形式
	 * @throws IllegalArgumentException 格式不合法时抛出
	 */
	public static EmailOrPhoneAccount parse(String raw) {
		if (raw == null) {
			throw new IllegalArgumentException("账号不能为空");
		}
		String s = raw.trim();
		if (s.isEmpty()) {
			throw new IllegalArgumentException("账号不能为空");
		}
		if (s.contains("@")) {
			if (!SIMPLE_EMAIL.matcher(s).matches()) {
				throw new IllegalArgumentException("邮箱格式不正确");
			}
			return new EmailOrPhoneAccount(IdentityType.email, s.toLowerCase(Locale.ROOT));
		}
		if (CN_MOBILE.matcher(s).matches()) {
			return new EmailOrPhoneAccount(IdentityType.phone, s);
		}
		throw new IllegalArgumentException("账号须为有效邮箱或 11 位中国大陆手机号");
	}
}
