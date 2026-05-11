package com.aigp.demo.domain.plan;

import com.aigp.demo.domain.enums.PlanStatus;
import com.aigp.demo.domain.enums.PlanTriggerReason;
import com.aigp.demo.domain.goal.Goal;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
public class Plan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "plan_id")
	private Long planId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_id", nullable = false)
	private Goal goal;

	@Column(nullable = false)
	private Integer version;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private PlanStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "trigger_reason", length = 30)
	private PlanTriggerReason triggerReason;

	@Column(name = "generated_at")
	private LocalDateTime generatedAt;

	@Column(name = "ai_prompt_version", length = 50)
	private String aiPromptVersion;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
