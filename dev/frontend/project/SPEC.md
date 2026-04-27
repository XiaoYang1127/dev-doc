<!-- @format -->

# 项目技术规范

> 本目录为项目级别规范，复制到新项目时按需修改。

## 技术栈

| 类别     | 技术                                                  | 版本                  |
| -------- | ----------------------------------------------------- | --------------------- |
| 框架     | Vue                                                   | 3.x (Composition API) |
| 语言     | TypeScript                                            | 5.x+                  |
| 构建     | Vite                                                  | 5.x                   |
| 路由     | Vue Router                                            | 4.x                   |
| 状态管理 | Pinia                                                 | 2.x                   |
| UI 框架  | 自选（按需选择）                               | 参考：生态、团队熟悉度、主题定制需求 |
| HTTP     | Axios                                                 | -                     |

---

## 目录结构（Feature-First + API-First）

```
src/
├── main.ts                  # 应用入口
├── App.vue                  # 根组件
├── api/                     # API 接口封装
├── assets/                  # 静态资源
├── components/              # 通用 UI 组件（基础组件）
│   └── common/            # Button、Input、Modal 等
├── composables/            # 组合式函数
├── layouts/                # 布局组件
├── router/                 # 路由配置
├── stores/                 # 状态管理
├── types/                  # 类型定义
└── features/               # 功能模块
    ├── auth/              # 认证模块
    │   ├── api/
    │   ├── components/
    │   └── views/
    ├── user/              # 用户模块
    │   └── ...
    └── home/              # 首页模块
```

### 模块内分层职责

| 层级             | 职责                     | 不管     |
| ---------------- | ------------------------ | -------- |
| **api/**         | HTTP 调用、请求/响应类型 | UI 逻辑  |
| **types/**       | 业务类型定义             | 实现     |
| **composables/** | 业务逻辑、页面状态       | UI 展示  |
| **components/**  | 纯视图展示               | 业务逻辑 |

---

## 分页响应类型

```typescript
// types/common.ts
export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface PageRequest {
  page: number;
  pageSize: number;
}
```

---

## 常用命令

```bash
# 开发
npm run dev

# 构建
npm run build

# 类型检查
npm run type-check

# 代码检查
npm run lint

# 打包分析
npm run build:analyze
```

---

## API 封装

```typescript
// utils/request.ts
import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import router from '@/router';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

// 请求拦截器 - 添加 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  (response: AxiosResponse) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权
      localStorage.removeItem('token');
      router.push('/login');
    }
    return Promise.reject(error);
  },
);

export default request;
```

### API 模块示例

```typescript
// features/user/api/index.ts
import request from '@/utils/request';
import type { User, PageResult, PageRequest } from '@/types/user';

export const userApi = {
  getList: (params: PageRequest & { keyword?: string }) => request.get<PageResult<User>>('/users', { params }),

  getById: (id: string) => request.get<User>(`/users/${id}`),

  create: (data: CreateUserRequest) => request.post<User>('/users', data),

  update: (id: string, data: UpdateUserRequest) => request.put<User>(`/users/${id}`, data),

  delete: (id: string) => request.delete(`/users/${id}`),
};
```

---

## 性能优化

- 路由懒加载：`() => import('./views/Home.vue')`
- 图片资源：按需加载、压缩、缓存
- 防抖节流：高频事件（搜索输入、窗口 resize）必须处理

---

## 代码规范配置

### ESLint

```json
// .eslintrc.cjs
{
  "extends": ["plugin:vue/vue3-recommended", "@vue/ts/recommended"],
  "rules": {
    // 允许单词组件名（根组件）
    "vue/multi-word-component-names": "off",
    // 安全：禁止 v-html
    "vue/no-v-html": "warn",
    // 类型：禁止未使用变量
    "@typescript-eslint/no-unused-vars": ["error", { "argsIgnorePattern": "^_" }]
  }
}
```

### Prettier

```json
// .prettierrc.json
{
  "semi": false,
  "singleQuote": true,
  "printWidth": 100,
  "trailingComma": "es5",
  "arrowParens": "always",
  "endOfLine": "auto"
}
```

### TypeScript

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    },
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "skipLibCheck": true
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

```json
// tsconfig.node.json（Vite 构建配置）
{
  "compilerOptions": {
    "composite": true,
    "module": "ESNext",
    "moduleResolution": "bundler"
  },
  "include": ["vite.config.ts"]
}
```
