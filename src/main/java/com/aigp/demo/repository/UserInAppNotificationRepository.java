package com.aigp.demo.repository;

import com.aigp.demo.domain.chat.UserInAppNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInAppNotificationRepository extends JpaRepository<UserInAppNotification, Long> {

	long countByUser_IdAndReadAtIsNull(Long userId);

	List<UserInAppNotification> findByUser_IdOrderByCreatedAtDesc(Long userId);

	List<UserInAppNotification> findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(Long userId);

	Optional<UserInAppNotification> findByIdAndUser_Id(Long id, Long userId);

	@Modifying
	@Query("UPDATE UserInAppNotification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.readAt IS NULL")
	int markAllReadForUser(@Param("userId") Long userId);
}
