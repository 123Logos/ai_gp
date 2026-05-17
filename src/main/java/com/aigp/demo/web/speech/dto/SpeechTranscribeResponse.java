package com.aigp.demo.web.speech.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "语音转写结果")
public record SpeechTranscribeResponse(@Schema(description = "识别出的文本，可直接作为 AI 对话 message") String text) {}
