package com.aigp.demo.repository;

import com.aigp.demo.domain.ai.AiPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptRepository extends JpaRepository<AiPrompt, Long> {
}
