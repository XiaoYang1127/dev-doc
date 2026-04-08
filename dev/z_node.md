<!-- @format -->

1、各端 Claude.md 重构完成

结构：
```
dev/
├── backend/
│   ├── backend-claude.md     # 主入口（精简）
│   └── references/           # 详细信息（自动加载）
│       ├── 01-naming.md
│       ├── 02-api.md
│       ├── 03-testing.md
│       ├── 04-deployment.md
│       ├── 05-security.md
│       └── 06-database.md
├── frontend/
│   ├── vue3-claude.md       # 主入口（精简）
│   └── references/
│       ├── 01-naming.md
│       ├── 02-component.md
│       ├── 03-state.md
│       ├── 04-routing.md
│       ├── 05-testing.md
│       └── 06-deployment.md
└── mobile/
    ├── flutter-claude.md    # 主入口（精简）
    └── references/
        ├── 01-naming.md
        ├── 02-widget.md
        ├── 03-state.md
        ├── 04-testing.md
        └── 05-deployment.md
```

设计原则：
- 主入口精简到 200-300 行，聚焦核心原则和常用命令
- references/ 目录存放详细规范，Claude Code 自动扫描
- 编号排序，便于查找
- 通用规范保留在 ~/.kiro/steering/，各端引用即可

