package com.aigp.demo.repository;

import com.aigp.demo.domain.ai.AiPrompt;
import com.aigp.demo.domain.enums.PromptType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptRepository extends JpaRepository<AiPrompt, Long> {

	Optional<AiPrompt> findFirstByTypeAndActiveTrueOrderByUpdatedAtDesc(PromptType type);
}
