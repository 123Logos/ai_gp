package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.IdentityType;
import com.aigp.demo.domain.user.UserIdentity;
import com.aigp.demo.exception.ConflictException;
import com.aigp.demo.repository.UserIdentityRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserIdentityService {

	private final UserIdentityRepository userIdentityRepository;

	@Transactional(readOnly = true)
	public Optional<UserIdentity> find(IdentityType identityType, String identifier) {
		return userIdentityRepository.findByIdentityTypeAndIdentifier(identityType, identifier);
	}

	/**
	 * Binds a login identity to a user. Enforces {@code uk_identity (identity_type, identifier)}.
	 * When {@code primary} is true, clears primary flag on other identities of the same user.
	 */
	@Transactional
	public UserIdentity linkIdentity(
			AppUser user, IdentityType identityType, String identifier, String credential, boolean primary) {
		Optional<UserIdentity> existing =
				userIdentityRepository.findByIdentityTypeAndIdentifier(identityType, identifier);
		if (existing.isPresent()) {
			UserIdentity row = existing.get();
			if (!row.getUser().getId().equals(user.getId())) {
				throw new ConflictException("Identity already bound to another user");
			}
			if (credential != null) {
				row.setCredential(credential);
			}
			if (primary) {
				clearPrimaryForUser(user.getId());
				row.setPrimary(true);
			}
			return userIdentityRepository.save(row);
		}
		if (primary) {
			clearPrimaryForUser(user.getId());
		}
		UserIdentity row = new UserIdentity();
		row.setUser(user);
		row.setIdentityType(identityType);
		row.setIdentifier(identifier);
		row.setCredential(credential);
		row.setPrimary(primary);
		return userIdentityRepository.save(row);
	}

	private void clearPrimaryForUser(Long userId) {
		List<UserIdentity> siblings = userIdentityRepository.findByUser_Id(userId);
		for (UserIdentity s : siblings) {
			s.setPrimary(false);
		}
	}
}
