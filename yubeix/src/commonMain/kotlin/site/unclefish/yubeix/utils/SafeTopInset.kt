// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp

/**
 * How far down the window content has to start to clear everything the system draws at the top.
 *
 * The status bar is the usual one, but a freeform/desktop window has a caption bar instead and a
 * landscape notch reports only as a display cutout, so all three have to be taken into account.
 */
@Composable
fun rememberSafeTopInset(): Dp {
    val statusBars = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val captionBar = WindowInsets.captionBar.asPaddingValues().calculateTopPadding()
    val displayCutout = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    return remember(statusBars, captionBar, displayCutout) {
        maxOf(statusBars, captionBar, displayCutout)
    }
}
