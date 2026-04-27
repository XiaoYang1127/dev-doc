<!-- @format -->

# Java Layering Rules

## JAVA-LYR-001 Controller 直接访问 Mapper 或 Repository

Severity: High
Dimension: 模块分层设计
Scope: Controller / Resource / Endpoint

### 检测信号

- Controller 直接注入 Mapper、DAO、Repository。
- Controller 直接执行数据库访问。

### 必须确认

- 项目是否存在 Service/Application 层约定。

### 误报条件

- 极小型内部工具且项目明确允许。

### 建议修复

- Controller 调用 Application/Service。
- 数据访问下沉到 Repository/Mapper。

## JAVA-LYR-002 事务 AOP 可能失效

Severity: High
Dimension: 模块分层设计
Scope: Service / Application

### 检测信号

- `@Transactional` 标注在 private 方法。
- 同类自调用事务方法。
- catch 异常后不抛出，导致事务无法回滚。

### 必须确认

- 项目事务代理模式。
- 异常类型和 rollbackFor 配置。

### 误报条件

- 使用 AspectJ 编织并明确支持。
- catch 后显式设置 rollback-only。

### 建议修复

- 将事务放到 public Service/Application 方法。
- 避免同类自调用。
- 捕获异常后重新抛出或显式回滚。
