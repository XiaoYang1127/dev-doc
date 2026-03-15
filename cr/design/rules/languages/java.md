# Java 代码审核规则库

> 基于 plan.md 审核维度设计的 Java 技术栈具体规则实现

---

## 目录

1. [片段审核规则 (Layer 2)](#1-片段审核规则-layer-2)
2. [上下文审核规则 (Layer 3)](#2-上下文审核规则-layer-3)
3. [架构审核规则 (Layer 4)](#3-架构审核规则-layer-4)
4. [调用链审核规则 (Layer 5)](#4-调用链审核规则-layer-5)
5. [数据流审核规则 (Layer 6)](#5-数据流审核规则-layer-6)
6. [安全审核规则 (Layer 7)](#6-安全审核规则-layer-7)
7. [性能审核规则 (Layer 8)](#7-性能审核规则-layer-8)
8. [全量扫描规则 (Layer 9)](#8-全量扫描规则-layer-9)

---

## 1. 片段审核规则 (Layer 2)

### 1.1 语法与基础缺陷

#### JAVA-L2-001: 对象相等性误用

```yaml
id: java.fragment.object-equality
name: 对象相等性误用
description: 使用 == 比较对象而非 equals() 方法
category: logic
severity: error
layer: 2
language: java
scope: expression

condition:
  type: ast
  pattern: |
    //BinaryExpr[@operator='==']
    [
      descendant::Name[type-resolution='java.lang.String'] or
      descendant::Name[type-resolution='java.lang.Integer'] or
      descendant::Name[type-resolution='java.lang.Long'] or
      descendant::Name[type-resolution='java.lang.Double'] or
      descendant::Name[type-resolution='java.math.BigDecimal']
    ]

message:
  template: "使用 '==' 比较对象 '{operand_type}'，应使用 equals()"
  suggestion: "使用 Objects.equals() 或手动调用 equals() 进行对象比较"
  example_good: |
    if ("active".equals(status)) { ... }
    if (Objects.equals(a, b)) { ... }
  example_bad: |
    if (status == "active") { ... }  // 错误！
    if (a == b) { ... }  // 对于对象是错误的

meta:
  confidence: 0.95
  auto_fixable: true
  tags: [fragment, equality, string, object]
```

#### JAVA-L2-002: 空指针链式调用

```yaml
id: java.fragment.null-chain
description: 多层链式调用存在 NPE 风险
category: npe
severity: warning
layer: 2
condition:
  type: ast
  pattern: |
    //MethodCallExpr[
      count(descendant::MethodCallExpr) >= 2
    ][
      not(ancestor::IfStmt[contains(@condition, '!=') and contains(@condition, 'null')])
    ]
message:
  template: "链式调用缺少空值检查，可能导致 NPE"
  suggestion: "使用 Optional 或提前进行空值判断"
meta:
  confidence: 0.80
  tags: [fragment, npe, chain]
```

#### JAVA-L2-003: 数组越界风险

```yaml
id: java.fragment.array-index
description: 数组/列表访问缺少边界检查
category: logic
severity: warning
layer: 2
condition:
  type: ast
  pattern: |
    //ArrayAccessExpr[
      not(preceding::IfStmt[contains(@condition, '.length') or contains(@condition, '.size()')])
    ]
message:
  template: "数组/列表访问可能越界"
  suggestion: "访问前检查长度或使用安全的方法如 getOrDefault"
meta:
  confidence: 0.75
  tags: [fragment, array, index]
```

#### JAVA-L2-004: 资源未关闭

```yaml
id: java.fragment.resource-leak
description: JDBC 连接、IO 流等资源未在 finally 中关闭
category: resource
severity: error
layer: 2
condition:
  type: ast
  pattern: |
    //VariableDeclarationExpr[
      contains(Type, 'Connection|Statement|ResultSet|InputStream|OutputStream|Reader|Writer')
    ][
      not(descendant::MethodCallExpr[@name='close']) and
      not(ancestor::TryStmt[descendant::MethodCallExpr[@name='close']])
    ]
message:
  template: "资源 '{resource_type}' 可能未正确关闭，存在泄漏风险"
  suggestion: "使用 try-with-resources 或在 finally 中关闭"
  example_good: |
    try (Connection conn = dataSource.getConnection()) {
        // 使用连接
    }
  example_bad: |
    Connection conn = dataSource.getConnection();  // 错误：未关闭
meta:
  confidence: 0.90
  auto_fixable: true
  tags: [fragment, resource, leak, io]
```

### 1.2 安全基础

#### JAVA-L2-101: 硬编码敏感信息

```yaml
id: java.security.hardcoded-secret
description: 代码中硬编码密码、密钥、Token
category: security
severity: error
layer: 2
condition:
  type: regex
  pattern: '(?i)(password|passwd|pwd|secret|private.?key|api.?key|access.?token|auth.?token)\s*=\s*["\'][^"\']{4,}["\']'
  constraints:
    - not_in_comment: true
message:
  template: "发现硬编码的敏感信息: {matched_keyword}"
  suggestion: "使用配置中心、环境变量或密钥管理系统"
meta:
  confidence: 0.95
  tags: [fragment, security, hardcoded, secret]
```

#### JAVA-L2-102: 不安全的随机数

```yaml
id: java.security.insecure-random
description: 使用 Math.random() 或 Random 生成安全敏感随机数
category: security
severity: warning
layer: 2
condition:
  type: ast
  pattern: |
    //MethodCallExpr[
      @name='random' and parent::Name[@name='Math']
    ]
message:
  template: "使用 Math.random() 生成安全敏感随机数不安全"
  suggestion: "使用 SecureRandom 生成安全敏感随机数"
  example_good: |
    SecureRandom secureRandom = new SecureRandom();
    byte[] bytes = new byte[16];
    secureRandom.nextBytes(bytes);
  example_bad: |
    String token = String.valueOf(Math.random());  // 不安全！
meta:
  confidence: 0.90
  tags: [fragment, security, random]
```

#### JAVA-L2-103: 日志输出敏感信息

```yaml
id: java.security.sensitive-log
description: 日志中输出密码、身份证号、手机号等敏感信息
category: security
severity: error
layer: 2
condition:
  type: regex
  pattern: '(?i)(log\.|logger\.)(debug|info|warn|error).*\b(password|idCard|phone|mobile|email|creditCard|cvv)\b'
message:
  template: "日志可能输出敏感字段"
  suggestion: "对敏感字段脱敏后再输出，或使用专门的脱敏工具"
meta:
  confidence: 0.85
  tags: [fragment, security, log, sensitive]
```

---

## 2. 上下文审核规则 (Layer 3)

### 2.1 逻辑一致性

#### JAVA-L3-001: 异常吞没

```yaml
id: java.context.swallowed-exception
description: catch 块中未处理异常（空 catch 或仅打印）
category: logic
severity: warning
layer: 3
condition:
  type: ast
  pattern: |
    //CatchClause[
      descendant::BlockStmt[
        count(descendant::*) <= 2 or  // 空或极少内容
        descendant::MethodCallExpr[@name='printStackTrace'] or
        descendant::MethodCallExpr[@name='debug' or @name='info'][contains(arg, 'e.getMessage()')]
      ]
    ]
message:
  template: "异常被吞没，可能导致问题难以排查"
  suggestion: "至少记录完整异常堆栈，或重新抛出包装异常"
  example_good: |
    catch (IOException e) {
        log.error("读取文件失败: {}", filePath, e);
        throw new BusinessException("文件读取失败", e);
    }
  example_bad: |
    catch (IOException e) {
        e.printStackTrace();  // 仅打印，无处理
    }
meta:
  confidence: 0.85
  tags: [context, exception, error-handling]
```

#### JAVA-L3-002: 不一致的返回值处理

```yaml
id: java.context.ignored-return
description: 忽略可能返回 null 或错误码的方法返回值
category: logic
severity: warning
layer: 3
condition:
  type: semantic
  pattern:
    ignored_calls:
      - "\.delete\\("  # 删除操作
      - "\.remove\\("  # 移除操作
      - "\.update\\("  # 更新操作
      - "Optional\.of\\("  # Optional 包装
message:
  template: "忽略了方法 '{method_name}' 的返回值"
  suggestion: "检查返回值确认操作是否成功"
meta:
  confidence: 0.70
  tags: [context, return-value, ignored]
```

#### JAVA-L3-003: 资源状态不一致

```yaml
id: java.context.resource-state
description: 已关闭资源被继续使用
category: logic
severity: error
layer: 3
condition:
  type: semantic
  pattern:
    check_sequence:
      - pattern: "\\.close\\("
      - pattern: "\\.read\\(|\\.write\\(|\\.execute\\("
        after: true
message:
  template: "资源在关闭后仍被使用"
  suggestion: "检查资源生命周期，确保使用后才关闭"
meta:
  confidence: 0.80
  tags: [context, resource, state]
```

### 2.2 边界条件

#### JAVA-L3-101: 空集合判断

```yaml
id: java.context.empty-check
description: 对可能为 null 的集合直接调用 isEmpty()
category: npe
severity: warning
layer: 3
condition:
  type: ast
  pattern: |
    //MethodCallExpr[@name='isEmpty'][
      not(preceding::IfStmt[contains(@condition, '!= null')]) and
      not(parent::BinaryExpr[@operator='&&'])
    ]
message:
  template: "对可能为 null 的集合直接调用 isEmpty()"
  suggestion: "先判空或使用 CollectionUtils.isEmpty()"
  example_good: |
    if (CollectionUtils.isEmpty(list)) { ... }
    if (list != null && !list.isEmpty()) { ... }
  example_bad: |
    if (!list.isEmpty()) { ... }  // 可能 NPE
meta:
  confidence: 0.85
  auto_fixable: true
  tags: [context, null, collection]
```

#### JAVA-L3-102: 除零风险

```yaml
id: java.context.divide-by-zero
description: 除法运算缺少除数为零检查
category: logic
severity: error
layer: 3
condition:
  type: ast
  pattern: |
    //BinaryExpr[@operator='/'][
      not(preceding::IfStmt[contains(@condition, '!= 0')])
    ]
message:
  template: "除法运算可能除数为零"
  suggestion: "添加除数为零的检查"
meta:
  confidence: 0.75
  tags: [context, math, divide]
```

#### JAVA-L3-103: 数值溢出问题

```yaml
id: java.context.numeric-overflow
description: 大数相乘可能导致溢出
category: logic
severity: warning
layer: 3
condition:
  type: ast
  pattern: |
    //BinaryExpr[@operator='*'][
      parent::AssignExpr[@operator='+='] or
      descendant::IntegerLiteralExpr[value > 10000]
    ]
message:
  template: "数值运算可能溢出"
  suggestion: "使用 BigDecimal 或 Math.multiplyExact() 检测溢出"
meta:
  confidence: 0.70
  tags: [context, numeric, overflow]
```

---

## 3. 架构审核规则 (Layer 4)

### 3.1 分层违规

#### JAVA-L4-001: Controller 直接调用 Repository

```yaml
id: java.architecture.controller-repository
description: Controller 层禁止直接调用 Repository/Mapper
category: architecture
severity: error
layer: 4
condition:
  type: ast
  pattern: |
    //ClassDeclaration[contains(@name, 'Controller')]
    //FieldDeclaration[
      contains(Type, 'Repository') or
      contains(Type, 'Mapper') or
      contains(Type, 'DAO')
    ]
message:
  template: "Controller '{class_name}' 直接依赖 Repository，违反分层架构"
  suggestion: "通过 Service 层访问数据"
  example_good: |
    @RestController
    public class UserController {
        @Autowired
        private UserService userService;  // 正确：依赖 Service
    }
  example_bad: |
    @RestController
    public class UserController {
        @Autowired
        private UserRepository userRepository;  // 错误：直接依赖 Repository
    }
meta:
  confidence: 0.95
  auto_fixable: true
  tags: [architecture, layer, controller, repository]
```

#### JAVA-L4-002: PO 直接返回前端

```yaml
id: java.architecture.po-expose
description: Controller 返回 PO/Entity 给前端
category: architecture
severity: error
layer: 4
condition:
  type: ast
  pattern: |
    //ClassDeclaration[contains(@name, 'Controller')]
    //MethodDeclaration
    //ReturnStmt[
      descendant::Name[ends-with(@name, 'PO') or ends-with(@name, 'Entity')]
    ]
message:
  template: "Controller 方法返回 PO 对象 '{return_type}'"
  suggestion: "使用 Converter 将 PO 转换为 DTO/VO"
meta:
  confidence: 0.90
  auto_fixable: true
  tags: [architecture, po, dto, vo]
```

#### JAVA-L4-003: 跨模块数据访问

```yaml
id: java.architecture.cross-module-data
description: 模块 A 直接操作模块 B 的数据库表
category: architecture
severity: error
layer: 4
condition:
  type: semantic
  pattern:
    check_imports:
      - "com\\.example\\.(\w+)\\.repository"
      - "@Query.*FROM\s+(\w+)"
    validation: "查询的表不属于当前模块"
message:
  template: "模块 '{current_module}' 直接访问模块 '{target_module}' 的数据"
  suggestion: "通过 API 调用或消息队列进行模块间通信"
meta:
  confidence: 0.85
  tags: [architecture, module, data-access]
```

### 3.2 依赖方向

#### JAVA-L4-101: 循环依赖

```yaml
id: java.architecture.circular-dependency
description: 模块或类之间存在循环依赖
category: architecture
severity: warning
layer: 4
condition:
  type: semantic
  pattern:
    type: dependency-graph
    detect: cycle
message:
  template: "检测到循环依赖: {cycle_path}"
  suggestion: "重构代码，打破循环依赖（引入接口、事件驱动等）"
meta:
  confidence: 0.90
  tags: [architecture, dependency, cycle]
```

#### JAVA-L4-102: Service 层臃肿

```yaml
id: java.architecture.fat-service
description: Service 类行数超过阈值或职责过多
category: architecture
severity: warning
layer: 4
condition:
  type: ast
  pattern: |
    //ClassDeclaration[contains(@name, 'Service')][
      count(descendant::MethodDeclaration) > 15 or
      lineCount > 500
    ]
message:
  template: "Service 类 '{class_name}' 过于臃肿"
  suggestion: "按业务拆分多个 Service 或提取 Manager/Helper"
meta:
  confidence: 0.75
  tags: [architecture, service, complexity]
```

---

## 4. 调用链审核规则 (Layer 5)

### 4.1 接口兼容性

#### JAVA-L5-001: 方法签名变更影响

```yaml
id: java.callchain.signature-change
description: 方法参数或返回值变更可能影响上游调用者
category: compatibility
severity: warning
layer: 5
condition:
  type: semantic
  pattern:
    detect: method-signature-change
    check_callers: true
message:
  template: "方法 '{method_name}' 签名变更可能影响 {caller_count} 处调用"
  suggestion: "检查所有调用点是否需要同步修改"
meta:
  confidence: 0.85
  tags: [callchain, signature, compatibility]
```

#### JAVA-L5-002: 废弃方法仍有调用

```yaml
id: java.callchain.deprecated-usage
description: @Deprecated 方法仍有调用者
category: compatibility
severity: suggestion
layer: 5
condition:
  type: ast
  pattern: |
    //MethodDeclaration[
      descendant::MarkerAnnotationExpr[@name='Deprecated']
    ]
    //MethodCallExpr
message:
  template: "调用了已废弃的方法 '{method_name}'"
  suggestion: "迁移到新方法实现"
meta:
  confidence: 0.95
  tags: [callchain, deprecated]
```

### 4.2 影响范围

#### JAVA-L5-101: 公共方法修改

```yaml
id: java.callchain.public-method-modify
description: 修改了被多处调用的公共方法
category: impact
severity: warning
layer: 5
condition:
  type: semantic
  pattern:
    modifier: "public"
    caller_count_threshold: 5
message:
  template: "公共方法 '{method_name}' 被 {caller_count} 处调用，修改需谨慎"
  suggestion: "评估影响范围，考虑向后兼容"
meta:
  confidence: 0.80
  tags: [callchain, public, impact]
```

#### JAVA-L5-102: 基础工具类变更

```yaml
id: java.callchain.util-modify
description: 修改了通用的工具类或常量
category: impact
severity: warning
layer: 5
condition:
  type: semantic
  pattern:
    class_pattern: "Util|Utils|Helper|Constants|Constant"
message:
  template: "工具类 '{class_name}' 变更影响范围广泛"
  suggestion: "确保变更向后兼容，或全量回归测试"
meta:
  confidence: 0.90
  tags: [callchain, util, impact]
```

---

## 5. 数据流审核规则 (Layer 6)

### 5.1 事务安全

#### JAVA-L6-001: 事务内远程调用

```yaml
id: java.dataflow.remote-in-transaction
description: @Transactional 方法内调用远程服务
category: transaction
severity: error
layer: 6
condition:
  type: semantic
  pattern:
    has_annotation: "@Transactional"
    forbidden_calls:
      - type: http
        patterns: ["RestTemplate", "WebClient", "HttpClient"]
      - type: rpc
        patterns: ["@FeignClient", "@DubboReference"]
      - type: mq
        patterns: ["rabbitTemplate", "kafkaTemplate"]
message:
  template: "@Transactional 方法内调用远程服务 '{service_name}'"
  suggestion: "将远程调用移至事务外，或使用 TransactionTemplate"
  example_good: |
    public void process(OrderDTO dto) {
        // 先调用远程服务（事务外）
        PaymentResult result = paymentClient.charge(dto);

        // 再开启本地事务
        transactionTemplate.execute(status -> {
            orderRepository.save(order);
            return null;
        });
    }
  example_bad: |
    @Transactional
    public void process(OrderDTO dto) {
        orderRepository.save(order);
        paymentClient.charge(dto);  // 错误：事务内远程调用
    }
meta:
  confidence: 0.90
  tags: [dataflow, transaction, remote]
```

#### JAVA-L6-002: 事务内异步操作

```yaml
id: java.dataflow.async-in-transaction
description: @Transactional 方法内提交异步任务
category: transaction
severity: warning
layer: 6
condition:
  type: ast
  pattern: |
    //MethodDeclaration[
      descendant::MarkerAnnotationExpr[@name='Transactional']
    ]
    //MethodCallExpr[
      @name in ['submit', 'execute', 'runAsync', 'supplyAsync']
    ]
message:
  template: "事务内提交异步任务，可能导致事务边界混乱"
  suggestion: "异步任务内的数据库操作需要独立控制事务"
meta:
  confidence: 0.85
  tags: [dataflow, transaction, async]
```

#### JAVA-L6-003: 事务边界过大

```yaml
id: java.dataflow.large-transaction
description: 事务包含过多操作或长时间运行逻辑
category: transaction
severity: warning
layer: 6
condition:
  type: semantic
  pattern:
    has_annotation: "@Transactional"
    metrics:
      - db_operation_count: "> 5"
      - loop_with_db: true
      - sleep_or_wait: true
message:
  template: "事务范围过大，可能导致连接池耗尽"
  suggestion: "缩小事务范围，非 DB 操作移至事务外"
meta:
  confidence: 0.80
  tags: [dataflow, transaction, scope]
```

### 5.2 并发安全

#### JAVA-L6-101: 非线程安全字段

```yaml
id: java.dataflow.non-threadsafe-field
description: Spring Bean 中使用非线程安全的实例变量
category: concurrency
severity: error
layer: 6
condition:
  type: ast
  pattern: |
    //ClassDeclaration[
      descendant::MarkerAnnotationExpr[@name='Service' or @name='Component' or @name='Controller']
    ]
    //FieldDeclaration[
      contains(Type, 'SimpleDateFormat') or
      contains(Type, 'Calendar') or
      contains(Type, 'HashMap') or
      contains(Type, 'ArrayList') or
      contains(Type, 'StringBuilder')
    ][
      not(descendant::Modifier[@keyword='static' and @keyword='final'])
    ]
message:
  template: "Spring Bean 中使用非线程安全的实例变量 '{field_type}'"
  suggestion: "使用 ThreadLocal 或改为方法内局部变量"
  example_good: |
    private static final ThreadLocal<DateFormat> df =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
  example_bad: |
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // 非线程安全！
meta:
  confidence: 0.90
  tags: [dataflow, concurrency, threadsafe]
```

#### JAVA-L6-102: 竞态条件

```yaml
id: java.dataflow.race-condition
description: 先检查后操作模式存在竞态条件
category: concurrency
severity: warning
layer: 6
condition:
  type: semantic
  pattern:
    sequence:
      - "if.*exists|if.*==.*null"
      - "save|update|insert|delete"
message:
  template: "检测到可能的竞态条件"
  suggestion: "使用数据库唯一约束或分布式锁"
meta:
  confidence: 0.75
  tags: [dataflow, concurrency, race-condition]
```

---

## 6. 安全审核规则 (Layer 7)

### 6.1 注入攻击

#### JAVA-L7-001: SQL 注入

```yaml
id: java.security.sql-injection
description: SQL 字符串拼接存在注入风险
category: security
severity: error
layer: 7
condition:
  type: regex
  pattern: '(?i)(SELECT|INSERT|UPDATE|DELETE|REPLACE|CREATE|ALTER|DROP).*\+.*\$?\{'
  constraints:
    - not_in_comment: true
message:
  template: "发现 SQL 注入风险：SQL 字符串拼接"
  suggestion: "使用预编译语句或参数化查询"
  example_good: |
    @Query("SELECT * FROM users WHERE name = ?1")
    User findByName(String name);

    // 或
    String sql = "SELECT * FROM users WHERE name = ?";
    jdbcTemplate.query(sql, new Object[]{name}, mapper);
  example_bad: |
    String sql = "SELECT * FROM users WHERE name = '" + name + "'";
meta:
  confidence: 0.95
  tags: [security, sql-injection, owasp]
```

#### JAVA-L7-002: 命令注入

```yaml
id: java.security.command-injection
description: 用户输入拼接到命令执行
category: security
severity: error
layer: 7
condition:
  type: ast
  pattern: |
    //MethodCallExpr[
      @name in ['exec', 'call', 'start'] and
      contains(arg, '+') and
      contains(arg, 'userInput\|request\|param')
    ]
message:
  template: "命令执行拼接用户输入，存在注入风险"
  suggestion: "使用参数数组形式，避免字符串拼接"
meta:
  confidence: 0.90
  tags: [security, command-injection]
```

#### JAVA-L7-003: XPath/LDAP 注入

```yaml
id: java.security.xpath-ldap-injection
description: XPath 或 LDAP 查询拼接用户输入
category: security
severity: error
layer: 7
condition:
  type: regex
  pattern: '(?i)(xpath|ldap|jndi).*(compile|evaluate|search).*(\+|\$\{)'
message:
  template: "XPath/LDAP 查询拼接用户输入"
  suggestion: "使用参数化查询或严格的白名单校验"
meta:
  confidence: 0.85
  tags: [security, xpath, ldap, injection]
```

### 6.2 不安全反序列化

#### JAVA-L7-101: 不安全的反序列化

```yaml
id: java.security.insecure-deserialization
description: 使用不安全的反序列化方式
category: security
severity: error
layer: 7
condition:
  type: ast
  pattern: |
    //MethodCallExpr[
      @name in ['readObject', 'readUnshared'] and
      parent::Name[contains(@name, 'ObjectInputStream')]
    ]
message:
  template: "使用 ObjectInputStream 进行反序列化存在安全风险"
  suggestion: "使用 JSON 等安全格式，或实现白名单过滤"
  example_good: |
    // 使用 JSON 替代 Java 序列化
    ObjectMapper mapper = new ObjectMapper();
    User user = mapper.readValue(json, User.class);
  example_bad: |
    ObjectInputStream ois = new ObjectInputStream(input);
    Object obj = ois.readObject();  // 危险！
meta:
  confidence: 0.90
  tags: [security, deserialization, rce]
```

### 6.3 越权访问

#### JAVA-L7-201: 缺少权限检查

```yaml
id: java.security.missing-auth
description: 敏感操作缺少权限校验
category: security
severity: error
layer: 7
condition:
  type: semantic
  pattern:
    sensitive_patterns:
      - "delete|remove|drop"
      - "admin|superuser"
      - "export.*all|backup"
    check_annotation:
      missing: ["@PreAuthorize", "@Secured", "@RolesAllowed"]
      missing_method_call: ["checkPermission", "assertAuth"]
message:
  template: "敏感操作 '{operation}' 缺少权限校验"
  suggestion: "添加权限注解或显式权限检查"
meta:
  confidence: 0.75
  tags: [security, auth, authorization]
```

#### JAVA-L7-202: IDOR 风险

```yaml
id: java.security.idor
description: 直接使用用户传入的 ID 访问资源，未校验归属
category: security
severity: warning
layer: 7
condition:
  type: semantic
  pattern:
    parameter_source: "@PathVariable|@RequestParam"
    direct_usage: "findById|getOne|selectById"
    missing_check: "userId.*==.*currentUser|owner.*=="
message:
  template: "直接使用用户传入 ID 查询资源，可能存在越权风险"
  suggestion: "查询后校验资源归属或使用用户维度查询"
meta:
  confidence: 0.70
  tags: [security, idor, authorization]
```

---

## 7. 性能审核规则 (Layer 8)

### 7.1 数据库性能

#### JAVA-L8-001: N+1 查询问题

```yaml
id: java.performance.n-plus-one
description: 循环内进行数据库查询
category: performance
severity: warning
layer: 8
condition:
  type: semantic
  pattern:
    loop_types: ["for", "while", "forEach"]
    db_operations:
      - "findBy"
      - "getOne"
      - "select"
      - "query"
      - "load"
message:
  template: "循环内执行数据库查询，可能导致 N+1 问题"
  suggestion: "使用批量查询或 JOIN 一次获取数据"
  example_good: |
    // 批量查询
    List<Long> ids = orders.stream()
        .map(Order::getUserId)
        .collect(Collectors.toList());
    List<User> users = userRepository.findByIdIn(ids);
  example_bad: |
    for (Order order : orders) {
        User user = userRepository.findById(order.getUserId());  // N+1
    }
meta:
  confidence: 0.90
  auto_fixable: false
  tags: [performance, database, n-plus-one]
```

#### JAVA-L8-002: 大对象创建

```yaml
id: java.performance.large-object
description: 创建过大的集合或字符串，可能导致内存问题
category: performance
severity: warning
layer: 8
condition:
  type: ast
  pattern: |
    //VariableDeclarationExpr[
      contains(Type, 'List|Set|Map|StringBuilder|StringBuffer')
    ][
      descendant::IntegerLiteralExpr[value > 10000] or
      descendant::MethodCallExpr[@name='initialCapacity'][arg > 10000]
    ]
message:
  template: "创建大对象（容量 > 10000），可能导致内存压力"
  suggestion: "使用分页或流式处理"
meta:
  confidence: 0.80
  tags: [performance, memory, large-object]
```

#### JAVA-L8-003: 未使用索引的查询

```yaml
id: java.performance.missing-index
description: 查询条件字段可能缺少索引
category: performance
severity: suggestion
layer: 8
condition:
  type: semantic
  pattern:
    query_method: "findBy|queryBy|searchBy"
    field_patterns:
      - "description"
      - "content"
      - "remark"
      - "memo"
message:
  template: "对长文本字段 '{field_name}' 进行查询，可能未走索引"
  suggestion: "评估是否需要全文检索或添加索引"
meta:
  confidence: 0.70
  tags: [performance, database, index]
```

### 7.2 算法效率

#### JAVA-L8-101: 嵌套循环效率

```yaml
id: java.performance.nested-loop
description: 多层嵌套循环时间复杂度过高
category: performance
severity: warning
layer: 8
condition:
  type: ast
  pattern: |
    //ForStmt[
      count(ancestor::ForStmt) >= 2
    ][
      count(descendant::MethodCallExpr[contains(@name, 'get') or contains(@name, 'find')]) > 0
    ]
message:
  template: "多层嵌套循环，时间复杂度为 O(n^{depth})"
  suggestion: "考虑使用 HashMap 优化或重新设计算法"
meta:
  confidence: 0.80
  tags: [performance, algorithm, nested-loop]
```

#### JAVA-L8-102: 重复计算

```yaml
id: java.performance.redundant-calculation
description: 循环内重复计算不变的值
category: performance
severity: suggestion
layer: 8
condition:
  type: semantic
  pattern:
    loop_types: ["for", "while"]
    invariant_operations:
      - "size\\(\\)"
      - "length"
      - "getConfig"
      - "getCache"
message:
  template: "循环内重复调用不变的方法 '{method_name}'"
  suggestion: "将不变值提取到循环外"
  example_good: |
    int size = list.size();
    for (int i = 0; i < size; i++) { ... }
  example_bad: |
    for (int i = 0; i < list.size(); i++) { ... }  // 每次循环都调用 size()
meta:
  confidence: 0.85
  tags: [performance, loop, redundant]
```

### 7.3 锁与同步

#### JAVA-L8-201: 大范围同步锁

```yaml
id: java.performance.wide-synchronization
description: synchronized 块范围过大，影响并发性能
category: performance
severity: warning
layer: 8
condition:
  type: ast
  pattern: |
    //SynchronizedStmt[
      lineCount > 20 or
      contains(descendant::MethodCallExpr[contains(@name, 'sleep|wait|remote|db')])
    ]
message:
  template: "同步代码块范围过大或包含耗时操作"
  suggestion: "缩小锁范围，或使用细粒度锁"
meta:
  confidence: 0.80
  tags: [performance, concurrency, lock]
```

#### JAVA-L8-202: 锁对象不当

```yaml
id: java.performance.lock-object
description: 使用可变对象或字符串字面量作为锁
category: performance
severity: warning
layer: 8
condition:
  type: ast
  pattern: |
    //SynchronizedStmt[
      descendant::Name[contains(@name, 'String') or contains(@name, 'Integer') or contains(@name, 'Boolean')]
    ]
message:
  template: "使用基础类型包装类或字符串作为锁对象"
  suggestion: "使用 final Object 或专用锁对象"
meta:
  confidence: 0.90
  tags: [performance, concurrency, lock]
```

---

## 8. 全量扫描规则 (Layer 9)

### 8.1 代码质量

#### JAVA-L9-001: 圈复杂度过高

```yaml
id: java.scan.cyclomatic-complexity
description: 方法圈复杂度超过阈值
category: quality
severity: warning
layer: 9
condition:
  type: ast
  pattern: |
    //MethodDeclaration[
      count(descendant::IfStmt) +
      count(descendant::ForStmt) +
      count(descendant::WhileStmt) +
      count(descendant::SwitchEntry) +
      count(descendant::CatchClause) > 10
    ]
message:
  template: "方法 '{method_name}' 圈复杂度过高 ({complexity})"
  suggestion: "提取方法或简化条件逻辑"
meta:
  confidence: 0.95
  tags: [scan, complexity, maintainability]
```

#### JAVA-L9-002: 方法过长

```yaml
id: java.scan.long-method
description: 方法行数超过阈值
category: quality
severity: suggestion
layer: 9
condition:
  type: ast
  pattern: |
    //MethodDeclaration[
      lineCount > 50
    ]
message:
  template: "方法 '{method_name}' 过长 ({line_count} 行)"
  suggestion: "按职责拆分为多个小方法"
meta:
  confidence: 0.95
  tags: [scan, method-length, maintainability]
```

#### JAVA-L9-003: 类过大

```yaml
id: java.scan.large-class
description: 类行数或方法数超过阈值
category: quality
severity: warning
layer: 9
condition:
  type: ast
  pattern: |
    //ClassDeclaration[
      count(descendant::MethodDeclaration) > 20 or
      lineCount > 500
    ]
message:
  template: "类 '{class_name}' 过大，可能违反单一职责原则"
  suggestion: "按功能拆分为多个类"
meta:
  confidence: 0.85
  tags: [scan, class-size, srp]
```

### 8.2 重复代码

#### JAVA-L9-101: 重复代码块

```yaml
id: java.scan.duplicate-code
description: 检测到重复的代码块
category: quality
severity: suggestion
layer: 9
condition:
  type: semantic
  pattern:
    min_lines: 6
    similarity: "> 80%"
message:
  template: "发现 {duplicate_count} 处相似代码块"
  suggestion: "提取公共方法或工具类"
meta:
  confidence: 0.80
  tags: [scan, duplicate, dry]
```

### 8.3 技术债务

#### JAVA-L9-201: 待修复标记

```yaml
id: java.scan.todo-fixme
description: 代码中存在 TODO/FIXME/HACK 标记
category: debt
severity: suggestion
layer: 9
condition:
  type: regex
  pattern: "(?i)(TODO|FIXME|HACK|XXX|BUG|NOTE):"
  constraints:
    - in_comment: true
message:
  template: "代码中存在 '{marker_type}' 标记"
  suggestion: "定期清理技术债务标记"
meta:
  confidence: 0.95
  tags: [scan, todo, technical-debt]
```

#### JAVA-L9-202: 废弃 API 使用

```yaml
id: java.scan.deprecated-api
description: 使用了 @Deprecated 标注的 API
category: debt
severity: suggestion
layer: 9
condition:
  type: ast
  pattern: |
    //MethodCallExpr[
      ancestor::MethodDeclaration[
        descendant::MarkerAnnotationExpr[@name='Deprecated']
      ]
    ]
message:
  template: "使用了已废弃的 API '{api_name}'"
  suggestion: "迁移到新的 API 实现"
meta:
  confidence: 0.95
  tags: [scan, deprecated, migration]
```

---

## 附录：规则速查表

### 按维度分类

| 维度          | 规则数量 | 核心关注点                        |
| ------------- | -------- | --------------------------------- |
| L2 片段审核   | 7        | 语法错误、NPE、资源泄漏、安全基础 |
| L3 上下文审核 | 6        | 异常处理、边界条件、返回值检查    |
| L4 架构审核   | 5        | 分层合规、依赖方向、模块边界      |
| L5 调用链审核 | 4        | 接口兼容性、影响范围              |
| L6 数据流审核 | 5        | 事务安全、并发安全                |
| L7 安全审核   | 7        | 注入攻击、反序列化、越权访问      |
| L8 性能审核   | 7        | 数据库性能、算法效率、锁优化      |
| L9 全量扫描   | 6        | 圈复杂度、重复代码、技术债务      |

### 按严重程度分类

| 严重度     | 规则数量 | 示例                                                |
| ---------- | -------- | --------------------------------------------------- |
| error      | 12       | SQL注入、Controller直接调Repository、事务内远程调用 |
| warning    | 22       | NPE风险、圈复杂度过高、非线程安全字段               |
| suggestion | 12       | 方法过长、TODO标记、废弃API使用                     |

### 规则 ID 命名规范

```
java.{layer}.{category}.{specific-rule}
```

- **layer**: fragment | context | architecture | callchain | dataflow | security | performance | scan
- **category**: 见各规则定义中的 category 字段
