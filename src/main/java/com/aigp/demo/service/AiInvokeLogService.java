package com.aigp.demo.service;

import com.aigp.demo.domain.ai.AiInvokeLog;
import com.aigp.demo.repository.AiInvokeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiInvokeLogService {

	private final AiInvokeLogRepository aiInvokeLogRepository;

	@Transactional
	public AiInvokeLog save(AiInvokeLog log) {
		return aiInvokeLogRepository.save(log);
	}
}
