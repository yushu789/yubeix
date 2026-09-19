// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.Slider
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.TextField
import site.unclefish.yubeix.extra.CupertinoAlertAction
import site.unclefish.yubeix.extra.CupertinoAlertActionStyle
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.extra.SuperDialog
import site.unclefish.yubeix.extra.SuperGroup
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Contacts
import site.unclefish.yubeix.theme.YubeixTheme

fun LazyListScope.arrowSection() {
    item(key = "arrow") {
        var volume by remember { mutableFloatStateOf(0.5f) }
        val showVolumeDialog = remember { mutableStateOf(false) }
        val volumeDialogHoldDown = remember { mutableStateOf(false) }

        SmallTitle(text = "Arrow")
        SuperGroup(
            modifier = Modifier
                .padding(bottom = 8.dp),
        ) {
            SuperArrow(
                title = "Arrow",
                startAction = {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(
                            imageVector = YubeixIcons.Contacts,
                            contentDescription = "Personal",
                            tint = YubeixTheme.colorScheme.onBackground,
                        )
                    }
                },
                endActions = {
                    Text(
                        text = "End",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.onSurfaceVariantActions,
                    )
                },
                onClick = {},
            )
            SuperArrow(
                title = "Arrow + Slider + Dialog",
                endActions = {
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.onSurfaceVariantActions,
                    )
                },
                onClick = {
                    showVolumeDialog.value = true
                    volumeDialogHoldDown.value = true
                },
                holdDownState = volumeDialogHoldDown.value,
                bottomAction = {
                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                    )
                },
            )
            SuperArrow(
                title = "Disabled Arrow",
                endActions = {
                    Text(
                        text = "End",
                        fontSize = YubeixTheme.textStyles.body2.fontSize,
                        color = YubeixTheme.colorScheme.disabledOnSecondaryVariant,
                    )
                },
                enabled = false,
            )
        }

        SliderDialog(
            showVolumeDialog,
            volumeState = { volume },
            onVolumeChange = { volume = it },
            onDismissFinished = { volumeDialogHoldDown.value = false },
        )
    }
}

@Composable
private fun SliderDialog(
    showDialog: MutableState<Boolean>,
    volumeState: () -> Float,
    onVolumeChange: (Float) -> Unit,
    onDismissFinished: () -> Unit,
) {
    // The field state lives here (not inside the content slot) so the Confirm action can read it;
    // keyed on visibility so the field restarts from the current volume on every presentation.
    var text by remember(showDialog.value) { mutableStateOf(((volumeState() * 100).toInt()).toString()) }
    SuperDialog(
        show = showDialog.value,
        title = "Adjust Volume",
        message = "Enter 0-100",
        onDismissRequest = {
            showDialog.value = false
        },
        onDismissFinished = onDismissFinished,
        actions = listOf(
            CupertinoAlertAction(
                label = "Cancel",
                role = CupertinoAlertActionStyle.Cancel,
                onClick = { showDialog.value = false },
            ),
            CupertinoAlertAction(
                label = "Confirm",
                onClick = {
                    val parsed = text.toIntOrNull()
                    val clamped = parsed?.coerceIn(0, 100) ?: ((volumeState() * 100).toInt())
                    onVolumeChange(clamped / 100f)
                    showDialog.value = false
                },
            ),
        ),
        content = {
            TextField(
                value = text,
                maxLines = 1,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        text = ""
                    } else {
                        val limited = digits.take(3)
                        val num = limited.toIntOrNull() ?: 0
                        val clamped = num.coerceIn(0, 100)
                        text = clamped.toString()
                    }
                },
            )
        },
    )
}
