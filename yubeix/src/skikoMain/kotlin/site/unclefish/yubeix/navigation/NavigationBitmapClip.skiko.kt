// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.kyant.shapes.RoundedRectangle

/**
 * Non-Android fallback: clip through the compose shape pipeline. The scene content is already
 * fullscreen and square when idle, so an always-on corner clip is visually a no-op outside
 * transitions and correct during them.
 */
internal actual fun Modifier.navigationBitmapClip(
    cornerRadius: Dp,
    enabled: () -> Boolean,
): Modifier = clip(RoundedRectangle(cornerRadius))
