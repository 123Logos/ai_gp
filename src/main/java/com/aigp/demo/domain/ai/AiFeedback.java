package com.aigp.demo.domain.ai;

import com.aigp.demo.domain.enums.AiFeedbackType;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ai_feedback_records")
@Getter
@Setter
@NoArgsConstructor
public class AiFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goal_id")
	private Goal goal;

	@Enumerated(EnumType.STRING)
	@Column(name = "feedback_type", nullable = false, length = 30)
	private AiFeedbackType feedbackType;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "is_read", nullable = false)
	private boolean readByUser;

	@CreationTimestamp
	@Column(name = "generated_at", nullable = false, updatable = false)
	private LocalDateTime generatedAt;
}
