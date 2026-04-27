<!-- @format -->

# Java Architecture Rules

## JAVA-ARCH-001 Controller 承载复杂业务编排

Severity: High
Dimension: 架构设计
Scope: Controller / Resource / Endpoint

### 检测信号

- Controller 中出现事务、复杂状态流转、缓存、MQ、RPC、SQL 或多服务编排。
- Controller 方法过长，包含多层条件分支和业务规则。

### 必须确认

- 项目是否允许轻量 Controller 直接调用应用服务。
- 业务编排是否已有 Application/Service/Domain 层承载位置。

### 误报条件

- Controller 只做参数校验、协议转换和调用单个应用服务。

### 建议修复

- 将业务编排下沉到 Application/Service。
- Controller 只保留协议适配、校验和响应封装。

## JAVA-ARCH-002 局部需求引入全局机制

Severity: Medium
Dimension: 架构设计
Scope: Configuration / Interceptor / Filter / Aspect / Common

### 检测信号

- 为单一业务新增全局拦截器、切面、公共框架或 common 工具。
- 全局配置影响多个无关模块。

### 必须确认

- 是否有明确的跨模块复用场景。
- 是否存在更局部的扩展点。

### 误报条件

- 该机制确实是平台能力，且已有多处调用方。

### 建议修复

- 优先使用业务模块内部扩展点。
- 公共能力需要稳定契约、隔离配置和测试。
