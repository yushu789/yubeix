// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.extra.SuperGroup
import site.unclefish.yubeix.theme.YubeixTheme

fun LazyListScope.basicComponentSection() {
    item(key = "basicComponent") {
        SmallTitle(text = "Basic Component")
        SuperGroup(
            modifier = Modifier
                .padding(bottom = 8.dp),
        ) {
            BasicComponent(
                title = "Title",
                startAction = {
                    Text(
                        text = "Start",
                    )
                },
                endActions = {
                    Text(
                        text = "End1",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.onSurfaceVariantActions,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "End2",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.onSurfaceVariantActions,
                    )
                },
                enabled = true,
            )
            BasicComponent(
                title = "Title",
                startAction = {
                    Text(
                        text = "Start",
                        color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
                    )
                },
                endActions = {
                    Text(
                        text = "End1",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "End2",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
                    )
                },
                enabled = false,
            )
        }
        SectionCaption("Title rows with start and end actions; the second row is disabled")
    }
}
