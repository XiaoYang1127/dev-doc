<!-- @format -->

# AI 开发约束体系设计

**日期**：2026-03-29
**状态**：草案

---

## 一、定位与目标

### 1.1 核心定位

建立一套让 AI 生成的代码**符合团队预期**的约束体系，核心是"让 AI 按照既定要求来生成"。

### 1.2 目标

| 目标 | 说明 |
|------|------|
| 生成时约束 | AI 写代码时直接遵循规范，而非事后审核 |
| 可验证性 | 关键规则可自动化检查，复杂规则可 AI 审核 |
| 分层清晰 | 通用规范与项目规范分离，按需引用 |

### 1.3 设计原则

- **规则分层**：AI 注意力放在最关键的规则上
- **示例 > 规则**：用示例让 AI 学习，而非长篇规则文档
- **可验证**：无法自动化的交给 AI 审核，机器能做的机器做

---

## 二、整体架构

### 2.1 目录结构

```
用户级别 (~/.claude/)                          # 通用规范，所有项目生效
├── CLAUDE.md                                 # 用户级宪法（铁律）
└── steering/                                 # 用户级通用规范
    ├── code-style.md                        # 代码风格
    ├── api-standards.md                     # API 标准
    ├── testing-standards.md                 # 测试规范
    ├── development-principles.md            # 开发原则
    └── git-workflow.md                      # Git 工作流

项目级别 ({project}/)                         # 项目特定规范
├── CLAUDE.md                                 # 项目宪法（引用+扩展）
└── dev/steering/                            # 项目特定规范
    ├── backend-claude.md                    # 后端项目规范
    ├── vue3-claude.md                      # Vue3 项目规范
    └── flutter-claude.md                    # Flutter 项目规范
```

### 2.2 CLAUDE.md 宪法层级

```
Layer 1: 用户级 CLAUDE.md (~/.claude/CLAUDE.md)
   → 铁律 + 核心规则，AI 每次任务前读取

Layer 2: 项目级 CLAUDE.md ({project}/CLAUDE.md)
   → 继承用户级 + 项目特定规则

Layer 3: Steering Docs（被 CLAUDE.md 引用）
   → 详细规范，AI 按需读取
```

---

## 三、CLAUDE.md 设计

### 3.1 用户级宪法（~/.claude/CLAUDE.md）

**原则**：控制在 80 行以内，只放关键铁律。

```markdown
# 开发铁律

## 绝对禁止（AI 生成时主动检查）
1. ❌ Controller 直接操作 Repository（分层越界）
2. ❌ POJO/Entity 直接返回前端（必须用 DTO）
3. ❌ @Transactional 内远程调用/异步操作（事务失效）
4. ❌ 不校验入参就暴露 API（安全风险）
5. ❌ 吞掉异常不记录日志

## 强制遵循
- 所有外部输入必须校验
- 异常必须记录日志（记录原因+堆栈）
- 事务边界必须清晰

## 详细规范
- [代码风格](steering/code-style.md)
- [API 标准](steering/api-standards.md)
- [测试规范](steering/testing-standards.md)
```

### 3.2 项目级宪法（{project}/CLAUDE.md）

**原则**：继承用户级 + 补充项目特定，冲突时项目级优先。

```markdown
# {项目名} 开发规范

## 继承与覆盖
- 继承用户级 `~/.claude/CLAUDE.md` 的所有规则
- 项目特定规则**追加**到末尾
- **冲突时项目级覆盖用户级**

## 项目特定铁律
1. ❌ 禁止使用 ThreadLocal（内存泄漏风险）
2. ❌ 禁止硬编码魔法数字（必须用常量）
...

## 技术栈
- Spring Boot 3.x / JDK 21
- Vue 3 + TypeScript
- Flutter 3.x

## 详细规范（见 dev/steering/）
- [后端规范](dev/steering/backend-claude.md)
- [前端规范](dev/steering/vue3-claude.md)
- [移动端规范](dev/steering/flutter-claude.md)
```

### 3.3 引用合并算法

| 层级 | 来源 | 合并方式 |
|------|------|----------|
| Layer 1 | 用户级 CLAUDE.md | 基础规则，始终生效 |
| Layer 2 | 项目级 CLAUDE.md | 追加到 Layer 1，冲突时覆盖 |
| Layer 3 | Steering Docs | 按需引用，AI 根据文件类型/任务类型选择 |

**触发机制**：
- AI 检测到 `*.java` 文件 → 自动引用后端规范
- AI 检测到 `*.vue` / `*.ts` 文件 → 自动引用前端规范
- AI 检测到 `*.dart` 文件 → 自动引用移动端规范
- AI 任务涉及 API → 自动引用 `steering/api-standards.md`
- AI 任务涉及测试 → 自动引用 `steering/testing-standards.md`

---

## 四、Steering Docs 设计

### 4.1 用户级通用规范（~/.claude/steering/）

| 文档 | 用途 | 适用 |
|------|------|------|
| code-style.md | 代码风格、命名、反模式 | 所有语言 |
| api-standards.md | RESTful API 设计规范 | 提供 API 的项目 |
| testing-standards.md | 测试金字塔、Mock 策略 | 所有项目 |
| development-principles.md | 铁律、设计原则 | 所有项目 |
| git-workflow.md | 分支管理、提交规范 | 所有项目 |

### 4.2 项目特定规范（{project}/dev/steering/）

| 文档 | 用途 |
|------|------|
| backend-claude.md | 后端分层、事务、依赖管理 |
| vue3-claude.md | Vue3 组件、状态管理、API 层 |
| flutter-claude.md | Flutter 架构、状态管理、平台通道 |

---

## 五、约束机制

### 5.1 生成时约束

| 机制 | 说明 |
|------|------|
| CLAUDE.md 宪法 | AI 每次任务前读取，强制遵循铁律 |
| Steering Docs 引用 | AI 按需读取详细规范 |
| 示例学习 | 用 good/bad 示例让 AI 理解预期 |

### 5.2 审核时检查

| 机制 | 说明 |
|------|------|
| AI Reviewer | 用 AI 审核代码是否符合规范 |
| CI 自动检查 | 关键规则用 AST/脚本自动化检查 |
| 反馈学习 | 根据审核结果优化规范和提示词 |

### 5.3 违规处理流程

| 阶段 | 处理方式 |
|------|----------|
| 生成中 | AI 主动检查铁律，违反时拒绝生成并说明原因 |
| 生成后 | AI Reviewer 检查，标记警告但不阻止 |
| CI 检查 | 关键规则自动化检查，失败则阻止合并 |

### 5.4 CI 自动化检查（分阶段）

**Phase 1（简单规则）**：
| 规则 | 工具 |
|------|------|
| @Valid 注解存在 | grep / 正则匹配 |
| 无硬编码密码/密钥 | gitleaks / detect-secrets |
| 代码格式规范 | prettier / google-java-format |

**Phase 2（中等规则）**：
| 规则 | 工具 |
|------|------|
| 分层越界检查 | SonarQube 规则 / PMD |
| 方法长度限制 | SonarQube |
| 重复代码检测 | SonarQube / dupree |

**Phase 3（复杂规则，需调用链分析）**：
| 规则 | 工具 |
|------|------|
| @Transactional 内无 RPC | 需要字节码分析（Arthas/Javassist） |
| 调用链影响分析 | 需要代码血缘分析工具 |

---

## 六、与审核系统的关系

```
┌─────────────────────────────────────────────────────────────┐
│ 生成时约束                                                  │
│ CLAUDE.md + Steering Docs                                  │
│ → AI 写代码时遵循规范                                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│ 审核时检查                                                  │
│ cr/review/（AI 审核） + CI Scripts（自动检查）               │
│ → 生成后验证，发现问题反馈                                   │
└─────────────────────────────────────────────────────────────┘
```

| 模块 | 职责 |
|------|------|
| `dev/ai-standards-system.md` | 规范体系设计（约束 AI 生成） |
| `cr/` | 审核体系设计（检查代码是否符合规范） |

---

## 七、演进路线

```
Phase 1: 用户级规范
   └── 建立 ~/.claude/steering/ 通用规范
   └── 定义铁律和示例

Phase 2: 项目级规范
   └── 建立项目特定 CLAUDE.md + dev/steering/
   └── 配置触发机制

Phase 3: CI 集成
   └── Phase 1 规则自动化检查
   └── SonarQube 规则配置

Phase 4: AI 审核集成
   └── AI Reviewer 接入
   └── 反馈收集机制
   └── 规则迭代优化
```

---

## 八、待确认事项

1. **CI 检查工具**：用 SonarQube / 自研脚本 / ESLint？
2. **审核触发时机**：MR 创建时 / 代码提交时 / 定时全量？
3. **反馈学习机制**：如何收集和利用审核反馈？
