package com.aigp.demo.repository;

import com.aigp.demo.domain.chat.UserAssistantTask;
import com.aigp.demo.domain.enums.UserAssistantTaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAssistantTaskRepository extends JpaRepository<UserAssistantTask, Long> {

	List<UserAssistantTask> findByUser_IdOrderByUpdatedAtDesc(Long userId);

	List<UserAssistantTask> findByUser_IdAndStatusOrderByUpdatedAtDesc(Long userId, UserAssistantTaskStatus status);

	Optional<UserAssistantTask> findByIdAndUser_Id(Long id, Long userId);

	@Query(
			"""
			SELECT t FROM UserAssistantTask t JOIN FETCH t.user u
			WHERE t.status = :status
			  AND t.dueDate IS NOT NULL
			  AND t.dueDate <= :today
			ORDER BY t.dueDate ASC, t.id ASC
			""")
	List<UserAssistantTask> findOpenTasksDueOnOrBefore(
			@Param("status") UserAssistantTaskStatus status, @Param("today") LocalDate today);
}
