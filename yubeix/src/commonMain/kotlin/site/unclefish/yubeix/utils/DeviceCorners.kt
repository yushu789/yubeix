// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.UnevenRoundedRectangle
import kotlinx.coroutines.delay

/**
 * Get the corner radius of the device screen
 */
@Composable
fun rememberDeviceCornerRadius(): Dp {
    val systemCornerRadius = getRoundedCorner()
    return remember(systemCornerRadius) {
        if (systemCornerRadius.value > 0f) {
            systemCornerRadius
        } else {
            24.dp
        }
    }
}

/**
 * Page container with dynamic corner radius
 * cornerRadius - Target corner radius
 * animateToSharp - Whether to become sharp after animation ends (direct flash, no animation)
 * animationDuration - Page animation duration, used to delay corner changes
 */
@Composable
fun AnimatedRoundedLeftPage(
    cornerRadius: Dp = rememberDeviceCornerRadius(),
    backgroundColor: Color = Color.White,
    modifier: Modifier = Modifier,
    animateToSharp: Boolean = false,
    animationDuration: Int = 350,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedRadius = remember { Animatable(cornerRadius.value) }

    LaunchedEffect(animateToSharp) {
        if (animateToSharp) {
            // Wait for page animation to end, then directly flash to become sharp
            delay(animationDuration.toLong())
            animatedRadius.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(UnevenRoundedRectangle(topStart = animatedRadius.value.dp, bottomStart = animatedRadius.value.dp))
            .background(backgroundColor)
    ) {
        content()
    }
}

/**
 * Page container with left corner radius
 */
@Composable
fun RoundedLeftPage(
    cornerRadius: Dp = rememberDeviceCornerRadius(),
    backgroundColor: Color = Color.White,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(UnevenRoundedRectangle(topStart = cornerRadius, bottomStart = cornerRadius))
            .background(backgroundColor)
    ) {
        content()
    }
}

/**
 * Dark page container with left corner radius
 */
@Composable
fun RoundedLeftDarkPage(
    cornerRadius: Dp = rememberDeviceCornerRadius(),
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    RoundedLeftPage(
        cornerRadius = cornerRadius,
        backgroundColor = Color(0xFF1a1a1a),
        modifier = modifier,
        content = content
    )
}

/**
 * Semi-transparent darkening overlay with left corner radius
 */
@Composable
fun RoundedLeftDimOverlay(
    cornerRadius: Dp = rememberDeviceCornerRadius(),
    dimAlpha: Float = 0.4f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(UnevenRoundedRectangle(topStart = cornerRadius, bottomStart = cornerRadius))
            .background(Color.Black.copy(alpha = dimAlpha))
    )
}
