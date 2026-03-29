# 测试中台架构设计

**日期**：2026-03-29
**状态**：草稿

---

## 一、定位与目标

### 1.1 核心定位

测试中台是**测试能力与流程的平台化基础设施**，通过插件化架构为各业务团队提供统一的测试执行、质量门禁、AI 辅助生成能力，同时支持业务方扩展特殊测试门禁。

### 1.2 设计原则

- **平台侧**：负责流程自动化、通用测试能力（三大测试类型 + AI 辅助生成）、基础设施
- **业务侧**：可自定义特殊测试门禁，平台提供表达能力
- **门禁**：可配置，触发时阻止合并操作
- **扩展性**：三大测试类型（单测/集成测/E2E）作为平台内置能力开箱即用，业务方可在平台基础上进行自定义扩展

---

## 二、系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         上游（代码仓库）                           │
│              GitLab / GitHub / 任意代码仓库 Webhook              │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      平台接入层 (Gateway)                         │
│   • 事件接收（Webhook / API）                                      │
│   • 认证鉴权                                                     │
│   • 协议适配                                                     │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      核心引擎 (Core Engine)                       │
│   • 工作流编排 • 任务调度 • 门禁评估 • 结果聚合                     │
└─────────────────────────────────────────────────────────────────┘
           │                    │                    │
           ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   AI 生成引擎    │  │   执行引擎       │  │   门禁系统       │
│                 │  │                 │  │                 │
│ • 单测/集成测    │  │ • 本地/容器执行  │  │ • DSL 门禁     │
│   /E2E 生成     │  │ • 资源管理       │  │ • 规则引擎     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
                                          │
                                          ▼
                              ┌─────────────────────────┐
                              │      插件系统            │
                              │  内置插件：单测/集成/E2E  │
                              │  扩展插件：业务方自定义    │
                              └─────────────────────────┘
                                          │
                                          ▼
                              ┌─────────────────────────┐
                              │      下游通知与回执       │
                              │  GitLab / 钉钉/企微/邮件  │
                              └─────────────────────────┘
```

### 2.2 模块边界与依赖

```
调用方向：上游 → 下游

Gateway（接入层）
  └─> Core Engine（核心调度层）
        ├─> AI Engine（AI 生成）
        ├─> Execution Engine（测试执行） ──> Plugin System（插件）
        └─> Gate System（门禁评估） ─────> Plugin System（插件）
```

| 模块 | 职责 | 不管什么 |
|------|------|---------|
| Gateway | 事件接入、协议适配 | 测试逻辑、门禁规则 |
| Core Engine | 流程编排、任务调度、结果聚合 | 具体执行、AI 生成 |
| AI Engine | 单测/集成测/E2E 生成、质量评估 | 任务调度、门禁执行 |
| Execution Engine | 环境准备、测试执行、资源回收 | 测试内容、门禁逻辑 |
| Gate System | 门禁规则解析、评估、结果判定 | 测试执行、资源管理 |
| Plugin System | 插件生命周期、接口契约 | 具体业务逻辑 |

### 2.3 设计原则

| 原则 | 说明 |
|------|------|
| **单向依赖** | 模块只能调用下游模块，不能反向调用 |
| **Core Engine 自主** | 不被任何核心模块依赖，可独立演进 |
| **Plugin System 底层** | 作为基础层，被 Execution Engine 和 Gate System 共用 |
| **Gateway 隔离** | 不感知内部模块，只负责事件接入 |

---

## 三、核心模块详解

### 3.1 Gateway（平台接入层）

| 职责 | 说明 |
|------|------|
| 事件接收 | 接收上游 Webhook 事件（MR 创建、更新、代码提交等） |
| 协议适配 | 将上游事件转换为平台内部事件格式 |
| 认证鉴权 | 验证请求合法性，平台级认证 |

**边界要点**：平台不感知上游是 GitLab 还是 GitHub，适配层负责协议转换。

### 3.2 Core Engine（核心引擎）

| 职责 | 说明 |
|------|------|
| 工作流编排 | 定义测试流程步骤、依赖关系、并行策略 |
| 任务调度 | 将测试任务分配给执行引擎，支持队列与优先级 |
| 门禁评估 | 按顺序执行门禁规则，决定是否阻止合并 |
| 结果聚合 | 收集各环节结果，生成最终报告 |

**边界要点**：核心引擎**不**执行具体测试，只负责任务编排和流程控制。

### 3.3 AI Generation Engine（AI 生成引擎）

| 职责 | 说明 |
|------|------|
| 代码变更分析 | 分析 MR 代码变更范围和内容 |
| 单测生成 | 基于变更代码生成单测用例 |
| 集成测生成 | 基于模块交互、API 调用生成集成测试用例 |
| E2E 测试生成 | 基于用户流程、页面交互生成端到端测试用例 |
| 质量评估 | 对生成的测试进行静态分析和覆盖率预估 |

| 测试类型 | AI 生成成熟度 | 推荐 |
|---------|-------------|------|
| 单测 | ★★★★★ | 生产可用 |
| 集成测 | ★★★☆☆ | 建议启用 |
| E2E | ★★☆☆☆ | 实验性 |

**边界要点**：AI 生成是**平台内置能力**，不作为插件。不同测试类型的 AI 生成能力作为独立模块，可按需启用。

### 3.4 Execution Engine（执行引擎）

| 职责 | 说明 |
|------|------|
| 环境准备 | 创建测试执行环境（容器、Mock 数据） |
| 测试执行 | 调用对应插件执行测试 |
| 结果收集 | 收集测试输出、日志、覆盖率数据 |
| 资源回收 | 测试完成后释放资源 |

### 3.5 Gate System（门禁系统）

#### 3.5.1 DSL 门禁（简单场景）

```yaml
# 示例：覆盖率门禁
- type: coverage
  threshold: 80
  scope: src/main/java

# 示例：测试通过率门禁
- type: test_pass_rate
  threshold: 95
```

#### 3.5.2 规则引擎（复杂场景）

```java
@RuleDef(name = "高风险业务门禁")
public class HighRiskBusinessGate implements GateRule {
    @Override
    public GateResult evaluate(Context context) {
        // 自定义复杂逻辑
    }
}
```

#### 3.5.3 门禁组合

```yaml
gates:
  - name: 代码质量
    gates: [coverage_check, code_review_score]
    require_all: true
  - name: 安全检查
    gates: [secrets_detection, dependency_scan]
    require_all: false
```

### 3.6 Plugin System（插件系统）

#### 3.6.1 插件接口

```java
// 测试执行插件
public interface TestPlugin {
    String getName();
    TestType getSupportedType();
    default void prepare(ExecutionContext ctx) {}
    TestResult execute(TestTask task);
    default void cleanup(ExecutionContext ctx) {}
}

// 门禁评估插件
public interface GatePlugin {
    String getName();
    GateResult evaluate(GateContext context);
}
```

#### 3.6.2 内置插件

| 类型 | 说明 |
|------|------|
| 单测执行插件 | JUnit / Jest / pytest |
| 集成测执行插件 | TestContainers |
| E2E 执行插件 | Playwright / Selenium |

---

## 四、工作流程

### 4.1 MR 测试主流程

```
触发阶段
│
├─ 1. 代码提交 / MR 创建 / MR 更新
├─ 2. Gateway 接收 Webhook 事件
├─ 3. 协议转换 → 内部 TestEvent
└─ 4. Core Engine 创建 TestPipeline
        │
        ▼
测试执行阶段
│
├─ 5. AI Engine 分析代码变更
│   ├─ 识别变更范围
│   └─ AI 生成测试用例（按类型配置）
│      ├─ 单测生成（成熟）
│      ├─ 集成测生成（实验性）
│      └─ E2E 生成（实验性）
│
├─ 6. Execution Engine 执行测试
│   ├─ 加载对应插件
│   ├─ 准备执行环境
│   └─ 收集结果（通过/失败/覆盖率）
│
        ▼
门禁评估阶段
│
├─ 7. Gate System 收集门禁列表
│   └─ 平台内置门禁 ∪ 业务方门禁
│
├─ 8. 按优先级逐个执行门禁
│   ├─ DSL 门禁 → 解析执行
│   └─ 规则门禁 → 调用规则引擎
│
├─ 9. 结果聚合
│   ├─ 全部通过 → PASS
│   ├─ 有容错项 → PASS_WITH_WARNING
│   └─ 关键项失败 → FAIL（阻止合并）
│
        ▼
回执阶段
│
├─ 10. 回执给上游（GitLab Status Check）
└─ 11. 通知（钉钉/企微/邮件）+ 报告存储
```

#### 失败处理策略

| 场景 | 处理策略 |
|------|---------|
| 测试执行失败 | 配置重试次数（默认 2 次），指数退避 |
| AI 生成失败 | 阻断型→Pipeline FAIL；警告型→记录日志继续 |
| 门禁执行失败 | 默认并行；支持"快速失败"（任一 FAIL 即停止） |
| 执行超时 | 单任务 TIMEOUT；Pipeline 可配置整体超时 |
| MR 关闭取消 | IMMEDIATE（立即）/ GRACEFUL（完成后） |

#### 触发策略

```yaml
trigger:
  debounce_window_seconds: 30   # 防抖窗口
  deduplicate_by: commit_sha     # 去重依据
  mode:
    auto: true                   # 自动触发
    manual: true                 # 手动触发
```

### 4.2 业务方插件注册与执行流程

```
插件注册（一次性）
│
├─ 1. 业务方开发插件，实现标准接口
├─ 2. 通过平台 UI / API 上传
├─ 3. 平台验证接口完整性和权限
└─ 4. 注册成功，进入插件目录
        │
        ▼
插件执行（每次 MR 触发）
│
├─ 5. 业务方在项目中配置启用的插件
├─ 6. Core Engine 调度时加载插件列表
├─ 7. Execution Engine / Gate System 调用插件
│   └─ prepare() → execute() → cleanup()
└─ 8. 结果返回 Core Engine
```

---

## 五、核心对象定义

### 5.1 数据结构

```java
// 触发事件
TestEvent {
    String eventId;
    String eventType;         // MR_CREATED / MR_UPDATED / PUSH
    String repositoryId;
    String mrId;
    String commitSha;
    String branch;
    String targetBranch;
    Long timestamp;
}

// 测试流水线
TestPipeline {
    String pipelineId;
    String eventId;
    PipelineStatus status;
    List<TestTask> tasks;
    List<GateResult> gateResults;
    Long startTime;
    Long endTime;
}

// 测试任务
TestTask {
    String taskId;
    TaskType type;            // UNIT_TEST / INTEGRATION_TEST / E2E_TEST
    TestPlugin plugin;
    ExecutionContext context;
    TaskStatus status;
}

enum TaskType {
    UNIT_TEST,
    INTEGRATION_TEST,
    E2E_TEST,
    AI_GENERATED,
    PERFORMANCE_TEST,
    CONTRACT_TEST
}
```

### 5.2 状态机

```
PENDING → RUNNING → SUCCESS
                  → FAIL
                  → WARNING
                  → CANCELLED（用户取消）
                  → TIMEOUT（超时）
```

---

## 六、安全设计

### 6.1 插件隔离

| 方案 | 推荐度 | 说明 |
|------|-------|------|
| **独立进程隔离** | 推荐 | 实现简单，隔离性好 |
| **容器隔离** | 可选 | 完全隔离，适合高安全要求 |
| **JVM 沙箱** | 慎用 | 性能好，但安全性低 |

### 6.2 插件权限控制

```java
@PluginDef(
    name = "secrets-detection",
    permissions = {
        "file:read:/src",
        "net:http:internal-api",
        "cpu:2",
        "memory:1gb"
    }
)
public class SecretsDetectionPlugin implements GatePlugin { }
```

### 6.3 密钥管理

| 类型 | 管理方式 |
|------|---------|
| AI API 密钥 | Vault / 密管服务存储，运行时注入 |
| Webhook 回调密钥 | 平台统一管理 |
| 插件外部密钥 | 插件自行管理 |

---

## 七、插件场景示例

### 7.1 自定义安全扫描门禁

```java
@PluginDef(name = "secrets-detection")
public class SecretsDetectionPlugin implements GatePlugin {
    @Override
    public GateResult evaluate(Context context) {
        boolean hasSecrets = scanForSecrets(context.getDiff());
        return GateResult.builder()
            .passed(!hasSecrets)
            .message(hasSecrets ? "发现敏感信息泄露" : "无敏感信息")
            .build();
    }
}
```

### 7.2 性能基准测试

```java
@PluginDef(name = "performance-benchmark")
public class PerformanceBenchmarkPlugin implements TestPlugin {
    @Override
    public TestResult execute(TestTask task) {
        // 执行性能测试，与历史基准对比
    }
}
```

### 7.3 多服务契约测试

```java
@PluginDef(name = "service-contract-check")
public class ServiceContractPlugin implements TestPlugin {
    @Override
    public TestResult execute(TestTask task) {
        // 验证下游服务接口兼容性
    }
}
```

---

## 八、实现路径

### Phase 1：核心框架
- 搭建项目骨架
- 实现 Gateway 接入层
- 实现 Core Engine 核心调度
- 实现基础门禁系统（DSL）

### Phase 2：内置能力
- 实现 AI 生成引擎（单测 / 集成测 / E2E）
- 实现 Execution Engine
- 内置单测 / 集成测 / E2E 执行插件

### Phase 3：插件生态
- 完善 Plugin System
- 定义插件 SDK
- 开发业务方插件文档

### Phase 4：高级特性
- 规则引擎增强
- 分布式执行
- 测试数据分析

---

## 九、待确认事项

1. **插件 SDK 技术栈**：Java SDK / HTTP API / 其他？
2. **执行引擎**：本地执行 / 容器化 / K8s？
3. **状态存储**：MySQL / PostgreSQL / 其他？
4. **部署形态**：独立部署 / GitLab App / 混合？
5. **AI 厂商选择**：Claude / GPT / 通义 / 自建？
6. **E2E 测试框架**：Playwright / Selenium / Cypress？
