package com.aigp.demo.repository;

import com.aigp.demo.domain.goal.Milestone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

	List<Milestone> findByGoal_IdOrderBySortOrderAsc(Long goalId);

	Optional<Milestone> findByIdAndGoal_Id(Long id, Long goalId);
}
