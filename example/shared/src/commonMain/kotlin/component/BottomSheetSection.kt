// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.CardDefaults
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.IconButton
import site.unclefish.yubeix.basic.Slider
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.TextField
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.extra.SuperBottomSheet
import site.unclefish.yubeix.extra.SuperDropdown
import site.unclefish.yubeix.extra.SuperSwitch
import site.unclefish.yubeix.extra.WindowBottomSheet
import site.unclefish.yubeix.extra.WindowDropdown
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Close
import site.unclefish.yubeix.icon.extended.Ok
import site.unclefish.yubeix.theme.LocalDismissState
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.utils.overScrollVertical
import site.unclefish.yubeix.utils.scrollEndHaptic

private val BottomSheetDropdownOptions = listOf("Option 1", "Option 2")

fun LazyListScope.bottomSheetSection() {
    item(key = "bottomSheet") {
        var showSuperBottomSheet by remember { mutableStateOf(false) }
        var showWindowBottomSheet by remember { mutableStateOf(false) }
        var superBottomSheetHoldDown by remember { mutableStateOf(false) }
        var windowBottomSheetHoldDown by remember { mutableStateOf(false) }
        var bottomSheetDropdownSelectedOption by remember { mutableIntStateOf(0) }
        var bottomSheetSuperSwitchState by remember { mutableStateOf(true) }

        SmallTitle(text = "BottomSheet")
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            SuperArrow(
                title = "SuperBottomSheet",
                summary = "Click to show a SuperBottomSheet",
                onClick = {
                    showSuperBottomSheet = true
                    superBottomSheetHoldDown = true
                },
                holdDownState = superBottomSheetHoldDown,
            )
            SuperArrow(
                title = "WindowBottomSheet",
                summary = "Click to show a WindowBottomSheet",
                onClick = {
                    showWindowBottomSheet = true
                    windowBottomSheetHoldDown = true
                },
                holdDownState = windowBottomSheetHoldDown,
            )
        }

        SuperBottomSheetDemo(
            show = showSuperBottomSheet,
            onDismissRequest = { showSuperBottomSheet = false },
            dropdownSelectedIndex = bottomSheetDropdownSelectedOption,
            onDropdownSelectedIndexChange = { bottomSheetDropdownSelectedOption = it },
            switchChecked = bottomSheetSuperSwitchState,
            onSwitchCheckedChange = { bottomSheetSuperSwitchState = it },
            onDismissFinished = { superBottomSheetHoldDown = false },
        )
        WindowBottomSheetDemo(
            show = showWindowBottomSheet,
            onDismissRequest = { showWindowBottomSheet = false },
            dropdownSelectedIndex = bottomSheetDropdownSelectedOption,
            onDropdownSelectedIndexChange = { bottomSheetDropdownSelectedOption = it },
            switchChecked = bottomSheetSuperSwitchState,
            onSwitchCheckedChange = { bottomSheetSuperSwitchState = it },
            onDismissFinished = { windowBottomSheetHoldDown = false },
        )
    }
}

@Composable
private fun SuperBottomSheetDemo(
    show: Boolean,
    onDismissRequest: () -> Unit,
    dropdownSelectedIndex: Int,
    onDropdownSelectedIndexChange: (Int) -> Unit,
    switchChecked: Boolean,
    onSwitchCheckedChange: (Boolean) -> Unit,
    onDismissFinished: () -> Unit,
) {
    var allowDismiss by remember { mutableStateOf(true) }
    var enableNestedScroll by remember { mutableStateOf(true) }

    SuperBottomSheet(
        title = "SuperBottomSheet",
        show = show,
        allowDismiss = allowDismiss,
        enableNestedScroll = enableNestedScroll,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        startAction = {
            IconButton(
                onClick = onDismissRequest,
            ) {
                Icon(
                    imageVector = YubeixIcons.Close,
                    contentDescription = "Cancel",
                    tint = YubeixTheme.colorScheme.onBackground,
                )
            }
        },
        endAction = {
            IconButton(
                onClick = onDismissRequest,
            ) {
                Icon(
                    imageVector = YubeixIcons.Ok,
                    contentDescription = "Confirm",
                    tint = YubeixTheme.colorScheme.onBackground,
                )
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .scrollEndHaptic()
                .overScrollVertical(),
        ) {
            item {
                SmallTitle(text = "Behavior Settings", insideMargin = PaddingValues(16.dp, 8.dp))
                Card(
                    modifier = Modifier.padding(bottom = 12.dp),
                    colors = CardDefaults.defaultColors(
                        color = YubeixTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    SuperSwitch(
                        title = "Allow Dismiss",
                        summary = "Drag or Back to dismiss",
                        checked = allowDismiss,
                        onCheckedChange = { allowDismiss = it },
                    )
                    SuperSwitch(
                        title = "Enable NestedScroll",
                        summary = "Scroll content vs Drag sheet",
                        checked = enableNestedScroll,
                        onCheckedChange = { enableNestedScroll = it },
                    )
                }
            }
            item {
                var sliderValue by remember { mutableFloatStateOf(0.5f) }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                var textFieldValue by remember { mutableStateOf("") }
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = "TextField",
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Card(
                    modifier = Modifier.padding(bottom = 12.dp),
                    colors = CardDefaults.defaultColors(
                        color = YubeixTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    SuperDropdown(
                        title = "SuperDropdown",
                        items = BottomSheetDropdownOptions,
                        selectedIndex = dropdownSelectedIndex,
                        onSelectedIndexChange = onDropdownSelectedIndexChange,
                    )
                    SuperSwitch(
                        title = "SuperSwitch",
                        checked = switchChecked,
                        onCheckedChange = onSwitchCheckedChange,
                    )
                }
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun WindowBottomSheetDemo(
    show: Boolean,
    onDismissRequest: () -> Unit,
    dropdownSelectedIndex: Int,
    onDropdownSelectedIndexChange: (Int) -> Unit,
    switchChecked: Boolean,
    onSwitchCheckedChange: (Boolean) -> Unit,
    onDismissFinished: () -> Unit,
) {
    var allowDismiss by remember { mutableStateOf(true) }
    var enableNestedScroll by remember { mutableStateOf(true) }

    WindowBottomSheet(
        title = "WindowBottomSheet",
        show = show,
        allowDismiss = allowDismiss,
        enableNestedScroll = enableNestedScroll,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        startAction = {
            val dismissState = LocalDismissState.current
            IconButton(
                onClick = { dismissState?.invoke() },
            ) {
                Icon(
                    imageVector = YubeixIcons.Close,
                    contentDescription = "Cancel",
                    tint = YubeixTheme.colorScheme.onBackground,
                )
            }
        },
        endAction = {
            val dismissState = LocalDismissState.current
            IconButton(
                onClick = { dismissState?.invoke() },
            ) {
                Icon(
                    imageVector = YubeixIcons.Ok,
                    contentDescription = "Confirm",
                    tint = YubeixTheme.colorScheme.onBackground,
                )
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .scrollEndHaptic()
                .overScrollVertical(),
        ) {
            item {
                SmallTitle(text = "Behavior Settings", insideMargin = PaddingValues(16.dp, 8.dp))
                Card(
                    modifier = Modifier.padding(bottom = 12.dp),
                    colors = CardDefaults.defaultColors(
                        color = YubeixTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    SuperSwitch(
                        title = "Allow Dismiss",
                        summary = "Drag or Back to dismiss",
                        checked = allowDismiss,
                        onCheckedChange = { allowDismiss = it },
                    )
                    SuperSwitch(
                        title = "Enable NestedScroll",
                        summary = "Scroll content vs Drag sheet",
                        checked = enableNestedScroll,
                        onCheckedChange = { enableNestedScroll = it },
                    )
                }
            }
            item {
                var sliderValue by remember { mutableFloatStateOf(0.5f) }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                var textFieldValue by remember { mutableStateOf("") }
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = "TextField",
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Card(
                    modifier = Modifier.padding(bottom = 12.dp),
                    colors = CardDefaults.defaultColors(
                        color = YubeixTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    WindowDropdown(
                        title = "WindowDropdown",
                        items = BottomSheetDropdownOptions,
                        selectedIndex = dropdownSelectedIndex,
                        onSelectedIndexChange = onDropdownSelectedIndexChange,
                    )
                    SuperSwitch(
                        title = "SuperSwitch",
                        checked = switchChecked,
                        onCheckedChange = onSwitchCheckedChange,
                    )
                }
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding(),
                    ),
                )
            }
        }
    }
}
