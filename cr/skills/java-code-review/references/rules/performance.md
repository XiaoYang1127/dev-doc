<!-- @format -->

# Java Performance Rules

## JAVA-PERF-001 循环内执行数据库或远程调用

Severity: High
Dimension: 代码性能
Scope: Service / Repository / Client

### 检测信号

- for/foreach/stream 中调用 Mapper、Repository、RPC、HTTP、Redis、文件 IO。
- 循环次数来自数据库、接口入参或不受控集合。

### 必须确认

- 最大循环次数是否有硬限制。
- 是否已有批量接口或缓存。

### 误报条件

- 循环次数固定且很小。
- 调用对象是纯内存计算。

### 建议修复

- 改为批量查询、批量写入、批量 RPC。
- 设置上限、分页或异步批处理。

## JAVA-PERF-002 分页查询缺少稳定排序

Severity: Medium
Dimension: 代码性能
Scope: SQL / Mapper XML / Repository

### 检测信号

- limit/page 查询没有 order by。
- order by 字段不唯一，可能导致翻页重复或遗漏。

### 必须确认

- 业务是否要求稳定翻页。

### 误报条件

- 查询只取固定小集合，且不分页展示。

### 建议修复

- 使用稳定排序字段。
- 必要时追加主键作为次级排序。
