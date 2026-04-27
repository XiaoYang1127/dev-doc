---
description: Java code review entry. Prefer /code-review-java-change for changed code and /code-review-java-scan for full project scan.
argument-hint: '[change | scan | arguments passed to selected mode]'
allowed-tools: Bash(git:*), Bash(gh:*), Bash(rg:*), Bash(find:*), Bash(sed:*), Bash(cat:*), Bash(mvn:*), Bash(./mvnw:*), Bash(gradle:*), Bash(./gradlew:*)
---

<!-- @format -->

# Java Code Review

这是 Java 后端代码审核入口命令。它不直接定义完整审核流程，优先引导使用两个更明确的命令：

- `/code-review-java-change`：变更审核，回答“这次改动能不能合并？”
- `/code-review-java-scan`：全量巡检，回答“这个 Java 项目有哪些系统性风险？”

**Input**: `$ARGUMENTS`

---

## Mode Selection

如果 `$ARGUMENTS` 包含 `scan`、`full`、`full-scan`：

→ 使用 `/code-review-java-scan` 的流程。

否则：

→ 使用 `/code-review-java-change` 的流程。

---

## Shared Principles

- 审核对象是用户传入或当前目录下的 **Java 后端项目代码**，不是保存本命令的文档仓库。
- 参考《阿里巴巴 Java 开发手册》和项目自身约定。
- 优先报告有明确代码证据的问题。
- 每条问题输出规则 ID、严重级别、置信度、文件行号、影响和建议。
- 支持 `.code-review.yml` 最小配置规范；若该命令来自 `java-code-review` Skill，详见 `references/config/code-review-yml.md`。
- 支持 Markdown + JSON 双格式报告；若该命令来自 `java-code-review` Skill，JSON 格式见 `references/report/json-report.md`。
- 变更审核只阻塞本次变更新增或放大的问题。
- 全量巡检报告项目健康风险，不默认阻塞 PR/MR。

---

## Lightweight Rule System

审核时优先读取目标项目中的规则卡片：

- `.code-review/rules/java/**/*.md`
- `.claude/rules/java/**/*.md`

如果不存在规则卡片，使用命令内置的 Java 审核维度：

- 架构设计
- 服务边界
- 服务耦合
- 模块分层设计
- 接口设计
- 代码性能
- 安全问题
- 阿里巴巴 Java 开发手册强制项

---

## Lightweight False Positive Control

支持行级忽略：

```java
// review-ignore JAVA-SEC-001 reason: orderBy 字段来自固定枚举白名单
```

规则：

- 必须包含规则 ID。
- 必须包含 `reason:`。
- 忽略只对相邻代码块生效。
- 没有 reason 的 ignore 不生效。
- Critical 安全问题即使有 ignore，也必须放入“需要确认”。

支持项目级配置：

```yaml
# .code-review.yml
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

---

## Output

最终输出必须说明实际采用的模式：

```text
Java Code Review mode: change | scan
Decision: <...>
Issues: <critical> critical, <high> high, <medium> medium, <low> low
```
