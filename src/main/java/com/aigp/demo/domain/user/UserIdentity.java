package com.aigp.demo.domain.user;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
		name = "user_identities",
		uniqueConstraints = @UniqueConstraint(name = "uk_identity", columnNames = {"identity_type", "identifier"}))
@Getter
@Setter
@NoArgsConstructor
public class UserIdentity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "identity_type", nullable = false, length = 20)
	private IdentityType identityType;

	@Column(nullable = false, length = 255)
	private String identifier;

	@Column(length = 255)
	private String credential;

	@Column(name = "is_primary", nullable = false)
	private boolean primary;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Column(name = "extra_meta", columnDefinition = "json")
	private String extraMeta;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
