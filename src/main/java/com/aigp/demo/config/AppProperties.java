package com.aigp.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Jwt jwt = new Jwt();
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
