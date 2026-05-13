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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 16)
	private String uid;

	@Column(length = 50)
	private String nickname;

	@Column(name = "avatar_url", length = 500)
	private String avatarUrl;

	/**
	 * 每周可投入小时数（0–40），与库表 {@code TINYINT UNSIGNED} 一致；未设置可为 {@code null}（库默认 0）。
	 */
	@Column(name = "weekly_hours")
	private Byte weeklyHours;

	@Column(nullable = false)
	private Byte status = 1;

	@Column(nullable = false, length = 50)
	private String timezone = "Asia/Shanghai";

	@Column(nullable = false, length = 10)
	private String language = "zh-CN";

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
