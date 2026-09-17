// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.NavigationItem
import site.unclefish.yubeix.basic.NavigationRail
import site.unclefish.yubeix.basic.NavigationRailDisplayMode
import site.unclefish.yubeix.basic.NavigationRailItem
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Contacts
import site.unclefish.yubeix.icon.extended.Settings
import site.unclefish.yubeix.icon.extended.VerticalSplit
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun NavigationRailDemo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xff667eea), Color(0xff764ba2)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .widthIn(max = 800.dp) // Wider for Rail
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val pages = listOf("Home", "Profile", "Settings")
            val items = listOf(
                NavigationItem("Home", YubeixIcons.VerticalSplit),
                NavigationItem("Profile", YubeixIcons.Contacts),
                NavigationItem("Settings", YubeixIcons.Settings),
            )
            var selectedIndex by remember { mutableIntStateOf(0) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp),
            ) {
                Card(
                    modifier = Modifier.weight(0.5f).fillMaxHeight(),
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            mode = NavigationRailDisplayMode.IconAndText,
                        ) {
                            items.forEachIndexed { index, item ->
                                NavigationRailItem(
                                    selected = selectedIndex == index,
                                    onClick = { selectedIndex = index },
                                    icon = item.icon,
                                    label = item.label,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Current: ${pages[selectedIndex]}",
                                style = YubeixTheme.textStyles.title1,
                            )
                        }
                    }
                }
            }
        }
    }
}
