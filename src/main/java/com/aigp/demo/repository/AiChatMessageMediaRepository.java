package com.aigp.demo.repository;

import com.aigp.demo.domain.media.AiChatMessageMedia;
import com.aigp.demo.domain.media.AiChatMessageMedia.Pk;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiChatMessageMediaRepository extends JpaRepository<AiChatMessageMedia, Pk> {

	List<AiChatMessageMedia> findByMessageIdOrderBySortOrderAsc(Long messageId);

	List<AiChatMessageMedia> findByMessageIdInOrderByMessageIdAscSortOrderAsc(Collection<Long> messageIds);

	@Query(
			"""
			SELECT m.assetId FROM AiChatMessageMedia m
			WHERE m.messageId IN :messageIds
			ORDER BY m.messageId ASC, m.sortOrder ASC
			""")
	List<Long> findAssetIdsByMessageIdIn(@Param("messageIds") Collection<Long> messageIds);
}
