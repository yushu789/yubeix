// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.IconButton
import site.unclefish.yubeix.basic.Scaffold
import site.unclefish.yubeix.basic.SmallTopAppBar
import site.unclefish.yubeix.basic.TopAppBar
import site.unclefish.yubeix.basic.YubeixScrollBehavior
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Back
import site.unclefish.yubeix.icon.extended.More

@Composable
fun TopAppBarDemo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xff667eea), Color(0xff764ba2)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier.weight(0.5f),
                ) {
                    val scrollBehavior = YubeixScrollBehavior()
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = "Title",
                                largeTitle = "Large Title", // If not specified, title value will be used
                                scrollBehavior = scrollBehavior,
                                navigationIcon = {
                                    IconButton(
                                        onClick = { /* Handle click event */ },
                                        modifier = Modifier.padding(start = 16.dp),
                                    ) {
                                        Icon(
                                            YubeixIcons.Back,
                                            contentDescription = "Back",
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { /* Handle click event */ },
                                        modifier = Modifier.padding(end = 16.dp),
                                    ) {
                                        Icon(
                                            YubeixIcons.More,
                                            contentDescription = "More",
                                        )
                                    }
                                },
                            )
                        },
                    ) { paddingValues ->
                        LazyColumn(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                        ) {
                            item {
                                Spacer(Modifier.height(8.dp))
                            }
                            items(100) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 8.dp),
                                ) {
                                    SuperArrow(
                                        title = "Something",
                                    )
                                }
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(0.5f),
                ) {
                    Scaffold(
                        topBar = {
                            SmallTopAppBar(
                                title = "Title",
                                navigationIcon = {
                                    IconButton(
                                        onClick = { /* Handle click event */ },
                                        modifier = Modifier.padding(start = 16.dp),
                                    ) {
                                        Icon(
                                            YubeixIcons.Back,
                                            contentDescription = "Back",
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { /* Handle click event */ },
                                        modifier = Modifier.padding(end = 16.dp),
                                    ) {
                                        Icon(
                                            YubeixIcons.More,
                                            contentDescription = "More",
                                        )
                                    }
                                },
                            )
                        },
                    ) { paddingValues ->
                        LazyColumn(
                            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                        ) {
                            item {
                                Spacer(Modifier.height(8.dp))
                            }
                            items(100) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 8.dp),
                                ) {
                                    SuperArrow(
                                        title = "Something",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
