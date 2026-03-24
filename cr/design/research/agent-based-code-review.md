# 基于 Agent 的代码审核系统调研报告

> 调研目标：评估 LangChain、CrewAI、Claude Agent SDK 等 Agent 框架作为代码审核系统底座的可行性，提出与现有规则引擎结合的混合架构方案。
>
> 调研日期：2026-03-24
> 配套文档：[implementation.md](../implementation.md) - 现有规则引擎实现方案

---

## 目录

1. [方案对比分析](#1-方案对比分析)
2. [推荐的混合架构](#2-推荐的混合架构)
3. [关键增强点详解](#3-关键增强点详解)
4. [渐进式迁移策略](#4-渐进式迁移策略)
5. [技术选型结论](#5-技术选型结论)
6. [参考资源](#6-参考资源)

---

## 1. 方案对比分析

### 1.1 核心维度对比

| 维度 | LangChain / CrewAI | Claude Agent SDK | 现有规则引擎 |
|------|-------------------|------------------|-------------|
| **核心定位** | 通用 LLM 编排框架 | Claude 原生 Agent 平台 | 静态规则执行 |
| **扩展性** | 高（多模型/多工具）| 中高（Claude 生态）| 中（规则 DSL）|
| **代码理解** | 依赖 LLM 通用能力 | Claude 代码能力原生优化 | AST/字节码精确分析 |
| **执行确定性** | 低（LLM 不确定性）| 中（结构化输出改善）| 高（规则硬编码）|
| **成本** | 高（大量 LLM 调用）| 中高（长上下文优化）| 低（本地计算）|
| **延迟** | 高 | 中 | 低 |
| **上下文长度** | 依赖底层模型 | 200K 原生支持 | 无限制（本地）|
| **工具生态** | 丰富但泛化 | MCP 标准化对接 | 开源工具集成 |

### 1.2 适用场景分析

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| 语法/规范检查（L1-L3）| **规则引擎** | 确定性高、成本低、延迟低 |
| 架构依赖分析（L4-L5）| **混合** | 规则做索引，Agent 做推理 |
| 安全漏洞检测（L7）| **混合** | SpotBugs 做污点分析，Agent 做路径验证 |
| 复杂业务逻辑审核 | **Agent** | 需要上下文理解和推理 |
| 反馈学习优化（L10）| **Agent** | 需要模式识别和持续学习 |
| 审核策略编排 | **Agent** | 动态决策、灵活路由 |

---

## 2. 推荐的混合架构

### 2.1 整体架构图

```
┌──────────────────────────────────────────────────────────────┐
│                    Agent 编排层 (Claude SDK)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │ Review      │  │ Context     │  │ Feedback Learning   │   │
│  │ Orchestrator│  │ Manager     │  │ Agent (L10)         │   │
│  └──────┬──────┘  └──────┬──────┘  └─────────────────────┘   │
│         │                │                                    │
│         └────────────────┼────────────────┐                   │
│                          ▼                ▼                   │
│              ┌──────────────────┐  ┌──────────────┐          │
│              │ Plan & Execute   │  │ Multi-Agent  │          │
│              │ (复杂场景推理)    │  │ (并行审核)    │          │
│              └────────┬─────────┘  └──────┬───────┘          │
└───────────────────────┼───────────────────┼──────────────────┘
                        │                   │
        ┌───────────────┼───────────────────┘
        ▼               ▼
┌──────────────────────────────────────────────────────────────┐
│                  规则引擎层 (保留并增强)                        │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │
│  │L1-L3    │ │L4-L6    │ │L7       │ │L8       │ │L9       │ │
│  │语法/片段│ │架构/调用│ │安全规则 │ │性能模式 │ │全量扫描 │ │
│  │(PMD/AST)│ │(ArchUnit│ │(SpotBugs)│ │(内置)   │ │(Sonar)  │ │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ │
└───────┼───────────┼───────────┼───────────┼───────────┼──────┘
        │           │           │           │           │
        └───────────┴───────────┴───────────┴───────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│                    上下文管理层                                │
│        AST Cache / Call Graph Index / Data Flow Graph         │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 分层职责说明

| 层级 | 组件 | 职责 | 技术选型 |
|------|------|------|---------|
| 编排层 | Review Orchestrator | 根据变更特征动态选择审核策略 | Claude Agent SDK |
| 编排层 | Context Manager | 智能管理 LLM 上下文，解决大库问题 | Claude Agent SDK |
| 编排层 | Feedback Learning | 从反馈中学习，优化规则质量 | Claude Agent SDK |
| 执行层 | L1-L3 | 语法、规范、片段审核 | PMD + JavaParser |
| 执行层 | L4-L6 | 架构、调用链、数据流 | ArchUnit + 自研 |
| 执行层 | L7 | 安全漏洞检测 | SpotBugs + FindSecBugs |
| 执行层 | L8 | 性能模式识别 | 自研 + PMD |
| 执行层 | L9 | 全量扫描 | SonarQube |
| 基础层 | 上下文管理 | AST 缓存、调用图索引、数据流图 | Caffeine + Redis |

---

## 3. 关键增强点详解

### 3.1 Agent 化审核编排

替代原有的硬编码 `ReviewOrchestrator`，实现动态策略选择。

```python
# agents/review_orchestrator.py
from claude_agent_sdk import Agent, Tool

class ReviewOrchestrator(Agent):
    """智能审核编排器：根据代码变更自动选择审核策略"""

    system_prompt = """你是代码审核编排专家。
根据代码变更的类型和复杂度，决定调用哪些审核维度。
可并行调用多个子 Agent，最后合并结果。"""

    tools = [
        Tool(name="detect_change_scope",
             description="分析变更范围：影响面、复杂度"),
        Tool(name="route_to_layer",
             description="路由到对应审核层"),
        Tool(name="aggregate_results",
             description="聚合多维度审核结果")
    ]

    async def execute(self, review_request: ReviewRequest) -> ReviewReport:
        # 1. 分析变更特征
        change_scope = await self.call_tool("detect_change_scope",
                                            review_request.diff)

        # 2. 动态选择审核维度（替代原来的硬编码分层）
        layers = self.decide_layers(change_scope)

        # 3. 并行执行子 Agent
        results = await self.parallel_execute([
            self.spawn_subagent(f"layer_{layer}", review_request)
            for layer in layers
        ])

        return self.merge_results(results)

    def decide_layers(self, scope: ChangeScope) -> List[int]:
        """智能决策：根据变更特征决定审核深度"""
        layers = [1, 2, 3]  # 基础层必做

        if scope.affects_api:
            layers.extend([4, 5])  # 架构 + 调用链

        if scope.has_user_input:
            layers.append(7)  # 安全审核

        if scope.database_changes:
            layers.append(6)  # 数据流

        return layers
```

**核心价值**：
- 不再硬编码分层触发逻辑
- 根据变更特征动态调整审核深度
- 支持并行执行和结果聚合

### 3.2 上下文管理 Agent

解决大代码库上下文加载问题，替代原有的 `CodeContextFactory`。

```python
# agents/context_manager.py
class ContextManager(Agent):
    """智能上下文管理：决定加载哪些代码到 LLM 上下文"""

    system_prompt = """你是代码上下文管理专家。
目标：为 LLM 审核提供最相关的代码上下文，同时控制 token 消耗。
大库策略：索引 + 检索，而非全量加载。"""

    async def build_context(self,
                          primary_file: str,
                          review_request: ReviewRequest) -> CodeContext:

        # 1. 从索引检索相关符号
        related_symbols = await self.query_codebase_index(
            query=primary_file,
            top_k=20,  # 限制相关文件数量
            filters={
                "module": review_request.module,
                "recent_changes": True  # 优先最近变更
            }
        )

        # 2. 分层加载策略
        context = CodeContext()

        # L1-L3: 当前文件完整加载
        context.primary_ast = await self.load_ast(primary_file)

        # L4-L5: 接口定义 + 直接调用方
        context.interface_defs = await self.load_interfaces(related_symbols)
        context.callers = await self.load_callers(related_symbols, depth=1)

        # L6-L7: 数据流摘要（预分析结果）
        context.dataflow_summary = await self.load_dataflow_summary(
            related_symbols
        )

        # 3. 如果 token 预算充足，加载更多上下文
        if self.token_budget.remaining > 100000:
            context.extended_context = await self.load_extended_context(
                related_symbols
            )

        return context

    async def query_codebase_index(self, query: str, **filters):
        """查询预构建的代码索引（非实时解析）"""
        # 使用向量检索 + 符号索引
        return await self.vector_store.similarity_search(
            query=query,
            embedding_model="code-embeddings",
            **filters
        )
```

**核心价值**：
- 智能决定加载哪些上下文，控制 token 消耗
- 向量检索 + 符号索引，避免全库加载
- 分层加载策略，优先高价值上下文

### 3.3 反馈学习 Agent（L10）

替代原有的 `RuleOptimizer`，实现真正的持续学习。

```python
# agents/feedback_learning_agent.py
class FeedbackLearningAgent(Agent):
    """从开发者反馈中持续学习，优化审核质量"""

    system_prompt = """你是代码审核质量优化专家。
分析开发者反馈，识别规则误报、漏报模式，
提出规则优化建议，或生成新的审核规则。"""

    async def analyze_feedback_batch(self,
                                   feedback_list: List[ReviewFeedback]):

        # 1. 聚类分析反馈
        patterns = await self.identify_patterns(feedback_list)

        for pattern in patterns:
            if pattern.type == "FALSE_POSITIVE":
                await self.handle_false_positive(pattern)
            elif pattern.type == "MISSING_ISSUE":
                await self.handle_missing_issue(pattern)

    async def handle_false_positive(self, pattern: FeedbackPattern):
        """处理误报：生成排除条件或调整规则"""

        # 分析误报的共同特征
        analysis = await self.analyze(
            examples=pattern.examples,
            prompt="""分析这些误报的共同点。
输出：
1. 误报原因
2. 建议的排除条件（XPath/正则）
3. 是否需要人工审查"""
        )

        # 自动创建规则变体进行 A/B 测试
        if analysis.confidence > 0.8:
            await self.create_rule_variant(
                base_rule=pattern.rule_id,
                exclusion=analysis.exclusion_condition
            )

    async def handle_missing_issue(self, pattern: FeedbackPattern):
        """处理漏报：生成新规则"""

        new_rule = await self.generate(
            examples=pattern.examples,
            prompt="""基于这些漏报的代码，生成一条审核规则。
格式：
- rule_id:
- description:
- condition (XPath/AST):
- severity:"""
        )

        # 提交到规则审核队列
        await self.submit_rule_for_review(new_rule)
```

**核心价值**：
- 自动分析误报/漏报模式
- 生成规则优化建议
- 自动创建 A/B 测试验证改进效果

### 3.4 Specialist Agent 示例

复杂场景可以创建专门的 Agent：

```python
# agents/distributed_tx_agent.py
class DistributedTxAgent(Agent):
    """检测分布式事务问题（需复杂推理）"""

    system_prompt = """你是分布式事务审核专家。
分析代码中的 @Transactional 边界和跨服务调用，
识别可能导致数据不一致的模式。"""

    async def review(self, context: CodeContext) -> List[Violation]:

        # 1. 找出所有事务边界
        tx_boundaries = context.find_annotations("@Transactional")

        # 2. 检查每个边界内的跨服务调用
        violations = []
        for boundary in tx_boundaries:
            service_calls = await self.find_service_calls(boundary)

            for call in service_calls:
                # LLM 推理：这是否会导致分布式事务问题？
                analysis = await self.analyze(f"""
                事务方法: {boundary.method}
                跨服务调用: {call.target_service}.{call.method}
                调用方式: {call.pattern} (同步/异步/MQ)

                这个调用是否会导致事务不一致？
                考虑因素：
                - 远程调用失败后的回滚策略
                - 异步调用的最终一致性
                - 超时和重试机制
                """)

                if analysis.has_issue:
                    violations.append(Violation(
                        rule_id="CUSTOM_DISTRIBUTED_TX",
                        message=analysis.explanation,
                        suggestion=analysis.fix_suggestion
                    ))

        return violations
```

---

## 4. 渐进式迁移策略

### 4.1 四阶段迁移路径

| 阶段 | 目标 | 范围 | 工作量 |
|------|------|------|--------|
| **Stage 1** | 编排层 Agent 化 | ReviewOrchestrator | 中 |
| **Stage 2** | 上下文管理 Agent 化 | ContextManager | 中 |
| **Stage 3** | 反馈学习 Agent 化 | L10 全替换 | 中高 |
| **Stage 4** | 复杂规则 Agent 化 | L4-L7 部分场景 | 高 |

### 4.2 Stage 1：编排层 Agent 化

**目标**：用 Agent 替换硬编码的审核策略

**改动点**：
- 保留 L1-L9 规则引擎不变
- 用 `ReviewOrchestrator` Agent 替代 `getLayersForTrigger()` 硬编码逻辑
- 实现动态分层决策

**收益**：
- 策略可配置化，无需发版即可调整
- 支持更细粒度的触发条件

### 4.3 Stage 2：上下文管理 Agent 化

**目标**：解决大库上下文问题

**改动点**：
- 引入 `ContextManager` Agent
- 实现智能上下文选择和分层加载
- 集成向量检索索引

**收益**：
- 支持超大代码库（百万行级别）
- 控制 LLM token 消耗成本

### 4.4 Stage 3：反馈学习 Agent 化

**目标**：实现真正的持续学习

**改动点**：
- 用 `FeedbackLearningAgent` 替代原有 `RuleOptimizer`
- 实现自动模式识别和规则生成
- 集成 A/B 测试框架

**收益**：
- 规则质量自动优化
- 减少人工维护成本

### 4.5 Stage 4：复杂规则 Agent 化

**目标**：复杂场景用 Agent 替代规则

**适用场景**：
- 分布式事务检测
- 复杂业务逻辑校验
- 跨文件架构模式识别

**策略**：
- 简单规则（语法、命名）保持 DSL
- 复杂场景（事务、并发）用 Specialist Agent
- 通过反馈数据决定哪些规则值得 Agent 化

---

## 5. 技术选型结论

### 5.1 为什么选 Claude Agent SDK 而非 LangChain/CrewAI

| 考量因素 | LangChain/CrewAI | Claude Agent SDK |
|---------|-----------------|------------------|
| **代码理解深度** | 通用 LLM，需大量 prompt engineering | Claude 原生代码能力强 |
| **长上下文** | 需自行管理分块 | 200K 原生支持 |
| **工具生态** | 丰富但泛化 | MCP 标准化对接 |
| **可控性** | 抽象层较厚 | 底层可控 |
| **与现有系统集成** | 需大量适配 | 通过 MCP 标准化对接 |
| **结构化输出** | 需额外处理 | 原生支持 JSON/XML |
| **子 Agent 机制** | 自行实现 | 原生支持 |

### 5.2 推荐技术栈

```
┌─────────────────────────────────────────────────────────────┐
│                    Agent 层                                   │
│  Claude Agent SDK + MCP (Model Context Protocol)            │
├─────────────────────────────────────────────────────────────┤
│                    规则引擎层                                 │
│  PMD + SpotBugs + ArchUnit (保留现有投资)                    │
├─────────────────────────────────────────────────────────────┤
│                    上下文层                                   │
│  Caffeine (本地缓存) + Redis (分布式) + 向量检索 (Milvus)     │
├─────────────────────────────────────────────────────────────┤
│                    索引层                                     │
│  调用图索引 + 数据流图 + 符号表                               │
└─────────────────────────────────────────────────────────────┘
```

### 5.3 风险与应对

| 风险 | 影响 | 应对策略 |
|------|------|---------|
| LLM 调用成本高 | 高 | 1. 分层加载控制 token 消耗<br>2. 简单规则仍用 DSL<br>3. 结果缓存复用 |
| LLM 输出不确定 | 中 | 1. 结构化输出约束<br>2. 结果校验机制<br>3. 回退到规则引擎 |
| 引入外部依赖 | 中 | 1. 抽象 Agent 接口<br>2. 保持规则引擎可独立运行 |
| 学习曲线 | 低 | 1. 渐进式迁移<br>2. 保留原有团队技能栈 |

---

## 6. 参考资源

### 6.1 相关文档

- [Claude Agent SDK 官方文档](https://docs.claude.com/en/api/agent-sdk/overview)
- [MCP (Model Context Protocol) 规范](https://modelcontextprotocol.io/)
- [implementation.md](../implementation.md) - 现有规则引擎实现
- [plan.md](../plan.md) - 10层审核维度设计

### 6.2 对比框架

- [LangChain 文档](https://python.langchain.com/)
- [CrewAI 文档](https://docs.crewai.com/)
- [AutoGen (Microsoft)](https://microsoft.github.io/autogen/)

### 6.3 开源工具

- [PMD](https://pmd.github.io/) - Java 静态分析
- [SpotBugs](https://spotbugs.github.io/) - Java 字节码分析
- [ArchUnit](https://www.archunit.org/) - 架构测试

---

## 附录：术语表

| 术语 | 说明 |
|------|------|
| **Agent** | 能够自主决策、调用工具完成任务的智能体 |
| **MCP** | Model Context Protocol，Anthropic 提出的标准化协议 |
| **L1-L10** | 代码审核的 10 个维度，详见 [plan.md](../plan.md) |
| **DSL** | Domain Specific Language，领域特定语言（如规则 YAML）|
| **AST** | Abstract Syntax Tree，抽象语法树 |

---

*文档结束*
