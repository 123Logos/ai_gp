package com.aigp.demo.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "未读站内通知数量")
public record UnreadCountResponse(@Schema(description = "未读条数") long unreadCount) {}
