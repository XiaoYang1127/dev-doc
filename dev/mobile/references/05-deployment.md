# 打包部署规范

## 版本管理

```yaml
# pubspec.yaml
version: 1.0.0+1  # version+buildNumber
```

## Android 打包

```bash
# APK
flutter build apk --release

# AppBundle（Google Play 推荐）
flutter build appbundle --release

# 混淆
flutter build apk --obfuscate --split-debug-info=build/app/outputs/symbols
```

## iOS 打包

```bash
# Release Build
flutter build ios --release

# 模拟器构建
flutter build ios --simulator --no-codesign
```

## Android 签名配置

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
        }
    }
}
```

## 发布检查清单

- [ ] 更新版本号（pubspec.yaml）
- [ ] 真机测试所有功能
- [ ] 检查性能（Profile 模式）
- [ ] 生成签名文件（Android）
- [ ] 配置证书（iOS）
- [ ] 检查权限声明
- [ ] 测试不同屏幕尺寸

## 权限配置

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA"/>
```

```xml
<!-- Info.plist (iOS) -->
<key>NSCameraUsageDescription</key>
<string>需要相机权限</string>
```
