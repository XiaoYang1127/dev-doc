---
inclusion: auto
---

<!-- @format -->

# API 开发标准

## 一、RESTful API 设计规范

### 1.1 URL 设计

- **使用名词复数**：`/api/users`、`/api/orders`
- **资源嵌套不超过两层**：`/api/users/{userId}/orders`
- **使用小写字母和短横线**：`/api/user-profiles`
- **版本控制**：`/api/v1/users` 或通过 Header `Accept: application/vnd.api.v1+json`

### 1.2 HTTP 方法

- **GET**：查询资源，不修改数据，幂等操作
- **POST**：创建新资源，非幂等操作
- **PUT**：完整更新资源，幂等操作
- **PATCH**：部分更新资源，幂等操作
- **DELETE**：删除资源，幂等操作

### 1.3 HTTP 状态码

- **2xx 成功**
  - **200 OK**：请求成功（GET、PUT、PATCH）
  - **201 Created**：创建成功（POST）
  - **204 No Content**：删除成功（DELETE）
- **4xx 客户端错误**
  - **400 Bad Request**：请求参数错误
  - **401 Unauthorized**：未认证
  - **403 Forbidden**：无权限
  - **404 Not Found**：资源不存在
  - **409 Conflict**：资源冲突
  - **422 Unprocessable Entity**：语义错误
  - **429 Too Many Requests**：请求过于频繁
- **5xx 服务器错误**
  - **500 Internal Server Error**：服务器错误
  - **502 Bad Gateway**：网关错误
  - **503 Service Unavailable**：服务不可用

## 二、统一响应格式

### 2.1 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三"
  },
  "traceId": "1710489600000_abc123",
  "timestamp": 1710489600000
}
```

### 2.2 错误响应

```json
{
  "code": 400,
  "message": "参数验证失败",
  "errors": [
    {
      "field": "email",
      "message": "邮箱格式不正确"
    }
  ],
  "traceId": "1710489600000_abc123",
  "timestamp": 1710489600000
}
```

### 2.3 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "totalPages": 10
  },
  "traceId": "1710489600000_abc123",
  "timestamp": 1710489600000
}
```

## 三、请求规范

### 3.1 请求头

- **Content-Type**: application/json（默认）
- **Authorization**: Bearer {token}（认证）
- **X-Trace-Id**: 请求追踪 ID（可选，服务端生成）
- **Accept-Language**: zh-CN,en（国际化）
- **X-Request-ID**: 客户端请求 ID（可选）

### 3.2 参数验证

- **所有输入参数必须验证**：不信任任何外部输入
- **使用标准验证库**：
  - Java: JSR-303/JSR-380 (Hibernate Validator)
  - JavaScript/TypeScript: class-validator, joi, yup
  - Python: pydantic, marshmallow
  - Go: validator
- **自定义复杂验证逻辑**：业务规则验证
- **错误信息友好**：提供清晰的错误提示

### 3.3 接口文档

- **使用标准规范**：OpenAPI 3.0 / Swagger
- **文档工具**：
  - Java: Smart-Doc, Springdoc
  - JavaScript: Swagger UI, Redoc
  - Python: FastAPI (自动生成)
- **必须包含**：
  - 接口描述和用途
  - 请求参数说明
  - 响应格式示例
  - 错误码说明
  - 认证要求

## 四、安全规范

### 4.1 认证授权

- **使用标准协议**：OAuth 2.0、JWT
- **Token 管理**：
  - Access Token 短期有效（15分钟 - 2小时）
  - Refresh Token 长期有效（7天 - 30天）
  - 支持 Token 主动失效
- **敏感接口必须鉴权**：默认拒绝，显式授权

### 4.2 数据安全

- **HTTPS 通信**：生产环境强制使用 HTTPS
- **敏感数据加密**：密码、支付信息等
- **防止注入攻击**：SQL 注入、XSS、CSRF
- **参数化查询**：避免拼接 SQL

### 4.3 限流防护

- **接口限流**：防止恶意请求
- **IP 限流**：防止 DDoS 攻击
- **用户限流**：防止滥用
- **返回 429 状态码**：请求过于频繁

## 五、性能优化

### 5.1 缓存策略

- **HTTP 缓存头**：Cache-Control, ETag, Last-Modified
- **CDN 缓存**：静态资源使用 CDN
- **服务端缓存**：Redis、Memcached
- **缓存失效策略**：主动更新、TTL 过期

### 5.2 分页查询

- **必须分页**：列表查询必须支持分页
- **默认分页大小**：10-20 条
- **最大分页大小**：不超过 100 条
- **游标分页**：大数据量使用游标分页

### 5.3 响应优化

- **字段过滤**：支持 fields 参数选择返回字段
- **数据压缩**：启用 Gzip/Brotli 压缩
- **异步处理**：耗时操作异步处理，返回任务 ID

## 六、最佳实践

### 6.1 幂等性

- **GET、PUT、DELETE 必须幂等**
- **POST 非幂等**：可使用幂等键实现幂等
- **幂等键设计**：客户端生成唯一 ID

### 6.2 错误处理

- **统一错误格式**：使用标准错误响应格式
- **错误码设计**：业务错误码与 HTTP 状态码分离
- **错误信息**：开发环境详细，生产环境简洁
- **日志记录**：记录完整的错误堆栈和上下文

### 6.3 版本管理

- **URL 版本**：`/api/v1/users`（推荐）
- **Header 版本**：`Accept: application/vnd.api.v1+json`
- **向后兼容**：新版本保持向后兼容
- **废弃通知**：提前通知 API 废弃计划

### 6.4 监控告警

- **请求追踪**：TraceId 贯穿整个请求链路
- **性能监控**：响应时间、吞吐量、错误率
- **业务监控**：关键业务指标
- **告警机制**：异常情况及时告警
