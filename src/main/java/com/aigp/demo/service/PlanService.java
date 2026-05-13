package com.aigp.demo.service;

import com.aigp.demo.domain.enums.PlanStatus;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.domain.plan.Plan;
import com.aigp.demo.repository.PlanRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;
	private final GoalService goalService;

	@Transactional(readOnly = true)
	public Optional<Plan> findActiveForGoal(Long userId, Long goalId) {
		goalService.requireOwned(userId, goalId);
		return planRepository.findFirstByGoal_IdAndStatusOrderByVersionDesc(goalId, PlanStatus.ACTIVE);
	}

	/**
	 * Deprecates all {@code ACTIVE} plans for the goal, then inserts a new {@code ACTIVE} row with
	 * {@code version = max(version)+1}, matching {@code md文档/数据库.md} plan versioning.
	 */
	@Transactional
	public Plan createNextPlanVersion(Long userId, Long goalId, String aiPromptVersion) {
		Goal goal = goalService.requireOwned(userId, goalId);
		List<Plan> active = planRepository.findByGoal_IdAndStatus(goalId, PlanStatus.ACTIVE);
		for (Plan p : active) {
			p.setStatus(PlanStatus.DEPRECATED);
		}
		int nextVersion =
				planRepository.findTopByGoal_IdOrderByVersionDesc(goalId).map(Plan::getVersion).orElse(0) + 1;
		Plan plan = new Plan();
		plan.setGoal(goal);
		plan.setVersion(nextVersion);
		plan.setStatus(PlanStatus.ACTIVE);
		plan.setGeneratedAt(LocalDateTime.now());
		plan.setAiPromptVersion(aiPromptVersion);
		return planRepository.save(plan);
	}
}
