package com.aigp.demo.domain.admin;

import com.aigp.demo.domain.enums.DecompositionIssueType;
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

@Entity
@Table(name = "goal_decomposition_review")
@Getter
@Setter
@NoArgsConstructor
public class GoalDecompositionReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long reviewId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_id", nullable = false)
	private Goal goal;

	@Column
	private Integer score;

	@Enumerated(EnumType.STRING)
	@Column(name = "issue_type", length = 40)
	private DecompositionIssueType issueType;

	@Column(length = 1000)
	private String remark;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reviewer_admin_id", nullable = false)
	private AdminUser reviewer;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
