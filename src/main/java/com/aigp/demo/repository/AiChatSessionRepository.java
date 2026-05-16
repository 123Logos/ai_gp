package com.aigp.demo.repository;

import com.aigp.demo.domain.chat.AiChatSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {

	Optional<AiChatSession> findByIdAndUser_Id(Long id, Long userId);

	Optional<AiChatSession> findFirstByUser_IdAndTitle(Long userId, String title);
}
