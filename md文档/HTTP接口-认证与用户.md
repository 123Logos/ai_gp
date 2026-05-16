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
- `POST /api/v1/auth/register/send-code`
- `POST /api/v1/auth/password/reset`
- `POST /api/v1/auth/password/reset/send-code`
- `POST /api/v1/auth/refresh`

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

常见 `code`：`NOT_FOUND`、`CONFLICT`、`UNAUTHORIZED`、`VALIDATION_ERROR`、`BAD_REQUEST`、`INTERNAL_ERROR`。HTTP 状态码与语义对应（如 400、401、409 等）。

过滤器直接返回的 401 JSON 字段名相同（`code` / `message`）。

### 1.7 注册与登录：逐步要传的参数与返回值

以下均指 **请求体 JSON**（`Content-Type: application/json`），**无需** `Authorization`，除非另有说明。

#### 会话与换机说明（无需客户端 `deviceId`）

- 登录、注册成功后，服务端在 `user_sessions` 使用 **内置设备键** `builtin-{userId}` 与刷新令牌绑定；**客户端不必、也不应再传 `deviceId`**。
- 换手机后只要 **账号 + 密码** 重新登录即可拿到新令牌；**刷新**只需携带当前 `refreshToken`。

---

#### A. 用户注册（两步）

| 步骤 | 接口 | 发送（请求体） | 成功时得到（响应体） |
|------|------|----------------|----------------------|
| **A1 发验证码** | `POST /api/v1/auth/register/send-code` | `{ "email": "<未注册邮箱>" }` | `{ "expiresInSeconds": <秒>, "debugCode": "<仅开发配置开启时有>" }` |
| **A2 提交注册** | `POST /api/v1/auth/register` | `{ "email": "<与A1相同>", "password": "<至少8位>", "nickname": "<必填，最长50>", "verificationCode": "<6位邮箱验证码>" }` | 与登录相同结构的 **令牌包**（见下表「令牌包」） |

- **A1 说明**：仅支持 **邮箱**；`email` 须为 **未注册**；同一邮箱 **60 秒内** 只能发一次码。配置 `spring.mail` 时验证码走 **邮件**（见 `md文档/QQ邮箱SMTP与验证码.md`），否则为进程内内存。生产环境一般 **没有** `debugCode`；本地可把 `app.auth.verification-debug-return-code` 设为 `true` 便于联调。
- **A2 说明**：须与 A1 使用同一邮箱；**昵称必填**。手机号等请在登录后通过 **PATCH `/api/v1/users/me`** 绑定（见第 3 章）。成功后 **自动登录**，直接拿到令牌。

---

#### B. 用户登录（一步）

| 步骤 | 接口 | 发送（请求体） | 成功时得到（响应体） |
|------|------|----------------|----------------------|
| **B1 登录** | `POST /api/v1/auth/login` | `{ "account": "<邮箱或11位手机号或16位uid>", "password": "<密码>" }` | **令牌包**（见下表） |

- **`account` 规则**：含 `@` 按邮箱；长度为 16 且以 `U`/`u` 开头按对外 `uid`；否则按手机号与库中 `identifier` 完全一致匹配。

---

#### 令牌包（`TokenResponse`，注册成功与登录成功结构相同）

| 字段 | 类型 | 含义与后续用法 |
|------|------|----------------|
| `accessToken` | string | **访问令牌（JWT）**。之后调受保护接口时放在请求头：`Authorization: Bearer <accessToken>` |
| `refreshToken` | string | **明文刷新令牌**，仅客户端保存；**不要**当作用户 id。过期前用下面「续期」换新的 access + refresh |
| `tokenType` | string | 固定为 `Bearer` |
| `expiresIn` | number | `accessToken` 有效秒数（由 `app.jwt.access-token-expire-minutes` 换算） |
| `uid` | string | 对外用户 id（`users.uid`），用于展示或业务关联 |

**续期（可选）**：`POST /api/v1/auth/refresh`，请求体：`{ "refreshToken": "<当前明文refresh>" }`，响应体仍是上表令牌包（滚动换新 refresh）。

---

## 2. 认证相关 `/api/v1/auth`

### 2.1 登录

- **方法 / 路径**：`POST /api/v1/auth/login`
- **鉴权**：不需要
- **说明**：校验密码后为该用户在库中 **创建或更新** 内置会话键（`builtin-{userId}`）对应的会话行，返回访问令牌与明文刷新令牌（库中仅存刷新令牌哈希）。  
  **`account` 三种形态**（按以下顺序识别）：
  1. 含 `@` → 按 **邮箱** 查 `user_identities`（`identity_type=email`，identifier 会按小写匹配）。
  2. 长度为 **16** 且首字符为 **`U` 或 `u`** → 视为对外 **`users.uid`**（会先转成大写再查表），再取该用户下已设置密码的邮箱或手机身份做密码校验。
  3. 其它 → 按 **手机号** 查 `user_identities`（`identity_type=phone`，identifier 与传入字符串一致）。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 邮箱、手机号，或 16 位对外 uid（`U` 开头） |
| password | string | 是 | 密码 |

- **200 响应体**：与 **2.6 刷新访问令牌** 中 `TokenResponse` 结构相同（`accessToken`、`refreshToken`、`tokenType`、`expiresIn`、`uid`）。
- **401**：账号或密码错误、账号已禁用/注销等（文案可能统一为「账号或密码错误」以降低枚举风险）。

**示例**：测试数据用户可用 `account = "UTEST00000000001"` 或 `account = "test01@example.com"`，密码见 `scripts/seed-5-test-users.sql` 说明。

---

### 2.2 注册：发送验证码

- **方法 / 路径**：`POST /api/v1/auth/register/send-code`
- **鉴权**：不需要
- **说明**：向 **未注册邮箱** 发注册验证码（仅邮箱；配置 SMTP 时走邮件，否则内存 + 可选 `debugCode`）。同一邮箱 **60 秒内** 不可重复发送。SMTP 配置见 `md文档/QQ邮箱SMTP与验证码.md`。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 有效邮箱，与后续注册请求一致（服务端存小写） |

- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| expiresInSeconds | number | 验证码有效秒数（与 `app.auth.verification-ttl-seconds` 一致） |
| debugCode | string \| null | 仅当 `app.auth.verification-debug-return-code=true` 时返回明文验证码，便于本地联调；**生产必须为 false** |

- **409**：该邮箱已注册。
- **503**：已配置 SMTP 但邮件发送失败（如授权码错误、网络问题）；错误码 `SERVICE_UNAVAILABLE`，本次发码已回滚，可稍后重试。

---

### 2.3 注册

- **方法 / 路径**：`POST /api/v1/auth/register`
- **鉴权**：不需要
- **说明**：校验 **邮箱** 验证码（一次性）后创建 `users`、绑定 **邮箱+密码** 身份、写入默认通知设置，并 **自动登录** 返回令牌对。邮箱与发码阶段须一致（小写存库）。**手机号请在登录后 PATCH 资料绑定。**
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 与发码时相同的邮箱 |
| password | string | 是 | 登录密码，至少 8 位 |
| nickname | string | 是 | 昵称，最长 50 |
| verificationCode | string | 是 | 邮箱收到的 6 位数字验证码 |

- **200 响应体**：同 **2.6** `TokenResponse`。
- **401**：验证码错误、过期等。
- **409**：该邮箱已注册。

---

### 2.4 找回密码：发送验证码

- **方法 / 路径**：`POST /api/v1/auth/password/reset/send-code`
- **鉴权**：不需要
- **说明**：向 **已注册且已设置密码** 的邮箱或手机号发码；频控同 2.2。邮箱在已配置 SMTP 时发邮件，否则内存验证码。
- **请求体**：同 2.2（`{ "account": "..." }`）。
- **200 响应体**：同 2.2（`expiresInSeconds`、`debugCode`）。
- **400**：账号不存在、未设置密码登录等。
- **503**：同 2.2（SMTP 发送失败时）。

---

### 2.5 找回密码：重置密码

- **方法 / 路径**：`POST /api/v1/auth/password/reset`
- **鉴权**：不需要
- **说明**：校验验证码后更新密码哈希，并 **撤销该用户全部会话**（所有设备需重新登录）。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 邮箱或手机号 |
| verificationCode | string | 是 | 6 位验证码 |
| newPassword | string | 是 | 新密码，至少 8 位 |

- **204**：成功，无响应体。
- **401**：验证码错误、过期等。

---

### 2.6 刷新访问令牌

- **方法 / 路径**：`POST /api/v1/auth/refresh`
- **鉴权**：不需要
- **说明**：使用明文 `refreshToken` 换新令牌；服务端在库中按 **SHA-256 哈希** 存储刷新令牌，滚动更新。无需也不校验客户端设备指纹。
- **请求体**（JSON）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 登录或上次刷新得到的明文刷新令牌 |

- **200 响应体**（JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| accessToken | string | 新 JWT |
| refreshToken | string | 新明文刷新令牌（请安全保存） |
| tokenType | string | 固定 `Bearer` |
| expiresIn | number | 访问令牌有效秒数 |
| uid | string | 对外用户标识 |

- **401**：刷新令牌无效或已过期等。

---

### 2.7 修改密码

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

### 2.8 登出

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
| phone | string \| null | 已绑定手机号（脱敏，如 `138****8000`）；未绑定为 `null` |
| identitySummary | string \| null | 身份/角色简述 |
| hobbies | string \| null | 爱好 |
| explorationInterests | string \| null | 希望探索的专业方向等 |
| onboardingCompleted | boolean | 是否已完成首次画像填写 |

---

### 3.2 更新当前用户资料

- **方法 / 路径**：`PATCH /api/v1/users/me`
- **请求体**（JSON）：字段均可选，未传的字段不改。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| nickname | string | 最长 50 | 昵称 |
| avatarUrl | string | 最长 500 | 头像 |
| weeklyHours | number | 0–40 | 每周可投入小时数 |
| phone | string | 11 位大陆号 `1[3-9]…` | 绑定到当前用户；**无短信验证、不写密码**，仅作资料展示与后续扩展；与库中其他用户冲突时 **409** |

- **200**：同 3.1 响应结构。

---

### 3.3 更新首次登录用户画像（身份、爱好、探索方向）

- **方法 / 路径**：`PATCH /api/v1/users/me/onboarding`
- **说明**：用于注册后首次进入产品时的问卷/画像；**部分更新**：请求体里未出现的字段保持原值。传 **空字符串** `""` 可清空对应文本字段；`onboardingCompleted` 未传则不改。
- **请求体**（JSON）：

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| identitySummary | string | 最长 200 | 身份或角色简述，如学生、产品经理 |
| hobbies | string | 最长 4000 | 爱好 |
| explorationInterests | string | 最长 4000 | 希望探索的专业方向、领域等 |
| onboardingCompleted | boolean | — | 是否标记为已完成首次画像；`true` / `false` |

- **200**：同 3.1 响应结构。
- **400**：字段超长等校验错误。

---

### 3.4 注销当前账号（软删除）

- **方法 / 路径**：`DELETE /api/v1/users/me`
- **说明**：将用户状态置为 **3（注销）**，并撤销 **全部** 会话。
- **204**：成功，无响应体。

---

## 4. 配置与安全（部署备忘）

**本地与生产如何分别配置 JWT、数据库等**：见 **`md文档/环境与密钥配置.md`**。

| 配置项 | 说明 |
|--------|------|
| `JWT_SECRET_KEY` / `app.jwt.secret-key` | HS256 密钥，**至少 32 字节**；注册/登录签发令牌必填；生产仅环境变量、与本地密钥必须不同。 |
| `app.jwt.access-token-expire-minutes` | 访问令牌有效分钟数。 |
| `app.jwt.refresh-token-expire-days` | 刷新令牌对应会话过期天数。 |
| `app.auth.verification-ttl-seconds` | 注册/找回密码验证码有效秒数（默认 300）。 |
| `app.auth.verification-debug-return-code` | 为 `true` 时发码接口 JSON 会带 `debugCode`；**仅开发联调**，生产必须为 `false`。 |

---

## 5. 与数据脚本的关系

批量测试用户可参考仓库内 SQL：`scripts/seed-5-test-users.sql`（与库表结构对应，不在本文展开）。

---

## 6. 相关文档

| 文档 | 说明 |
|------|------|
| `md文档/环境与密钥配置.md` | JWT、.env、dev/prod 配置分层 |
| `md文档/HTTP接口-AI对话与通知.md` | AI 对话、助手任务、站内通知、WebSocket |
| `md文档/AI对话与助手任务-流程说明.md` | 对话与提醒业务流程（非接口字段） |

---

## 7. 文档维护

接口变更时，请同步更新 **本文件**（`md文档/HTTP接口-认证与用户.md`），保持与控制器代码一致。
