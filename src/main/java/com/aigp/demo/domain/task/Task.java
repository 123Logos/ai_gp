package com.aigp.demo.domain.task;

import com.aigp.demo.domain.enums.TaskStatus;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.domain.plan.Plan;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "task_id")
	private Long taskId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private Plan plan;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_id", nullable = false)
	private Goal goal;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(name = "scheduled_date")
	private LocalDate scheduledDate;

	@Column(name = "estimated_minutes")
	private Integer estimatedMinutes;

	@Column(name = "actual_minutes")
	private Integer actualMinutes;

	@Column(name = "quality_score")
	private Integer qualityScore;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TaskStatus status;

	@Column(name = "skip_reason", length = 500)
	private String skipReason;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "milestone_index")
	private Integer milestoneIndex;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder = 0;

	@Column(name = "resources_note", length = 2000)
	private String resourcesNote;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
