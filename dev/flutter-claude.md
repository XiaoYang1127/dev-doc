---
inclusion: auto
---

# Flutter 移动端开发规范

> **通用规范参考**：本规范是 Flutter/Dart 项目特定规范，通用开发规范请参考 `~/.kiro/steering/` 目录：
> - 开发铁律与基本原则：`development-principles.md`
> - 代码风格规范：`code-style.md`
> - API 开发标准：`api-standards.md`
> - 测试规范：`testing-standards.md`
> - Git 工作流规范：`git-workflow.md`

## 一、技术栈

### 1.1 核心技术

- **Flutter**: 3.x+
- **Dart**: 3.x+
- **状态管理**: Riverpod（推荐）/ Provider / GetX
- **路由管理**: go_router（推荐）
- **网络请求**: dio
- **本地存储**: shared_preferences（轻量）/ hive（复杂数据）
- **安全存储**: flutter_secure_storage（敏感数据）
- **依赖注入**: get_it / riverpod

### 1.2 常用库

- **UI 组件**: flutter_screenutil（屏幕适配）
- **图片**: cached_network_image
- **权限**: permission_handler
- **日期**: intl
- **JSON**: json_serializable
- **日志**: logger
- **Toast**: fluttertoast

## 二、项目架构

### 2.1 目录结构（Feature-First）

```
lib/
├── main.dart                   # 应用入口
├── app/                        # 应用配置
│   ├── app.dart               # App 根组件
│   ├── routes.dart            # 路由配置
│   ├── theme.dart             # 主题配置
│   └── constants.dart         # 全局常量
├── core/                       # 核心模块
│   ├── network/               # 网络层
│   │   ├── api_client.dart
│   │   ├── api_interceptor.dart
│   │   └── api_exception.dart
│   ├── storage/               # 存储层
│   │   ├── storage_service.dart
│   │   └── cache_manager.dart
│   ├── utils/                 # 工具类
│   │   ├── date_util.dart
│   │   ├── validator.dart
│   │   └── logger.dart
│   └── extensions/            # 扩展方法
│       ├── string_extension.dart
│       └── context_extension.dart
├── shared/                     # 共享模块
│   ├── widgets/               # 通用组件
│   │   ├── buttons/
│   │   ├── inputs/
│   │   └── loading/
│   ├── models/                # 通用模型
│   └── constants/             # 共享常量
├── features/                   # 功能模块
│   ├── auth/                  # 认证模块
│   │   ├── data/
│   │   │   ├── models/
│   │   │   ├── repositories/
│   │   │   └── datasources/
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   ├── repositories/
│   │   │   └── usecases/
│   │   └── presentation/
│   │       ├── pages/
│   │       ├── widgets/
│   │       └── providers/
│   ├── home/                  # 首页模块
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   └── profile/               # 个人中心模块
│       ├── data/
│       ├── domain/
│       └── presentation/
└── l10n/                       # 国际化
    ├── app_en.arb
    └── app_zh.arb

assets/
├── images/                     # 图片资源
├── icons/                      # 图标资源
└── fonts/                      # 字体资源
```

### 2.2 Clean Architecture 分层

- **Presentation Layer**: UI 和状态管理
- **Domain Layer**: 业务逻辑和实体
- **Data Layer**: 数据源和仓储实现

## 三、开发规范

### 3.1 命名规范

#### 文件命名

- 使用小写下划线：`user_profile_page.dart`、`login_button.dart`
- Widget 文件：`xxx_page.dart`、`xxx_widget.dart`
- Model 文件：`xxx_model.dart`
- Provider 文件：`xxx_provider.dart`

#### 类命名

- **Page**: `UserProfilePage`、`LoginPage`
- **Widget**: `LoginButton`、`UserAvatar`
- **Model**: `UserModel`、`OrderModel`
- **Provider**: `AuthProvider`、`UserProvider`
- **Repository**: `UserRepository`、`OrderRepository`
- **Service**: `ApiService`、`StorageService`

#### 变量命名

- 使用驼峰命名：`userName`、`isLoading`
- 私有变量：`_userName`、`_isLoading`
- 常量：`kPrimaryColor`、`kDefaultPadding`
- 枚举：`UserStatus.active`、`OrderType.online`

### 3.2 Widget 开发规范

#### StatelessWidget 示例

```dart
class UserAvatar extends StatelessWidget {
  const UserAvatar({
    super.key,
    required this.imageUrl,
    this.size = 40,
    this.onTap,
  });

  final String imageUrl;
  final double size;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: CircleAvatar(
        radius: size / 2,
        backgroundImage: CachedNetworkImageProvider(imageUrl),
      ),
    );
  }
}
```

#### StatefulWidget 示例

```dart
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();

  bool _isLoading = false;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      // 登录逻辑
    } catch (e) {
      // 错误处理
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('登录')),
      body: Form(
        key: _formKey,
        child: Column(
          children: [
            // UI 组件
          ],
        ),
      ),
    );
  }
}
```

### 3.3 状态管理（以 Riverpod 为例）

#### Provider 定义

```dart
// 简单状态
final counterProvider = StateProvider<int>((ref) => 0);

// 异步数据
final userProvider = FutureProvider<User>((ref) async {
  final repository = ref.read(userRepositoryProvider);
  return repository.getCurrentUser();
});

// 状态通知
class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier(this._repository) : super(const AuthState.initial());

  final AuthRepository _repository;

  Future<void> login(String username, String password) async {
    state = const AuthState.loading();

    try {
      final user = await _repository.login(username, password);
      state = AuthState.authenticated(user);
    } catch (e) {
      state = AuthState.error(e.toString());
    }
  }
}

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(ref.read(authRepositoryProvider));
});
```

#### 使用 Provider

```dart
class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userAsync = ref.watch(userProvider);

    return userAsync.when(
      data: (user) => Text('Hello, ${user.name}'),
      loading: () => const CircularProgressIndicator(),
      error: (error, stack) => Text('Error: $error'),
    );
  }
}
```

### 3.4 网络请求

#### API Client 封装

```dart
class ApiClient {
  late final Dio _dio;

  ApiClient() {
    _dio = Dio(BaseOptions(
      baseUrl: 'https://api.example.com',
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
      },
    ));

    _dio.interceptors.add(AuthInterceptor());
    _dio.interceptors.add(LogInterceptor(
      requestBody: true,
      responseBody: true,
    ));
  }

  Future<T> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
  }) async {
    try {
      final response = await _dio.get(path, queryParameters: queryParameters);
      return _parseResponse<T>(response);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<T> post<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
  }) async {
    try {
      final response = await _dio.post(
        path,
        data: data,
        queryParameters: queryParameters,
      );
      return _parseResponse<T>(response);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  T _parseResponse<T>(Response response) {
    final data = response.data;
    if (data is Map<String, dynamic>) {
      // 统一响应格式：{ code, message, data, traceId }
      final code = data['code'] as int;
      final message = data['message'] as String;

      if (code == 200) {
        return data['data'] as T;
      } else {
        throw ApiException(message, code: code);
      }
    }
    return data as T;
  }

  ApiException _handleError(DioException e) {
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
        return ApiException('连接超时', code: -1);
      case DioExceptionType.receiveTimeout:
        return ApiException('响应超时', code: -2);
      case DioExceptionType.badResponse:
        final statusCode = e.response?.statusCode;
        if (statusCode == 401) {
          return ApiException('未授权，请重新登录', code: 401);
        }
        return ApiException('服务器错误', code: statusCode ?? -3);
      default:
        return ApiException('网络错误', code: -4);
    }
  }
}

/// API 异常
class ApiException implements Exception {
  final String message;
  final int code;

  ApiException(this.message, {this.code = -1});

  @override
  String toString() => 'ApiException: $message (code: $code)';
}

/// 认证拦截器
class AuthInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    // 从存储中获取 token
    final token = StorageService.instance.getToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }

    // 添加 traceId
    options.headers['X-Trace-Id'] = _generateTraceId();

    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response?.statusCode == 401) {
      // Token 过期，清除登录状态
      StorageService.instance.clearToken();
      // 跳转到登录页
      // 注意：这里需要使用全局导航
    }
    handler.next(err);
  }

  String _generateTraceId() {
    return '${DateTime.now().millisecondsSinceEpoch}_${Random().nextInt(9999)}';
  }
}
```

#### Repository 实现

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

### 3.5 数据模型

#### Model 定义

```dart
import 'package:json_annotation/json_annotation.dart';

part 'user_model.g.dart';

@JsonSerializable()
class UserModel {
  const UserModel({
    required this.id,
    required this.username,
    required this.email,
    this.avatar,
  });

  final int id;
  final String username;
  final String email;
  final String? avatar;

  factory UserModel.fromJson(Map<String, dynamic> json) =>
      _$UserModelFromJson(json);

  Map<String, dynamic> toJson() => _$UserModelToJson(this);
}
```

#### 生成代码

```bash
flutter pub run build_runner build --delete-conflicting-outputs
```

### 3.6 路由管理（go_router）

```dart
final router = GoRouter(
  initialLocation: '/splash',
  routes: [
    GoRoute(
      path: '/splash',
      builder: (context, state) => const SplashPage(),
    ),
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginPage(),
    ),
    GoRoute(
      path: '/home',
      builder: (context, state) => const HomePage(),
      routes: [
        GoRoute(
          path: 'profile',
          builder: (context, state) => const ProfilePage(),
        ),
      ],
    ),
  ],
  redirect: (context, state) {
    final isLoggedIn = // 检查登录状态
    final isLoggingIn = state.matchedLocation == '/login';

    if (!isLoggedIn && !isLoggingIn) {
      return '/login';
    }
    return null;
  },
);
```

### 3.7 本地存储

```dart
/// 存储服务（单例模式）
class StorageService {
  StorageService._();
  static final instance = StorageService._();

  static const _keyToken = 'token';
  static const _keyUser = 'user';
  static const _keyTheme = 'theme';

  late final SharedPreferences _prefs;
  late final FlutterSecureStorage _secureStorage;

  /// 初始化（在 main 函数中调用）
  Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
    _secureStorage = const FlutterSecureStorage(
      aOptions: AndroidOptions(
        encryptedSharedPreferences: true,
      ),
    );
  }

  // ========== Token 管理（使用安全存储）==========

  Future<void> saveToken(String token) async {
    await _secureStorage.write(key: _keyToken, value: token);
  }

  Future<String?> getToken() async {
    return await _secureStorage.read(key: _keyToken);
  }

  Future<void> clearToken() async {
    await _secureStorage.delete(key: _keyToken);
  }

  // ========== 用户信息管理 ==========

  Future<void> saveUser(User user) async {
    await _prefs.setString(_keyUser, jsonEncode(user.toJson()));
  }

  User? getUser() {
    final json = _prefs.getString(_keyUser);
    if (json == null) return null;
    try {
      return User.fromJson(jsonDecode(json));
    } catch (e) {
      debugPrint('解析用户信息失败: $e');
      return null;
    }
  }

  Future<void> clearUser() async {
    await _prefs.remove(_keyUser);
  }

  // ========== 主题设置 ==========

  Future<void> saveTheme(String theme) async {
    await _prefs.setString(_keyTheme, theme);
  }

  String? getTheme() {
    return _prefs.getString(_keyTheme);
  }

  // ========== 清除所有数据 ==========

  Future<void> clearAll() async {
    await _prefs.clear();
    await _secureStorage.deleteAll();
  }
}

/// 在 main.dart 中初始化
void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 初始化存储服务
  await StorageService.instance.init();

  runApp(const MyApp());
}
```

## 四、UI 开发规范

### 4.1 主题配置

```dart
class AppTheme {
  static ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    colorScheme: ColorScheme.fromSeed(
      seedColor: Colors.blue,
      brightness: Brightness.light,
    ),
    appBarTheme: const AppBarTheme(
      centerTitle: true,
      elevation: 0,
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        minimumSize: const Size(double.infinity, 48),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    ),
  );
}
```

### 4.2 屏幕适配

```dart
// 初始化
ScreenUtil.init(
  context,
  designSize: const Size(375, 812),
);

// 使用
Container(
  width: 100.w,        // 宽度适配
  height: 50.h,        // 高度适配
  padding: EdgeInsets.all(16.w),
  child: Text(
    'Hello',
    style: TextStyle(fontSize: 14.sp),  // 字体适配
  ),
)
```

### 4.3 常用组件封装

#### 加载按钮

```dart
class LoadingButton extends StatelessWidget {
  const LoadingButton({
    super.key,
    required this.onPressed,
    required this.child,
    this.isLoading = false,
  });

  final VoidCallback? onPressed;
  final Widget child;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: isLoading ? null : onPressed,
      child: isLoading
          ? const SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(strokeWidth: 2),
            )
          : child,
    );
  }
}
```

#### 空状态组件

```dart
class EmptyWidget extends StatelessWidget {
  const EmptyWidget({
    super.key,
    this.message = '暂无数据',
    this.onRetry,
  });

  final String message;
  final VoidCallback? onRetry;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.inbox, size: 64, color: Colors.grey),
          SizedBox(height: 16),
          Text(message, style: TextStyle(color: Colors.grey)),
          if (onRetry != null) ...[
            SizedBox(height: 16),
            TextButton(onPressed: onRetry, child: Text('重试')),
          ],
        ],
      ),
    );
  }
}
```

## 五、性能优化

### 5.1 列表优化

- **使用 builder**：`ListView.builder` 而不是 `ListView`
- **const 构造**：尽可能使用 `const` 构造函数
- **避免重建**：不要在 `build` 方法中创建对象
- **隔离重绘**：使用 `RepaintBoundary` 隔离重绘区域
- **懒加载**：长列表使用分页加载
- **缓存高度**：固定高度的列表项性能更好

```dart
// Good - 使用 builder 和 const
ListView.builder(
  itemCount: items.length,
  itemBuilder: (context, index) {
    return const ListTile(
      title: Text('Item'),
    );
  },
)

// Bad - 直接创建所有子组件
ListView(
  children: items.map((item) => ListTile(title: Text(item))).toList(),
)
```

### 5.2 图片优化

- **网络图片缓存**：使用 `cached_network_image`
- **图片压缩**：上传前压缩图片（image_picker 配置）
- **格式选择**：优先使用 WebP 格式
- **占位图**：提供占位图和错误图
- **尺寸适配**：根据显示尺寸加载对应大小的图片

```dart
CachedNetworkImage(
  imageUrl: imageUrl,
  width: 100,
  height: 100,
  fit: BoxFit.cover,
  placeholder: (context, url) => const CircularProgressIndicator(),
  errorWidget: (context, url, error) => const Icon(Icons.error),
  memCacheWidth: 100, // 内存缓存宽度
  memCacheHeight: 100, // 内存缓存高度
)
```

### 5.3 状态管理优化

- **最小化 rebuild**：只监听需要的状态
- **select 监听**：使用 `select` 监听部分状态
- **避免依赖**：减少不必要的 Provider 依赖
- **计算缓存**：使用 `computed` 缓存计算结果

```dart
// Good - 只监听需要的字段
final userName = ref.watch(userProvider.select((user) => user.name));

// Bad - 监听整个对象
final user = ref.watch(userProvider);
```

### 5.4 内存优化

- **及时释放**：Controller、Stream 等及时 dispose
- **避免内存泄漏**：注意 Timer、Listener 的清理
- **图片缓存**：合理设置图片缓存大小
- **大对象**：避免在内存中保存大对象

### 5.5 启动优化

- **延迟初始化**：非必要的初始化延后执行
- **预加载**：首页数据可以预加载
- **闪屏优化**：使用原生闪屏页

## 六、测试规范

> 测试原则和最佳实践请参考 `~/.kiro/steering/testing-standards.md`

### 6.1 单元测试（Mockito）

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  group('UserRepository', () {
    late UserRepository repository;
    late MockApiClient mockApiClient;

    setUp(() {
      mockApiClient = MockApiClient();
      repository = UserRepository(mockApiClient);
    });

    test('getCurrentUser returns user when successful', () async {
      // Arrange
      when(() => mockApiClient.get('/api/v1/user/me'))
          .thenAnswer((_) async => {'id': 1, 'username': 'test', 'email': 'test@example.com'});

      // Act
      final user = await repository.getCurrentUser();

      // Assert
      expect(user.id, 1);
      expect(user.username, 'test');
      verify(() => mockApiClient.get('/api/v1/user/me')).called(1);
    });

    test('getCurrentUser throws exception when API fails', () async {
      // Arrange
      when(() => mockApiClient.get('/api/v1/user/me'))
          .thenThrow(ApiException('Network error'));

      // Act & Assert
      expect(
        () => repository.getCurrentUser(),
        throwsA(isA<ApiException>()),
      );
    });
  });
}
```

### 6.2 Widget 测试

```dart
void main() {
  testWidgets('LoginButton shows loading when pressed', (tester) async {
    bool pressed = false;

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: LoadingButton(
            onPressed: () => pressed = true,
            child: const Text('Login'),
          ),
        ),
      ),
    );

    // 验证初始状态
    expect(find.text('Login'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);

    // 点击按钮
    await tester.tap(find.text('Login'));
    await tester.pump();

    expect(pressed, true);
  });

  testWidgets('LoadingButton shows loading indicator', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: LoadingButton(
            isLoading: true,
            onPressed: () {},
            child: const Text('Login'),
          ),
        ),
      ),
    );

    // 验证加载状态
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    expect(find.text('Login'), findsNothing);
  });
}
```

### 6.3 集成测试

```dart
// integration_test/app_test.dart
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Complete login flow', (tester) async {
    // 启动应用
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle();

    // 输入用户名
    await tester.enterText(find.byKey(const Key('username')), 'test');
    await tester.enterText(find.byKey(const Key('password')), 'Test123456');

    // 点击登录
    await tester.tap(find.text('登录'));
    await tester.pumpAndSettle();

    // 验证跳转到首页
    expect(find.text('首页'), findsOneWidget);
  });
}
```

## 七、安全规范

### 7.1 数据安全

- **Token 存储**：使用 flutter_secure_storage 加密存储
- **敏感数据**：密码、支付信息等必须加密
- **HTTPS 通信**：所有 API 请求必须使用 HTTPS
- **证书校验**：生产环境启用 SSL 证书校验
- **防止截屏**：敏感页面禁止截屏（Android/iOS 原生实现）

### 7.2 代码混淆

```bash
# Android
flutter build apk --obfuscate --split-debug-info=build/app/outputs/symbols

# iOS
flutter build ios --obfuscate --split-debug-info=build/ios/symbols
```

### 7.3 权限管理

```dart
/// 权限请求工具类
class PermissionUtil {
  /// 请求相机权限
  static Future<bool> requestCamera() async {
    final status = await Permission.camera.request();
    return status.isGranted;
  }

  /// 请求相册权限
  static Future<bool> requestPhotos() async {
    final status = await Permission.photos.request();
    return status.isGranted;
  }

  /// 请求位置权限
  static Future<bool> requestLocation() async {
    final status = await Permission.location.request();
    return status.isGranted;
  }

  /// 检查权限并请求
  static Future<bool> checkAndRequest(Permission permission) async {
    final status = await permission.status;
    if (status.isGranted) {
      return true;
    }

    final result = await permission.request();
    if (result.isPermanentlyDenied) {
      // 引导用户去设置页面
      await openAppSettings();
      return false;
    }

    return result.isGranted;
  }
}
```

## 八、发布规范

### 8.1 版本管理

```yaml
# pubspec.yaml
version: 1.0.0+1 # version+buildNumber
```

### 8.2 打包命令

```bash
# Android
flutter build apk --release
flutter build appbundle --release

# iOS
flutter build ios --release
```

### 8.3 Android 签名配置

```properties
# android/key.properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=../keystore.jks
```

```gradle
// android/app/build.gradle
android {
    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
        }
    }
}
```

### 8.4 iOS 配置

```bash
# 配置 Bundle Identifier
# 在 Xcode 中设置 Signing & Capabilities

# 构建
flutter build ios --release
```

### 8.5 发布检查清单

- [ ] 更新版本号（pubspec.yaml）
- [ ] 测试所有功能（真机测试）
- [ ] 检查性能（Profile 模式）
- [ ] 更新 CHANGELOG.md
- [ ] 生成签名文件（Android）
- [ ] 配置证书（iOS）
- [ ] 准备应用截图和描述
- [ ] 检查权限声明
- [ ] 测试不同屏幕尺寸
- [ ] 上传应用商店
