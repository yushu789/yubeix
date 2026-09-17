// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape

/**
 * A [Button] component with Yubeix style.
 *
 * @param onClick The callback when the [Button] is clicked.
 * @param modifier The modifier to be applied to the [Button].
 * @param enabled Whether the [Button] is enabled.
 * @param cornerRadius The corner radius of the [Button].
 * @param capsule Whether to use a capsule (stadium) shape instead of the rounded corner shape.
 * @param minWidth The minimum width of the [Button].
 * @param minHeight The minimum height of the [Button].
 * @param colors The [ButtonColors] of the [Button].
 * @param insideMargin The margin inside the [Button].
 * @param interactionSource The [MutableInteractionSource] to be used for the [Button].
 * @param indication The [Indication] to be used for the [Button].
 * @param content The [Composable] content of the [Button].
 */
@Composable
@NonRestartableComposable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = ButtonDefaults.CornerRadius,
    capsule: Boolean = false,
    minWidth: Dp = ButtonDefaults.MinWidth,
    minHeight: Dp = ButtonDefaults.MinHeight,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    insideMargin: PaddingValues = ButtonDefaults.InsideMargin,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = LocalIndication.current,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = if (capsule) Capsule() else yubeixShape(cornerRadius)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.semantics { role = Role.Button },
        shape = shape,
        color = if (enabled) colors.color else colors.disabledColor,
        contentColor = if (enabled) colors.contentColor else colors.disabledContentColor,
        interactionSource = interactionSource,
        indication = indication,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
                .padding(insideMargin),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * A [TextButton] component with Yubeix style.
 *
 * @param text The text of the [TextButton].
 * @param onClick The callback when the [TextButton] is clicked.
 * @param modifier The modifier to be applied to the [TextButton].
 * @param enabled Whether the [TextButton] is enabled.
 * @param cornerRadius The corner radius of the [TextButton].
 * @param capsule Whether to use a capsule (stadium) shape instead of the rounded corner shape.
 * @param minWidth The minimum width of the [TextButton].
 * @param minHeight The minimum height of the [TextButton].
 * @param colors The [TextButtonColors] of the [TextButton].
 * @param insideMargin The margin inside the [TextButton].
 * @param interactionSource The [MutableInteractionSource] to be used for the [TextButton].
 * @param indication The [Indication] to be used for the [TextButton].
 */
@Composable
@NonRestartableComposable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = ButtonDefaults.CornerRadius,
    capsule: Boolean = false,
    minWidth: Dp = ButtonDefaults.MinWidth,
    minHeight: Dp = ButtonDefaults.MinHeight,
    colors: TextButtonColors = ButtonDefaults.textButtonColors(),
    insideMargin: PaddingValues = ButtonDefaults.InsideMargin,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = LocalIndication.current,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        cornerRadius = cornerRadius,
        capsule = capsule,
        minWidth = minWidth,
        minHeight = minHeight,
        colors = ButtonColors(
            color = colors.color,
            disabledColor = colors.disabledColor,
            contentColor = colors.textColor,
            disabledContentColor = colors.disabledTextColor,
        ),
        insideMargin = insideMargin,
        interactionSource = interactionSource,
        indication = indication,
    ) {
        Text(
            text = text,
            style = YubeixTheme.textStyles.button,
        )
    }
}

object ButtonDefaults {

    /**
     * The default min width applied for all buttons. Note that you can override it by applying
     * Modifier.widthIn directly on the button composable.
     */
    val MinWidth = 58.dp

    /**
     * The default min height applied for all buttons. Note that you can override it by applying
     * Modifier.heightIn directly on the button composable.
     */
    val MinHeight = 40.dp

    /**
     * The default corner radius applied for all buttons.
     */
    val CornerRadius = 16.dp

    /**
     * The default inside margin applied for all buttons.
     */
    val InsideMargin = PaddingValues(horizontal = 16.dp, vertical = 13.dp)

    /**
     * The default [ButtonColors] for all buttons.
     */
    @Composable
    fun buttonColors(
        color: Color = YubeixTheme.colorScheme.secondaryVariant,
        disabledColor: Color = YubeixTheme.colorScheme.disabledSecondaryVariant,
        contentColor: Color = YubeixTheme.colorScheme.onSecondaryVariant,
        disabledContentColor: Color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
    ): ButtonColors = remember(color, disabledColor, contentColor, disabledContentColor) {
        ButtonColors(
            color = color,
            disabledColor = disabledColor,
            contentColor = contentColor,
            disabledContentColor = disabledContentColor,
        )
    }

    /**
     * The [ButtonColors] for primary buttons.
     */
    @Composable
    fun buttonColorsPrimary(): ButtonColors {
        val color = YubeixTheme.colorScheme.primary
        val disabledColor = YubeixTheme.colorScheme.disabledPrimaryButton
        val contentColor = YubeixTheme.colorScheme.onPrimary
        val disabledContentColor = YubeixTheme.colorScheme.disabledOnPrimaryButton
        return remember(color, disabledColor, contentColor, disabledContentColor) {
            ButtonColors(
                color = color,
                disabledColor = disabledColor,
                contentColor = contentColor,
                disabledContentColor = disabledContentColor,
            )
        }
    }

    /**
     * The default [TextButtonColors] for all text buttons.
     */
    @Composable
    fun textButtonColors(
        color: Color = YubeixTheme.colorScheme.secondaryVariant,
        disabledColor: Color = YubeixTheme.colorScheme.disabledSecondaryVariant,
        textColor: Color = YubeixTheme.colorScheme.onSecondaryVariant,
        disabledTextColor: Color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
    ): TextButtonColors = remember(color, disabledColor, textColor, disabledTextColor) {
        TextButtonColors(
            color = color,
            disabledColor = disabledColor,
            textColor = textColor,
            disabledTextColor = disabledTextColor,
        )
    }

    /**
     * The [TextButtonColors] for primary text buttons.
     */
    @Composable
    fun textButtonColorsPrimary(): TextButtonColors {
        val color = YubeixTheme.colorScheme.primary
        val disabledColor = YubeixTheme.colorScheme.disabledPrimaryButton
        val textColor = YubeixTheme.colorScheme.onPrimary
        val disabledTextColor = YubeixTheme.colorScheme.disabledOnPrimaryButton
        return remember(color, disabledColor, textColor, disabledTextColor) {
            TextButtonColors(
                color = color,
                disabledColor = disabledColor,
                textColor = textColor,
                disabledTextColor = disabledTextColor,
            )
        }
    }
}

@Immutable
data class ButtonColors(
    val color: Color,
    val disabledColor: Color,
    val contentColor: Color,
    val disabledContentColor: Color,
)

@Immutable
data class TextButtonColors(
    val color: Color,
    val disabledColor: Color,
    val textColor: Color,
    val disabledTextColor: Color,
)
