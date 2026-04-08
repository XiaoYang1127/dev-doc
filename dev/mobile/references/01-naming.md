# 命名规范

## 文件命名

- 使用小写下划线：`user_profile_page.dart`、`login_button.dart`
- Widget 文件：`xxx_page.dart`、`xxx_widget.dart`
- Model 文件：`xxx_model.dart`
- Provider 文件：`xxx_provider.dart`

## 类命名

| 类型 | 规则 | 示例 |
| ---- | ---- | ---- |
| Page | PascalCase | `UserProfilePage`、`LoginPage` |
| Widget | PascalCase | `LoginButton`、`UserAvatar` |
| Model | PascalCase | `UserModel`、`OrderModel` |
| Provider | PascalCase | `AuthProvider`、`UserProvider` |
| Repository | PascalCase | `UserRepository`、`OrderRepository` |
| Service | PascalCase | `ApiService`、`StorageService` |

## 变量命名

- 使用驼峰命名：`userName`、`isLoading`
- 私有变量：`_userName`、`_isLoading`
- 常量：`kPrimaryColor`、`kDefaultPadding`
- 枚举：`UserStatus.active`、`OrderType.online`

## 目录命名

- 全部使用小写下划线
- 功能模块目录：`features/auth`、`features/home`
- 组件目录：`shared/widgets/buttons`
