package com.aigp.demo.repository;

import com.aigp.demo.domain.ai.AiModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiModelConfigRepository extends JpaRepository<AiModelConfig, Long> {
}
