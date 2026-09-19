// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.kyant.shapes.UnevenRoundedRectangle
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import site.unclefish.yubeix.basic.PullToRefresh
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenScaffoldDefaults
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberPullToRefreshState
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.extra.SuperDropdown
import site.unclefish.yubeix.extra.WindowDropdown
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.YubeixTheme.colorScheme

private val DropdownListTopShape = UnevenRoundedRectangle(topStart = 16.dp, topEnd = 16.dp)
private val DropdownListBottomShape = UnevenRoundedRectangle(bottomStart = 16.dp, bottomEnd = 16.dp)

@Composable
fun DropdownPage(
    padding: PaddingValues,
) {
    val isWideScreen = LocalIsWideScreen.current
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val dropdownOptions = remember { listOf("Option 1", "Option 2", "Option 3", "Option 4") }
    var dropdownSelectedOption by remember { mutableIntStateOf(0) }
    var dropdownCount by remember { mutableIntStateOf(6) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(200)
            dropdownCount += 6
            isRefreshing = false
        }
    }

    val lazyListState = rememberLazyListState()
    // ScreenScaffold marks the scrolling content as the haze source itself, so the chrome bar can
    // frost it without a manual hazeSource on the list.
    val hazeState = rememberHazeState()
    // The compact shell's bottom bar floats over the page, so its height joins the content's
    // bottom padding; the wide pane has no bottom bar and the scaffold's own inset is enough.
    val bottomBarOverlayHeight = if (isWideScreen) 0.dp else padding.calculateBottomPadding()
    // PullToRefresh owns the body instead of the scaffold's built-in list, so the content padding
    // mirrors the scaffold's own geometry: chrome bar top inset + height + gap, plus the extra
    // breathing room the page had before.
    val listContentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            ScreenScaffoldDefaults.TopBarVisualHeight + 12.dp,
        bottom = 32.dp + bottomBarOverlayHeight +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        start = 16.dp,
        end = 16.dp,
    )

    ScreenScaffold(
        title = "Dropdown",
        onBack = null,
        titleMode = ScreenTitleMode.Hero,
        listState = lazyListState,
        hazeState = hazeState,
        bottomContentPadding = 32.dp + bottomBarOverlayHeight,
        floatingBottomContent = {
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = listContentPadding,
            )
        },
        bodyContent = { contentModifier ->
            // PullToRefresh wraps the scrolling content; the modifier carries the haze source
            // marking for the chrome bar's frost.
            PullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true },
                pullToRefreshState = pullToRefreshState,
                modifier = contentModifier,
                contentPadding = PaddingValues(top = listContentPadding.calculateTopPadding()),
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listContentPadding,
                ) {
                    items(
                        count = dropdownCount,
                        key = { "dropdown_$it" },
                    ) { i ->
                        val isFirst = i == 0
                        val isLast = i == dropdownCount - 1
                        val shape = when {
                            isFirst -> DropdownListTopShape
                            isLast -> DropdownListBottomShape
                            else -> RectangleShape
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(colorScheme.surfaceContainer),
                        ) {
                            if (i % 2 == 0) {
                                SuperDropdown(
                                    title = "SuperDropdown ${i + 1}",
                                    items = dropdownOptions,
                                    selectedIndex = dropdownSelectedOption,
                                    onSelectedIndexChange = { newOption ->
                                        dropdownSelectedOption = newOption
                                    },
                                )
                            } else {
                                WindowDropdown(
                                    title = "WindowDropdown ${i + 1}",
                                    items = dropdownOptions,
                                    selectedIndex = dropdownSelectedOption,
                                    onSelectedIndexChange = { newOption ->
                                        dropdownSelectedOption = newOption
                                    },
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        },
    ) {
        // The body is provided by bodyContent above; nothing goes into the built-in list.
    }
}
