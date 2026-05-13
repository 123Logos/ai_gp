package com.aigp.demo.service;

import com.aigp.demo.domain.ai.AiFeedback;
import com.aigp.demo.domain.enums.AiFeedbackType;
import com.aigp.demo.domain.goal.Goal;
import com.aigp.demo.domain.user.AppUser;
import com.aigp.demo.exception.NotFoundException;
import com.aigp.demo.repository.AiFeedbackRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiFeedbackService {

	private final AiFeedbackRepository aiFeedbackRepository;

	@Transactional(readOnly = true)
	public List<AiFeedback> listForUser(Long userId) {
		return aiFeedbackRepository.findByUser_IdOrderByGeneratedAtDesc(userId);
	}

	@Transactional(readOnly = true)
	public long countUnread(Long userId) {
		return aiFeedbackRepository.countByUser_IdAndReadByUserFalse(userId);
	}

	@Transactional
	public AiFeedback append(AppUser user, Goal goal, AiFeedbackType feedbackType, String content) {
		AiFeedback row = new AiFeedback();
		row.setUser(user);
		row.setGoal(goal);
		row.setFeedbackType(feedbackType);
		row.setContent(content);
		row.setReadByUser(false);
		return aiFeedbackRepository.save(row);
	}

	@Transactional
	public void markRead(Long userId, Long feedbackId) {
		AiFeedback row = aiFeedbackRepository
				.findByIdAndUser_Id(feedbackId, userId)
				.orElseThrow(() -> new NotFoundException("AI feedback not found: id=" + feedbackId));
		row.setReadByUser(true);
	}
}
