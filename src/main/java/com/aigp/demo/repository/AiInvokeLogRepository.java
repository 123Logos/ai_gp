package com.aigp.demo.repository;

import com.aigp.demo.domain.ai.AiInvokeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiInvokeLogRepository extends JpaRepository<AiInvokeLog, Long> {
}
