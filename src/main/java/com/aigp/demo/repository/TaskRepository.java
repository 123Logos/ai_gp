package com.aigp.demo.repository;

import com.aigp.demo.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
