// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import site.unclefish.yubeix.basic.PullToRefresh
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenScaffoldDefaults
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberPullToRefreshState
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.component.OverscrollTitle
import site.unclefish.yubeix.component.overscrollTitleTextStyle
import site.unclefish.yubeix.extra.SuperDropdown
import site.unclefish.yubeix.extra.SuperGroup
import site.unclefish.yubeix.extra.WindowDropdown
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.YubeixTheme.colorScheme

// ScreenScaffold's private DefaultScreenHeroTitlePadding is the reference for this inset; body
// mode pages replicate it to keep the hero title visually aligned with the built-in list path.
private val DropdownHeroTitlePadding = PaddingValues(
    start = 12.dp,
    end = 16.dp,
    top = 4.dp,
    bottom = 4.dp,
)

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
    val density = LocalDensity.current
    // The hero title rests right below the chrome bar, matching the scaffold's own hero geometry:
    // status bar inset + chrome bar height, measured in window pixels for OverscrollTitle.
    val heroTitleRestingTop =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            ScreenScaffoldDefaults.TopBarVisualHeight
    val heroTitleRestingTopInWindowPx = with(density) { heroTitleRestingTop.toPx() }
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
            // marking for the chrome bar's frost. Body mode does not get an automatic hero, so
            // the page renders its own with OverscrollTitle; the scaffold's collapsed title is
            // driven from the hoisted lazyListState shared with the list below.
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
                    // Mirrors the built-in list's itemSpacing so the hero title keeps the same
                    // distance to the first group as on content-path pages.
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "dropdownHero") {
                        OverscrollTitle(
                            restingTopInWindowPx = heroTitleRestingTopInWindowPx,
                        ) {
                            Text(
                                text = "Dropdown",
                                style = overscrollTitleTextStyle(
                                    YubeixTheme.textStyles.title1.copy(fontWeight = FontWeight.SemiBold),
                                ),
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(DropdownHeroTitlePadding),
                            )
                        }
                    }
                    item(key = "dropdownGroup") {
                        // SuperGroup is the built-in grouped container: it draws the rounded card
                        // background and the dividers between rows, so no per-row shapes are
                        // needed. Its SubcomposeLayout measures fine inside a lazy list item.
                        SuperGroup {
                            repeat(dropdownCount) { i ->
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
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        },
    ) {
        // The body is provided by bodyContent above; nothing goes into the built-in list.
    }
}
