<!-- @format -->

# Flutter 移动端开发规范

> 用户级别规范，跨项目复用。详细规范参考 `references/` 目录。

---

## 一、架构理念

### Feature-First（特性优先）

按业务功能垂直拆分。修改功能时，文件修改范围应集中在单一 feature 目录内。

### Clean Architecture 分层

- **Presentation Layer**：Widget 展示、用户交互、状态管理（Provider）
- **Domain Layer**：业务逻辑、实体定义、仓储接口
- **Data Layer**：仓储实现、数据源（API / 本地）

---

## 二、状态管理原则

### Riverpod 2.0 核心规则

- 使用 `sealed class` 建模状态，强制穷举检查
- 状态变更通过 `StateNotifierProvider`
- Provider 按职责分层：数据获取、业务逻辑、UI 状态

### 状态位置划分

| 状态位置                | 适用场景                            |
| ----------------------- | ----------------------------------- |
| Widget 内部             | UI 相关状态                         |
| `StateProvider`         | 简单可复用状态                      |
| `StateNotifierProvider` | 复杂业务逻辑状态                    |
| 全局单例（get_it）      | 跨模块共享（ApiClient、Repository） |

---

## 三、Widget 开发原则

- 所有 Widget 尽可能使用 `const` 构造函数（强制）
- 页面组件继承 `ConsumerWidget` 或 `StatelessWidget`
- 禁止在 Widget build 方法中执行副作用

---

## 四、详细规范索引

| 文档                                         | 内容                                    | 参考标准                        |
| -------------------------------------------- | --------------------------------------- | ------------------------------- |
| [01-naming](references/01-naming.md)         | 文件、类、变量、目录命名规范            | Dart 官方 / Flutter 官方        |
| [02-widget](references/02-widget.md)         | StatelessWidget、StatefulWidget 开发    | Flutter 官方文档                |
| [03-state](references/03-state.md)           | Riverpod Provider、Repository、依赖注入 | Flutter 官方架构 + Riverpod 2.0 |
| [04-testing](references/04-testing.md)       | 单元测试、Widget 测试、集成测试         | -                               |
| [05-deployment](references/05-deployment.md) | Android/iOS 打包、签名配置              | -                               |

> Flutter 官方架构文档：https://docs.flutter.dev/development/data-and-backend/state-mgmt
> Riverpod 2.0：https://riverpod.dev/
