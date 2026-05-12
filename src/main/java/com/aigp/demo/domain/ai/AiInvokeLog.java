package com.aigp.demo.domain.ai;

import com.aigp.demo.domain.enums.AiInvokeStatus;
import com.aigp.demo.domain.enums.AiInvokeType;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.domain.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ai_invoke_logs")
@Getter
@Setter
@NoArgsConstructor
public class AiInvokeLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goal_id")
	private Goal goal;

	@Enumerated(EnumType.STRING)
	@Column(name = "invoke_type", nullable = false, length = 20)
	private AiInvokeType invokeType;

	@Column(length = 20, nullable = false)
	private String provider = "qwen";

	@Column(length = 30, nullable = false)
	private String model;

	@Column(columnDefinition = "text")
	private String prompt;

	@Column(name = "prompt_tokens")
	private Integer promptTokens;

	@Column(name = "completion_tokens")
	private Integer completionTokens;

	@Column(name = "cost_cny", precision = 10, scale = 6)
	private BigDecimal costCny;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private AiInvokeStatus status = AiInvokeStatus.SUCCESS;

	@Column(name = "response_time_ms")
	private Integer responseTimeMs;

	@Column(name = "error_message", length = 500)
	private String errorMessage;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
