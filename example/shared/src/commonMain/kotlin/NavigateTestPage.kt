// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import appnavigation.Route
import component.BackNavigationIcon
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.DropdownImpl
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.IconButton
import site.unclefish.yubeix.basic.ListPopupColumn
import site.unclefish.yubeix.basic.ListPopupDefaults
import site.unclefish.yubeix.basic.PopupPositionProvider
import site.unclefish.yubeix.basic.Scaffold
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.YubeixScrollBehavior
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.extra.WindowListPopup
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Edit
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.LocalDismissState
import site.unclefish.yubeix.theme.YubeixTheme.colorScheme
import utils.AdaptiveTopAppBar
import utils.pageContentPadding
import utils.pageScrollModifiers
import kotlin.random.Random

private val TopBarPopupItems = listOf("Window 1", "Window 2", "Window 3")

@Composable
fun NavTestPage(
    index: Int,
    padding: PaddingValues,
) {
    val appState = LocalAppState.current
    val isWideScreen = LocalIsWideScreen.current
    val topAppBarScrollBehavior = YubeixScrollBehavior()
    val navigator = LocalNavigator.current

    Scaffold(
        topBar = {
            AdaptiveTopAppBar(
                title = "Navigate Test $index",
                showTopAppBar = appState.showTopAppBar,
                isWideScreen = isWideScreen,
                scrollBehavior = topAppBarScrollBehavior,
                navigationIcon = {
                    BackNavigationIcon(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = { navigator.pop() },
                    )
                },
                actions = {
                    TopBarActions()
                },
            )
        },
    ) { innerPadding ->
        val lazyListState = rememberLazyListState()
        val contentPadding = pageContentPadding(
            innerPadding,
            padding,
            isWideScreen,
            extraStart = WindowInsets.displayCutout.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr),
            extraEnd = WindowInsets.displayCutout.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr),
        )
        Box {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.pageScrollModifiers(
                    appState.enableScrollEndHaptic,
                    appState.showTopAppBar,
                    topAppBarScrollBehavior,
                ),
                contentPadding = contentPadding,
            ) {
                item(key = "nav_push") {
                    Card(
                        modifier = Modifier
                            .padding(all = 12.dp),
                    ) {
                        val navigator = LocalNavigator.current
                        SuperArrow(
                            title = "Push another Navigate Test Page",
                            onClick = { navigator.push(Route.NavTest(Random.nextLong().toString())) },
                        )
                    }
                }
                item(key = "nav_layout") {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        SuperArrow(
                            title = "Long Title Long Title Long Title Long Title Long Title Long Title Long Title Long Title",
                            summary = "Summary",
                            startAction = {
                                Text(text = "Start")
                            },
                            endActions = {
                                Text(text = "End1", textAlign = TextAlign.End)
                                Spacer(Modifier.width(8.dp))
                                Text(text = "End2", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Title",
                            summary = "Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary",
                            startAction = {
                                Text(text = "Start")
                            },
                            endActions = {
                                Text(text = "End1", textAlign = TextAlign.End)
                                Spacer(Modifier.width(8.dp))
                                Text(text = "End2", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Title",
                            summary = "Summary",
                            startAction = {
                                Text(text = "Long Start Long Start Long Start Long Start Long Start Long Start Long Start Long Start")
                            },
                            endActions = {
                                Text(text = "End1", textAlign = TextAlign.End)
                                Spacer(Modifier.width(8.dp))
                                Text(text = "End2", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Title",
                            summary = "Summary",
                            startAction = {
                                Text(text = "Start")
                            },
                            endActions = {
                                Text(
                                    text = "Long End Long End Long End Long End Long End Long End Long End Long End",
                                    textAlign = TextAlign.End,
                                )
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Long Title Long Title Long Title Long Title Long Title Long Title Long Title Long Title",
                            summary = "Summary",
                            startAction = {
                                Text(text = "Long Start Long Start Long Start Long Start Long Start Long Start Long Start Long Start")
                            },
                            endActions = {
                                Text(text = "End", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Long Title Long Title Long Title Long Title Long Title Long Title Long Title Long Title",
                            summary = "Summary",
                            startAction = {
                                Text(text = "Start")
                            },
                            endActions = {
                                Text(
                                    text = "Long End Long End Long End Long End Long End Long End Long End Long End",
                                    textAlign = TextAlign.End,
                                )
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Title",
                            summary = "Summary",
                            startAction = {
                                Text(text = "Long Start Long Start Long Start Long Start Long Start Long Start Long Start Long Start")
                            },
                            endActions = {
                                Text(
                                    text = "Long End Long End Long End Long End Long End Long End Long End Long End",
                                    textAlign = TextAlign.End,
                                )
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Title",
                            summary = "Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary",
                            endActions = {
                                Text(
                                    text = "Long End Long End Long End Long End Long End Long End Long End Long End",
                                    textAlign = TextAlign.End,
                                )
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Long Title Long Title Long Title Long Title Long Title Long Title Long Title Long Title",
                            summary = "Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary Long Summary",
                            endActions = {
                                Text(text = "Long End Long End Long End Long End", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Long Title Long Title Long Title Long Title",
                            summary = "Summary",
                            endActions = {
                                Text(text = "Long End Long End Long End Long End", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                        SuperArrow(
                            title = "Title",
                            summary = "Long Summary Long Summary Long Summary Long Summary",
                            endActions = {
                                Text(text = "Long End Long End", textAlign = TextAlign.End)
                            },
                            enabled = true,
                        )
                    }
                }
            }
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = contentPadding,
            )
        }
    }
}

@Composable
fun TopBarActions() {
    val showTopPopup = remember { mutableStateOf(false) }
    val topPopupHoldDown = remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val hapticFeedback = LocalHapticFeedback.current
    IconButton(
        modifier = Modifier.padding(end = 16.dp),
        onClick = {
            showTopPopup.value = true
            topPopupHoldDown.value = true
        },
        holdDownState = topPopupHoldDown.value,
    ) {
        Icon(
            imageVector = YubeixIcons.Edit,
            contentDescription = "WindowListPopup",
            tint = colorScheme.onBackground,
        )
    }
    WindowListPopup(
        show = showTopPopup.value,
        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
        alignment = PopupPositionProvider.Align.TopEnd,
        onDismissRequest = {
            showTopPopup.value = false
        },
        onDismissFinished = {
            topPopupHoldDown.value = false
        },
        content = {
            val state = LocalDismissState.current
            ListPopupColumn {
                TopBarPopupItems.forEachIndexed { index, string ->
                    key(index) {
                        DropdownImpl(
                            text = string,
                            optionSize = TopBarPopupItems.size,
                            isSelected = selectedIndex == index,
                            onSelectedIndexChange = { selectedIdx ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                selectedIndex = selectedIdx
                                state?.invoke()
                            },
                            index = index,
                        )
                    }
                }
            }
        },
    )
}
