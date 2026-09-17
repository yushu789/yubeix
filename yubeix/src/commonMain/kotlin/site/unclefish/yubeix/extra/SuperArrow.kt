// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedCornerStyle
import com.kyant.shapes.RoundedRectangle
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.BasicComponentColors
import site.unclefish.yubeix.basic.BasicComponentDefaults
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.ChevronBackward
import site.unclefish.yubeix.icon.cupertino.outlined.ChevronForward
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * Default metrics for preference rows, ported from wordmoment's preference row components.
 *
 * Rows resolve their padding and min height from the presence of a summary and a bottom action,
 * mirroring the source behavior: a summary row grows taller with vertical padding, and a row with
 * a bottom action (e.g. a slider) grows taller still.
 */
object SuperRowDefaults {

    /**
     * The default min height of a title-only preference row.
     */
    val MinHeight = 48.dp

    /**
     * The default min height of a preference row with a summary.
     */
    val SummaryMinHeight = 64.dp

    /**
     * The default min height of a preference row with a bottom action.
     */
    val SliderMinHeight = 76.dp

    /**
     * The default padding inside a title-only preference row.
     */
    val ItemPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)

    /**
     * The default padding inside a preference row with a summary.
     */
    val SummaryItemPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)

    /**
     * The default padding inside a preference row with a bottom action.
     */
    val SliderItemPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    /**
     * The default corner radius of a preference group card and of the selected row highlight.
     */
    val CardCornerRadius = 12.dp

    /**
     * The default shape used to clip and highlight a selected preference row.
     */
    val SelectedShape: Shape = RoundedRectangle(CardCornerRadius, style = RoundedCornerStyle.Continuous)

    /**
     * Resolves the row padding from the presence of a summary and a bottom action.
     */
    fun resolvedItemPadding(
        hasSummary: Boolean,
        hasBottomAction: Boolean,
    ): PaddingValues = when {
        hasBottomAction -> SliderItemPadding
        hasSummary -> SummaryItemPadding
        else -> ItemPadding
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
}

/**
 * Applies the selected-row highlight: clips to [selectedShape] and fills the background with the
 * theme's high-emphasis surface container color. A no-op when [selected] is false.
 */
@Composable
internal fun Modifier.superRowSelectedModifier(
    selected: Boolean,
    selectedShape: Shape,
): Modifier = if (selected) {
    clip(selectedShape).background(YubeixTheme.colorScheme.surfaceContainerHigh)
} else {
    this
}

/**
 * The trailing forward chevron used by preference rows. Points backward in right-to-left layouts.
 *
 * @param modifier The modifier to be applied to the icon.
 * @param tint The tint color of the icon.
 */
@Composable
fun PreferenceChevronForwardIcon(
    modifier: Modifier = Modifier,
    tint: Color = YubeixTheme.colorScheme.onSurfaceVariantActions,
) {
    val icon = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        CupertinoIcons.Outlined.ChevronBackward
    } else {
        CupertinoIcons.Outlined.ChevronForward
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(14.dp),
    )
}

/**
 * An arrow row with a title and a summary, styled after wordmoment's preference rows. The row
 * always shows a trailing forward chevron.
 *
 * @param title The title of the [SuperArrow].
 * @param modifier The modifier to be applied to the [SuperArrow].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperArrow].
 * @param summaryColor The color of the summary.
 * @param startAction The [Composable] content that on the start side of the [SuperArrow].
 * @param endActions The [Composable] content on the end side of the [SuperArrow].
 * @param bottomAction The [Composable] content at the bottom of the [SuperArrow].
 * @param insideMargin The margin inside the [SuperArrow]. Defaults to the adaptive preference row
 *   padding, which grows with a summary or a bottom action.
 * @param onClick The callback when the [SuperArrow] is clicked.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the [SuperArrow] is clickable.
 * @param minHeight The min height of the [SuperArrow]. Defaults to the adaptive preference row
 *   min height.
 * @param selected Whether the [SuperArrow] is highlighted as selected.
 * @param selectedShape The shape used to clip the selected highlight of the [SuperArrow].
 */
@Composable
@NonRestartableComposable
fun SuperArrow(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable RowScope.() -> Unit = {},
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = SuperRowDefaults.resolvedItemPadding(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true,
    minHeight: Dp = SuperRowDefaults.resolvedMinHeight(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    selected: Boolean = false,
    selectedShape: Shape = SuperRowDefaults.SelectedShape,
) {
    val selectedModifier = Modifier.superRowSelectedModifier(selected, selectedShape)
    BasicComponent(
        modifier = modifier
            .then(selectedModifier),
        insideMargin = insideMargin,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        startAction = startAction,
        endActions = {
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f, fill = false),
            ) {
                endActions()
            }
            SuperArrowEndAction(
                enabled = enabled,
            )
        },
        bottomAction = bottomAction,
        onClick = onClick,
        holdDownState = holdDownState,
        enabled = enabled,
        minHeight = minHeight,
    )
}

@Composable
private fun RowScope.SuperArrowEndAction(
    enabled: Boolean,
) {
    val actionColors = SuperArrowDefaults.endActionColors()
    PreferenceChevronForwardIcon(
        tint = actionColors.color(enabled = enabled),
        modifier = Modifier.align(Alignment.CenterVertically),
    )
}

object SuperArrowDefaults {
    /**
     * The default color of the arrow.
     */
    @Composable
    fun endActionColors(): EndActionColors {
        val color = YubeixTheme.colorScheme.onSurfaceVariantActions
        val disabledColor = YubeixTheme.colorScheme.disabledOnSecondaryVariant
        return remember(color, disabledColor) {
            EndActionColors(
                color = color,
                disabledColor = disabledColor,
            )
        }
    }
}

@Immutable
data class EndActionColors(
    private val color: Color,
    private val disabledColor: Color,
) {
    @Stable
    internal fun color(enabled: Boolean): Color = if (enabled) color else disabledColor
}
