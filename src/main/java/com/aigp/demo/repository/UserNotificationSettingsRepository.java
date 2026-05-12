package com.aigp.demo.repository;

import com.aigp.demo.domain.user.UserNotificationSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettings, Long> {

	Optional<UserNotificationSettings> findByUser_Id(Long userId);
}
