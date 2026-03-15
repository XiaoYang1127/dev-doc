---
inclusion: auto
---

# Git 工作流规范

## 一、分支管理

### 1.1 分支类型

- **main/master**：生产环境分支，只接受合并，不直接提交
- **develop**：开发主分支，集成所有功能
- **feature/**：功能分支，从 develop 创建
- **bugfix/**：bug 修复分支，从 develop 创建
- **hotfix/**：紧急修复分支，从 main 创建
- **release/**：发布分支，从 develop 创建

### 1.2 分支命名

- feature/user-login
- bugfix/fix-payment-error
- hotfix/critical-security-patch
- release/v1.2.0

## 二、提交规范

### 2.1 Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 2.2 Type 类型

- **feat**：新功能
- **fix**：bug 修复
- **docs**：文档更新
- **style**：代码格式调整（不影响功能）
- **refactor**：重构（不是新功能也不是 bug 修复）
- **perf**：性能优化
- **test**：测试相关
- **chore**：构建过程或辅助工具的变动

### 2.3 提交示例

```
feat(auth): 添加用户登录功能

- 实现用户名密码登录
- 添加 JWT token 生成
- 集成 Redis 存储 token

Closes #123
```

```
fix(payment): 修复支付金额计算错误

修复了在优惠券和积分同时使用时的金额计算问题

Fixes #456
```

## 三、工作流程

### 3.1 功能开发流程

1. 从 develop 创建 feature 分支
2. 在 feature 分支开发和测试
3. 提交 Pull Request 到 develop
4. Code Review 通过后合并
5. 删除 feature 分支

### 3.2 发布流程

1. 从 develop 创建 release 分支
2. 在 release 分支进行测试和 bug 修复
3. 测试通过后合并到 main 和 develop
4. 在 main 上打 tag
5. 删除 release 分支

### 3.3 紧急修复流程

1. 从 main 创建 hotfix 分支
2. 修复问题并测试
3. 合并到 main 和 develop
4. 在 main 上打 tag
5. 删除 hotfix 分支

## 四、最佳实践

- **小步提交**：每次提交只包含一个逻辑变更
- **频繁推送**：及时推送到远程仓库
- **及时同步**：定期从主分支拉取最新代码
- **冲突处理**：遇到冲突及时解决，不要拖延
- **保持整洁**：定期清理已合并的本地分支
