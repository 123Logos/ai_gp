package com.aigp.demo.service;

import com.aigp.demo.domain.ai.AiPrompt;
import com.aigp.demo.domain.enums.PromptType;
import com.aigp.demo.repository.AiPromptRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiPromptService {

	private final AiPromptRepository aiPromptRepository;

	@Transactional(readOnly = true)
	public Optional<AiPrompt> findActivePrompt(PromptType type) {
		return aiPromptRepository.findFirstByTypeAndActiveTrueOrderByUpdatedAtDesc(type);
	}
}
