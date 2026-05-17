package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.IdentityType;
import com.aigp.demo.domain.user.UserIdentity;
import com.aigp.demo.exception.ConflictException;
import com.aigp.demo.repository.UserIdentityRepository;
import java.time.LocalDateTime;
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
	 * 绑定或更新身份；{@code verifiedAt} 非空时写入验证完成时间（注册验码通过后使用）。
	 */
	@Transactional
	public UserIdentity linkIdentity(
			AppUser user,
			IdentityType identityType,
			String identifier,
			String credential,
			boolean primary,
			LocalDateTime verifiedAt) {
		Optional<UserIdentity> existing =
				userIdentityRepository.findByIdentityTypeAndIdentifier(identityType, identifier);
		if (existing.isPresent()) {
			UserIdentity row = existing.get();
			if (!row.getUser().getId().equals(user.getId())) {
				throw new ConflictException("该认证标识已被其他账号绑定");
			}
			if (credential != null) {
				row.setCredential(credential);
			}
			if (verifiedAt != null) {
				row.setVerifiedAt(verifiedAt);
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
		row.setVerifiedAt(verifiedAt);
		return userIdentityRepository.save(row);
	}

	private void clearPrimaryForUser(Long userId) {
		List<UserIdentity> siblings = userIdentityRepository.findByUser_Id(userId);
		for (UserIdentity s : siblings) {
			s.setPrimary(false);
		}
	}
}
