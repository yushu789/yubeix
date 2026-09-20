// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import appnavigation.Navigator
import appnavigation.Route
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
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
import site.unclefish.yubeix.basic.LocalSceneLeadingInset
import site.unclefish.yubeix.basic.NavigationBar
import site.unclefish.yubeix.basic.NavigationBarDisplayMode
import site.unclefish.yubeix.basic.NavigationBarItem
import site.unclefish.yubeix.basic.NavigationItem
import site.unclefish.yubeix.basic.NavigationRail
import site.unclefish.yubeix.basic.NavigationRailDefaults
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
import site.unclefish.yubeix.navigation.NavEntry
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

/** The slot the pinned sidebar toggle reserves inside the sidebar header; matches the 40dp [IconButton]. */
private val SidebarToggleSlotSize = 40.dp

// Marker string only: lets a build be proven fresh by grepping the APK's dex, since
// Gradle's incremental compile can otherwise hand back a stale artifact.
private const val SIDEBAR_REVEAL_MARKER = "railTwoModes"

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
            }
        }

        if (isWideScreen) {
            // iPadOS-style wide layout: the sidebar lives OUTSIDE the scene navigation, so pushed
            // secondary pages only cover the area to its right while the sidebar stays resident.
            WideScreenAppLayout(
                navigationItems = navigationItems,
                mainPagerState = mainPagerState,
                navigator = navigator,
                railMode = appState.navigationRailMode,
                showSidebar = appState.showNavigationBar,
                followResize = appState.sidebarFollowResize,
                sharedTopBarEnabled = appState.sharedTopBarEnabled,
                navigationPath = navigationPath,
                entryProvider = entryProvider,
            )
        } else {
            SceneDisplay(
                navigationPath = navigationPath,
                entryProvider = entryProvider,
                predictiveBackEnabled = true,
                sharedTopBarEnabled = appState.sharedTopBarEnabled,
            )
        }
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
    snackbarHostState: SnackbarHostState,
    layoutDirection: LayoutDirection,
    mainPagerState: MainPagerState,
) {
    val appState = LocalAppState.current
    // The persistent sidebar lives at the AppContent level (outside the scene navigation), so
    // this is just the Route.Main content pane: scaffold insets, FAB/toolbar and the pager.
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
        snackbarHost = {
            SnackbarHost(state = snackbarHostState)
        },
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

@Composable
private fun WideScreenAppLayout(
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
    navigator: Navigator,
    railMode: Int,
    showSidebar: Boolean,
    followResize: Boolean,
    sharedTopBarEnabled: Boolean,
    navigationPath: NavigationPath<Route>,
    entryProvider: (Route) -> NavEntry<Route>,
) {
    val sidebarWidth = NavigationRailDefaults.MinWidth

    // Toggle masking, same hand-off as the tab-switch transition (NavigationPageTransition)
    // but compressed (50ms out + 100ms in instead of the tab hand-off's 82+138): the pane
    // only changes width here, not content, so the mask stays tighter than a page switch.
    // The rail starts sliding immediately (it is render-side only and lives in its own
    // lane) while the pane shrinks to 0.97x and fades out; the slot snaps at zero alpha so
    // the pane's one reflow happens invisibly, then the relaid-out pane scales up from
    // 0.98x and fades in while the rail finishes its glide — every animated frame is
    // render-side only, so none of them can drop. (The marker const exists because
    // Gradle's incremental compile can hand back a stale APK.)
    val sidebarReveal = remember { Animatable(if (showSidebar) 1f else 0f) }
    var slotWidth by remember { mutableStateOf(if (showSidebar) sidebarWidth else 0.dp) }
    val paneAlpha = remember { Animatable(1f) }
    val paneScale = remember { Animatable(1f) }

    LaunchedEffect(showSidebar, followResize) {
        val target = if (showSidebar) 1f else 0f
        if (sidebarReveal.targetValue == target) return@LaunchedEffect

        if (followResize) {
            // Old direct mode: the pane's width tracks the rail's edge frame by frame —
            // the layout genuinely resizes every frame, no crossfade mask. One Animatable
            // drives both the rail's slide and the slot's width so their edges stay glued.
            paneAlpha.snapTo(1f)
            paneScale.snapTo(1f)
            sidebarReveal.animateTo(
                targetValue = target,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 400f),
            ) { slotWidth = sidebarWidth * value }
            slotWidth = if (showSidebar) sidebarWidth else 0.dp
            return@LaunchedEffect
        }

        // Cross-fade hand-off (default). The rail animation runs detached so its spring
        // does NOT gate the hand-off below (a coroutineScope here would block on it and
        // stall the fade-in until the rail settles). Only the pane's fade-out is awaited:
        // the slot snaps right after it, the relaid-out pane re-enters from a visible 0.35
        // alpha one frame later, and the rail finishes its glide alongside — every
        // animated frame is render-side only.
        launch {
            sidebarReveal.animateTo(
                targetValue = target,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 400f),
            )
        }
        launch { paneScale.animateTo(0.97f, tween(50, easing = LinearOutSlowInEasing)) }
        paneAlpha.animateTo(0f, tween(50, easing = LinearOutSlowInEasing))
        slotWidth = if (showSidebar) sidebarWidth else 0.dp
        // One frame: produce the relaid-out layout, then re-enter from a clearly visible
        // alpha (0.35) instead of climbing back from zero — the blank window stays a blink.
        withFrameNanos { }
        paneAlpha.snapTo(0.35f)
        paneScale.snapTo(0.98f)
        coroutineScope {
            launch { paneScale.animateTo(1f, tween(100, easing = LinearOutSlowInEasing)) }
            paneAlpha.animateTo(1f, tween(100, easing = LinearOutSlowInEasing))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // The slot snaps between 0 and the rail width; the pane (weight) absorbs the
            // difference, so the pane's content just changes width once per toggle —
            // hidden by the pane being at zero alpha at that moment.
            Spacer(modifier = Modifier.width(slotWidth))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        alpha = paneAlpha.value
                        scaleX = paneScale.value
                        scaleY = paneScale.value
                    },
            ) {
                // While the sidebar is resident the scenes are panes, not cards — skip the
                // phone-style rounded-corner clip; collapsed, the card look comes right back.
                // When collapsed with a secondary page pushed, its back button yields its
                // corner to the pinned toggle via the leading inset (gliding, not teleporting).
                val collapsedOnSecondaryPage =
                    !showSidebar && navigator.current() !is Route.Main
                val sceneLeadingInset by animateDpAsState(
                    targetValue = if (collapsedOnSecondaryPage) 48.dp else 0.dp,
                    animationSpec = tween(durationMillis = 200),
                    label = "sceneLeadingInset",
                )
                CompositionLocalProvider(
                    LocalSceneLeadingInset provides sceneLeadingInset,
                ) {
                    SceneDisplay(
                        navigationPath = navigationPath,
                        entryProvider = entryProvider,
                        predictiveBackEnabled = true,
                        sharedTopBarEnabled = sharedTopBarEnabled,
                        sceneCornerClipEnabled = !showSidebar,
                    )
                }
            }
        }

        // The rail overlays the slot and only slides by reveal progress — render-side
        // translation, so animating it never touches layout.
        NavigationRail(
            modifier = Modifier.graphicsLayer {
                translationX = -sidebarWidth.toPx() * (1f - sidebarReveal.value)
            },
            title = "Miuix",
            dividerColor = YubeixTheme.colorScheme.outline,
            mode = NavigationRailDisplayMode.entries[railMode],
            // The toggle itself is NOT part of the sliding sidebar — a pinned copy is
            // overlaid below so it never moves; this spacer just reserves its spot above
            // the title.
            header = { Spacer(modifier = Modifier.size(SidebarToggleSlotSize)) },
        ) {
            navigationItems.forEachIndexed { index, item ->
                NavigationRailItem(
                    selected = mainPagerState.selectedPage == index,
                    onClick = {
                        // Tapping a top-level destination while a secondary page is pushed
                        // pops the secondary pages first, iPadOS-style.
                        while (navigator.current() != Route.Main) {
                            navigator.pop()
                        }
                        mainPagerState.animateToPage(index)
                    },
                    icon = item.icon,
                    label = item.label,
                )
            }
        }

        // Like iPadOS, the sidebar toggle stays pinned at the same top-left spot in both
        // states, outside the sidebar's slide animation so it never fades and never moves —
        // pushed pages yield their back button instead.
        val updateAppState = LocalUpdateAppState.current
        SidebarCollapseButton(
            onClick = { updateAppState { it.copy(showNavigationBar = !it.showNavigationBar) } },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 8.dp, top = 4.dp),
        )
    }
}

@Composable
private fun SidebarCollapseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = YubeixIcons.HorizontalSplit,
            contentDescription = "Toggle sidebar",
            tint = YubeixTheme.colorScheme.primary,
        )
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
    // The cupertino tab bar overlays the pager content and frosts it through this state, like
    // wordmoment's HomeScreen; the floating bar and the hidden bar skip the extra haze layer.
    val cupertinoBarShown = appState.showNavigationBar && !appState.useFloatingNavigationBar
    val navigationBarHazeState = rememberHazeState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                navigationItems = navigationItems,
                mainPagerState = mainPagerState,
                hazeState = navigationBarHazeState.takeIf { cupertinoBarShown },
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
                .then(
                    if (cupertinoBarShown) {
                        Modifier.hazeSource(navigationBarHazeState)
                    } else {
                        Modifier
                    },
                )
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
    hazeState: HazeState?,
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
                    hazeState = hazeState,
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
