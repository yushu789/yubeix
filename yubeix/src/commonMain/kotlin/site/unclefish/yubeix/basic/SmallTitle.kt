// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * A [SmallTitle] with Yubeix style.
 *
 * @param text The text to be displayed in the [SmallTitle].
 * @param modifier The modifier to be applied to the [SmallTitle].
 * @param textColor The color of the [SmallTitle].
 * @param insideMargin The margin inside the [SmallTitle].
 */
@Composable
@NonRestartableComposable
fun SmallTitle(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = YubeixTheme.colorScheme.onBackgroundVariant,
    insideMargin: PaddingValues = SmallTitleDefaults.InsideMargin,
) {
    Text(
        modifier = modifier.padding(insideMargin),
        text = text,
        style = YubeixTheme.textStyles.subtitle,
        color = textColor,
    )
}

/** Contains default values used by [SmallTitle]. */
object SmallTitleDefaults {
    /** The default inside margin of the [SmallTitle]: section titles align with row content. */
    val InsideMargin = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
}
