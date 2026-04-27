<!-- @format -->

# 项目技术规范

> 本目录为项目级别规范，复制到新项目时按需修改。

## 技术栈

| 类别     | 技术                   | 版本 |
| -------- | ---------------------- | ---- |
| 框架     | Flutter                | 3.x+ |
| 语言     | Dart                   | 3.x+ |
| 状态管理 | Riverpod（官方推荐）   | 2.x  |
| 路由     | go_router（官方推荐）  | -    |
| 网络     | dio                    | -    |
| 本地存储 | shared_preferences     | -    |
| 安全存储 | flutter_secure_storage | -    |
| 依赖注入 | get_it                 | -    |

---

## 目录结构（Feature-First + Clean Architecture）

```
lib/
├── main.dart                   # 应用入口
├── app/                        # 应用配置
│   ├── app.dart               # App 根组件
│   ├── router.dart            # 路由配置
│   └── theme.dart             # 主题配置
├── core/                       # 核心模块
│   ├── network/              # 网络层（dio 封装）
│   ├── storage/              # 存储层
│   ├── di/                   # 依赖注入（get_it）
│   └── utils/                # 工具类
├── shared/                     # 共享模块
│   ├── widgets/              # 通用组件
│   └── constants/            # 共享常量
└── features/                   # 功能模块
    ├── auth/                 # 认证模块
    │   ├── data/
    │   │   ├── models/
    │   │   └── datasources/
    │   ├── domain/
    │   │   ├── entities/
    │   │   └── repositories/
    │   └── presentation/
    │       ├── pages/
    │       └── providers/
    └── home/                 # 首页模块
```

### 模块内分层职责

| 层级              | 职责                         | 不管         |
| ----------------- | ---------------------------- | ------------ |
| **presentation/** | Widget、Page、Provider       | 数据获取逻辑 |
| **domain/**       | 实体、仓储接口、业务规则     | 数据源实现   |
| **data/**         | 仓储实现、数据源（API/本地） | 业务规则     |

---

## 常用命令

```bash
# 获取依赖
flutter pub get

# 代码生成（Riverpod、freezed 等）
flutter pub run build_runner build --delete-conflicting-outputs

# 分析代码
flutter analyze

# 测试
flutter test

# Android 打包
flutter build apk --release

# iOS 打包
flutter build ios --release
```

---

## 网络请求封装

```dart
// core/network/api_client.dart
class ApiClient {
  ApiClient({required String baseUrl}) {
    _dio = Dio(BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ));
    _dio.interceptors.add(AuthInterceptor());
    _dio.interceptors.add(LogInterceptor());
  }

  late final Dio _dio;

  Future<T> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    final response = await _dio.get<T>(
      path,
      queryParameters: queryParameters,
      options: options,
    );
    return _handleResponse<T>(response);
  }

  Future<T> post<T>(
    String path, {
    dynamic data,
    Options? options,
  }) async {
    final response = await _dio.post<T>(
      path,
      data: data,
      options: options,
    );
    return _handleResponse<T>(response);
  }

  T _handleResponse<T>(DioResponse response) {
    if (response.statusCode == 200) {
      return response.data as T;
    }
    throw ApiException(response.statusMessage);
  }
}
```

### AuthInterceptor 示例

```dart
class AuthInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final token = localStorage.getToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response?.statusCode == 401) {
      // 处理未授权 - 跳转登录
      getIt<AuthNotifier>().logout();
    }
    handler.next(err);
  }
}
```

---

## 依赖注入（get_it）

```dart
// core/di/injection.dart
final getIt = GetIt.instance;

void setupDependencies() {
  // API Client
  getIt.registerLazySingleton<ApiClient>(
    () => ApiClient(baseUrl: Env.apiBaseUrl),
  );

  // Repositories
  getIt.registerLazySingleton<UserRepository>(
    () => UserRepositoryImpl(getIt<ApiClient>()),
  );

  getIt.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(getIt<ApiClient>()),
  );
}
```

---

## 主题配置

```dart
// app/theme.dart
class AppTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: const Color(0xFF409EFF),
        brightness: Brightness.light,
      ),
      appBarTheme: const AppBarTheme(
        centerTitle: true,
        elevation: 0,
      ),
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }
}
```

---

## 本地存储

```dart
// 普通数据
final prefs = await SharedPreferences.getInstance();
await prefs.setString('token', token);
final token = prefs.getString('token');

// 敏感数据（加密存储）
final secureStorage = FlutterSecureStorage();
await secureStorage.write(key: 'token', value: token);
final token = await secureStorage.read(key: 'token');
```

---

## Riverpod 2.0 Provider 示例

```dart
// features/user/presentation/providers/user_provider.dart

// 状态建模（sealed class）
sealed class UserState {}

class UserInitial extends UserState {}

class UserLoading extends UserState {}

class UserLoaded extends UserState {
  UserLoaded(this.user);
  final User user;
}

class UserError extends UserState {
  UserError(this.message);
  final String message;
}

// StateNotifier
final userProvider = StateNotifierProvider<UserNotifier, UserState>((ref) {
  return UserNotifier(ref.read(userRepositoryProvider));
});

class UserNotifier extends StateNotifier<UserState> {
  UserNotifier(this._repository) : super(UserInitial());

  final UserRepository _repository;

  Future<void> fetchUser(String id) async {
    state = UserLoading();
    try {
      final user = await _repository.getUser(id);
      state = UserLoaded(user);
    } catch (e) {
      state = UserError(e.toString());
    }
  }
}
```

### Widget 中使用

```dart
// 页面中使用
class UserPage extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userState = ref.watch(userProvider);

    return switch (userState) {
      UserInitial() => const SizedBox.shrink(),
      UserLoading() => const CircularProgressIndicator(),
      UserLoaded(:final user) => Text(user.name),
      UserError(:final message) => Text('Error: $message'),
    };
  }
}
```

---

## 性能优化

- 使用 `const` 构造函数（强制）
- 长列表使用 `ListView.builder`
- 状态变化时优先使用 `ref.read` / `ref.watch` 而非重建 Widget
- 图片使用 `cached_network_image` 缓存
- 使用 `RepaintBoundary` 隔离重绘区域
