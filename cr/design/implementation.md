# 代码审核系统实现方案

> 本文档描述如何将**审核维度**、**规则定义**、**规则执行**这一套打通的具体实现方案。
>
> **配套文件**：
> - [plan.md](./plan.md) - 系统架构概览（10层审核维度设计）
> - [rules/rule-framework.md](./rules/rule-framework.md) - 规则管理框架（规则定义规范）
> - [rules/languages/java.md](./rules/languages/java.md) - Java完整规则库（46条规则）
>
> **本文档重点**：规则执行引擎、开源工具集成、L10反馈学习实现

---

## 目录

1. [整体架构](#1-整体架构)
2. [核心组件实现](#2-核心组件实现)
3. [规则执行流程](#3-规则执行流程)
4. [开源工具集成](#4-开源工具集成)
5. [反馈学习层 (L10)](#5-反馈学习层-l10)
6. [快速启动](#6-快速启动)

---

## 1. 整体架构

### 1.1 三层打通架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         接入层 (API/Gateway)                     │
│              GitLab Webhook / CLI / IDE Plugin                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      审核编排引擎 (Orchestrator)                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │  维度选择器  │→ │  规则加载器  │→ │  执行调度器  │→ │ 结果聚合 │ │
│  │  (Layer)    │  │  (Registry) │  │ (Scheduler) │  │ (Merge) │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   L1 变更感知  │    │   L2 片段审核  │    │   L3 上下文   │
│  (git diff)   │    │  (Regex/AST)  │    │  (AST分析)    │
└───────────────┘    └───────────────┘    └───────────────┘
        │                     │                     │
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   L4 架构审核  │    │   L5 调用链   │    │   L6 数据流   │
│  (依赖分析)    │    │  (全局搜索)   │    │  (注解分析)   │
└───────────────┘    └───────────────┘    └───────────────┘
        │                     │                     │
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   L7 安全审核  │    │   L8 性能审核  │    │   L9 全量扫描  │
│  (污点分析)    │    │  (模式识别)   │    │  (Sonar集成)  │
└───────────────┘    └───────────────┘    └───────────────┘
```

### 1.2 数据流转

```
规则定义 (YAML) → 规则注册表 → 执行引擎 → 统一报告格式 (SARIF)
```

---

## 2. 核心组件实现

### 2.1 规则注册表

**与 rule-framework 的关系**：
- rule-framework.md 定义了规则的**结构规范**（YAML格式、字段定义、生命周期）
- RuleRegistry 负责将 YAML 规则加载到内存并管理

加载 [rule-framework.md](./rules/rule-framework.md) 定义的 YAML 规则：

```java
@Component
public class RuleRegistry {

    private final Map<String, Rule> rules = new ConcurrentHashMap<>();
    private final Map<Integer, List<Rule>> layerRules = new HashMap<>();

    @PostConstruct
    public void loadRules() {
        loadBuiltinRules();   // 加载内置规则
        loadProjectRules();   // 加载项目自定义规则
    }

    public List<Rule> getRulesForLayer(int layer) {
        return layerRules.getOrDefault(layer, Collections.emptyList());
    }

    public RuleExecutor getExecutor(Rule rule) {
        return switch (rule.getCondition().getType()) {
            case REGEX -> new RegexRuleExecutor();
            case AST -> new AstRuleExecutor();
            case SEMANTIC -> new SemanticRuleExecutor();
            case AI -> new AiRuleExecutor();
        };
    }
}
```

### 2.2 统一规则执行接口

```java
public interface RuleExecutor {
    List<Violation> execute(Rule rule, CodeContext context);
}
```

### 2.3 AST 规则执行器（基于 JavaParser）

```java
@Component
public class AstRuleExecutor implements RuleExecutor {

    @Override
    public List<Violation> execute(Rule rule, CodeContext context) {
        String xpath = rule.getCondition().getPattern();

        NodeList<Node> matches = context.getAst().findAll(MethodDeclaration.class)
            .stream()
            .filter(node -> matchesXpath(node, xpath))
            .collect(Collectors.toList());

        return matches.stream()
            .map(node -> createViolation(rule, node))
            .collect(Collectors.toList());
    }
}
```

### 2.4 语义规则执行器（基于污点分析）

```java
@Component
public class SemanticRuleExecutor implements RuleExecutor {

    @Override
    public List<Violation> execute(Rule rule, CodeContext context) {
        DataFlowGraph dfg = buildDataFlowGraph(context);

        List<DataFlowPath> paths = dfg.findPaths(
            rule.getCondition().getSourcePattern(),
            rule.getCondition().getSinkPattern()
        );

        return paths.stream()
            .filter(path -> !hasSanitizer(path, rule.getCondition().getSanitizers()))
            .map(path -> createViolation(rule, path))
            .collect(Collectors.toList());
    }
}
```

### 2.5 审核编排引擎

```java
@Service
public class ReviewOrchestrator {

    @Autowired
    private RuleRegistry ruleRegistry;

    public ReviewReport execute(ReviewRequest request) {
        ReviewReport report = new ReviewReport();

        // 根据触发场景选择维度
        List<Integer> layers = getLayersForTrigger(request.getTriggerType());

        for (int layer : layers) {
            CodeContext context = buildContext(layer, request);
            List<Rule> rules = ruleRegistry.getRulesForLayer(layer);

            List<Violation> violations = rules.parallelStream()
                .map(rule -> executeRule(rule, context))
                .flatMap(List::stream)
                .collect(Collectors.toList());

            report.addViolations(violations);
        }

        return postProcess(report);
    }

    private List<Integer> getLayersForTrigger(TriggerType type) {
        return switch (type) {
            case PRE_COMMIT -> List.of(1, 2, 3);
            case MR_CREATED -> List.of(1, 2, 3, 4, 5, 6);
            case NIGHTLY -> List.of(9);
        };
    }
}
```

---

## 3. 规则执行流程

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  触发审核  │ → │ 选择维度  │ → │ 加载规则  │ → │ 执行检测  │
└──────────┘    └──────────┘    └──────────┘    └────┬─────┘
     │                                               │
     │         ┌──────────┐    ┌──────────┐        │
     └──────── │ 去重分级  │ ← │ 结果聚合  │ ←──────┘
               └────┬─────┘    └──────────┘
                    │
               ┌────┴─────┐
               │ 输出报告  │
               └──────────┘
```

---

## 4. 开源工具集成

### 4.1 工具选型对比

| 工具 | 核心能力 | 对应维度 | 接入建议 |
|------|----------|----------|----------|
| [PMD](https://pmd.github.io/) | AST 分析 | L2, L3, L4, L8, L9 | 首选，易扩展 |
| [SpotBugs](https://spotbugs.github.io/) | 字节码分析 | L2, L5, L7 | 安全检测 |
| [Checkstyle](https://checkstyle.sourceforge.io/) | 源码分析 | L9 | 编码规范 |
| [SonarQube](https://www.sonarqube.org/) | 综合平台 | L1-L9 | 企业级门户 |
| [P3C](https://github.com/alibaba/p3c) | PMD扩展 | L4 | Java分层规范 |

### 4.2 推荐技术栈

```
┌─────────────────────────────────────────────────────────────┐
│                      代码审核系统                             │
├─────────────────────────────────────────────────────────────┤
│  规则层：PMD (XPath + Java) + 自定义规则                      │
│  安全层：SpotBugs + FindSecBugs 插件                         │
│  架构层：ArchUnit + P3C (分层检测)                           │
│  AI 层：Claude API / OpenAI API (L6复杂场景)                │
└─────────────────────────────────────────────────────────────┘
```

### 4.3 PMD 集成示例

```java
public class PmdRuleAdapter implements RuleExecutor {

    private final PMDConfiguration pmdConfig;

    public PmdRuleAdapter() {
        this.pmdConfig = new PMDConfiguration();
        pmdConfig.setRuleSets("rulesets/java/quickstart.xml");
    }

    @Override
    public List<Violation> execute(Rule rule, CodeContext context) {
        try (RuleContext ctx = new RuleContext()) {
            List<RuleViolation> pmdViolations = new ArrayList<>();
            ctx.setReport(new Report(pmdViolations));

            RuleSet ruleSet = loadRuleSet(rule);
            ruleSet.apply(context.getAst(), ctx);

            return pmdViolations.stream()
                .map(v -> convertToViolation(v, rule))
                .collect(Collectors.toList());
        }
    }
}
```

### 4.4 SpotBugs 集成示例

```java
public class SpotBugsAdapter implements RuleExecutor {

    @Override
    public List<Violation> execute(Rule rule, CodeContext context) {
        FindBugs2 engine = new FindBugs2();
        engine.setProject(context.getProject());
        engine.setDetectorFactoryCollection(
            DetectorFactoryCollection.instance()
        );

        engine.execute();

        BugCollection bugs = engine.getBugCollection();
        return bugs.stream()
            .map(b -> convertToViolation(b, rule))
            .collect(Collectors.toList());
    }
}
```

---

## 5. 反馈学习层 (L10)

反馈学习层是代码审核系统的持续优化引擎，通过收集开发者反馈来改进规则质量。

### 5.1 反馈闭环架构

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  审核执行  │ → │ 开发者反馈 │ → │ 数据分析  │ → │ 规则优化  │
└────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘
     │               │               │               │
     │               ▼               │               │
     │          ┌──────────┐         │               │
     └────────→ │ 结果展示  │ ←───────┘               │
                └──────────┘                         │
                                                      ▼
                                               ┌──────────┐
                                               │ A/B测试  │
                                               └────┬─────┘
                                                    │
                                               ┌────┴─────┐
                                               │ 规则发布  │
                                               └──────────┘
```

### 5.2 数据模型

```java
@Entity
public class ReviewFeedback {
    @Id
    private String id;

    // 关联的审核结果
    private String violationId;
    private String ruleId;

    // 反馈类型：CONFIRMED(确认有效) / FALSE_POSITIVE(误报) / IGNORED(忽略)
    @Enumerated(EnumType.STRING)
    private FeedbackType type;

    // 反馈人信息
    private String reviewer;
    private String comment;
    private Instant createdAt;

    // 修复追踪
    private boolean isFixed;
    private String fixCommitHash;
    private Instant fixedAt;
}

@Entity
public class RuleMetrics {
    @Id
    private String ruleId;

    // 统计指标
    private long hitCount;              // 命中次数
    private long confirmedCount;        // 确认有效数
    private long falsePositiveCount;    // 误报数
    private long ignoredCount;          // 忽略数

    // 计算指标
    private double falsePositiveRate;   // 误报率 = FP / (FP + TP)
    private double fixRate;             // 修复率 = 已修复 / 确认有效
    private double avgFixTimeHours;     // 平均修复时间

    private Instant lastHitAt;
    private Instant updatedAt;
}
```

### 5.3 反馈收集服务

```java
@Service
public class FeedbackCollector {

    @Autowired
    private ReviewFeedbackRepository feedbackRepo;

    @Autowired
    private RuleMetricsRepository metricsRepo;

    @Autowired
    private GitLabApi gitlabApi;

    // 处理 MR 评论中的反馈标记
    @EventListener
    public void onMrComment(MrCommentEvent event) {
        String comment = event.getComment();
        String violationId = extractViolationId(comment);

        if (comment.contains("/false-positive")) {
            saveFeedback(violationId, FeedbackType.FALSE_POSITIVE, event);
        } else if (comment.contains("/confirmed")) {
            saveFeedback(violationId, FeedbackType.CONFIRMED, event);
        } else if (comment.contains("/ignore")) {
            saveFeedback(violationId, FeedbackType.IGNORED, event);
        }
    }

    // 追踪修复状态
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void trackFixStatus() {
        List<ReviewFeedback> pending = feedbackRepo.findByIsFixedFalse();

        for (ReviewFeedback feedback : pending) {
            String mrId = feedback.getMrId();
            MrChanges changes = gitlabApi.getMrChanges(mrId);

            // 检查问题是否已被修复
            boolean isFixed = checkIfFixed(feedback, changes);
            if (isFixed) {
                feedback.setFixed(true);
                feedback.setFixedAt(Instant.now());
                feedbackRepo.save(feedback);

                // 更新规则指标
                updateRuleMetrics(feedback.getRuleId());
            }
        }
    }

    private boolean checkIfFixed(ReviewFeedback feedback, MrChanges changes) {
        // 检查代码是否修改了问题所在行
        // 或删除了相关代码
        return changes.isLineModifiedOrDeleted(
            feedback.getFilePath(),
            feedback.getLineNumber()
        );
    }
}
```

### 5.4 规则优化引擎

```java
@Service
public class RuleOptimizer {

    @Autowired
    private RuleMetricsRepository metricsRepo;

    @Autowired
    private RuleRegistry ruleRegistry;

    // 每周分析规则质量
    @Scheduled(cron = "0 0 3 ? * MON") // 每周一凌晨3点
    public void analyzeRuleQuality() {
        List<RuleMetrics> allMetrics = metricsRepo.findAll();

        for (RuleMetrics metrics : allMetrics) {
            String ruleId = metrics.getRuleId();

            // 误报率过高
            if (metrics.getFalsePositiveRate() > 0.30) {
                handleHighFalsePositive(ruleId, metrics);
            }

            // 90天无命中
            if (isStaleRule(metrics)) {
                handleStaleRule(ruleId);
            }

            // 修复率过低
            if (metrics.getFixRate() < 0.50) {
                handleLowFixRate(ruleId, metrics);
            }
        }
    }

    private void handleHighFalsePositive(String ruleId, RuleMetrics metrics) {
        // 1. 降低规则置信度
        Rule rule = ruleRegistry.getRule(ruleId);
        double newConfidence = Math.max(0.5, rule.getMeta().getConfidence() - 0.1);
        rule.getMeta().setConfidence(newConfidence);

        // 2. 生成优化建议报告
        OptimizationSuggestion suggestion = new OptimizationSuggestion();
        suggestion.setRuleId(ruleId);
        suggestion.setIssue("误报率 " + metrics.getFalsePositiveRate() * 100 + "%");
        suggestion.setAction("建议添加排除条件或调整匹配模式");
        suggestion.setSampleFalsePositives(
            getSampleFalsePositives(ruleId, 5)
        );

        notifyRuleMaintainer(suggestion);
    }

    private void handleStaleRule(String ruleId) {
        // 标记为废弃候选
        Rule rule = ruleRegistry.getRule(ruleId);
        rule.getMeta().setStatus(RuleStatus.DEPRECATED_CANDIDATE);

        log.info("规则 {} 90天无命中，建议废弃", ruleId);
    }

    private void handleLowFixRate(String ruleId, RuleMetrics metrics) {
        // 修复率低可能意味着建议质量差
        // 需要人工审查规则提示语
        log.warn("规则 {} 修复率仅 {}%，建议审查提示语质量",
            ruleId, metrics.getFixRate() * 100);
    }
}
```

### 5.5 A/B测试框架

```java
@Service
public class RuleABTestManager {

    @Autowired
    private RuleRegistry ruleRegistry;

    // 创建A/B测试
    public ABTest createTest(String ruleId, RuleVariant variant) {
        ABTest test = new ABTest();
        test.setId(UUID.randomUUID().toString());
        test.setRuleId(ruleId);
        test.setControlGroupRatio(0.5);  // 对照组50%
        test.setTreatmentGroupRatio(0.5); // 实验组50%
        test.setStartTime(Instant.now());
        test.setDurationDays(14);
        test.setStatus(ABTestStatus.RUNNING);

        // 实验组使用变体规则
        variant.setBaseRuleId(ruleId);
        ruleRegistry.registerVariant(variant);

        return test;
    }

    // 根据用户分配到不同组
    public Rule getRuleForUser(String ruleId, String userId) {
        ABTest activeTest = getActiveTest(ruleId);

        if (activeTest == null) {
            return ruleRegistry.getRule(ruleId);
        }

        // 基于用户ID哈希分配
        boolean isTreatment = isTreatmentGroup(userId, activeTest);

        if (isTreatment) {
            return ruleRegistry.getVariant(activeTest.getVariantId());
        } else {
            return ruleRegistry.getRule(ruleId);
        }
    }

    // 评估A/B测试结果
    @Scheduled(cron = "0 0 4 ? * MON")
    public void evaluateTests() {
        List<ABTest> tests = getTestsToEvaluate();

        for (ABTest test : tests) {
            RuleMetrics controlMetrics = metricsRepo.findByRuleIdAndGroup(
                test.getRuleId(), "control"
            );
            RuleMetrics treatmentMetrics = metricsRepo.findByRuleIdAndGroup(
                test.getVariantId(), "treatment"
            );

            // 评估指标
            double fpImprovement = controlMetrics.getFalsePositiveRate()
                - treatmentMetrics.getFalsePositiveRate();

            ABTestDecision decision;
            if (fpImprovement > 0.03) { // 误报率下降3%以上
                decision = ABTestDecision.ROLL_OUT;
            } else if (fpImprovement < -0.05) { // 误报率上升5%以上
                decision = ABTestDecision.ROLL_BACK;
            } else {
                decision = ABTestDecision.CONTINUE;
            }

            executeDecision(test, decision);
        }
    }
}
```

### 5.6 规则动态更新

```java
@Service
public class RuleDynamicUpdater {

    @Autowired
    private RuleRegistry ruleRegistry;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // 监听规则配置变更
    @NacosConfigListener(dataId = "code-review-rules", groupId = "default")
    public void onRuleConfigChange(String config) {
        List<Rule> newRules = parseRules(config);

        for (Rule newRule : newRules) {
            Rule oldRule = ruleRegistry.getRule(newRule.getId());

            if (oldRule == null) {
                // 新增规则
                ruleRegistry.register(newRule);
                eventPublisher.publishEvent(new RuleAddedEvent(newRule));
            } else if (isSignificantChange(oldRule, newRule)) {
                // 重大变更，走A/B测试
                startABTestForChange(oldRule, newRule);
            } else {
                // 小幅更新，热加载
                ruleRegistry.update(newRule);
                eventPublisher.publishEvent(new RuleUpdatedEvent(newRule));
            }
        }
    }

    // 热加载规则（无需重启）
    public void hotReload(String ruleId) {
        Rule rule = loadRuleFromConfig(ruleId);
        ruleRegistry.update(rule);

        // 清空该规则的缓存结果
        reviewCache.invalidateByRule(ruleId);

        log.info("规则 {} 已热加载", ruleId);
    }
}
```

### 5.7 反馈数据可视化

```java
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @GetMapping("/dashboard")
    public FeedbackDashboard getDashboard() {
        FeedbackDashboard dashboard = new FeedbackDashboard();

        // 整体指标
        dashboard.setTotalViolations(reviewRepo.count());
        dashboard.setConfirmedRate(calculateConfirmedRate());
        dashboard.setFalsePositiveRate(calculateFPRate());
        dashboard.setAvgFixTime(calculateAvgFixTime());

        // 规则质量排行
        dashboard.setTopFalsePositiveRules(
            metricsRepo.findTopFalsePositiveRules(10)
        );

        // 趋势图数据
        dashboard.setTrendData(
            metricsRepo.getTrendData(Duration.ofDays(30))
        );

        return dashboard;
    }

    @GetMapping("/rules/{ruleId}/detail")
    public RuleFeedbackDetail getRuleFeedbackDetail(@PathVariable String ruleId) {
        RuleFeedbackDetail detail = new RuleFeedbackDetail();

        detail.setRuleId(ruleId);
        detail.setMetrics(metricsRepo.findByRuleId(ruleId));
        detail.setRecentFeedback(
            feedbackRepo.findRecentByRuleId(ruleId, 20)
        );
        detail.setSampleViolations(
            violationRepo.findSamplesByRuleId(ruleId, 10)
        );

        return detail;
    }
}
```

---

## 6. 快速启动

### 5.1 Maven 依赖

```xml
<dependency>
    <groupId>net.sourceforge.pmd</groupId>
    <artifactId>pmd-java</artifactId>
    <version>7.0.0</version>
</dependency>

<dependency>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs</artifactId>
    <version>4.8.6</version>
</dependency>

<dependency>
    <groupId>com.github.javaparser</groupId>
    <artifactId>javaparser-symbol-solver-core</artifactId>
    <version>3.25.8</version>
</dependency>
```

### 5.2 最小可用示例

```java
public class QuickStartReview {

    public static void main(String[] args) throws Exception {
        // 1. 加载代码
        String sourceCode = Files.readString(Path.of("UserService.java"));

        // 2. 解析 AST
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(sourceCode);

        // 3. 执行 XPath 规则
        String xpath = "//MethodDeclaration[contains(@name, 'Service')]" +
                       "//MethodCallExpr[@name in ['submit', 'execute']]";

        List<Node> matches = result.getResult().get()
            .findAll(MethodDeclaration.class)
            .stream()
            .filter(node -> node.toString().matches(".*submit.*|.*execute.*"))
            .collect(Collectors.toList());

        // 4. 输出结果
        matches.forEach(node -> {
            System.out.println("发现事务内异步调用: " +
                node.getBegin().get().line);
        });
    }
}
```

---

## 参考资源

- [PMD GitHub](https://github.com/pmd/pmd)
- [SpotBugs GitHub](https://github.com/spotbugs/spotbugs)
- [P3C (阿里巴巴Java规范)](https://github.com/alibaba/p3c)
- [ArchUnit 架构测试](https://www.archunit.org/)
