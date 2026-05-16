# QQ 邮箱 SMTP 与验证码

本服务在 **`spring.mail.host`、`username`、`password` 均已配置** 且发码账号为 **邮箱** 时，会通过 SMTP 发送 **注册 / 找回密码** 的 **6 位数字验证码** 邮件。**注册发码仅支持邮箱**；找回密码仍支持已绑定的手机或邮箱（手机发码仍为进程内演示，未接短信网关）。

## 1. 在 QQ 邮箱开通 SMTP

1. 浏览器打开 [QQ 邮箱](https://mail.qq.com)，登录。
2. **设置** → **账户**。
3. 找到 **POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV 服务**，开启 **SMTP 服务**（若提示先开 IMAP，按页面指引操作）。
4. 按提示用密保手机验证后，会得到 **授权码**（一串字母，**不是** QQ 登录密码）。请妥善保存，填入应用配置中的 `spring.mail.password`。

## 2. Spring Boot 配置示例

复制 `src/main/resources/application-dev.yaml.example` 为 `application-dev.yaml`，把 `username` / `password` 改成你的 QQ 邮箱与授权码；或在本机 `application-dev.yaml` 中保留如下结构（端口 **465** + SSL 常见且稳定）：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: 你的QQ号@qq.com
    password: 上面生成的授权码
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
```

也可使用 **587** + STARTTLS（将 `port` 改为 `587`，并配置 `spring.mail.properties.mail.smtp.starttls.enable=true` 等，以 Spring Mail 文档为准）。

## 3. 联调说明

- 未配置 `spring.mail` 时：邮箱账号与手机号一样，验证码只在 **服务端内存**；开发可把 `app.auth.verification-debug-return-code` 设为 `true`，发码接口会返回 `debugCode`。
- 配置错误导致发送失败：接口返回 **503**，错误码 `SERVICE_UNAVAILABLE`，本次发码会回滚（不会占用 60 秒频控条目的有效码，因存储已清除）。

## 4. 生产注意

- 生产环境务必将 `app.auth.verification-debug-return-code` 设为 **false**。
- 验证码长期仍建议迁到 **Redis** 等中心化存储；当前为单机内存，重启即失效。
