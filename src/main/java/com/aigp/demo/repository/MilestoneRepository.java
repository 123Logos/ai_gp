package com.aigp.demo.repository;

import com.aigp.demo.domain.goal.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
}
