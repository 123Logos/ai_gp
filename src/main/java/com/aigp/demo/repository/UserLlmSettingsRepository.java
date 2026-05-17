package com.aigp.demo.repository;

import com.aigp.demo.domain.user.UserLlmSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLlmSettingsRepository extends JpaRepository<UserLlmSettings, Long> {

	Optional<UserLlmSettings> findByUser_Id(Long userId);
}
