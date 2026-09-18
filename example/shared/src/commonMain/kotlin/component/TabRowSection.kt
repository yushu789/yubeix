// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import site.unclefish.yubeix.component.animatePagerToPage
import site.unclefish.yubeix.component.rememberPagerFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.TabRow
import site.unclefish.yubeix.basic.TabRowWithContour
import site.unclefish.yubeix.basic.Text

fun LazyListScope.tabRowSection() {
    item(key = "tabRow") {
        SmallTitle(text = "TabRow")
        val tabTexts = remember { listOf("Tab 1", "Tab 2", "Tab 3") }
        val tabTexts1 = remember { listOf("Tab 1", "Tab 2", "Tab 3", "Tab 4", "Tab 5", "Tab 6") }
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        TabRow(
            tabs = tabTexts,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = {
                selectedTabIndex = it
            },
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
            insideMargin = PaddingValues(16.dp),
        ) {
            val scope = rememberCoroutineScope()
            val pagerState = rememberPagerState(pageCount = { tabTexts1.size })
            TabRowWithContour(
                tabs = tabTexts1,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = {
                    scope.launch {
                        pagerState.animatePagerToPage(it)
                    }
                },
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                flingBehavior = rememberPagerFlingBehavior(pagerState),
                userScrollEnabled = true,
                key = { it },
                pageContent = { page ->
                    Text(
                        text = "Content of ${tabTexts1[page]}",
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                },
            )
        }
    }
}
