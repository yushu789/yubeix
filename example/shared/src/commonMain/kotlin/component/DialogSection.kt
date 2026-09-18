// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.ButtonDefaults
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.TextButton
import site.unclefish.yubeix.extra.CupertinoAlertAction
import site.unclefish.yubeix.extra.CupertinoAlertActionStyle
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.extra.SuperDialog
import site.unclefish.yubeix.extra.WindowDialog
import site.unclefish.yubeix.theme.LocalDismissState

fun LazyListScope.dialogSection() {
    item(key = "dialog") {
        var showSuperDialog by remember { mutableStateOf(false) }
        var showWindowDialog by remember { mutableStateOf(false) }
        var superDialogHoldDown by remember { mutableStateOf(false) }
        var windowDialogHoldDown by remember { mutableStateOf(false) }

        SmallTitle(text = "Dialog")
        Card(
            modifier = Modifier
                .padding(bottom = 12.dp),
        ) {
            SuperArrow(
                title = "SuperDialog",
                onClick = {
                    showSuperDialog = true
                    superDialogHoldDown = true
                },
                holdDownState = superDialogHoldDown,
            )
            SuperArrow(
                title = "WindowDialog",
                onClick = {
                    showWindowDialog = true
                    windowDialogHoldDown = true
                },
                holdDownState = windowDialogHoldDown,
            )
        }
        SectionCaption("Tap a row to present its dialog")

        SuperDialogDemo(
            show = showSuperDialog,
            onDismissRequest = { showSuperDialog = false },
            onDismissFinished = { superDialogHoldDown = false },
        )
        WindowDialogDemo(
            show = showWindowDialog,
            onDismissRequest = { showWindowDialog = false },
            onDismissFinished = { windowDialogHoldDown = false },
        )
    }
}

@Composable
private fun SuperDialogDemo(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    SuperDialog(
        show = show,
        title = "SuperDialog",
        message = "A dialog component inside YubeixPopupHost.",
        actions = listOf(
            CupertinoAlertAction(
                label = "Cancel",
                role = CupertinoAlertActionStyle.Cancel,
                onClick = onDismissRequest,
            ),
            CupertinoAlertAction(
                label = "Confirm",
                onClick = onDismissRequest,
            ),
        ),
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
    )
}

@Composable
private fun WindowDialogDemo(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = "WindowDialog",
        summary = "A window-level dialog, no YubeixPopupHost required.",
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        content = {
            val dismissState = LocalDismissState.current
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    text = "Cancel",
                    onClick = { dismissState?.invoke() },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = "Confirm",
                    onClick = { dismissState?.invoke() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        },
    )
}
