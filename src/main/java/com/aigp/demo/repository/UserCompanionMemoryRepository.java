package com.aigp.demo.repository;

import com.aigp.demo.domain.user.UserCompanionMemory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserCompanionMemoryRepository extends JpaRepository<UserCompanionMemory, Long> {

	Optional<UserCompanionMemory> findByUser_Id(Long userId);

	/**
	 * 已有待推送回顾、且尚未在本周投递的用户记忆行。
	 */
	@Query(
			"""
			SELECT m FROM UserCompanionMemory m JOIN FETCH m.user u
			WHERE m.pendingDigestText IS NOT NULL
			  AND m.summarizedWeekKey IS NOT NULL
			  AND (m.digestDeliveredWeekKey IS NULL OR m.digestDeliveredWeekKey <> m.summarizedWeekKey)
			""")
	List<UserCompanionMemory> findPendingDigestDeliveries();
}
