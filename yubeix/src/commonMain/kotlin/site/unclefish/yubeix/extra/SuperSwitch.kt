// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.BasicComponentColors
import site.unclefish.yubeix.basic.BasicComponentDefaults
import site.unclefish.yubeix.basic.Switch
import site.unclefish.yubeix.basic.SwitchColors
import site.unclefish.yubeix.basic.SwitchDefaults

/**
 * A switch row with a title and a summary, styled after wordmoment's preference rows. The row
 * shows a trailing [Switch]; tapping the row toggles the switch only when [toggleOnRowClick] is
 * enabled.
 *
 * @param checked The checked state of the [SuperSwitch].
 * @param onCheckedChange The callback when the checked state of the [SuperSwitch] is changed.
 * @param title The title of the [SuperSwitch].
 * @param modifier The modifier to be applied to the [SuperSwitch].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperSwitch].
 * @param summaryColor The color of the summary.
 * @param startAction The [Composable] content that on the start side of the [SuperSwitch].
 * @param endActions The [Composable] content on the end side of the [SuperSwitch].
 * @param bottomAction The [Composable] content at the bottom of the [SuperSwitch].
 * @param switchColors The [SwitchColors] of the [SuperSwitch].
 * @param insideMargin The margin inside the [SuperSwitch]. Defaults to the adaptive preference row
 *   padding, which grows with a summary or a bottom action.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the [SuperSwitch] is clickable.
 * @param toggleOnRowClick Whether tapping anywhere on the row toggles the switch. When false,
 *   only the [Switch] itself is interactive.
 * @param minHeight The min height of the [SuperSwitch]. Defaults to the adaptive preference row
 *   min height.
 * @param selected Whether the [SuperSwitch] is highlighted as selected.
 * @param selectedShape The shape used to clip the selected highlight of the [SuperSwitch].
 */
@Composable
@NonRestartableComposable
fun SuperSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable RowScope.() -> Unit = {},
    bottomAction: (@Composable () -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.switchColors(),
    insideMargin: PaddingValues = SuperRowDefaults.resolvedItemPadding(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    holdDownState: Boolean = false,
    enabled: Boolean = true,
    toggleOnRowClick: Boolean = false,
    minHeight: Dp = SuperRowDefaults.resolvedMinHeight(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    selected: Boolean = false,
    selectedShape: Shape = SuperRowDefaults.SelectedShape,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
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
            SuperSwitchEndActions(
                checked = checked,
                onCheckedChange = currentOnCheckedChange,
                enabled = enabled,
                switchColors = switchColors,
            )
        },
        bottomAction = bottomAction,
        onClick = if (toggleOnRowClick) {
            { currentOnCheckedChange.takeIf { enabled }?.invoke(!checked) }
        } else {
            null
        },
        holdDownState = holdDownState,
        enabled = enabled,
        minHeight = minHeight,
    )
}

@Composable
private fun SuperSwitchEndActions(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    switchColors: SwitchColors,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = switchColors,
    )
}
