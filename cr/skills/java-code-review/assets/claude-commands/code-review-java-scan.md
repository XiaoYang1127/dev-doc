---
description: Full Java backend project scan for architecture, boundaries, coupling, layering, APIs, performance, security, and Alibaba Java rules.
argument-hint: "[blank | directory | module | package]"
allowed-tools: Bash(git:*), Bash(rg:*), Bash(find:*), Bash(sed:*), Bash(cat:*), Bash(mvn:*), Bash(./mvnw:*), Bash(gradle:*), Bash(./gradlew:*)
---

<!-- @format -->

# Java Full Scan

> 全量巡检回答一个问题：**这个 Java 后端项目当前有哪些系统性风险？**  
> 它不是 PR 阻塞审核；重点输出健康度、风险清单和治理优先级。

**Input**: `$ARGUMENTS`

---

## Review Scope

| 输入 | 范围 |
| --- | --- |
| 空 | 当前 Java 项目 |
| 目录 | 指定目录 |
| 模块名 | 指定 Maven/Gradle module |
| 包路径 | 指定 package 下代码 |

如果目标路径不是 Java 项目或不包含 Java 源码，停止并说明原因。

---

## Phase 1 — Inventory

收集项目画像：

```bash
find . -maxdepth 3 -name pom.xml -o -name build.gradle -o -name build.gradle.kts -o -name settings.gradle -o -name settings.gradle.kts
rg --files | rg 'src/(main|test)/(java|resources)|pom.xml|build.gradle|application.*\.(yml|yaml|properties)$'
```

识别：

- 构建工具：Maven / Gradle。
- 技术栈：Spring MVC、Spring Boot、MyBatis、JPA、RPC、MQ、Redis、Scheduler。
- 模块结构：单体、多模块、微服务、DDD、传统三层。
- 分层约定：controller、api/facade、application/service、domain、repository/mapper、infrastructure、dto/entity/converter。

---

## Phase 2 — Rules And Config

读取目标项目规则和配置：

- `.code-review.yml`
- `.code-review/rules/java/**/*.md`
- `.claude/rules/java/**/*.md`
- `CLAUDE.md`
- `README.md`
- `CONTRIBUTING.md`

`.code-review.yml` 按最小配置规范解释，支持项目架构、分层包名、禁止依赖、忽略路径、规则严重级别覆盖、验证命令和报告输出配置。若该命令来自 `java-code-review` Skill，配置规范见 Skill 的 `references/config/code-review-yml.md`。

如果没有规则文件，使用内置维度：

- 架构设计
- 服务边界
- 服务耦合
- 模块分层设计
- 接口设计
- 代码性能
- 安全问题
- 阿里巴巴 Java 开发手册强制项

---

## Phase 3 — Scan Dimensions

### 架构设计

- 模块职责是否清晰。
- 是否存在跨领域散落修改和公共模块膨胀。
- 是否通过策略、事件、领域服务承载扩展。
- 状态流转、事务、幂等、补偿是否有统一所有者。

### 服务边界

- 是否直接依赖其他模块 `impl`、`mapper`、`repository`、`entity`。
- Entity/PO/DO 是否泄漏到 HTTP、RPC、MQ 契约。
- 事务内是否调用外部服务、MQ、HTTP、文件 IO。
- 是否直接读写其他服务拥有的表、Redis key 或内部配置。

### 服务耦合

- 循环依赖、双向调用、共享可变状态。
- 静态变量、ThreadLocal、全局缓存传递业务上下文。
- 跨模块复制枚举、状态码、状态机、表结构知识。

### 模块分层设计

- Controller 是否只做协议适配和参数校验。
- Service/Application 是否承载业务编排和事务。
- Domain 是否承载领域规则。
- Repository/Mapper 是否只做数据访问。
- DTO、Entity、Request、Response 是否混用。
- Converter/Assembler 是否集中转换。

### 接口设计

- URL、HTTP method、版本策略、错误码、统一响应。
- Bean Validation、分页排序边界、过滤字段白名单。
- RPC/MQ DTO 版本、幂等 key、traceId、业务主键。
- 公开契约是否向后兼容。

### 代码性能

- N+1 查询、循环 IO、大结果集、内存分页。
- SQL `SELECT *`、无稳定排序分页、索引字段函数运算、隐式类型转换。
- 缓存穿透、击穿、雪崩和脏读风险。
- 长事务、大事务、锁粒度、线程池边界。

### 安全问题

- 鉴权、授权、租户隔离、IDOR。
- SQL 注入、SSRF、路径穿越、XSS、反序列化、SpEL/OGNL 注入。
- 敏感日志、密钥泄漏、Actuator 暴露、CORS 过宽。
- MyBatis `${}`、用户输入拼接 SQL/URL/路径/命令。

---

## Confidence

每条 finding 必须包含置信度：

| Confidence | 判定标准 |
| --- | --- |
| High | 有明确代码行证据，可从当前文件和直接上下文确认 |
| Medium | 有代码信号，但需要业务约束、运行环境或上下游契约确认 |
| Low | 只是风险信号，缺少完整上下文 |

全量巡检允许输出 Medium/Low 置信度风险，但必须和 High 置信度缺陷分开。

---

## Ignore Rules

支持行级忽略：

```java
// review-ignore JAVA-PERF-003 reason: 最大结果集受上游固定限制，最多 20 条
```

支持 `.code-review.yml`：

```yaml
version: 1

project:
  language: java
  architecture:
    style: layered
    layers:
      controller: "..controller..|..web.."
      service: "..service..|..application.."
      repository: "..repository..|..mapper..|..dao.."

rules:
  ignorePaths:
    - "src/test/**"
    - "generated/**"
  severityOverrides:
    JAVA-API-002: Medium

report:
  formats:
    - markdown
    - json
```

兼容旧的简写：

```yaml
ignoreRules:
  - JAVA-NAME-001

ignorePaths:
  - "src/test/**"
  - "generated/**"

ruleSeverity:
  JAVA-API-002: Medium
```

---

## Phase 4 — Validate

根据项目规模选择成本可控的验证：

- 编译：`./mvnw -q -DskipTests compile` 或 `./gradlew compileJava`
- 测试：相关模块测试优先，全量测试视项目规模决定。
- 静态检查：仅运行项目已有任务，例如 Checkstyle、PMD、SpotBugs、ArchUnit。

不要为了巡检修改项目配置。

---

## Report Format

必须输出两种格式：

- Markdown：给人阅读，突出健康度、Top Risks 和治理顺序。
- JSON：给 CI、Bot、趋势统计和误报分析使用。

JSON 格式遵循 `java-code-review` Skill 的 `references/report/json-report.md`。如果写文件，默认路径：

```text
.code-review/reports/java-full-scan.json
```

如果不写文件，最终回复中必须包含同结构 JSON 摘要。

```markdown
## Java Full Scan

扫描范围：
- <project/module/package/path>

健康度结论：
- Overall: Good | Moderate Risk | High Risk
- Critical: 0
- High: 0
- Medium: 0
- Low: 0

## Top Risks

1. <最值得优先治理的问题>

## Findings

### Critical

None

### High

1. <标题>

- 规则：JAVA-ARCH-001
- 置信度：High
- 位置：`path/to/File.java:123`
- 维度：架构设计
- 证据：<最小必要代码证据>
- 影响：<系统性风险>
- 建议：<治理方案>

### Medium

None

### Low

None

## Architecture Notes

- <项目画像、分层约定、模块边界观察>

## Validation Results

| Check | Result |
| --- | --- |
| Compile | Pass / Fail / Skipped |
| Tests | Pass / Fail / Skipped |
| Static checks | Pass / Fail / Skipped |

## Recommended Next Steps

1. <优先级最高的治理动作>
2. <第二优先级>
3. <第三优先级>
```

JSON 最小结构：

```json
{
  "schemaVersion": "1.0",
  "tool": "java-code-review",
  "mode": "scan",
  "target": {
    "type": "project",
    "scope": []
  },
  "decision": "NOT_APPLICABLE",
  "summary": {
    "critical": 0,
    "high": 0,
    "medium": 0,
    "low": 0,
    "ignored": 0,
    "needsConfirmation": 0
  },
  "health": {
    "overall": "Good",
    "topRisks": []
  },
  "findings": [],
  "contextRisks": [],
  "confirmations": [],
  "validation": [],
  "filesReviewed": [],
  "metadata": {}
}
```
