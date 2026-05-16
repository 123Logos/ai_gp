package com.aigp.demo.domain.media;

import com.aigp.demo.domain.chat.UserAssistantTask;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_assistant_task_media")
@IdClass(UserAssistantTaskMedia.Pk.class)
@Getter
@Setter
@NoArgsConstructor
public class UserAssistantTaskMedia {

	@Id
	@Column(name = "task_id")
	private Long taskId;

	@Id
	@Column(name = "asset_id")
	private Long assetId;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", insertable = false, updatable = false)
	private UserAssistantTask task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asset_id", insertable = false, updatable = false)
	private UserMediaAsset asset;

	public record Pk(Long taskId, Long assetId) implements Serializable {}

	public static UserAssistantTaskMedia of(Long taskId, Long assetId, int sortOrder) {
		UserAssistantTaskMedia row = new UserAssistantTaskMedia();
		row.setTaskId(taskId);
		row.setAssetId(assetId);
		row.setSortOrder(sortOrder);
		return row;
	}
}
