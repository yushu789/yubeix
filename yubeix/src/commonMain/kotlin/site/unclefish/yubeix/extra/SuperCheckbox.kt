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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.BasicComponentColors
import site.unclefish.yubeix.basic.BasicComponentDefaults
import site.unclefish.yubeix.basic.Checkbox
import site.unclefish.yubeix.basic.CheckboxColors
import site.unclefish.yubeix.basic.CheckboxDefaults

/**
 * A checkbox row with a title and a summary, styled after wordmoment's preference rows.
 *
 * @param title The title of the [SuperCheckbox].
 * @param checked The checked state of the [SuperCheckbox].
 * @param onCheckedChange The callback when the checked state of the [SuperCheckbox] is changed.
 * @param modifier The modifier to be applied to the [SuperCheckbox].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperCheckbox].
 * @param summaryColor The color of the summary.
 * @param checkboxColors The [CheckboxColors] of the [SuperCheckbox].
 * @param endActions The [Composable] content that on the end side of the [SuperCheckbox].
 * @param checkboxLocation The location of checkbox, [CheckboxLocation.Start] or [CheckboxLocation.End].
 * @param bottomAction The [Composable] content at the bottom of the [SuperCheckbox].
 * @param insideMargin The margin inside the [SuperCheckbox]. Defaults to the adaptive preference
 *   row padding, which grows with a summary or a bottom action.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the [SuperCheckbox] is clickable.
 * @param minHeight The min height of the [SuperCheckbox]. Defaults to the adaptive preference row
 *   min height.
 * @param selected Whether the [SuperCheckbox] is highlighted as selected.
 * @param selectedShape The shape used to clip the selected highlight of the [SuperCheckbox].
 */
@Composable
@NonRestartableComposable
fun SuperCheckbox(
    title: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    checkboxColors: CheckboxColors = CheckboxDefaults.checkboxColors(),
    endActions: @Composable RowScope.() -> Unit = {},
    checkboxLocation: CheckboxLocation = CheckboxLocation.Start,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = SuperRowDefaults.resolvedItemPadding(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    holdDownState: Boolean = false,
    enabled: Boolean = true,
    minHeight: Dp = SuperRowDefaults.resolvedMinHeight(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    selected: Boolean = false,
    selectedShape: Shape = SuperRowDefaults.SelectedShape,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val selectedModifier = Modifier.superRowSelectedModifier(selected, selectedShape)
    val startAction = if (checkboxLocation == CheckboxLocation.Start) {
        @Composable {
            SuperCheckboxStartAction(
                checked = checked,
                onCheckedChange = currentOnCheckedChange,
                enabled = enabled,
                checkboxColors = checkboxColors,
            )
        }
    } else {
        null
    }

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
            if (checkboxLocation == CheckboxLocation.End) {
                SuperCheckboxEndAction(
                    checked = checked,
                    onCheckedChange = currentOnCheckedChange,
                    enabled = enabled,
                    checkboxColors = checkboxColors,
                )
            }
        },
        bottomAction = bottomAction,
        onClick = {
            currentOnCheckedChange.takeIf { enabled }?.invoke(!checked)
        },
        holdDownState = holdDownState,
        enabled = enabled,
        minHeight = minHeight,
    )
}

@Composable
private fun SuperCheckboxStartAction(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    checkboxColors: CheckboxColors,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val currentChecked by rememberUpdatedState(checked)
    val onClick = remember(onCheckedChange != null) {
        if (onCheckedChange != null) {
            { currentOnCheckedChange?.invoke(!currentChecked) ?: Unit }
        } else {
            null
        }
    }
    Checkbox(
        modifier = Modifier
            .padding(end = 8.dp),
        state = ToggleableState(checked),
        onClick = onClick,
        enabled = enabled,
        colors = checkboxColors,
    )
}

@Composable
private fun SuperCheckboxEndAction(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    checkboxColors: CheckboxColors,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val currentChecked by rememberUpdatedState(checked)
    val onClick = remember(onCheckedChange != null) {
        if (onCheckedChange != null) {
            { currentOnCheckedChange?.invoke(!currentChecked) ?: Unit }
        } else {
            null
        }
    }
    Checkbox(
        state = ToggleableState(checked),
        onClick = onClick,
        enabled = enabled,
        colors = checkboxColors,
    )
}

enum class CheckboxLocation {
    Start,
    End,
}
