package com.aigp.demo.domain.media;

import com.aigp.demo.domain.chat.AiChatMessage;
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
@Table(name = "ai_chat_message_media")
@IdClass(AiChatMessageMedia.Pk.class)
@Getter
@Setter
@NoArgsConstructor
public class AiChatMessageMedia {

	@Id
	@Column(name = "message_id")
	private Long messageId;

	@Id
	@Column(name = "asset_id")
	private Long assetId;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", insertable = false, updatable = false)
	private AiChatMessage message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asset_id", insertable = false, updatable = false)
	private UserMediaAsset asset;

	public record Pk(Long messageId, Long assetId) implements Serializable {}

	public static AiChatMessageMedia of(Long messageId, Long assetId, int sortOrder) {
		AiChatMessageMedia row = new AiChatMessageMedia();
		row.setMessageId(messageId);
		row.setAssetId(assetId);
		row.setSortOrder(sortOrder);
		return row;
	}
}
