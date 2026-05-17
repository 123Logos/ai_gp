package com.aigp.demo.support.crypto;

import com.aigp.demo.config.AppProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 使用应用主密钥对用户 API Key 等敏感字符串做 AES-GCM 加解密（不落明文）。
 */
@Component
public class SecretStringEncryptor {

	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int GCM_TAG_BITS = 128;
	private static final int IV_BYTES = 12;

	private final SecretKey secretKey;
	private final SecureRandom secureRandom = new SecureRandom();

	public SecretStringEncryptor(AppProperties appProperties) {
		String material = appProperties.getJwt().getSecretKey();
		if (!StringUtils.hasText(material)) {
			throw new IllegalStateException("未配置 JWT_SECRET_KEY，无法加密用户 API Key");
		}
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(material.trim().getBytes(StandardCharsets.UTF_8));
			this.secretKey = new SecretKeySpec(digest, "AES");
		} catch (Exception e) {
			throw new IllegalStateException("初始化加密组件失败", e);
		}
	}

	/** 加密为 Base64（前置 12 字节 IV）。 */
	public String encrypt(String plainText) {
		if (!StringUtils.hasText(plainText)) {
			throw new IllegalArgumentException("待加密内容不能为空");
		}
		try {
			byte[] iv = new byte[IV_BYTES];
			secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] encrypted = cipher.doFinal(plainText.trim().getBytes(StandardCharsets.UTF_8));
			ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
			buffer.put(iv);
			buffer.put(encrypted);
			return Base64.getEncoder().encodeToString(buffer.array());
		} catch (Exception e) {
			throw new IllegalStateException("加密失败", e);
		}
	}

	/** 解密 Base64 密文。 */
	public String decrypt(String cipherTextBase64) {
		if (!StringUtils.hasText(cipherTextBase64)) {
			throw new IllegalArgumentException("密文不能为空");
		}
		try {
			byte[] payload = Base64.getDecoder().decode(cipherTextBase64.trim());
			if (payload.length <= IV_BYTES) {
				throw new IllegalArgumentException("密文格式无效");
			}
			ByteBuffer buffer = ByteBuffer.wrap(payload);
			byte[] iv = new byte[IV_BYTES];
			buffer.get(iv);
			byte[] encrypted = new byte[buffer.remaining()];
			buffer.get(encrypted);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] plain = cipher.doFinal(encrypted);
			return new String(plain, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("解密失败", e);
		}
	}
}
