package com.aigp.demo.repository;

import com.aigp.demo.domain.user.IdentityType;
import com.aigp.demo.domain.user.UserIdentity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

	Optional<UserIdentity> findByIdentityTypeAndIdentifier(IdentityType identityType, String identifier);
}
