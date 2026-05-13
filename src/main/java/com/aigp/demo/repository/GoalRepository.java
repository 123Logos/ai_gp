package com.aigp.demo.repository;

import com.aigp.demo.domain.enums.GoalStatus;
import com.aigp.demo.domain.goal.Goal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

	List<Goal> findByUser_IdOrderByUpdatedAtDesc(Long userId);

	Optional<Goal> findByIdAndUser_Id(Long id, Long userId);

	long countByUser_IdAndStatusIn(Long userId, Collection<GoalStatus> statuses);
}
