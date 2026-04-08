# 状态管理规范

## Pinia Store 结构

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
    logout,
  };
});
```

## Store 使用

```vue
<script setup lang="ts">
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

// 在模板中使用
console.log(userStore.userName);

// 调用 action
await userStore.login("username", "password");
</script>
```

## Composables 封装

```typescript
// composables/useRequest.ts
import { ref } from "vue";

export function useRequest<T>(requestFn: () => Promise<T>) {
  const data = ref<T | null>(null);
  const loading = ref(false);
  const error = ref<Error | null>(null);

  const execute = async () => {
    loading.value = true;
    error.value = null;
    try {
      data.value = await requestFn();
    } catch (e) {
      error.value = e as Error;
    } finally {
      loading.value = false;
    }
  };

  return { data, loading, error, execute };
}
```

## 状态划分原则

| Store | 作用域 | 说明 |
| ---- | ------ | ---- |
| userStore | 全局 | 用户信息、登录状态 |
| appStore | 全局 | 主题、语言设置 |
| 页面级状态 | 局部 | 使用 ref/computed |

- 避免过度拆分，一个功能一个 store
- 跨组件共享的状态放入 store
- 组件内部状态使用 ref
