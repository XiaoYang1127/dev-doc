<!-- @format -->

# Java 代码审核规则卡片

这个目录用于沉淀轻量级 Java 后端代码审核规则。它不是规则引擎，先用 Markdown 规则卡片约束 AI 审核输出，后续再逐步自动化。

## 规则卡片格式

```markdown
## JAVA-SEC-001 MyBatis `${}` 拼接 SQL

Severity: Critical
Dimension: 安全问题
Scope: Mapper XML / MyBatis 注解 SQL

### 检测信号

- MyBatis XML 或注解 SQL 中出现 `${}`。
- `${}` 内容来自 HTTP、RPC、MQ、DB 或外部配置输入。

### 必须确认

- `${}` 是否只用于表名、字段名、排序方向等无法使用 `#{}` 的位置。
- 是否有固定枚举、白名单映射或强类型转换。

### 误报条件

- `${}` 值来自不可被用户控制的常量。
- 动态字段经过白名单映射，且没有透传原始输入。

### 建议修复

- 普通参数改为 `#{}`。
- 动态排序、字段名、表名使用白名单映射。
```

## 输出要求

命中规则时，finding 必须包含：

- 规则 ID
- 严重级别
- 置信度：High / Medium / Low
- 文件行号
- 证据
- 影响
- 建议

## 置信度

| Confidence | 判定标准 |
| --- | --- |
| High | 有明确代码行证据，可从当前文件和直接上下文确认 |
| Medium | 有代码信号，但需要业务约束、运行环境或上下游契约确认 |
| Low | 只是风险信号，缺少完整上下文 |

## 忽略机制

行级忽略：

```java
// review-ignore JAVA-SEC-001 reason: orderBy 字段来自固定枚举白名单
```

要求：

- 必须包含规则 ID。
- 必须包含 `reason:`。
- 只对相邻代码块生效。
- Critical 安全问题即使被忽略，也应进入“需要确认”。

项目级配置见 [`.code-review.yml` 配置规范](../config/code-review-yml.md)。最小示例：

```yaml
# .code-review.yml
version: 1

project:
  language: java
  architecture:
    style: layered

rules:
  ignoreRules:
    - JAVA-NAME-001
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

JSON 报告格式见 [JSON 报告格式](../report/json-report.md)。
