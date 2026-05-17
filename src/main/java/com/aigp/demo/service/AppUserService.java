package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.IdentityType;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.exception.UnauthorizedException;
import com.aigp.demo.repository.AppUserRepository;
import com.aigp.demo.repository.UserIdentityRepository;
import com.aigp.demo.support.UidGenerator;
import com.aigp.demo.web.user.dto.UserProfileResponse;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {

	private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

	private final AppUserRepository appUserRepository;
	private final UserNotificationSettingsService userNotificationSettingsService;
	private final UserSessionService userSessionService;
	private final UserIdentityRepository userIdentityRepository;
	private final UserIdentityService userIdentityService;

	@Transactional(readOnly = true)
	public AppUser requireById(Long id) {
		return appUserRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found: id=" + id));
	}

	@Transactional(readOnly = true)
	public AppUser requireByUid(String uid) {
		return appUserRepository
				.findByUid(uid)
				.orElseThrow(() -> new NotFoundException("User not found: uid=" + uid));
	}

	/**
	 * 要求用户存在且状态为「正常」，否则抛出未授权异常（禁用/已注销账户不可继续操作）。
	 */
	@Transactional(readOnly = true)
	public AppUser requireActive(Long id) {
		AppUser user = requireById(id);
		if (user.getStatus() == null || user.getStatus() != 1) {
			throw new UnauthorizedException("账号已禁用或已注销，无法使用此功能");
		}
		return user;
	}

	/**
	 * Registers a minimal {@code users} row plus default {@code user_notification_settings}, per
	 * {@code md文档/数据库.md}.
	 */
	@Transactional
	public AppUser registerNewUser(String nickname) {
		AppUser user = new AppUser();
		user.setUid(UidGenerator.nextUid());
		user.setStatus((byte) 1);
		user.setTimezone("Asia/Shanghai");
		user.setLanguage("zh-CN");
		user.setWeeklyHours((byte) 0);
		if (org.springframework.util.StringUtils.hasText(nickname)) {
			user.setNickname(nickname.trim());
		} else {
			user.setNickname("新用户");
		}
		appUserRepository.save(user);
		userNotificationSettingsService.getOrCreate(user);
		return user;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse toProfileResponse(AppUser user) {
		String phoneMasked = maskedPhoneForUser(user.getId());
		return UserProfileResponse.fromEntity(user, phoneMasked);
	}

	private String maskedPhoneForUser(Long userId) {
		return userIdentityRepository
				.findByUser_IdAndIdentityType(userId, IdentityType.phone)
				.map(id -> maskPhone(id.getIdentifier()))
				.orElse(null);
	}

	private static String maskPhone(String phone) {
		if (phone == null || phone.length() < 11) {
			return phone;
		}
		return phone.substring(0, 3) + "****" + phone.substring(7);
	}

	@Transactional
	public AppUser updateProfile(Long userId, String nickname, String avatarUrl, Integer weeklyHours, String phone) {
		AppUser user = requireActive(userId);
		if (nickname != null) {
			user.setNickname(nickname);
		}
		if (avatarUrl != null) {
			user.setAvatarUrl(avatarUrl);
		}
		if (weeklyHours != null) {
			if (weeklyHours < 0 || weeklyHours > 40) {
				throw new IllegalArgumentException("weeklyHours must be between 0 and 40");
			}
			user.setWeeklyHours(weeklyHours.byteValue());
		}
		if (org.springframework.util.StringUtils.hasText(phone)) {
			String p = phone.trim();
			if (!CN_MOBILE.matcher(p).matches()) {
				throw new IllegalArgumentException("手机号须为 11 位中国大陆号码");
			}
			userIdentityService.linkIdentity(user, IdentityType.phone, p, null, false, null);
		}
		return user;
	}

	/**
	 * 更新首次登录画像（身份、爱好、探索方向等）；字段为 null 表示不修改，空字符串会清空对应文本。
	 */
	@Transactional
	public AppUser updateOnboardingProfile(
			Long userId,
			String identitySummary,
			String hobbies,
			String explorationInterests,
			Boolean onboardingCompleted) {
		AppUser user = requireActive(userId);
		if (identitySummary != null) {
			user.setProfileIdentity(blankToNull(identitySummary));
		}
		if (hobbies != null) {
			user.setProfileHobbies(blankToNull(hobbies));
		}
		if (explorationInterests != null) {
			user.setProfileExploration(blankToNull(explorationInterests));
		}
		if (onboardingCompleted != null) {
			user.setOnboardingCompleted(onboardingCompleted);
		}
		return appUserRepository.save(user);
	}

	private static String blankToNull(String raw) {
		String t = raw.trim();
		return t.isEmpty() ? null : t;
	}

	/**
	 * 软注销：将 {@code users.status} 置为 3，并撤销全部登录会话。
	 */
	@Transactional
	public void markDeletedAccount(Long userId) {
		AppUser user = requireById(userId);
		user.setStatus((byte) 3);
		userSessionService.revokeAllForUser(userId);
	}
}
