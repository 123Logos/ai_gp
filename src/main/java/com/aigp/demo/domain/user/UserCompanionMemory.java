package com.aigp.demo.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 每用户一份陪伴长期记忆：周六凌晨由定时任务「先本周、后合并总记忆」更新；
 * {@link #pendingDigestText} 在周六早上与任务提醒一并推送。
 */
@Entity
@Table(name = "user_companion_memory")
@Getter
@Setter
@NoArgsConstructor
public class UserCompanionMemory {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	/** 合并后的长期记忆，注入 AI 对话 system */
	@Column(name = "memory_text", columnDefinition = "MEDIUMTEXT")
	private String memoryText;

	/** 最近一次「本周」内部总结（不直接展示给用户） */
	@Column(name = "week_internal_summary", columnDefinition = "TEXT")
	private String weekInternalSummary;

	/** 待推送的用户可见本周回顾正文 */
	@Column(name = "pending_digest_text", columnDefinition = "TEXT")
	private String pendingDigestText;

	/** 已完成 AI 总结的周键，如 {@code 2026-W20} */
	@Column(name = "summarized_week_key", length = 16)
	private String summarizedWeekKey;

	/** 已推送回顾的周键 */
	@Column(name = "digest_delivered_week_key", length = 16)
	private String digestDeliveredWeekKey;

	@Column(name = "last_summarized_at")
	private LocalDateTime lastSummarizedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
