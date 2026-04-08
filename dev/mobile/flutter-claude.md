---
inclusion: auto
---

# Flutter 移动端开发规范

> 详细规范参考 `references/` 目录，Claude Code 会自动扫描相关文件。

## 一、技术栈

| 类别 | 技术 | 版本 |
| ---- | ---- | ---- |
| 框架 | Flutter | 3.x+ |
| 语言 | Dart | 3.x+ |
| 状态管理 | Riverpod | 推荐 |
| 路由 | go_router | 推荐 |
| 网络 | dio | - |
| 本地存储 | shared_preferences / hive | - |

## 二、目录结构（Feature-First）

```
lib/
├── main.dart                   # 应用入口
├── app/                        # 应用配置
│   ├── app.dart               # App 根组件
│   ├── routes.dart            # 路由配置
│   └── theme.dart             # 主题配置
├── core/                       # 核心模块
│   ├── network/              # 网络层
│   ├── storage/              # 存储层
│   └── utils/                # 工具类
├── shared/                     # 共享模块
│   ├── widgets/              # 通用组件
│   └── constants/            # 共享常量
└── features/                   # 功能模块
    ├── auth/                 # 认证模块
    │   ├── data/
    │   ├── domain/
    │   └── presentation/
    └── home/                 # 首页模块
```

## 三、核心原则

### Feature-First（特性优先）

按业务功能垂直拆分。修改功能时，文件修改范围应集中在单一 feature 目录内。

### Clean Architecture 分层

- **Presentation Layer**: UI 和状态管理
- **Domain Layer**: 业务逻辑和实体
- **Data Layer**: 数据源和仓储实现

### 状态管理选择

| 场景 | 推荐方案 |
| ---- | -------- |
| 简单状态 | `StateProvider` |
| 异步数据 | `FutureProvider` / `StreamProvider` |
| 复杂逻辑 | `StateNotifierProvider` |

## 四、常用命令

```bash
# 获取依赖
flutter pub get

# 代码生成
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

## 五、references 导航

| 文档 | 内容 |
| ---- | ---- |
| 01-naming | 文件、类、变量、目录命名规范 |
| 02-widget | StatelessWidget、StatefulWidget 开发 |
| 03-state | Riverpod Provider、Repository、依赖注入 |
| 04-testing | 单元测试、Widget 测试、集成测试 |
| 05-deployment | Android/iOS 打包、签名配置、发布清单 |

## 六、网络请求封装

```dart
class ApiClient {
  late final Dio _dio;

  ApiClient() {
    _dio = Dio(BaseOptions(
      baseUrl: 'https://api.example.com',
      connectTimeout: const Duration(seconds: 10),
    ));
    _dio.interceptors.add(AuthInterceptor());
  }

  Future<T> get<T>(String path, {Map<String, dynamic>? params}) async {
    final response = await _dio.get(path, queryParameters: params);
    return _parseResponse<T>(response);
  }

  T _parseResponse<T>(Response response) {
    final data = response.data;
    if (data['code'] == 200) {
      return data['data'] as T;
    }
    throw ApiException(data['message']);
  }
}
```

## 七、主题配置

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
  );
}
```

## 八、本地存储

```dart
// 使用 shared_preferences
final prefs = await SharedPreferences.getInstance();
await prefs.setString('token', token);

// 使用 flutter_secure_storage（敏感数据）
final secureStorage = FlutterSecureStorage();
await secureStorage.write(key: 'token', value: token);
```

## 九、性能优化

- 使用 `ListView.builder` 代替 `ListView`
- 尽可能使用 `const` 构造函数
- 使用 `RepaintBoundary` 隔离重绘区域
- 图片使用 `cached_network_image` 缓存
