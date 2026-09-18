// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.basic.ArrowUpDown
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.Checkmark
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * The size of the checkmark icon in the dropdown and spinner popup entries.
 */
private val DropdownPopupIconSize = 18.dp

/**
 * The arrow shown at the end of a dropdown or spinner row. Shared with the Super row family, which
 * renders the same end action after the selected value text.
 */
@Composable
fun RowScope.DropdownArrowEndAction(
    actionColor: Color,
) {
    val colorFilter = remember(actionColor) { ColorFilter.tint(actionColor) }
    Image(
        modifier = Modifier
            .size(10.dp, 16.dp)
            .align(Alignment.CenterVertically),
        imageVector = YubeixIcons.Basic.ArrowUpDown,
        colorFilter = colorFilter,
        contentDescription = null,
    )
}

/**
 * The implementation of the dropdown popup entry, styled after wordmoment: a leading checkmark
 * marks the selected entry and a divider separates neighboring entries.
 *
 * @param text The text of the current option.
 * @param optionSize The size of the options.
 * @param isSelected Whether the option is selected.
 * @param index The index of the current option in the options.
 * @param dropdownColors The [DropdownColors] to resolve the entry and checkmark colors from.
 * @param onSelectedIndexChange The callback when the index is selected.
 */
@Composable
fun DropdownImpl(
    text: String,
    optionSize: Int,
    isSelected: Boolean,
    index: Int,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    onSelectedIndexChange: (Int) -> Unit,
) {
    val textColor = if (isSelected) dropdownColors.selectedContentColor else dropdownColors.contentColor
    val checkColor = if (isSelected) dropdownColors.selectedContentColor else Color.Transparent

    val currentOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { currentOnSelectedIndexChange(index) }
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
                    .size(DropdownPopupIconSize),
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

/**
 * The implementation of the spinner popup entry, styled after wordmoment: a leading checkmark
 * marks the selected entry and a divider separates neighboring entries. In dialog mode the entry
 * keeps the dialog scaffolding (taller rows, wider padding, a full-bleed highlight on the selected
 * entry) and drops the dividers.
 *
 * @param entry the [SpinnerEntry] to be shown in the spinner.
 * @param entryCount the count of the entries in the spinner.
 * @param isSelected whether the entry is selected.
 * @param index the index of the entry.
 * @param spinnerColors the [SpinnerColors] to resolve the entry and checkmark colors from.
 * @param dialogMode whether the spinner is in dialog mode.
 * @param onSelectedIndexChange the callback to be invoked when the selected index of the spinner is changed.
 */
@Composable
fun SpinnerItemImpl(
    entry: SpinnerEntry,
    entryCount: Int,
    isSelected: Boolean,
    index: Int,
    spinnerColors: SpinnerColors,
    dialogMode: Boolean = false,
    onSelectedIndexChange: (Int) -> Unit,
) {
    val (titleColor, summaryColor) = if (isSelected) {
        spinnerColors.selectedContentColor to spinnerColors.selectedSummaryColor
    } else {
        spinnerColors.contentColor to spinnerColors.summaryColor
    }
    val checkColor = if (isSelected) spinnerColors.selectedIndicatorColor else Color.Transparent
    val backgroundColor = if (isSelected) {
        spinnerColors.selectedContainerColor
    } else {
        spinnerColors.containerColor
    }

    val currentOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)
    Column(
        modifier = if (dialogMode) {
            Modifier.drawBehind { drawRect(backgroundColor) }
        } else {
            Modifier
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { currentOnSelectedIndexChange(index) }
                .then(
                    if (dialogMode) {
                        Modifier
                            .heightIn(min = 56.dp)
                            .widthIn(min = 200.dp)
                            .padding(horizontal = 28.dp)
                    } else {
                        Modifier.padding(start = 14.dp, end = 20.dp)
                    },
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = CupertinoIcons.Outlined.Checkmark,
                contentDescription = null,
                tint = checkColor,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(DropdownPopupIconSize),
            )
            entry.icon?.let {
                it(Modifier.sizeIn(minWidth = 26.dp, minHeight = 26.dp).padding(end = 12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                entry.title?.let {
                    Text(
                        text = it,
                        style = YubeixTheme.textStyles.body1,
                        color = titleColor,
                    )
                }
                entry.summary?.let {
                    Text(
                        text = it,
                        style = YubeixTheme.textStyles.body2,
                        color = summaryColor,
                    )
                }
            }
        }
        if (!dialogMode && index < entryCount - 1) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = YubeixTheme.colorScheme.dividerLine,
            )
        }
    }
}

@Immutable
data class DropdownColors(
    val contentColor: Color,
    val containerColor: Color,
    val selectedContentColor: Color,
    val selectedContainerColor: Color,
)

object DropdownDefaults {

    @Composable
    fun dropdownColors(
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceContainer,
        containerColor: Color = YubeixTheme.colorScheme.surfaceContainer,
        selectedContentColor: Color = YubeixTheme.colorScheme.primary,
        selectedContainerColor: Color = YubeixTheme.colorScheme.surfaceContainer,
    ): DropdownColors = remember(contentColor, containerColor, selectedContentColor, selectedContainerColor) {
        DropdownColors(
            contentColor = contentColor,
            containerColor = containerColor,
            selectedContentColor = selectedContentColor,
            selectedContainerColor = selectedContainerColor,
        )
    }
}

object SpinnerDefaults {
    @Composable
    fun spinnerColors(
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceContainer,
        summaryColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        containerColor: Color = YubeixTheme.colorScheme.surfaceContainer,
        selectedContentColor: Color = YubeixTheme.colorScheme.primary,
        selectedSummaryColor: Color = YubeixTheme.colorScheme.primary,
        selectedContainerColor: Color = YubeixTheme.colorScheme.surfaceContainer,
        selectedIndicatorColor: Color = YubeixTheme.colorScheme.primary,
    ): SpinnerColors = remember(
        contentColor,
        summaryColor,
        containerColor,
        selectedContentColor,
        selectedSummaryColor,
        selectedContainerColor,
        selectedIndicatorColor,
    ) {
        SpinnerColors(
            contentColor = contentColor,
            summaryColor = summaryColor,
            containerColor = containerColor,
            selectedContentColor = selectedContentColor,
            selectedSummaryColor = selectedSummaryColor,
            selectedContainerColor = selectedContainerColor,
            selectedIndicatorColor = selectedIndicatorColor,
        )
    }

    @Composable
    fun dialogSpinnerColors(
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceContainer,
        summaryColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        containerColor: Color = Color.Transparent,
        selectedContentColor: Color = YubeixTheme.colorScheme.onTertiaryContainer,
        selectedSummaryColor: Color = YubeixTheme.colorScheme.onTertiaryContainer,
        selectedContainerColor: Color = YubeixTheme.colorScheme.tertiaryContainer,
        selectedIndicatorColor: Color = YubeixTheme.colorScheme.onTertiaryContainer,
    ): SpinnerColors = remember(
        contentColor,
        summaryColor,
        containerColor,
        selectedContentColor,
        selectedSummaryColor,
        selectedContainerColor,
        selectedIndicatorColor,
    ) {
        SpinnerColors(
            contentColor = contentColor,
            summaryColor = summaryColor,
            containerColor = containerColor,
            selectedContentColor = selectedContentColor,
            selectedSummaryColor = selectedSummaryColor,
            selectedContainerColor = selectedContainerColor,
            selectedIndicatorColor = selectedIndicatorColor,
        )
    }
}

@Immutable
data class SpinnerColors(
    val contentColor: Color,
    val summaryColor: Color,
    val containerColor: Color,
    val selectedContentColor: Color,
    val selectedSummaryColor: Color,
    val selectedContainerColor: Color,
    val selectedIndicatorColor: Color,
)

/**
 * The spinner entry.
 */
@Immutable
data class SpinnerEntry(
    val icon: @Composable ((Modifier) -> Unit)? = null,
    val title: String? = null,
    val summary: String? = null,
)
