// Copyright 2025, yubeix contributors
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.CardDefaults
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.utils.PressFeedbackType

@Composable
fun CardDemo() {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = CardDefaults.defaultColors(
                    color = YubeixTheme.colorScheme.primaryVariant,
                ),
                insideMargin = PaddingValues(16.dp),
            ) {
                Text(
                    color = YubeixTheme.colorScheme.onPrimary,
                    text = "Card",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    color = YubeixTheme.colorScheme.onPrimaryVariant,
                    text = "This is a Card",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    insideMargin = PaddingValues(16.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    showIndication = true,
                    onClick = { },
                    content = {
                        Text(
                            color = YubeixTheme.colorScheme.onSurface,
                            text = "Card",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
                            text = "PressFeedback: Sink\nShowIndication: true",
                            style = YubeixTheme.textStyles.paragraph,
                        )
                    },
                )
                Card(
                    modifier = Modifier.weight(1f),
                    insideMargin = PaddingValues(16.dp),
                    pressFeedbackType = PressFeedbackType.Tilt,
                    content = {
                        Text(
                            color = YubeixTheme.colorScheme.onSurface,
                            text = "Card",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
                            text = "PressFeedback: Tilt\nShowIndication: false",
                            style = YubeixTheme.textStyles.paragraph,
                        )
                    },
                )
            }
        }
    }
}
