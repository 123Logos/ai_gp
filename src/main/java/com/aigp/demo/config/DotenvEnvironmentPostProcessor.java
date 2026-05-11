package com.aigp.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads a {@code .env} file from the JVM working directory (typically the module root) so that
 * variables match common tooling from Node/Python projects. Missing file is ignored.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
		if (dotenv.entries().isEmpty()) {
			return;
		}
		Map<String, Object> map = new HashMap<>();
		dotenv.entries().forEach(e -> map.put(e.getKey(), e.getValue()));
		environment.getPropertySources().addFirst(new MapPropertySource("dotenv", map));
	}
}
