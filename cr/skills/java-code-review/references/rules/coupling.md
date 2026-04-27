<!-- @format -->

# Java Coupling Rules

## JAVA-CPL-001 通过全局状态传递业务上下文

Severity: High
Dimension: 服务耦合
Scope: Static fields / ThreadLocal / Global cache

### 检测信号

- 使用 static 可变字段保存业务上下文。
- ThreadLocal 保存用户、租户、订单等业务上下文但没有清理。
- 全局缓存承载请求级状态。

### 必须确认

- 生命周期是否跨请求、跨线程或异步传播。
- ThreadLocal 是否在 finally 中 remove。

### 误报条件

- static final 常量。
- 框架级只读配置。

### 建议修复

- 使用显式参数、上下文对象或框架安全上下文。
- ThreadLocal 必须在 finally 中 remove。

## JAVA-CPL-002 复制其他服务状态机或枚举

Severity: Medium
Dimension: 服务耦合
Scope: Enum / constants / state transition

### 检测信号

- 多个模块复制同一状态码、枚举、状态流转。
- 当前服务硬编码其他服务的内部状态。

### 必须确认

- 状态所有者是否明确。
- 是否存在公共契约或同步机制。

### 误报条件

- 共享枚举是稳定公开契约。

### 建议修复

- 通过契约包、API 或防腐层隔离状态。
- 状态流转保持单一所有者。
