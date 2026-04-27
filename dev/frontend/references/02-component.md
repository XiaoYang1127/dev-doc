# 组件开发规范

> 核心规则参见 [Vue 3 Style Guide](https://vuejs.org/style-guide/)。以下为强制内联条目。

## 组件结构顺序

```vue
<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from "vue";

// 2. Props 定义（必须显式声明）
interface Props {
  title: string;
  size?: "small" | "medium" | "large";
}
const props = withDefaults(defineProps<Props>(), {
  size: "medium",
});

// 3. Emits 定义
const emit = defineEmits<{
  click: [];
  update: [value: string];
}>();

// 4. 响应式数据
const isLoading = ref(false);

// 5. 计算属性
const displayName = computed(() => {
  return props.user?.nickname || props.user?.username;
});

// 6. 方法
const handleUpdate = () => {};

// 7. 生命周期
onMounted(() => {});

// 8. 暴露给父组件
defineExpose({ refresh: () => {} });
</script>
```

## Props 验证（强制）

```typescript
// ✅ 必须使用 TypeScript 联合类型校验
interface Props {
  status: "pending" | "active" | "closed";
  size?: "small" | "medium" | "large";
}
const props = withDefaults(defineProps<Props>(), {
  size: "medium",
});
```

## defineModel 双向绑定（Vue 3.3+）

优先使用 `defineModel` 简化双向绑定，减少 `props + emit` 模板代码：

```vue
<script setup lang="ts">
// ✅ 推荐：defineModel 双向绑定
const model = defineModel<string>();
const checked = defineModel<boolean>('checked');
</script>

<template>
  <!-- 等价于 :model-value="model" @update:model-value="model = $event" -->
  <input v-model="model" />

  <!-- 等价于 :checked="checked" @update:checked="checked = $event" -->
  <input type="checkbox" v-model="checked" />
</template>
```

对比传统写法：

```typescript
// ❌ 传统写法：繁琐
const props = defineProps<{ modelValue: string }>();
const emit = defineEmits<{ 'update:modelValue': [value: string] }>();

// ✅ defineModel：简洁
const model = defineModel<string>();
```

## v-if 与 v-for 优先级（强制）

v-for 元素上禁止使用 v-if，必须用 computed 过滤。

```vue
<!-- ❌ 错误 -->
<li v-for="user in users" v-if="user.isActive">

<!-- ✅ 正确 -->
<li v-for="user in activeUsers">

<script setup>
const activeUsers = computed(() =>
  users.value.filter(user => user.isActive)
);
</script>
```

## 组件 ref 模式（强烈推荐）

通过 composable 而非直接在组件上用 ref。

```typescript
// ✅ composable 封装
export function useForm<T>() {
  const ref = ref<T | null>(null);
  const validate = () => ref.value?.validate();
  const reset = () => ref.value?.reset();
  return { ref, validate, reset };
}

// 使用
const { ref: formRef, validate } = useForm<InstanceType<typeof UserForm>>();
```

## watchEffect 警告（阿里前端规范）

禁止使用 `watchEffect`，优先使用 `watch`。

```typescript
// ❌
watchEffect(() => {
  console.log(user.value.name); // 隐式依赖
});

// ✅
watch(user, (newUser) => {
  console.log(newUser.name);
}, { immediate: true });
```

## 组件分类

| 类别 | 存放位置 | 说明 |
| ---- | -------- | ---- |
| 基础组件 | `components/common/` | Button、Input、Modal |
| 业务组件 | `components/business/` | UserCard、OrderTable |
| 布局组件 | `layouts/` | DefaultLayout、BlankLayout |
| 页面组件 | `views/` 或 `features/*/views/` | 路由页面 |
