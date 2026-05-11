package com.aigp.demo.domain.ai;

import com.aigp.demo.domain.enums.PromptType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ai_prompt")
@Getter
@Setter
@NoArgsConstructor
public class AiPrompt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prompt_id")
	private Long promptId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PromptType type;

	@Column(nullable = false, length = 20)
	private String version;

	@Column(length = 20)
	private String provider;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "is_ab_test", nullable = false)
	private boolean abTest;

	@Column(name = "ab_test_bucket", length = 32)
	private String abTestBucket;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
