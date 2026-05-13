package com.aigp.demo.service;

import com.aigp.demo.domain.enums.TaskStatus;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.domain.goal.Milestone;
import com.aigp.demo.domain.plan.Plan;
import com.aigp.demo.domain.task.Task;
import com.aigp.demo.exception.ConflictException;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;

	@Transactional(readOnly = true)
	public List<Task> listForUserOnDate(Long userId, LocalDate scheduledDate) {
		return taskRepository.findByUser_IdAndScheduledDateOrderByCreatedAtAsc(userId, scheduledDate);
	}

	@Transactional
	public Task complete(Long userId, Long taskId, Integer actualMinutes, Integer qualityScore) {
		Task task = taskRepository
				.findByIdAndUser_Id(taskId, userId)
				.orElseThrow(() -> new NotFoundException("Task not found: id=" + taskId));
		if (task.getStatus() != TaskStatus.PENDING) {
			throw new ConflictException("Task is not pending");
		}
		task.setStatus(TaskStatus.COMPLETED);
		task.setCompletedAt(LocalDateTime.now());
		if (actualMinutes != null) {
			task.setActualMinutes(actualMinutes);
		}
		if (qualityScore != null) {
			if (qualityScore < 1 || qualityScore > 5) {
				throw new IllegalArgumentException("qualityScore must be 1..5");
			}
			task.setQualityScore(qualityScore.byteValue());
		}
		return task;
	}

	@Transactional
	public Task skip(Long userId, Long taskId, String skipReason) {
		if (!StringUtils.hasText(skipReason)) {
			throw new IllegalArgumentException("skipReason is required");
		}
		Task task = taskRepository
				.findByIdAndUser_Id(taskId, userId)
				.orElseThrow(() -> new NotFoundException("Task not found: id=" + taskId));
		if (task.getStatus() != TaskStatus.PENDING) {
			throw new ConflictException("Task is not pending");
		}
		task.setStatus(TaskStatus.SKIPPED);
		task.setSkipReason(skipReason);
		return task;
	}

	/** Creates a task row under a plan; caller must ensure plan is active and goal/user match. */
	@Transactional
	public Task scheduleTask(
			Long userId,
			Plan plan,
			Goal goal,
			Milestone milestone,
			String title,
			String description,
			LocalDate scheduledDate,
			int estimatedMinutes) {
		if (!goal.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("goal does not belong to user");
		}
		if (!plan.getGoal().getId().equals(goal.getId())) {
			throw new IllegalArgumentException("plan does not belong to goal");
		}
		Task task = new Task();
		task.setPlan(plan);
		task.setGoal(goal);
		task.setUser(goal.getUser());
		task.setMilestone(milestone);
		task.setTitle(title);
		task.setDescription(description);
		task.setScheduledDate(scheduledDate);
		task.setEstimatedMinutes(estimatedMinutes);
		task.setStatus(TaskStatus.PENDING);
		return taskRepository.save(task);
	}
}
