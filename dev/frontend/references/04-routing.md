# 路由规范

## 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

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
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;
```

## 路由守卫

```typescript
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
```

## 路由懒加载

```typescript
// 组件懒加载（推荐）
const Home = () => import("@/views/Home.vue");

// 路由懒加载
{
  path: "/home",
  component: () => import("@/features/home/views/Home.vue"),
}
```

## 路由命名

- name 使用 PascalCase：`name: "Home"`
- path 使用 kebab-case：`path: "/user-profile"`
- 遵循 RESTful 资源命名
