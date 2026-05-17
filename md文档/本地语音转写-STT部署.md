# 本地 / 内网语音转写（STT）部署指南

后端 **`POST /api/v1/speech/transcribe`** 会把前端上传的音频转发到本机 STT HTTP 服务，默认约定与 **[whisper-asr-webservice](https://github.com/ahmetoner/whisper-asr-webservice)** 一致。

**原则**：STT 只监听内网（如 `127.0.0.1:9000`），不要对公网暴露；用户只访问 Java API（8000）。

---

## 1. 推荐方案：Docker + whisper-asr-webservice（CPU 可跑）

### 1.1 前置

- 已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)（Windows）或 Docker Engine（Linux）
- 磁盘约 **2～4GB**（镜像 + small 模型）
- 有 NVIDIA 显卡可选 GPU 镜像（更快）

### 1.2 启动（CPU，中文）

PowerShell：

```powershell
docker run -d --name whisper-asr `
  -p 127.0.0.1:9000:9000 `
  -e ASR_MODEL=small `
  -e ASR_ENGINE=openai_whisper `
  onerahmet/openai-whisper-asr-webservice:latest
```

说明：

- **`-p 127.0.0.1:9000:9000`**：仅本机可访问，与 `application.yaml` 中 `app.speech.base-url` 一致
- **`ASR_MODEL=small`**：体积与速度平衡；要更准确可改为 `medium`（更慢、更占内存）
- 首次启动会**下载模型**，需等待数分钟

### 1.3 验证 STT 是否就绪

浏览器打开：`http://127.0.0.1:9000/docs`（Swagger）。

或用 curl（将 `test.wav` 换成你的短中文录音）：

```bash
curl -X POST "http://127.0.0.1:9000/asr?encode=true&task=transcribe&language=zh&output=json" \
  -F "audio_file=@test.wav"
```

应返回 JSON，含 `"text":"..."` 字段。

### 1.4 配置 Java 后端（Docker 已启动后必做）

仓库**默认已写好** `application.yaml` 里的 `app.speech.*`，一般只需配置 **`.env`**（或 `application-dev.yaml`）。

#### A. 复制环境变量文件（推荐）

项目根目录 `E:\houduan\AI_GP`：

```powershell
copy .env.example .env
```

在 `.env` 中确认或填写（与 Whisper 容器、Java 端口一致）：

```env
PORT=8080
APP_PUBLIC_BASE_URL=http://localhost:8080
APP_SPEECH_ENABLED=true
APP_SPEECH_BASE_URL=http://127.0.0.1:9000
```

| 变量 | 含义 |
|------|------|
| `PORT` | Spring Boot 端口（示例 8080） |
| `APP_PUBLIC_BASE_URL` | 头像等对外链接前缀，**须与 PORT 一致** |
| `APP_SPEECH_BASE_URL` | 内网 STT 地址，与 `docker run -p 127.0.0.1:9000:9000` 一致 |

未建 `.env` 时：`application.yaml` 默认 Java **8000**、STT **9000**；若你用 8080，务必设 `PORT` 或复制 `application-dev.yaml.example` → `application-dev.yaml` 并改端口。

#### B. 可选：`application-dev.yaml`

```powershell
copy src\main\resources\application-dev.yaml.example src\main\resources\application-dev.yaml
```

示例里已含 `app.speech` 与 `public-base-url`，按本机改数据库密码、MiMo Key 即可。

#### C. 配置对照（无需改代码）

| 项 | 默认值 | 说明 |
|----|--------|------|
| `app.speech.enabled` | `true` | 关闭则接口 503 `SPEECH_DISABLED` |
| `app.speech.base-url` | `http://127.0.0.1:9000` | 仅本机 STT |
| `app.speech.transcribe-path` | `/asr` | whisper-asr-webservice |
| `app.speech.upstream-file-part-name` | `audio_file` | 转发字段名 |

### 1.5 启动 Java 并联调

1. 确认 Docker：`docker ps` 中 `whisper-asr` 为 **Up**
2. 在 IDE 或项目根目录启动 Spring Boot（`mvn spring-boot:run` 或运行 `DemoApplication`）
3. 浏览器可访问：`http://localhost:8080/swagger-ui.html`（端口随 `PORT`）
4. 登录拿 Token 后：

```bash
curl -X POST "http://localhost:8080/api/v1/speech/transcribe" \
  -H "Authorization: Bearer TOKEN" \
  -F "file=@test.wav"
```

（若 `PORT=8000` 则把 `8080` 改成 `8000`。）

---

## 2. GPU 加速（可选）

机器有 NVIDIA 驱动 + Container Toolkit 时，可使用项目文档中的 **cuda** 标签镜像（见 whisper-asr-webservice 官方 README），将 `ASR_ENGINE` / 镜像 tag 换为 GPU 版本，端口映射仍为 `127.0.0.1:9000:9000`。

---

## 3. 与 Java 配置的对应关系

| whisper-asr-webservice | Java `app.speech` |
|------------------------|-------------------|
| `POST /asr` | `transcribe-path: /asr` |
| 表单字段 `audio_file` | `upstream-file-part-name: audio_file` |
| `language=zh` | `query-language: zh` |
| `output=json` | `query-output: json` |
| `encode=true` | `query-encode: true` |
| `task=transcribe` | `query-task: transcribe` |

若改用**其他 STT**（字段名、路径不同），只改 `application.yaml` 或环境变量，**无需改前端**。

---

## 4. 常见问题

| 现象 | 处理 |
|------|------|
| Java 返回 `SPEECH_UNAVAILABLE` | 确认容器在跑：`docker ps`；`curl http://127.0.0.1:9000/docs` |
| 识别很慢 | 换 `base` 模型或启用 GPU；缩短录音时长 |
| 浏览器 webm 失败 | 确认请求带 `encode=true`（已默认）；或前端录 wav |
| 9000 被占用 | 改 Docker 端口映射与 `APP_SPEECH_BASE_URL` 一致 |

---

## 5. 生产 / 内网服务器

- STT 与 Java **同机或同 VPC**：`base-url` 填内网 IP，如 `http://10.0.1.50:9000`
- 防火墙**禁止** 9000 对公网开放
- 资源：small 模型建议 **4GB+** 内存；并发高时需排队或水平扩容 STT 实例

---

## 6. 相关文档

- 接口字段：`md文档/HTTP接口-语音转写.md`
- OpenAPI：`docs/openapi/v1-speech.yaml`
- 环境变量：`md文档/环境与密钥配置.md` §3.4
