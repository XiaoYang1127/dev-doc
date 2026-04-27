<!-- @format -->

# Java 单元测试方案设计

> 本文档是 [测试中台架构设计](../platform-design.md) 的 Java 语言具体实现方案
>
> 相关文档：
>
> - 测试流程说明 - 规划中
> - [测试策略选型](../reference/strategy.md) - 11种测试策略选择指南

---

## 一、核心目标

**通过单元测试发现 70% 的问题，降低项目出错概率**

### 1.1 目标拆解

- **缺陷前移**：在开发阶段发现问题，而非测试或生产阶段
- **快速反馈**：单测执行时间 < 5 分钟，支持快速迭代
- **回归保护**：代码变更时自动验证，防止引入新问题
- **文档作用**：测试即文档，展示代码的正确用法
- **重构信心**：有测试保护，敢于重构优化代码

### 1.2 成功指标

| 指标               | 目标值   | 说明                           |
| ------------------ | -------- | ------------------------------ |
| 核心业务逻辑覆盖率 | ≥ 80%    | 行覆盖率 + 分支覆盖率          |
| 单测执行时间       | < 5 分钟 | 全量单测执行时间               |
| 缺陷发现率         | ≥ 70%    | 单测发现的缺陷占总缺陷比例     |
| 测试通过率         | ≥ 95%    | 主分支测试通过率               |
| 测试维护成本       | < 20%    | 测试代码维护时间占开发时间比例 |

---

## 二、技术栈选型

### 2.1 核心工具

| 工具           | 版本    | 用途       | 选型理由                             |
| -------------- | ------- | ---------- | ------------------------------------ |
| JUnit 5        | 5.10+   | 测试框架   | 业界标准，支持参数化、动态测试       |
| Mockito        | 5.x     | Mock 框架  | 简单易用，支持 Spy、ArgumentCaptor   |
| AssertJ        | 3.24+   | 断言库     | 流式 API，可读性强                   |
| JaCoCo         | 0.8.11+ | 覆盖率工具 | Maven/Gradle 集成好，报告清晰        |
| TestContainers | 1.19+   | 容器测试   | 真实环境测试，支持 MySQL/Redis/Kafka |
| Awaitility     | 4.2+    | 异步测试   | 优雅处理异步逻辑                     |
| ArchUnit       | 1.2+    | 架构测试   | 验证分层架构、依赖规则               |

### 2.2 辅助工具

| 工具       | 用途         | 说明                 |
| ---------- | ------------ | -------------------- |
| Faker      | 测试数据生成 | 生成真实感的随机数据 |
| JSONassert | JSON 比对    | 灵活的 JSON 断言     |
| WireMock   | HTTP Mock    | 模拟第三方 HTTP 服务 |
| Instancio  | 对象构建     | 快速创建测试对象     |

---

## 三、测试分层策略

### 3.1 测试金字塔

```
           /\
          /  \      E2E 测试（5%）
         /----\     - 完整业务流程
        /      \    - 真实环境
       /--------\
      /          \  集成测试（15%）
     /            \ - 模块间交互
    /--------------\- TestContainers
   /                \
  /------------------\ 单元测试（80%）
 /                    \- 单个方法/类
/----------------------\- Mock 外部依赖
```

### 3.2 分层测试职责

#### 单元测试（Unit Test）

**测试范围**：单个类或方法

**依赖处理**：Mock 所有外部依赖

**测试内容**：

- 业务逻辑正确性
- 边界条件处理
- 异常情况处理
- 参数校验逻辑

**示例场景**：

```java
// 测试订单金额计算逻辑
@Test
void calculateTotalPrice_withDiscount_shouldApplyCorrectly() {
    // 不依赖数据库、不调用外部服务
    OrderService service = new OrderService();
    BigDecimal result = service.calculatePrice(items, discount);
    assertThat(result).isEqualByComparingTo("85.00");
}
```

#### 集成测试（Integration Test）

**测试范围**：多个模块协作

**依赖处理**：使用真实依赖（TestContainers）

**测试内容**：

- 数据库交互（CRUD、事务）
- 缓存逻辑（Redis）
- 消息队列（Kafka）
- 模块间接口调用

**示例场景**：

```java
// 测试订单创建流程（涉及数据库、缓存）
@SpringBootTest
@Testcontainers
class OrderIntegrationTest {
    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    @Test
    void createOrder_shouldPersistToDatabase() {
        Order order = orderService.create(request);
        // 验证数据库真实写入
        Order saved = orderRepository.findById(order.getId());
        assertThat(saved).isNotNull();
    }
}
```

#### E2E 测试（End-to-End Test）

**测试范围**：完整业务流程

**依赖处理**：完整应用 + 真实中间件

**测试内容**：

- 用户核心路径
- 关键业务流程
- 跨系统交互

**示例场景**：

```java
// 测试完整下单流程（注册 → 下单 → 支付 → 发货）
@Test
void completeOrderFlow_shouldSucceed() {
    // 1. 注册用户
    String token = registerUser("test@example.com");
    // 2. 创建订单
    Long orderId = createOrder(token, items);
    // 3. 支付订单
    payOrder(orderId, paymentInfo);
    // 4. 验证订单状态
    assertOrderStatus(orderId, OrderStatus.PAID);
}
```

---

## 四、测试维度与覆盖策略

### 4.1 必测维度

#### 1. 输入输出测试

**目标**：验证不同输入产生正确输出

**测试方法**：参数化测试

```java
@ParameterizedTest
@CsvSource({
    "100, 0.1, 90.0",      // 正常折扣
    "100, 0, 100.0",       // 无折扣
    "100, 1.0, 0.0",       // 全额折扣
    "0, 0.5, 0.0"          // 零金额
})
void calculatePrice_withDifferentInputs(BigDecimal price, BigDecimal discount, BigDecimal expected) {
    BigDecimal result = priceCalculator.calculate(price, discount);
    assertThat(result).isEqualByComparingTo(expected);
}
```

#### 2. 路径覆盖测试

**目标**：覆盖所有代码分支

**测试方法**：针对每个 if/else、switch 分支编写测试

```java
// 被测代码
public OrderStatus getOrderStatus(Order order) {
    if (order.isPaid()) {
        return OrderStatus.PAID;
    } else if (order.isCancelled()) {
        return OrderStatus.CANCELLED;
    } else {
        return OrderStatus.PENDING;
    }
}

// 测试代码 - 覆盖所有分支
@Test void getOrderStatus_whenPaid_returnsPaid() { /* ... */ }
@Test void getOrderStatus_whenCancelled_returnsCancelled() { /* ... */ }
@Test void getOrderStatus_whenPending_returnsPending() { /* ... */ }
```

#### 3. 异常处理测试

**目标**：验证异常场景的处理逻辑

**测试方法**：assertThrows + 异常信息验证

```java
@Test
void createOrder_withInvalidUser_throwsException() {
    // 验证抛出正确的异常类型
    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> orderService.create(invalidUserId, items)
    );

    // 验证异常信息
    assertThat(exception.getCode()).isEqualTo("USER_NOT_FOUND");
    assertThat(exception.getMessage()).contains("用户不存在");
}
```

#### 4. 边界条件测试

**目标**：验证临界值处理

**边界值类型**：

- 数值边界：0、负数、最大值、最小值
- 集合边界：空集合、单元素、大量元素
- 字符串边界：空字符串、超长字符串、特殊字符
- 时间边界：过去、未来、时区边界

```java
@ParameterizedTest
@ValueSource(ints = {-1, 0, 1, Integer.MAX_VALUE})
void processQuantity_withBoundaryValues(int quantity) {
    // 测试边界值处理
}

@Test
void processItems_withEmptyList_handlesGracefully() {
    List<Item> result = service.process(Collections.emptyList());
    assertThat(result).isEmpty();
}
```

### 4.2 数据结构与类型测试

**测试内容**：

- 空值处理（null、Optional.empty()）
- 类型转换（DTO ↔ Entity）
- 默认值验证
- 不可变对象验证

```java
@Test
void mapToDTO_withNullFields_handlesGracefully() {
    User user = new User();
    user.setName(null);  // 空字段

    UserDTO dto = userMapper.toDTO(user);

    assertThat(dto.getName()).isEqualTo("");  // 验证默认值
}

@Test
void createUser_withBuilder_setsDefaultValues() {
    User user = User.builder().name("张三").build();

    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);  // 验证默认状态
    assertThat(user.getCreatedAt()).isNotNull();  // 验证自动时间戳
}
```

### 4.3 并发与性能测试

**并发测试**：

```java
@Test
void incrementCounter_withConcurrentAccess_isThreadSafe() throws Exception {
    int threadCount = 100;
    CountDownLatch latch = new CountDownLatch(threadCount);

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            counter.increment();
            latch.countDown();
        });
    }

    latch.await(5, TimeUnit.SECONDS);
    assertThat(counter.getValue()).isEqualTo(threadCount);
}
```

**性能基准测试**：

```java
@Test
void searchUsers_shouldCompleteWithin100ms() {
    long start = System.currentTimeMillis();

    List<User> users = userService.search(criteria);

    long duration = System.currentTimeMillis() - start;
    assertThat(duration).isLessThan(100);  // 性能要求
}
```

---

## 五、Mock 策略与真实测试

### 5.1 Mock vs 真实依赖决策树

```
需要测试的依赖是什么？
│
├── 数据库
│   ├── 简单查询 → Mock Repository
│   ├── 复杂 SQL/事务 → TestContainers (真实数据库)
│   └── 性能测试 → TestContainers
│
├── 外部 HTTP 服务
│   ├── 可控第三方 → WireMock (录制真实响应)
│   ├── 不可控第三方 → Mock
│   └── 契约测试 → Pact
│
├── 缓存 (Redis)
│   ├── 简单 get/set → Mock
│   └── 复杂逻辑 (过期、分布式锁) → TestContainers
│
├── 消息队列
│   ├── 发送消息 → Mock
│   └── 消费逻辑 → TestContainers
│
└── 内部服务
    ├── 同模块 → 真实对象
    └── 跨模块 → Mock 接口
```

### 5.2 Mock 最佳实践

#### 原则 1：只 Mock 外部依赖

```java
// ❌ 错误：Mock 被测对象
@Mock
private OrderService orderService;  // 不要 Mock 被测对象

// ✅ 正确：Mock 外部依赖
@Mock
private OrderRepository orderRepository;
@Mock
private PaymentClient paymentClient;

@InjectMocks
private OrderService orderService;  // 被测对象用真实实例
```

#### 原则 2：优先使用真实对象

```java
// ✅ 推荐：使用真实的值对象
PriceCalculator calculator = new PriceCalculator();  // 无依赖的工具类

// ❌ 避免：过度 Mock
@Mock
private PriceCalculator calculator;  // 不必要的 Mock
```

#### 原则 3：验证关键交互

```java
@Test
void createOrder_shouldSendNotification() {
    orderService.create(request);

    // 验证调用了通知服务
    verify(notificationService).send(
        eq("order.created"),
        argThat(msg -> msg.contains("订单创建成功"))
    );
}
```

### 5.3 TestContainers 真实环境测试

**使用场景**：

- 数据库事务测试
- 复杂 SQL 验证
- 缓存逻辑测试
- 消息队列集成

**配置示例**：

```java
@SpringBootTest
@Testcontainers
class OrderRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("schema.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void saveOrder_shouldPersistToDatabase() {
        Order order = new Order();
        order.setUserId(1L);

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }
}
```

**性能优化**：

```java
// 容器重用 - 所有测试共享同一容器
@Testcontainers
class IntegrationTestBase {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withReuse(true);  // 启用容器重用
}
```

---

## 六、覆盖率评估标准

### 6.1 覆盖率指标

| 指标         | 目标  | 说明             |
| ------------ | ----- | ---------------- |
| 行覆盖率     | ≥ 70% | 代码行执行比例   |
| 分支覆盖率   | ≥ 60% | if/else 分支覆盖 |
| 方法覆盖率   | ≥ 80% | 方法调用覆盖     |
| 核心业务逻辑 | ≥ 90% | Service 层覆盖率 |

### 6.2 JaCoCo 配置

**Maven 配置**：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 6.3 覆盖率分析策略

**优先级排序**：

1. **核心业务逻辑**（必须 ≥ 90%）
   - 订单处理
   - 支付流程
   - 权限验证
   - 数据计算

2. **重要功能**（必须 ≥ 80%）
   - 用户管理
   - 数据查询
   - 状态流转

3. **辅助功能**（建议 ≥ 70%）
   - 工具类
   - 配置类
   - DTO 转换

4. **可忽略**（无强制要求）
   - 自动生成代码
   - 简单 getter/setter
   - 配置类

**排除配置**：

```xml
<configuration>
    <excludes>
        <exclude>**/dto/**</exclude>
        <exclude>**/entity/**</exclude>
        <exclude>**/config/**</exclude>
        <exclude>**/*Application.class</exclude>
    </excludes>
</configuration>
```

---

## 七、AI 辅助测试生成

### 7.1 AI 生成测试的工作流

```
1. 代码分析阶段
   ├── AI 读取源代码
   ├── 识别方法签名和依赖
   ├── 分析业务逻辑分支
   └── 识别边界条件

2. 测试策略制定
   ├── 确定测试类型（单元/集成）
   ├── 选择 Mock 策略
   ├── 设计测试场景
   └── 生成测试数据

3. 测试代码生成
   ├── 生成测试类框架
   ├── 生成测试方法
   ├── 生成 Mock 配置
   └── 生成断言逻辑

4. 质量检查
   ├── 检查覆盖率
   ├── 验证测试可运行性
   ├── 检查测试独立性
   └── 优化测试可读性
```

### 7.2 AI 生成测试的提示词模板

**基础模板**：

```
请为以下 Java 类生成单元测试：

[粘贴源代码]

要求：
1. 使用 JUnit 5 + Mockito + AssertJ
2. 覆盖所有公共方法
3. 包含正常场景、异常场景、边界条件
4. Mock 所有外部依赖
5. 使用 AAA 模式（Arrange-Act-Assert）
6. 测试方法命名：test{MethodName}_{Scenario}_{ExpectedResult}
7. 目标覆盖率 ≥ 80%
```

**高级模板**：

```
请为以下 Service 类生成完整的测试套件：

[粘贴源代码]

测试要求：
1. 单元测试：
   - Mock Repository 和外部服务
   - 覆盖所有业务逻辑分支
   - 验证异常处理

2. 集成测试：
   - 使用 @SpringBootTest
   - 使用 TestContainers 提供真实数据库
   - 测试事务行为

3. 测试数据：
   - 使用 Faker 生成真实感数据
   - 准备边界值测试用例
   - 提供 JSON 格式的参数化测试数据

4. 断言：
   - 使用 AssertJ 流式断言
   - 验证返回值、异常、Mock 调用
```

### 7.3 AI 生成测试的质量评估

**评估维度**：

| 维度       | 评估标准                     | 权重 |
| ---------- | ---------------------------- | ---- |
| 覆盖率     | 行覆盖 ≥ 80%，分支覆盖 ≥ 70% | 30%  |
| 场景完整性 | 正常/异常/边界场景齐全       | 25%  |
| 可运行性   | 测试可直接运行，无编译错误   | 20%  |
| 可读性     | 命名清晰，结构清晰           | 15%  |
| 独立性     | 测试间无依赖，可并行执行     | 10%  |

**人工复查清单**：

- [ ] 测试是否覆盖核心业务逻辑？
- [ ] 异常场景是否充分测试？
- [ ] Mock 使用是否合理（未过度 Mock）？
- [ ] 测试数据是否真实可信？
- [ ] 断言是否精确（避免过于宽松）？
- [ ] 测试是否独立（无共享状态）？
- [ ] 测试命名是否清晰表达意图？

---

## 八、测试维护策略

### 8.1 需求变更时的测试维护

**变更类型与应对策略**：

| 变更类型 | 测试维护策略  | 示例                        |
| -------- | ------------- | --------------------------- |
| 新增功能 | AI 生成新测试 | 新增支付方式 → 生成支付测试 |
| 修改逻辑 | 更新相关测试  | 折扣规则变更 → 更新计算测试 |
| 删除功能 | 删除对应测试  | 移除旧接口 → 删除接口测试   |
| 重构代码 | 测试保持不变  | 提取方法 → 测试无需改动     |

**自动化维护流程**：

```
1. 代码变更检测
   ├── Git Diff 分析变更文件
   └── 识别影响的测试文件

2. AI 辅助更新
   ├── 分析代码变更内容
   ├── 推荐需要更新的测试
   └── 生成更新后的测试代码

3. 人工审核
   ├── Review AI 生成的测试
   ├── 验证测试逻辑正确性
   └── 运行测试确认通过

4. 提交变更
   ├── 代码 + 测试一起提交
   └── CI 自动验证
```

### 8.2 测试代码审查标准

**Code Review 检查项**：

**基础检查**：

- [ ] 测试类命名：`{ClassName}Test`
- [ ] 测试方法命名：`test{Method}_{Scenario}_{Expected}`
- [ ] 使用 AAA 模式（Arrange-Act-Assert）
- [ ] 每个测试只验证一个行为

**质量检查**：

- [ ] 测试覆盖正常/异常/边界场景
- [ ] Mock 使用合理（未过度 Mock）
- [ ] 断言精确（避免 assertTrue(result != null)）
- [ ] 测试数据有意义（避免 user1、user2）

**可维护性检查**：

- [ ] 测试独立（无共享状态）
- [ ] 测试可读（清晰表达意图）
- [ ] 避免重复代码（提取测试工具方法）
- [ ] 合理使用 @BeforeEach/@AfterEach

**性能检查**：

- [ ] 单元测试执行快速（< 100ms）
- [ ] 避免 Thread.sleep()
- [ ] 合理使用 TestContainers（避免每个测试启动容器）

### 8.3 测试重构与优化

**重构时机**：

- 测试代码重复 > 3 次
- 测试方法过长 > 50 行
- 测试难以理解
- 测试执行缓慢

**重构技巧**：

```java
// ❌ 重复的测试准备代码
@Test
void test1() {
    User user = new User();
    user.setName("张三");
    user.setAge(25);
    // ...
}

@Test
void test2() {
    User user = new User();
    user.setName("李四");
    user.setAge(30);
    // ...
}

// ✅ 提取测试数据构建器
class UserTestBuilder {
    public static User defaultUser() {
        return User.builder()
            .name("张三")
            .age(25)
            .status(UserStatus.ACTIVE)
            .build();
    }

    public static User userWithAge(int age) {
        return defaultUser().toBuilder().age(age).build();
    }
}

@Test
void test1() {
    User user = UserTestBuilder.defaultUser();
    // ...
}
```

---

## 九、CI/CD 集成

### 9.1 GitLab CI 配置

```yaml
# .gitlab-ci.yml
stages:
  - test
  - quality

unit-test:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn clean test
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/site/jacoco/
  coverage: '/Total.*?([0-9]{1,3})%/'

integration-test:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  services:
    - mysql:8.0
    - redis:7-alpine
  variables:
    MYSQL_DATABASE: testdb
    MYSQL_ROOT_PASSWORD: test
  script:
    - mvn verify -P integration-test

quality-gate:
  stage: quality
  script:
    - mvn jacoco:check # 检查覆盖率阈值
  only:
    - merge_requests
    - main
```

### 9.2 测试执行策略

**分层执行**：

```
1. 本地开发
   └── 快速单元测试（< 1 分钟）
       mvn test -Dtest=*Test

2. 提交前
   └── 完整单元测试（< 5 分钟）
       mvn test

3. MR 流水线
   ├── 单元测试
   ├── 集成测试（< 10 分钟）
   └── 覆盖率检查

4. 主分支流水线
   ├── 完整测试套件
   ├── 性能测试
   └── E2E 测试
```

**并行执行**：

```xml
<!-- Maven Surefire 并行配置 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

---

## 十、常见问题与解决方案

### 10.1 AI 生成测试的质量问题

**问题 1：AI 生成的测试覆盖不全**

**解决方案**：

- 提供更详细的提示词，明确要求覆盖场景
- 使用覆盖率报告识别未覆盖分支
- 人工补充边界条件和异常场景测试

**问题 2：AI 生成的测试过度 Mock**

**解决方案**：

- 在提示词中明确 Mock 策略
- 要求 AI 优先使用真实对象
- Code Review 时检查 Mock 合理性

**问题 3：AI 生成的测试数据不真实**

**解决方案**：

- 要求 AI 使用 Faker 生成真实感数据
- 提供业务场景示例
- 从生产日志提取真实数据作为测试用例

### 10.2 Mock 与真实场景的差异

**问题：Mock 行为与真实服务不一致**

**解决方案**：

1. **使用 Contract Testing**

```java
// 消费者定义契约
@Pact(consumer = "order-service", provider = "user-service")
public RequestResponsePact userPact(PactDslWithProvider builder) {
    return builder
        .given("user exists")
        .uponReceiving("get user")
        .path("/users/1")
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(new PactDslJsonBody().integerType("id", 1));
}
```

2. **使用 Recording 测试**

```java
// WireMock 录制真实响应
@Test
void testWithRecordedResponse() {
    // 首次运行录制真实 API 响应
    // 后续运行使用录制的响应
    stubFor(get("/api/users/1")
        .willReturn(aResponse()
            .withBodyFile("user-response.json")));
}
```

3. **定期刷新 Mock 数据**

- 每月从生产环境同步一次 API 响应
- 使用 API 文档自动生成 Mock 数据
- 集成测试使用 TestContainers 验证真实行为

### 10.3 测试维护成本高

**问题：需求变更导致大量测试失败**

**解决方案**：

1. **提高测试抽象层级**

```java
// ❌ 脆弱的测试 - 依赖实现细节
@Test
void test() {
    verify(userRepository).findById(1L);
    verify(userRepository).save(any());
}

// ✅ 健壮的测试 - 关注行为结果
@Test
void test() {
    User result = userService.updateUser(1L, request);
    assertThat(result.getName()).isEqualTo("张三");
}
```

2. **使用测试数据构建器**

```java
// 集中管理测试数据，变更时只需修改一处
class TestDataBuilder {
    public static CreateUserRequest defaultRequest() {
        return CreateUserRequest.builder()
            .name("张三")
            .email("test@example.com")
            .build();
    }
}
```

3. **AI 辅助批量更新**

- 使用 AI 分析代码变更
- 自动识别需要更新的测试
- 批量生成更新后的测试代码

---

## 十一、完整测试示例

### 11.1 Service 层单元测试

```java
package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    // 正常场景测试
    @Test
    void createOrder_withValidRequest_shouldReturnOrder() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .items(List.of(new OrderItem(1L, 2)))
            .build();

        User user = User.builder().id(1L).name("张三").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Order result = orderService.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
    }

    // 异常场景测试
    @Test
    void createOrder_withInvalidUser_shouldThrowException() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(999L)
            .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.create(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户不存在");

        verify(orderRepository, never()).save(any());
    }

    // 边界条件测试
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MAX_VALUE})
    void createOrder_withInvalidQuantity_shouldThrowException(int quantity) {
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .items(List.of(new OrderItem(1L, quantity)))
            .build();

        assertThatThrownBy(() -> orderService.create(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // 交互验证测试
    @Test
    void payOrder_shouldCallPaymentClient() {
        // Arrange
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentClient.pay(any())).thenReturn(PaymentResult.success());

        // Act
        orderService.pay(1L, paymentInfo);

        // Assert
        ArgumentCaptor<PaymentRequest> captor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentClient).pay(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(1L);
    }
}
```

### 11.2 Repository 层集成测试

```java
package com.example.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withInitScript("schema.sql");

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void save_shouldPersistOrder() {
        // Arrange
        Order order = Order.builder()
            .userId(1L)
            .totalAmount(new BigDecimal("100.00"))
            .status(OrderStatus.PENDING)
            .build();

        // Act
        Order saved = orderRepository.save(order);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByUserIdAndStatus_shouldReturnMatchingOrders() {
        // Arrange
        orderRepository.save(Order.builder().userId(1L).status(OrderStatus.PAID).build());
        orderRepository.save(Order.builder().userId(1L).status(OrderStatus.PENDING).build());
        orderRepository.save(Order.builder().userId(2L).status(OrderStatus.PAID).build());

        // Act
        List<Order> result = orderRepository.findByUserIdAndStatus(1L, OrderStatus.PAID);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
```

### 11.3 Controller 层集成测试

```java
package com.example.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_withValidRequest_shouldReturn201() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(1L, items);
        Order order = Order.builder().id(1L).build();
        when(orderService.create(any())).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_withInvalidRequest_shouldReturn400() throws Exception {
        // Arrange - 空请求体

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }
}
```

---

## 十二、实施路线图

### 12.1 第一阶段：基础建设（1-2 周）

**目标**：搭建测试基础设施

**任务清单**：

- [ ] 配置 JUnit 5 + Mockito + AssertJ
- [ ] 配置 JaCoCo 覆盖率工具
- [ ] 配置 TestContainers
- [ ] 编写测试模板和示例
- [ ] 配置 CI/CD 测试流水线

**产出**：

- 测试框架完整配置
- 测试编写指南文档
- CI 自动化测试流水线

### 12.2 第二阶段：核心覆盖（2-4 周）

**目标**：核心业务逻辑测试覆盖 ≥ 80%

**任务清单**：

- [ ] 识别核心业务模块
- [ ] AI 生成核心模块测试
- [ ] 人工审核和优化测试
- [ ] 补充边界和异常场景
- [ ] 达到覆盖率目标

**产出**：

- 核心模块完整测试套件
- 覆盖率报告
- 测试维护文档

### 12.3 第三阶段：全面推广（持续）

**目标**：全项目测试覆盖 ≥ 70%

**任务清单**：

- [ ] 新功能开发同步编写测试
- [ ] 定期 Review 测试质量
- [ ] 优化测试执行速度
- [ ] 建立测试最佳实践库
- [ ] 团队测试培训

**产出**：

- 完整测试体系
- 测试文化建立
- 持续改进机制

---

## 十三、总结

### 13.1 核心要点

1. **测试金字塔**：80% 单元测试 + 15% 集成测试 + 5% E2E 测试
2. **覆盖率目标**：核心业务 ≥ 90%，整体 ≥ 70%
3. **Mock 策略**：优先真实对象，必要时 Mock 外部依赖
4. **AI 辅助**：生成测试 + 质量评估 + 维护更新
5. **持续集成**：CI 自动运行测试，覆盖率门禁

### 13.2 成功关键

- **自动化优先**：测试生成、执行、报告全自动化
- **质量门禁**：覆盖率不达标不允许合并
- **快速反馈**：单测 < 5 分钟，快速发现问题
- **持续优化**：定期 Review 测试质量，优化测试策略
- **团队协作**：测试文化建设，Code Review 重视测试

### 13.3 预期收益

| 收益项   | 预期效果                             |
| -------- | ------------------------------------ |
| 缺陷发现 | 70% 问题在开发阶段发现               |
| 修复成本 | 降低 80%（开发阶段修复 vs 生产修复） |
| 重构信心 | 敢于重构，代码质量持续提升           |
| 开发效率 | 减少返工，整体效率提升 30%           |
| 线上故障 | 减少 60% 线上故障                    |

---

## 附录

### A. 参考资源

- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 官方文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers 官方文档](https://testcontainers.com/)
- [AssertJ 官方文档](https://assertj.github.io/doc/)
- [JaCoCo 官方文档](https://www.jacoco.org/jacoco/trunk/doc/)

### B. 工具对比

| 工具           | 优势               | 劣势                | 推荐场景  |
| -------------- | ------------------ | ------------------- | --------- |
| Mockito        | 简单易用，社区活跃 | 不支持 static/final | 通用 Mock |
| PowerMock      | 支持 static/final  | 性能差，维护少      | 遗留代码  |
| WireMock       | HTTP Mock 强大     | 配置复杂            | 外部 API  |
| TestContainers | 真实环境           | 启动慢              | 集成测试  |
| H2             | 快速轻量           | SQL 兼容性差        | 简单查询  |

### C. 常用注解速查

```java
// JUnit 5
@Test                    // 测试方法
@BeforeEach             // 每个测试前执行
@AfterEach              // 每个测试后执行
@BeforeAll              // 所有测试前执行一次
@AfterAll               // 所有测试后执行一次
@Disabled               // 禁用测试
@ParameterizedTest      // 参数化测试
@RepeatedTest(10)       // 重复测试

// Mockito
@Mock                   // 创建 Mock 对象
@InjectMocks            // 注入 Mock 到被测对象
@Spy                    // 部分 Mock
@Captor                 // 参数捕获器

// Spring Boot Test
@SpringBootTest         // 完整应用测试
@WebMvcTest            // Controller 测试
@DataJpaTest           // Repository 测试
@MockBean              // Spring 容器中的 Mock
```
