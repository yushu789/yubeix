// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import appnavigation.Route
import component.SectionCaption
import dev.chrisbanes.haze.rememberHazeState
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.extra.SuperDropdown
import site.unclefish.yubeix.extra.SuperGroup
import site.unclefish.yubeix.extra.SuperSwitch
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.ThemeColorSpec
import site.unclefish.yubeix.theme.ThemePaletteStyle

private val NavigationBarDisplayModeOptions = listOf("IconAndText", "IconOnly", "TextOnly", "IconWithSelectedLabel")
private val NavigationRailDisplayModeOptions = listOf("IconAndText", "IconOnly", "TextOnly", "IconWithSelectedLabel")
private val FloatingNavigationBarDisplayModeOptions = listOf("IconAndText", "IconOnly", "TextOnly")
private val FloatingNavigationBarPositionOptions = listOf("Center", "Start", "End")
private val FloatingToolbarPositionOptions =
    listOf("TopStart", "CenterStart", "BottomStart", "TopEnd", "CenterEnd", "BottomEnd", "TopCenter", "BottomCenter")
private val FloatingToolbarOrientationOptions = listOf("Horizontal", "Vertical")
private val FabPositionOptions = listOf("Start", "Center", "End", "EndOverlay")
private val ColorModeOptions = listOf("System", "Light", "Dark", "MonetSystem", "MonetLight", "MonetDark")
private val PaletteStyleOptions = ThemePaletteStyle.entries.map { it.name }
private val ColorSpecOptions = ThemeColorSpec.entries.map { it.name }
private val KeyColorOptions = listOf("Default") + ui.KeyColors.map { it.first }

@Composable
fun SettingsPage(
    padding: PaddingValues,
) {
    val appState = LocalAppState.current
    val isWideScreen = LocalIsWideScreen.current
    val updateAppState = LocalUpdateAppState.current
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()
    // ScreenScaffold marks the scrolling content as the haze source itself, so the chrome bar can
    // frost it without a manual hazeSource on the list.
    val hazeState = rememberHazeState()
    // The compact shell's bottom bar floats over the page, so its height joins the content's
    // bottom padding; the wide pane has no bottom bar and the scaffold's own inset is enough.
    val bottomBarOverlayHeight = if (isWideScreen) 0.dp else padding.calculateBottomPadding()
    // Keeps the scrollbar track between the chrome bar at the top and the page bottom.
    val scrollBarTrackPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = if (isWideScreen) {
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        } else {
            bottomBarOverlayHeight
        },
    )

    // The settings list is long, so ScrollAware keeps the collapsed centered title hidden until
    // the content scrolls instead of spending a hero row on it.
    ScreenScaffold(
        title = "Settings",
        onBack = null,
        titleMode = ScreenTitleMode.Hero,
        listState = lazyListState,
        hazeState = hazeState,
        itemSpacing = 0.dp,
        bottomContentPadding = 32.dp + bottomBarOverlayHeight,
        floatingBottomContent = {
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = scrollBarTrackPadding,
            )
        },
    ) {
        item(key = "settingsUi") {
            SuperGroup(
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                SuperSwitch(
                    title = "Show FPS Monitor",
                    checked = appState.showFPSMonitor,
                    onCheckedChange = { updateAppState { state -> state.copy(showFPSMonitor = it) } },
                )
                SuperSwitch(
                    title = "Show TopAppBar",
                    checked = appState.showTopAppBar,
                    onCheckedChange = { updateAppState { state -> state.copy(showTopAppBar = it) } },
                )
                SuperSwitch(
                    title = if (isWideScreen) "Show NavigationRail" else "Show NavigationBar",
                    checked = appState.showNavigationBar,
                    onCheckedChange = { updateAppState { state -> state.copy(showNavigationBar = it) } },
                )
                AnimatedVisibility(visible = appState.showNavigationBar && !isWideScreen && !appState.useFloatingNavigationBar) {
                    SuperDropdown(
                        title = "NavigationBar Mode",
                        items = NavigationBarDisplayModeOptions,
                        selectedIndex = appState.navigationBarMode,
                        onSelectedIndexChange = { updateAppState { state -> state.copy(navigationBarMode = it) } },
                    )
                }
                AnimatedVisibility(visible = appState.showNavigationBar && isWideScreen) {
                    SuperDropdown(
                        title = "NavigationRail Mode",
                        items = NavigationRailDisplayModeOptions,
                        selectedIndex = appState.navigationRailMode,
                        onSelectedIndexChange = { updateAppState { state -> state.copy(navigationRailMode = it) } },
                    )
                }
                AnimatedVisibility(visible = appState.showNavigationBar && !isWideScreen) {
                    Column {
                        SuperSwitch(
                            title = "Use FloatingNavigationBar",
                            checked = appState.useFloatingNavigationBar,
                            onCheckedChange = { updateAppState { state -> state.copy(useFloatingNavigationBar = it) } },
                        )
                        AnimatedVisibility(visible = appState.useFloatingNavigationBar) {
                            Column {
                                SuperDropdown(
                                    title = "FloatingNavigationBar Mode",
                                    items = FloatingNavigationBarDisplayModeOptions,
                                    selectedIndex = appState.floatingNavigationBarMode,
                                    onSelectedIndexChange = { updateAppState { state -> state.copy(floatingNavigationBarMode = it) } },
                                )
                                SuperDropdown(
                                    title = "FloatingNavigationBar Position",
                                    items = FloatingNavigationBarPositionOptions,
                                    selectedIndex = appState.floatingNavigationBarPosition,
                                    onSelectedIndexChange = { updateAppState { state -> state.copy(floatingNavigationBarPosition = it) } },
                                )
                            }
                        }
                    }
                }
                SuperSwitch(
                    title = "Show FloatingToolbar",
                    checked = appState.showFloatingToolbar,
                    onCheckedChange = { updateAppState { state -> state.copy(showFloatingToolbar = it) } },
                )
                AnimatedVisibility(visible = appState.showFloatingToolbar) {
                    Column {
                        SuperDropdown(
                            title = "FloatingToolbar Position",
                            items = FloatingToolbarPositionOptions,
                            selectedIndex = appState.floatingToolbarPosition,
                            onSelectedIndexChange = { updateAppState { state -> state.copy(floatingToolbarPosition = it) } },
                        )
                        SuperDropdown(
                            title = "FloatingToolbar Orientation",
                            items = FloatingToolbarOrientationOptions,
                            selectedIndex = appState.floatingToolbarOrientation,
                            onSelectedIndexChange = { updateAppState { state -> state.copy(floatingToolbarOrientation = it) } },
                        )
                    }
                }
                SuperSwitch(
                    title = "Show FloatingActionButton",
                    checked = appState.showFloatingActionButton,
                    onCheckedChange = { updateAppState { state -> state.copy(showFloatingActionButton = it) } },
                )
                AnimatedVisibility(visible = appState.showFloatingActionButton) {
                    SuperDropdown(
                        title = "FloatingActionButton Position",
                        items = FabPositionOptions,
                        selectedIndex = appState.floatingActionButtonPosition,
                        onSelectedIndexChange = { updateAppState { state -> state.copy(floatingActionButtonPosition = it) } },
                    )
                }
                SuperSwitch(
                    title = "Enable Scroll End Haptic",
                    checked = appState.enableScrollEndHaptic,
                    onCheckedChange = { updateAppState { state -> state.copy(enableScrollEndHaptic = it) } },
                )
                SuperSwitch(
                    title = "Enable Page User Scroll",
                    checked = appState.enablePageUserScroll,
                    onCheckedChange = { updateAppState { state -> state.copy(enablePageUserScroll = it) } },
                )
                SuperDropdown(
                    title = "Color Mode",
                    items = ColorModeOptions,
                    selectedIndex = appState.colorMode,
                    onSelectedIndexChange = { updateAppState { state -> state.copy(colorMode = it) } },
                )
                AnimatedVisibility(visible = appState.colorMode in 3..5) {
                    SuperDropdown(
                        title = "Key Color",
                        items = KeyColorOptions,
                        selectedIndex = appState.seedIndex,
                        onSelectedIndexChange = { updateAppState { state -> state.copy(seedIndex = it) } },
                    )
                }
                AnimatedVisibility(visible = appState.colorMode in 3..5 && appState.seedIndex > 0) {
                    Column {
                        SuperDropdown(
                            title = "Palette Style",
                            items = PaletteStyleOptions,
                            selectedIndex = appState.paletteStyle,
                            onSelectedIndexChange = { updateAppState { state -> state.copy(paletteStyle = it) } },
                        )
                        SuperDropdown(
                            title = "Color Spec",
                            items = ColorSpecOptions,
                            selectedIndex = appState.colorSpec,
                            onSelectedIndexChange = { updateAppState { state -> state.copy(colorSpec = it) } },
                        )
                    }
                }
            }
        }
        item(key = "settingsTransition") {
            SuperGroup {
                SuperSwitch(
                    title = "Enable Corner Clip",
                    checked = appState.enableCornerClip,
                    onCheckedChange = { updateAppState { state -> state.copy(enableCornerClip = it) } },
                )
                SuperSwitch(
                    title = "Enable Dim",
                    checked = appState.enableDim,
                    onCheckedChange = { updateAppState { state -> state.copy(enableDim = it) } },
                )
                SuperSwitch(
                    title = "Block Input During Transition",
                    checked = appState.blockInputDuringTransition,
                    onCheckedChange = { updateAppState { state -> state.copy(blockInputDuringTransition = it) } },
                )
                SuperSwitch(
                    title = "Pop Follows Swipe Edge",
                    checked = appState.popDirectionFollowsSwipeEdge,
                    onCheckedChange = { updateAppState { state -> state.copy(popDirectionFollowsSwipeEdge = it) } },
                )
                SuperSwitch(
                    title = "Sidebar Follow Resize",
                    checked = appState.sidebarFollowResize,
                    onCheckedChange = { updateAppState { state -> state.copy(sidebarFollowResize = it) } },
                )
            }
            // One merged footer for the whole group, wordmoment SettingsCardWithBottomSummary style;
            // the caption's own 4dp bottom plus 8dp keeps the 12dp group rhythm.
            SectionCaption(
                text = "During transitions, clip the top scene with rounded corners, dim the scene " +
                    "behind, and block touch input on the non-target scene; the pop animation " +
                    "direction follows the finger swipe edge. Sidebar Follow Resize swaps the " +
                    "toggle's cross-fade hand-off for the direct mode, where the pane's width " +
                    "tracks the sidebar edge frame by frame.",
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item(key = "settingsNavigation") {
            SuperGroup {
                SuperArrow(
                    title = "Navigate Test",
                    // A stable route instance, not a fresh random id per tap: NavigationPath.push
                    // only reverses a mid-exit scene back open when the pushed route equals the
                    // exiting one, and a randomized id would tear the leaving page down and restart
                    // the push from the right edge instead of interrupting it.
                    onClick = { navigator.push(Route.NavTest("settings")) },
                )
            }
            SectionCaption(
                text = "Navigate to a Navigate Test Page",
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item(key = "settingsAbout") {
            SuperGroup {
                SuperArrow(
                    title = "About",
                    onClick = { navigator.push(Route.About) },
                )
            }
            SectionCaption(text = "About this example App")
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
