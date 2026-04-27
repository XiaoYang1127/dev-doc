<!-- @format -->

# Java Security Rules

## JAVA-SEC-001 MyBatis `${}` 拼接 SQL

Severity: Critical
Dimension: 安全问题
Scope: Mapper XML / MyBatis 注解 SQL

### 检测信号

- SQL 中出现 `${}`。
- `${}` 的值来自外部输入或未经过白名单映射。

### 必须确认

- 是否只能用于表名、字段名、排序方向等无法用 `#{}` 的位置。
- 是否有固定枚举或白名单映射。

### 误报条件

- `${}` 值来自不可被用户控制的常量。
- 动态字段经过白名单映射。

### 建议修复

- 普通参数改为 `#{}`。
- 排序字段、表名、字段名使用白名单映射。

## JAVA-SEC-002 敏感信息日志泄漏

Severity: High
Dimension: 安全问题
Scope: Controller / Service / Filter / Interceptor / Client

### 检测信号

- 日志直接打印 request、response、headers、token、cookie、authorization。
- 日志打印手机号、身份证、银行卡、密码、密钥、验证码。

### 必须确认

- 对象 `toString()` 是否包含敏感字段。
- 是否已有脱敏工具或日志切面。

### 误报条件

- 日志字段已显式脱敏。
- 日志只在本地测试 profile 生效。

### 建议修复

- 使用脱敏工具。
- 避免打印完整对象、header、cookie 和 token。
