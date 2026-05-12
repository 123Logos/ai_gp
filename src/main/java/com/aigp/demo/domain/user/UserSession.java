package com.aigp.demo.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
		name = "user_sessions",
		uniqueConstraints = @UniqueConstraint(name = "uk_user_device", columnNames = {"user_id", "device_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(name = "device_id", nullable = false, length = 64)
	private String deviceId;

	@Column(length = 20)
	private String platform;

	@Column(name = "device_name", length = 100)
	private String deviceName;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(name = "refresh_token", nullable = false, length = 255)
	private String refreshToken;

	@Column(name = "access_token_jti", length = 64)
	private String accessTokenJti;

	@Column(name = "is_trusted", nullable = false)
	private boolean trusted;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;
}
