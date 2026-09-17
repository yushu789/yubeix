# 快速开始

当前支持的平台: **Android** / **Desktop(JVM)** / **iOS** / **WasmJs** / **Js** / **macOS(Native)**

::: warning 注意
此库处于实验阶段，API 可能会在未来版本中变更而不另行通知
:::

## 添加依赖

要在您的项目中使用 Miuix，请按照以下步骤添加依赖：

### Gradle (Kotlin DSL)

1. 在根目录的 settings.gradle.kts 添加（正常情况应已包含）：

```kotlin
repositories {
    mavenCentral()
}
```

2. 检查 Maven Central 当前最新版本：
   [![Maven Central](https://img.shields.io/maven-central/v/site.unclefish.yubeix/miuix)](https://search.maven.org/search?q=g:site.unclefish.yubeix)

3. 在项目的 build.gradle.kts 中添加依赖：

- 在 Compose Multiplatform 项目目录的 build.gradle.kts 中：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.unclefish.yubeix:miuix:<version>")
            // 可选：添加 miuix-icons 以获取更多图标
            implementation("site.unclefish.yubeix:miuix-icons:<version>")
        }
    }
}

```

- 在 Android Compose 项目目录的 build.gradle.kts 中：

```kotlin
dependencies {
    implementation("site.unclefish.yubeix:miuix-android:<version>")
    // 可选：添加 miuix-icons 以获取更多图标
    implementation("site.unclefish.yubeix:miuix-icons-android:<version>")
}
```

- 在其他常规项目中使用，则只需要根据需要添加对应平台后缀的依赖即可：

```kotlin
implementation("site.unclefish.yubeix:miuix-iosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-iossimulatorarm64:<version>")
implementation("site.unclefish.yubeix:miuix-macosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-desktop:<version>")
implementation("site.unclefish.yubeix:miuix-wasmjs:<version>")
implementation("site.unclefish.yubeix:miuix-js:<version>")
// 可选：添加 miuix-icons
implementation("site.unclefish.yubeix:miuix-icons-iosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-icons-iossimulatorarm64:<version>")
implementation("site.unclefish.yubeix:miuix-icons-macosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-icons-desktop:<version>")
implementation("site.unclefish.yubeix:miuix-icons-wasmjs:<version>")
implementation("site.unclefish.yubeix:miuix-icons-js:<version>")
```

## 基本用法

### 应用 Miuix 主题

```kotlin
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // 可用模式: System, Light, Dark, MonetSystem, MonetLight, MonetDark
    val controller = remember { ThemeController(ColorSchemeMode.System) }
    return MiuixTheme(
        controller = controller,
        content = content
    )
}
```

### 使用 Miuix 脚手架

```kotlin
Scaffold(
    topBar = {
        // TopBar
    },
    bottomBar = {
        // BottomBar
    },
    floatingActionButton = {
        // FloatingActionButton
    },
    floatingToolbar = {
        // FloatingToolbar
    }
) {
    // Content...
}
```

::: warning 注意
Scaffold 组件为跨平台提供了一个合适的弹出窗口的容器。`SuperDialog`、`SuperDropdown`、`SuperSpinner`、
`ListPopup` 等组件都基于此实现弹出窗口，因此都需要被该组件包裹。
:::

## API 文档

- 查看 [API 文档](/miuix/dokka/index.html){target="_blank"}，此文档使用 Dokka 生成，包含了所有 API
  的详细信息。
