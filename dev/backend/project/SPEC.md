<!-- @format -->

# 项目技术规范

> 本目录为项目级别规范，复制到新项目时按需修改。

## 技术栈

| 类别     | 技术         | 版本             |
| -------- | ------------ | ---------------- |
| Java     | JDK          | 21               |
| 框架     | Spring Boot  | 3.x              |
| ORM      | MyBatis-Plus | 3.x              |
| 数据库   | MySQL        | 8.0+             |
| 缓存     | Redis        | 7.x              |
| 消息队列 | RabbitMQ     | 3.x              |
| 构建     | Maven        | 3.9+ / Gradle 8+ |

---

## 目录结构（Feature-First + API-First）

```
src/main/java/com/company/project/
├── user/                         # 【User 特性】
│   ├── api/                      # 对外契约（DTO/CO/枚举）
│   │   ├── dto/
│   │   ├── request/
│   │   ├── vo/
│   │   ├── enums/
│   │   └── constants/
│   ├── controller/
│   ├── service/impl/
│   ├── manager/                  # 本特性通用逻辑
│   ├── repository/
│   └── entity/
├── order/                        # 【Order 特性】
│   └── ...
├── product/                      # 【Product 特性】
│   └── ...
├── job/                          # 【全局】定时任务
│   └── config/
├── listener/                     # 【全局】MQ 监听
│   └── config/
└── common/                       # 【全局】公共模块
    ├── config/
    ├── exception/
    ├── util/
    └── constant/
```

### 模块内分层职责

| 层级       | 职责                          | 不管     |
| ---------- | ----------------------------- | -------- |
| api        | 对外契约、DTO、枚举           | 业务逻辑 |
| controller | HTTP 处理、参数校验、结果转换 | 业务编排 |
| service    | 核心业务逻辑、事务控制        | 数据访问 |
| manager    | 跨模块通用逻辑、第三方封装    | -        |
| repository | 数据访问（单表操作）          | 业务逻辑 |

### 跨特性调用规则

- 模块间调用必须通过 `api` 模块，禁止直接引用其他特性的 `service/controller/entity`
- 跨服务调用使用 Feign/Dubbo，引用对方的 `api` 模块
- 禁止跨特性直接操作数据库表

---

## 分页对象

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
```

---

## 常用命令

```bash
# 构建
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 生成文档
mvn smart-doc:html

# Docker 构建
docker build -t app:1.0.0 .
```

---

## 异常码规范

### 异常码结构

```
XYZS
├── X: 错误级别（1-系统级 / 2-业务级）
├── Y: 服务模块（1-用户 / 2-订单 / 3-商品 / 9-通用）
├── Z: 错误序号（001-999）
└── S: 子错误码（可选）
```

### 通用异常码

| 异常码 | 说明         | HTTP 状态码 |
| ------ | ------------ | ----------- |
| 1001   | 参数校验失败 | 400         |
| 1002   | 签名验证失败 | 401         |
| 1003   | 令牌过期     | 401         |
| 1004   | 权限不足     | 403         |
| 2001   | 资源不存在   | 404         |
| 2002   | 资源已存在   | 409         |
| 9001   | 系统内部错误 | 500         |
| 9002   | 服务降级     | 503         |

### 异常类定义

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
  // 系统级
  INVALID_PARAMS("1001", "参数校验失败", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED("1003", "未授权", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("1004", "权限不足", HttpStatus.FORBIDDEN),

  // 业务级
  RESOURCE_NOT_FOUND("2001", "资源不存在", HttpStatus.NOT_FOUND),
  RESOURCE_CONFLICT("2002", "资源已存在", HttpStatus.CONFLICT),

  // 通用
  INTERNAL_ERROR("9001", "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
  SERVICE_UNAVAILABLE("9002", "服务降级", HttpStatus.SERVICE_UNAVAILABLE);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
```

---

## 日志规范

### 日志级别使用

| 级别  | 使用场景                              |
| ----- | ------------------------------------- |
| ERROR | 异常、错误、需要立即处理的问题        |
| WARN  | 潜在问题、业务降级、参数边界异常      |
| INFO  | 关键业务节点、接口调用、任务开始/结束 |
| DEBUG | 开发调试、详细业务流程                |

### 日志格式

```properties
# logback-spring.xml
pattern=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [traceId:%X{traceId}] %msg%n
```

### 日志内容规范

```java
// ✅ 包含上下文
log.info("用户登录成功, userId={}, ip={}", userId, ip);

// ❌ 缺乏上下文
log.info("用户登录成功");
```

### 敏感信息处理

```java
// ❌ 禁止记录
log.info("password={}", password);

// ✅ 脱敏记录
log.info("手机号登录, phone={}", phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
```

---

## 配置规范

### application.yml 结构

```yaml
spring:
  application:
    name: ${APP_NAME:project-name}
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api

# 数据库
datasource:
  url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=Asia/Shanghai
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}

# Redis
redis:
  host: ${REDIS_HOST}
  port: ${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:}
  database: ${REDIS_DB:0}

# 日志
logging:
  level:
    root: INFO
    com.company.project: DEBUG
```

### 环境变量命名约定

| 变量         | 说明       | 示例                 |
| ------------ | ---------- | -------------------- |
| `DB_*`       | 数据库配置 | `DB_HOST`, `DB_PORT` |
| `REDIS_*`    | Redis 配置 | `REDIS_HOST`         |
| `RABBITMQ_*` | MQ 配置    | `RABBITMQ_HOST`      |
| `APP_*`      | 应用配置   | `APP_NAME`           |

---

## 统一响应封装

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
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
}
```
