<!-- @format -->

# Vue3 Web 端开发规范

> 用户级别规范，跨项目复用。详细规范参考 `references/` 目录。

---

## 一、架构理念

### Feature-First（特性优先）

按业务功能垂直拆分。修改功能时，文件修改范围应集中在单一 feature 目录内。

### API-First（接口先行）

编码前先定义接口和类型，确保契约稳定。API 类型与业务类型分离。

---

## 二、组件开发原则（强制）

- **必须**使用 `<script setup lang="ts">` 语法
- **必须**显式声明 Props 和 Emits 类型
- 组件文件**必须**使用 PascalCase 命名
- **禁止**在 v-for 元素上使用 v-if，**必须**用 computed 过滤
- **推荐**使用 `watch` 而非 `watchEffect`（避免隐式依赖）

---

---

## 三、详细规范索引

| 文档                                         | 内容                     | 参考标准          |
| -------------------------------------------- | ------------------------ | ----------------- |
| [01-naming](references/01-naming.md)         | 文件、组件、CSS 命名规范 | Vue 3 Style Guide |
| [02-component](references/02-component.md)   | 组件结构、Props、Emits   | Vue 3 Style Guide |
| [03-state](references/03-state.md)           | Pinia Store、Composables | -                 |
| [04-routing](references/04-routing.md)       | Vue Router 配置、守卫    | Vue Router 官方   |
| [05-testing](references/05-testing.md)       | Vitest 单元测试          | -                 |
| [06-deployment](references/06-deployment.md) | 环境变量、Nginx 配置     | -                 |
