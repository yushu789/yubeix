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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Contacts
import site.unclefish.yubeix.icon.extended.Edit
import site.unclefish.yubeix.icon.extended.Rename
import site.unclefish.yubeix.icon.extended.Settings
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun IconDemo() {
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
            Card(
                insideMargin = PaddingValues(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    Icon(
                        imageVector = YubeixIcons.Contacts,
                        contentDescription = "Personal",
                        tint = YubeixTheme.colorScheme.onBackground,
                    )
                    Icon(
                        imageVector = YubeixIcons.Settings,
                        contentDescription = "Settings",
                        tint = YubeixTheme.colorScheme.onBackground,
                    )
                    Icon(
                        imageVector = YubeixIcons.Edit,
                        contentDescription = "Blue Edit",
                        tint = Color.Blue,
                    )
                    Icon(
                        imageVector = YubeixIcons.Rename,
                        contentDescription = "Green Rename",
                        tint = Color.Green,
                    )
                }
            }
        }
    }
}
