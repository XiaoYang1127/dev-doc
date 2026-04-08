# 组件开发规范

## 组件结构顺序

```vue
<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from "vue";

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
}
const emit = defineEmits<Emits>();

// 4. 响应式数据
const isLoading = ref(false);

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
  refresh: () => {},
});
</script>

<template>
  <div class="user-card">
    <div class="user-card__avatar">
      <img :src="user.avatar" :alt="displayName" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.user-card {
  display: flex;
}
</style>
```

## 组件分类

| 类别 | 存放位置 | 说明 |
| ---- | -------- | ---- |
| 基础组件 | `components/common/` | Button、Input、Modal 等 |
| 业务组件 | `components/business/` | UserCard、OrderTable 等 |
| 布局组件 | `layouts/` | DefaultLayout、BlankLayout 等 |
| 页面组件 | `views/` 或 `features/*/views/` | 路由页面 |

## Props 定义

```typescript
// 简单类型
defineProps<{
  title: string;
  count?: number;
}>();

// 带默认值
const props = withDefaults(defineProps<{
  title: string;
  count?: number;
}>(), {
  count: 0,
});
```

## emits 定义

```typescript
// 简洁模式
const emit = defineEmits<{
  (e: "click"): void;
  (e: "update", value: string): void;
}>();

// 对象模式（更好的类型提示）
const emit = defineEmits<{
  click: [];
  update: [value: string];
}>();
```
