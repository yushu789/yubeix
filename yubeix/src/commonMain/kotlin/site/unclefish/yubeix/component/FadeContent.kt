// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

private const val CONTENT_FADE_DURATION_MILLIS = 200
private const val OVERLAY_FADE_DURATION_MILLIS = 420

/**
 * Cross-fades between values of [targetState] without moving anything: the outgoing state fades
 * out while the incoming one fades in on top of a size-preserving container. Use it for content
 * that swaps in place (an answer swapping for the next question) where a slide would read as
 * motion the content doesn't have.
 */
@Composable
fun <T> FadeContent(
    targetState: T,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    label: String = "FadeContent",
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(
                animationSpec = tween(
                    durationMillis = CONTENT_FADE_DURATION_MILLIS,
                    easing = LinearOutSlowInEasing,
                ),
            )
            val exit = fadeOut(
                animationSpec = tween(
                    durationMillis = CONTENT_FADE_DURATION_MILLIS,
                    easing = LinearOutSlowInEasing,
                ),
            )
            enter togetherWith exit using SizeTransform(clip = false)
        },
        modifier = modifier,
        contentAlignment = contentAlignment,
        label = label,
    ) { state ->
        content(state)
    }
}

/**
 * Cross-fades an [overlayContent] over [baseContent] while keeping exactly one of them composed
 * at rest: showing the overlay fades it in and then drops the base from composition, hiding it
 * composes the base back first and fades the overlay away. Use it for full-surface takeovers
 * (a study session over its host page) that must not keep both trees alive once settled.
 */
@Composable
fun OverlayFadeContent(
    showOverlay: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    baseContent: @Composable () -> Unit = {},
    overlayContent: @Composable () -> Unit,
) {
    val overlayAlpha = remember { Animatable(if (showOverlay) 1f else 0f) }
    var baseComposed by remember { mutableStateOf(!showOverlay) }
    var overlayComposed by remember { mutableStateOf(showOverlay) }

    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            overlayComposed = true
            baseComposed = true
            overlayAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = OVERLAY_FADE_DURATION_MILLIS,
                    easing = LinearEasing,
                ),
            )
            baseComposed = false
        } else {
            baseComposed = true
            if (overlayComposed || overlayAlpha.value > 0f) {
                overlayComposed = true
                overlayAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = OVERLAY_FADE_DURATION_MILLIS,
                        easing = LinearEasing,
                    ),
                )
            }
            overlayComposed = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        if (baseComposed) {
            Box(modifier = Modifier.fillMaxSize()) {
                baseContent()
            }
        }
        if (overlayComposed) {
            val alpha = overlayAlpha.value
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                        clip = false
                    },
            ) {
                overlayContent()
            }
        }
    }
}
