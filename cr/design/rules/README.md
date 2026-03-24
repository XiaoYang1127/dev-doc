# 代码审核规则体系

本目录包含代码审核系统的规则定义和管理文档。

## 目录结构

```
rules/
├── README.md                 # 本文件
├── rule-framework.md         # 规则管理框架（通用理论）
└── languages/                # 各语言具体规则
    ├── java.md               # Java 规则库
    ├── typescript.md         # TypeScript 规则库（待创建）
    └── dart.md               # Dart 规则库（待创建）
```

## 三层架构关系

```
┌─────────────────────────────────────────────────────────────┐
│  plan.md (项目层)                                           │
│  ├── 系统架构设计                                            │
│  ├── 10个审核维度定义                                        │
│  └── 实施路线图                                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  rules/rule-framework.md (框架层)                           │
│  ├── 规则结构规范（YAML格式）                                 │
│  ├── 规则生命周期管理                                        │
│  ├── 规则类型体系（正则/AST/语义/AI）                         │
│  └── 反馈学习机制                                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  rules/languages/*.md (实现层)                              │
│  ├── java.md                                                │
│  ├── typescript.md                                          │
│  └── ...                                                    │
│                                                             │
│  各语言具体规则实现，遵循 rule-framework 定义的规范           │
└─────────────────────────────────────────────────────────────┘
```

## 使用说明

### 添加新语言规则

1. 在 `languages/` 下创建 `{language}.md`
2. 参考 `rule-framework.md` 中的规则结构规范
3. 参考 `java.md` 的组织方式按审核维度分层
4. 更新本 README 的目录结构

### 规则字段规范

所有规则必须包含以下字段：

```yaml
id: {language}.{layer}.{category}.{rule-name}
name: 规则名称
description: 规则描述
category: 分类
severity: error|warning|suggestion
layer: 2-9
language: java|typescript|dart|all
scope: file|method|statement|expression
condition: 匹配条件
message: 输出模板
meta: 元数据
```

详细规范见 [rule-framework.md](./rule-framework.md)。

## 规则 ID 命名规范

```
{语言}.{层级}.{分类}.{具体规则}
```

**示例：**
- `java.fragment.security.hardcoded-secret`
- `java.architecture.controller-direct-repository`
- `typescript.context.null-check`

## 配套文档

| 文档 | 说明 |
|------|------|
| [../plan.md](../plan.md) | 系统整体架构与10层审核维度设计 |
| [../implementation.md](../implementation.md) | 规则执行引擎与开源工具集成实现 |
| [rule-framework.md](./rule-framework.md) | 规则管理框架（结构规范、生命周期） |
| [languages/java.md](./languages/java.md) | Java 完整规则库（46条规则） |
