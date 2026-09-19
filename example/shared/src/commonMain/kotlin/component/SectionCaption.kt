// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * A caption rendered below a section card: yubeix keeps row descriptions out of the rows and
 * merges them into one footer paragraph under the card, styled after wordmoment's
 * SettingsCardWithBottomSummary (footnote1 gray text, 16dp/4dp padding hugging the card).
 */
@Composable
internal fun SectionCaption(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        style = YubeixTheme.textStyles.footnote1,
        color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
