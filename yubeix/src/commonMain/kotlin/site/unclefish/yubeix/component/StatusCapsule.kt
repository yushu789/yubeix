// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixCapsuleShape

/**
 * A small pill-shaped status chip: an optional leading icon and a single-line label on a capsule
 * of [containerColor]. Optionally clickable as a button.
 *
 * @param text The label shown inside the capsule.
 * @param modifier The modifier applied to the capsule container.
 * @param textModifier The modifier applied to the label inside the capsule.
 * @param leadingIcon Optional content rendered before the label, sized to fit the capsule height.
 * @param minHeight The minimum height of the capsule.
 * @param contentPadding The padding between the capsule edge and its content.
 * @param containerColor The capsule background color.
 * @param contentColor The label color.
 * @param textStyle The typographic style of the label.
 * @param onClick When non-null, the capsule becomes clickable and gets the button semantics role.
 */
@Composable
fun StatusCapsule(
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    leadingIcon: (@Composable RowScope.() -> Unit)? = null,
    minHeight: Dp = 32.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
    containerColor: Color = YubeixTheme.colorScheme.surface,
    contentColor: Color = YubeixTheme.colorScheme.onSurface,
    textStyle: TextStyle = YubeixTheme.textStyles.body1,
    onClick: (() -> Unit)? = null,
) {
    val shape = yubeixCapsuleShape()
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .then(clickModifier)
            .defaultMinSize(minHeight = minHeight)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke(this)
        Text(
            text = text,
            modifier = textModifier,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
