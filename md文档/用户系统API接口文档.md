# AI 智能日程管家 - 用户系统 API 接口文档

> 版本：V1.0  
> 技术栈：Java 21 / Spring Boot 4.x / MyBatis-Plus / MySQL 8.0  
> API 规范：RESTful  
> 认证协议：JWT (Access Token + Refresh Token)  
> 文档工具：SpringDoc OpenAPI (Swagger 3)

---

## 目录

- [1. 通用说明](#1-通用说明)
- [2. 数据模型定义](#2-数据模型定义)
- [3. 验证码接口](#3-验证码接口)
- [4. 注册与登录接口](#4-注册与登录接口)
- [5. 用户信息接口](#5-用户信息接口)
- [6. 第三方账号绑定接口](#6-第三方账号绑定接口)
- [7. 会话与设备管理接口](#7-会话与设备管理接口)
- [8. API Key 管理接口](#8-api-key-管理接口)
- [9. 附录](#9-附录)

---

## 1. 通用说明

### 1.1 基础 URL

```
开发环境：http://localhost:8080/api/v1
生产环境：https://api.example.com/api/v1
```

### 1.2 统一响应格式

所有接口返回统一 JSON 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1714867200000,
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，200 表示成功 |
| message | String | 提示信息 |
| data | Object | 业务数据 |
| timestamp | long | 响应时间戳 (ms) |
| traceId | String | 请求追踪 ID，用于链路排查 |

### 1.3 认证方式

除登录、注册、发送验证码等公开接口外，其他接口均需在请求头中携带 Access Token：

```
Authorization: Bearer <access_token>
```

### 1.4 公共请求头

| 请求头 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 鉴权接口必填 | Bearer Token |
| X-Device-Id | String | 登录/注册必填 | 客户端生成的设备唯一 ID |
| X-Device-Platform | String | 否 | 设备平台：ios / android / web |
| X-Device-Name | String | 否 | 设备名称 |
| X-Request-Id | String | 否 | 请求幂等 ID，防重放攻击 |
| Accept-Language | String | 否 | 语言偏好，如 zh-CN, en-US |

### 1.5 设备信任说明

- 客户端首次启动时生成唯一 `device_id`，存储在 Keychain/Keystore 中。
- 登录时携带 `X-Device-Id`，服务端判断该设备是否已信任：
  - **已信任**：直接登录，更新会话。
  - **未信任**：触发 `trust_device` 场景验证码流程。
- 验证成功后设置 `is_trusted=1`，后续该设备登录可免验证码。

### 1.6 Token 说明

| Token 类型 | 有效期 | 用途 |
|-----------|--------|------|
| Access Token | 2 小时 | 请求鉴权，放在 Authorization 头 |
| Refresh Token | 30 天 | 刷新 Access Token，存储在数据库（SHA-256 哈希） |

---

## 2. 数据模型定义

### 2.1 用户信息 VO (`UserVO`)

```json
{
  "uid": "u_a1b2c3d4e5f6g7h8",
  "nickname": "张三",
  "avatarUrl": "https://example.com/avatars/xxx.jpg",
  "status": 1,
  "timezone": "Asia/Shanghai",
  "language": "zh-CN",
  "identities": [
    {
      "identityType": "phone",
      "identifier": "138****1234",
      "isPrimary": true,
      "verifiedAt": "2026-05-10T10:00:00"
    }
  ],
  "createdAt": "2026-05-10T10:00:00",
  "updatedAt": "2026-05-10T10:00:00"
}
```

### 2.2 登录响应 (`LoginResponse`)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 7200,
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
  "tokenType": "Bearer",
  "user": {
    "uid": "u_a1b2c3d4e5f6g7h8",
    "nickname": "张三"
  },
  "deviceTrusted": true
}
```

### 2.3 会话信息 VO (`SessionVO`)

```json
{
  "sessionId": 1,
  "deviceId": "device-uuid-xxx",
  "platform": "ios",
  "deviceName": "iPhone 15 Pro",
  "ipAddress": "192.168.1.100",
  "isTrusted": true,
  "isCurrent": true,
  "createdAt": "2026-05-10T10:00:00",
  "expiresAt": "2026-06-09T10:00:00"
}
```

### 2.4 API Key VO (`ApiKeyVO`)

```json
{
  "id": 1,
  "keyAlias": "我的 OpenAI Key",
  "provider": "openai",
  "maskedKey": "sk-...x7kD",
  "isEnabled": true,
  "isValid": true,
  "lastCheckedAt": null,
  "createdAt": "2026-05-10T10:00:00"
}
```

---

## 3. 验证码接口

### 3.1 发送验证码

- **请求路径**：`/api/v1/auth/code`
- **请求方法**：`POST`
- **接口描述**：向指定手机号或邮箱发送验证码，用于注册、登录、绑定设备等场景。需携带设备 ID 以判断是否在可信设备上。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identifier | String | 是 | 手机号或邮箱 |
| scene | String | 是 | 场景：`register` / `login` / `trust_device` / `reset_password` / `bind` |
| identityType | String | 是 | 认证类型：`phone` / `email` |

#### 请求示例

```json
{
  "identifier": "13800138000",
  "scene": "register",
  "identityType": "phone"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | int | 业务状态码 |
| message | String | 提示信息，如"验证码已发送" |
| data.expiresIn | int | 验证码有效期（秒），默认 300 |
| data.cooldown | int | 重发冷却时间（秒），默认 60 |
| data.identifier | String | 脱敏标识，如 "138****8000" |

#### 响应示例

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {
    "expiresIn": 300,
    "cooldown": 60,
    "identifier": "138****8000"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 发送成功 |
| 400 | 参数错误 | identifier / scene / identityType 不合法 |
| 429 | 发送过于频繁 | 该标识已有有效验证码且在冷却期内 |
| 500 | 发送失败 | 短信/邮件服务异常 |

---

## 4. 注册与登录接口

### 4.1 用户注册

- **请求路径**：`/api/v1/auth/register`
- **请求方法**：`POST`
- **接口描述**：使用手机号或邮箱注册新用户。先发送验证码，再凭验证码完成注册。支持首次注册时设置密码。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identifier | String | 是 | 手机号或邮箱 |
| identityType | String | 是 | 认证类型：`phone` / `email` |
| code | String | 是 | 验证码 |
| password | String | 否 | 密码（建议设置，6-32位，含字母和数字） |
| nickname | String | 否 | 昵称，不传则自动生成默认昵称 |
| deviceId | String | 是 | 设备 ID（从请求头 X-Device-Id 获取） |

#### 请求示例

```json
{
  "identifier": "13800138000",
  "identityType": "phone",
  "code": "123456",
  "password": "Abc123456",
  "nickname": "张三"
}
```

#### 响应参数

返回 `LoginResponse` 结构（见 2.2 节），`deviceTrusted` 为该设备首次登录时为 `false`。

#### 响应示例

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
    "tokenType": "Bearer",
    "user": {
      "uid": "u_a1b2c3d4e5f6g7h8",
      "nickname": "张三"
    },
    "deviceTrusted": false
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 注册成功 |
| 400 | 验证码错误 | code 不正确或已过期 |
| 409 | 账号已存在 | 该手机号/邮箱已被注册 |
| 422 | 密码强度不足 | 密码不符合安全规范 |

---

### 4.2 密码登录

- **请求路径**：`/api/v1/auth/login/password`
- **请求方法**：`POST`
- **接口描述**：使用手机号/邮箱 + 密码登录。若设备未信任，需额外走验证码流程。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identifier | String | 是 | 手机号或邮箱 |
| identityType | String | 是 | 认证类型：`phone` / `email` |
| password | String | 是 | 密码 |

#### 请求示例

```json
{
  "identifier": "13800138000",
  "identityType": "phone",
  "password": "Abc123456"
}
```

#### 响应参数

返回 `LoginResponse` 结构。若 `deviceTrusted=false`，前端应引导用户进入验证码验证流程。

#### 响应示例（设备已信任）

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
    "tokenType": "Bearer",
    "user": {
      "uid": "u_a1b2c3d4e5f6g7h8",
      "nickname": "张三"
    },
    "deviceTrusted": true
  }
}
```

#### 响应示例（设备未信任）

```json
{
  "code": 200,
  "message": "登录成功，请验证新设备",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
    "tokenType": "Bearer",
    "user": {
      "uid": "u_a1b2c3d4e5f6g7h8",
      "nickname": "张三"
    },
    "deviceTrusted": false,
    "requireDeviceVerify": true
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 登录成功 |
| 401 | 密码错误 | 账号或密码不正确 |
| 404 | 账号不存在 | 该手机号/邮箱未注册 |
| 423 | 账号已禁用 | 用户状态为禁用或注销 |

---

### 4.3 验证码登录

- **请求路径**：`/api/v1/auth/login/code`
- **请求方法**：`POST`
- **接口描述**：使用手机号/邮箱 + 验证码登录。如果设备未信任，此接口同时完成设备信任验证。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identifier | String | 是 | 手机号或邮箱 |
| identityType | String | 是 | 认证类型：`phone` / `email` |
| code | String | 是 | 验证码（场景：`login` 或 `trust_device`） |

#### 请求示例

```json
{
  "identifier": "13800138000",
  "identityType": "phone",
  "code": "123456"
}
```

#### 响应参数

返回 `LoginResponse` 结构。若原设备未信任，验证成功后 `deviceTrusted` 为 `true`。

#### 响应示例

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
    "tokenType": "Bearer",
    "user": {
      "uid": "u_a1b2c3d4e5f6g7h8",
      "nickname": "张三"
    },
    "deviceTrusted": true
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 登录成功 |
| 400 | 验证码错误 | code 不正确或已过期 |
| 404 | 账号不存在 | 该手机号/邮箱未注册（未注册时请使用注册接口） |

---

### 4.4 刷新 Token

- **请求路径**：`/api/v1/auth/token/refresh`
- **请求方法**：`POST`
- **接口描述**：使用 Refresh Token 换取新的 Access Token。旧的 Refresh Token 会被作废，返回新的 Refresh Token（Token 轮换机制）。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | String | 是 | 登录时返回的 Refresh Token |

#### 请求示例

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| accessToken | String | 新的 Access Token |
| expiresIn | int | Access Token 有效期（秒） |
| refreshToken | String | 新的 Refresh Token |
| tokenType | String | Token 类型，固定 `Bearer` |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "refreshToken": "bmV3IHJlZnJl...",
    "tokenType": "Bearer"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 刷新成功 |
| 401 | refreshToken 无效 | Token 不存在、已过期或已被作废 |
| 401 | 会话已撤销 | 该会话已被用户主动撤销 |

---

### 4.5 退出登录

- **请求路径**：`/api/v1/auth/logout`
- **请求方法**：`POST`
- **接口描述**：使当前会话失效，撤销 Refresh Token 和 Access Token。需携带 Access Token。

#### 请求头

| 请求头 | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | Bearer \<access_token\> |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | String | 否 | 如需同时撤销 Refresh Token |

#### 请求示例

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "已退出登录",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 退出成功 |
| 401 | 未授权 | Token 无效或已过期 |

---

## 5. 用户信息接口

> 以下接口均需携带 `Authorization` 头。

### 5.1 获取当前用户信息

- **请求路径**：`/api/v1/user/me`
- **请求方法**：`GET`
- **接口描述**：获取当前登录用户的详细信息，包括绑定的认证方式列表。

#### 请求示例

```
GET /api/v1/user/me
Authorization: Bearer <access_token>
```

#### 响应参数

返回 `UserVO` 结构（见 2.1 节）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uid": "u_a1b2c3d4e5f6g7h8",
    "nickname": "张三",
    "avatarUrl": "https://example.com/avatars/xxx.jpg",
    "status": 1,
    "timezone": "Asia/Shanghai",
    "language": "zh-CN",
    "identities": [
      {
        "identityType": "phone",
        "identifier": "138****8000",
        "isPrimary": true,
        "verifiedAt": "2026-05-10T10:00:00"
      }
    ],
    "createdAt": "2026-05-10T10:00:00",
    "updatedAt": "2026-05-10T10:00:00"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 查询成功 |
| 401 | 未授权 | Token 无效或已过期 |

---

### 5.2 更新用户信息

- **请求路径**：`/api/v1/user/me`
- **请求方法**：`PUT`
- **接口描述**：更新当前用户的昵称、头像、时区、语言等基本信息。支持部分更新（仅传需要修改的字段）。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称，1-50 字符 |
| avatarUrl | String | 否 | 头像 URL，不超过 500 字符 |
| timezone | String | 否 | 时区，如 `Asia/Shanghai` |
| language | String | 否 | 语言，如 `zh-CN`、`en-US` |

#### 请求示例

```json
{
  "nickname": "李四",
  "timezone": "America/New_York",
  "language": "en-US"
}
```

#### 响应参数

返回更新后的 `UserVO`。

#### 响应示例

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "uid": "u_a1b2c3d4e5f6g7h8",
    "nickname": "李四",
    "timezone": "America/New_York",
    "language": "en-US",
    "updatedAt": "2026-05-11T09:14:00"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 更新成功 |
| 400 | 参数不合法 | 昵称超长、时区格式错误等 |
| 401 | 未授权 | Token 无效或已过期 |

---

### 5.3 修改密码

- **请求路径**：`/api/v1/user/password`
- **请求方法**：`PUT`
- **接口描述**：已登录用户在知晓当前密码的情况下修改密码。如果需要重置密码（忘记密码），请使用验证码重置流程。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 当前密码 |
| newPassword | String | 是 | 新密码，6-32 位，须含字母和数字 |

#### 请求示例

```json
{
  "oldPassword": "Abc123456",
  "newPassword": "Xyz789012"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 修改成功 |
| 400 | 密码强度不足 | 新密码不符合安全规范 |
| 401 | 当前密码错误 | oldPassword 不正确 |
| 401 | 未授权 | Token 无效或已过期 |

---

### 5.4 重置密码

- **请求路径**：`/api/v1/auth/password/reset`
- **请求方法**：`POST`
- **接口描述**：通过验证码重置密码，无需登录。适用于用户忘记密码的场景。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identifier | String | 是 | 手机号或邮箱 |
| identityType | String | 是 | 认证类型：`phone` / `email` |
| code | String | 是 | 验证码（场景：`reset_password`） |
| newPassword | String | 是 | 新密码，6-32 位，须含字母和数字 |

#### 请求示例

```json
{
  "identifier": "13800138000",
  "identityType": "phone",
  "code": "123456",
  "newPassword": "NewPass789"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 重置成功 |
| 400 | 验证码错误 | code 不正确或已过期 |
| 400 | 密码强度不足 | 新密码不符合安全规范 |

---

### 5.5 注销账号

- **请求路径**：`/api/v1/user/me`
- **请求方法**：`DELETE`
- **接口描述**：注销当前用户账号（软删除，将用户状态置为 `3-注销`）。此操作不可逆，所有关联数据根据外键级联策略处理。

#### 请求头

| 请求头 | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | Bearer \<access_token\> |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 否 | 注销原因（选填，用于改进） |

#### 请求示例

```json
{
  "reason": "不再使用该服务"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "账号已注销",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 注销成功 |
| 401 | 未授权 | Token 无效或已过期 |
| 409 | 操作冲突 | 账号已处于注销状态 |

---

## 6. 第三方账号绑定接口

> 以下接口均需携带 `Authorization` 头。

### 6.1 绑定第三方账号

- **请求路径**：`/api/v1/user/identities`
- **请求方法**：`POST`
- **接口描述**：为当前用户绑定第三方账号（微信 / Apple / Google）。根据第三方 OAuth 流程，需传入第三方返回的授权码或 Token。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identityType | String | 是 | `wx` / `apple` / `google` |
| authCode | String | 是 | 第三方 OAuth 授权码 |
| redirectUri | String | 否 | 第三方回调地址（部分平台需要） |

#### 请求示例

```json
{
  "identityType": "wx",
  "authCode": "wx_auth_code_xxx",
  "redirectUri": "https://example.com/oauth/callback"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "绑定成功",
  "data": {
    "identityType": "wx",
    "identifier": "wx_openid_xxx",
    "isPrimary": false,
    "verifiedAt": "2026-05-11T09:14:00"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 绑定成功 |
| 400 | 第三方授权失败 | authCode 无效 |
| 409 | 已被其他账号绑定 | 该第三方账号已被其他用户绑定 |
| 409 | 已达到绑定上限 | 同一类型最多绑定一个 |

---

### 6.2 解绑第三方账号

- **请求路径**：`/api/v1/user/identities/{identityType}`
- **请求方法**：`DELETE`
- **接口描述**：解绑指定的第三方账号。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identityType | String | 是 | `wx` / `apple` / `google` |

#### 请求示例

```
DELETE /api/v1/user/identities/wx
Authorization: Bearer <access_token>
```

#### 响应示例

```json
{
  "code": 200,
  "message": "解绑成功",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 解绑成功 |
| 400 | 不可解绑 | 用户仅剩一种登录方式，无法解绑 |
| 404 | 未找到 | 该账号未绑定该类型的第三方 |

---

### 6.3 获取已绑定账号列表

- **请求路径**：`/api/v1/user/identities`
- **请求方法**：`GET`
- **接口描述**：获取当前用户已绑定的所有认证方式列表（含手机、邮箱、第三方账号）。

#### 请求示例

```
GET /api/v1/user/identities
Authorization: Bearer <access_token>
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| identities | Array | 已绑定认证方式列表 |

每个认证方式对象：

| 字段 | 类型 | 说明 |
|------|------|------|
| identityType | String | 认证类型 |
| identifier | String | 脱敏后的标识 |
| isPrimary | Boolean | 是否为主要认证方式 |
| verifiedAt | String | 验证通过时间 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "identities": [
      {
        "identityType": "phone",
        "identifier": "138****8000",
        "isPrimary": true,
        "verifiedAt": "2026-05-10T10:00:00"
      },
      {
        "identityType": "wx",
        "identifier": "wx_****_abc",
        "isPrimary": false,
        "verifiedAt": "2026-05-11T09:14:00"
      }
    ]
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 查询成功 |
| 401 | 未授权 | Token 无效或已过期 |

---

### 6.4 绑定手机号/邮箱（新增密码登录方式）

- **请求路径**：`/api/v1/user/identities/bind`
- **请求方法**：`POST`
- **接口描述**：为当前用户绑定新的手机号或邮箱，支持设置密码以便后续使用密码登录。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| identifier | String | 是 | 新手机号或邮箱 |
| identityType | String | 是 | `phone` / `email` |
| code | String | 是 | 验证码（场景：`bind`） |
| password | String | 否 | 密码（选填） |
| setPrimary | Boolean | 否 | 是否设为主要认证方式 |

#### 请求示例

```json
{
  "identifier": "user@example.com",
  "identityType": "email",
  "code": "654321",
  "password": "EmailPass789",
  "setPrimary": false
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "绑定成功",
  "data": {
    "identityType": "email",
    "identifier": "us****@example.com",
    "isPrimary": false,
    "verifiedAt": "2026-05-11T09:14:00"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 绑定成功 |
| 400 | 验证码错误 | code 不正确或已过期 |
| 409 | 已被其他账号绑定 | 该手机号/邮箱已被其他用户注册 |

---

## 7. 会话与设备管理接口

> 以下接口均需携带 `Authorization` 头。

### 7.1 获取已登录设备列表

- **请求路径**：`/api/v1/user/sessions`
- **请求方法**：`GET`
- **接口描述**：获取当前用户所有活跃会话（已登录设备）列表，方便用户管理自己的登录设备。

#### 请求示例

```
GET /api/v1/user/sessions
Authorization: Bearer <access_token>
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| sessions | Array | 活跃会话列表 |
| total | int | 当前活跃会话总数 |

每个会话对象为 `SessionVO`（见 2.3 节）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessions": [
      {
        "sessionId": 1,
        "deviceId": "device-uuid-xxx",
        "platform": "ios",
        "deviceName": "iPhone 15 Pro",
        "ipAddress": "192.168.1.100",
        "isTrusted": true,
        "isCurrent": true,
        "createdAt": "2026-05-10T10:00:00",
        "expiresAt": "2026-06-09T10:00:00"
      },
      {
        "sessionId": 2,
        "deviceId": "device-uuid-yyy",
        "platform": "web",
        "deviceName": "Chrome on Windows",
        "ipAddress": "203.0.113.50",
        "isTrusted": false,
        "isCurrent": false,
        "createdAt": "2026-05-11T08:00:00",
        "expiresAt": "2026-06-10T08:00:00"
      }
    ],
    "total": 2
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 查询成功 |
| 401 | 未授权 | Token 无效或已过期 |

---

### 7.2 撤销指定设备会话

- **请求路径**：`/api/v1/user/sessions/{sessionId}`
- **请求方法**：`DELETE`
- **接口描述**：将指定设备强制下线，撤销其会话。不能撤销当前设备自身的会话（请使用退出登录接口）。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话 ID |

#### 请求示例

```
DELETE /api/v1/user/sessions/2
Authorization: Bearer <access_token>
```

#### 响应示例

```json
{
  "code": 200,
  "message": "设备已下线",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 撤销成功 |
| 400 | 不能撤销当前设备 | 请使用退出登录接口 |
| 401 | 未授权 | Token 无效或已过期 |
| 404 | 会话不存在 | sessionId 不正确或已过期 |

---

## 8. API Key 管理接口

> 以下接口均需携带 `Authorization` 头。

### 8.1 新增 API Key

- **请求路径**：`/api/v1/user/api-keys`
- **请求方法**：`POST`
- **接口描述**：用户添加自有的第三方 AI 服务 API Key。Key 在服务端使用 AES-256-GCM 加密存储，仅返回脱敏后的前缀。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyAlias | String | 否 | 识别名称，如"我的 OpenAI Key" |
| provider | String | 是 | 服务商：`openai` / `azure` / `claude` 等 |
| apiKey | String | 是 | API Key 原文 |

#### 请求示例

```json
{
  "keyAlias": "我的 OpenAI Key",
  "provider": "openai",
  "apiKey": "sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "keyAlias": "我的 OpenAI Key",
    "provider": "openai",
    "maskedKey": "sk-...xxxx",
    "isEnabled": false,
    "isValid": true,
    "lastCheckedAt": null,
    "createdAt": "2026-05-11T09:14:00"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 添加成功 |
| 400 | Key 格式验证失败 | 服务商不支持的 Key 格式 |
| 400 | 超出数量上限 | 最多允许添加 10 个 API Key |

---

### 8.2 获取 API Key 列表

- **请求路径**：`/api/v1/user/api-keys`
- **请求方法**：`GET`
- **接口描述**：获取当前用户的所有 API Key 列表。仅返回脱敏信息，不返回明文 Key。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| provider | String | 否 | 按服务商筛选 |

#### 请求示例

```
GET /api/v1/user/api-keys?provider=openai
Authorization: Bearer <access_token>
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| apiKeys | Array | API Key 列表 |
| total | int | 总数 |

每个 API Key 对象为 `ApiKeyVO`（见 2.4 节）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "apiKeys": [
      {
        "id": 1,
        "keyAlias": "我的 OpenAI Key",
        "provider": "openai",
        "maskedKey": "sk-...xxxx",
        "isEnabled": true,
        "isValid": true,
        "lastCheckedAt": "2026-05-11T09:00:00",
        "createdAt": "2026-05-10T10:00:00"
      }
    ],
    "total": 1
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 查询成功 |
| 401 | 未授权 | Token 无效或已过期 |

---

### 8.3 更新 API Key

- **请求路径**：`/api/v1/user/api-keys/{keyId}`
- **请求方法**：`PUT`
- **接口描述**：更新指定 API Key 的别名、密钥值、启用状态。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyId | Long | 是 | API Key ID |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyAlias | String | 否 | 新的识别名称 |
| apiKey | String | 否 | 新的 Key 值（如需更换） |
| isEnabled | Boolean | 否 | 是否启用该 Key |

#### 请求示例

```json
{
  "keyAlias": "生产环境 Key",
  "isEnabled": true
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "keyAlias": "生产环境 Key",
    "provider": "openai",
    "maskedKey": "sk-...xxxx",
    "isEnabled": true,
    "isValid": true,
    "updatedAt": "2026-05-11T09:14:00"
  }
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 更新成功 |
| 401 | 未授权 | Token 无效或已过期 |
| 404 | Key 不存在 | keyId 不正确 |

---

### 8.4 删除 API Key

- **请求路径**：`/api/v1/user/api-keys/{keyId}`
- **请求方法**：`DELETE`
- **接口描述**：删除指定的 API Key（物理删除）。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyId | Long | 是 | API Key ID |

#### 请求示例

```
DELETE /api/v1/user/api-keys/1
Authorization: Bearer <access_token>
```

#### 响应示例

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 状态码

| 状态码 | message | 说明 |
|--------|---------|------|
| 200 | success | 删除成功 |
| 401 | 未授权 | Token 无效或已过期 |
| 404 | Key 不存在 | keyId 不正确 |

---

## 9. 附录

### 9.1 全局业务状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误（参数缺失、格式错误等） |
| 401 | 未授权（Token 无效、过期或未提供） |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如账号已存在、已被绑定等） |
| 422 | 业务校验不通过（如密码强度不足） |
| 423 | 资源被锁定（如账号被禁用） |
| 429 | 请求过于频繁（限流） |
| 500 | 服务器内部错误 |

### 9.2 验证码场景对照表

| 场景值 | 触发条件 | 说明 |
|--------|---------|------|
| `register` | 新用户注册 | 验证手机号/邮箱归属 |
| `login` | 验证码登录 | 无需密码直接登录 |
| `trust_device` | 新设备首次登录 | 验证设备可信 |
| `reset_password` | 忘记密码 | 重置密码前验证身份 |
| `bind` | 绑定新手机号/邮箱 | 验证新联系方式归属 |

### 9.3 安全规范说明

| 安全措施 | 说明 |
|---------|------|
| **密码存储** | 使用 Argon2id 算法哈希存储，不保存明文 |
| **敏感字段加密** | 手机号、邮箱、第三方 openid、API Key 使用 AES-256-GCM 加密 |
| **Refresh Token 存储** | 数据库中以 SHA-256 哈希形式存储 |
| **Token 轮换** | 每次刷新 Token，旧 Refresh Token 立即作废 |
| **日志脱敏** | 禁止记录明文密钥和令牌，日志中以 `***` 替换敏感字段 |
| **传输安全** | 全站 HTTPS，API 接口强制 TLS 1.3 |
| **限流保护** | 验证码接口限制每分钟 1 次，登录接口限制每小时 5 次失败 |

### 9.4 接口总览表

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 验证码 | POST | `/auth/code` | 发送验证码 |
| 注册 | POST | `/auth/register` | 用户注册 |
| 登录 | POST | `/auth/login/password` | 密码登录 |
| 登录 | POST | `/auth/login/code` | 验证码登录 |
| Token | POST | `/auth/token/refresh` | 刷新 Access Token |
| 退出 | POST | `/auth/logout` | 退出登录 |
| 密码 | POST | `/auth/password/reset` | 重置密码 |
| 用户 | GET | `/user/me` | 获取用户信息 |
| 用户 | PUT | `/user/me` | 更新用户信息 |
| 用户 | DELETE | `/user/me` | 注销账号 |
| 密码 | PUT | `/user/password` | 修改密码 |
| 身份 | GET | `/user/identities` | 获取绑定列表 |
| 身份 | POST | `/user/identities` | 绑定第三方账号 |
| 身份 | POST | `/user/identities/bind` | 绑定手机号/邮箱 |
| 身份 | DELETE | `/user/identities/{type}` | 解绑第三方账号 |
| 会话 | GET | `/user/sessions` | 获取设备列表 |
| 会话 | DELETE | `/user/sessions/{id}` | 撤销设备会话 |
| API Key | GET | `/user/api-keys` | 获取 API Key 列表 |
| API Key | POST | `/user/api-keys` | 新增 API Key |
| API Key | PUT | `/user/api-keys/{id}` | 更新 API Key |
| API Key | DELETE | `/user/api-keys/{id}` | 删除 API Key |

---

> 文档版本：V1.0  
> 最后更新：2026-05-11  
> 对应数据库设计：`数据库.md`（5张用户核心表）
