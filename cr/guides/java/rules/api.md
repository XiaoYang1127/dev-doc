<!-- @format -->

# Java API Rules

## JAVA-API-001 新接口缺少参数校验

Severity: High
Dimension: 接口设计
Scope: Controller / RPC API / MQ consumer

### 检测信号

- 新增接口入参没有 Bean Validation、枚举校验、长度或范围校验。
- 分页 size、排序字段、过滤字段无边界。

### 必须确认

- 是否已有全局校验切面。
- 参数是否来自可信内部系统。

### 误报条件

- 入参对象已在上游明确校验，且当前接口非公开边界。

### 建议修复

- 使用 `@Valid`、`@Validated` 和明确约束。
- 对分页、排序、枚举、金额、时间范围设置边界。

## JAVA-API-002 接口契约使用 Map/Object/JSON 字符串

Severity: Medium
Dimension: 接口设计
Scope: HTTP / RPC / MQ DTO

### 检测信号

- 核心接口入参或返回值使用 `Map<String, Object>`、`Object`、裸 JSON 字符串。

### 必须确认

- 是否为透传型网关或扩展字段。

### 误报条件

- 明确的元数据扩展字段，且有 schema 或白名单。

### 建议修复

- 定义 Request/Response/DTO。
- 扩展字段限制 key、value 类型和大小。
