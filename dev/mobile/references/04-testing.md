# 测试规范

## 单元测试（Mockito）

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
      when(() => mockApiClient.get('/api/v1/user/me'))
          .thenAnswer((_) async => {'id': 1, 'username': 'test'});

      final user = await repository.getCurrentUser();

      expect(user.id, 1);
      expect(user.username, 'test');
    });
  });
}
```

## Widget 测试

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

    await tester.tap(find.text('Login'));
    await tester.pump();

    expect(pressed, true);
  });
}
```

## 集成测试

```dart
import 'package:integration_test/integration_test.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Complete login flow', (tester) async {
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('username')), 'test');
    await tester.enterText(find.byKey(const Key('password')), 'Test123456');
    await tester.tap(find.text('登录'));
    await tester.pumpAndSettle();

    expect(find.text('首页'), findsOneWidget);
  });
}
```

## 测试命令

```bash
# 单元测试
flutter test

# 集成测试
flutter test integration_test/app_test.dart

# 生成 mock
flutter pub run build_runner build --delete-conflicting-outputs
```
