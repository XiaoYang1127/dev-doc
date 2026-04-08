---
inclusion: auto
---

# Java 后端开发规范

> 详细规范参考 `references/` 目录，Claude Code 会自动扫描相关文件。

## 一、技术栈

| 类别 | 技术 | 版本 |
| ---- | ---- | ---- |
| Java | JDK | 21 |
| 框架 | Spring Boot | 3.x |
| ORM | MyBatis-Plus | 3.x |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.x |
| 消息队列 | RabbitMQ | 3.x |
| 构建 | Maven | 3.9+ / Gradle 8+ |

## 二、目录结构（Feature-First）

```
src/main/java/com/company/project/
├── api/                # 契约层 - 接口定义、DTO
├── controller/         # 控制层 - HTTP 请求处理
├── service/           # 服务层 - 业务逻辑
│   └── impl/         # 服务实现
├── domain/            # 领域层 - 领域模型
│   ├── model/        # 领域模型
│   └── repository/   # 仓储接口
├── infrastructure/    # 基础设施层
│   ├── mapper/      # MyBatis Mapper
│   ├── entity/      # 数据库实体
│   └── repository/  # 仓储实现
├── converter/         # 对象转换器
├── config/            # 配置类
└── common/            # 公共模块
    ├── constant/    # 常量
    ├── enums/       # 枚举
    └── exception/   # 异常
```

## 三、核心原则

### Feature-First（特性优先）

按业务功能垂直拆分，层内按职责水平划分。修改功能时，文件修改范围应集中在单一 feature 目录内。

### API-First（接口先行）

编码前先定义接口和类型，确保契约稳定。模块间调用必须依赖 `api` 模块，严禁直接引用其他模块的实现类。

### 分层职责

| 层级 | 职责 |
| ---- | ---- |
| API 契约层 | 定义对外接口、DTO、枚举 |
| Controller 层 | 实现 API 契约，参数校验 |
| Service 层 | 业务逻辑编排，事务控制 |
| Domain 层 | 领域模型、核心业务规则 |
| Infrastructure 层 | 数据持久化、外部服务调用 |

## 四、常用命令

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

## 五、references 导航

| 文档 | 内容 |
| ---- | ---- |
| 01-naming | 文件、类、方法、数据库表命名规范 |
| 02-api | RESTful API 设计、响应封装、版本控制 |
| 03-testing | 单元测试、集成测试规范 |
| 04-deployment | Docker 部署、环境配置、回滚策略 |
| 05-security | 认证授权、数据安全、参数校验 |
| 06-database | 表设计、索引规范、SQL 编写规范 |

## 六、日志规范

### 日志级别

- **ERROR**：系统错误、异常
- **WARN**：警告信息、潜在问题
- **INFO**：关键业务流程、状态变更
- **DEBUG**：调试信息（生产环境关闭）

### 日志格式

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n
```

## 七、分页对象

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

## 八、异常处理

```java
// 业务异常
throw new BusinessException("用户名已存在");

// 全局捕获
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
```

## 九、缓存策略

- **Key 命名**：`业务模块:功能:标识`，如 `user:info:123`
- **热点数据**：用户信息、配置信息
- **更新策略**：数据变更时主动更新缓存
