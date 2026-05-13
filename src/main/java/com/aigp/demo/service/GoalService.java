package com.aigp.demo.service;

import com.aigp.demo.domain.enums.GoalPriority;
import com.aigp.demo.domain.enums.GoalStatus;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.enums.GrowthDomain;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.exception.ConflictException;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.GoalRepository;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalService {

	/** From {@code md文档/数据库.md} — at most five goals in an active pipeline per user. */
	public static final int MAX_CONCURRENT_PIPELINE_GOALS = 5;

	private static final Set<GoalStatus> PIPELINE_STATUSES =
			EnumSet.of(GoalStatus.DECOMPOSING, GoalStatus.PENDING, GoalStatus.ACTIVE);

	private final GoalRepository goalRepository;

	@Transactional(readOnly = true)
	public List<Goal> listForUser(Long userId) {
		return goalRepository.findByUser_IdOrderByUpdatedAtDesc(userId);
	}

	@Transactional(readOnly = true)
	public Goal requireOwned(Long userId, Long goalId) {
		return goalRepository
				.findByIdAndUser_Id(goalId, userId)
				.orElseThrow(() -> new NotFoundException("Goal not found: id=" + goalId));
	}

	@Transactional
	public Goal create(
			AppUser owner,
			String title,
			String description,
			GrowthDomain domain,
			GoalPriority priority,
			LocalDate deadline) {
		long pipelineCount = goalRepository.countByUser_IdAndStatusIn(owner.getId(), PIPELINE_STATUSES);
		if (pipelineCount >= MAX_CONCURRENT_PIPELINE_GOALS) {
			throw new ConflictException("At most " + MAX_CONCURRENT_PIPELINE_GOALS + " goals in DECOMPOSING/PENDING/ACTIVE");
		}
		Goal goal = new Goal();
		goal.setUser(owner);
		goal.setTitle(title);
		goal.setDescription(description);
		goal.setDomain(domain);
		goal.setPriority(priority != null ? priority : GoalPriority.MEDIUM);
		goal.setDeadline(deadline);
		goal.setStatus(GoalStatus.DECOMPOSING);
		return goalRepository.save(goal);
	}

	@Transactional
	public Goal updateMeta(
			Long userId,
			Long goalId,
			String title,
			String description,
			GrowthDomain domain,
			GoalPriority priority,
			LocalDate deadline) {
		Goal goal = requireOwned(userId, goalId);
		if (title != null) {
			goal.setTitle(title);
		}
		if (description != null) {
			goal.setDescription(description);
		}
		if (domain != null) {
			goal.setDomain(domain);
		}
		if (priority != null) {
			goal.setPriority(priority);
		}
		if (deadline != null) {
			goal.setDeadline(deadline);
		}
		return goal;
	}

	@Transactional
	public Goal updateStatus(Long userId, Long goalId, GoalStatus status) {
		Goal goal = requireOwned(userId, goalId);
		goal.setStatus(status);
		return goal;
	}

	@Transactional
	public Goal updateMilestonesJsonSnapshot(Long userId, Long goalId, String milestonesJson) {
		Goal goal = requireOwned(userId, goalId);
		goal.setMilestonesJson(milestonesJson);
		return goal;
	}
}
