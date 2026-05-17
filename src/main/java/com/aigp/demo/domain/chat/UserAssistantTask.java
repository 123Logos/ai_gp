package com.aigp.demo.domain.chat;

import com.aigp.demo.domain.enums.UserAssistantTaskStatus;
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
@Table(name = "user_assistant_tasks")
@Getter
@Setter
@NoArgsConstructor
public class UserAssistantTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserAssistantTaskStatus status = UserAssistantTaskStatus.OPEN;

	@Column(name = "due_date")
	private LocalDate dueDate;

	/** 截止时刻（用户本地时间语义，精确到分；秒恒为 00） */
	@Column(name = "due_at")
	private LocalDateTime dueAt;

	/** 最近一次到期提醒发送时间（避免重复推送） */
	@Column(name = "reminder_sent_at")
	private LocalDateTime reminderSentAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
