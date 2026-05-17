package com.aigp.demo.support.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppEmailSender {

	private final Environment environment;
	private final ObjectProvider<JavaMailSender> javaMailSenderProvider;

	public boolean isMailConfigured() {
		if (javaMailSenderProvider.getIfAvailable() == null) {
			return false;
		}
		return StringUtils.hasText(environment.getProperty("spring.mail.host"))
				&& StringUtils.hasText(environment.getProperty("spring.mail.username"))
				&& StringUtils.hasText(environment.getProperty("spring.mail.password"));
	}

	public void sendPlainText(String toEmail, String subject, String text) {
		if (!StringUtils.hasText(toEmail) || !toEmail.contains("@")) {
			return;
		}
		JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
		if (javaMailSender == null || !isMailConfigured()) {
			log.warn("未配置 spring.mail，跳过发信 to={} subject={}", toEmail, subject);
			return;
		}
		String from = environment.getProperty("spring.mail.username");
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(toEmail);
			message.setSubject(subject);
			message.setText(text);
			javaMailSender.send(message);
			log.info("邮件已发送至 {} subject={}", toEmail, subject);
		} catch (Exception e) {
			log.error("邮件发送失败 to={} subject={}", toEmail, subject, e);
			throw new IllegalStateException("邮件发送失败: " + e.getMessage());
		}
	}
}
