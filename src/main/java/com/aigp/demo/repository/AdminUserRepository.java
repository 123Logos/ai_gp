package com.aigp.demo.repository;

import com.aigp.demo.domain.admin.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

	Optional<AdminUser> findByUsername(String username);
}
