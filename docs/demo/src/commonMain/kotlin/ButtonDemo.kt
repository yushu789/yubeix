// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Button
import site.unclefish.yubeix.basic.ButtonDefaults
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.TextButton
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Contacts
import site.unclefish.yubeix.icon.extended.Favorites
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun ButtonDemo() {
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
            var buttonText1 by remember { mutableStateOf("Button") }
            var buttonText2 by remember { mutableStateOf("TextButton") }
            var clickCount1 by remember { mutableIntStateOf(0) }
            var clickCount2 by remember { mutableIntStateOf(0) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = {
                        clickCount1++
                        buttonText1 = "Button: $clickCount1"
                    },
                ) {
                    Icon(
                        imageVector = YubeixIcons.Favorites,
                        contentDescription = "Favorites",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = buttonText1,
                        style = YubeixTheme.textStyles.button,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                TextButton(
                    text = buttonText2,
                    onClick = {
                        clickCount2++
                        buttonText2 = "TextButton: $clickCount2"
                    },
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    enabled = false,
                    onClick = {},
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Icon(
                        imageVector = YubeixIcons.Contacts,
                        contentDescription = null,
                        tint = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Disabled Button",
                        style = YubeixTheme.textStyles.button,
                        color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                TextButton(
                    text = "Disabled TextButton",
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}
