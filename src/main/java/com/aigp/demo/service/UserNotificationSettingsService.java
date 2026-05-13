package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.UserNotificationSettings;
import com.aigp.demo.repository.UserNotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserNotificationSettingsService {

	private final UserNotificationSettingsRepository notificationSettingsRepository;

	@Transactional
	public UserNotificationSettings getOrCreate(AppUser user) {
		return notificationSettingsRepository.findByUser_Id(user.getId()).orElseGet(() -> {
			UserNotificationSettings s = new UserNotificationSettings();
			s.setUser(user);
			return notificationSettingsRepository.save(s);
		});
	}

	@Transactional
	public UserNotificationSettings update(
			AppUser user,
			boolean dailyTaskReminder,
			boolean conflictAlert,
			boolean milestoneCelebration,
			boolean laggingWarning) {
		UserNotificationSettings settings = getOrCreate(user);
		settings.setDailyTaskReminder(dailyTaskReminder);
		settings.setConflictAlert(conflictAlert);
		settings.setMilestoneCelebration(milestoneCelebration);
		settings.setLaggingWarning(laggingWarning);
		return settings;
	}
}
