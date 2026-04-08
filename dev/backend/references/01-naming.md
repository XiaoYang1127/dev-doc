# 命名规范

## 包命名

- 全小写，使用点分隔：`com.company.project.user.service`

## 类命名

| 类型 | 命名规则 | 示例 |
| ---- | -------- | ---- |
| Facade | PascalCase | `UserFacade`、`OrderFacade` |
| Controller | PascalCase | `UserController`、`OrderController` |
| Service | PascalCase | `UserService`、`OrderService` |
| ServiceImpl | PascalCase | `UserServiceImpl`、`OrderServiceImpl` |
| Domain Model | PascalCase | `User`、`Order` |
| Entity | PascalCase | `UserEntity`、`OrderEntity` |
| Mapper | PascalCase | `UserMapper`、`OrderMapper` |
| Repository | PascalCase | `UserRepository`、`OrderRepository` |
| DTO | PascalCase | `UserCreateRequest`、`UserResponse` |
| Converter | PascalCase | `UserConverter`、`OrderConverter` |

## 方法命名

| 操作 | 命名规则 | 示例 |
| ---- | -------- | ---- |
| 查询单个 | `getBy` + 字段 | `getById`、`getByUsername` |
| 查询列表 | `list` / `listBy` + 条件 | `list`、`listByStatus` |
| 分页查询 | `page` / `pageBy` + 条件 | `page`、`pageByCondition` |
| 新增 | `create` / `save` | `create`、`save` |
| 更新 | `update` / `updateById` | `update`、`updateById` |
| 删除 | `delete` / `deleteById` | `delete`、`deleteById` |
| 判断 | `exists` / `isValid` | `exists`、`isValid` |

## 数据库表命名

- 小写下划线分隔：`t_user`、`t_order`
- 中间表：`t_user_role`
- 字段：`user_id`、`created_at`、`updated_at`、`deleted_at`
- 时间字段统一使用 `BIGINT` 类型存储毫秒时间戳

## Key 命名（Redis）

```
业务模块:功能:标识
user:info:123
order:detail:456
product:list:category:1
```
