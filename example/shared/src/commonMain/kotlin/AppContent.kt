// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBarPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import appnavigation.Navigator
import appnavigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import site.unclefish.yubeix.basic.FabPosition
import site.unclefish.yubeix.basic.FloatingActionButton
import site.unclefish.yubeix.basic.FloatingNavigationBar
import site.unclefish.yubeix.basic.FloatingNavigationBarDisplayMode
import site.unclefish.yubeix.basic.FloatingNavigationBarItem
import site.unclefish.yubeix.basic.FloatingToolbar
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.IconButton
import site.unclefish.yubeix.basic.NavigationBar
import site.unclefish.yubeix.basic.NavigationBarDisplayMode
import site.unclefish.yubeix.basic.NavigationBarItem
import site.unclefish.yubeix.basic.NavigationItem
import site.unclefish.yubeix.basic.NavigationRail
import site.unclefish.yubeix.basic.NavigationRailDisplayMode
import site.unclefish.yubeix.basic.NavigationRailItem
import site.unclefish.yubeix.basic.Scaffold
import site.unclefish.yubeix.basic.SnackbarHost
import site.unclefish.yubeix.basic.SnackbarHostState
import site.unclefish.yubeix.basic.ToolbarPosition
import site.unclefish.yubeix.component.NavigationPageTransition
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Create
import site.unclefish.yubeix.icon.extended.Delete
import site.unclefish.yubeix.icon.extended.Edit
import site.unclefish.yubeix.icon.extended.HorizontalSplit
import site.unclefish.yubeix.icon.extended.Image
import site.unclefish.yubeix.icon.extended.Link
import site.unclefish.yubeix.icon.extended.More
import site.unclefish.yubeix.icon.extended.Settings
import site.unclefish.yubeix.icon.extended.Sort
import site.unclefish.yubeix.navigation.NavigationPath
import site.unclefish.yubeix.navigation.SceneDisplay
import site.unclefish.yubeix.navigation.entryProvider
import site.unclefish.yubeix.theme.YubeixTheme
import utils.FPSMonitor
import utils.shouldShowSplitPane

private object UIConstants {
    const val MAIN_PAGE_INDEX = 0
    const val ICON_PAGE_INDEX = 1
    const val COLOR_PAGE_INDEX = 2
    const val DROPDOWN_PAGE_INDEX = 3
    const val PAGE_COUNT = 5
    const val GITHUB_URL = "https://github.com/compose-miuix-ui/miuix"

    val PAGE_TITLES = listOf("Home", "Icon", "Color", "Dropdown", "Settings")
}

enum class FloatingNavigationBarAlignment(val value: Int) {
    Center(0),
    Start(1),
    End(2),
    ;

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: Center
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator found!") }
val LocalIsWideScreen = staticCompositionLocalOf { false }
val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@Composable
fun AppContent(
    padding: PaddingValues,
) {
    val appState = LocalAppState.current

    val mainPagerState = rememberMainPagerState()
    val navigationPath = remember { NavigationPath<Route>(Route.Main) }
    val navigator = remember { Navigator(navigationPath) }

    val navigationItems = remember {
        listOf(
            NavigationItem(UIConstants.PAGE_TITLES[0], YubeixIcons.HorizontalSplit),
            NavigationItem(UIConstants.PAGE_TITLES[1], YubeixIcons.Create),
            NavigationItem(UIConstants.PAGE_TITLES[2], YubeixIcons.Image),
            NavigationItem(UIConstants.PAGE_TITLES[3], YubeixIcons.Sort),
            NavigationItem(UIConstants.PAGE_TITLES[4], YubeixIcons.Settings),
        )
    }

    MainScreenBackHandler(mainPagerState, navigator)

    val isWideScreen = shouldShowSplitPane()

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalMainPagerState provides mainPagerState,
        LocalIsWideScreen provides isWideScreen,
    ) {
        val entryProvider = remember {
            entryProvider<Route> {
                entry<Route.Main> {
                    Home(
                        padding = padding,
                        navigationItems = navigationItems,
                        mainPagerState = mainPagerState,
                    )
                }
                entry<Route.About> {
                    AboutPage(padding = padding)
                }
                entry<Route.License> {
                    LicensePage(padding = padding)
                }
                entry<Route.NavTest> { route ->
                    val index = navigationPath.activeRoutes
                        .filterIsInstance<Route.NavTest>()
                        .indexOf(route) + 1
                    NavTestPage(
                        index = index,
                        padding = padding,
                    )
                }
                entry<Route.MultiScaffoldTest> {
                    MultiScaffoldTestPage(padding = padding)
                }
            }
        }

        SceneDisplay(
            navigationPath = navigationPath,
            entryProvider = entryProvider,
            predictiveBackEnabled = true,
        )
    }

    AnimatedVisibility(
        visible = appState.showFPSMonitor,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        FPSMonitor(
            modifier = Modifier
                .statusBarsPadding()
                .captionBarPadding()
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun Home(
    padding: PaddingValues,
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
) {
    val isWideScreen = LocalIsWideScreen.current
    val layoutDirection = LocalLayoutDirection.current
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            if (isWideScreen) {
                SnackbarHost(state = snackbarHostState)
            }
        },
    ) {
        if (isWideScreen) {
            WideScreenContent(
                navigationItems = navigationItems,
                snackbarHostState = snackbarHostState,
                layoutDirection = layoutDirection,
                mainPagerState = mainPagerState,
            )
        } else {
            CompactScreenLayout(
                navigationItems = navigationItems,
                snackbarHostState = snackbarHostState,
                padding = padding,
                mainPagerState = mainPagerState,
            )
        }
    }
}

@Composable
private fun WideScreenContent(
    navigationItems: List<NavigationItem>,
    snackbarHostState: SnackbarHostState,
    layoutDirection: LayoutDirection,
    mainPagerState: MainPagerState,
) {
    val appState = LocalAppState.current
    val page = mainPagerState.selectedPage
    Row {
        if (appState.showNavigationBar) {
            NavigationRail(
                modifier = Modifier.background(YubeixTheme.colorScheme.surface),
                mode = NavigationRailDisplayMode.entries[appState.navigationRailMode],
            ) {
                navigationItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        selected = page == index,
                        onClick = { mainPagerState.animateToPage(index) },
                        icon = item.icon,
                        label = item.label,
                    )
                }
            }
        }
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            contentWindowInsets =
            WindowInsets.systemBars.union(
                WindowInsets.displayCutout.exclude(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Start),
                ),
            ),
            floatingActionButton = {
                FloatingActionButton(show = appState.showFloatingActionButton)
            },
            floatingActionButtonPosition = appState.floatingActionButtonPosition.toFabPosition(),
            floatingToolbar = {
                FloatingToolbar(
                    showFloatingToolbar = appState.showFloatingToolbar,
                    floatingToolbarOrientation = appState.floatingToolbarOrientation,
                )
            },
            floatingToolbarPosition = appState.floatingToolbarPosition.toToolbarPosition(),
        ) { padding ->
            AppPager(
                snackbarHostState = snackbarHostState,
                padding = PaddingValues(top = padding.calculateTopPadding()),
                selectedPage = mainPagerState.selectedPage,
                modifier = Modifier
                    .imePadding()
                    .padding(end = padding.calculateEndPadding(layoutDirection)),
            )
        }
    }
}

@Composable
private fun CompactScreenLayout(
    navigationItems: List<NavigationItem>,
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
    mainPagerState: MainPagerState,
) {
    val appState = LocalAppState.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                navigationItems = navigationItems,
                mainPagerState = mainPagerState,
                modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            )
        },
        floatingActionButton = {
            FloatingActionButton(show = appState.showFloatingActionButton)
        },
        floatingActionButtonPosition = appState.floatingActionButtonPosition.toFabPosition(),
        floatingToolbar = {
            FloatingToolbar(
                showFloatingToolbar = appState.showFloatingToolbar,
                floatingToolbarOrientation = appState.floatingToolbarOrientation,
            )
        },
        floatingToolbarPosition = appState.floatingToolbarPosition.toToolbarPosition(),
        snackbarHost = {
            SnackbarHost(state = snackbarHostState)
        },
    ) { innerPadding ->
        AppPager(
            snackbarHostState = snackbarHostState,
            padding = innerPadding,
            selectedPage = mainPagerState.selectedPage,
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .imePadding(),
        )
    }
}

@Composable
private fun NavigationBar(
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
    modifier: Modifier = Modifier,
) {
    val appState = LocalAppState.current
    val page = mainPagerState.selectedPage
    AnimatedVisibility(
        visible = appState.showNavigationBar,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        AnimatedVisibility(
            visible = !appState.useFloatingNavigationBar,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Box(
                modifier = Modifier
                    .background(YubeixTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { change ->
                                    if (change.pressed) change.consume()
                                }
                            }
                        }
                    }
                    .then(modifier),
            ) {
                NavigationBar(
                    modifier = Modifier,
                    mode = NavigationBarDisplayMode.entries[appState.navigationBarMode],
                ) {
                    navigationItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = page == index,
                            onClick = { mainPagerState.animateToPage(index) },
                            icon = item.icon,
                            label = item.label,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = appState.useFloatingNavigationBar,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Box(
                modifier = modifier,
            ) {
                FloatingNavigationBar(
                    mode = FloatingNavigationBarDisplayMode.entries[appState.floatingNavigationBarMode],
                    horizontalAlignment = FloatingNavigationBarAlignment.fromInt(appState.floatingNavigationBarPosition)
                        .toAlignment(),
                ) {
                    navigationItems.forEachIndexed { index, item ->
                        FloatingNavigationBarItem(
                            selected = page == index,
                            onClick = { mainPagerState.animateToPage(index) },
                            icon = item.icon,
                            label = item.label,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingActionButton(
    show: Boolean,
) {
    if (show) {
        val uriHandler = LocalUriHandler.current
        FloatingActionButton(
            onClick = {
                uriHandler.openUri(UIConstants.GITHUB_URL)
            },
        ) {
            Icon(
                imageVector = YubeixIcons.Link,
                tint = YubeixTheme.colorScheme.onPrimary,
                contentDescription = "GitHub",
            )
        }
    }
}

@Composable
private fun FloatingToolbar(
    showFloatingToolbar: Boolean,
    floatingToolbarOrientation: Int,
) {
    AnimatedVisibility(
        visible = showFloatingToolbar,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        FloatingToolbar(
            color = YubeixTheme.colorScheme.primary,
            cornerRadius = 20.dp,
        ) {
            AnimatedContent(
                targetState = floatingToolbarOrientation,
            ) { orientation ->
                val iconTint = YubeixTheme.colorScheme.onPrimary
                val content = @Composable {
                    IconButton(onClick = { /* Action 1 */ }) {
                        Icon(
                            YubeixIcons.Edit,
                            contentDescription = "Edit",
                            tint = iconTint,
                        )
                    }
                    IconButton(onClick = { /* Action 2 */ }) {
                        Icon(
                            YubeixIcons.Delete,
                            contentDescription = "Delete",
                            tint = iconTint,
                        )
                    }
                    IconButton(onClick = { /* Action 3 */ }) {
                        Icon(
                            YubeixIcons.More,
                            contentDescription = "More",
                            tint = iconTint,
                        )
                    }
                }
                when (orientation) {
                    0 -> Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) { content() }

                    else -> Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) { content() }
                }
            }
        }
    }
}

private fun Int.toFabPosition(): FabPosition = when (this) {
    0 -> FabPosition.Start
    1 -> FabPosition.Center
    2 -> FabPosition.End
    else -> FabPosition.EndOverlay
}

private fun Int.toToolbarPosition(): ToolbarPosition = when (this) {
    0 -> ToolbarPosition.TopStart
    1 -> ToolbarPosition.CenterStart
    2 -> ToolbarPosition.BottomStart
    3 -> ToolbarPosition.TopEnd
    4 -> ToolbarPosition.CenterEnd
    5 -> ToolbarPosition.BottomEnd
    6 -> ToolbarPosition.TopCenter
    else -> ToolbarPosition.BottomCenter
}

private fun FloatingNavigationBarAlignment.toAlignment(): Alignment.Horizontal = when (this) {
    FloatingNavigationBarAlignment.Center -> CenterHorizontally
    FloatingNavigationBarAlignment.Start -> Alignment.Start
    FloatingNavigationBarAlignment.End -> Alignment.End
}

@Composable
fun AppPager(
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
    selectedPage: Int,
    modifier: Modifier = Modifier,
) {
    val appState = LocalAppState.current
    NavigationPageTransition(
        targetState = selectedPage,
        modifier = modifier,
    ) { page ->
        when (page) {
            UIConstants.MAIN_PAGE_INDEX -> MainPage(
                snackbarHostState = snackbarHostState,
                padding = padding,
            )

            UIConstants.ICON_PAGE_INDEX -> IconsPage(padding = padding)

            UIConstants.COLOR_PAGE_INDEX -> ColorPage(padding = padding)

            UIConstants.DROPDOWN_PAGE_INDEX -> DropdownPage(padding = padding)

            else -> SettingsPage(padding = padding)
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navigator: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navigator.current() is Route.Main && navigator.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        },
    )
}

@Stable
class MainPagerState(
    initialPage: Int,
) {
    var selectedPage by mutableIntStateOf(initialPage)
        private set

    /**
     * Tab switches run through [NavigationPageTransition]'s scale-and-fade hand-off, so
     * "animating" to a page is just publishing the new target.
     */
    fun animateToPage(targetIndex: Int) {
        selectedPage = targetIndex
    }
}

@Composable
fun rememberMainPagerState(): MainPagerState = remember { MainPagerState(initialPage = 0) }
