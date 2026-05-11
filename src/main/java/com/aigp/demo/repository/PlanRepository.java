package com.aigp.demo.repository;

import com.aigp.demo.domain.plan.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
