// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

private const val FlipCardCameraDistance = 12f

/**
 * How far the card is allowed to tilt at the halfway point of the flip.
 *
 * A card rendered edge-on projects to zero width, so any frame that lands near 90 degrees draws the
 * card as a bright needle. The tween this flip used to run on crossed the middle at well over
 * 1500deg/s, fast enough that no frame landed there; the spring crosses at about 790deg/s, which is
 * ~7deg per frame at 120Hz - close enough to make one or two needle frames likely. Compressing the
 * rendered tilt keeps the narrowest frame at cos(80deg), about a sixth of the card's width, and
 * leaves the flat open and closed states untouched.
 */
private const val FlipCardMaxTiltDegrees = 80f

/**
 * Stateless 3D flip-card renderer: [front] and [back] swap at the halfway point of
 * [rotationDegrees], and tilt is compressed near 90 degrees so no frame renders edge-on.
 * Animation state stays with the caller, so changing the target can interrupt and reverse the
 * current animation directly.
 */
@Composable
fun FlipCard(
    rotationDegrees: Float,
    startBounds: Rect,
    containerSize: IntSize,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startScale: Float = 1f,
    sinkScale: Float = 1f,
    front: @Composable BoxScope.() -> Unit,
    back: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val progress = (rotationDegrees / 180f).coerceIn(0f, 1f)
    val clampedStartScale = startScale.coerceIn(0f, 1f)
    val containerCenterX = containerSize.width / 2f
    val containerCenterY = containerSize.height / 2f
    val startCenterX = containerCenterX +
        (startBounds.center.x - containerCenterX) * clampedStartScale
    val startCenterY = containerCenterY +
        (startBounds.center.y - containerCenterY) * clampedStartScale
    val startLeft = startCenterX - startBounds.width / 2f
    val startTop = startCenterY - startBounds.height / 2f
    val targetLeft = ((containerSize.width - startBounds.width) / 2f).coerceAtLeast(0f)
    val targetTop = ((containerSize.height - startBounds.height) / 2f).coerceAtLeast(0f)
    val animatedLeft = startLeft + (targetLeft - startLeft) * progress
    val animatedTop = startTop + (targetTop - startTop) * progress
    val visualScale = clampedStartScale + (1f - clampedStartScale) * progress
    val clampedSinkScale = sinkScale.coerceIn(0f, 1f)
    val animatedCenterX = animatedLeft + startBounds.width / 2f
    val animatedCenterY = animatedTop + startBounds.height / 2f
    val sunkCenterX = containerCenterX +
        (animatedCenterX - containerCenterX) * clampedSinkScale
    val sunkCenterY = containerCenterY +
        (animatedCenterY - containerCenterY) * clampedSinkScale
    val renderedLeft = sunkCenterX - startBounds.width / 2f
    val renderedTop = sunkCenterY - startBounds.height / 2f
    val renderedScale = visualScale * clampedSinkScale
    val showFront = rotationDegrees <= 90f
    val faceRotation = if (showFront) rotationDegrees else rotationDegrees - 180f
    val visibleRotation = faceRotation * (FlipCardMaxTiltDegrees / 90f)

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = renderedLeft.roundToInt(),
                    y = renderedTop.roundToInt(),
                )
            }
            .requiredSize(
                width = with(density) { startBounds.width.toDp() },
                height = with(density) { startBounds.height.toDp() },
            )
            .graphicsLayer {
                rotationY = visibleRotation
                cameraDistance = FlipCardCameraDistance * density.density
                transformOrigin = TransformOrigin.Center
                scaleX = renderedScale
                scaleY = renderedScale
                clip = false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        if (showFront) front() else back()
    }
}
