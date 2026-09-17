// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Appends a trailing spacer that grows to fill any empty space between the last
 * real item and the bottom of the viewport. This keeps short scaffold lists
 * reaching the viewport edge (and minimally scrollable), and collapses to a
 * no-op once the content already fills the viewport.
 */
fun LazyListScope.paddingItem(
    state: LazyListState,
    minimumScrollDistance: Dp = 0.dp,
) {
    item(contentType = LazyListPaddingItem) {
        val cachedExpandedHeightPx = remember(state, minimumScrollDistance) { intArrayOf(-1) }
        Box(
            Modifier.layout { _, constraints ->
                val layoutInfo = state.layoutInfo
                val viewportHeight = layoutInfo.viewportSize.height
                val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
                // Derive viewport bottom from the stable viewport size rather than
                // viewportEndOffset, which can drift during overscroll and cause the
                // spacer to grow on every subsequent fling.
                val scrollOffset = firstItem?.offset ?: 0
                val viewportBottom = scrollOffset + viewportHeight - layoutInfo.afterContentPadding
                val lastContentItem =
                    layoutInfo.visibleItemsInfo.lastOrNull { it.contentType !== LazyListPaddingItem }
                val contentBottom = lastContentItem?.let { it.offset + it.size } ?: 0
                val fillHeight = (viewportBottom - contentBottom + 1).coerceAtLeast(0)
                val minimumScrollDistancePx = minimumScrollDistance.roundToPx()
                val height = if (minimumScrollDistancePx > 0) {
                    val expandedHeight = fillHeight + minimumScrollDistancePx
                    if (state.firstVisibleItemIndex == 0 || cachedExpandedHeightPx[0] < 0) {
                        cachedExpandedHeightPx[0] = expandedHeight
                    }
                    cachedExpandedHeightPx[0].coerceAtLeast(minimumScrollDistancePx)
                } else {
                    fillHeight
                }
                layout(constraints.maxWidth, height) {}
            },
        )
    }
}

private data object LazyListPaddingItem
