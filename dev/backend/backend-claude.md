<!-- @format -->

# Java 后端开发规范

> 用户级别规范，跨项目复用。详细规范参考 `references/` 目录。

---

## 一、架构理念

### Feature-First（特性优先）

按业务功能垂直拆分，层内按职责水平划分。修改功能时，文件修改范围应集中在单一 feature 目录内。

### API-First（接口先行）

编码前先定义接口和类型，确保契约稳定。模块间调用必须依赖 `api` 模块，严禁直接引用其他模块的实现类。

---

## 二、异常处理原则

- **异常不能吞**：catch 后必须日志 + 上抛，或转业务异常
- **不忽略 catch 块**：必须记录日志或上报
- **事务内异常**：不要捕获后默默吞掉，事务回滚是正确行为

---

## 三、详细规范索引

| 文档                                           | 内容       | 参考标准                |
| ---------------------------------------------- | ---------- | ----------------------- |
| [01-naming](references/01-naming.md)           | 命名规范   | 阿里巴巴手册-命名篇     |
| [02-api](references/02-api.md)                 | API 设计   | 阿里巴巴手册-API 规范   |
| [03-testing](references/03-testing.md)         | 测试规范   | 阿里巴巴手册-测试篇     |
| [04-deployment](references/04-deployment.md)   | 部署规范   | -                       |
| [05-security](references/05-security.md)       | 安全规范   | 阿里巴巴手册-安全篇     |
| [06-database](references/06-database.md)       | 数据库规范 | 阿里巴巴手册-MySQL 规约 |
| [07-concurrency](references/07-concurrency.md) | 并发规范   | 阿里巴巴手册-并发篇     |

> 阿里巴巴 Java 开发手册原文：https://github.com/alibaba/p3c
