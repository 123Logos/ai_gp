package com.aigp.demo.service;

import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.domain.user.UserSession;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.UserSessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSessionService {

	private final UserSessionRepository userSessionRepository;

	/** 按 {@code (user_id, device_id)} 查找或新建会话行，写入刷新令牌、过期时间、IP、JTI、信任标记，并将 {@code revoked_at} 置空。 */
	@Transactional
	public UserSession upsertSession(
			AppUser user,
			String deviceId,
			String refreshToken,
			LocalDateTime expiresAt,
			String ipAddress,
			String accessTokenJti,
			boolean trusted) {
		UserSession session =
				userSessionRepository.findByUser_IdAndDeviceId(user.getId(), deviceId).orElseGet(UserSession::new);
		session.setUser(user);
		session.setDeviceId(deviceId);
		session.setRefreshToken(refreshToken);
		session.setExpiresAt(expiresAt);
		session.setIpAddress(ipAddress);
		session.setAccessTokenJti(accessTokenJti);
		session.setTrusted(trusted);
		session.setRevokedAt(null);
		return userSessionRepository.save(session);
	}

	@Transactional
	public void revokeSession(Long userId, Long sessionId) {
		UserSession session = userSessionRepository
				.findById(sessionId)
				.filter(s -> s.getUser().getId().equals(userId))
				.orElseThrow(() -> new NotFoundException("Session not found"));
		session.setRevokedAt(LocalDateTime.now());
	}

	/**
	 * 撤销某用户下所有未撤销的会话（账号注销或「全部设备下线」时使用）。
	 */
	@Transactional
	public void revokeAllForUser(Long userId) {
		LocalDateTime now = LocalDateTime.now();
		List<UserSession> list = userSessionRepository.findByUser_Id(userId);
		for (UserSession s : list) {
			if (s.getRevokedAt() == null) {
				s.setRevokedAt(now);
			}
		}
	}
}
