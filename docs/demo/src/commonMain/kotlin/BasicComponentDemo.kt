// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Contacts
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun BasicComponentDemo() {
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
            Card {
                BasicComponent(
                    title = "BasicComponent",
                    summary = "Without onClick",
                )
                BasicComponent(
                    title = "Wi-Fi",
                    summary = "Connected to MIUI-WiFi",
                    onClick = { /* Handle click event */ },
                )
                BasicComponent(
                    title = "Nickname",
                    summary = "A brief introduction",
                    startAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            imageVector = YubeixIcons.Contacts,
                            contentDescription = "Avatar Icon",
                            tint = YubeixTheme.colorScheme.onBackground,
                        )
                    },
                    onClick = { /* Handle click event */ },
                )
                BasicComponent(
                    title = "Mobile Network",
                    summary = "SIM card not inserted",
                    enabled = false,
                )
            }
        }
    }
}
