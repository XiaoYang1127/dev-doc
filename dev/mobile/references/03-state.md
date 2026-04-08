# 状态管理规范

## Provider 类型选择

| 类型 | 适用场景 | 示例 |
| ---- | -------- | ---- |
| `StateProvider` | 简单状态 | 开关、计数器 |
| `FutureProvider` | 单次异步数据 | 用户信息获取 |
| `StreamProvider` | 实时数据流 | 消息列表 |
| `StateNotifierProvider` | 复杂状态逻辑 | 购物车、表单 |

## Repository 模式

```dart
class UserRepository {
  UserRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<User> getCurrentUser() async {
    final data = await _apiClient.get('/api/v1/user/me');
    return User.fromJson(data);
  }

  Future<void> updateProfile(UserUpdateRequest request) async {
    await _apiClient.put('/api/v1/user/profile', data: request.toJson());
  }
}
```

## 依赖注入（get_it）

```dart
// 初始化
final getIt = GetIt.instance;

void setupDependencies() {
  getIt.registerLazySingleton<ApiClient>(() => ApiClient());
  getIt.registerLazySingleton<UserRepository>(
    () => UserRepository(getIt<ApiClient>()),
  );
}

// 使用
final repository = getIt<UserRepository>();
```

## 状态划分原则

| 状态位置 | 说明 |
| -------- | ---- |
| Widget 内部 | UI 相关状态 |
| StateProvider | 简单可复用状态 |
| StateNotifier | 复杂业务逻辑状态 |
| 全局单例 | 跨模块共享（ApiClient、StorageService） |
