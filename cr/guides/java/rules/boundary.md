<!-- @format -->

# Java Boundary Rules

## JAVA-BND-001 跨模块依赖实现细节

Severity: High
Dimension: 服务边界
Scope: Java imports / module dependencies

### 检测信号

- 跨模块 import 指向 `impl`、`mapper`、`repository`、`dao`、`entity`、`po`、`do` 包。
- 一个服务直接调用另一个服务的实现类。

### 必须确认

- 当前项目的模块边界和允许依赖方向。
- 是否存在 API、Facade、Client 或 Domain Service 契约。

### 误报条件

- 目标包实际是当前模块内部包。
- 项目约定该模块是共享基础设施模块。

### 建议修复

- 依赖 API/Facade/Client 契约。
- 通过 DTO/Command/Response 转换隔离内部模型。

## JAVA-BND-002 内部持久化模型泄漏到接口

Severity: High
Dimension: 服务边界
Scope: Controller / RPC API / MQ message

### 检测信号

- HTTP/RPC/MQ 返回 Entity、PO、DO。
- 对外接口直接接收或返回数据库模型。

### 必须确认

- 该接口是否跨模块、跨服务或对前端开放。

### 误报条件

- 仅限模块内部测试或内部私有方法。

### 建议修复

- 使用 Request/Response/DTO。
- 通过 Converter/Assembler 做模型转换。
