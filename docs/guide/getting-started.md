# Getting Started

Supported platforms: **Android** / **Desktop (JVM)** / **iOS** / **WasmJs** / **Js** / **macOS (Native)**

::: warning
This library is experimental, and APIs may change in future versions without notice.
:::

## Adding Dependencies

To use Miuix in your project, follow these steps to add dependencies:

### Gradle (Kotlin DSL)

1. Add the following to the root `settings.gradle.kts` file (usually already included):

```kotlin
repositories {
    mavenCentral()
}
```

2. Check the latest version on Maven Central:
   [![Maven Central](https://img.shields.io/maven-central/v/site.unclefish.yubeix/miuix)](https://search.maven.org/search?q=g:site.unclefish.yubeix)

3. Add dependencies to your project's `build.gradle.kts`:

- For Compose Multiplatform projects:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.unclefish.yubeix:miuix:<version>")
            // Optional: Add miuix-icons for more icons
            implementation("site.unclefish.yubeix:miuix-icons:<version>")
        }
    }
}
```

- For Android Compose projects:

```kotlin
dependencies {
    implementation("site.unclefish.yubeix:miuix-android:<version>")
    // Optional: Add miuix-icons for more icons
    implementation("site.unclefish.yubeix:miuix-icons-android:<version>")
}
```

- For other projects, add platform-specific dependencies as needed:

```kotlin
implementation("site.unclefish.yubeix:miuix-iosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-iossimulatorarm64:<version>")
implementation("site.unclefish.yubeix:miuix-macosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-desktop:<version>")
implementation("site.unclefish.yubeix:miuix-wasmjs:<version>")
implementation("site.unclefish.yubeix:miuix-js:<version>")
// Optional: Add miuix-icons
implementation("site.unclefish.yubeix:miuix-icons-iosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-icons-iossimulatorarm64:<version>")
implementation("site.unclefish.yubeix:miuix-icons-macosarm64:<version>")
implementation("site.unclefish.yubeix:miuix-icons-desktop:<version>")
implementation("site.unclefish.yubeix:miuix-icons-wasmjs:<version>")
implementation("site.unclefish.yubeix:miuix-icons-js:<version>")
```

## Basic Usage

### Applying the Miuix Theme

```kotlin
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // Available modes: System, Light, Dark, MonetSystem, MonetLight, MonetDark
    val controller = remember { ThemeController(ColorSchemeMode.System) }
    return MiuixTheme(
        controller = controller,
        content = content
    )
}
```

### Using the Miuix Scaffold

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

::: warning
The Scaffold component provides a suitable container for cross-platform popup windows.
Components such as `SuperDialog`, `SuperDropdown`, `SuperSpinner`, and `ListPopup` are
all implemented based on this and therefore need to be wrapped by this component.
:::

## API Documentation

- View the [API Documentation](/miuix/dokka/index.html){target="_blank"},
  generated using Dokka, which contains detailed information about all APIs.
