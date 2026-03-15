# 代码审核助手 (Code Review Assistant)

后 AI 时代的智能代码审核方案。

## 目标

让 AI 审核更准确、更少出错、更符合团队预期，从而替代人工审核效率较低的问题。

## 当前阶段

| 维度 | 当前状态 |
|------|---------|
| **部署范围** | 个人本地（通过 Claude Code Skill）|
| **技术栈** | Java（Vue、Flutter 后续支持）|
| **审核严格度** | 宽松建议型（只标记高价值问题）|

## 快速开始

### 1. 安装 Skill

```bash
# 进入你的项目目录
cd your-java-project

# 创建 skill 软链接（或使用 Claude Code 的 skill 管理功能）
ln -s /path/to/cr/skill .claude/skills/code-review
```

### 2. 使用审核

在 Claude Code 中输入：

```
/cr                    # 审核当前 git 变更
/cr <文件路径>          # 审核指定文件
```

### 3. 查看报告

AI 将输出结构化的审核报告：

```markdown
## 代码审核报告

### 概览
- 审核文件：3 个
- 发现问题：严重 1 | 警告 2 | 建议 0

---

### UserService.java:42
**等级**: 🔴 严重
**类型**: 事务安全
**问题**: 事务内调用远程支付服务，可能导致连接池耗尽
...
```

## 审核范围

### 当前检查项

| 检查项 | 说明 | 优先级 |
|--------|------|--------|
| ✅ 分层越界 | Controller 直接操作 Repository、PO 直接返回前端等 | P0 |
| ✅ 事务安全 | @Transactional 内的远程调用/异步操作 | P0 |
| ✅ NPE 风险 | 可能空指针的场景 | P1 |
| ✅ 明显逻辑缺陷 | 死代码、资源未关闭、错误比较等 | P1 |

### 暂不检查（宽松阶段）

- 代码风格（命名、缩进、空格）
- 缺少注释/JavaDoc
- 魔法数字
- 方法/类长度
- 圈复杂度

## 项目结构

```
cr/
├── skill/                       # Claude Code Skill
│   ├── SKILL.md                 # 主入口
│   ├── java-review.md           # Java 审核规范
│   └── examples/                # 正例和反例
│       ├── good/
│       └── bad/
└── README.md                    # 本文件
```

## 演进路线

```
Phase 1: 个人本地（当前）
   └── Java 宽松审核

Phase 2: 个人本地增强
   ├── Java 严格审核（开启风格检查）
   ├── Vue 审核
   └── Flutter 审核

Phase 3: 团队 CI 集成
   └── GitLab MR 自动审核 + 评论通知

Phase 4: 持续学习
   └── 反馈闭环 + 规范自动优化
```

## 反馈与改进

审核结果有误？在 `skill/feedback/review-log.md` 中记录：

```markdown
## 2024-XX-XX

### 误报案例
- 文件：XxxService.java
- 问题：AI 标记了 xx，但实际是正确的
- 原因：...

### 漏检案例
- 文件：XxxController.java
- 问题：AI 未检测到 xx 问题
- 建议：...
```

## 设计原则

1. **先个人后团队** - 先在本地打磨审核质量，再接入 CI
2. **先 Java 后其他** - 专注一个技术栈做到极致，再扩展
3. **先宽松后严格** - 宁可漏报不可误报，逐步收紧

## License

MIT
