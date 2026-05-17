package com.aigp.demo.web.user;

import com.aigp.demo.service.UserAvatarService;
import com.aigp.demo.service.UserAvatarService.AvatarFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 用户头像公开读取（无需 JWT，供前端 {@code img} 直接引用 {@code users.avatar_url}）。
 */
@RestController
@RequestMapping("/api/v1/public/avatars")
@Validated
@RequiredArgsConstructor
@Tag(name = "公开头像", description = "按 uid 读取用户头像图片")
public class PublicAvatarController {

	private final UserAvatarService userAvatarService;

	/**
	 * [公开头像] 按对外 uid 返回图片字节；无头像或账号非正常时 404。
	 */
	@GetMapping("/{uid}")
	@Operation(summary = "读取用户头像（公开）")
	public ResponseEntity<byte[]> getAvatar(
			@PathVariable @Pattern(regexp = "^U[0-9A-F]{15}$", message = "uid 格式无效") String uid) {
		AvatarFile file = userAvatarService.readPublicAvatar(uid);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, file.contentType())
				.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
				.body(file.bytes());
	}
}
