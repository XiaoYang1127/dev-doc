---
inclusion: auto
---

# 后端项目开发规范

> **通用规范参考**：本规范是 Java/Spring Boot 项目特定规范，通用开发规范请参考 `~/.kiro/steering/` 目录：
> - 开发铁律与基本原则：`development-principles.md`
> - 代码风格规范：`code-style.md`
> - API 开发标准：`api-standards.md`
> - 测试规范：`testing-standards.md`
> - Git 工作流规范：`git-workflow.md`

## 一、技术栈

### 1.1 核心技术

- **Java**: JDK 21
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Plus 3.x
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.x
- **消息队列**: RabbitMQ 3.x

### 1.2 辅助工具

- **构建工具**: Maven 3.9+ / Gradle 8+
- **API 文档**: Smart-Doc
- **日志**: SLF4J + Logback
- **工具类**: Hutool、Apache Commons
- **JSON**: Jackson
- **验证**: Hibernate Validator

## 二、项目架构

### 2.1 分层设计（契约优先）

```
src/main/java/com/company/project/
├── api/                # 契约层 - API 接口定义
│   ├── dto/           # 数据传输对象
│   │   ├── request/   # 请求 DTO
│   │   └── response/  # 响应 DTO
│   └── facade/        # 对外接口定义
├── controller/         # 控制层 - 实现 API 契约
├── service/           # 服务层 - 业务逻辑
│   └── impl/         # 服务实现
├── domain/            # 领域层 - 领域模型和业务规则
│   ├── model/        # 领域模型
│   └── repository/   # 仓储接口
├── infrastructure/    # 基础设施层
│   ├── mapper/       # MyBatis Mapper
│   ├── entity/       # 数据库实体
│   └── repository/   # 仓储实现
├── converter/         # 对象转换器
├── config/            # 配置类
├── common/            # 公共模块
│   ├── constant/     # 常量定义
│   ├── enums/        # 枚举类
│   ├── exception/    # 自定义异常
│   └── util/         # 工具类
├── aspect/            # 切面
├── filter/            # 过滤器
├── interceptor/       # 拦截器
└── schedule/          # 定时任务

src/main/resources/
├── mapper/             # MyBatis XML 文件
├── application.yml     # 主配置文件
├── application-dev.yml # 开发环境配置
├── application-prod.yml# 生产环境配置
└── logback-spring.xml  # 日志配置
```

### 2.2 Feature-First 模块设计

对于大型项目，按业务功能模块组织（契约优先）：

```
src/main/java/com/company/project/
├── user/               # 用户模块
│   ├── api/           # 契约层
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── facade/
│   ├── controller/    # 控制层
│   ├── service/       # 服务层
│   │   └── impl/
│   ├── domain/        # 领域层
│   │   ├── model/
│   │   └── repository/
│   ├── infrastructure/# 基础设施层
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── repository/
│   └── converter/     # 转换器
├── order/              # 订单模块
│   ├── api/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   ├── infrastructure/
│   └── converter/
└── payment/            # 支付模块
    ├── api/
    ├── controller/
    ├── service/
    ├── domain/
    ├── infrastructure/
    └── converter/
```

**分层职责说明**：

- **API 契约层**：定义对外接口契约，包括 DTO 和 Facade 接口，可独立打包供其他服务依赖
- **Controller 层**：实现 API 契约，处理 HTTP 请求，参数校验
- **Service 层**：业务逻辑编排，事务控制
- **Domain 层**：领域模型和核心业务规则，不依赖外部框架
- **Infrastructure 层**：技术实现细节，数据持久化、外部服务调用等

## 三、开发规范

### 3.1 命名规范

#### 包命名

- 全小写，使用点分隔：`com.company.project.user.service`

#### 类命名

- **Facade**: `UserFacade`、`OrderFacade`（API 契约接口）
- **Controller**: `UserController`、`OrderController`
- **Service**: `UserService`、`OrderService`
- **ServiceImpl**: `UserServiceImpl`、`OrderServiceImpl`
- **Domain Model**: `User`、`Order`（领域模型）
- **Entity**: `UserEntity`、`OrderEntity`（数据库实体）
- **Mapper**: `UserMapper`、`OrderMapper`
- **Repository**: `UserRepository`、`OrderRepository`
- **DTO**: `UserCreateRequest`、`UserUpdateRequest`、`UserResponse`
- **Converter**: `UserConverter`、`OrderConverter`

#### 方法命名

- **查询单个**: `getById`、`getByUsername`
- **查询列表**: `list`、`listByStatus`
- **分页查询**: `page`、`pageByCondition`
- **新增**: `create`、`save`
- **更新**: `update`、`updateById`
- **删除**: `delete`、`deleteById`
- **判断**: `exists`、`isValid`

#### 数据库表命名

- 小写下划线分隔：`t_user`、`t_order`
- 中间表：`t_user_role`
- 字段：`user_id`、`created_at`、`updated_at`、`deleted_at`
- 时间字段统一使用 `BIGINT` 类型存储毫秒时间戳

### 3.2 API 契约层规范

```java
/**
 * 用户服务 API 契约
 *
 * @author 作者
 * @since 2024-01-01
 */
public interface UserFacade {

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<UserResponse> getById(Long id);

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户ID
     */
    Result<Long> create(UserCreateRequest request);

    /**
     * 分页查询用户
     *
     * @param request 查询条件
     * @return 用户列表
     */
    Result<PageResult<UserResponse>> page(UserPageRequest request);
}
```

### 3.3 Controller 层规范

```java
/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserFacade {

    private final UserService userService;

    @Override
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Override
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @Override
    @GetMapping("/page")
    public Result<PageResult<UserResponse>> page(@Valid UserPageRequest request) {
        return Result.success(userService.page(request));
    }
}
```

### 3.4 Service 层规范

```java
public interface UserService {
    /**
     * 根据ID获取用户
     */
    UserResponse getById(Long id);

    /**
     * 创建用户
     */
    Long create(UserCreateRequest request);

    /**
     * 分页查询用户
     */
    PageResult<UserResponse> page(UserPageRequest request);
}

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCreateRequest request) {
        // 1. 参数校验
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 转换为领域模型
        User user = userConverter.toDomain(request);

        // 3. 业务处理
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        // 4. 保存数据
        userRepository.save(user);

        // 5. 返回结果
        log.info("用户创建成功，userId: {}", user.getId());
        return user.getId();
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return userConverter.toResponse(user);
    }

    @Override
    public PageResult<UserResponse> page(UserPageRequest request) {
        PageResult<User> pageResult = userRepository.page(
            UserPageQuery.builder()
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .keyword(request.getKeyword())
                .build()
        );

        List<UserResponse> responses = pageResult.getList().stream()
            .map(userConverter::toResponse)
            .collect(Collectors.toList());

        return PageResult.of(responses, pageResult.getTotal(),
            pageResult.getPage(), pageResult.getPageSize());
    }
}
```

### 3.5 Repository 层规范

```java
/**
 * 用户仓储接口
 */
public interface UserRepository {

    /**
     * 根据ID查询用户
     */
    User findById(Long id);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 保存用户
     */
    void save(User user);

    /**
     * 更新用户
     */
    void update(User user);

    /**
     * 分页查询
     */
    PageResult<User> page(UserPageQuery query);
}

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    public User findById(Long id) {
        UserEntity entity = userMapper.selectById(id);
        return userConverter.toDomain(entity);
    }

    @Override
    public void save(User user) {
        UserEntity entity = userConverter.toEntity(user);
        userMapper.insert(entity);
        user.setId(entity.getId());
    }
}
```

### 3.6 Mapper 层规范

```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户
     */
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户
     */
    IPage<UserEntity> selectPageByCondition(
        Page<UserEntity> page,
        @Param("condition") UserPageRequest condition
    );
}
```

### 3.7 Entity 规范

```java
/**
 * 用户数据库实体
 */
@Data
@TableName("t_user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 创建时间（毫秒时间戳）
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    /**
     * 删除时间（毫秒时间戳，NULL 表示未删除）
     */
    @TableLogic
    private Long deletedAt;
}
```

### 3.8 Converter 转换器规范

```java
/**
 * 用户对象转换器
 */
@Component
public class UserConverter {

    /**
     * Request -> Domain
     */
    public User toDomain(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return user;
    }

    /**
     * Domain -> Entity
     */
    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        return entity;
    }

    /**
     * Entity -> Domain
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setStatus(entity.getStatus() != null ?
            UserStatus.valueOf(entity.getStatus()) : null);
        return user;
    }

    /**
     * Domain -> Response
     */
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        return response;
    }
}
```

### 3.9 Domain Model 规范

```java
/**
 * 用户领域模型
 */
@Data
public class User {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private UserStatus status;

    /**
     * 激活用户
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new BusinessException("用户已激活");
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 禁用用户
     */
    public void disable() {
        if (this.status == UserStatus.DISABLED) {
            throw new BusinessException("用户已禁用");
        }
        this.status = UserStatus.DISABLED;
    }

    /**
     * 验证密码强度
     */
    public boolean isPasswordStrong(String password) {
        // 密码强度验证逻辑
        return password.length() >= 8
            && password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*\\d.*");
    }
}
```

### 3.10 分页对象定义

```java
/**
 * 分页结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }

    public static <T> PageResult<T> empty(Integer page, Integer pageSize) {
        return new PageResult<>(Collections.emptyList(), 0L, page, pageSize);
    }
}

/**
 * 分页查询对象
 */
@Data
@Builder
public class UserPageQuery {
    private Integer page;
    private Integer pageSize;
    private String keyword;
    private UserStatus status;
}
```

### 3.11 统一响应封装

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }
}

/**
 * 请求追踪上下文
 */
public class TraceContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String getTraceId() {
        String traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = generateTraceId();
            TRACE_ID.set(traceId);
        }
        return traceId;
    }

    public static void clear() {
        TRACE_ID.remove();
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

/**
 * TraceId 拦截器
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (StringUtils.isBlank(traceId)) {
            traceId = TraceContext.generateTraceId();
        }
        TraceContext.setTraceId(traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        // 设置到 MDC，用于日志输出
        MDC.put("traceId", traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceContext.clear();
        MDC.clear();
    }
}
```

### 3.12 全局异常处理

```java
/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请稍后重试");
    }
}
```

### 3.13 MyBatis-Plus 配置

```java
/**
 * MyBatis-Plus 配置
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 元数据处理器 - 自动填充时间戳
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                long now = System.currentTimeMillis();
                this.strictInsertFill(metaObject, "createdAt", Long.class, now);
                this.strictInsertFill(metaObject, "updatedAt", Long.class, now);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", Long.class,
                    System.currentTimeMillis());
            }
        };
    }
}
```

## 四、数据库设计规范

### 4.1 表设计原则

- 每张表必须有主键
- 必须有 `created_at`、`updated_at` 字段（BIGINT 类型，存储毫秒时间戳）
- 使用逻辑删除，添加 `deleted_at` 字段（BIGINT 类型，NULL 表示未删除，非 NULL 存储删除时间戳）
- 外键使用 `_id` 后缀：`user_id`、`order_id`
- 金额字段使用 `DECIMAL(19,2)`，单位为分
- 所有时间字段统一使用 `BIGINT` 类型存储毫秒时间戳
- 字符串字段必须指定长度，避免使用 TEXT 类型（除非确实需要大文本）
- 表必须有注释，字段必须有注释
- 避免使用外键约束，通过应用层保证数据一致性

**表结构示例**：

```sql
CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，DISABLED-禁用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `deleted_at` BIGINT DEFAULT NULL COMMENT '删除时间（毫秒时间戳，NULL表示未删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted_at`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 4.2 索引规范

- 主键自动创建索引
- 外键字段创建索引
- 频繁查询的字段创建索引
- 联合索引遵循最左前缀原则
- 索引命名：`idx_字段名` 或 `idx_字段1_字段2`
- 唯一索引命名：`uk_字段名`
- 单表索引数量不超过 5 个
- 单个索引字段数不超过 5 个
- 逻辑删除字段需要加入唯一索引（如：`uk_username_deleted_at`）

### 4.3 SQL 编写规范

- 禁止使用 `SELECT *`，明确指定需要的字段
- 使用参数化查询，防止 SQL 注入
- 复杂查询使用 XML 方式编写
- 批量操作使用批处理（MyBatis-Plus 的 saveBatch）
- 避免在循环中执行 SQL
- 分页查询必须指定排序字段
- 避免使用子查询，优先使用 JOIN
- 大数据量查询考虑使用流式查询
- 禁止在 WHERE 条件中对字段进行函数操作（会导致索引失效）

**示例**：

```java
// Good - 使用索引
@Select("SELECT id, username, email FROM t_user WHERE username = #{username} AND deleted_at IS NULL")
UserEntity selectByUsername(@Param("username") String username);

// Bad - 索引失效
@Select("SELECT * FROM t_user WHERE DATE(created_at) = #{date}")
List<UserEntity> selectByDate(@Param("date") String date);
```

## 五、缓存使用规范

### 5.1 缓存策略

- **热点数据**：用户信息、配置信息
- **缓存时间**：根据数据更新频率设置
- **缓存更新**：数据变更时主动更新缓存
- **缓存穿透**：使用布隆过滤器或缓存空值
- **缓存雪崩**：设置随机过期时间
- **缓存击穿**：使用分布式锁

### 5.2 Key 命名规范

```
业务模块:功能:标识
user:info:123
order:detail:456
product:list:category:1
```

## 六、日志规范

### 6.1 日志级别

- **ERROR**：系统错误、异常
- **WARN**：警告信息、潜在问题
- **INFO**：关键业务流程、状态变更
- **DEBUG**：调试信息（生产环境关闭）

### 6.2 日志内容

```java
// 方法入口
log.info("创建用户开始，username: {}", username);

// 关键步骤
log.info("用户创建成功，userId: {}", userId);

// 异常信息
log.error("创建用户失败，username: {}, error: {}", username, e.getMessage(), e);
```

### 6.3 Logback 配置（包含 traceId）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 日志格式 -->
    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n"/>

    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/app.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 错误日志单独输出 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/error.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 根日志级别 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>

    <!-- 框架日志级别 -->
    <logger name="com.company.project" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>
    <logger name="com.baomidou.mybatisplus" level="INFO"/>
</configuration>
```

## 七、安全规范

### 7.1 认证授权

- 使用 JWT 进行身份认证
- Token 存储在 Redis，支持主动失效
- 敏感接口必须鉴权
- 使用 Spring Security 或 Sa-Token

### 7.2 数据安全

- 密码使用 BCrypt 加密
- 敏感信息加密存储
- API 接口使用 HTTPS
- 防止 SQL 注入、XSS 攻击

### 7.3 参数校验

```java
@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度3-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
             message = "密码至少8位，包含大小写字母和数字")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

## 八、性能优化

### 8.1 数据库优化

- 合理使用索引
- 避免 N+1 查询
- 使用批量操作
- 分页查询大数据量

### 8.2 缓存优化

- 缓存热点数据
- 使用本地缓存 + Redis 二级缓存
- 缓存预热

### 8.3 异步处理

- 使用 `@Async` 异步执行
- 使用消息队列解耦
- 定时任务处理非实时业务

## 九、API 文档规范（Smart-Doc）

### 9.1 Smart-Doc 配置

Smart-Doc 是一款基于 Java 注释生成 API 文档的工具，无需使用注解侵入代码。

#### Maven 配置

```xml
<plugin>
    <groupId>com.ly.smart-doc</groupId>
    <artifactId>smart-doc-maven-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <configFile>./src/main/resources/smart-doc.json</configFile>
    </configuration>
</plugin>
```

#### smart-doc.json 配置

```json
{
  "serverUrl": "http://localhost:8080",
  "outPath": "src/main/resources/static/doc",
  "isStrict": false,
  "allInOne": true,
  "coverOld": true,
  "createDebugPage": true,
  "packageFilters": "com.company.project.*.controller",
  "projectName": "项目名称",
  "style": "xt256"
}
```

### 9.2 文档注释规范

```java
/**
 * 用户管理接口
 *
 * @author 张三
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserFacade {

    private final UserService userService;

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }
}

/**
 * 用户创建请求
 *
 * @author 张三
 */
@Data
public class UserCreateRequest {

    /**
     * 用户名（3-20位字符）
     */
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度3-20位")
    private String username;

    /**
     * 密码（至少8位，包含大小写字母和数字）
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（可选）
     */
    private String phone;
}
```

### 9.3 生成文档命令

```bash
# Maven 生成文档
mvn smart-doc:html
mvn smart-doc:markdown
mvn smart-doc:openapi

# Gradle 生成文档
gradle smartdoc
```

## 十、测试规范

> 测试原则和最佳实践请参考 `~/.kiro/steering/testing-standards.md`

### 10.1 单元测试（JUnit 5 + Mockito）

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("创建用户 - 成功")
    void testCreate_Success() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("test");
        request.setPassword("Test123456");
        request.setEmail("test@example.com");

        // Act
        Long userId = userService.create(request);

        // Assert
        assertNotNull(userId);
        assertTrue(userId > 0);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void testCreate_UsernameExists() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");

        when(userRepository.findByUsername("existing"))
            .thenReturn(new User());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            userService.create(request);
        });
    }
}
```

### 10.2 集成测试（TestContainers）

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        // 保存用户
        User user = new User();
        user.setUsername("test");
        userRepository.save(user);

        // 查询用户
        User found = userRepository.findByUsername("test");
        assertNotNull(found);
        assertEquals("test", found.getUsername());
    }

    @AfterEach
    void cleanup() {
        // 清理测试数据
        userRepository.deleteAll();
    }
}
```

## 十一、部署规范

### 11.1 打包部署

```bash
# Maven 打包
mvn clean package -DskipTests

# Gradle 打包
gradle clean build -x test

# Docker 部署
docker build -t app:1.0.0 .
docker run -d -p 8080:8080 app:1.0.0

# Docker Compose 部署
docker-compose up -d
```

### 11.2 环境配置

- 使用 Spring Profile 区分环境（dev、test、prod）
- 敏感配置使用环境变量或加密配置
- 配置文件分离：application.yml、application-{profile}.yml
