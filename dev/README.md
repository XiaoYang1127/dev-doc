<!-- @format -->

# 开发规范体系

面向 AI 辅助开发的多技术栈规范，覆盖通用开发原则、后端、前端、移动端和项目级约束。

---

## 文档导航

### 体系设计

| 文档                                          | 说明                         |
| --------------------------------------------- | ---------------------------- |
| [AI 开发约束体系设计](ai-standards-system.md) | 规范体系、分层机制和审核关系 |
| [演进记录](z_node.md)                         | 各端 Claude.md 重构说明      |

### 技术栈规范

| 技术栈         | 主入口                                         | 项目模板                            | 详细规范                            |
| -------------- | ---------------------------------------------- | ----------------------------------- | ----------------------------------- |
| Java 后端      | [backend-claude.md](backend/backend-claude.md) | [SPEC.md](backend/project/SPEC.md)  | [references/](backend/references/)  |
| Vue 3 前端     | [vue3-claude.md](frontend/vue3-claude.md)      | [SPEC.md](frontend/project/SPEC.md) | [references/](frontend/references/) |
| Flutter 移动端 | [flutter-claude.md](mobile/flutter-claude.md)  | [SPEC.md](mobile/project/SPEC.md)   | [references/](mobile/references/)   |

### 通用规范

| 文档                                           | 说明                              |
| ---------------------------------------------- | --------------------------------- |
| [通用开发规范说明](steering/README.md)         | steering 文档使用方式             |
| [开发原则](steering/development-principles.md) | 代码质量、安全、协作原则          |
| [代码风格](steering/code-style.md)             | 通用代码风格、命名、注释          |
| [API 标准](steering/api-standards.md)          | RESTful API、响应格式、安全与性能 |
| [测试标准](steering/testing-standards.md)      | 测试原则、单测、集成测试          |
| [Git 工作流](steering/git-workflow.md)         | 分支、提交、发布流程              |

---

## 目录结构

```text
dev/
├── README.md
├── ai-standards-system.md
├── z_node.md
├── backend/
│   ├── backend-claude.md
│   ├── project/SPEC.md
│   └── references/
├── frontend/
│   ├── vue3-claude.md
│   ├── project/SPEC.md
│   └── references/
├── mobile/
│   ├── flutter-claude.md
│   ├── project/SPEC.md
│   └── references/
└── steering/
    ├── README.md
    ├── api-standards.md
    ├── code-style.md
    ├── development-principles.md
    ├── git-workflow.md
    └── testing-standards.md
```
