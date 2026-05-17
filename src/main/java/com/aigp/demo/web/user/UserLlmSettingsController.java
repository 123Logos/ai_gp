package com.aigp.demo.web.user;

import com.aigp.demo.service.UserLlmSettingsService;
import com.aigp.demo.web.security.CurrentUser;
import com.aigp.demo.web.security.JwtUserClaims;
import com.aigp.demo.web.user.dto.PutUserLlmSettingsRequest;
import com.aigp.demo.web.user.dto.UserLlmProvidersResponse;
import com.aigp.demo.web.user.dto.UserLlmSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户 AI 密钥与计费模式：平台代调或使用自带 API Key 调用同一套后端对话接口。
 */
@RestController
@RequestMapping("/api/v1/users/me/llm")
@Validated
@RequiredArgsConstructor
@Tag(name = "用户 LLM 配置", description = "自带 Key / 平台代调与提供商选择")
@SecurityRequirement(name = "bearerAuth")
public class UserLlmSettingsController {

	private final UserLlmSettingsService userLlmSettingsService;

	/**
	 * [LLM 设置] 查询当前用户的计费模式、提供商与 Key 配置状态（不返回完整 Key）。
	 */
	@GetMapping("/settings")
	@Operation(summary = "获取 LLM 调用配置")
	public UserLlmSettingsResponse getSettings(@CurrentUser JwtUserClaims user) {
		return userLlmSettingsService.getSettings(user.userId());
	}

	/**
	 * [LLM 设置] 保存计费模式、提供商、可选 baseUrl/model 与用户 API Key。
	 */
	@PutMapping("/settings")
	@Operation(summary = "保存 LLM 调用配置")
	public UserLlmSettingsResponse putSettings(
			@CurrentUser JwtUserClaims user, @Valid @RequestBody PutUserLlmSettingsRequest body) {
		return userLlmSettingsService.saveSettings(user.userId(), body);
	}

	/**
	 * [LLM 设置] 恢复平台代调并清除已存用户 Key。
	 */
	@DeleteMapping("/settings")
	@Operation(summary = "恢复平台代调（清除自带 Key）")
	public ResponseEntity<UserLlmSettingsResponse> deleteSettings(@CurrentUser JwtUserClaims user) {
		UserLlmSettingsResponse body = userLlmSettingsService.resetToPlatform(user.userId());
		return ResponseEntity.status(HttpStatus.OK).body(body);
	}

	/**
	 * [LLM 提供商] 列出后端已配置的提供商（不含密钥），供前端下拉选择。
	 */
	@GetMapping("/providers")
	@Operation(summary = "列出可选 AI 提供商")
	public UserLlmProvidersResponse listProviders(@CurrentUser JwtUserClaims user) {
		return userLlmSettingsService.listProviderOptions(user.userId());
	}
}
