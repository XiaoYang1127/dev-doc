---
inclusion: auto
---

# Vue3 Web 端开发规范

> 详细规范参考 `references/` 目录，Claude Code 会自动扫描相关文件。

## 一、技术栈

| 类别 | 技术 | 版本 |
| ---- | ---- | ---- |
| 框架 | Vue | 3.x (Composition API) |
| 语言 | TypeScript | 5.x+ |
| 构建 | Vite | 5.x |
| 路由 | Vue Router | 4.x |
| 状态 | Pinia | 2.x |
| UI 框架 | Element Plus | 推荐 |

## 二、目录结构（Feature-First）

```
src/
├── main.ts                  # 应用入口
├── App.vue                  # 根组件
├── api/                     # API 接口
├── assets/                  # 静态资源
├── components/              # 通用组件
│   ├── common/            # 基础组件
│   └── business/          # 业务组件
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
    └── home/              # 首页模块
```

## 三、核心原则

### Feature-First（特性优先）

按业务功能垂直拆分。修改功能时，文件修改范围应集中在单一 feature 目录内。

### API-First（接口先行）

编码前先定义接口和类型，确保契约稳定。API 类型与业务类型分离。

### 组件开发

- 使用 `<script setup lang="ts">` 语法
- Props 和 Emits 必须显式声明
- 组件文件使用 PascalCase 命名

## 四、常用命令

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

## 五、references 导航

| 文档 | 内容 |
| ---- | ---- |
| 01-naming | 文件、组件、CSS 命名规范 |
| 02-component | 组件结构、Props、Emits 定义 |
| 03-state | Pinia Store、Composables 封装 |
| 04-routing | Vue Router 配置、守卫、懒加载 |
| 05-testing | Vitest 单元测试、Playwright E2E |
| 06-deployment | 环境变量、Nginx 配置、Docker |

## 六、API 封装

```typescript
// utils/request.ts
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

// 请求拦截器 - 添加 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权
    }
    return Promise.reject(error);
  }
);
```

## 七、CSS 变量

```scss
:root {
  --color-primary: #409eff;
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --border-radius: 8px;
}
```

## 八、代码规范工具

```json
// .eslintrc.cjs
{
  "extends": ["plugin:vue/vue3-recommended"],
  "rules": {
    "vue/multi-word-component-names": "off"
  }
}
```

```json
// .prettierrc.json
{
  "semi": false,
  "singleQuote": true,
  "printWidth": 100
}
```
