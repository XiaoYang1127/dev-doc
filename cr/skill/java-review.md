# Java 代码审核规范（当前简化版）

> **定位**：Phase 1 当前使用的简化审核规范
> **目标**：架构合规 + 明显缺陷
> **完整规则**：见 [design/rules/languages/java.md](../design/rules/languages/java.md)

## 1. 分层架构规范

### 1.1 职责边界

```
Controller ──→ Service ──→ Manager/Repository
                  ↓
               Converter ──→ PO/DTO/VO
```

**禁止行为**（一旦发现，立即标记 🔴）：

| 违规场景 | 示例 | 正确做法 |
|---------|------|---------|
| Controller 直接调用 Repository | `userRepository.findById()` in Controller | 通过 Service 调用 |
| 跨模块直接操作 Repository | OrderService 直接查 user 表 | 通过 OrderService 调用 UserService API |
| PO 直接返回前端 | `return userPO;` | `return userConverter.toVO(userPO);` |
| DTO 直接用于持久化 | `userRepository.save(userDTO)` | `userRepository.save(userConverter.toPO(dto))` |

### 1.2 判断规则

```
如果 类名包含 Controller 且 调用 Repository/Mapper:
    → 违规

如果 类名包含 Service 且 调用其他模块的 Repository:
    → 违规

如果 返回类型是 PO 且 方法在 Controller 中:
    → 违规
```

## 2. 事务安全规范

### 2.1 高风险场景

**🔴 事务内远程调用**（HTTP/RPC）

```java
// ❌ 错误：事务内调用外部服务
@Transactional
public void createOrder(OrderDTO dto) {
    orderRepository.save(order);
    // 风险：外部服务超时会导致事务长时间占用连接
    paymentService.charge(dto.getAmount());
}

// ✅ 正确：远程调用放在事务外
public void createOrder(OrderDTO dto) {
    // 先执行远程调用
    paymentResult = paymentService.charge(dto.getAmount());

    // 再开启本地事务保存数据
    transactionTemplate.execute(status -> {
        orderRepository.save(order);
        return null;
    });
}
```

**🔴 事务内异步操作**

```java
// ❌ 错误：事务内发送 MQ
@Transactional
public void updateStatus(Long id, Status status) {
    userRepository.updateStatus(id, status);
    // 风险：事务可能回滚，但消息已发送
    mqProducer.send(new StatusChangeEvent(id, status));
}

// ✅ 正确：事务提交后发送
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleStatusChange(StatusChangeEvent event) {
    mqProducer.send(event);
}
```

### 2.2 判断规则

```
如果 方法有 @Transactional 且 方法体内有:
    - RestTemplate/WebClient/HttpClient 调用
    - @Async 方法调用
    - MQ 发送操作
    - 长时间睡眠/等待
→ 标记为事务安全风险
```

## 3. NPE 风险防范

### 3.1 常见场景

**🔴 参数未校验直接使用**

```java
// ❌ 错误：直接链式调用
public void process(UserDTO dto) {
    String name = dto.getProfile().getName();  // dto/profile 都可能 null
}

// ✅ 正确：前置校验或安全调用
public void process(UserDTO dto) {
    if (dto == null || dto.getProfile() == null) {
        throw new IllegalArgumentException("参数不能为空");
    }
    String name = dto.getProfile().getName();
}

// ✅ 或使用 Optional
String name = Optional.ofNullable(dto)
    .map(UserDTO::getProfile)
    .map(Profile::getName)
    .orElse("默认名称");
```

**🟡 集合操作前未判空**

```java
// ❌ 有风险
List<Item> items = orderService.getItems(orderId);
items.stream().map(...);  // items 可能 null

// ✅ 安全做法
List<Item> items = orderService.getItems(orderId);
if (CollectionUtils.isEmpty(items)) {
    return Collections.emptyList();
}
items.stream().map(...);
```

### 3.2 判断规则

```
如果发现:
    - 方法参数直接 .getXXX() 链式调用（超过 1 层）
    - 集合调用 stream()/size()/get() 前未判空
    - Optional.get() 未先调用 isPresent()
→ 标记 NPE 风险
```

## 4. 明显逻辑缺陷

### 4.1 死代码检测

```java
// 🔴 不可达代码
public void process() {
    return;
    System.out.println("这行永远不会执行");  // 标记
}
```

### 4.2 资源未关闭

```java
// 🟡 潜在资源泄漏
public String readFile(String path) {
    InputStream is = new FileInputStream(path);  // 未关闭
    return ...;
}

// ✅ 正确：try-with-resources
public String readFile(String path) {
    try (InputStream is = new FileInputStream(path)) {
        return ...;
    }
}
```

### 4.3 错误比较

```java
// 🔴 对象用 == 比较（除非故意比较引用）
if (userDTO.getStatus() == "ACTIVE")  // 错误

// ✅ 正确
if ("ACTIVE".equals(userDTO.getStatus()))

// 🟡 浮点数直接比较
if (price == 10.0)  // 精度问题

// ✅ 正确
if (Math.abs(price - 10.0) < 0.0001)
```

## 5. 输出格式规范

### 5.1 严重等级定义

| 等级 | 图标 | 定义 | 必须修复？ |
|------|------|------|-----------|
| 严重 | 🔴 | 可能导致生产故障（NPE、数据不一致、架构破坏） | 是 |
| 警告 | 🟡 | 潜在风险，建议评估后修复 | 建议 |
| 建议 | 💡 | 代码可优化，但无直接风险 | 可选 |

### 5.2 问题格式模板

```markdown
**类型**: {分层越界/事务安全/NPE风险/逻辑缺陷}
**等级**: {🔴/🟡/💡}
**位置**: {类名}:{方法名}:{行号}
**问题**: {一句话描述}

**当前代码**:
```java
{代码片段}
```

**建议修改**:
```java
{修复后的代码}
```

**理由**:
{为什么这是个问题，可能带来什么后果}
```

## 6. 暂不检查项（宽松阶段）

以下问题**暂不标记**，待进入严格阶段再开启：

- [ ] 代码风格（命名、缩进、空格）
- [ ] 缺少注释/JavaDoc
- [ ] 魔法数字
- [ ] import 顺序
- [ ] 方法过长（超过 50 行）
- [ ] 类过大（超过 500 行）
- [ ] 圈复杂度过高
- [ ] 重复代码
