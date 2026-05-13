package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.exception.UnauthorizedException;
import com.aigp.demo.repository.AppUserRepository;
import com.aigp.demo.support.UidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {

	private final AppUserRepository appUserRepository;
	private final UserNotificationSettingsService userNotificationSettingsService;
	private final UserSessionService userSessionService;

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
	public AppUser registerNewUser() {
		AppUser user = new AppUser();
		user.setUid(UidGenerator.nextUid());
		user.setStatus((byte) 1);
		user.setTimezone("Asia/Shanghai");
		user.setLanguage("zh-CN");
		user.setWeeklyHours((byte) 0);
		appUserRepository.save(user);
		userNotificationSettingsService.getOrCreate(user);
		return user;
	}

	@Transactional
	public AppUser updateProfile(Long userId, String nickname, String avatarUrl, Integer weeklyHours) {
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
		return user;
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
