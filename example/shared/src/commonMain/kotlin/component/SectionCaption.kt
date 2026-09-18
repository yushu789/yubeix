// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * A caption rendered below a section card: yubeix keeps row descriptions out of the rows and
 * under the card instead, aligned with the card's row content.
 */
@Composable
internal fun SectionCaption(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(start = 16.dp),
        style = YubeixTheme.textStyles.footnote1,
        color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
