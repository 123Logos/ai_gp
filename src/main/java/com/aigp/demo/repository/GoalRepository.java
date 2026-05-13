package com.aigp.demo.repository;

import com.aigp.demo.domain.goal.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}
