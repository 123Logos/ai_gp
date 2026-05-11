package com.aigp.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {
				"spring.autoconfigure.exclude="
						+ "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
						+ "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
						+ "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
						+ "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
		})
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
