<!-- @format -->

# 单元测试

## 目标

- 后 AI 时代，代码生成很快，也很便宜，如何让借助 AI 提高代码生成质量，确保少出错，不出错，降低人工测试成本。

## 痛点问题分析

- 测试用例准备不够。如部分逻辑分支未考虑到，测试数据准备不够充分等；
- 测试依赖其他环境，如数据库，第三方请求等；
- 如何测试性能问题

## 思路

### 思路 1：JSON 驱动的参数化测试

**核心理念**：将测试用例与测试代码分离，用 JSON 文件定义输入数据和期望输出，同一套测试逻辑读取不同 JSON 文件执行多组测试。

**适用场景**：

- 多组相似输入的边界条件测试
- 业务规则验证（不同输入产生不同输出）
- 数据驱动的基础算法测试
- 需要非技术人员提供测试数据的场景

**真实 vs Mock 对比**：

| 对比项     | JSON 驱动（推荐）             | 传统硬编码              |
| ---------- | ----------------------------- | ----------------------- |
| 用例维护   | JSON 文件独立管理，无需改代码 | 修改需重新编译          |
| 可读性     | 非技术人员可理解和补充        | 需要看懂代码            |
| AI 生成    | AI 可直接生成 JSON 格式用例   | AI 生成后需人工插入代码 |
| 执行速度   | 快（纯内存）                  | 快（纯内存）            |
| 依赖真实性 | 仅测试纯函数逻辑              | 仅测试纯函数逻辑        |

**工具推荐**：

- Java：JUnit 参数化测试 + Jackson
- JavaScript：Jest `test.each` / `describe.each`
- Python：pytest `@pytest.mark.parametrize` + JSON 加载
- Go：`testing` 包 + `encoding/json`

**AI 结合点**：

- AI 分析源码自动生成边界值 JSON 用例
- AI 根据函数签名推导输入/输出格式并生成模板
- AI 对比实际输出与期望输出的差异，推荐修正

**JavaScript 示例**：

```javascript
// test-cases/user-login.json
// [
//   { "input": { "username": "admin", "password": "123456" }, "expected": { "success": true, "code": 200 } },
//   { "input": { "username": "", "password": "123456" }, "expected": { "success": false, "code": 400, "error": "用户名不能为空" } },
//   { "input": { "username": "admin", "password": "wrong" }, "expected": { "success": false, "code": 401, "error": "密码错误" } }
// ]

const testCases = require('./test-cases/user-login.json');

test.each(testCases)('登录测试: $input.username', ({ input, expected }) => {
  const result = loginService.authenticate(input.username, input.password);
  expect(result.success).toBe(expected.success);
  expect(result.code).toBe(expected.code);
  if (expected.error) {
    expect(result.error).toBe(expected.error);
  }
});
```

**注意事项**：

- JSON 文件需要版本控制
- 敏感数据（真实密码、Token）需使用占位符或环境变量
- 测试数据量过大时考虑拆分为多个文件
- 复杂的对象结构可以用 YAML 替代 JSON 提高可读性

---

### 思路 2：TestContainers（真实容器测试）

**核心理念**：在 Docker 容器中启动真实的数据库、消息队列、缓存服务，测试结束自动销毁。

**适用场景**：

- 数据库集成测试（MySQL、PostgreSQL、MongoDB）
- 消息队列测试（Kafka、RabbitMQ）
- 缓存服务测试（Redis、Memcached）

**真实 vs Mock 对比**：

| 对比项     | TestContainers（真实） | Mock（内存数据库） |
| ---------- | ---------------------- | ------------------ |
| SQL 兼容性 | 100% 兼容              | 部分语法不支持     |
| 事务行为   | 真实事务               | 模拟事务           |
| 性能测试   | 可验证真实性能         | 无参考价值         |
| 启动速度   | 较慢（秒级）           | 快（毫秒级）       |

**工具推荐**：

- Java：[TestContainers](https://testcontainers.com/)
- Node.js：[testcontainers-node](https://github.com/testcontainers/testcontainers-node)
- Python：[testcontainers-python](https://github.com/testcontainers/testcontainers-python)
- Go：[testcontainers-go](https://github.com/testcontainers/testcontainers-go)

**AI 结合点**：

- AI 分析 SQL 语句自动生成数据库初始化的 schema 和 seed 数据
- 根据业务逻辑推导需要测试的数据库隔离级别

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
        // 使用真实的 MySQL 连接测试
        User user = userRepository.findById(1L);
        assertThat(user).isNotNull();
    }
}
```

**注意事项**：

- CI 环境需要 Docker 支持
- 并行测试时需要端口冲突处理
- 合理设置容器重用策略优化速度

---

### 思路 3：Property-Based Testing（属性驱动测试）

**核心理念**：不关注具体输入值，而是定义数据的"属性"和不变式，让框架自动生成海量随机数据验证。

**适用场景**：

- 算法验证（排序、搜索、计算）
- 数据转换逻辑（DTO 映射、格式转换）
- 业务规则验证（金额计算、日期处理）

**示例属性**：

- 排序后结果长度不变
- 反转两次等于原字符串
- 金额计算结果 >= 0
- 分页查询返回结果数 <= pageSize

**真实 vs Mock 对比**：

- 不需要 Mock，因为是纯函数测试
- 基于真实数据属性，比人工设计用例覆盖面更广

**工具推荐**：

- Java：[jqwik](https://jqwik.net/)、[junit-quickcheck](https://github.com/pholser/junit-quickcheck)
- JavaScript/TypeScript：[fast-check](https://github.com/dubzzz/fast-check)
- Python：[Hypothesis](https://hypothesis.readthedocs.io/)
- Go：[gopter](https://github.com/leanovate/gopter)

**AI 结合点**：

- AI 分析代码逻辑自动推导出应满足的属性
- 从生产日志中提取数据分布特征生成更贴近真实的随机数据

**JavaScript 示例**：

```javascript
const fc = require('fast-check');

// 属性：排序后数组长度不变
fc.assert(
  fc.property(fc.array(fc.integer()), (arr) => {
    const sorted = arr.sort((a, b) => a - b);
    return sorted.length === arr.length;
  }),
);

// 属性：反转两次等于原字符串
fc.assert(fc.property(fc.string(), (str) => str.split('').reverse().reverse().join('') === str));
```

**注意事项**：

- 属性定义需要数学思维，初期学习成本较高
- 失败时会提供最小可复现的输入值（shrinking）
- 对有副作用的代码不适用

---

### 思路 4：Snapshot / Approval Testing（快照/批准测试）

**核心理念**：首次运行生成"正确结果"快照文件，后续运行与快照对比，差异需人工批准更新。

**适用场景**：

- API 响应结构验证（JSON/XML 响应）
- 复杂报表数据比对
- 邮件/通知内容验证
- UI 组件渲染结果验证

**真实 vs Mock 对比**：

- 基于真实输出，无需人工编写期望值
- 可捕捉意外的格式变更
- 缺点：需要人工审核快照变更

**工具推荐**：

- JavaScript：[Jest Snapshot](https://jestjs.io/docs/snapshot-testing)
- Java：[ApprovalTests](https://approvaltests.com/)
- Go：[Verify](https://github.com/VerifyTests/Verify.Go)
- .NET：[Verify](https://github.com/VerifyTests/Verify)

**AI 结合点**：

- AI 辅助判断快照变更是否合法（如只是 ID 变化 vs 结构变化）
- 自动生成人类可读的变更描述

**JavaScript 示例**：

```javascript
// 首次运行生成快照，后续对比
test('API response matches snapshot', async () => {
  const response = await api.getUser(1);
  expect(response).toMatchSnapshot();
});

// 带名称的快照
test('complex report', async () => {
  const report = await generateReport();
  expect(report).toMatchSnapshot('monthly-sales-report');
});
```

**注意事项**：

- 快照文件需要版本控制
- 敏感数据（密码、Token）需要过滤
- 团队需要约定快照更新流程

---

### 思路 5：Contract Testing（契约测试）

**核心理念**：消费者定义期望的"契约"（请求/响应格式），提供者验证是否满足契约，双方独立测试。

**适用场景**：

- 微服务间接口依赖
- 前后端 API 契约
- 第三方 API 集成

**真实 vs Mock 对比**：

| 方式     | 说明                               |
| -------- | ---------------------------------- |
| Contract | 基于真实契约定义，双方独立验证     |
| Mock     | 人工编写假数据，可能与真实服务脱节 |

**工具推荐**：

- 多语言：[Pact](https://pact.io/)
- Java：[Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract)

**AI 结合点**：

- 从 OpenAPI/Swagger 文档自动生成契约
- 从真实流量中分析并生成契约定义

**Java + Pact 示例**：

```java
// 消费者定义契约
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
            .stringType("name", "John")
            .stringType("email", "john@example.com"));
}
```

**注意事项**：

- 契约变更需要消费者和提供者协商
- 需要契约仓库（Pact Broker）管理版本
- 契约测试通过不代表功能正确

---

### 思路 6：Recording / VCR Testing（录制回放测试）

**核心理念**：首次调用真实外部服务并记录请求/响应，后续测试使用录制数据回放，无需真实网络调用。

**适用场景**：

- 外部 API 调用测试
- 支付网关集成
- 短信/邮件服务商调用

**与 Mock 的区别**：

- **Mock** = 人工编写的假数据
- **VCR** = 从真实响应录制，更贴近生产环境

**工具推荐**：

- Python：[VCR.py](https://vcrpy.readthedocs.io/)
- Ruby：[VCR](https://relishapp.com/vcr/vcr/docs)
- JavaScript：[Polly.JS](https://netflix.github.io/pollyjs/)、[nock](https://github.com/nock/nock#recording)
- Java：[WireMock Record](http://wiremock.org/docs/record-playback/)、[Betamax](https://github.com/betamaxteam/betamax)

**AI 结合点**：

- AI 分析录制的响应，识别敏感信息并自动脱敏
- 从录制数据中提取模式生成更多测试场景

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

**注意事项**：

- 录制的 Cassette 文件需要版本控制
- 定期刷新录制以捕获 API 变更
- 敏感信息需要过滤或加密存储

---

### 思路 7：Golden Master Testing（金主测试）

**核心理念**：将系统输出与已批准的"金标准"输出比较，适用于复杂系统端到端验证。

**适用场景**：

- 遗留系统无测试时的安全网
- 重构前的基准建立
- 复杂计算结果验证（财务计算、报表生成）

**真实 vs Mock 对比**：

- 完全基于真实行为
- 不侵入业务代码
- 适合黑盒验证

**工具推荐**：

- 多语言：[ApprovalTests](https://approvaltests.com/)
- Python：[ApprovalTests.Python](https://github.com/approvals/ApprovalTests.Python)
- Java：[ApprovalTests.Java](https://github.com/approvals/ApprovalTests.Java)

**AI 结合点**：

- AI 分析差异并分类（数值精度 vs 逻辑错误）
- 自动识别可以忽略的无关差异

**Java 示例**：

```java
@Test
void testComplexReport() {
    // 生成复杂报表
    Report report = reportService.generateMonthlyReport();
    String output = report.toString();

    // 与已批准的金标准比较
    Approvals.verify(output);
}
```

**注意事项**：

- 需要有明确的"批准"流程
- 变更频繁时维护成本高
- 适合输出相对稳定的场景

---

### 思路 8：Fuzzing（模糊测试）

**核心理念**：自动生成随机/畸形输入数据，发现边界条件、异常处理漏洞。

**适用场景**：

- 输入验证逻辑
- 解析器（JSON、XML、CSV 解析）
- 文件上传处理
- 安全性测试

**真实 vs Mock 对比**：

- 黑盒/灰盒测试，不修改业务代码
- 可以发现人工难以想到的边界情况

**工具推荐**：

- C/C++：[AFL](https://github.com/google/AFL)、[libFuzzer](https://llvm.org/docs/LibFuzzer.html)
- Go：[Go Fuzzing](https://go.dev/doc/security/fuzz/)
- Java：[JQF](https://github.com/rohanpadhye/JQF)
- Python：[atheris](https://github.com/google/atheris)

**AI 结合点**：

- 基于 API 结构生成有意义的模糊输入（而非纯随机）
- 从生产日志提取真实输入模式指导模糊策略
- 优先测试 AI 识别的高风险代码路径

**Go 示例**：

```go
func FuzzParseUserInput(f *testing.F) {
    // 提供种子语料
    f.Add("valid@email.com", 25)
    f.Add("invalid-email", -1)

    f.Fuzz(func(t *testing.T, email string, age int) {
        // 模糊测试会自动生成各种变体
        user, err := ParseUserInput(email, age)
        if err != nil {
            // 错误处理逻辑
            return
        }
        // 验证解析结果
        if user.Age != age {
            t.Errorf("age mismatch")
        }
    })
}
```

**注意事项**：

- 可能需要长时间运行才能发现深层问题
- 发现的问题可能需要人工分析是否为真实漏洞
- 适合作为 CI 的定时任务而非每次提交运行

---

### 思路 9：Table-Driven Integration Tests（表驱动集成测试）

**核心理念**：将多组真实场景数据整理成表格，同一套测试逻辑遍历所有场景，但使用真实依赖。

**适用场景**：

- 复杂业务规则验证
- 多边界条件组合测试
- API 接口多场景测试

**真实 vs Mock 对比**：

- 与"思路 1"的 JSON 驱动类似，但强调使用真实依赖
- 数据库使用真实 TestContainers，API 使用真实服务或 Recording

**工具推荐**：

- Go 语言原生支持（testing 包）
- Java：JUnit 参数化测试 + TestContainers
- JavaScript：Jest 的 `test.each`

**AI 结合点**：

- AI 分析代码逻辑自动生成边界值组合
- 从生产日志提取高频场景作为测试数据

**Go 示例**：

```go
func TestUserCreation(t *testing.T) {
    tests := []struct {
        name     string
        input    CreateUserRequest
        dbSeed   string  // 数据库种子文件
        wantErr  bool
        wantCode int
    }{
        {
            name:     "正常创建",
            input:    CreateUserRequest{Name: "张三", Email: "zs@example.com"},
            dbSeed:   "empty.sql",
            wantErr:  false,
            wantCode: 201,
        },
        {
            name:     "邮箱已存在",
            input:    CreateUserRequest{Name: "李四", Email: "exists@example.com"},
            dbSeed:   "seed_with_user.sql",
            wantErr:  true,
            wantCode: 409,
        },
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            // 使用真实数据库容器
            db := setupTestDB(tt.dbSeed)
            service := NewUserService(db)

            result, err := service.Create(tt.input)

            if (err != nil) != tt.wantErr {
                t.Errorf("error = %v, wantErr %v", err, tt.wantErr)
            }
            if result.Code != tt.wantCode {
                t.Errorf("code = %d, want %d", result.Code, tt.wantCode)
            }
        })
    }
}
```

**注意事项**：

- 表格设计要清晰易读
- 每个测试用例应有独立的数据库初始状态
- 避免表格过于庞大导致维护困难

---

### 思路 10：Database Integration Testing with Real Transactions（真实事务测试）

**核心理念**：每个测试在真实数据库上执行，测试后回滚事务，数据不留痕迹。

**适用场景**：

- 验证真实 SQL 语句执行
- 测试事务行为（回滚、隔离级别）
- 发现 ORM 映射问题
- 触发器、存储过程测试

**真实 vs Mock 对比**：

| 对比项   | 真实事务           | Mock 数据库 |
| -------- | ------------------ | ----------- |
| SQL 方言 | 支持特定数据库方言 | 通用 SQL    |
| 性能     | 略慢               | 快          |
| 准确性   | 100% 生产一致      | 可能有差异  |
| 事务测试 | 真实               | 模拟        |

**工具推荐**：

- Java：Spring `@Transactional` 测试、@DataJpaTest
- Ruby：Database Cleaner
- Python：pytest-django 的 transaction 测试
- Go：手动事务回滚

**AI 结合点**：

- AI 分析数据库操作自动生成回滚验证逻辑
- 检测可能导致死锁的并发测试场景

**Java 示例**：

```java
@SpringBootTest
@Transactional  // 测试后自动回滚
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldRollbackOnPaymentFailure() {
        // 创建订单
        Order order = orderService.createOrder(items);
        Long orderId = order.getId();

        // 模拟支付失败
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

**注意事项**：

- 并发测试时需要特殊处理（事务隔离）
- 涉及多数据源时需要额外配置
- 测试执行速度比纯内存测试慢

---

### 思路 11：E2E API Testing with Real Dependencies（真实依赖 E2E 测试）

**核心理念**：启动完整应用，调用真实 API，使用 TestContainers 提供真实中间件，最接近用户真实使用场景。

**适用场景**：

- 端到端业务流程验证
- 部署前的最终验证
- 关键业务路径回归测试

**测试层级金字塔**：

```
        /\
       /  \     E2E 测试（少量，完整应用）
      /----\
     /      \   集成测试（中等，真实容器）
    /--------\
   /          \ 单元测试（大量，隔离/轻量依赖）
  /------------\
```

**工具推荐**：

- Java：[REST Assured](https://rest-assured.io/)
- JavaScript：[SuperTest](https://github.com/ladjs/supertest)、[Playwright API Testing](https://playwright.dev/docs/api-testing)
- Python：[Requests](https://requests.readthedocs.io/) + [pytest](https://docs.pytest.org/)
- Go：[httpexpect](https://github.com/gavv/httpexpect)

**AI 结合点**：

- AI 从 API 文档和用户行为日志生成 E2E 测试场景
- 自动识别核心业务路径优先测试
- 基于变更影响分析选择运行哪些 E2E 测试

**JavaScript 示例**：

```javascript
const request = require('supertest');
const { setupTestApp } = require('./test-helpers');

describe('订单流程 E2E', () => {
  let app;

  beforeAll(async () => {
    // 启动完整应用 + TestContainers（MySQL, Redis）
    app = await setupTestApp();
  });

  afterAll(async () => {
    await app.stop();
  });

  test('完整下单流程', async () => {
    // 1. 注册用户
    const user = await request(app).post('/api/users').send({ name: '张三', email: 'zs@example.com' }).expect(201);

    // 2. 创建订单
    const order = await request(app)
      .post('/api/orders')
      .set('Authorization', `Bearer ${user.body.token}`)
      .send({ items: [{ productId: 1, quantity: 2 }] })
      .expect(201);

    // 3. 支付订单
    await request(app).post(`/api/orders/${order.body.id}/pay`).send({ cardNumber: '4111111111111111' }).expect(200);

    // 4. 验证订单状态
    const result = await request(app).get(`/api/orders/${order.body.id}`).expect(200);

    expect(result.body.status).toBe('PAID');
  });
});
```

**注意事项**：

- 执行速度慢，不应频繁运行
- 需要完善的测试数据准备和清理机制
- 失败时定位问题较困难
- 建议使用专门的测试环境

---

## 测试策略选择决策树

```
需要测试什么？
│
├── 纯函数/算法逻辑
│   └── Property-Based Testing（思路 3）
│
├── 数据库交互
│   ├── 需要验证 SQL 方言/事务 → 真实事务测试（思路 10）
│   └── 一般查询操作 → TestContainers（思路 2）
│
├── 外部 API 调用
│   ├── 能录制真实响应 → Recording 测试（思路 6）
│   ├── 有契约定义 → Contract 测试（思路 5）
│   └── 无法控制第三方 → Mock（兜底方案）
│
├── 复杂数据输出
│   ├── 输出结构复杂 → Snapshot 测试（思路 4）
│   └── 需要精确比对 → Golden Master（思路 7）
│
├── 输入边界/安全性
│   └── Fuzzing（思路 8）
│
├── 多场景业务规则
│   └── 表驱动集成测试（思路 9）
│
└── 完整业务流程
    └── E2E + 真实依赖（思路 11）
```

## Mock 降级路径

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

## AI 辅助测试生成工作流

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
