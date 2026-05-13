package com.aigp.demo.web.security;

/**
 * 从访问令牌中解析出的当前用户上下文（供接口层与参数解析器使用）。
 *
 * @param userId    用户内部主键 {@code users.id}
 * @param uid       对外用户标识 {@code users.uid}
 * @param sessionId 登录会话主键 {@code user_sessions.id}（旧令牌可能为空）
 * @param jti       JWT 唯一编号，可用于审计
 */
public record JwtUserClaims(Long userId, String uid, Long sessionId, String jti) {}
