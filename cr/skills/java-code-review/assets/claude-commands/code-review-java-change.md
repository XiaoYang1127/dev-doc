---
description: Java backend change review for local diffs, Git refs, or GitHub PRs.
argument-hint: "[blank | git-ref | pr-number | pr-url | --pr <number/url>]"
allowed-tools: Bash(git:*), Bash(gh:*), Bash(rg:*), Bash(find:*), Bash(sed:*), Bash(cat:*), Bash(mvn:*), Bash(./mvnw:*), Bash(gradle:*), Bash(./gradlew:*)
---

<!-- @format -->

# Java Change Review

> 变更审核只回答一个问题：**这次 Java 后端改动能不能合并？**  
> 只阻塞本次变更新增、修改或明显放大的问题；历史问题放入“上下文风险”或“需要确认”。

**Input**: `$ARGUMENTS`

---

## Review Scope

根据输入选择审核范围：

| 输入 | 范围 |
| --- | --- |
| 空 | 当前工作区未提交变更：`git diff HEAD` |
| Git ref，例如 `main`、`origin/main`、`HEAD~1` | `git diff <ref>...HEAD` |
| PR number、PR URL、`--pr` | GitHub PR diff |

如果没有变更，停止并输出 `Nothing to review.`。

如果当前目录不是 Java 项目，停止并说明需要在目标 Java 项目根目录执行。

Java 项目信号：

- `pom.xml`
- `build.gradle` / `build.gradle.kts`
- `settings.gradle` / `settings.gradle.kts`
- `src/main/java`
- `src/test/java`

---

## Phase 1 — Gather

本地变更：

```bash
git status --short
git diff --name-status HEAD
git diff --stat HEAD
git diff HEAD
```

Git ref：

```bash
git diff --name-status <ref>...HEAD
git diff --stat <ref>...HEAD
git diff <ref>...HEAD
```

PR：

```bash
gh pr view <NUMBER> --json number,title,body,author,baseRefName,headRefName,headRefOid,isDraft,changedFiles,additions,deletions,url
gh pr diff <NUMBER> --name-only
gh pr diff <NUMBER>
```

如果 `gh` 不可用，退化为本地 diff 审核，并明确说明无法发布 GitHub review。

---

## Phase 2 — Context

必须读取目标项目上下文：

- `CLAUDE.md`
- `README.md`
- `CONTRIBUTING.md`
- `.code-review.yml`
- `.code-review/rules/java/**/*.md`
- `pom.xml`
- `build.gradle*`
- `settings.gradle*`
- `src/main/resources/application*.yml`
- `src/main/resources/application*.yaml`
- `src/main/resources/application*.properties`

`.code-review.yml` 按最小配置规范解释，支持项目架构、忽略路径、规则严重级别覆盖、验证命令和报告输出配置。若该命令来自 `java-code-review` Skill，配置规范见 Skill 的 `references/config/code-review-yml.md`。

对每个变更 Java 文件，必须读取：

- 完整类、接口、枚举或配置类
- 变更方法的 imports、注解、字段、构造器
- 同 feature/module 下的 Controller、API/Facade、Service、Domain、Repository/Mapper、DTO、Entity、Converter
- 直接调用方和被调用方
- 相关 Mapper XML、SQL、配置、消息 topic、缓存 key 定义
- 相关测试文件

---

## Phase 3 — Lightweight Rule Matching

优先按规则卡片输出问题。若该命令来自 `java-code-review` Skill，规则卡片格式见 Skill 的 `references/rules/rule-format.md`。

如果目标项目没有规则卡片，使用以下内置维度：

- 架构设计：业务能力聚合、扩展点复用、事务/幂等/补偿边界。
- 服务边界：跨模块契约、数据所有权、Entity/PO/DO 泄漏、事务内远程调用。
- 服务耦合：循环依赖、共享可变状态、硬编码对方状态机/表结构/缓存 key。
- 模块分层设计：Controller、API/Facade、Service/Application、Domain、Repository/Mapper、DTO/Entity/Converter 职责。
- 接口设计：版本、HTTP method、Request/Response、Bean Validation、分页排序边界、兼容性。
- 代码性能：N+1、循环 IO、长事务、大结果集、缓存风险、锁粒度、线程池边界。
- 安全问题：鉴权、授权、越权、SQL 注入、SSRF、路径穿越、XSS、反序列化、敏感日志、密钥泄漏。
- 阿里巴巴 Java 开发手册强制项：线程池、ThreadLocal、集合修改、BigDecimal、异常日志、MyBatis SQL、数据库索引。

---

## Confidence

每条 finding 必须包含置信度：

| Confidence | 判定标准 | 是否可阻塞 |
| --- | --- | --- |
| High | 有明确代码行证据，可从当前文件和直接上下文确认，不依赖业务假设 | Critical/High 可阻塞 |
| Medium | 有代码信号，但需要部分业务约束、运行环境或上下游契约确认 | 不直接阻塞，除非风险极高 |
| Low | 只是风险信号，缺少完整上下文 | 不阻塞，放入“需要确认” |

只有 `High confidence` 的 `Critical` 或 `High` 问题才能作为合并阻塞理由。

---

## Ignore Rules

支持行级忽略：

```java
// review-ignore JAVA-SEC-001 reason: orderBy 字段来自固定枚举白名单
```

要求：

- 必须包含规则 ID。
- 必须包含 `reason:`。
- 只对相邻代码块生效。
- 没有 reason 的 ignore 不生效。
- Critical 安全问题即使有 ignore，也必须放入“需要确认”。

支持 `.code-review.yml`：

```yaml
version: 1

project:
  language: java
  architecture:
    style: layered

rules:
  ignorePaths:
    - "src/test/**"
    - "generated/**"
  ignoreRules:
    - JAVA-NAME-001
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

只运行适用且成本可控的命令。优先相关模块，避免无意义全量慢跑。

Maven：

```bash
./mvnw -q -DskipTests compile
./mvnw test
```

如果没有 wrapper：

```bash
mvn -q -DskipTests compile
mvn test
```

Gradle：

```bash
./gradlew compileJava
./gradlew test
```

如果没有 wrapper：

```bash
gradle compileJava
gradle test
```

如果项目已有 Checkstyle、PMD、SpotBugs、Error Prone、ArchUnit、JaCoCo、Sonar 本地任务，按项目约定运行。不要为了审核临时改项目配置。

---

## Decision

| 条件 | Decision |
| --- | --- |
| 存在 High confidence Critical | BLOCK |
| 存在 High confidence High，或关键验证失败 | REQUEST CHANGES |
| 只有 Medium/Low，或只有 Medium/Low confidence | APPROVE WITH COMMENTS |
| 无问题 | APPROVE |
| Draft PR | COMMENT |

---

## Report Format

必须输出两种格式：

- Markdown：给人阅读，问题优先。
- JSON：给 CI、Bot、趋势统计和误报分析使用。

JSON 格式遵循 `java-code-review` Skill 的 `references/report/json-report.md`。如果写文件，默认路径：

```text
.code-review/reports/java-change-review.json
```

如果不写文件，最终回复中必须包含同结构 JSON 摘要。

```markdown
## Java Change Review

审核对象：
- <local diff | ref diff | PR>

结论：APPROVE | APPROVE WITH COMMENTS | REQUEST CHANGES | BLOCK | COMMENT

问题统计：
- Critical: 0
- High: 0
- Medium: 0
- Low: 0

## Findings

### Critical

None

### High

1. <标题>

- 规则：JAVA-SEC-001
- 置信度：High
- 位置：`path/to/File.java:123`
- 维度：安全问题
- 证据：<最小必要代码证据或 diff 描述>
- 影响：<线上、数据、安全或维护风险>
- 建议：<可落地修复方案>

### Medium

None

### Low

None

## 上下文风险

- <历史问题、非本次变更新增但与变更相关的风险>

## 需要确认

- <低置信度问题或无法从代码确认的业务约束>

## Validation Results

| Check | Result |
| --- | --- |
| Compile | Pass / Fail / Skipped |
| Tests | Pass / Fail / Skipped |
| Static checks | Pass / Fail / Skipped |

## Files Reviewed

- `path/to/File.java` - Modified
```

JSON 最小结构：

```json
{
  "schemaVersion": "1.0",
  "tool": "java-code-review",
  "mode": "change",
  "target": {
    "type": "local-diff",
    "baseRef": "HEAD",
    "headRef": "working-tree",
    "scope": []
  },
  "decision": "APPROVE_WITH_COMMENTS",
  "summary": {
    "critical": 0,
    "high": 0,
    "medium": 0,
    "low": 0,
    "ignored": 0,
    "needsConfirmation": 0
  },
  "findings": [],
  "contextRisks": [],
  "confirmations": [],
  "validation": [],
  "filesReviewed": [],
  "metadata": {}
}
```
