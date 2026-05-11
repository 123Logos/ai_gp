package com.aigp.demo.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_subscription_pref")
@Getter
@Setter
@NoArgsConstructor
public class UserSubscriptionPref {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pref_id")
	private Long prefId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private AppUser user;

	@Column(name = "daily_reminder", nullable = false)
	private boolean dailyReminder = true;

	@Column(name = "conflict_notify", nullable = false)
	private boolean conflictNotify = true;

	@Column(name = "milestone_celebrate", nullable = false)
	private boolean milestoneCelebrate = true;

	@Column(name = "lag_warning", nullable = false)
	private boolean lagWarning = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
