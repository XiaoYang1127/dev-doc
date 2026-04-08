# Claude.md 重构设计方案

**日期：** 2026-04-08
**目标：** 将臃肿的端级别 Claude.md 拆分为可按需加载的模块化结构

## 背景

当前问题：
- `dev/z_node.md` 内容臃肿（100+ 行）
- 各端 `claude.md` 文件过长（backend: 1286行, flutter: 1049行, vue3: 1173行）
- 所有规范混在一个文件中，不便查阅和维护

## 设计目标

1. **主入口精简**：每个端 claude.md 控制在 200-300 行
2. **按需加载**：详细信息拆分到 references/ 目录，Claude Code 自动扫描
3. **结构清晰**：编号排序，职责单一

## 目录结构

```
dev/
├── backend/
│   ├── claude.md              # 主入口（200-300行）
│   └── references/
│       ├── 01-naming.md       # 命名规范
│       ├── 02-api.md          # API 设计规范
│       ├── 03-testing.md      # 测试规范
│       ├── 04-deployment.md   # 部署规范
│       ├── 05-security.md     # 安全规范
│       └── 06-database.md     # 数据库规范
├── frontend/
│   ├── claude.md              # 主入口（200-300行）
│   └── references/
│       ├── 01-naming.md       # 命名规范
│       ├── 02-component.md    # 组件开发规范
│       ├── 03-state.md        # 状态管理规范
│       ├── 04-routing.md      # 路由规范
│       ├── 05-testing.md      # 测试规范
│       └── 06-deployment.md   # 部署规范
└── mobile/
    ├── claude.md               # 主入口（200-300行）
    └── references/
        ├── 01-naming.md       # 命名规范
        ├── 02-widget.md       # Widget 开发规范
        ├── 03-state.md        # 状态管理规范
        ├── 04-testing.md      # 测试规范
        └── 05-deployment.md   # 打包部署规范
```

## 主入口结构（claude.md）

```markdown
# {端名称} 开发规范

> 详细规范参考 `references/` 目录，Claude Code 会自动扫描相关文件。

## 一、技术栈

- 核心技术版本
- 必装工具

## 二、目录结构

Feature-First 目录结构概览

## 三、核心原则

- Feature-First（特性优先）
- API-First（接口先行）
- DRY / KISS / 单一职责

## 四、快速命令

常用开发命令速查

## 五、详细参考

| 文档 | 内容 |
| ---- | ---- |
| 01-naming | 文件、类、方法命名规范 |
| 02-api | RESTful API 设计 |
| ... | ... |
```

## references/ 文档结构

每个 reference 文件：
- 独立完整，可单独理解
- 包含代码示例
- 适合 Claude Code 在相关上下文中自动加载

## 实施步骤

1. **创建目录结构**
   - 创建各端 `references/` 目录

2. **提炼主入口**
   - 从现有 claude.md 提取核心内容
   - 控制行数在 200-300 行

3. **拆分 references**
   - 按编号命名
   - 迁移详细规范内容

4. **保留 steering 通用规范**
   - `~/.kiro/steering/` 下的通用规范保持不变
   - 各端 claude.md 引用即可

## 加载机制

Claude Code 启动时或处理相关上下文时：
1. 读取项目根 `CLAUDE.md`
2. 读取当前目录 `claude.md`
3. 根据上下文需要，自动扫描 `references/` 目录

无需手动 include，Claude Code 约定优于配置。

## TODO

- [ ] 创建 backend/references/ 目录和文件
- [ ] 创建 frontend/references/ 目录和文件
- [ ] 创建 mobile/references/ 目录和文件
- [ ] 精简各端主入口 claude.md
- [ ] 更新 dev/z_node.md 指向新结构
