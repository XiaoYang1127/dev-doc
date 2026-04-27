<!-- @format -->

# Java 后端代码审核指南

这个目录沉淀可复用的 Java 后端代码审核资产，面向其他 Java 项目使用，不作为本仓库自身的 Claude 命令启用。

## 目录结构

```text
cr/guides/java/
├── README.md
├── commands/                 # 可复制到目标项目 .claude/commands/ 的命令
│   ├── code-review-java.md
│   ├── code-review-java-change.md
│   └── code-review-java-scan.md
├── config/
│   └── code-review-yml.md    # .code-review.yml 配置规范
├── report/
│   └── json-report.md        # JSON 报告格式
└── rules/                    # Java 审核规则卡片
    ├── README.md
    ├── architecture.md
    ├── boundary.md
    ├── coupling.md
    ├── layering.md
    ├── api.md
    ├── performance.md
    ├── security.md
    └── alibaba-java.md
```

## 使用方式

在目标 Java 项目中使用时，将 `commands/` 下的命令复制到目标项目的 `.claude/commands/`，并按需复制 `rules/`、`config/`、`report/` 文档或转换为目标项目自己的 `.code-review.yml` 和规则卡片。

推荐命令：

- `/code-review-java-change`：审核本次变更，适合 PR/MR。
- `/code-review-java-scan`：全量巡检项目系统性风险。
- `/code-review-java`：入口命令，根据参数转到 change 或 scan。
