<!-- @format -->

# JSON 报告格式

Java 审核命令必须能输出 Markdown 和 JSON。Markdown 给人读，JSON 给 CI、Bot、趋势统计和误报分析使用。

## 输出位置

默认输出位置：

```text
.code-review/reports/java-change-review.json
.code-review/reports/java-full-scan.json
```

如果不写文件，必须在最终回复中提供同结构 JSON 摘要。

## 顶层结构

```json
{
  "schemaVersion": "1.0",
  "tool": "java-code-review",
  "mode": "change",
  "target": {
    "type": "local-diff",
    "baseRef": "HEAD",
    "headRef": "working-tree",
    "scope": ["src/main/java"]
  },
  "decision": "REQUEST_CHANGES",
  "summary": {
    "critical": 0,
    "high": 1,
    "medium": 2,
    "low": 0,
    "ignored": 1,
    "needsConfirmation": 1
  },
  "findings": [],
  "contextRisks": [],
  "confirmations": [],
  "validation": [],
  "filesReviewed": [],
  "metadata": {}
}
```

## 枚举值

### `mode`

- `change`
- `scan`

### `decision`

- `APPROVE`
- `APPROVE_WITH_COMMENTS`
- `REQUEST_CHANGES`
- `BLOCK`
- `COMMENT`
- `NOT_APPLICABLE`

### `severity`

- `Critical`
- `High`
- `Medium`
- `Low`

### `confidence`

- `High`
- `Medium`
- `Low`

### `dimension`

- `架构设计`
- `服务边界`
- `服务耦合`
- `模块分层设计`
- `接口设计`
- `代码性能`
- `安全问题`
- `阿里巴巴Java规约`

## Finding 结构

```json
{
  "id": "F-001",
  "ruleId": "JAVA-SEC-001",
  "title": "MyBatis 使用 ${} 拼接外部输入",
  "severity": "Critical",
  "confidence": "High",
  "dimension": "安全问题",
  "location": {
    "file": "src/main/resources/mapper/OrderMapper.xml",
    "line": 42,
    "symbol": "selectOrders"
  },
  "evidence": "orderBy 直接通过 ${orderBy} 拼接到 SQL",
  "impact": "攻击者可控制排序字段并注入 SQL 片段。",
  "recommendation": "将普通参数改为 #{}；排序字段使用枚举白名单映射。",
  "isNewOrAmplified": true,
  "blocksMerge": true,
  "ignored": false,
  "ignoreReason": null
}
```

## Context Risk 结构

`contextRisks` 用于变更审核中的历史问题、低置信度风险、非本次变更新增的问题。

```json
{
  "ruleId": "JAVA-LYR-001",
  "title": "Controller 历史上直接依赖 Mapper",
  "severity": "High",
  "confidence": "Medium",
  "location": {
    "file": "src/main/java/com/acme/order/OrderController.java",
    "line": 28
  },
  "reasonNotBlocking": "该依赖不是本次 diff 新增，但本次变更继续沿用该结构。"
}
```

## Confirmation 结构

`confirmations` 用于需要人工确认的业务约束或上下文。

```json
{
  "question": "orderBy 是否只能来自后端枚举白名单？",
  "relatedRuleId": "JAVA-SEC-001",
  "location": {
    "file": "src/main/java/com/acme/order/OrderQuery.java",
    "line": 19
  },
  "riskIfUnconfirmed": "如果该字段来自用户输入，可能导致 SQL 注入。"
}
```

## Validation 结构

```json
{
  "name": "compile",
  "command": "./mvnw -q -DskipTests compile",
  "result": "pass",
  "durationSeconds": 18,
  "summary": "Compilation succeeded."
}
```

`result` 可选值：

- `pass`
- `fail`
- `skipped`

## Files Reviewed 结构

```json
{
  "path": "src/main/java/com/acme/order/OrderService.java",
  "changeType": "Modified",
  "reviewedFully": true,
  "relatedFiles": [
    "src/main/java/com/acme/order/OrderMapper.java",
    "src/test/java/com/acme/order/OrderServiceTest.java"
  ]
}
```

## Change Review 示例

```json
{
  "schemaVersion": "1.0",
  "tool": "java-code-review",
  "mode": "change",
  "target": {
    "type": "ref-diff",
    "baseRef": "origin/main",
    "headRef": "HEAD",
    "scope": ["src/main/java/com/acme/order"]
  },
  "decision": "REQUEST_CHANGES",
  "summary": {
    "critical": 0,
    "high": 1,
    "medium": 0,
    "low": 0,
    "ignored": 0,
    "needsConfirmation": 0
  },
  "findings": [
    {
      "id": "F-001",
      "ruleId": "JAVA-LYR-002",
      "title": "同类自调用导致事务可能失效",
      "severity": "High",
      "confidence": "High",
      "dimension": "模块分层设计",
      "location": {
        "file": "src/main/java/com/acme/order/OrderService.java",
        "line": 88,
        "symbol": "createOrder"
      },
      "evidence": "createOrder 调用同类 this.persistOrder()，persistOrder 标注 @Transactional。",
      "impact": "事务代理不会生效，异常时订单和库存状态可能不一致。",
      "recommendation": "将事务边界放到外部 public service 方法，或拆分到独立 bean。",
      "isNewOrAmplified": true,
      "blocksMerge": true,
      "ignored": false,
      "ignoreReason": null
    }
  ],
  "contextRisks": [],
  "confirmations": [],
  "validation": [],
  "filesReviewed": [],
  "metadata": {
    "generatedAt": "2026-04-27T00:00:00Z"
  }
}
```

## Full Scan 示例

```json
{
  "schemaVersion": "1.0",
  "tool": "java-code-review",
  "mode": "scan",
  "target": {
    "type": "project",
    "scope": ["."]
  },
  "decision": "NOT_APPLICABLE",
  "summary": {
    "critical": 1,
    "high": 4,
    "medium": 8,
    "low": 3,
    "ignored": 0,
    "needsConfirmation": 2
  },
  "health": {
    "overall": "High Risk",
    "topRisks": [
      "多个 Controller 直接依赖 Mapper，分层边界弱。",
      "Mapper XML 中存在未确认白名单的 ${}。"
    ]
  },
  "findings": [],
  "contextRisks": [],
  "confirmations": [],
  "validation": [],
  "filesReviewed": [],
  "metadata": {
    "generatedAt": "2026-04-27T00:00:00Z"
  }
}
```
