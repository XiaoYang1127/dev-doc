<!-- @format -->

# AI 辅助软件开发体系

后 AI 时代的软件开发质量保障体系，覆盖代码审核、开发规范、测试策略三大维度。

---

## 项目简介

本项目是一套完整的 AI 辅助软件开发文档体系，旨在：

- **提升代码质量** - 通过分层代码审核，在开发阶段发现潜在问题
- **统一开发规范** - 建立跨技术栈的标准化开发实践
- **保障交付质量** - 构建可落地的分层测试策略

> **核心理念**：后 AI 时代，代码生成很快，但质量保障是核心挑战。

---

## 三大模块

### 1. 代码审核系统 ([cr/](cr/))

后 AI 时代的智能代码审核方案，从个人本地审核到团队 CI 集成的完整解决方案。

**当前能力**：

- Java 代码审核（4 个高价值检查项）
- Claude Code Skill 集成

**核心特性**：

- 10 层审核架构（L1 变更感知 → L10 反馈学习）
- 分层解耦，可单独启用/禁用
- 宁可漏报，不可误报

[查看详情 →](cr/README.md)

---

### 2. 开发规范 ([dev/](dev/))

多技术栈开发最佳实践，为 AI 辅助开发提供上下文规范。

**覆盖技术栈**：
| 技术栈 | 文档 | 说明 |
|--------|------|------|
| Vue 3 | [vue3-claude.md](dev/frontend/vue3-claude.md) | Vue 3 + TypeScript 开发规范 |
| Flutter | [flutter-claude.md](dev/mobile/flutter-claude.md) | Flutter 跨平台开发规范 |
| Java 后端 | [backend-claude.md](dev/backend/backend-claude.md) | Spring Boot 开发规范 |

**规范内容**：

- 项目架构设计（Feature-First + API-First）
- 代码风格与命名规范
- 分层职责与依赖关系
- 错误处理与安全规范

---

### 3. 测试体系 ([test/](test/))

完整的软件测试体系设计与实践指南。

**核心文档**：
| 文档 | 说明 |
|------|------|
| [架构设计](test/platform-design.md) | 分层测试策略、质量指标体系 |
| [策略指南](test/reference/strategy.md) | 11 种测试策略选择指南 |
| [业界调研](test/reference/benchmark.md) | 国内外大厂测试实践调研 |
| [Java 单测](test/guides/java.md) | Java 单元测试完整方案 |

**测试目标**：

- 开发阶段发现 70%+ 的问题
- AI 生成代码的同时生成配套测试
- 建立可落地的测试流程

[查看详情 →](test/README.md)

---

## 快速开始

### 开发新项目

1. **选择技术栈** → 查看 [dev/](dev/) 对应规范
2. **编码时** → 使用 `/cr` 进行代码审核
3. **测试时** → 参考 [test/](test/) 建立测试体系

### 使用代码审核

```bash
cd your-java-project

# 在 Claude Code 中使用
# 先将 cr/guides/java/commands/*.md 复制到目标项目 .claude/commands/
/code-review-java-change             # 审核当前 Java git 变更
/code-review-java-scan               # 全量巡检 Java 项目
```

---

## 目录结构

```
dev-doc/                       # 本项目根目录
├── README.md                  # 本文档：项目总览
├── CLAUDE.md                  # Claude Code 全局配置
│
├── cr/                        # 【代码审核】模块
│   ├── README.md              # 模块总览
│   ├── cr-platform-design.md  # 审核中台架构设计
│   ├── guides/                # 可复用审核指南
│   ├── skills/                # 可复用 Codex Skills
│   └── review/                # 审核类型设计
│
├── dev/                       # 【开发规范】模块
│   ├── README.md              # 模块总览
│   ├── backend/               # Java 后端规范
│   ├── frontend/              # Vue 3 前端规范
│   ├── mobile/                # Flutter 移动端规范
│   └── steering/              # 通用开发规范
│
└── test/                      # 【测试体系】模块
    ├── README.md              # 模块总览
    ├── platform-design.md     # 测试中台架构设计
    ├── guides/                # 实现指南
    └── reference/             # 参考资料与策略
```

---

## 演进路线图

```
Phase 1: 基础能力（当前）
   ├── Java 代码审核（宽松模式）
   ├── Java 单元测试方案
   └── Vue/Flutter/Java 开发规范

Phase 2: 能力扩展（进行中）
   ├── Vue/Flutter 代码审核
   ├── Python/TypeScript 单元测试
   └── TestContainers 集成测试

Phase 3: 团队集成
   ├── GitLab MR 自动审核
   ├── CI/CD 测试流水线
   └── 质量门禁与报告

Phase 4: 持续优化
   ├── 审核反馈学习
   ├── 规范自动优化
   └── 智能测试生成
```

---

## 贡献指南

1. **发现问题** - 提交 Issue 描述问题
2. **改进文档** - Fork 后修改，提交 PR
3. **反馈实践** - 记录审核效果并同步完善 [cr/](cr/) 相关文档

---

## License

MIT

---

_文档维护：Claude Code_
_最后更新：2026-03-26_
