# Yubeix

A Compose Multiplatform UI library, forked from [Miuix](https://github.com/compose-miuix-ui/miuix) v0.8.8 and heavily customized ("鱼背化") for the Yubeix design language — iOS-flavored controls, collapsing large-title chrome, scene-based navigation, and blur-backed surfaces.

> This library is experimental. APIs may change without notice.

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.20-7F52FF)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/compose-1.10.3-4285F4)](https://www.jetbrains.com/compose-multiplatform)
[![License](https://img.shields.io/github/license/yushu789/yubeix)](LICENSE)

## Attribution

Yubeix is a fork of [miuix](https://github.com/compose-miuix-ui/miuix) (Apache-2.0, © compose-miuix-ui contributors).
Portions of the Cupertino-style controls are adapted from [RobinPcrd/compose-cupertino](https://github.com/RobinPcrd/compose-cupertino) (Apache-2.0).
Portions of the overscroll effect are adapted from the Android Open Source Project.
See [LICENSE](LICENSE) for the full license text.

## Supported Platforms

![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-Native-white?logo=apple)
![macOS](https://img.shields.io/badge/macOS-Native-white?logo=apple)
![Desktop](https://img.shields.io/badge/Desktop-JVM-007396?logo=openjdk)
![JsCanvas](https://img.shields.io/badge/Web-JsCanvas-F7DF1E?logo=javascript&logoColor=white)
![WasmJs](https://img.shields.io/badge/Web-WasmJs-654FF0?logo=webassembly&logoColor=white)

## Getting Started

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.unclefish.yubeix:yubeix:<version>")
            // Optional: more icons
            implementation("site.unclefish.yubeix:yubeix-icons:<version>")
        }
    }
}
```

### Usage

- Provide a color scheme via `YubeixTheme(colors = ...)`, e.g., `lightColorScheme()` or `darkColorScheme()`.

```kotlin
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    return YubeixTheme(
        colors = colors,
        content = content
    )
}
```

- Use `ThemeController` to manage modes and enable Monet dynamic colors. Pass `keyColor` to set a custom seed color.

```kotlin
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val controller = remember {
        ThemeController(
            ColorSchemeMode.MonetSystem,
            keyColor = Color(0xFF3482FF)
        )
    }
    return YubeixTheme(
        controller = controller,
        content = content
    )
}
```

- Declare scenes and navigate with the built-in scene navigation:

```kotlin
val navigationPath = rememberNavigationPath(initialRoute = Route.Main)
SceneDisplay(
    navigationPath = navigationPath,
    entryProvider = entryProvider {
        entry<Route.Main> { MainPage(onNavigate = navigationPath::push) }
        entry<Route.Detail> { route -> DetailPage(route) }
    }
)
```
