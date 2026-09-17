// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

enum class SharedTransitionDirection {
    None,
    Push,
    Pop,
}

/** The shared-transition scope while its scene is part of an active shared pair, else null. */
val LocalSharedTransitionScope = staticCompositionLocalOf<Any?> { null }

/** Whether the shared-transition host target is currently meant to be visible. */
val LocalSharedHostTargetVisible = compositionLocalOf { true }

/** Whether this scene is one of the two scenes of an active shared transition. */
val LocalSharedTransitionActive = compositionLocalOf { false }

/** The live shared-transition progress, unclamped into the overshoot band. */
val LocalSharedTransitionProgress = compositionLocalOf { 1f }

val LocalSharedTransitionDirection = compositionLocalOf {
    SharedTransitionDirection.None
}

/** Whether this scene is the receding background of a running transition. */
val LocalHostIsTransitionBackground = compositionLocalOf { false }

/** Whether this scene is the front scene of the stack right now. */
val LocalSceneIsForeground = compositionLocalOf { true }

/**
 * Anchor a composable as the shared element standing in for [sharedKey] during a scene
 * transition. This is the extension point hosts use to draw the transition window over the
 * source's twin; the default implementation simply lays out [content] unchanged.
 */
@Composable
fun SharedElement(
    query: String,
    shape: Shape,
    sharedKey: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
    }
}
