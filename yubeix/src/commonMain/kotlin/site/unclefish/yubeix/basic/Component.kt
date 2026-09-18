// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.interfaces.HoldDownInteraction
import site.unclefish.yubeix.theme.YubeixTheme

// Spacing between the center content and the start/end action slots, matching the preference row
// primitive this component is ported from.
private val BASIC_COMPONENT_ACTION_SPACING = 8.dp

// Spacing between the main content and the bottom action slot.
private val BASIC_COMPONENT_BOTTOM_ACTION_SPACING = 8.dp

/**
 * A basic component with Yubeix style, rendering as a preference row. Widely used in other
 * extension components.
 *
 * The row follows the adaptive preference row metrics: the min height and the inside margin grow
 * with the presence of a summary and a bottom action. While the row is pressed it sinks slightly
 * (the shared row-family press feedback), and [holdDownState] keeps it visually held down.
 *
 * @param modifier The modifier to be applied to the [BasicComponent].
 * @param title The title of the [BasicComponent].
 * @param titleColor The color of the title.
 * @param summary The summary of the [BasicComponent].
 * @param summaryColor The color of the summary.
 * @param startAction The [Composable] content that on the start side of the [BasicComponent].
 * @param endActions The [Composable] content on the end side of the [BasicComponent].
 * @param bottomAction The [Composable] content at the bottom of the [BasicComponent].
 * @param insideMargin The margin inside the [BasicComponent]. Defaults to the adaptive preference
 *   row padding, which grows with a summary or a bottom action.
 * @param minHeight The min height of the [BasicComponent]. Defaults to the adaptive preference row
 *   min height.
 * @param onClick The callback when the [BasicComponent] is clicked.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the [BasicComponent] is enabled.
 * @param interactionSource The [MutableInteractionSource] for the [BasicComponent].
 */
@Composable
@NonRestartableComposable
fun BasicComponent(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable (RowScope.() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = BasicComponentDefaults.resolvedInsideMargin(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    minHeight: Dp = BasicComponentDefaults.resolvedMinHeight(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    BasicComponent(
        startAction = startAction,
        endActions = endActions,
        bottomAction = bottomAction,
        modifier = modifier,
        insideMargin = insideMargin,
        minHeight = minHeight,
        onClick = onClick,
        holdDownState = holdDownState,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        if (title != null) {
            Text(
                text = title,
                style = YubeixTheme.textStyles.body1,
                color = titleColor.color(enabled),
            )
        }
        if (summary != null) {
            Text(
                text = summary,
                style = YubeixTheme.textStyles.footnote1,
                color = summaryColor.color(enabled),
            )
        }
    }
}

/**
 * A basic component with Yubeix style, rendering as a preference row. Widely used in other
 * extension components.
 *
 * The row follows the adaptive preference row metrics: the min height and the inside margin grow
 * with the presence of a bottom action. While the row is pressed it sinks slightly (the shared
 * row-family press feedback), and [holdDownState] keeps it visually held down.
 *
 * @param modifier The modifier to be applied to the [BasicComponent].
 * @param startAction The [Composable] content that on the start side of the [BasicComponent].
 * @param endActions The [Composable] content on the end side of the [BasicComponent].
 * @param bottomAction The [Composable] content at the bottom of the [BasicComponent].
 * @param insideMargin The margin inside the [BasicComponent]. Defaults to the adaptive preference
 *   row padding, which grows with a bottom action.
 * @param minHeight The min height of the [BasicComponent]. Defaults to the adaptive preference row
 *   min height.
 * @param onClick The callback when the [BasicComponent] is clicked.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the [BasicComponent] is enabled.
 * @param interactionSource The [MutableInteractionSource] for the [BasicComponent].
 * @param content The content of the [BasicComponent].
 */
@Composable
fun BasicComponent(
    modifier: Modifier = Modifier,
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable (RowScope.() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = BasicComponentDefaults.resolvedInsideMargin(
        hasSummary = false,
        hasBottomAction = bottomAction != null,
    ),
    minHeight: Dp = BasicComponentDefaults.resolvedMinHeight(
        hasSummary = false,
        hasBottomAction = bottomAction != null,
    ),
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)

    val holdDown = remember { mutableStateOf<HoldDownInteraction.HoldDown?>(null) }
    LaunchedEffect(holdDownState, interactionSource) {
        suspend fun releaseHoldDown() {
            holdDown.value?.let { oldValue ->
                interactionSource.emit(HoldDownInteraction.Release(oldValue))
                holdDown.value = null
            }
        }
        if (holdDownState) {
            releaseHoldDown()
            val interaction = HoldDownInteraction.HoldDown()
            holdDown.value = interaction
            interactionSource.emit(interaction)
        } else {
            releaseHoldDown()
        }
    }

    val hasOnClick = onClick != null
    val clickableModifier = remember(enabled, hasOnClick, interactionSource) {
        if (enabled && hasOnClick) {
            Modifier.clickable(
                interactionSource = interactionSource,
                onClick = { currentOnClick?.invoke() },
            )
        } else {
            Modifier
        }
    }
    Column(
        modifier = modifier
            .heightIn(min = minHeight)
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(insideMargin),
        verticalArrangement = Arrangement.Center,
    ) {
        if (startAction == null && endActions == null) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                content = content,
            )
        } else {
            Layout(
                content = {
                    startAction?.let {
                        Column(
                            modifier = Modifier.layoutId("start"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                        ) { it() }
                    }
                    Column(
                        modifier = Modifier.layoutId("center"),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        content = content,
                    )
                    endActions?.let {
                        Column(
                            modifier = Modifier.layoutId("end"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) { it() }
                        }
                    }
                },
            ) { measurables, constraints ->
                val spacerPx = BASIC_COMPONENT_ACTION_SPACING.roundToPx()

                val startMeasurable = measurables.firstOrNull { it.layoutId == "start" }
                val centerMeasurable = measurables.first { it.layoutId == "center" }
                val endMeasurable = measurables.firstOrNull { it.layoutId == "end" }

                val maxWidth = constraints.maxWidth
                val maxHeight = constraints.maxHeight

                val hasStart = startMeasurable != null
                val hasEnd = endMeasurable != null
                val startSpacerWidth = if (hasStart) spacerPx else 0
                val endSpacerWidth = if (hasEnd) spacerPx else 0
                val availableWidth = (maxWidth - startSpacerWidth - endSpacerWidth).coerceAtLeast(0)

                val reqStart = startMeasurable?.maxIntrinsicWidth(maxHeight) ?: 0
                val reqCenter = centerMeasurable.maxIntrinsicWidth(maxHeight)
                val reqEnd = endMeasurable?.maxIntrinsicWidth(maxHeight) ?: 0

                val totalReq = reqStart + reqCenter + reqEnd

                var targetStart = 0
                var targetCenter = 0
                var targetEnd = 0

                if (totalReq <= availableWidth) {
                    targetStart = reqStart
                    targetEnd = reqEnd
                    targetCenter = (availableWidth - reqStart - reqEnd).coerceAtLeast(0)
                } else {
                    val minStart = startMeasurable?.minIntrinsicWidth(maxHeight) ?: 0
                    val minCenter = centerMeasurable.minIntrinsicWidth(maxHeight)
                    val minEnd = endMeasurable?.minIntrinsicWidth(maxHeight) ?: 0

                    val wStart = if (hasStart) 2 else 0
                    val wCenter = 5
                    val wEnd = if (hasEnd) 3 else 0
                    val totalWeight = wStart + wCenter + wEnd

                    if (totalWeight > 0) {
                        val baseStart = (availableWidth.toLong() * wStart / totalWeight).toInt()
                        val baseCenter = (availableWidth.toLong() * wCenter / totalWeight).toInt()
                        val baseEnd = (availableWidth.toLong() * wEnd / totalWeight).toInt()

                        val isStartHuge = hasStart && reqStart > baseStart
                        val isCenterHuge = reqCenter > baseCenter
                        val isEndHuge = hasEnd && reqEnd > baseEnd

                        val hugeCount = (if (isStartHuge) 1 else 0) +
                            (if (isCenterHuge) 1 else 0) +
                            (if (isEndHuge) 1 else 0)

                        when (hugeCount) {
                            3 -> {
                                targetStart = baseStart
                                targetCenter = baseCenter
                                targetEnd = baseEnd
                                val used = targetStart + targetCenter + targetEnd
                                if (used < availableWidth) {
                                    targetCenter += (availableWidth - used)
                                }
                            }

                            2 -> {
                                var remWidth = availableWidth
                                if (!isStartHuge) {
                                    targetStart = reqStart
                                    remWidth -= targetStart
                                }
                                if (!isCenterHuge) {
                                    targetCenter = reqCenter
                                    remWidth -= targetCenter
                                }
                                if (!isEndHuge) {
                                    targetEnd = reqEnd
                                    remWidth -= targetEnd
                                }

                                val hugeSum = (if (isStartHuge) reqStart else 0) +
                                    (if (isCenterHuge) reqCenter else 0) +
                                    (if (isEndHuge) reqEnd else 0)

                                if (hugeSum > 0) {
                                    if (isStartHuge) targetStart = (remWidth.toLong() * reqStart / hugeSum).toInt()
                                    if (isCenterHuge) targetCenter = (remWidth.toLong() * reqCenter / hugeSum).toInt()
                                    if (isEndHuge) targetEnd = (remWidth.toLong() * reqEnd / hugeSum).toInt()
                                }
                            }

                            1 -> {
                                var remWidth = availableWidth
                                if (!isStartHuge) {
                                    targetStart = reqStart
                                    remWidth -= targetStart
                                }
                                if (!isCenterHuge) {
                                    targetCenter = reqCenter
                                    remWidth -= targetCenter
                                }
                                if (!isEndHuge) {
                                    targetEnd = reqEnd
                                    remWidth -= targetEnd
                                }

                                if (isStartHuge) targetStart = remWidth
                                if (isCenterHuge) targetCenter = remWidth
                                if (isEndHuge) targetEnd = remWidth
                            }

                            else -> {
                                targetStart = reqStart
                                targetCenter = reqCenter
                                targetEnd = reqEnd
                                val used = targetStart + targetCenter + targetEnd
                                if (used < availableWidth) {
                                    targetCenter += (availableWidth - used)
                                }
                            }
                        }
                    } else {
                        targetCenter = availableWidth
                    }

                    if (minCenter in (targetCenter + 1)..availableWidth) {
                        val need = minCenter - targetCenter
                        val startSlack = (targetStart - minStart).coerceAtLeast(0)
                        val endSlack = (targetEnd - minEnd).coerceAtLeast(0)
                        val totalSlack = startSlack + endSlack

                        if (totalSlack > 0) {
                            val useStart = (need.toLong() * startSlack / totalSlack).toInt().coerceAtMost(startSlack)
                            val useEnd = (need - useStart).coerceAtLeast(0).coerceAtMost(endSlack)
                            targetStart -= useStart
                            targetEnd -= useEnd
                            targetCenter += useStart + useEnd
                        }
                    }

                    if (minEnd in (targetEnd + 1)..availableWidth) {
                        val need = minEnd - targetEnd
                        val startSlack = (targetStart - minStart).coerceAtLeast(0)
                        val centerSlack = (targetCenter - minCenter).coerceAtLeast(0)
                        val totalSlack = startSlack + centerSlack

                        if (totalSlack > 0) {
                            val useStart = (need.toLong() * startSlack / totalSlack).toInt().coerceAtMost(startSlack)
                            val useCenter = (need - useStart).coerceAtLeast(0).coerceAtMost(centerSlack)
                            targetStart -= useStart
                            targetCenter -= useCenter
                            targetEnd += useStart + useCenter
                        }
                    }
                }

                val startPlaceable = startMeasurable?.measure(
                    constraints.copy(minWidth = 0, maxWidth = targetStart),
                )
                val startWidth = startPlaceable?.width ?: 0
                val startHeight = startPlaceable?.height ?: 0

                val centerPlaceable = centerMeasurable.measure(
                    constraints.copy(minWidth = 0, maxWidth = targetCenter),
                )

                val endPlaceable = endMeasurable?.measure(
                    constraints.copy(minWidth = 0, maxWidth = targetEnd),
                )
                val endHeight = endPlaceable?.height ?: 0

                val rowHeight = maxOf(startHeight, centerPlaceable.height, endHeight)
                val layoutHeight = rowHeight
                    .coerceIn(constraints.minHeight, maxHeight.takeIf { it != Constraints.Infinity } ?: rowHeight)

                layout(width = maxWidth, height = layoutHeight) {
                    val startTop = (rowHeight - startHeight).coerceAtLeast(0) / 2
                    val centerTop = (rowHeight - centerPlaceable.height) / 2
                    val endTop = (rowHeight - endHeight).coerceAtLeast(0) / 2

                    startPlaceable?.placeRelative(0, startTop)

                    val centerX = startWidth + startSpacerWidth
                    centerPlaceable.placeRelative(centerX, centerTop)

                    endPlaceable?.let {
                        val endX = maxWidth - it.width
                        it.placeRelative(endX, endTop)
                    }
                }
            }
        }

        if (bottomAction != null) {
            Spacer(modifier = Modifier.height(BASIC_COMPONENT_BOTTOM_ACTION_SPACING))
            bottomAction()
        }
    }
}

/**
 * Default values used by [BasicComponent], mirroring the adaptive preference row metrics of
 * [site.unclefish.yubeix.extra.SuperRowDefaults]: a row with a summary grows taller with vertical
 * padding, and a row with a bottom action (e.g. a slider) grows taller still.
 */
object BasicComponentDefaults {

    /**
     * The default margin inside a title-only [BasicComponent].
     */
    val InsideMargin = PaddingValues(horizontal = 16.dp, vertical = 0.dp)

    /**
     * The default margin inside a [BasicComponent] with a summary.
     */
    val SummaryInsideMargin = PaddingValues(horizontal = 16.dp, vertical = 6.dp)

    /**
     * The default margin inside a [BasicComponent] with a bottom action.
     */
    val SliderInsideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    /**
     * The default min height of a title-only [BasicComponent].
     */
    val MinHeight = 48.dp

    /**
     * The default min height of a [BasicComponent] with a summary.
     */
    val SummaryMinHeight = 64.dp

    /**
     * The default min height of a [BasicComponent] with a bottom action.
     */
    val SliderMinHeight = 76.dp

    /**
     * Resolves the row padding from the presence of a summary and a bottom action.
     */
    fun resolvedInsideMargin(
        hasSummary: Boolean,
        hasBottomAction: Boolean,
    ): PaddingValues = when {
        hasBottomAction -> SliderInsideMargin
        hasSummary -> SummaryInsideMargin
        else -> InsideMargin
    }

    /**
     * Resolves the row min height from the presence of a summary and a bottom action.
     */
    fun resolvedMinHeight(
        hasSummary: Boolean,
        hasBottomAction: Boolean,
    ): Dp = when {
        hasBottomAction -> SliderMinHeight
        hasSummary -> SummaryMinHeight
        else -> MinHeight
    }

    /**
     * The default color of the title.
     */
    @Composable
    fun titleColor(
        color: Color = YubeixTheme.colorScheme.onBackground,
        disabledColor: Color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
    ): BasicComponentColors = remember(color, disabledColor) {
        BasicComponentColors(
            color = color,
            disabledColor = disabledColor,
        )
    }

    /**
     * The default color of the summary.
     */
    @Composable
    fun summaryColor(
        color: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        disabledColor: Color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
    ): BasicComponentColors = remember(color, disabledColor) {
        BasicComponentColors(
            color = color,
            disabledColor = disabledColor,
        )
    }
}

@Immutable
data class BasicComponentColors(
    val color: Color,
    val disabledColor: Color,
) {
    @Stable
    internal fun color(enabled: Boolean): Color = if (enabled) color else disabledColor
}
