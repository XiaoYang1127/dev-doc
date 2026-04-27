# 命名规范

## 文件命名

| 类型 | 规范 | 示例 |
| ---- | ---- | ---- |
| 组件文件 | PascalCase | `UserCard.vue`、`LoginForm.vue` |
| 工具文件 | camelCase | `request.ts`、`formatDate.ts` |
| 类型文件 | camelCase | `user.d.ts`、`api.d.ts` |
| 常量文件 | camelCase | `index.ts`、`enums.ts` |

## 组件命名（Vue 官方 Style Guide）

### 多单词规则（强制）

组件名必须使用多单词（除根组件 `App.vue` 和 `WelcomeItem.vue` 等明确场景）。

```vue
<!-- ❌ 错误 - 单单词与 HTML 元素冲突 -->
<Item />
<Button />

<!-- ✅ 正确 - 多单词 -->
<TodoItem />
<UserCard />
```

### 基础组件统一前缀（强烈推荐）

```text
components/
|- BaseButton.vue    # 基础按钮
|- BaseIcon.vue      # 基础图标
|- AppHeader.vue     # 应用级头部
|- AppSidebar.vue    # 应用级侧边栏
|- VTable.vue        # V 前缀（另一种风格）
```

### SFC 文件名规范（强烈推荐）

| 类型 | 正确 | 错误 | 原因 |
|------|------|------|------|
| PascalCase | `UserCard.vue` | `userCard.vue` | 保持一致 |
| kebab-case | `user-card.vue` | `userProfile.vue` | 符合 HTML 规范 |
| 禁止 | - | `userprofile.vue` | 可读性差 |

### 业务组件命名

- **紧密耦合**：`ArticleList` + `ArticleListItem`
- **页面组件**：`HomePage.vue`、`LoginPage.vue`

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

## 路径别名配置

推荐配置 `@/*` 指向 `src/*`，简化导入路径：

```json
// tsconfig.json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

```typescript
// vite.config.ts
import { resolve } from 'path';

export default defineConfig({
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
});
```

**导入示例：**

```typescript
// ✅ 推荐
import { useUserStore } from '@/stores/user';
import UserCard from '@/components/UserCard.vue';

// ❌ 不推荐
import { useUserStore } from '../../stores/user';
```
