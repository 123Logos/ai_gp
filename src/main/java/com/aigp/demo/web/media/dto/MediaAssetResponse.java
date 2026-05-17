package com.aigp.demo.web.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "已上传图片资源")
public record MediaAssetResponse(
		@Schema(description = "资源 ID，对话/任务引用用") Long id,
		@Schema(description = "带鉴权的访问 URL（GET 需 Bearer）") String url,
		@Schema(description = "MIME 类型") String contentType,
		@Schema(description = "字节大小") long sizeBytes) {}
