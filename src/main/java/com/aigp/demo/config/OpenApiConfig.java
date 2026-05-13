package com.aigp.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 文档元信息配置。
 * <p>
 * 启动后可在浏览器打开 Swagger UI 页面调试 HTTP 接口；具体路径以 {@code springdoc.swagger-ui.path} 为准。
 */
@Configuration
public class OpenApiConfig {

	/**
	 * 注册对外 API 文档的标题、版本与说明，供 SpringDoc 生成 {@code /v3/api-docs} 与 Swagger UI。
	 *
	 * @return OpenAPI 根描述对象
	 */
	@Bean
	public OpenAPI aiGrowthPlannerOpenAPI() {
		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes(
								"bearerAuth",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description("在 Authorization 头携带：`Bearer {accessToken}`")))
				.info(new Info()
						.title("ai成长计划1.0")
						.version("1.0")
						.description("AI 成长计划后端 HTTP API（OpenAPI 3，由 SpringDoc 自动生成）。需鉴权接口请在右上角 Authorize 填入访问令牌。"));
	}
}
