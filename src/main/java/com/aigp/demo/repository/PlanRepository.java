package com.aigp.demo.repository;

import com.aigp.demo.domain.enums.PlanStatus;
import com.aigp.demo.domain.plan.Plan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	List<Plan> findByGoal_IdAndStatus(Long goalId, PlanStatus status);

	Optional<Plan> findTopByGoal_IdOrderByVersionDesc(Long goalId);

	Optional<Plan> findFirstByGoal_IdAndStatusOrderByVersionDesc(Long goalId, PlanStatus status);
}
