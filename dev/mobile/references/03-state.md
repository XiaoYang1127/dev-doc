# 状态管理规范（Flutter 官方推荐架构）

## Flutter 官方架构分层

Flutter 官方推荐三层架构：

| 层级 | 职责 | 官方推荐实现 |
|------|------|-------------|
| **UI Layer** | Widget 展示、用户交互 | StatefulWidget / StatelessWidget |
| **Business Logic** | 状态管理、业务规则 | ChangeNotifier (ViewModel) |
| **Data Layer** | 数据获取、持久化 | Repository + DataSource |

## ViewModel (ChangeNotifier) - 官方推荐

ViewModel 使用 `ChangeNotifier` 管理状态，分离 UI 与业务逻辑：

```dart
class ArticleViewModel extends ChangeNotifier {
  ArticleModel model;
  Summary? summary;
  String? errorMessage;
  bool loading = false;

  ArticleViewModel(this.model);

  Future<void> getRandomArticleSummary() async {
    loading = true;
    notifyListeners(); // 通知 UI 更新

    try {
      summary = await model.getRandomArticleSummary();
      errorMessage = null;
    } on HttpException catch (e) {
      errorMessage = e.message;
      summary = null;
    } finally {
      loading = false;
      notifyListeners();
    }
  }
}
```

### ViewModel 使用

```dart
class ArticlePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => ArticleViewModel(ArticleModel()),
      child: const ArticleView(),
    );
  }
}

class ArticleView extends StatelessWidget {
  const ArticleView({super.key});

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<ArticleViewModel>();

    if (viewModel.loading) {
      return const CircularProgressIndicator();
    }

    if (viewModel.errorMessage != null) {
      return Text('Error: ${viewModel.errorMessage}');
    }

    return Text(viewModel.summary?.body ?? '');
  }
}
```

## Data Source 分层

### Remote DataSource - 原始数据获取

```dart
class ArticleRemoteDataSource {
  final http.Client _client;

  ArticleRemoteDataSource(this._client);

  Future<ArticleModel> getArticle(String id) async {
    final response = await _client.get(
      Uri.parse('https://api.example.com/articles/$id'),
    );

    if (response.statusCode == 200) {
      return ArticleModel.fromJson(jsonDecode(response.body));
    }
    throw HttpException('Failed to load article: ${response.statusCode}');
  }
}
```

### Repository - 聚合与缓存

```dart
class ArticleRepository {
  final ArticleRemoteDataSource _remoteDataSource;
  ArticleModel? _cache;

  ArticleRepository(this._remoteDataSource);

  Future<ArticleModel> getArticle(String id) async {
    // 可在此添加缓存逻辑
    if (_cache != null) {
      return _cache!;
    }

    final article = await _remoteDataSource.getArticle(id);
    _cache = article;
    return article;
  }
}
```

## Provider 类型选择（官方推荐）

| 场景 | 推荐方案 |
|------|----------|
| 简单状态 | `StateProvider` |
| 单次异步数据 | `FutureProvider` |
| 实时数据流 | `StreamProvider` |
| 复杂业务逻辑 | `ChangeNotifierProvider` (ViewModel) |

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
