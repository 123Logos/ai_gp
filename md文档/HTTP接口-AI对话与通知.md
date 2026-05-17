# HTTP 接口说明 — AI 对话、助手任务与站内通知（v1）

本文档描述当前后端已实现的 **AI 对话、助手任务查询、站内通知** 及 **WebSocket** 约定，与代码路径一致。  
通用鉴权、错误格式见 **`md文档/HTTP接口-认证与用户.md`** 第 1 章。

业务流程（非接口细节）见 **`md文档/AI对话与助手任务-流程说明.md`**。

---

## 1. 通用约定

- 默认主机：`http://localhost:8000`
- URL 前缀：`/api/v1`
- 本章 REST 接口均需：`Authorization: Bearer {accessToken}`
- 用户 `status` 须为 **1（正常）**

### 1.1 业务错误码补充

| code | 典型场景 |
|------|----------|
| `FEATURE_UNAVAILABLE` | AI 提供商未配置或 MiMo 无 API Key |
| `NOT_FOUND` | 会话不存在或不属于当前用户 |

---

## 2. AI 对话 `/api/v1/ai`

### 2.1 发送对话消息

- **方法 / 路径**：`POST /api/v1/ai/chat`
- **说明**：用户发一轮消息，服务端完成规划（可选）、调用大模型、执行任务工具（可选），返回助手回复；**自动创建或续用会话**，并将本轮用户消息与助手最终回复落库。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | string | 是 | 用户本轮输入，最长 8000 字符 |
| sessionId | number | 否 | 继续已有会话时传入；不传则新建会话 |
| provider | string | 否 | AI 提供商：`mimo`（默认）或 `ollama`；未传时使用用户在 **LLM 设置** 中的偏好 |

- **密钥来源**：由 `GET/PUT /api/v1/users/me/llm/settings` 中的 `billingMode` 决定——`PLATFORM` 走服务端配置的 Key，`BYOK` 走用户保存的 Key。详见 **`md文档/HTTP接口-认证与用户.md`** 第 3.5 节。
- **附图对话**：含图片时仍使用服务端 **VLM** 配置（`VLM_API_KEY`），与用户 BYOK 无关。

- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| reply | string | 助手回复正文 |
| sessionId | number | 会话 ID，下轮请求请原样带回 |
| provider | string | 实际使用的提供商 |
| model | string | 实际调用的模型名 |
| userMessageId | number | 本轮用户消息在库中的 ID |
| assistantMessageId | number | 本轮助手消息在库中的 ID |

- **示例请求**：

```json
{
  "message": "帮我记一下周五前要交周报",
  "sessionId": 12,
  "provider": "mimo"
}
```

- **示例响应**：

```json
{
  "reply": "好的，已为你创建待办「交周报」，截止日期 2026-05-23。",
  "sessionId": 12,
  "provider": "mimo",
  "model": "mimo-v2.5-pro",
  "userMessageId": 101,
  "assistantMessageId": 102
}
```

- **503 / FEATURE_UNAVAILABLE**：提供商未配置或 Key 缺失。

---

### 2.2 拉取会话历史消息

- **方法 / 路径**：`GET /api/v1/ai/chat/sessions/{sessionId}/messages`
- **路径参数**：`sessionId` — 会话 ID
- **说明**：返回该会话下已落库的消息（仅 `USER` / `ASSISTANT`），按 `createdAt` **升序**；含「任务提醒」会话。
- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| sessionId | number | 会话 ID |
| sessionTitle | string \| null | 会话标题 |
| count | number | 消息条数 |
| messages | array | 消息列表 |

**messages[] 元素**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 消息 ID |
| role | string | `USER` 或 `ASSISTANT` |
| content | string \| null | 文本内容 |
| createdAt | string | ISO 日期时间 |

- **404**：会话不存在或不属于当前用户。

---

## 3. 助手任务 `/api/v1/users/me/tasks`

助手任务与成长计划中的 `tasks` 表无关，数据来自 `user_assistant_tasks`。

### 3.1 查询当前用户的助手任务

- **方法 / 路径**：`GET /api/v1/users/me/tasks`
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 筛选：`OPEN`、`DONE`、`CANCELLED`；不传则返回全部 |

- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| count | number | 条数 |
| tasks | array | 任务列表 |

**tasks[] 元素**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 任务 ID |
| title | string | 标题 |
| description | string \| null | 描述 |
| status | string | `OPEN` / `DONE` / `CANCELLED` |
| dueDate | string \| null | 截止日期 `yyyy-MM-dd` |
| reminderSentAt | string \| null | 最近一次到期提醒发送时间 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

> 任务的创建、更新、取消主要由 **AI 对话接口** 内模型调用后端工具完成；本接口供 App 任务列表页直接展示。

---

## 4. 站内通知 `/api/v1/users/me/notifications`

### 4.1 未读数量

- **方法 / 路径**：`GET /api/v1/users/me/notifications/unread-count`
- **200 响应体**：`{ "count": <number> }`

### 4.2 通知列表

- **方法 / 路径**：`GET /api/v1/users/me/notifications`
- **查询参数**：

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| unreadOnly | boolean | `false` | `true` 时仅未读 |

- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| count | number | 条数 |
| items | array | 通知列表，按创建时间降序 |

**items[] 元素**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 通知 ID |
| type | string | 如 `TASK_DUE_REMINDER` |
| title | string | 标题 |
| body | string | 正文摘要 |
| taskId | number \| null | 关联助手任务 ID |
| sessionId | number \| null | 关联会话 ID |
| messageId | number \| null | 关联消息 ID |
| readAt | string \| null | 已读时间；未读为 `null` |
| createdAt | string | 创建时间 |

### 4.3 标记单条已读

- **方法 / 路径**：`PATCH /api/v1/users/me/notifications/{id}/read`
- **204**：成功，无响应体。
- **404**：通知不存在。

### 4.4 全部标记已读

- **方法 / 路径**：`PATCH /api/v1/users/me/notifications/read-all`
- **204**：成功，无响应体。

---

## 5. WebSocket `/ws/v1/chat`

- **地址**：`ws://localhost:8000/ws/v1/chat`（生产使用 `wss`）
- **鉴权**（握手阶段二选一）：
  - Query：`?token={accessToken}`
  - Header：`Authorization: Bearer {accessToken}`
- **说明**：连接成功后仅接收服务端推送；鉴权失败则握手失败。

### 5.1 服务端推送 JSON

**对话新回复**（`type` = `CHAT_REPLY`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| type | string | 固定 `CHAT_REPLY` |
| sessionId | number | 会话 ID |
| messageId | number | 助手消息 ID |
| contentPreview | string | 正文预览（最长约 120 字） |
| unreadCount | number | 站内通知未读数 |

**任务到期提醒**（`type` = `TASK_DUE_REMINDER`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| type | string | 固定 `TASK_DUE_REMINDER` |
| notificationId | number | 通知 ID |
| sessionId | number | 「任务提醒」会话 ID |
| messageId | number | 会话内消息 ID |
| taskId | number | 助手任务 ID |
| title | string | 通知标题 |
| body | string | 通知正文 |
| unreadCount | number | 站内通知未读数 |

**每周陪伴回顾**（`type` = `WEEKLY_COMPANION_DIGEST`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| type | string | 固定 `WEEKLY_COMPANION_DIGEST` |
| notificationId | number | 通知 ID |
| sessionId | number | 「本周回顾」会话 ID |
| messageId | number | 会话内消息 ID |
| taskId | null | 本类型无任务关联 |
| title | string | 如 `本周回顾 · 2026-W20` |
| body | string | 回顾正文预览（最长约 500 字） |
| unreadCount | number | 站内通知未读数 |

推送时刻：用户本地 **周六** `app.companion-memory.digest-delivery-time`（默认 `08:00`，与仅 `due_date` 任务提醒同窗）。需 `user_notification_settings.weekly_companion_digest=true`。

---

## 6. 配置与安全（部署备忘）

| 配置项 | 说明 |
|--------|------|
| `MIMO_API_KEY` / `app.chat.providers.mimo.api-key` | MiMo 对话 Key |
| `AI_CHAT_DEFAULT_PROVIDER` | 默认提供商 |
| `AI_CHAT_MULTI_PHASE_ENABLED` | 多阶段规划开关 |
| `AI_CHAT_PUSH_ON_REPLY` | 对话回复 WebSocket 推送 |
| `APP_TASK_REMINDER_ENABLED` | 是否执行定时提醒 |
| `APP_TASK_REMINDER_CRON` | 提醒 cron 表达式 |
| `APP_TASK_REMINDER_PUSH` | 提醒是否 WebSocket 推送 |
| `APP_COMPANION_MEMORY_ENABLED` | 是否启用每周陪伴记忆总结 |
| `APP_COMPANION_MEMORY_SUMMARIZE_CRON` | 周总结 cron（默认周六 03:00） |
| `APP_COMPANION_MEMORY_DIGEST_TIME` | 用户本地周六推送时刻（默认 08:00） |
| `APP_COMPANION_MEMORY_DIGEST_PUSH` | 本周回顾是否 WebSocket 推送 |

---

## 7. 与数据脚本的关系

| 脚本 | 作用 |
|------|------|
| `scripts/mysql-ai-chat.sql` | 会话、消息、助手任务表 |
| `scripts/mysql-ai-chat-task-reminder.sql` | 任务提醒字段 |
| `scripts/mysql-in-app-notifications.sql` | 站内通知表 |

---

## 8. 文档维护

接口变更时，请同步更新 **本文件**、`md文档/AI对话与助手任务-流程说明.md`、`md文档/数据库.md` 及 `docs/openapi/v1-ai-chat.yaml`。
