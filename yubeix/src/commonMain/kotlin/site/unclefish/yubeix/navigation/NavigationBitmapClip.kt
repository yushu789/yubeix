// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Clips a scene to the device's rounded-corner shape while a transition runs, so a page sliding
 * in or out reads as a physical card rather than a full-bleed rectangle.
 *
 * The Android actual masks the layer through a cached ALPHA_8 bitmap (crisp corner alpha at any
 * scale); other platforms clip through the compose shape pipeline. [enabled] is read every draw,
 * so the clip may toggle while transitions start and settle.
 */
internal expect fun Modifier.navigationBitmapClip(
    cornerRadius: Dp,
    enabled: () -> Boolean,
): Modifier

/** Scene-transition-aware wrapper: the clip only runs while [scene] is actually transitioning. */
internal fun Modifier.navigationSceneBitmapClip(
    scene: Scene<*>,
    cornerRadius: Dp,
    enabled: Boolean = true,
): Modifier = navigationBitmapClip(cornerRadius = cornerRadius) {
    val clipActive = enabled && when (scene.transition) {
        is SceneTransition.Enter,
        is SceneTransition.Exit,
        SceneTransition.Drag,
        is SceneTransition.DragEnd -> true

        SceneTransition.None -> false
    }
    clipActive
}
