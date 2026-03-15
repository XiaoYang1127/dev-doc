# 代码审核规则管理规范

> 本文件定义代码审核系统中规则的结构规范、管理机制、生命周期及持续改进理论。
>
> **文件定位**：宏观规则管理理论，不涉及具体语言实现。
>
> **配套文件**：
> - [languages/java.md](./languages/java.md) - Java 技术栈规则具体实现
> - [languages/typescript.md](./languages/typescript.md) - TypeScript 技术栈规则具体实现（待创建）

---

## 目录

1. [规则结构规范](#1-规则结构规范)
2. [规则类型体系](#2-规则类型体系)
3. [规则管理机制](#3-规则管理机制)
4. [规则生命周期](#4-规则生命周期)
5. [规则持续改进](#5-规则持续改进)
6. [附录](#6-附录)

---

## 1. 规则结构规范

### 1.1 设计原则

规则结构设计遵循以下原则：

| 原则 | 说明 | 实践 |
|------|------|------|
| **声明式** | 规则描述"检查什么"，而非"如何检查" | 通过 condition 声明匹配条件 |
| **可组合** | 规则可叠加、可继承、可覆盖 | 支持规则集和优先级配置 |
| **可度量** | 每个规则必须有质量指标 | confidence、误报率、命中率 |
| **自包含** | 规则包含完整的上下文信息 | 正例、反例、修复建议 |

### 1.2 完整字段定义

```yaml
Rule:
  # ========== 基础信息 ==========
  id: string                    # 唯一标识
  name: string                  # 规则名称
  description: string           # 规则描述
  category: enum                # 分类
  severity: enum                # 严重级别
  layer: int                    # 所属审核维度（1-9）

  # ========== 适用范围 ==========
  language: string              # 适用语言
  scope: enum                   # 检测粒度
  applies_to:                   # 适用场景（可选）
    file_patterns: []
    exclude_patterns: []

  # ========== 匹配条件 ==========
  condition:
    type: enum                  # 条件类型
    pattern: any                # 匹配模式
    constraints: []             # 约束条件
    exclusions: []              # 排除条件（第2.4节）

  # ========== 检测配置 ==========
  detector:
    type: enum
    config: object

  # ========== 输出模板 ==========
  message:
    template: string            # 问题描述（支持变量）
    suggestion: string          # 修复建议
    example_good: string        # 正例代码
    example_bad: string         # 反例代码
    fix_template: string        # 自动修复模板（可选）

  # ========== 元数据 ==========
  meta:
    confidence: float           # 置信度（0-1）
    auto_fixable: boolean       # 是否支持自动修复
    enabled: boolean            # 是否启用
    tags: []                    # 标签

    # 统计信息（运行时填充）
    stats:
      hit_count: int            # 命中次数
      false_positive_count: int # 误报次数
      last_hit_at: timestamp    # 最后命中时间

    # 版本信息
    version: string             # 规则版本（语义化版本）
    created_at: timestamp
    updated_at: timestamp
    author: string              # 规则作者
    reviewers: []               # 审核人员
```

### 1.3 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 全局唯一标识，命名规范见附录 |
| `name` | string | 是 | 人类可读的简短名称（20字以内） |
| `description` | string | 是 | 详细描述，包含问题场景和后果 |
| `category` | enum | 是 | 规则所属大类，见附录分类列表 |
| `severity` | enum | 是 | `error` / `warning` / `suggestion` |
| `layer` | int | 是 | 审核维度 1-9，对应 plan.md 中的层级 |
| `language` | string | 是 | `java` / `typescript` / `dart` / `all` |
| `scope` | enum | 是 | 检测粒度：`file` / `method` / `statement` / `expression` |
| `condition` | object | 是 | 匹配条件，类型见第2节 |
| `message` | object | 是 | 输出模板，支持变量替换 |
| `meta.confidence` | float | 是 | 默认置信度，用于结果排序和过滤 |

---

## 2. 规则类型体系

### 2.1 类型概览

| 类型 | 标识 | 适用场景 | 性能 | 准确度 |
|------|------|----------|------|--------|
| 正则规则 | `regex` | 文本模式匹配 | 高 | 中 |
| AST规则 | `ast` | 语法结构匹配 | 中 | 高 |
| 语义规则 | `semantic` | 数据流分析 | 低 | 高 |
| AI规则 | `ai` | 复杂逻辑判断 | 极低 | 中 |

**选型建议**：
- 能用正则解决的，不用 AST
- 能用 AST 解决的，不用语义
- 能用语义解决的，不用 AI

### 2.2 正则规则（RegexRule）

适用于简单的文本模式匹配。

```yaml
condition:
  type: regex
  pattern: string           # 正则表达式
  constraints:              # 约束条件
    not_in_comment: boolean # 排除注释
    not_in_string: boolean  # 排除字符串
    not_in_import: boolean  # 排除 import
```

### 2.3 AST规则（AstRule）

基于抽象语法树的结构化匹配。

```yaml
condition:
  type: ast
  pattern: string           # AST查询语句（类XPath）
  constraints:              # 结构约束
    min_depth: int
    max_depth: int
    parent_types: []        # 父节点类型限制
```

### 2.4 语义规则（SemanticRule）

基于代码语义分析，支持数据流追踪。

```yaml
condition:
  type: semantic
  pattern:
    # 注解检测
    annotations: []

    # 调用链检测
    call_chain:
      entry: string         # 入口方法
      forbidden: []         # 禁止的调用
      required: []          # 必须的调用

    # 数据流检测
    data_flow:
      source: string        # 数据源
      sink: string          # 数据终点
      sanitizers: []        # 清洗函数
```

### 2.5 AI规则（AiRule）

使用AI进行复杂场景分析。

```yaml
condition:
  type: ai
  prompt: string            # 提示词模板
  context_provider: string  # 上下文提供方式

detector:
  type: ai
  config:
    model: string           # AI模型
    temperature: float
    max_tokens: int
    response_format: object # 期望的JSON格式
```

### 2.6 排除规则（Exclusion）

定义规则不触发的情况，减少误报。

```yaml
condition:
  exclusions:
    - type: annotation      # 有特定注解时排除
      annotations: ["@Test", "@Deprecated"]

    - type: pattern         # 匹配特定模式时排除
      patterns: ["mock", "test", "stub"]

    - type: scope           # 特定范围内排除
      scopes: ["test/*", "**/test/**"]

    - type: comment         # 特定注释标记时排除
      markers: ["#noinspection", "//NOSONAR", "//skip-review"]
```

---

## 3. 规则管理机制

### 3.1 规则优先级

#### 严重级别权重

| 严重级别 | 权重 | 阻断构建 | 默认显示 |
|----------|------|----------|----------|
| `error` | 100 | 是 | 是 |
| `warning` | 50 | 否 | 是 |
| `suggestion` | 10 | 否 | 否 |

#### 排序策略

```yaml
rule_priority:
  # 第一优先级：严重级别
  severity_order: [error, warning, suggestion]

  # 第二优先级：置信度（降序）
  confidence_weight: 1.0

  # 第三优先级：层级（靠前的优先）
  layer_order: [2, 3, 4, 5, 6, 7, 8, 9]

  # 第四优先级：文件路径
  location_order: alphabetical
```

### 3.2 规则覆盖策略

#### 覆盖层级

```
执行时覆盖优先级（高到低）：

┌─────────────────────────────────────┐
│  1. 命令行参数 (--disable-rule)      │  ← 临时禁用
├─────────────────────────────────────┤
│  2. 项目级配置 (.code-review.yml)    │  ← 项目定制
├─────────────────────────────────────┤
│  3. 团队级配置 (远程配置中心)         │  ← 团队规范
├─────────────────────────────────────┤
│  4. 全局默认规则 (builtin)           │  ← 系统默认
└─────────────────────────────────────┘
```

#### 覆盖方式

```yaml
# 项目级配置示例
rule_override:
  # 禁用特定规则
  disabled:
    - java.performance.micro-optimization

  # 调整严重级别
  severity_adjust:
    java.security.sql-injection: error
    java.architecture.layer-violation: warning

  # 调整阈值
  threshold_adjust:
    java.scan.cyclomatic-complexity:
      max_complexity: 15  # 覆盖默认的10

  # 添加排除
  add_exclusions:
    java.npe.null-check:
      - type: scope
        scopes: ["**/legacy/**"]
```

### 3.3 规则集管理

#### 规则集定义

```yaml
rule_set:
  id: string                # 唯一标识
  name: string              # 显示名称
  description: string       # 描述
  extends: []               # 继承的规则集

  # 规则选择
  rules:
    include: []             # 包含规则（通配符）
    exclude: []             # 排除规则

  # 全局调整
  severity_override: {}
  threshold_override: {}
```

#### 预定义规则集

| 规则集ID | 说明 | 适用场景 |
|----------|------|----------|
| `essential` | 仅最严重问题 | 快速检查 |
| `standard` | 标准规则集 | 日常使用 |
| `strict` | 严格模式 | 核心模块 |
| `security-focus` | 安全专项 | 安全审计 |
| `performance-focus` | 性能专项 | 性能优化 |

---

## 4. 规则生命周期

### 4.1 状态流转

```
┌─────────┐    提交PR     ┌─────────┐    委员会审批    ┌─────────┐
│  draft  │ ───────────▶ │ review  │ ──────────────▶ │ preview │
│  (草稿)  │              │ (审核中) │                │ (试运行) │
└─────────┘              └─────────┘                └────┬────┘
     ▲                                                   │
     │                    ┌─────────┐                    │ 2周试点
     └────────────────────│removed  │◀───────────────────┤
                          │ (已删除) │                    │
                          └─────────┘              ┌─────┴────┐
                                                   │          │ 质量门禁
                                              不通过│          │通过
                                                   ▼          ▼
                                            ┌─────────┐  ┌─────────┐
                                            │deprecated│  │ active  │
                                            │ (已废弃) │  │ (已发布) │
                                            └─────────┘  └────┬────┘
                                                              │
                                    ┌─────────────────────────┘
                                    │ 误报率>30% 或 90天无命中
                                    ▼
                              ┌─────────┐
                              │deprecated
                              │ (已废弃) │
                              └────┬────┘
                                   │ 保留6个月
                                   ▼
                              ┌─────────┐
                              │ removed │
                              │ (已删除) │
                              └─────────┘
```

### 4.2 各阶段说明

| 状态 | 说明 | 可见性 | 执行审核 | 质量要求 |
|------|------|--------|----------|----------|
| `draft` | 规则草稿 | 仅作者 | 否 | 无 |
| `review` | 规则审核中 | 审核人员 | 否 | 规范检查 |
| `preview` | 试运行 | 试点项目 | 是 | 误报率<30% |
| `active` | 已发布 | 全部 | 是 | 误报率<10% |
| `deprecated` | 已废弃 | 全部 | 是（仅提示） | 无 |
| `removed` | 已删除 | 无 | 否 | 无 |

### 4.3 质量门禁

#### 试运行转正标准

```yaml
preview_to_active:
  min_test_cases: 10              # 最少测试用例数
  max_false_positive_rate: 0.30   # 最大误报率 30%
  min_hit_rate: 0.01              # 最小命中率 1%
  code_review_approved: true      # 代码审核通过
  docs_complete: true             # 文档完整
```

#### 废弃标准

```yaml
active_to_deprecated:
  max_false_positive_rate: 0.50   # 误报率超过50%
  min_days_since_last_hit: 90     # 90天无命中
  min_issues_without_fix: 10      # 10个问题未修复
```

---

## 5. 规则持续改进

### 5.1 反馈闭环

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   审核    │────▶│  开发者   │────▶│  反馈标记 │────▶│  数据分析 │
│  执行    │     │  查看结果 │     │ (正/误报) │     │         │
└──────────┘     └──────────┘     └──────────┘     └────┬─────┘
     ▲                                                  │
     │                                                  ▼
     │                                           ┌──────────┐
     │                                           │  规则优化  │
     │                                           │  决策     │
     │                                           └────┬─────┘
     │                                                  │
     │              ┌──────────┐     ┌──────────┐      │
     └──────────────│  A/B测试  │◀────│  规则更新  │◀─────┘
                    │  验证    │     │         │
                    └──────────┘     └──────────┘
```

### 5.2 反馈收集

#### 反馈类型

| 类型 | 标识 | 说明 | 处理动作 |
|------|------|------|----------|
| 确认有效 | `confirmed` | 开发者认可并修复 | 提升规则权重 |
| 误报标记 | `false-positive` | 错误报警 | 降低规则权重，分析改进 |
| 忽略标记 | `ignore` | 特定场景豁免 | 记录白名单 |
| 补充上下文 | `context` | 提供额外信息 | 优化提示词 |

#### 反馈方式

```yaml
feedback_collection:
  # MR评论标记
  mr_comments:
    - pattern: "/false-positive"
      action: mark_false_positive
    - pattern: "/confirmed"
      action: mark_confirmed
    - pattern: "/ignore"
      action: add_to_whitelist

  # IDE交互
  ide_actions:
    - action: "一键修复"
      metric: auto_fix_usage
    - action: "忽略此规则"
      metric: manual_disable

  # 修复追踪
  fix_tracking:
    enabled: true
    track_commit_hash: true
    track_fix_time: true
```

### 5.3 数据分析与优化

#### 核心指标

| 指标 | 计算公式 | 目标值 | 行动阈值 |
|------|----------|--------|----------|
| 误报率 | FP / (FP + TP) | < 10% | > 30% 需优化 |
| 漏报率 | FN / (FN + TP) | < 5% | > 20% 需优化 |
| 命中率 | 命中次数 / 总审核数 | > 1% | < 0.1% 考虑废弃 |
| 修复率 | 已修复 / 确认有效 | > 80% | < 50% 检查建议质量 |
| 平均修复时间 | 发现问题到修复的时长 | < 7天 | > 30天 检查实用性 |

#### 优化决策矩阵

```
                高误报率 (>30%)
                     │
         ┌───────────┼───────────┐
         │           │           │
    低命中率        紧急优化        废弃或重写
    (<0.1%)         │           高命中率
         │     ┌────┴────┐      (>1%)
         │     │         │        │
         └────▶│  调优   │◀───────┘
               │ 阈值    │
               │ 模式    │
               │ 提示词  │
               └────┬────┘
                    │
              低误报率 (<10%)
```

### 5.4 A/B测试流程

```yaml
ab_test_workflow:
  # 测试触发条件
  trigger:
    - rule_modified: true
    - threshold_changed: true
    - pattern_updated: true

  # 测试配置
  config:
    control_group: 50%      # 对照组比例
    treatment_group: 50%    # 实验组比例
    min_sample_size: 100    # 最小样本数
    max_duration_days: 14   # 最大测试时长

  # 评估指标
  metrics:
    primary: false_positive_rate_change
    secondary:
      - hit_rate_change
      - fix_rate_change
      - user_satisfaction

  # 决策阈值
  decision:
    rollback:
      fp_rate_increase: 0.05    # 误报率上升5%则回滚
    release:
      fp_rate_decrease: 0.03    # 误报率下降3%则发布
```

---

## 6. 附录

### 6.1 规则ID命名规范

#### 命名格式

```
{语言}.{层级}.{分类}.{具体规则}
```

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| `语言` | 编程语言或 all | `java`, `typescript`, `dart`, `all` |
| `层级` | 审核维度 | `fragment`, `context`, `architecture`, `callchain`, `dataflow`, `security`, `performance`, `scan` |
| `分类` | 规则所属大类 | 见分类列表 |
| `具体规则` | 短横线分隔的描述 | `controller-direct-repository` |

#### 层级代码列表

| 层级代码 | 对应维度 | 说明 |
|----------|----------|------|
| `change` | L1 | 变更感知层 |
| `fragment` | L2 | 片段审核层 |
| `context` | L3 | 上下文审核层 |
| `architecture` | L4 | 架构审核层 |
| `callchain` | L5 | 调用链审核层 |
| `dataflow` | L6 | 数据流审核层 |
| `security` | L7 | 安全审核层 |
| `performance` | L8 | 性能审核层 |
| `scan` | L9 | 全量扫描层 |

#### 分类列表

| 分类代码 | 说明 | 适用层级 |
|----------|------|----------|
| `architecture` | 架构规范 | L4 |
| `security` | 安全漏洞 | L2, L7 |
| `performance` | 性能优化 | L6, L8 |
| `npe` | NPE防护 | L2, L3 |
| `transaction` | 事务管理 | L6 |
| `logic` | 业务逻辑 | L2, L3 |
| `style` | 代码风格 | L9 |
| `compatibility` | 兼容性 | L5 |
| `concurrency` | 并发安全 | L6 |
| `resource` | 资源管理 | L2, L3 |
| `quality` | 代码质量 | L9 |
| `debt` | 技术债务 | L9 |

#### 示例

- `java.fragment.security.hardcoded-secret`
- `java.architecture.controller-direct-repository`
- `java.dataflow.transaction.remote-in-transaction`
- `java.scan.cyclomatic-complexity`

### 6.2 severity 定义

| 级别 | 标识 | 定义 | 示例 |
|------|------|------|------|
| `error` | 严重 | 必须修复，可能导致故障或安全问题 | SQL注入、NPE风险、架构违规 |
| `warning` | 警告 | 建议修复，可能存在隐患 | 圈复杂度过高、事务内远程调用 |
| `suggestion` | 建议 | 可选优化，提升代码质量 | 方法过长、TODO标记 |

### 6.3 规则模板

创建新规则时，使用以下模板：

```yaml
id: {language}.{layer}.{category}.{rule-name}
name: 规则名称（简洁描述）
description: |
  详细描述规则检测的问题场景、
  可能导致的后果、适用条件
category: {category}
severity: {error|warning|suggestion}
layer: {2-9}
language: {java|typescript|dart|all}
scope: {file|method|statement|expression}

condition:
  type: {regex|ast|semantic|ai}
  # 根据类型填写具体配置

message:
  template: "问题描述，支持{变量}替换"
  suggestion: "修复建议"
  example_good: |
    // 正确的代码示例
  example_bad: |
    // 错误的代码示例

meta:
  confidence: 0.85
  auto_fixable: false
  tags: [tag1, tag2]
  version: "1.0.0"
```
