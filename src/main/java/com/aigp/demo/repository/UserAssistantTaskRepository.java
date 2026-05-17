package com.aigp.demo.repository;

import com.aigp.demo.domain.chat.UserAssistantTask;
import com.aigp.demo.domain.enums.UserAssistantTaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAssistantTaskRepository extends JpaRepository<UserAssistantTask, Long> {

	List<UserAssistantTask> findByUser_IdOrderByUpdatedAtDesc(Long userId);

	List<UserAssistantTask> findByUser_IdAndStatusOrderByUpdatedAtDesc(Long userId, UserAssistantTaskStatus status);

	Optional<UserAssistantTask> findByIdAndUser_Id(Long id, Long userId);

	/**
	 * 提醒扫描候选集（粗筛）：再由 {@link TaskReminderDueEvaluator} 按用户时区精确判断。
	 */
	@Query(
			"""
			SELECT t FROM UserAssistantTask t JOIN FETCH t.user u
			WHERE t.status = :status
			  AND (
			    (t.dueAt IS NOT NULL AND t.dueAt >= :dueAtFloor AND t.dueAt <= :dueAtCeiling)
			    OR (t.dueAt IS NULL AND t.dueDate IS NOT NULL AND t.dueDate <= :dueDateCeiling)
			  )
			ORDER BY t.dueAt ASC, t.dueDate ASC, t.id ASC
			""")
	List<UserAssistantTask> findOpenTasksReminderCandidates(
			@Param("status") UserAssistantTaskStatus status,
			@Param("dueAtFloor") LocalDateTime dueAtFloor,
			@Param("dueAtCeiling") LocalDateTime dueAtCeiling,
			@Param("dueDateCeiling") LocalDate dueDateCeiling);

	@Query(
			"""
			SELECT t FROM UserAssistantTask t
			WHERE t.user.id = :userId
			  AND t.updatedAt >= :from AND t.updatedAt < :to
			ORDER BY t.updatedAt ASC
			""")
	List<UserAssistantTask> findByUser_IdAndUpdatedAtBetween(
			@Param("userId") Long userId,
			@Param("from") LocalDateTime fromUtc,
			@Param("to") LocalDateTime toUtc);
}
