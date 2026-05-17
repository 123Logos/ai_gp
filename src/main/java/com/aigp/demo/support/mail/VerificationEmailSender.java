package com.aigp.demo.support.mail;

import com.aigp.demo.service.VerificationCodeService.Purpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 在已配置 {@code spring.mail.*} 时，将验证码发到用户邮箱（如 QQ 邮箱 SMTP）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEmailSender {

	private final Environment environment;
	private final ObjectProvider<JavaMailSender> javaMailSenderProvider;

	/**
	 * 是否已配置可发信的 SMTP（且容器中存在 {@link JavaMailSender} Bean）。
	 */
	public boolean isMailConfigured() {
		if (javaMailSenderProvider.getIfAvailable() == null) {
			return false;
		}
		return StringUtils.hasText(environment.getProperty("spring.mail.host"))
				&& StringUtils.hasText(environment.getProperty("spring.mail.username"))
				&& StringUtils.hasText(environment.getProperty("spring.mail.password"));
	}

	/**
	 * 向指定邮箱发送验证码正文；未配置 SMTP 时不执行（由调用方打日志提示）。
	 */
	public void sendVerificationCode(String toEmail, Purpose purpose, String code, int ttlSeconds) {
		if (!StringUtils.hasText(toEmail) || !toEmail.contains("@")) {
			return;
		}
		JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
		if (javaMailSender == null || !isMailConfigured()) {
			return;
		}
		String from = environment.getProperty("spring.mail.username");
		String subject =
				purpose == Purpose.REGISTER ? "[AI成长计划] 注册验证码" : "[AI成长计划] 找回密码验证码";
		int minutes = Math.max(1, ttlSeconds / 60);
		String text = "您的验证码为：" + code + "\n" + "有效期 " + minutes + " 分钟。如非本人操作请忽略本邮件。";

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(toEmail);
			message.setSubject(subject);
			message.setText(text);
			javaMailSender.send(message);
			log.info("验证码邮件已发送至 {}", toEmail);
		} catch (Exception e) {
			log.error("验证码邮件发送失败 to={}", toEmail, e);
			throw new IllegalStateException("验证码邮件发送失败，请稍后重试或检查邮箱 SMTP 配置");
		}
	}
}
