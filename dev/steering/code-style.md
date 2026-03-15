---
inclusion: auto
---

# 代码风格规范

## 一、通用代码风格

### 1.1 缩进与空格

- **Java/C#/C++/Go**: 使用 4 个空格缩进
- **JavaScript/TypeScript/Vue/React**: 使用 2 个空格缩进
- **Python**: 使用 4 个空格缩进（PEP 8）
- **Dart/Flutter**: 使用 2 个空格缩进
- **不使用 Tab 字符**：统一使用空格
- **行尾不留空格**：避免无意义的空格

### 1.2 行长度

- **推荐长度**：单行代码不超过 100-120 字符
- **Python**: 79 字符（PEP 8）
- **超长代码**：合理换行，保持可读性

### 1.3 空行使用

- **方法之间**：空一行
- **逻辑块之间**：空一行分隔不同逻辑
- **类成员分组**：不同类型成员之间空一行
- **文件末尾**：保留一个空行

### 1.4 括号风格

- **K&R 风格**（推荐）：左括号不换行
- **Allman 风格**：左括号换行（C# 常用）
- **单行语句也使用括号**：提高可读性

```javascript
// Good - K&R 风格
if (condition) {
  doSomething();
}

// Bad - 省略括号
if (condition) doSomething();

// Allman 风格（C# 常用）
if (condition) {
  doSomething();
}
```

## 二、导入/引用规范

### 2.1 导入顺序

1. **标准库导入**：语言内置库
2. **第三方库导入**：外部依赖
3. **项目内部导入**：本项目模块
4. **各组之间空一行**

```python
# Python 示例
import os
import sys

import requests
import pandas as pd

from myapp.models import User
from myapp.utils import helper
```

```typescript
// TypeScript 示例
import { ref, computed } from "vue";
import type { User } from "vue";

import axios from "axios";
import dayjs from "dayjs";

import { useUserStore } from "@/stores/user";
import type { ApiResponse } from "@/types/api";
```

### 2.2 导入规则

- **不使用通配符导入**：避免 `import *`
- **删除未使用的导入**：保持代码整洁
- **使用 IDE 自动整理**：配置自动格式化
- **相对路径 vs 绝对路径**：
  - 项目内部使用绝对路径（配置别名）
  - 同目录文件可使用相对路径

## 三、注释规范

### 3.1 文档注释

```java
/**
 * 计算订单总价
 *
 * @param orderId 订单ID
 * @param couponCode 优惠券码（可选）
 * @return 订单总价
 * @throws OrderNotFoundException 订单不存在时抛出
 */
public BigDecimal calculateOrderTotal(Long orderId, String couponCode) {
    // implementation
}
```

```typescript
/**
 * 获取用户信息
 * @param userId - 用户ID
 * @returns 用户信息对象
 * @throws {ApiException} 用户不存在时抛出
 */
async function getUserInfo(userId: number): Promise<User> {
  // implementation
}
```

```python
def calculate_total(order_id: int, coupon_code: str = None) -> Decimal:
    """
    计算订单总价

    Args:
        order_id: 订单ID
        coupon_code: 优惠券码（可选）

    Returns:
        订单总价

    Raises:
        OrderNotFoundException: 订单不存在时抛出
    """
    pass
```

### 3.2 行内注释

- **解释"为什么"而不是"是什么"**：代码本身应该是自解释的
- **复杂算法必须注释**：说明算法思路
- **临时方案必须标记**：TODO、FIXME、HACK

```java
// TODO: 优化查询性能，考虑添加缓存
// FIXME: 并发情况下可能出现数据不一致
// HACK: 临时方案，等待第三方 API 修复后移除
// NOTE: 这里使用了特殊算法，参考 https://xxx
```

### 3.3 注释最佳实践

- **避免废弃注释**：删除代码时同步删除注释
- **避免注释掉的代码**：使用版本控制，不要注释代码
- **保持注释更新**：代码变更时同步更新注释
- **使用英文注释**：国际化团队使用英文

## 四、命名规范

### 4.1 通用命名规则

- **有意义的命名**：避免 a、b、temp 等无意义命名
- **避免缩写**：除非是广为人知的缩写（HTTP、URL、ID）
- **布尔变量**：使用 is、has、can 等前缀
- **集合变量**：使用复数形式

```typescript
// Good
const isActive = true;
const hasPermission = false;
const canEdit = true;
const userList = [];
const userMap = new Map();

// Bad
const active = true;
const permission = false;
const edit = true;
const users = []; // 不明确是数组还是对象
```

### 4.2 语言特定命名

| 语言                  | 变量/函数  | 类/接口    | 常量           | 文件名          |
| --------------------- | ---------- | ---------- | -------------- | --------------- |
| Java                  | camelCase  | PascalCase | UPPER_SNAKE    | PascalCase.java |
| JavaScript/TypeScript | camelCase  | PascalCase | UPPER_SNAKE    | camelCase.ts    |
| Python                | snake_case | PascalCase | UPPER_SNAKE    | snake_case.py   |
| Go                    | camelCase  | PascalCase | PascalCase     | snake_case.go   |
| Dart                  | camelCase  | PascalCase | lowerCamelCase | snake_case.dart |
| C#                    | camelCase  | PascalCase | PascalCase     | PascalCase.cs   |

## 五、反模式（要避免的）

### 5.1 魔法数字/字符串

```java
// Bad
if (user.getAge() > 18) { }
if (status == "active") { }

// Good
private static final int ADULT_AGE = 18;
private static final String STATUS_ACTIVE = "active";

if (user.getAge() > ADULT_AGE) { }
if (status.equals(STATUS_ACTIVE)) { }
```

### 5.2 过长的方法/函数

- **单个方法不超过 50 行**（特殊情况除外）
- **复杂逻辑拆分**：提取为多个小方法
- **单一职责**：一个方法只做一件事

### 5.3 深层嵌套

- **嵌套层级不超过 3 层**
- **使用卫语句提前返回**：减少嵌套
- **提取方法**：复杂条件提取为方法

```java
// Bad - 深层嵌套
if (user != null) {
    if (user.isActive()) {
        if (user.hasPermission()) {
            // do something
        }
    }
}

// Good - 卫语句
if (user == null) return;
if (!user.isActive()) return;
if (!user.hasPermission()) return;
// do something
```

### 5.4 上帝类/上帝方法

- **单个类不超过 500 行**（特殊情况除外）
- **单个方法不超过 50 行**
- **职责单一**：合理拆分类和方法
- **高内聚低耦合**：相关功能放在一起

### 5.5 重复代码

- **DRY 原则**：Don't Repeat Yourself
- **提取公共方法**：重复代码提取为方法
- **使用工具类**：通用功能放入工具类
- **三次原则**：代码重复三次就应该重构

## 六、代码格式化工具

### 6.1 推荐工具

- **Java**: Google Java Format, Checkstyle
- **JavaScript/TypeScript**: Prettier, ESLint
- **Python**: Black, autopep8, isort
- **Go**: gofmt, goimports
- **Dart**: dartfmt
- **C#**: dotnet format

### 6.2 配置示例

#### Prettier 配置

```json
{
  "semi": false,
  "singleQuote": true,
  "printWidth": 100,
  "trailingComma": "es5",
  "arrowParens": "always",
  "endOfLine": "lf"
}
```

#### Black 配置

```toml
[tool.black]
line-length = 100
target-version = ['py38', 'py39', 'py310']
```

### 6.3 IDE 配置

- **自动格式化**：保存时自动格式化
- **导入整理**：保存时自动整理导入
- **代码检查**：实时显示代码问题
- **团队统一**：使用 .editorconfig 统一配置

```ini
# .editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{js,ts,vue}]
indent_style = space
indent_size = 2

[*.{java,py}]
indent_style = space
indent_size = 4
```
