<!-- @format -->

# 测试文档体系

## 理念

后 AI 时代，代码生成快速，但质量保障才是核心挑战。通过分层测试，在开发阶段发现 70%+ 问题。

---

## 文档导航

### 平台架构

| 文档                                   | 说明                 |
| -------------------------------------- | -------------------- |
| [测试中台架构设计](platform-design.md) | 测试中台完整架构设计 |

### 参考资料

| 文档                                   | 说明                                                    |
| -------------------------------------- | ------------------------------------------------------- |
| [业界调研报告](reference/benchmark.md) | Google、Meta、Amazon、Netflix、阿里、字节、腾讯测试实践 |
| [测试策略指南](reference/strategy.md)  | 11 种测试策略选择指南                                   |

### 实现指南

| 文档                            | 说明                                          |
| ------------------------------- | --------------------------------------------- |
| [Java 单元测试](guides/java.md) | JUnit 5 + Mockito + AssertJ + JaCoCo 实现指南 |

---

## TODO

以下内容待补充：

| 事项                | 优先级 | 说明                        |
| ------------------- | ------ | --------------------------- |
| 集成测试方案        | 中     | TestContainers 集成测试     |
| Python 单元测试     | 低     | pytest + pytest-mock        |
| TypeScript 单元测试 | 低     | Jest + Testing Library      |
| E2E 测试方案        | 中     | Playwright / Cypress        |
| Mock 最佳实践       | 低     | Mock 模式总结               |
| 测试数据构建器      | 低     | Builder 模式、Object Mother |
| Flaky 测试处理      | 低     | 不稳定测试策略              |

---

## 目录结构

```
test/
├── README.md                # 本文件
├── platform-design.md       # 平台架构设计
├── reference/              # 参考资料
│   ├── benchmark.md        # 业界调研
│   └── strategy.md         # 测试策略
└── guides/                # 实现指南
    └── java.md             # Java 单测
```

---

_文档维护：Claude Code_
