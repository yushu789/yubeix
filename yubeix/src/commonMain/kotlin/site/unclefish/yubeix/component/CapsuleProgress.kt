// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixCapsuleShape

/**
 * A capsule that shows the current step of a bounded run — the `current/total` count above a thin
 * progress track — and can swap the whole capsule content for [trailingLabel] (a mode caption such
 * as the name of an in-flight phase) with a vertical slide. Showing the label and showing the
 * count are the two states of one animated switch, so the capsule keeps a constant size while its
 * content changes.
 *
 * @param current The completed step, shown as the left side of the `current/total` caption.
 * @param total The number of steps the whole run has; drives the fill fraction of the track.
 * @param modifier The modifier applied to the capsule container.
 * @param containerColor The capsule background color.
 * @param contentColor The color of the count caption and of the unfilled track.
 * @param progressBarColor The color of the filled part of the track.
 * @param trailingLabel When non-null, rendered in place of the count and track with a slide-and-fade
 *  transition; pass `null` to show the count again.
 */
@Composable
fun CapsuleProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = YubeixTheme.colorScheme.surface,
    contentColor: Color = YubeixTheme.colorScheme.onSurface,
    progressBarColor: Color = YubeixTheme.colorScheme.primary,
    trailingLabel: (@Composable () -> Unit)? = null,
) {
    val progress = if (total > 0) current.toFloat() / total else 0f
    val capsuleShape = yubeixCapsuleShape()
    val showingLabel = trailingLabel != null

    Box(
        modifier = modifier
            .height(42.dp)
            .widthIn(min = 120.dp)
            .graphicsLayer(clip = false)
            .clip(capsuleShape)
            .background(containerColor, capsuleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Key on the mode rather than the label slot itself: two successive labels are the same
        // visual state and must not re-run the enter transition on every recomposition.
        AnimatedContent(
            targetState = showingLabel,
            transitionSpec = {
                val enter = fadeIn(
                    animationSpec = tween(durationMillis = 240, delayMillis = 40)
                ) + slideInVertically(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) { fullHeight -> fullHeight / 3 }
                val exit = fadeOut(animationSpec = tween(durationMillis = 160))
                enter togetherWith exit
            },
            contentAlignment = Alignment.Center,
            label = "CapsuleProgressTransition",
        ) { labelVisible ->
            if (labelVisible) {
                Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                    trailingLabel?.invoke()
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = "$current/$total",
                        style = YubeixTheme.textStyles.footnote2.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                        ),
                        color = contentColor,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.15f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .background(progressBarColor),
                        )
                    }
                }
            }
        }
    }
}
