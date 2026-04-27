<!-- @format -->

# 代码审核系统 (Code Review System)

后 AI 时代的智能代码审核方案，从个人本地审核到团队 CI 集成的完整解决方案。

---

## 文档导航

### 架构设计

| 文档                                                                 | 说明                                                     |
| -------------------------------------------------------------------- | -------------------------------------------------------- |
| [审核中台架构设计](cr-platform-design.md)                            | Gateway、Core Engine、审核引擎、规则系统                 |
| [变更审核](review/change-review.md)                                  | 针对代码变更的实时审核（L1-L8）                          |
| [全量巡检](review/full-scan.md)                                      | 代码库健康度定时检查（L9）                               |
| [持续学习](review/continuous-learning.md)                            | 基于反馈优化审核质量（L10）                              |
| [Java 后端审核指南](guides/java/README.md)                           | 面向其他 Java 项目的可复用审核资产                       |
| [Java Code Review Skill](skills/java-code-review/SKILL.md)           | Codex `$java-code-review` Skill 包                       |
| [Java 审核入口](guides/java/commands/code-review-java.md)            | Claude Code `/code-review-java` 入口命令                 |
| [Java 变更审核命令](guides/java/commands/code-review-java-change.md) | Claude Code `/code-review-java-change`，只审核本次变更   |
| [Java 全量巡检命令](guides/java/commands/code-review-java-scan.md)   | Claude Code `/code-review-java-scan`，扫描项目系统性风险 |
| [Java 规则卡片](guides/java/rules/README.md)                         | 轻量规则 ID、置信度和误报治理约定                        |
| [.code-review.yml 配置规范](guides/java/config/code-review-yml.md)   | 目标 Java 项目的轻量审核配置                             |
| [JSON 报告格式](guides/java/report/json-report.md)                   | 面向 CI、Bot 和统计分析的结构化输出                      |

---

## 目录结构

```
cr/
├── README.md                   # 本文件
├── cr-platform-design.md       # 审核中台架构设计
├── z_node.md                   # 初始思路记录
├── guides/                    # 可复用审核指南
│   └── java/                  # Java 后端审核命令、规则、配置和报告规范
├── skills/                    # 可复用 Codex Skills
│   └── java-code-review/      # Java 后端代码审核 Skill
└── review/                    # 审核类型
    ├── change-review.md       # 变更审核
    ├── full-scan.md           # 全量巡检
    └── continuous-learning.md  # 持续学习
```

---

## 核心设计理念

| 理念                 | 说明                               |
| -------------------- | ---------------------------------- |
| **分层解耦**         | 10 个审核维度独立，可单独启用/禁用 |
| **渐进增强**         | 从简单规则开始，逐步叠加复杂分析   |
| **先个人后团队**     | 先在本地打磨审核质量，再接入 CI    |
| **宁可漏报不可误报** | 宽松起步，确保每条问题都是真问题   |

---

## TODO

| 事项              | 优先级 | 说明                       |
| ----------------- | ------ | -------------------------- |
| 10 层审核详细设计 | 中     | 各维度的详细设计与实现方案 |
| Java 规则库       | 高     | Java 46 条审核规则         |
| TypeScript 审核   | 中     | Vue/TS 前端审核            |
| Flutter 审核      | 中     | Dart/Flutter 审核          |
| Skill CLI 工具    | 高     | 本地审核 CLI               |
| GitLab MR 集成    | 中     | Webhook 自动审核           |

---

## License

MIT
