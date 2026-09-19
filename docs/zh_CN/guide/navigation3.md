# 导航支持

`miuix-navigation3-ui` 提供了 `androidx.navigation3` 的 UI 实现，适配了 Miuix 的设计风格和过渡动画。

## 配置

在 `build.gradle.kts` 中添加依赖：

```kotlin
implementation("androidx.navigation3:navigation3-runtime:<navigation3-version>")
implementation("site.unclefish.yubeix:miuix-navigation3-ui:<version>")
```

::: warning
本库仅包含 UI 实现。您必须自行引入 `androidx-navigation3-runtime` 依赖。
:::

## 使用

使用 `NavDisplay` 来渲染你的导航场景。定义实现 `NavKey` 接口的屏幕类。

```kotlin
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.ui.NavDisplay

sealed interface Screen : NavKey {
    data object Home : Screen
    data class Detail(val id: String) : Screen
}

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<NavKey>(Screen.Home) }

    val entryProvider = remember(backStack) {
        entryProvider<NavKey> {
            entry(Screen.Home) {
                HomePage()
            }
            entry<Screen.Detail> { screen ->
                DetailPage(screen.id)
            }
        }
    }

    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryProvider = entryProvider
    )

    NavDisplay(
        entries = entries,
        onBack = { backStack.removeLast() }
    )
}
```

## SceneDisplay

如果不基于 `androidx.navigation3`，可以直接使用 `SceneDisplay` 渲染 `NavigationPath` 中的所有场景：
场景以层叠方式呈现，并在它们之间驱动 iOS 风格的推入/弹出转场，同时为每个场景提供独立的
`ViewModelStore` 与可保存状态。

```kotlin
val navigationPath = rememberSaveable(saver = NavigationPath.saver()) { NavigationPath(Route.Main) }

SceneDisplay(
    navigationPath = navigationPath,
    entryProvider = entryProvider {
        entry<Route.Main> { MainPage() }
        entry<Route.Detail> { route -> DetailPage(route) }
    },
    predictiveBackEnabled = true,
    sharedTopBarEnabled = true,
)
```

| 参数名                 | 类型                                         | 默认值       | 说明                                                                                     |
| ---------------------- | -------------------------------------------- | ------------ | ---------------------------------------------------------------------------------------- |
| navigationPath         | NavigationPath\<T>                           | -            | 保存 `NavKey` 路由的返回栈                                                                |
| entryProvider          | (T) -> NavEntry\<T>                          | -            | 将路由映射为页面内容                                                                      |
| modifier               | Modifier                                     | Modifier     | 应用到宿主容器                                                                             |
| predictiveBackEnabled  | Boolean                                      | false        | 将系统返回手势接入前台场景的实时拖动（在平台上报进度的设备上）                             |
| sharedTransitionEnabled| (from: T?, to: T?) -> Boolean                | { false }    | 为指定路由组合启用共享元素动效                                                             |
| sharedTransitionElastic| (from: T?, to: T?) -> Boolean                | { false }    | 其中保留弹性落位的共享元素组合                                                             |
| sharedTopBarEnabled    | Boolean                                      | false        | 转场期间将成对场景的标题栏提取到窗口顶部统一绘制（iOS 导航栏行为），而不是随场景滑动。要求场景使用 `ScreenScaffold` 标题栏；没有标题栏的场景不受影响。任一方仍在显示大标题（`ScreenTitleMode.Hero` 且折叠标题栏未出现）时不共享，双方标题栏照常随页面滑动 |
| customSceneTransform   | (scene, followingScenes, zIndex) -> Modifier? | null         | 用自定义变换替换标准的页面滑动（例如水平引导页翻页）                                       |
| sceneCornerClipEnabled | Boolean                                      | true         | 推入/弹出转场期间为顶层场景应用圆角裁剪；当界面旁驻留其他元素（如常驻侧栏）时应关闭        |
