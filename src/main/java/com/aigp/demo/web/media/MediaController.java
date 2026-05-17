package com.aigp.demo.web.media;

import com.aigp.demo.service.MediaAssetService;
import com.aigp.demo.service.MediaAssetService.MediaFile;
import com.aigp.demo.web.media.dto.MediaAssetResponse;
import com.aigp.demo.web.security.CurrentUser;
import com.aigp.demo.web.security.JwtUserClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "媒体", description = "图片上传与访问")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

	private final MediaAssetService mediaAssetService;

	@PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "上传图片（对话或任务附图前先调此接口）")
	public MediaAssetResponse uploadImage(
			@CurrentUser JwtUserClaims user, @RequestPart("file") MultipartFile file) {
		return mediaAssetService.uploadImage(user.userId(), file);
	}

	@GetMapping("/{id}")
	@Operation(summary = "读取已上传图片（仅本人）")
	public ResponseEntity<byte[]> getImage(@CurrentUser JwtUserClaims user, @PathVariable Long id) {
		MediaFile file = mediaAssetService.readOwnedFile(user.userId(), id);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, file.contentType())
				.body(file.bytes());
	}
}
