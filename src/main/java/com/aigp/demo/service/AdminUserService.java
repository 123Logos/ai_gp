package com.aigp.demo.service;

import com.aigp.demo.domain.admin.AdminUser;
import com.aigp.demo.repository.AdminUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final AdminUserRepository adminUserRepository;

	@Transactional(readOnly = true)
	public Optional<AdminUser> findByUsername(String username) {
		return adminUserRepository.findByUsername(username);
	}
}
