---
inclusion: auto
---

# Vue3 Web 端开发规范

> **通用规范参考**：本规范是 Vue3/TypeScript 项目特定规范，通用开发规范请参考 `~/.kiro/steering/` 目录：
> - 开发铁律与基本原则：`development-principles.md`
> - 代码风格规范：`code-style.md`
> - API 开发标准：`api-standards.md`
> - 测试规范：`testing-standards.md`
> - Git 工作流规范：`git-workflow.md`

## 一、技术栈

### 1.1 核心技术

- **Vue**: 3.x (Composition API)
- **TypeScript**: 5.x+
- **构建工具**: Vite 5.x
- **路由**: Vue Router 4.x
- **状态管理**: Pinia 2.x
- **UI 框架**: Element Plus（推荐）/ Ant Design Vue / Naive UI

### 1.2 常用库

- **HTTP 客户端**: axios
- **工具库**: lodash-es
- **日期处理**: dayjs
- **表单验证**: async-validator（Element Plus 内置）
- **图标**: @iconify/vue / @element-plus/icons-vue
- **CSS 预处理**: SCSS（推荐）
- **代码规范**: ESLint + Prettier
- **Git Hooks**: husky + lint-staged
- **Mock 数据**: vite-plugin-mock（开发环境）

## 二、项目架构

### 2.1 目录结构（Feature-First）

```
project/
├── public/                     # 静态资源
│   └── favicon.ico
├── src/
│   ├── main.ts                # 应用入口
│   ├── App.vue                # 根组件
│   ├── assets/                # 资源文件
│   │   ├── images/
│   │   ├── styles/
│   │   │   ├── index.scss
│   │   │   ├── variables.scss
│   │   │   └── mixins.scss
│   │   └── fonts/
│   ├── components/            # 通用组件
│   │   ├── common/           # 基础组件
│   │   │   ├── Button/
│   │   │   ├── Input/
│   │   │   └── Modal/
│   │   └── business/         # 业务组件
│   │       ├── UserCard/
│   │       └── OrderTable/
│   ├── composables/           # 组合式函数
│   │   ├── useAuth.ts
│   │   ├── useRequest.ts
│   │   └── useTable.ts
│   ├── directives/            # 自定义指令
│   │   ├── permission.ts
│   │   └── loading.ts
│   ├── layouts/               # 布局组件
│   │   ├── DefaultLayout.vue
│   │   ├── BlankLayout.vue
│   │   └── components/
│   ├── router/                # 路由配置
│   │   ├── index.ts
│   │   ├── routes.ts
│   │   └── guards.ts
│   ├── stores/                # 状态管理
│   │   ├── index.ts
│   │   ├── user.ts
│   │   └── app.ts
│   ├── api/                   # API 接口
│   │   ├── index.ts
│   │   ├── user.ts
│   │   └── order.ts
│   ├── utils/                 # 工具函数
│   │   ├── request.ts
│   │   ├── storage.ts
│   │   ├── validator.ts
│   │   └── format.ts
│   ├── types/                 # 类型定义
│   │   ├── api.d.ts
│   │   ├── user.d.ts
│   │   └── global.d.ts
│   ├── constants/             # 常量定义
│   │   ├── index.ts
│   │   └── enums.ts
│   ├── features/              # 功能模块
│   │   ├── auth/             # 认证模块
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── composables/
│   │   │   ├── stores/
│   │   │   ├── types/
│   │   │   └── views/
│   │   │       ├── Login.vue
│   │   │       └── Register.vue
│   │   ├── home/             # 首页模块
│   │   │   ├── components/
│   │   │   └── views/
│   │   │       └── Home.vue
│   │   └── user/             # 用户模块
│   │       ├── api/
│   │       ├── components/
│   │       ├── stores/
│   │       └── views/
│   │           ├── Profile.vue
│   │           └── Settings.vue
│   └── views/                 # 页面组件（简单项目）
│       ├── Home.vue
│       ├── About.vue
│       └── NotFound.vue
├── .env                       # 环境变量
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .eslintrc.cjs             # ESLint 配置
├── .prettierrc.json          # Prettier 配置
├── tsconfig.json             # TypeScript 配置
├── vite.config.ts            # Vite 配置
└── package.json
```

## 三、开发规范

### 3.1 命名规范

#### 文件命名

- **组件文件**: PascalCase - `UserCard.vue`、`LoginForm.vue`
- **工具文件**: camelCase - `request.ts`、`formatDate.ts`
- **类型文件**: camelCase - `user.d.ts`、`api.d.ts`
- **常量文件**: camelCase - `index.ts`、`enums.ts`

#### 组件命名

- **多单词**: `UserCard`、`OrderList`（避免单个单词）
- **基础组件**: `BaseButton`、`BaseInput`
- **单例组件**: `TheHeader`、`TheSidebar`
- **紧密耦合**: `TodoList`、`TodoListItem`

#### 变量命名

- **变量/函数**: camelCase - `userName`、`getUserInfo`
- **常量**: UPPER_SNAKE_CASE - `API_BASE_URL`、`MAX_COUNT`
- **类型/接口**: PascalCase - `User`、`ApiResponse`
- **枚举**: PascalCase - `UserStatus`、`OrderType`

### 3.2 组件开发规范

#### 组件结构顺序

```vue
<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from "vue";
import type { User } from "@/types/user";

// 2. Props 定义
interface Props {
  user: User;
  size?: "small" | "medium" | "large";
}

const props = withDefaults(defineProps<Props>(), {
  size: "medium",
});

// 3. Emits 定义
interface Emits {
  (e: "update", value: User): void;
  (e: "delete", id: number): void;
}

const emit = defineEmits<Emits>();

// 4. 响应式数据
const isLoading = ref(false);
const userInfo = ref<User | null>(null);

// 5. 计算属性
const displayName = computed(() => {
  return props.user.nickname || props.user.username;
});

// 6. 方法
const handleUpdate = () => {
  emit("update", props.user);
};

// 7. 生命周期
onMounted(() => {
  // 初始化逻辑
});

// 8. 暴露给父组件
defineExpose({
  refresh: () => {
    // 刷新逻辑
  },
});
</script>

<template>
  <div class="user-card" :class="`user-card--${size}`">
    <div class="user-card__avatar">
      <img :src="user.avatar" :alt="displayName" />
    </div>
    <div class="user-card__info">
      <h3>{{ displayName }}</h3>
      <p>{{ user.email }}</p>
    </div>
    <div class="user-card__actions">
      <button @click="handleUpdate">编辑</button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.user-card {
  display: flex;
  padding: 16px;
  border-radius: 8px;
  background: #fff;

  &--small {
    padding: 8px;
  }

  &__avatar {
    img {
      width: 48px;
      height: 48px;
      border-radius: 50%;
    }
  }

  &__info {
    flex: 1;
    margin-left: 12px;
  }
}
</style>
```

### 3.3 Composables（组合式函数）

```typescript
// composables/useRequest.ts
import { ref } from "vue";
import type { Ref } from "vue";

interface UseRequestOptions<T> {
  immediate?: boolean;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
}

export function useRequest<T>(
  requestFn: () => Promise<T>,
  options: UseRequestOptions<T> = {},
) {
  const { immediate = false, onSuccess, onError } = options;

  const data = ref<T | null>(null) as Ref<T | null>;
  const loading = ref(false);
  const error = ref<Error | null>(null);

  const execute = async () => {
    loading.value = true;
    error.value = null;

    try {
      const result = await requestFn();
      data.value = result;
      onSuccess?.(result);
      return result;
    } catch (e) {
      error.value = e as Error;
      onError?.(e as Error);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  if (immediate) {
    execute();
  }

  return {
    data,
    loading,
    error,
    execute,
  };
}

// 使用示例
const {
  data: userList,
  loading,
  execute: fetchUsers,
} = useRequest(() => getUserList({ page: 1, pageSize: 10 }), {
  immediate: true,
  onSuccess: (data) => {
    console.log("获取成功", data);
  },
});
```

### 3.4 Pinia Store

```typescript
// stores/user.ts
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { User } from "@/types/user";
import { getUserInfo, login as loginApi } from "@/api/user";

export const useUserStore = defineStore("user", () => {
  // State
  const token = ref<string>("");
  const userInfo = ref<User | null>(null);

  // Getters
  const isLoggedIn = computed(() => !!token.value);
  const userName = computed(() => userInfo.value?.username || "");

  // Actions
  const login = async (username: string, password: string) => {
    const { token: newToken } = await loginApi({ username, password });
    token.value = newToken;
    localStorage.setItem("token", newToken);
    await fetchUserInfo();
  };

  const fetchUserInfo = async () => {
    const info = await getUserInfo();
    userInfo.value = info;
  };

  const logout = () => {
    token.value = "";
    userInfo.value = null;
    localStorage.removeItem("token");
  };

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    login,
    fetchUserInfo,
    logout,
  };
});
```

### 3.5 API 封装

#### Axios 配置

```typescript
// utils/request.ts
import axios from "axios";
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import router from "@/router";

interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  traceId: string;
  timestamp: number;
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();

    // 添加 Token
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }

    // 添加 TraceId
    config.headers["X-Trace-Id"] = generateTraceId();

    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  },
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data, traceId } = response.data;

    // 成功响应
    if (code === 200) {
      return data;
    }

    // 业务错误
    console.error(`业务错误 [${traceId}]:`, message);
    ElMessage.error(message || "请求失败");
    return Promise.reject(new Error(message));
  },
  (error) => {
    const { response } = error;

    // HTTP 错误处理
    if (response) {
      const { status, data } = response;

      switch (status) {
        case 401:
          // 未授权，清除登录状态并跳转登录页
          const userStore = useUserStore();
          userStore.logout();
          router.push({
            path: "/login",
            query: { redirect: router.currentRoute.value.fullPath },
          });
          ElMessage.error("登录已过期，请重新登录");
          break;
        case 403:
          ElMessage.error("没有权限访问");
          break;
        case 404:
          ElMessage.error("请求的资源不存在");
          break;
        case 500:
          ElMessage.error("服务器错误");
          break;
        default:
          ElMessage.error(data?.message || "请求失败");
      }
    } else if (error.code === "ECONNABORTED") {
      ElMessage.error("请求超时，请稍后重试");
    } else {
      ElMessage.error("网络错误，请检查网络连接");
    }

    return Promise.reject(error);
  },
);

/**
 * 生成追踪ID
 */
function generateTraceId(): string {
  return `${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

export default request;
```

#### API 定义

```typescript
// api/user.ts
import request from "@/utils/request";
import type { User, LoginRequest, LoginResponse } from "@/types/user";

export const login = (data: LoginRequest) => {
  return request.post<any, LoginResponse>("/api/v1/auth/login", data);
};

export const getUserInfo = () => {
  return request.get<any, User>("/api/v1/user/me");
};

export const updateProfile = (data: Partial<User>) => {
  return request.put<any, void>("/api/v1/user/profile", data);
};

export const getUserList = (params: {
  page: number;
  pageSize: number;
  keyword?: string;
}) => {
  return request.get<any, { list: User[]; total: number }>("/api/v1/users", {
    params,
  });
};
```

### 3.6 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import { useUserStore } from "@/stores/user";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/features/auth/views/Login.vue"),
    meta: { requiresAuth: false },
  },
  {
    path: "/",
    component: () => import("@/layouts/DefaultLayout.vue"),
    redirect: "/home",
    children: [
      {
        path: "home",
        name: "Home",
        component: () => import("@/features/home/views/Home.vue"),
        meta: { title: "首页", requiresAuth: true },
      },
      {
        path: "profile",
        name: "Profile",
        component: () => import("@/features/user/views/Profile.vue"),
        meta: { title: "个人中心", requiresAuth: true },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  // 设置页面标题
  document.title = (to.meta.title as string) || "应用名称";

  // 权限验证
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: "Login", query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

export default router;
```

### 3.7 类型定义

```typescript
// types/user.d.ts
export interface User {
  id: number;
  username: string;
  nickname?: string;
  email: string;
  avatar?: string;
  phone?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

// types/api.d.ts
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageRequest {
  page: number;
  pageSize: number;
}

export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}
```

## 四、UI 开发规范

### 4.1 样式规范

#### BEM 命名

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

#### CSS 变量

```scss
// variables.scss
:root {
  // 颜色
  --color-primary: #409eff;
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  // 间距
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  // 圆角
  --border-radius-sm: 4px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
}
```

### 4.2 响应式设计

```scss
// mixins.scss
@mixin respond-to($breakpoint) {
  @if $breakpoint == "mobile" {
    @media (max-width: 768px) {
      @content;
    }
  } @else if $breakpoint == "tablet" {
    @media (min-width: 769px) and (max-width: 1024px) {
      @content;
    }
  } @else if $breakpoint == "desktop" {
    @media (min-width: 1025px) {
      @content;
    }
  }
}

// 使用
.container {
  padding: 32px;

  @include respond-to("mobile") {
    padding: 16px;
  }
}
```

## 五、性能优化

### 5.1 组件懒加载

```typescript
// 路由懒加载（推荐）
const Home = () => import("@/views/Home.vue");

// 组件懒加载
import { defineAsyncComponent } from "vue";

const AsyncComp = defineAsyncComponent({
  loader: () => import("@/components/HeavyComponent.vue"),
  loadingComponent: LoadingSpinner, // 加载中显示的组件
  errorComponent: ErrorComponent, // 加载失败显示的组件
  delay: 200, // 延迟显示加载组件的时间
  timeout: 3000, // 超时时间
});
```

### 5.2 列表优化

```vue
<template>
  <!-- 使用 v-memo 缓存（Vue 3.2+） -->
  <div v-for="item in list" :key="item.id" v-memo="[item.id, item.status]">
    {{ item.name }}
  </div>

  <!-- 虚拟滚动（大列表，使用 vue-virtual-scroller） -->
  <RecycleScroller :items="largeList" :item-size="50" key-field="id">
    <template #default="{ item }">
      <div class="item">{{ item.name }}</div>
    </template>
  </RecycleScroller>

  <!-- 分页加载 -->
  <div v-infinite-scroll="loadMore" :infinite-scroll-disabled="loading">
    <div v-for="item in list" :key="item.id">{{ item.name }}</div>
  </div>
</template>
```

### 5.3 打包优化

```typescript
// vite.config.ts
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import { visualizer } from "rollup-plugin-visualizer";

export default defineConfig({
  plugins: [
    vue(),
    // 打包分析
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true,
    }),
  ],
  build: {
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          // Vue 全家桶
          "vue-vendor": ["vue", "vue-router", "pinia"],
          // UI 框架
          "ui-vendor": ["element-plus"],
          // 工具库
          "utils-vendor": ["axios", "dayjs", "lodash-es"],
        },
      },
    },
    // 压缩配置
    minify: "terser",
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除 console
        drop_debugger: true, // 移除 debugger
      },
    },
    // chunk 大小警告限制
    chunkSizeWarningLimit: 1000,
    // 启用 CSS 代码分割
    cssCodeSplit: true,
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: ["vue", "vue-router", "pinia", "axios"],
  },
});
```

### 5.4 图片优化

```vue
<template>
  <!-- 懒加载图片 -->
  <img v-lazy="imageUrl" alt="description" />

  <!-- 响应式图片 -->
  <img
    :srcset="`${image_small} 480w, ${image_medium} 800w, ${image_large} 1200w`"
    sizes="(max-width: 600px) 480px, (max-width: 900px) 800px, 1200px"
    :src="image_medium"
    alt="description"
  />

  <!-- WebP 格式 -->
  <picture>
    <source :srcset="imageWebp" type="image/webp" />
    <img :src="imageJpg" alt="description" />
  </picture>
</template>
```

### 5.5 性能监控

```typescript
// utils/performance.ts
export function measurePerformance() {
  if (typeof window !== "undefined" && window.performance) {
    const perfData = window.performance.timing;
    const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
    const connectTime = perfData.responseEnd - perfData.requestStart;
    const renderTime = perfData.domComplete - perfData.domLoading;

    console.log("页面加载时间:", pageLoadTime, "ms");
    console.log("请求响应时间:", connectTime, "ms");
    console.log("页面渲染时间:", renderTime, "ms");
  }
}

// 在 App.vue 中使用
onMounted(() => {
  measurePerformance();
});
```

## 六、测试规范

> 测试原则和最佳实践请参考 `~/.kiro/steering/testing-standards.md`

### 6.1 单元测试（Vitest）

```typescript
import { describe, it, expect, beforeEach, vi } from "vitest";
import { mount } from "@vue/test-utils";
import UserCard from "@/components/UserCard.vue";
import type { User } from "@/types/user";

describe("UserCard", () => {
  let mockUser: User;

  beforeEach(() => {
    mockUser = {
      id: 1,
      username: "test",
      email: "test@example.com",
      nickname: "Test User",
      createdAt: "2024-01-01T00:00:00Z",
      updatedAt: "2024-01-01T00:00:00Z",
    };
  });

  it("renders user name correctly", () => {
    // Arrange
    const wrapper = mount(UserCard, {
      props: { user: mockUser },
    });

    // Assert
    expect(wrapper.text()).toContain("test");
    expect(wrapper.text()).toContain("test@example.com");
  });

  it("emits update event when button clicked", async () => {
    // Arrange
    const wrapper = mount(UserCard, {
      props: { user: mockUser },
    });

    // Act
    await wrapper.find("button").trigger("click");

    // Assert
    expect(wrapper.emitted("update")).toBeTruthy();
    expect(wrapper.emitted("update")?.[0]).toEqual([mockUser]);
  });

  it("displays avatar when provided", () => {
    // Arrange
    const userWithAvatar = { ...mockUser, avatar: "https://example.com/avatar.jpg" };
    const wrapper = mount(UserCard, {
      props: { user: userWithAvatar },
    });

    // Assert
    const img = wrapper.find("img");
    expect(img.exists()).toBe(true);
    expect(img.attributes("src")).toBe(userWithAvatar.avatar);
  });
});
```

### 6.2 Composables 测试

```typescript
import { describe, it, expect, vi } from "vitest";
import { useRequest } from "@/composables/useRequest";

describe("useRequest", () => {
  it("executes request successfully", async () => {
    // Arrange
    const mockData = { id: 1, name: "test" };
    const requestFn = vi.fn().mockResolvedValue(mockData);
    const onSuccess = vi.fn();

    // Act
    const { data, loading, execute } = useRequest(requestFn, { onSuccess });
    await execute();

    // Assert
    expect(data.value).toEqual(mockData);
    expect(loading.value).toBe(false);
    expect(onSuccess).toHaveBeenCalledWith(mockData);
  });

  it("handles request error", async () => {
    // Arrange
    const error = new Error("Request failed");
    const requestFn = vi.fn().mockRejectedValue(error);
    const onError = vi.fn();

    // Act
    const { error: errorRef, execute } = useRequest(requestFn, { onError });
    await execute().catch(() => {});

    // Assert
    expect(errorRef.value).toEqual(error);
    expect(onError).toHaveBeenCalledWith(error);
  });
});
```

### 6.3 E2E 测试（Playwright）

```typescript
import { test, expect } from "@playwright/test";

test.describe("Login Flow", () => {
  test("should login successfully with valid credentials", async ({ page }) => {
    // 访问登录页
    await page.goto("/login");

    // 输入用户名和密码
    await page.fill('input[name="username"]', "test");
    await page.fill('input[name="password"]', "Test123456");

    // 点击登录按钮
    await page.click('button[type="submit"]');

    // 验证跳转到首页
    await expect(page).toHaveURL("/home");
    await expect(page.locator("h1")).toContainText("首页");
  });

  test("should show error with invalid credentials", async ({ page }) => {
    await page.goto("/login");

    await page.fill('input[name="username"]', "invalid");
    await page.fill('input[name="password"]', "wrong");
    await page.click('button[type="submit"]');

    // 验证错误提示
    await expect(page.locator(".error-message")).toBeVisible();
  });
});
```

## 七、代码规范工具

### 7.1 ESLint 配置

```javascript
// .eslintrc.cjs
module.exports = {
  extends: [
    "plugin:vue/vue3-recommended",
    "@vue/eslint-config-typescript",
    "@vue/eslint-config-prettier",
  ],
  rules: {
    "vue/multi-word-component-names": "off",
    "vue/require-default-prop": "off",
    "@typescript-eslint/no-explicit-any": "warn",
  },
};
```

### 7.2 Prettier 配置

```json
{
  "semi": false,
  "singleQuote": true,
  "printWidth": 100,
  "trailingComma": "none",
  "arrowParens": "always"
}
```

### 7.3 Git Hooks

```json
// package.json
{
  "lint-staged": {
    "*.{js,ts,vue}": ["eslint --fix", "prettier --write"],
    "*.{css,scss,vue}": ["prettier --write"]
  }
}
```

## 八、部署规范

### 8.1 环境变量

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=开发环境

# .env.test
VITE_API_BASE_URL=https://api.test.com
VITE_APP_TITLE=测试环境

# .env.production
VITE_API_BASE_URL=https://api.production.com
VITE_APP_TITLE=生产环境
```

### 8.2 构建命令

```bash
# 开发
npm run dev

# 构建（生产环境）
npm run build

# 构建（测试环境）
npm run build:test

# 预览构建结果
npm run preview

# 类型检查
npm run type-check

# 代码检查
npm run lint

# 代码格式化
npm run format

# 打包分析
npm run build:analyze
```

### 8.3 package.json 脚本配置

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc --noEmit && vite build",
    "build:test": "vue-tsc --noEmit && vite build --mode test",
    "build:analyze": "vue-tsc --noEmit && vite build --mode analyze",
    "preview": "vite preview",
    "type-check": "vue-tsc --noEmit",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --fix",
    "format": "prettier --write src/",
    "prepare": "husky install"
  }
}
```

### 8.4 Nginx 配置

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
    }
}
```

### 8.4 Nginx 配置

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/html;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript
               application/json application/javascript application/xml+rss;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

### 8.5 Docker 部署

```dockerfile
# Dockerfile
FROM node:18-alpine as build-stage

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```yaml
# docker-compose.yml
version: "3.8"
services:
  web:
    build: .
    ports:
      - "80:80"
    environment:
      - NODE_ENV=production
    restart: always
```
