# HTTP 接口说明 — 认证与用户资料（v1）

本文档描述当前后端已实现的 **REST 接口**，与代码路径一致，便于本地查阅与前后端对齐。  
（不依赖 Swagger 在线页面；若需在线调试，仍可自行访问 `http://localhost:8000/swagger-ui.html`。）

---

## 1. 通用约定

### 1.1 服务地址

- 默认主机：`http://localhost:8000`（端口见 `application.yaml` 中 `server.port`，可用环境变量 `PORT` 覆盖）。

### 1.2 URL 前缀

- 所有业务接口前缀：`/api/v1`

### 1.3 请求头

| 头名称 | 说明 |
|--------|------|
| `Content-Type` | 含请求体时使用 `application/json` |
| `Authorization` | 需鉴权接口：`Bearer {accessToken}`（`Bearer` 与令牌之间有一个空格） |

### 1.4 公开接口（无需 `Authorization`）

以下路径由过滤器放行，不校验 JWT：

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/forgot-password`

其余以 `/api/` 开头的接口均需携带有效访问令牌（过滤器未拦截的非 `/api/` 路径如 Swagger 文档等除外）。

### 1.5 访问令牌（JWT）载荷说明

签发时包含（供前端理解，一般无需解析）：`sub`（内部用户 id）、`uid`（对外用户标识）、`sid`（会话 id，用于登出/改密保留当前设备）、`jti`、`exp` 等。

### 1.6 统一错误响应

业务异常与校验失败时，响应体多为 JSON：

```json
{
  "code": "NOT_FOUND",
  "message": "具体说明"
}
```

常见 `code`：`NOT_FOUND`、`CONFLICT`、`UNAUTHORIZED`、`FEATURE_DISABLED`（或业务子码见下文）、`VALIDATION_ERROR`、`BAD_REQUEST`、`INTERNAL_ERROR`。HTTP 状态码与语义对应（如 401、404、409、503 等）。

过滤器直接返回的 401 JSON 字段名相同（`code` / `message`）。

---

## 2. 认证相关 `/api/v1/auth`

### 2.1 登录（暂不可用）

- **方法 / 路径**：`POST /api/v1/auth/login`
- **鉴权**：不需要
- **说明**：当前固定返回 **503**，占位给前端联调路径。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 邮箱或手机号 |
| password | string | 是 | 密码 |
| deviceId | string | 是 | 客户端设备唯一标识（与会话 `device_id` 对应） |

- **503 响应示例**：`code` 为 `AUTH_LOGIN_DISABLED`，`message` 为功能不可用说明。

---

### 2.2 注册（暂不可用）

- **方法 / 路径**：`POST /api/v1/auth/register`
- **鉴权**：不需要
- **说明**：当前固定返回 **503**。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 邮箱或手机号 |
| password | string | 是 | 密码 |
| deviceId | string | 是 | 客户端设备唯一标识 |
| nickname | string | 否 | 昵称 |

- **503 响应**：`code` 为 `AUTH_REGISTER_DISABLED`。

---

### 2.3 忘记密码（暂未开放）

- **方法 / 路径**：`POST /api/v1/auth/forgot-password`
- **鉴权**：不需要
- **说明**：当前固定返回 **503**（邮件/短信等通道未接入）。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 注册邮箱或手机号 |

- **503 响应**：`code` 为 `AUTH_FORGOT_PASSWORD_DISABLED`。

---

### 2.4 刷新访问令牌

- **方法 / 路径**：`POST /api/v1/auth/refresh`
- **鉴权**：不需要
- **说明**：使用明文 `refreshToken` 与 `deviceId` 换新令牌（会话仅按 `user_id` + `device_id` 识别设备）；服务端在库中按 **SHA-256 哈希** 存储刷新令牌，滚动更新。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 登录或上次刷新得到的明文刷新令牌 |
| deviceId | string | 是 | 须与创建会话时一致 |

- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| accessToken | string | 新 JWT |
| refreshToken | string | 新明文刷新令牌（请安全保存） |
| tokenType | string | 固定 `Bearer` |
| expiresIn | number | 访问令牌有效秒数 |
| uid | string | 对外用户标识 |

- **401**：刷新令牌无效、过期、或与 `deviceId` 不匹配等。

---

### 2.5 修改密码

- **方法 / 路径**：`POST /api/v1/auth/change-password`
- **鉴权**：需要 `Authorization: Bearer {accessToken}`
- **说明**：校验原密码后更新；成功后 **保留当前 JWT 对应会话**，**撤销该用户其余设备会话**。身份优先使用带密码的 `email`，否则 `phone`；若无可用密码身份则 **400**。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 当前密码 |
| newPassword | string | 是 | 新密码，至少 8 位 |

- **200**：无响应体。
- **401**：原密码错误等。

---

### 2.6 登出

- **方法 / 路径**：`POST /api/v1/auth/logout`
- **鉴权**：需要 Bearer
- **说明**：默认只撤销当前 JWT 对应会话（依赖载荷中的会话 id）；`allDevices=true` 时撤销该用户 **全部** 会话。若令牌中无会话 id，为安全起见会撤销全部会话。
- **请求体**（JSON，可选）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| allDevices | boolean | 否 | 默认 `false`；`true` 表示全部设备下线 |

- **200**：无响应体。

---

## 3. 当前用户资料 `/api/v1/users`

以下接口均需 Bearer；且用户 `status` 须为 **1（正常）**（注销/禁用会返回未授权类错误）。

### 3.1 获取当前用户资料

- **方法 / 路径**：`GET /api/v1/users/me`
- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| uid | string | 对外 uid |
| nickname | string \| null | 昵称 |
| avatarUrl | string \| null | 头像 URL |
| weeklyHours | number \| null | 每周可投入小时数 0–40 |
| timezone | string | 时区 |
| language | string | 语言偏好 |
| status | number | 1 正常 / 2 禁用 / 3 注销 |
| createdAt | string | 注册时间（ISO 风格日期时间，见全局 Jackson 配置） |
| updatedAt | string | 最近更新时间 |

---

### 3.2 更新当前用户资料

- **方法 / 路径**：`PATCH /api/v1/users/me`
- **请求体**（JSON）：字段均可选，未传的字段不改。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| nickname | string | 最长 50 | 昵称 |
| avatarUrl | string | 最长 500 | 头像 |
| weeklyHours | number | 0–40 | 每周可投入小时数 |

- **200**：同 3.1 响应结构。

---

### 3.3 注销当前账号（软删除）

- **方法 / 路径**：`DELETE /api/v1/users/me`
- **说明**：将用户状态置为 **3（注销）**，并撤销 **全部** 会话。
- **204**：成功，无响应体。

---

## 4. 配置与安全（部署备忘）

| 配置项 | 说明 |
|--------|------|
| `JWT_SECRET_KEY` / `app.jwt.secret-key` | HS256 密钥，**至少 32 字节**；过短或未配置时，受保护接口可能返回配置类错误。 |
| `app.jwt.access-token-expire-minutes` | 访问令牌有效分钟数。 |
| `app.jwt.refresh-token-expire-days` | 刷新令牌对应会话过期天数。 |

---

## 5. 与数据脚本的关系

批量测试用户可参考仓库内 SQL：`scripts/seed-5-test-users.sql`（与库表结构对应，不在本文展开）。

---

## 6. 文档维护

接口变更时，请同步更新 **本文件**（`md文档/HTTP接口-认证与用户.md`），保持与控制器代码一致。
