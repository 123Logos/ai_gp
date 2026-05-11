package com.aigp.demo.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(nullable = false, length = 64)
	private String openid;

	@Column(length = 64)
	private String unionid;

	@Column(length = 50)
	private String nickname;

	@Column(name = "avatar_url", length = 500)
	private String avatarUrl;

	@Column(name = "weekly_hours")
	private Integer weeklyHours;

	@Column(name = "current_career", length = 100)
	private String currentCareer;

	@Column(name = "interest_domains", columnDefinition = "json")
	private String interestDomains;

	@Column(name = "last_active_at")
	private LocalDateTime lastActiveAt;

	@Column(name = "profile_completed", nullable = false)
	private boolean profileCompleted;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
