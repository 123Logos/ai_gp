package com.aigp.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码学相关 Bean（BCrypt），不启用 Spring Security Web 过滤器链。
 */
@Configuration
public class SecurityBeansConfig {

	/**
	 * 用于 {@code user_identities.credential} 的密码哈希与校验。
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
