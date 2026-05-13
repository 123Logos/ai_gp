package com.aigp.demo.repository;

import com.aigp.demo.domain.ai.AiFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

	List<AiFeedback> findByUser_IdOrderByGeneratedAtDesc(Long userId);

	Optional<AiFeedback> findByIdAndUser_Id(Long id, Long userId);

	long countByUser_IdAndReadByUserFalse(Long userId);
}
