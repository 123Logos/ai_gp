package com.aigp.demo.repository;

import com.aigp.demo.domain.user.UserSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

	Optional<UserSession> findByUser_IdAndDeviceId(Long userId, String deviceId);

	List<UserSession> findByUser_Id(Long userId);

	/**
	 * 根据刷新令牌哈希查找未撤销且未过期的会话（用于刷新访问令牌）。
	 */
	@Query(
			"SELECT s FROM UserSession s WHERE s.refreshToken = :hash AND s.revokedAt IS NULL AND s.expiresAt > :now")
	Optional<UserSession> findActiveByRefreshTokenHash(@Param("hash") String hash, @Param("now") LocalDateTime now);
}