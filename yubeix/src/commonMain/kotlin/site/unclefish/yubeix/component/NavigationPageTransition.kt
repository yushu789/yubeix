// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import site.unclefish.yubeix.navigation.navigationBitmapClip
import site.unclefish.yubeix.utils.rememberDeviceCornerRadius

private const val NAVIGATION_PAGE_TRANSITION_DURATION_MILLIS = 220
private const val NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS = 0.375f
private const val NAVIGATION_PAGE_TRANSITION_INITIAL_SCALE = 0.98f
private const val NAVIGATION_PAGE_TRANSITION_TARGET_SCALE = 0.97f

/**
 * The home-tab page transition: the outgoing page fades out while scaling down to
 * [NAVIGATION_PAGE_TRANSITION_TARGET_SCALE], then the incoming page fades in scaling up from
 * [NAVIGATION_PAGE_TRANSITION_INITIAL_SCALE] - a 220ms two-phase hand-off (exit takes the first
 * 37.5% of the timeline) with the device's rounded-corner mask applied while pages cross.
 * Interrupting a running transition carries the in-flight alpha and scale over, so rapid tab
 * taps never restart from a blank frame.
 *
 * Not swipeable by design - this is the tab-switch transition, not a pager.
 */
@Composable
fun <T : Any> NavigationPageTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val cornerRadius = rememberDeviceCornerRadius()
    val progress = remember { Animatable(1f) }
    val saveableStateHolder = rememberSaveableStateHolder()
    var outgoingState by remember { mutableStateOf(targetState) }
    var incomingState by remember { mutableStateOf(targetState) }
    var exitStartAlpha by remember { mutableFloatStateOf(1f) }
    var exitStartScale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(targetState) {
        val currentProgress = progress.value.coerceIn(0f, 1f)
        val currentIsExiting =
            outgoingState != incomingState &&
                currentProgress < NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS
        val currentPhaseProgress = if (currentIsExiting) {
            (currentProgress / NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS).coerceIn(0f, 1f)
        } else {
            (
                (currentProgress - NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS) /
                    (1f - NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS)
                ).coerceIn(0f, 1f)
        }
        val currentAlpha = if (currentIsExiting) {
            navigationPageLerp(exitStartAlpha, 0f, currentPhaseProgress)
        } else {
            currentPhaseProgress
        }
        val currentScale = if (currentIsExiting) {
            navigationPageLerp(
                exitStartScale,
                NAVIGATION_PAGE_TRANSITION_TARGET_SCALE,
                currentPhaseProgress,
            )
        } else {
            navigationPageLerp(
                NAVIGATION_PAGE_TRANSITION_INITIAL_SCALE,
                1f,
                currentPhaseProgress,
            )
        }
        val visibleState = if (currentIsExiting) outgoingState else incomingState

        if (targetState == visibleState) {
            outgoingState = visibleState
            incomingState = visibleState
            exitStartAlpha = 1f
            exitStartScale = 1f
            progress.snapTo(1f)
            return@LaunchedEffect
        }

        outgoingState = visibleState
        incomingState = targetState
        exitStartAlpha = currentAlpha
        exitStartScale = currentScale
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = NAVIGATION_PAGE_TRANSITION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        )
        outgoingState = targetState
        exitStartAlpha = 1f
        exitStartScale = 1f
    }

    val progressValue = progress.value.coerceIn(0f, 1f)
    val transitionActive = outgoingState != incomingState
    val isExiting =
        transitionActive && progressValue < NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS
    val activeState = if (isExiting) outgoingState else incomingState
    val phaseProgress = if (isExiting) {
        (progressValue / NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS).coerceIn(0f, 1f)
    } else {
        (
            (progressValue - NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS) /
                (1f - NAVIGATION_PAGE_TRANSITION_EXIT_PROGRESS)
            ).coerceIn(0f, 1f)
    }
    val alpha = if (isExiting) {
        navigationPageLerp(exitStartAlpha, 0f, phaseProgress)
    } else {
        phaseProgress
    }
    val scale = if (isExiting) {
        navigationPageLerp(
            exitStartScale,
            NAVIGATION_PAGE_TRANSITION_TARGET_SCALE,
            phaseProgress,
        )
    } else {
        navigationPageLerp(NAVIGATION_PAGE_TRANSITION_INITIAL_SCALE, 1f, phaseProgress)
    }

    Box(modifier = modifier.fillMaxSize()) {
        saveableStateHolder.SaveableStateProvider(activeState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                    }
                    .navigationBitmapClip(
                        cornerRadius = cornerRadius,
                        enabled = { transitionActive },
                    ),
            ) {
                content(activeState)
            }
        }
    }
}

private fun navigationPageLerp(
    start: Float,
    stop: Float,
    fraction: Float,
): Float = start + (stop - start) * fraction.coerceIn(0f, 1f)
