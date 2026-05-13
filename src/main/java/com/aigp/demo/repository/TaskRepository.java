package com.aigp.demo.repository;

import com.aigp.demo.domain.task.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUser_IdAndScheduledDateOrderByCreatedAtAsc(Long userId, LocalDate scheduledDate);

	Optional<Task> findByIdAndUser_Id(Long id, Long userId);
}
