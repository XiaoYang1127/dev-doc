# 命名规范

## 文件命名

| 类型 | 规范 | 示例 |
| ---- | ---- | ---- |
| 组件文件 | PascalCase | `UserCard.vue`、`LoginForm.vue` |
| 工具文件 | camelCase | `request.ts`、`formatDate.ts` |
| 类型文件 | camelCase | `user.d.ts`、`api.d.ts` |
| 常量文件 | camelCase | `index.ts`、`enums.ts` |

## 组件命名

- **多单词**：必须使用 PascalCase，如 `UserCard`、`OrderList`
- **基础组件**：`BaseButton`、`BaseInput`
- **单例组件**：`TheHeader`、`TheSidebar`（带 The 前缀）
- **紧密耦合**：`TodoList`、`TodoListItem`

## 变量命名

| 类型 | 规范 | 示例 |
| ---- | ---- | ---- |
| 变量/函数 | camelCase | `userName`、`getUserInfo` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL`、`MAX_COUNT` |
| 类型/接口 | PascalCase | `User`、`ApiResponse` |
| 枚举 | PascalCase | `UserStatus`、`OrderType` |

## CSS 命名（BEM）

```scss
// Block
.user-card {
  // Element
  &__header {
    // Modifier
    &--large {
      font-size: 20px;
    }
  }

  &__body {
    padding: 16px;
  }
}
```

## 目录命名

- 全部使用 kebab-case
- 功能模块目录：`features/auth`、`features/home`
- 组件目录：`components/common/buttons`
