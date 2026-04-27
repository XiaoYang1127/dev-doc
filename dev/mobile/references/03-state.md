# 状态管理规范

> 核心规则参见 [Flutter 官方架构文档](https://docs.flutter.dev/development/data-and-backend/state-mgmt) 和 [Riverpod 2.0](https://riverpod.dev/)。以下为强制内联条目。

## 官方架构分层

| 层级 | 职责 | 推荐实现 |
| ---- | ---- | -------- |
| **UI Layer** | Widget 展示、用户交互 | StatelessWidget / StatefulWidget |
| **Business Logic Layer** | 状态管理、业务规则 | Riverpod Provider |
| **Data Layer** | 数据获取、持久化 | Repository + DataSource |

## Repository 模式

```dart
class ArticleRepository {
  ArticleRepository(this._remoteDataSource);
  final ArticleRemoteDataSource _remoteDataSource;
  ArticleModel? _cache;

  Future<ArticleModel> getArticle(String id) async {
    _cache ??= await _remoteDataSource.getArticle(id);
    return _cache!;
  }
}
```

## 依赖注入（get_it）

```dart
final getIt = GetIt.instance;

void setupDependencies() {
  getIt.registerLazySingleton<ApiClient>(() => ApiClient());
  getIt.registerLazySingleton<UserRepository>(
    () => UserRepository(getIt<ApiClient>()),
  );
}
```

## Riverpod 2.0（官方推荐）

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

// StateNotifier + sealed class 状态建模
sealed class UserState {}

class UserInitial extends UserState {}

class UserLoading extends UserState {}

class UserLoaded extends UserState {
  final User user;
  UserLoaded(this.user);
}

class UserError extends UserState {
  final String message;
  UserError(this.message);
}

// Provider 定义
final userProvider = StateNotifierProvider<UserNotifier, UserState>((ref) {
  return UserNotifier();
});

class UserNotifier extends StateNotifier<UserState> {
  UserNotifier() : super(UserInitial());

  Future<void> fetchUser() async {
    state = UserLoading();
    try {
      final user = await repository.getUser();
      state = UserLoaded(user);
    } catch (e) {
      state = UserError(e.toString());
    }
  }
}
```

## sealed class 状态建模（强制）

```dart
// ✅ 使用 sealed class 强制穷举检查
switch (userState) {
  case UserInitial() => Text('Init'),
  case UserLoading() => CircularProgressIndicator(),
  case UserLoaded(:final user) => Text(user.name),
  case UserError(:final message) => Text('Error: $message'),
}
```

## const 构造（强制）

所有 Widget 尽可能使用 const 构造。

```dart
// ✅
return const Padding(
  padding: EdgeInsets.all(16),
  child: Text('Hello'),
);

// ❌
return Padding(
  padding: EdgeInsets.all(16),
  child: Text('Hello'),
);
```

## 状态位置划分

| 状态位置 | 适用场景 |
| -------- | -------- |
| Widget 内部 | UI 相关状态 |
| StateProvider | 简单可复用状态 |
| StateNotifierProvider | 复杂业务逻辑状态 |
| 全局单例（get_it） | 跨模块共享（ApiClient、Repository） |
