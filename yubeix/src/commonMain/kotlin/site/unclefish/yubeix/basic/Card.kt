// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.theme.LocalContentColor
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape
import site.unclefish.yubeix.utils.ButtonHapticFeedback
import site.unclefish.yubeix.utils.PressFeedbackType
import site.unclefish.yubeix.utils.SinkFeedback
import site.unclefish.yubeix.utils.TiltFeedback
import site.unclefish.yubeix.utils.pressable

/**
 * A [Card] component with Yubeix style.
 * Card contain content and actions that relate information about a subject.
 *
 * This [Card] does not handle input events
 *
 * @param modifier The modifier to be applied to the [Card].
 * @param cornerRadius The corner radius of the [Card].
 * @param insideMargin The margin inside the [Card].
 * @param colors [CardColors] that will be used to resolve the color(s) used for the [Card].
 * @param content The [Composable] content of the [Card].
 */
@Composable
@NonRestartableComposable
fun Card(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    colors: CardColors = CardDefaults.defaultColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    BasicCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        colors = colors,
    ) {
        Column(
            modifier = Modifier.padding(insideMargin),
            content = content,
        )
    }
}

/**
 * A [Card] component with Yubeix style.
 * Card contain contain content and actions that relate information about a subject.
 *
 * This [Card] handles input events
 *
 * @param modifier The modifier to be applied to the [Card].
 * @param cornerRadius The corner radius of the [Card].
 * @param insideMargin The margin inside the [Card].
 * @param colors [CardColors] that will be used to resolve the color(s) used for the [Card].
 * @param pressFeedbackType The press feedback type of the [Card].
 * @param showIndication Whether to show indication of the [Card].
 * @param buttonHapticFeedbackEnabled Whether to play button haptics (press tick, delayed release
 *   confirm tick) while the [Card] is pressed.
 * @param onClick The callback to be invoked when the [Card] is clicked.
 * @param onLongPress The callback to be invoked when the [Card] is long pressed.
 * @param content The [Composable] content of the [Card].
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    colors: CardColors = CardDefaults.defaultColors(),
    pressFeedbackType: PressFeedbackType = PressFeedbackType.None,
    showIndication: Boolean = false,
    buttonHapticFeedbackEnabled: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)

    val pressFeedback = remember(pressFeedbackType) {
        when (pressFeedbackType) {
            PressFeedbackType.None -> null
            PressFeedbackType.Sink -> SinkFeedback()
            PressFeedbackType.Tilt -> TiltFeedback()
        }
    }

    val usedInteractionSource = if (pressFeedback != null) interactionSource else null
    val indicationToUse = if (showIndication) LocalIndication.current else null

    if (buttonHapticFeedbackEnabled) {
        ButtonHapticFeedback(
            interactionSource = interactionSource,
            enabled = onClick != null,
        )
    }

    BasicCard(
        modifier = modifier.pressable(
            interactionSource = usedInteractionSource,
            indication = pressFeedback,
            delay = null,
        ),
        cornerRadius = cornerRadius,
        colors = colors,
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = indicationToUse,
                    onClick = currentOnClick ?: EmptyClickAction,
                    onLongClick = currentOnLongPress,
                )
                .padding(insideMargin),
            content = content,
        )
    }
}

/**
 * A [BasicCard] component.
 *
 * @param modifier The modifier to be applied to the [BasicCard].
 * @param colors [CardColors] that will be used to resolve the color(s) used for the [BasicCard].
 * @param cornerRadius The corner radius of the [BasicCard].
 * @param content The [Composable] content of the [BasicCard].
 */
@Composable
private fun BasicCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.defaultColors(),
    cornerRadius: Dp = CardDefaults.CornerRadius,
    content: @Composable () -> Unit,
) {
    val shape = yubeixShape(cornerRadius)

    CompositionLocalProvider(
        LocalContentColor provides colors.contentColor,
    ) {
        Box(
            modifier = modifier
                .semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                }
                .drawWithCache {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    val path = when (outline) {
                        is Outline.Rectangle -> null
                        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
                        is Outline.Generic -> outline.path
                    }

                    onDrawWithContent {
                        if (path == null) {
                            drawRect(colors.color)
                            drawContent()
                        } else {
                            val contentDrawScope = this
                            drawPath(path = path, color = colors.color)
                            clipPath(path) {
                                contentDrawScope.drawContent()
                            }
                        }
                    }
                },
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}

private val EmptyClickAction: () -> Unit = {}

object CardDefaults {

    /**
     * The default corner radius of the [Card].
     */
    val CornerRadius = 16.dp

    /**
     * The default margin inside the [Card].
     */
    val InsideMargin = PaddingValues(0.dp)

    /**
     * The default colors width of the [Card].
     */
    @Composable
    fun defaultColors(
        color: Color = YubeixTheme.colorScheme.surfaceContainer,
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceContainer,
    ): CardColors = remember(color, contentColor) {
        CardColors(
            color = color,
            contentColor = contentColor,
        )
    }
}

@Immutable
data class CardColors(
    val color: Color,
    val contentColor: Color,
)
