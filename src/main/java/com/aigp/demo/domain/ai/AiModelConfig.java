package com.aigp.demo.domain.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ai_model_config")
@Getter
@Setter
@NoArgsConstructor
public class AiModelConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "config_id")
	private Long configId;

	@Column(nullable = false, length = 20)
	private String env;

	@Column(nullable = false, length = 20)
	private String provider;

	@Column(nullable = false, length = 64)
	private String model;

	@Column(nullable = false, precision = 4, scale = 3)
	private BigDecimal temperature = new BigDecimal("0.700");

	@Column(name = "max_tokens", nullable = false)
	private Integer maxTokens = 2048;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
