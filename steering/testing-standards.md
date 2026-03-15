---
inclusion: auto
---

# 测试规范

## 一、测试原则

### 1.1 测试金字塔

- **单元测试（70%）**：测试单个方法/函数
- **集成测试（20%）**：测试模块间交互
- **端到端测试（10%）**：测试完整业务流程

### 1.2 测试要求

- **核心业务逻辑覆盖率 ≥ 80%**
- **关键路径必须有测试**
- **边界条件必须测试**
- **异常情况必须测试**

## 二、单元测试

### 2.1 命名规范

- 测试类：`{ClassName}Test`
- 测试方法：`test{MethodName}_{Scenario}_{ExpectedResult}`
- 示例：`testCalculatePrice_WithDiscount_ReturnsDiscountedPrice`

### 2.2 测试结构（AAA 模式）

```java
@Test
void testUserLogin_WithValidCredentials_ReturnsToken() {
    // Arrange - 准备测试数据
    String username = "test@example.com";
    String password = "password123";

    // Act - 执行测试
    String token = authService.login(username, password);

    // Assert - 验证结果
    assertNotNull(token);
    assertTrue(token.startsWith("Bearer "));
}
```

### 2.3 Mock 使用

- 使用 Mockito 进行 Mock
- 只 Mock 外部依赖（数据库、第三方服务）
- 不要 Mock 被测试的类本身

## 三、集成测试

### 3.1 测试范围

- API 接口测试
- 数据库交互测试
- 消息队列测试
- 缓存测试

### 3.2 测试环境

- 使用 TestContainers 或 H2 内存数据库
- 独立的测试数据
- 测试后清理数据

## 四、测试最佳实践

### 4.1 测试独立性

- 每个测试独立运行
- 测试间不能有依赖关系
- 测试顺序不影响结果

### 4.2 测试可读性

- 测试名称清晰表达意图
- 使用有意义的测试数据
- 适当添加注释说明

### 4.3 测试维护

- 代码变更时同步更新测试
- 删除无用的测试
- 重构重复的测试代码

### 4.4 性能考虑

- 单元测试应该快速执行（< 100ms）
- 集成测试可以稍慢（< 5s）
- 使用 @Tag 区分快慢测试
