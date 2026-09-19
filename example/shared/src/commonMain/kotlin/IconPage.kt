// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.shapes.UnevenRoundedRectangle
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.InputField
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.SearchBar
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.YubeixTheme.colorScheme
import utils.All

private val IconListTopShape = UnevenRoundedRectangle(topStart = 16.dp, topEnd = 16.dp)
private val IconListBottomShape = UnevenRoundedRectangle(bottomStart = 16.dp, bottomEnd = 16.dp)

// The expanded search results are a lazy list nested inside the search bar item, so they need an
// explicit height bound to stay measurable and scrollable on their own.
private val SearchResultsMaxHeight = 360.dp

@Composable
fun IconsPage(
    padding: PaddingValues,
) {
    val isWideScreen = LocalIsWideScreen.current

    // Search state: the yubeix SearchBar expands in place as the first list item, like the Home
    // page; the icon rows hide while it is expanded so the results take over the page.
    var searchValue by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    val onCancelSearch = remember {
        {
            searchExpanded = false
            searchValue = ""
        }
    }

    // Icon data
    val allIcons = remember { YubeixIcons.All }
    val lightIcons = remember(allIcons) { allIcons["Light"] ?: emptyList() }
    val regularIcons = remember(allIcons) { allIcons["Regular"] ?: emptyList() }
    val heavyIcons = remember(allIcons) { allIcons["Heavy"] ?: emptyList() }
    val iconNames = remember(lightIcons) { lightIcons.map { it.name.substringBefore(".") } }

    // Search filtering
    val filteredIndices = remember(searchValue, iconNames) {
        if (searchValue.isBlank()) {
            emptyList()
        } else {
            iconNames.indices.filter {
                iconNames[it].contains(searchValue, ignoreCase = true)
            }
        }
    }

    // Scroll state
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
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

    ScreenScaffold(
        title = "Icon",
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
        item(key = "searchbar") {
            SearchBar(
                modifier = Modifier.padding(bottom = 12.dp),
                inputField = {
                    InputField(
                        query = searchValue,
                        onQueryChange = { searchValue = it },
                        onSearch = { searchExpanded = false },
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        label = "Search icons",
                    )
                },
                outsideEndAction = {
                    Text(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 16.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onCancelSearch,
                            ),
                        text = "Cancel",
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        color = YubeixTheme.colorScheme.primary,
                    )
                },
                expanded = searchExpanded,
                onExpandedChange = { searchExpanded = it },
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = SearchResultsMaxHeight),
                ) {
                    items(
                        count = filteredIndices.size,
                        key = { filteredIndices[it] },
                    ) { i ->
                        val index = filteredIndices[i]
                        BasicComponent(
                            title = iconNames[index],
                            startAction = {
                                Icon(
                                    imageVector = regularIcons[index],
                                    contentDescription = iconNames[index],
                                    tint = colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            onClick = {
                                searchValue = ""
                                searchExpanded = false
                                coroutineScope.launch {
                                    delay(350L)
                                    // item 0 = search bar, item 1 = header, icon rows start at item 2
                                    lazyListState.animateScrollToItem(index + 2)
                                }
                            },
                        )
                    }
                }
            }
        }
        if (!searchExpanded) {
            item(key = "iconsHeader") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(IconListTopShape)
                        .background(colorScheme.surfaceContainer)
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Name",
                        modifier = Modifier.weight(2f),
                        style = YubeixTheme.textStyles.footnote1,
                        color = colorScheme.onSurfaceVariantActions,
                    )
                    Text(
                        text = "Light",
                        modifier = Modifier.weight(1f),
                        style = YubeixTheme.textStyles.footnote1,
                        color = colorScheme.onSurfaceVariantActions,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Regular",
                        modifier = Modifier.weight(1f),
                        style = YubeixTheme.textStyles.footnote1,
                        color = colorScheme.onSurfaceVariantActions,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Heavy",
                        modifier = Modifier.weight(1f),
                        style = YubeixTheme.textStyles.footnote1,
                        color = colorScheme.onSurfaceVariantActions,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(
                count = lightIcons.size,
                key = { "icon_$it" },
            ) { index ->
                val isLast = index == lightIcons.lastIndex
                val shape = if (isLast) IconListBottomShape else RectangleShape
                val bottomPadding = if (isLast) 6.dp else 0.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colorScheme.surfaceContainer)
                        .padding(vertical = 6.dp)
                        .padding(bottom = bottomPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = iconNames[index],
                        modifier = Modifier.weight(2f),
                        style = YubeixTheme.textStyles.body2,
                        color = colorScheme.onSurface,
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = lightIcons[index],
                            contentDescription = lightIcons[index].name,
                            tint = colorScheme.onBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = regularIcons[index],
                            contentDescription = regularIcons[index].name,
                            tint = colorScheme.onBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = heavyIcons[index],
                            contentDescription = heavyIcons[index].name,
                            tint = colorScheme.onBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
