package com.aigp.demo.repository;

import com.aigp.demo.domain.user.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
