package com.aigp.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Jwt jwt = new Jwt();
	private final Auth auth = new Auth();
	private final Admin admin = new Admin();
	private final Llm llm = new Llm();
	private final Vlm vlm = new Vlm();

	@Getter
	@Setter
	public static class Jwt {
		/**
		 * HS256 签名密钥（建议通过环境变量注入，长度至少 32 字节）。
		 */
		private String secretKey = "";
		private long accessTokenExpireMinutes = 1440;
		/**
		 * 刷新令牌在数据库中的有效天数（会话 {@code user_sessions.expires_at}）。
		 */
		private int refreshTokenExpireDays = 30;
	}

	/**
	 * 注册 / 找回密码等场景的验证码策略（当前为进程内内存实现，生产需接短信或邮件网关）。
	 */
	@Getter
	@Setter
	public static class Auth {
		/** 验证码有效时长（秒） */
		private int verificationTtlSeconds = 300;
		/**
		 * 为 true 时，发码接口响应体会带上明文验证码，仅用于本地联调；生产环境必须为 false。
		 */
		private boolean verificationDebugReturnCode = false;
	}

	@Getter
	@Setter
	public static class Admin {
		private final Bootstrap bootstrap = new Bootstrap();

		@Getter
		@Setter
		public static class Bootstrap {
			private String username = "admin";
			private String password = "";
		}
	}

	@Getter
	@Setter
	public static class Llm {
		private String apiKey = "";
		private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
		private String model = "qwen3.5-flash";
	}

	@Getter
	@Setter
	public static class Vlm {
		private String apiKey = "";
		private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
		private String model = "qwen-vl-max-latest";
	}
}
