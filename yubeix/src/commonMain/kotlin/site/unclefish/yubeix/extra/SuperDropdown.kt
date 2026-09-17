// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.BasicComponentColors
import site.unclefish.yubeix.basic.BasicComponentDefaults
import site.unclefish.yubeix.basic.DropdownArrowEndAction
import site.unclefish.yubeix.basic.DropdownColors
import site.unclefish.yubeix.basic.DropdownDefaults
import site.unclefish.yubeix.basic.HorizontalDivider
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.ListPopupColumn
import site.unclefish.yubeix.basic.PopupPositionProvider
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.Checkmark
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * The size of the checkmark icon in the dropdown list popup.
 */
private val SuperDropdownPopupIconSize = 18.dp

/**
 * A dropdown row with a title and a summary, styled after wordmoment's preference rows. Selecting
 * an entry opens a list popup anchored to the row's end edge.
 *
 * @param items The options of the [SuperDropdown].
 * @param selectedIndex The index of the selected option.
 * @param title The title of the [SuperDropdown].
 * @param modifier The modifier to be applied to the [SuperDropdown].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperDropdown].
 * @param summaryColor The color of the summary.
 * @param dropdownColors The [DropdownColors] of the [SuperDropdown].
 * @param startAction The [Composable] content that on the start side of the [SuperDropdown].
 * @param bottomAction The [Composable] content at the bottom of the [SuperDropdown].
 * @param insideMargin The margin inside the [SuperDropdown]. Defaults to the adaptive preference
 *   row padding, which grows with a summary or a bottom action.
 * @param maxHeight The maximum height of the [SuperListPopup].
 * @param enabled Whether the [SuperDropdown] is enabled.
 * @param showValue Whether to show the selected value of the [SuperDropdown].
 * @param renderInRootScaffold Whether to render the popup in the root (outermost) Scaffold.
 *   When true (default), the popup covers the full screen. When false, it renders within the
 *   current Scaffold's bounds with position compensation.
 * @param onSelectedIndexChange The callback when the selected index of the [SuperDropdown] is changed.
 * @param minHeight The min height of the [SuperDropdown]. Defaults to the adaptive preference row
 *   min height.
 * @param actionColor The color of the selected value text and the arrow. When null, the color is
 *   resolved from the theme and the enabled state.
 * @param selected Whether the [SuperDropdown] is highlighted as selected.
 * @param selectedShape The shape used to clip the selected highlight of the [SuperDropdown].
 */
@Composable
fun SuperDropdown(
    items: List<String>,
    selectedIndex: Int,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    startAction: @Composable (() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = SuperRowDefaults.resolvedItemPadding(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    maxHeight: Dp? = null,
    enabled: Boolean = true,
    showValue: Boolean = true,
    renderInRootScaffold: Boolean = true,
    onSelectedIndexChange: ((Int) -> Unit)? = null,
    minHeight: Dp = SuperRowDefaults.resolvedMinHeight(
        hasSummary = !summary.isNullOrBlank(),
        hasBottomAction = bottomAction != null,
    ),
    actionColor: Color? = null,
    selected: Boolean = false,
    selectedShape: Shape = SuperRowDefaults.SelectedShape,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDropdownExpanded = remember { mutableStateOf(false) }
    val isHoldDown = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)

    val itemsNotEmpty = items.isNotEmpty()
    val actualEnabled = enabled && itemsNotEmpty

    val resolvedActionColor = when {
        !actualEnabled -> YubeixTheme.colorScheme.disabledOnSecondaryVariant
        actionColor != null -> actionColor
        else -> YubeixTheme.colorScheme.onSurfaceVariantActions
    }

    val handleClick = remember(actualEnabled) {
        {
            if (actualEnabled) {
                isDropdownExpanded.value = !isDropdownExpanded.value
                if (isDropdownExpanded.value) {
                    isHoldDown.value = true
                    currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
        }
    }

    val selectedModifier = Modifier.superRowSelectedModifier(selected, selectedShape)
    BasicComponent(
        modifier = modifier
            .then(selectedModifier),
        interactionSource = interactionSource,
        insideMargin = insideMargin,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        startAction = startAction,
        endActions = {
            if (showValue && itemsNotEmpty) {
                Text(
                    text = items[selectedIndex],
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .align(Alignment.CenterVertically),
                    style = YubeixTheme.textStyles.body2,
                    color = resolvedActionColor,
                )
            }
            DropdownArrowEndAction(
                actionColor = resolvedActionColor,
            )
            if (itemsNotEmpty) {
                SuperDropdownPopup(
                    items = items,
                    selectedIndex = selectedIndex,
                    isDropdownExpanded = isDropdownExpanded.value,
                    onDismiss = { isDropdownExpanded.value = false },
                    onDismissFinished = { isHoldDown.value = false },
                    maxHeight = maxHeight,
                    dropdownColors = dropdownColors,
                    hapticFeedback = hapticFeedback,
                    renderInRootScaffold = renderInRootScaffold,
                    onSelectedIndexChange = onSelectedIndexChange,
                )
            }
        },
        bottomAction = bottomAction,
        onClick = handleClick,
        holdDownState = isHoldDown.value,
        enabled = actualEnabled,
        minHeight = minHeight,
    )
}

@Composable
private fun SuperDropdownPopup(
    items: List<String>,
    selectedIndex: Int,
    isDropdownExpanded: Boolean,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
    maxHeight: Dp?,
    dropdownColors: DropdownColors,
    hapticFeedback: HapticFeedback,
    renderInRootScaffold: Boolean,
    onSelectedIndexChange: ((Int) -> Unit)?,
) {
    val onSelectState = rememberUpdatedState(onSelectedIndexChange)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
    val onItemSelected: (Int) -> Unit = remember {
        { selectedIdx ->
            currentHapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            onSelectState.value?.invoke(selectedIdx)
            currentOnDismiss()
        }
    }
    SuperListPopup(
        show = isDropdownExpanded,
        alignment = PopupPositionProvider.Align.End,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismissFinished,
        maxHeight = maxHeight,
        renderInRootScaffold = renderInRootScaffold,
    ) {
        ListPopupColumn {
            items.forEachIndexed { index, string ->
                key(index) {
                    SuperDropdownPopupItem(
                        text = string,
                        optionSize = items.size,
                        isSelected = selectedIndex == index,
                        index = index,
                        dropdownColors = dropdownColors,
                        onClick = { onItemSelected(index) },
                    )
                }
            }
        }
    }
}

/**
 * A dropdown popup item styled after wordmoment: a leading checkmark marks the selected entry and
 * a divider separates neighboring entries.
 */
@Composable
private fun SuperDropdownPopupItem(
    text: String,
    optionSize: Int,
    isSelected: Boolean,
    index: Int,
    dropdownColors: DropdownColors,
    onClick: () -> Unit,
) {
    val textColor = if (isSelected) dropdownColors.selectedContentColor else dropdownColors.contentColor
    val checkColor = if (isSelected) dropdownColors.selectedContentColor else Color.Transparent

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 14.dp, end = 20.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = CupertinoIcons.Outlined.Checkmark,
                contentDescription = null,
                tint = checkColor,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(SuperDropdownPopupIconSize),
            )
            Text(
                text = text,
                style = YubeixTheme.textStyles.body1,
                color = textColor,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
            )
        }
        if (index < optionSize - 1) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = YubeixTheme.colorScheme.dividerLine,
            )
        }
    }
}
