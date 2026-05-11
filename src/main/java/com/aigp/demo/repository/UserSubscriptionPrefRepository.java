package com.aigp.demo.repository;

import com.aigp.demo.domain.user.UserSubscriptionPref;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSubscriptionPrefRepository extends JpaRepository<UserSubscriptionPref, Long> {

	Optional<UserSubscriptionPref> findByUser_UserId(Long userId);
}
