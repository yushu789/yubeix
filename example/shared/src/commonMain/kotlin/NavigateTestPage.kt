// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import appnavigation.Route
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.DropdownImpl
import site.unclefish.yubeix.basic.ListPopupColumn
import site.unclefish.yubeix.basic.ListPopupDefaults
import site.unclefish.yubeix.basic.PopupPositionProvider
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.TopBarActionSpec
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.extra.WindowListPopup
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Edit
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.LocalDismissState
import kotlin.random.Random

private val TopBarPopupItems = listOf("Window 1", "Window 2", "Window 3")

@Composable
fun NavTestPage(
    index: Int,
    padding: PaddingValues,
) {
    val isWideScreen = LocalIsWideScreen.current
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()
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

    // Top bar action with a WindowListPopup anchored to the action button slot.
    var showTopPopup by remember { mutableStateOf(false) }
    var selectedPopupIndex by remember { mutableIntStateOf(0) }
    val hapticFeedback = LocalHapticFeedback.current

    ScreenScaffold(
        title = "Navigate Test $index",
        onBack = { navigator.pop() },
        titleMode = ScreenTitleMode.Pinned,
        listState = lazyListState,
        itemSpacing = 0.dp,
        bottomContentPadding = 32.dp + bottomBarOverlayHeight,
        actions = listOf(
            TopBarActionSpec(
                key = "nav-test-popup",
                contentDescription = "WindowListPopup",
                icon = YubeixIcons.Edit,
                onClick = { showTopPopup = true },
                popupContent = {
                    WindowListPopup(
                        show = showTopPopup,
                        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                        alignment = PopupPositionProvider.Align.TopEnd,
                        onDismissRequest = {
                            showTopPopup = false
                        },
                        content = {
                            val state = LocalDismissState.current
                            ListPopupColumn {
                                TopBarPopupItems.forEachIndexed { popupIndex, string ->
                                    key(popupIndex) {
                                        DropdownImpl(
                                            text = string,
                                            optionSize = TopBarPopupItems.size,
                                            isSelected = selectedPopupIndex == popupIndex,
                                            onSelectedIndexChange = { selectedIdx ->
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                                selectedPopupIndex = selectedIdx
                                                state?.invoke()
                                            },
                                            index = popupIndex,
                                        )
                                    }
                                }
                            }
                        },
                    )
                },
            ),
        ),
        floatingBottomContent = {
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = scrollBarTrackPadding,
            )
        },
    ) {
        item(key = "nav_push") {
            Card(
                modifier = Modifier
                    .padding(all = 12.dp),
            ) {
                SuperArrow(
                    title = "Push another Navigate Test Page",
                    onClick = { navigator.push(Route.NavTest(Random.nextLong().toString())) },
                )
            }
        }
        item(key = "nav_layout") {
            Card(
                modifier = Modifier
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
}
