# Navigation3 Support

`miuix-navigation3-ui` provides UI implementation for `androidx.navigation3`, adapting to Miuix design style and transitions.

## Setup

Add the dependency to your `build.gradle.kts`:

```kotlin
implementation("androidx.navigation3:navigation3-runtime:<navigation3-version>")
implementation("site.unclefish.yubeix:miuix-navigation3-ui:<version>")
```

::: warning
This library only contains the UI implementation. You must also include the `androidx-navigation3-runtime` dependency yourself.
:::

## Usage

Use `NavDisplay` to render your navigation scenes. Define your screens implementing `NavKey`.

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

For hosts that do not build on `androidx.navigation3`, `SceneDisplay` renders every live scene of a
`NavigationPath` as stacked layers and drives the iOS-style push/pop motion between them, with
per-scene `ViewModelStore` and saveable state.

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

| Parameter Name          | Type                                        | Default Value | Description                                                                                                                                     |
| ----------------------- | ------------------------------------------- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| navigationPath          | NavigationPath\<T>                          | -             | Holds the back stack of `NavKey` routes                                                                                                         |
| entryProvider           | (T) -> NavEntry\<T>                         | -             | Maps a route to its content                                                                                                                     |
| modifier                | Modifier                                    | Modifier      | Applied to the host container                                                                                                                   |
| predictiveBackEnabled   | Boolean                                     | false         | Wires the system back gesture to a live drag of the front scene where the platform reports progress                                             |
| sharedTransitionEnabled | (from: T?, to: T?) -> Boolean               | { false }     | Opts route pairs into shared-element motion                                                                                                     |
| sharedTransitionElastic | (from: T?, to: T?) -> Boolean               | { false }     | The subset of shared pairs whose landing keeps the elastic settle                                                                               |
| sharedTopBarEnabled     | Boolean                                     | false         | Draws the pair's chrome bars once, fixed at the top of the window, during a transition (the iOS navigation-bar behavior) instead of sliding with their scenes. Requires each scene to use the `ScreenScaffold` chrome bar; scenes without one are unaffected. A pair with either scene still showing its hero (large) title (`ScreenTitleMode.Hero` before the bar collapses) does not share - both bars keep sliding with their scenes |
| customSceneTransform    | (scene, followingScenes, zIndex) -> Modifier? | null          | Substitutes the standard page slide with a custom transform on selected transitions                                                             |
| sceneCornerClipEnabled  | Boolean                                     | true          | Rounds the top scene's corners during push/pop transitions. Turn off when chrome (e.g. a resident sidebar) sits beside the scenes                |
