package com.aigp.demo.domain.chat;

import com.aigp.demo.domain.enums.InAppNotificationType;
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
@Table(name = "user_in_app_notifications")
@Getter
@Setter
@NoArgsConstructor
public class UserInAppNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private InAppNotificationType type;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, length = 2000)
	private String body;

	@Column(name = "task_id")
	private Long taskId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id")
	private AiChatSession session;

	@Column(name = "message_id")
	private Long messageId;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
