// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.Switch
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.extra.SuperGroup
import site.unclefish.yubeix.extra.SuperSwitch
import site.unclefish.yubeix.theme.YubeixTheme

fun LazyListScope.switchSection() {
    item(key = "switch") {
        val switch = remember { mutableStateOf(false) }
        val switchTrue = remember { mutableStateOf(true) }
        val superSwitch = remember { mutableStateOf("false") }
        val superSwitchState = remember { mutableStateOf(false) }
        val superSwitchAnimState = remember { mutableStateOf(false) }

        SmallTitle(text = "Switch")
        SuperGroup(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Switch(
                    checked = switch.value,
                    onCheckedChange = { switch.value = it },
                )
                Switch(
                    checked = switchTrue.value,
                    onCheckedChange = { switchTrue.value = it },
                    modifier = Modifier.padding(start = 6.dp),
                )
                Switch(
                    checked = false,
                    onCheckedChange = { },
                    modifier = Modifier.padding(start = 6.dp),
                    enabled = false,
                )
                Switch(
                    checked = true,
                    onCheckedChange = { },
                    modifier = Modifier.padding(start = 6.dp),
                    enabled = false,
                )
            }
            SuperSwitch(
                title = "Switch",
                summary = "Click to expand a Switch",
                checked = superSwitchAnimState.value,
                onCheckedChange = {
                    superSwitchAnimState.value = it
                },
            )

            AnimatedVisibility(
                visible = superSwitchAnimState.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                SuperSwitch(
                    title = "Switch",
                    checked = superSwitchState.value,
                    endActions = {
                        Text(
                            text = superSwitch.value,
                            color = YubeixTheme.colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onCheckedChange = {
                        superSwitchState.value = it
                        superSwitch.value = "$it"
                    },
                )
            }
            SuperSwitch(
                title = "Disabled Switch",
                checked = true,
                enabled = false,
                onCheckedChange = {},
            )
        }
    }
}
