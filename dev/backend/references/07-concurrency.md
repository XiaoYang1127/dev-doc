<!-- @format -->

# 并发规范

> 本文档为强制规则索引，详细规则及示例参见 [阿里巴巴 Java 开发手册（并发篇）](https://github.com/alibaba/p3c)。

---

## 强制规则清单

| 序号 | 规则 | 错误示例 | 正确做法 |
|------|------|----------|----------|
| 1 | ThreadLocal 必须清理 | 直接 set 不 remove | finally 中 remove() |
| 2 | 循环内禁止 String += | `for + result +=` | StringBuilder 预容量 |
| 3 | Boolean 字段不加 is/has 前缀 | `private Boolean isDeleted` | `private Boolean deleted` |
| 4 | SimpleDateFormat 线程不安全 | 成员变量复用 | DateTimeFormatter（线程安全） |

---

## 并发集合选择

| 场景 | 推荐 | 避免 |
|------|------|------|
| 读多写少 | `CopyOnWriteArrayList` | `Vector` |
| 键值对 | `ConcurrentHashMap` | `Hashtable` |
| 高并发队列 | `ConcurrentLinkedQueue` | `LinkedList` |
| 定时任务 | `ScheduledExecutorService` | `Timer` |
| 枚举做 Key | `EnumMap` | ordinal() 数组下标 |

---

## 手册引用

| 规则 | 手册位置 |
|------|----------|
| volatile 使用场景 | 并发篇-规则 1 |
| synchronized 修饰位置 | 并发篇-规则 2 |
| 避免死锁 | 并发篇-规则 3 |
| 并行流副作用 | 并发篇-规则 8 |
| ThreadLocal 内存泄漏 | 并发篇-规则 6 |

> 完整规则及代码示例请参考 [阿里巴巴 Java 开发手册（并发篇）](https://github.com/alibaba/p3c)。
