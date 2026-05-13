package com.aigp.demo.domain.admin;

import com.aigp.demo.domain.enums.AdminRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
@NoArgsConstructor
public class AdminUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "real_name", length = 50)
	private String realName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AdminRole role = AdminRole.OPERATOR;

	/**
	 * 账号启用标记：与库中 {@code BIT(1)} / 布尔语义一致。
	 * <p>
	 * 若建表使用 {@code TINYINT(1)}（见 md文档/数据库.md），可改回 {@code Byte} 并去掉 {@code @JdbcTypeCode}。
	 */
	@Column(nullable = false)
	@JdbcTypeCode(SqlTypes.BIT)
	private Boolean status = Boolean.TRUE;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
