# 测试策略选型指南

## 一、选型决策树

```
需要测试什么？
│
├── 纯函数/算法逻辑
│   └── Property-Based Testing（属性驱动测试）
│       └── 工具：fast-check、Hypothesis、jqwik
│
├── 数据库交互
│   ├── 需要验证 SQL 方言/事务 → 真实事务测试
│   │   └── 工具：Spring @Transactional、TestContainers
│   └── 一般查询操作 → TestContainers
│       └── 工具：testcontainers-java、testcontainers-node
│
├── 外部 API 调用
│   ├── 能录制真实响应 → Recording 测试
│   │   └── 工具：VCR.py、Polly.js、WireMock
│   ├── 有契约定义 → Contract 测试
│   │   └── 工具：Pact、Spring Cloud Contract
│   └── 无法控制第三方 → Mock（兜底方案）
│       └── 工具：Mockito、Jest Mock
│
├── 复杂数据输出
│   ├── 输出结构复杂 → Snapshot 测试
│   │   └── 工具：Jest Snapshot、ApprovalTests
│   └── 需要精确比对 → Golden Master 测试
│       └── 工具：ApprovalTests
│
├── 输入边界/安全性
│   └── Fuzzing 模糊测试
│       └── 工具：AFL、Go Fuzzing、JQF
│
├── 多场景业务规则
│   └── 表驱动集成测试
│       └── 工具：JUnit 参数化、pytest.mark.parametrize
│
└── 完整业务流程
    └── E2E + 真实依赖
        └── 工具：Playwright、Selenium、REST Assured
```

---

## 二、策略对比表

| 策略 | 速度 | 真实性 | 维护成本 | 适用场景 | AI 辅助 |
|------|------|--------|----------|----------|---------|
| **单元测试** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ | 函数/类逻辑 | ✅ 自动生成 |
| **TestContainers** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 数据库集成 | ✅ 生成 schema |
| **Property-Based** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | 算法/数据转换 | ✅ 推导属性 |
| **Snapshot** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | API 响应/报表 | ✅ 对比差异 |
| **Contract** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | 微服务接口 | ✅ 从 OpenAPI 生成 |
| **Recording** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | 外部 API | ✅ 自动脱敏 |
| **Golden Master** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 遗留系统 | ✅ 分析差异 |
| **Fuzzing** | ⭐⭐ | ⭐⭐⭐⭐ | ⭐ | 输入验证/安全 | ✅ 生成用例 |
| **E2E** | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 核心业务流程 | ✅ 生成场景 |

---

## 三、11种测试策略详解

### 策略 1：JSON 驱动的参数化测试

**核心理念**：将测试用例与测试代码分离，用 JSON 文件定义输入数据和期望输出。

**适用场景**：
- 多组相似输入的边界条件测试
- 业务规则验证
- 数据驱动的基础算法测试
- 需要非技术人员提供测试数据的场景

**示例**：
```javascript
// test-cases/user-login.json
[
  { "input": { "username": "admin", "password": "123456" }, "expected": { "success": true } },
  { "input": { "username": "", "password": "123456" }, "expected": { "success": false, "error": "用户名不能为空" } }
]

// 测试代码
test.each(testCases)("登录测试: $input.username", ({ input, expected }) => {
  const result = loginService.authenticate(input.username, input.password);
  expect(result.success).toBe(expected.success);
});
```

---

### 策略 2：TestContainers（真实容器测试）

**核心理念**：在 Docker 容器中启动真实的数据库、消息队列、缓存服务，测试结束自动销毁。

**适用场景**：
- 数据库集成测试（MySQL、PostgreSQL、MongoDB）
- 消息队列测试（Kafka、RabbitMQ）
- 缓存服务测试（Redis、Memcached）

**Java 示例**：
```java
@Testcontainers
class UserRepositoryTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withInitScript("schema.sql");

    @Test
    void shouldFindUserById() {
        User user = userRepository.findById(1L);
        assertThat(user).isNotNull();
    }
}
```

---

### 策略 3：Property-Based Testing（属性驱动测试）

**核心理念**：不关注具体输入值，而是定义数据的"属性"和不变式，让框架自动生成海量随机数据验证。

**适用场景**：
- 算法验证（排序、搜索、计算）
- 数据转换逻辑（DTO 映射、格式转换）
- 业务规则验证（金额计算、日期处理）

**JavaScript 示例**：
```javascript
const fc = require("fast-check");

// 属性：排序后数组长度不变
fc.assert(
  fc.property(fc.array(fc.integer()), (arr) => {
    const sorted = arr.sort((a, b) => a - b);
    return sorted.length === arr.length;
  }),
);
```

---

### 策略 4：Snapshot / Approval Testing（快照/批准测试）

**核心理念**：首次运行生成"正确结果"快照文件，后续运行与快照对比，差异需人工批准更新。

**适用场景**：
- API 响应结构验证（JSON/XML 响应）
- 复杂报表数据比对
- 邮件/通知内容验证
- UI 组件渲染结果验证

**JavaScript 示例**：
```javascript
test("API response matches snapshot", async () => {
  const response = await api.getUser(1);
  expect(response).toMatchSnapshot();
});
```

---

### 策略 5：Contract Testing（契约测试）

**核心理念**：消费者定义期望的"契约"（请求/响应格式），提供者验证是否满足契约，双方独立测试。

**适用场景**：
- 微服务间接口依赖
- 前后端 API 契约
- 第三方 API 集成

**Java + Pact 示例**：
```java
@Pact(consumer = "order-service", provider = "user-service")
public RequestResponsePact userExistsPact(PactDslWithProvider builder) {
    return builder
        .given("user exists")
        .uponReceiving("get user by id")
        .path("/users/1")
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(new PactDslJsonBody()
            .integerType("id", 1)
            .stringType("name", "John"));
}
```

---

### 策略 6：Recording / VCR Testing（录制回放测试）

**核心理念**：首次调用真实外部服务并记录请求/响应，后续测试使用录制数据回放，无需真实网络调用。

**适用场景**：
- 外部 API 调用测试
- 支付网关集成
- 短信/邮件服务商调用

**Python 示例**：
```python
import vcr

@vcr.use_cassette('fixtures/cassettes/github_api.yml')
def test_get_user():
    # 首次运行会真实调用 GitHub API 并录制
    # 后续运行直接读取录制的响应
    response = requests.get('https://api.github.com/users/octocat')
    assert response.json()['login'] == 'octocat'
```

---

### 策略 7：Golden Master Testing（金主测试）

**核心理念**：将系统输出与已批准的"金标准"输出比较，适用于复杂系统端到端验证。

**适用场景**：
- 遗留系统无测试时的安全网
- 重构前的基准建立
- 复杂计算结果验证（财务计算、报表生成）

**Java 示例**：
```java
@Test
void testComplexReport() {
    Report report = reportService.generateMonthlyReport();
    String output = report.toString();
    Approvals.verify(output);
}
```

---

### 策略 8：Fuzzing（模糊测试）

**核心理念**：自动生成随机/畸形输入数据，发现边界条件、异常处理漏洞。

**适用场景**：
- 输入验证逻辑
- 解析器（JSON、XML、CSV 解析）
- 文件上传处理
- 安全性测试

**Go 示例**：
```go
func FuzzParseUserInput(f *testing.F) {
    f.Add("valid@email.com", 25)
    f.Fuzz(func(t *testing.T, email string, age int) {
        user, err := ParseUserInput(email, age)
        if err != nil {
            return
        }
        if user.Age != age {
            t.Errorf("age mismatch")
        }
    })
}
```

---

### 策略 9：Table-Driven Integration Tests（表驱动集成测试）

**核心理念**：将多组真实场景数据整理成表格，同一套测试逻辑遍历所有场景，但使用真实依赖。

**适用场景**：
- 复杂业务规则验证
- 多边界条件组合测试
- API 接口多场景测试

**Go 示例**：
```go
func TestUserCreation(t *testing.T) {
    tests := []struct {
        name     string
        input    CreateUserRequest
        wantErr  bool
        wantCode int
    }{
        {name: "正常创建", input: validRequest, wantErr: false, wantCode: 201},
        {name: "邮箱已存在", input: duplicateRequest, wantErr: true, wantCode: 409},
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            result, err := service.Create(tt.input)
            if (err != nil) != tt.wantErr {
                t.Errorf("error = %v, wantErr %v", err, tt.wantErr)
            }
        })
    }
}
```

---

### 策略 10：Database Integration Testing with Real Transactions（真实事务测试）

**核心理念**：每个测试在真实数据库上执行，测试后回滚事务，数据不留痕迹。

**适用场景**：
- 验证真实 SQL 语句执行
- 测试事务行为（回滚、隔离级别）
- 发现 ORM 映射问题
- 触发器、存储过程测试

**Java 示例**：
```java
@SpringBootTest
@Transactional  // 测试后自动回滚
class OrderServiceTest {
    @Test
    void shouldRollbackOnPaymentFailure() {
        Order order = orderService.createOrder(items);
        Long orderId = order.getId();

        assertThrows(PaymentException.class, () -> {
            orderService.pay(orderId, invalidCard);
        });

        // 验证订单状态回滚
        Order unchanged = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.PENDING, unchanged.getStatus());
    }
    // 测试结束后事务回滚，数据不污染数据库
}
```

---

### 策略 11：E2E API Testing with Real Dependencies（真实依赖 E2E 测试）

**核心理念**：启动完整应用，调用真实 API，使用 TestContainers 提供真实中间件，最接近用户真实使用场景。

**适用场景**：
- 端到端业务流程验证
- 部署前的最终验证
- 关键业务路径回归测试

**JavaScript 示例**：
```javascript
describe("订单流程 E2E", () => {
  test("完整下单流程", async () => {
    // 1. 注册用户
    const user = await request(app)
      .post("/api/users")
      .send({ name: "张三", email: "zs@example.com" })
      .expect(201);

    // 2. 创建订单
    const order = await request(app)
      .post("/api/orders")
      .set("Authorization", `Bearer ${user.body.token}`)
      .expect(201);

    // 3. 支付订单
    await request(app)
      .post(`/api/orders/${order.body.id}/pay`)
      .expect(200);

    // 4. 验证订单状态
    const result = await request(app)
      .get(`/api/orders/${order.body.id}`)
      .expect(200);

    expect(result.body.status).toBe("PAID");
  });
});
```

---

## 四、Mock 降级路径

当真实测试不可行时，按以下顺序降级：

```
首选：TestContainers（真实容器）
  ↓ 无法使用容器（如无 Docker 环境）
备选：Recording 测试（录制真实响应）
  ↓ 无法录制（如第三方不允许）
备选：Contract 测试（基于契约）
  ↓ 无契约定义
兜底：Mock（人工编写假数据）
```

---

## 五、AI 辅助测试生成工作流

```
1. 代码分析
   AI 扫描源码 → 识别测试切入点
              → 推荐测试策略
              → 识别高风险代码

2. 数据准备
   AI 分析 Schema → 生成测试数据
                → 识别边界值
                → 从生产日志提取模式

3. 测试生成
   AI 生成测试代码 → Property 定义
                  → JSON 测试用例
                  → Mock/Recording 配置

4. 结果验证
   AI 分析测试失败 → 分类失败原因
                  → 推荐修复方案
                  → 判断 Snapshot 变更是否合法

5. 持续优化
   AI 分析覆盖率 → 识别未覆盖路径
                → 推荐新增用例
                → 优化测试执行顺序
```

---

## 六、参考文档

- [测试体系架构](../design/architecture.md) - 整体架构设计
- [测试流程说明](../design/workflow.md) - 测试工作流程详解
- [Java 单元测试](../unit/java.md) - Java 具体实现方案
- [业界大厂实践调研](./report.md) - 国内外大厂测试实践调研
