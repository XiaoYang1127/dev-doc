<!-- @format -->

# Alibaba Java Manual Rules

## JAVA-ALI-001 使用 Executors 默认线程池工厂

Severity: High
Dimension: 代码性能
Scope: Async / scheduler / thread pool

### 检测信号

- 使用 `Executors.newFixedThreadPool`、`newCachedThreadPool`、`newSingleThreadExecutor`、`newScheduledThreadPool`。

### 必须确认

- 是否存在无界队列、无界线程、不可控 OOM 风险。

### 误报条件

- 测试代码或一次性本地工具。

### 建议修复

- 使用 `ThreadPoolExecutor` 显式配置核心线程、最大线程、队列、拒绝策略、线程名。

## JAVA-ALI-002 ThreadLocal 未清理

Severity: High
Dimension: 服务耦合
Scope: Filter / Interceptor / Async / Service

### 检测信号

- ThreadLocal set 后没有在 finally 中 remove。

### 必须确认

- 是否运行在线程池线程中。

### 误报条件

- 生命周期与线程完全一致，且不是线程池复用线程。

### 建议修复

- 使用 try/finally 清理 ThreadLocal。
