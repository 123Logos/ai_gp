package com.aigp.demo.repository;

import com.aigp.demo.domain.chat.AiChatMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

	List<AiChatMessage> findBySession_IdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);

	List<AiChatMessage> findBySession_IdOrderByCreatedAtAsc(Long sessionId);
}
