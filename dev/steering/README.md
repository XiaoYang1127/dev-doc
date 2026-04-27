<!-- @format -->

# 通用开发规范说明

本目录包含跨项目、跨语言的通用开发规范，适用于所有项目。

## 文件说明

### 1. development-principles.md

**开发铁律与基本原则**

- 代码质量铁律（可读性、简单性、DRY、KISS、YAGNI、SOLID）
- 安全铁律（输入验证、敏感信息、最小权限）
- 协作铁律（Code Review、文档同步、测试覆盖）
- 通用开发规范（命名、注释、代码组织）
- 性能原则、错误处理、版本控制
- 持续改进（代码审查、技术债务、知识分享）

**适用范围**：所有项目、所有语言

### 2. code-style.md

**代码风格规范**

- 通用代码风格（缩进、行长度、空行、括号）
- 导入/引用规范（顺序、规则）
- 注释规范（文档注释、行内注释）
- 命名规范（多语言对比表）
- 反模式（魔法数字、过长方法、深层嵌套、上帝类、重复代码）
- 代码格式化工具配置

**适用范围**：所有项目、所有语言

### 3. api-standards.md

**API 开发标准**

- RESTful API 设计规范（URL、HTTP 方法、状态码）
- 统一响应格式（成功、错误、分页）
- 请求规范（请求头、参数验证、接口文档）
- 安全规范（认证授权、数据安全、限流防护）
- 性能优化（缓存、分页、响应优化）
- 最佳实践（幂等性、错误处理、版本管理、监控告警）

**适用范围**：所有提供 API 的项目（后端、BFF）

### 4. testing-standards.md

**测试规范**

- 测试原则（测试金字塔、测试要求）
- 单元测试（命名规范、AAA 模式、Mock 使用）
- 集成测试（测试范围、测试环境）
- 测试最佳实践（独立性、可读性、维护、性能）

**适用范围**：所有项目、所有语言

### 5. git-workflow.md

**Git 工作流规范**

- 分支管理（分支类型、命名规范）
- 提交规范（Commit Message 格式、Type 类型）
- 工作流程（功能开发、发布、紧急修复）
- 最佳实践（小步提交、频繁推送、及时同步）

**适用范围**：所有使用 Git 的项目

## 使用方式

### 方式 1：用户级别（推荐）

将这些文件放在 `~/.kiro/steering/` 目录，对所有项目生效。

```bash
~/.kiro/steering/
├── README.md
├── development-principles.md
├── code-style.md
├── api-standards.md
├── testing-standards.md
└── git-workflow.md
```

### 方式 2：项目级别

在项目根目录创建 `claude.md` 或 `.kiro/steering/project-specific.md`，补充项目特定规范。

```bash
/project-root/
├── claude.md                    # 项目特定规范
└── .kiro/steering/
    └── project-specific.md      # 或放在这里
```

### 方式 3：混合使用（最佳实践）

- 通用规范放在用户级别（~/.kiro/steering/）
- 项目特定规范放在项目根目录（claude.md）

## 项目特定规范示例

### 后端项目（Java/Spring Boot）

在项目根目录创建 `claude.md`：

```markdown
# 后端项目开发规范

## 技术栈

- Java 21
- Spring Boot 3.x
- MyBatis-Plus 3.x
- MySQL 8.0+
- Redis 7.x

## 项目架构

（参考 backend-claude.md）

## 数据库设计

（项目特定的表设计规范）

## 部署规范

（项目特定的部署流程）
```

### 前端项目（Vue3）

在项目根目录创建 `claude.md`：

```markdown
# Vue3 项目开发规范

## 技术栈

- Vue 3.x
- TypeScript 5.x
- Vite 5.x
- Element Plus

## 项目架构

（参考 vue3-claude.md）

## 组件开发

（项目特定的组件规范）

## 部署规范

（项目特定的部署流程）
```

### 移动端项目（Flutter）

在项目根目录创建 `claude.md`：

```markdown
# Flutter 项目开发规范

## 技术栈

- Flutter 3.x
- Dart 3.x
- Riverpod

## 项目架构

（参考 flutter-claude.md）

## 性能优化

（项目特定的性能要求）

## 发布规范

（项目特定的发布流程）
```

## 规范优先级

当多个规范文件存在时，优先级如下：

1. **项目级别规范**（最高优先级）
   - `/project-root/claude.md`
   - `/project-root/.kiro/steering/*.md`

2. **用户级别规范**
   - `~/.kiro/steering/*.md`

3. **默认规范**（最低优先级）
   - Claude 内置规范

**原则**：项目特定规范覆盖通用规范。

## 维护建议

### 定期更新

- 技术栈更新时同步更新规范
- 新的最佳实践及时补充
- 团队反馈及时调整

### 团队协作

- 将规范纳入代码仓库
- Code Review 时参考规范
- 新人入职时作为培训材料

### 持续改进

- 收集团队反馈
- 记录常见问题和解决方案
- 补充实际项目中的最佳实践

## 常见问题

### Q: 规范太多，记不住怎么办？

A: 不需要记住所有规范，Claude 会自动读取并遵循。你只需要：

1. 了解规范的存在
2. Code Review 时参考规范
3. 遇到问题时查阅规范

### Q: 项目特定规范和通用规范冲突怎么办？

A: 项目特定规范优先级更高，会覆盖通用规范。

### Q: 如何让团队成员遵守规范？

A:

1. 将规范纳入代码仓库
2. Code Review 时检查规范遵守情况
3. 使用自动化工具（Linter、Formatter）
4. 定期组织规范培训

### Q: 规范文件太大会影响 Claude 吗？

A: 不会。当前所有规范文件总大小约 50KB，对 Claude 完全没有影响。

## 相关资源

- [后端开发规范模板](../backend/backend-claude.md)
- [Vue3 开发规范模板](../frontend/vue3-claude.md)
- [Flutter 开发规范模板](../mobile/flutter-claude.md)
- [后端项目规范模板](../backend/project/SPEC.md)
- [前端项目规范模板](../frontend/project/SPEC.md)
- [移动端项目规范模板](../mobile/project/SPEC.md)
