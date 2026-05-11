package com.aigp.demo.repository;

import com.aigp.demo.domain.ai.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {
}
