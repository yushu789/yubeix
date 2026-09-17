// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.FloatingToolbar
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.IconButton
import site.unclefish.yubeix.basic.Scaffold
import site.unclefish.yubeix.basic.ToolbarPosition
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Delete
import site.unclefish.yubeix.icon.extended.Edit
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun FloatingToolbarDemo() {
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
                    Scaffold(
                        floatingToolbar = {
                            FloatingToolbar {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // or Column
                                    IconButton(onClick = { /* Action 1 */ }) {
                                        Icon(
                                            imageVector = YubeixIcons.Edit,
                                            contentDescription = "Edit",
                                            tint = YubeixTheme.colorScheme.onBackground,
                                        )
                                    }
                                    IconButton(onClick = { /* Action 2 */ }) {
                                        Icon(
                                            imageVector = YubeixIcons.Delete,
                                            contentDescription = "Delete",
                                            tint = YubeixTheme.colorScheme.onBackground,
                                        )
                                    }
                                }
                            }
                        },
                    ) { paddingValues ->
                        LazyColumn(
                            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                        ) {
                            items(100) {
                                SuperArrow(
                                    title = "Something",
                                )
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(0.5f),
                ) {
                    Scaffold(
                        floatingToolbar = {
                            FloatingToolbar {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // or Column
                                    IconButton(onClick = { /* Action 1 */ }) {
                                        Icon(
                                            imageVector = YubeixIcons.Edit,
                                            contentDescription = "Edit",
                                            tint = YubeixTheme.colorScheme.onBackground,
                                        )
                                    }
                                    IconButton(onClick = { /* Action 2 */ }) {
                                        Icon(
                                            imageVector = YubeixIcons.Delete,
                                            contentDescription = "Delete",
                                            tint = YubeixTheme.colorScheme.onBackground,
                                        )
                                    }
                                }
                            }
                        },
                        floatingToolbarPosition = ToolbarPosition.BottomEnd,
                    ) { paddingValues ->
                        LazyColumn(
                            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                        ) {
                            items(100) {
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
