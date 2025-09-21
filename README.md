# Moni

Moni 是一个基于 Spring Boot 的个人财务助手，利用 Spring AI 统一接入多个大语言模型，完成消费记录的自动解析、分析与对账提醒。服务支持文本与票据图片两种输入形式，可将解析后的结果存入嵌入式 SQLite 数据库，并按计划任务生成邮件报告。

## 主要特性
- **多模型智能体编排**：借助 Spring AI 与自定义 Agent（`agent` 包），完成意图识别、票据解析、Markdown 转 HTML 等任务。
- **消费流水自动入库**：`/ai` 接口会判断用户意图并调用 `ExpenseAgent` 写入 `expense` 表，同时将原始请求记录到 `recon` 表以便对账。
- **票据图片 OCR**：`/file` 接口通过 `ExtractAgent` 提取图片中的金额、时间、商家等关键信息。
- **定时任务通知**：`JobService` 每日定时汇总近 2 日消费分析、回填失败对账并发送邮件提醒（`Asia/Shanghai` 时区）。
- **邮件与报告生成**：分析结果以 Markdown 输出，经 `HtmlAgent` 转换成邮件友好的 HTML 模板后发送。
- **可移植的本地存储**：使用 SQLite (`expense.db`) 与自增序列文件 (`id_sequence.dat`)，便于单机快速体验。

## 项目结构
```
├── pom.xml
├── sql/db.sql              # 数据库结构
├── src
│   ├── main/java/com/longjunwang/moni
│   │   ├── MoniApplication.java
│   │   ├── agent/           # LLM 智能体封装
│   │   ├── config/          # ChatClient 与数据源配置
│   │   ├── controller/      # REST 接口
│   │   ├── entity/          # 数据实体
│   │   ├── mapper/          # MyBatis Mapper 接口
│   │   ├── service/         # 业务服务 & 定时任务
│   │   └── util/            # 辅助工具（如 ID 生成器）
│   └── main/resources
│       ├── application.yml          # 默认配置（生产环境请覆盖敏感信息）
│       ├── application-*.yml        # 环境差异配置
│       ├── mapper/                  # MyBatis XML 映射
│       └── prompts/                 # 智能体 Prompt 模板
└── images/                          # 票据图片存储目录（可自定义路径）
```

## 环境准备
- Java 21
- Maven 3.9+
- 可访问的 LLM 兼容 OpenAI API 协议的推理服务
- 邮件 SMTP 账号（用于发送分析通知）

> 建议将敏感信息放入环境变量或 `application-local.yml` 等忽略文件，避免修改默认 `application.yml` 并提交到版本库。

### 必需的环境变量 / 配置项
以下键值默认为空，可通过环境变量或自定义 `application-*.yml` 提供：

| Key | 说明 |
| --- | --- |
| `MONI_API_KEY` | Spring AI ChatClient 使用的统一 API Key |
| `MONI_BASE_URL` | 兼容 OpenAI API 的网关地址 |
| `spring.ai.openai.api-key` | 当直接使用 Spring 配置时的备用 Key（推荐留空，依赖环境变量） |
| `spring.datasource.url` | SQLite 路径，例如 `jdbc:sqlite:/path/to/expense.db` |
| `spring.mail.username` / `spring.mail.password` | SMTP 凭证，用于发送邮件 |
| `moni.image.path` | 票据图片保存目录（默认指向仓库下的 `images/`） |

可将 `src/main/resources/application-github.yml` 复制为 `application-local.yml` 并填写本地值：

```bash
cp src/main/resources/application-github.yml src/main/resources/application-local.yml
# 编辑 application-local.yml 写入本地配置，启动时通过 --spring.profiles.active=local 载入
```

## 初始化数据库
项目自带的 `DataSourceConfig` 会在首次运行时自动创建 SQLite 文件并建表。若需要手工初始化或重建数据库，可执行：

```bash
sqlite3 expense.db < sql/db.sql
```

表结构概览：
- `expense`：主键 `id`（形如 `EXyyyymmddNNNN`），记录日期、金额、类型、备注等。
- `recon`：对账任务状态表，保存原始请求、状态与时间戳。

> 重置 `expense.db` 后，请同步删除 `id_sequence.dat` 以避免 ID 冲突。

## 本地运行
```bash
mvn spring-boot:run
```
应用默认监听端口 `7634`，启用定时任务并加载所有 Agent。

若需要以不同 profile 启动（例如 `local`）：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

打包可执行 JAR：
```bash
mvn clean package
java -jar target/moni-0.0.1-SNAPSHOT.jar
```

## API 说明
### 1. 消息入口 `/ai`
根据文本内容自动归类意图并处理。

```bash
curl -X POST http://localhost:7634/ai \
  -H 'Content-Type: application/json' \
  -d '{
        "content": "3月5日早餐花了18元在星巴克",
        "isFile": false
      }'
```

- 当意图为 `INSERT` 时，返回由 `ExpenseAgent` 解析后的 JSON 或错误提示。
- 当意图为 `ANALYZE` 时，系统会生成 Markdown 报告、转换成 HTML 并邮件发送，同时将 HTML 响应返回客户端。
- 其他内容会返回默认提示：当前不处理与消费无关的信息。

### 2. 票据上传 `/file`
上传图片后返回在磁盘上的保存路径，可配合 `/ai` 再次提交以触发识别。

```bash
curl -X POST http://localhost:7634/file \
  -F 'file=@/path/to/receipt.jpg'
```

错误时返回 500，并附带中文错误信息。

## 定时任务
`JobService`
- `analyseTask`：每日 06:00 (Asia/Shanghai) 触发，使用 `AnalyseAgent` 分析最近两日消费并发送 HTML 邮件。
- `cronTask`：每日 01:00 触发，重新插入 `recon` 状态为 `INIT` 的记录，统计成功/失败数并邮件通知。

如需关闭定时任务，可在自定义 profile 中移除 `@EnableScheduling` 或调整 Cron 表达式。

## 智能体与 Prompt
- `ClassifyIntentAgent`：使用 `prompts/intent.st` 识别用户意图。
- `ExpenseAgent`：基于 `prompts/expense.st` 解析消费明细并调用 MyBatis Mapper 写入。
- `AnalyseAgent`：通过 `prompts/analyse.st` 生成 Markdown 报告，并可调用工具方法查询数据库。
- `HtmlAgent`：将 Markdown 报告套用 `prompts/html.st` 转为 HTML。
- `ExtractAgent`：对票据图片提取文本，为 `/ai` 消费流程提供原始内容。

所有 Agent 持有独立的 `ChatClient` Bean，可通过环境变量切换模型（OpenAI、Claude、DeepSeek、Gemini 等）。

## 测试与验证
运行单元与集成测试：
```bash
mvn clean test
```

建议在提交前自查：
1. `mvn -U clean test`
2. 通过 `/ai` 与 `/file` 进行一次端到端测试
3. 检查邮件发送日志，确认 SMTP 配置生效

## 其他注意事项
- 真实 API Key、邮箱密码等敏感信息请改用环境变量传递，不要提交到 Git。
- 图片上传目录默认在仓库内，生产环境可改为共享存储并确保应用具有写权限。
- 若需要清理票据或重跑对账，只删除文件或数据库记录即可，任务会在下一次调度时自动追补。

如需更多贡献说明，请参见 `AGENTS.md` 中的仓库约定。
