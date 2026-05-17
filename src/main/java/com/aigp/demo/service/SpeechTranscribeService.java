package com.aigp.demo.service;

import com.aigp.demo.config.AppProperties;
import com.aigp.demo.exception.FeatureUnavailableException;
import com.aigp.demo.support.AudioUploadSupport;
import com.aigp.demo.support.speech.LocalSpeechToTextClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户语音转写：校验上传 → 转发内网 STT → 返回文本（不落库）。
 */
@Service
@RequiredArgsConstructor
public class SpeechTranscribeService {

	private final AppProperties appProperties;
	private final AppUserService appUserService;
	private final LocalSpeechToTextClient localSpeechToTextClient;

	/**
	 * @param userId 当前登录用户（JWT）
	 * @param file   multipart 字段 {@code file}
	 * @return 识别出的文本，可能为空串
	 */
	public String transcribe(Long userId, MultipartFile file) {
		appUserService.requireActive(userId);
		if (!appProperties.getSpeech().isEnabled()) {
			throw new FeatureUnavailableException("SPEECH_DISABLED", "语音转写功能未开启");
		}
		long maxBytes = Math.max(1024L, appProperties.getSpeech().getMaxAudioBytes());
		AudioUploadSupport.validateAudio(file, maxBytes);
		String text = localSpeechToTextClient.transcribe(file);
		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException("未识别到有效语音内容，请重试");
		}
		return text.trim();
	}
}
