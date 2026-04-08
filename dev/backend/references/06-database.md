# 数据库规范

## 表设计原则

- 每张表必须有主键
- 必须有 `created_at`、`updated_at` 字段（BIGINT 类型，存储毫秒时间戳）
- 使用逻辑删除，添加 `deleted_at` 字段（BIGINT 类型，NULL 表示未删除）
- 外键使用 `_id` 后缀：`user_id`、`order_id`
- 金额字段使用 `DECIMAL(19,2)`，单位为分
- 所有时间字段统一使用 `BIGINT` 类型存储毫秒时间戳
- 表必须有注释，字段必须有注释

## 表结构示例

```sql
CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `deleted_at` BIGINT DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted_at`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

## 索引规范

- 索引命名：`idx_字段名` 或 `idx_字段1_字段2`
- 唯一索引命名：`uk_字段名`
- 单表索引数量不超过 5 个
- 联合索引遵循最左前缀原则

## SQL 编写规范

- 禁止使用 `SELECT *`，明确指定需要的字段
- 使用参数化查询，防止 SQL 注入
- 避免在循环中执行 SQL
- 分页查询必须指定排序字段
- 禁止在 WHERE 条件中对字段进行函数操作（会导致索引失效）

## 集合处理规范（阿里巴巴 Java 开发手册）

### subList 禁用规则

```java
// ❌ 错误 - subList 返回的是视图，强制转换会抛 ClassCastException
List list = new ArrayList<>(Arrays.asList(1, 2, 3));
ArrayList subList = (ArrayList) list.subList(0, 1); // 运行时异常！

// ✅ 正确 - 新建 ArrayList
List subList = new ArrayList<>(list.subList(0, 1));

// ✅ 正确 - 使用 Stream
List subList = list.subList(0, 1).stream().collect(Collectors.toList());
```

### 集合初始化

```java
// ❌ 错误 - 未指定大小，扩容有性能损耗
List<Object> list = new ArrayList<>();
Map<String, Object> map = new HashMap<>();

// ✅ 正确 - 根据预估容量初始化
List<Object> list = new ArrayList<>(16);
Map<String, Object> map = new HashMap<>(32);

// ✅ 正确 - 使用 List.of / Map.of（不可变集合）
List<String> immutableList = List.of("a", "b", "c");
```

## 并发安全规范（阿里巴巴 Java 开发手册）

### SimpleDateFormat 线程不安全

```java
// ❌ 错误 - SimpleDateFormat 线程不安全
private static final SimpleDateFormat sdf =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

// ✅ 正确 - 使用 DateTimeFormatter（线程安全）
private static final DateTimeFormatter sdf =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

// ✅ 正确 - 使用 ThreadLocal 包装
private static final ThreadLocal<DateFormat> sdf =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

### 集合线程安全

```java
// ❌ 错误 - ArrayList / HashMap 线程不安全
List<String> list = new ArrayList<>();
Map<String, String> map = new HashMap<>();

// ✅ 正确 - 使用并发集合
List<String> list = new CopyOnWriteArrayList<>();
Map<String, String> map = new ConcurrentHashMap<>();

// ✅ 正确 - 使用 synchronizedList（读多写少场景）
List<String> list = Collections.synchronizedList(new ArrayList<>());
```

### 异常处理原则（阿里强制）

```java
// ❌ 错误 - 吞掉异常不做任何处理
try {
  doSomething();
} catch (Exception e) {
  // 什么都不做
}

// ❌ 错误 - 只打日志不抛异常
try {
  doSomething();
} catch (Exception e) {
  log.error("操作失败", e);
}

// ✅ 正确 - 日志 + 上抛，或转为业务异常
try {
  doSomething();
} catch (Exception e) {
  log.error("操作失败", e);
  throw new BusinessException("操作失败，请稍后重试");
}

// ✅ 正确 - 特定异常处理
try {
  doSomething();
} catch (IOException e) {
  log.error("IO错误", e);
  throw new SystemException("系统错误，请联系管理员");
}
```
