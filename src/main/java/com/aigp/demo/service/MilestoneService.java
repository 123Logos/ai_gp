package com.aigp.demo.service;

import com.aigp.demo.domain.enums.MilestoneStatus;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.domain.goal.Milestone;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.MilestoneRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MilestoneService {

	private final MilestoneRepository milestoneRepository;
	private final GoalService goalService;

	@Transactional(readOnly = true)
	public List<Milestone> listForGoal(Long userId, Long goalId) {
		goalService.requireOwned(userId, goalId);
		return milestoneRepository.findByGoal_IdOrderBySortOrderAsc(goalId);
	}

	@Transactional
	public Milestone create(
			Long userId,
			Long goalId,
			String name,
			int sortOrder,
			BigDecimal estimatedWeeks,
			String deliverable,
			String dependenciesJson) {
		Goal goal = goalService.requireOwned(userId, goalId);
		Milestone m = new Milestone();
		m.setGoal(goal);
		m.setName(name);
		m.setSortOrder(sortOrder);
		m.setEstimatedWeeks(estimatedWeeks);
		m.setDeliverable(deliverable);
		m.setDependencies(dependenciesJson);
		m.setStatus(MilestoneStatus.PENDING);
		return milestoneRepository.save(m);
	}

	@Transactional
	public Milestone updateStatus(Long userId, Long milestoneId, MilestoneStatus status, LocalDateTime completedAt) {
		Milestone m = milestoneRepository
				.findById(milestoneId)
				.orElseThrow(() -> new NotFoundException("Milestone not found: id=" + milestoneId));
		if (!m.getGoal().getUser().getId().equals(userId)) {
			throw new NotFoundException("Milestone not found: id=" + milestoneId);
		}
		m.setStatus(status);
		if (completedAt != null) {
			m.setCompletedAt(completedAt);
		} else if (status == MilestoneStatus.COMPLETED) {
			m.setCompletedAt(LocalDateTime.now());
		}
		return m;
	}
}
