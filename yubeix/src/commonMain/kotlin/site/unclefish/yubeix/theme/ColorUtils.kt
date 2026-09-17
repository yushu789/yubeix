// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.ui.graphics.Color

internal fun Color.mixWith(other: Color, ratio: Float): Color {
    val clampedRatio = ratio.coerceIn(0f, 1f)
    val baseRatio = 1f - clampedRatio
    return Color(
        red = red * baseRatio + other.red * clampedRatio,
        green = green * baseRatio + other.green * clampedRatio,
        blue = blue * baseRatio + other.blue * clampedRatio,
        alpha = alpha * baseRatio + other.alpha * clampedRatio,
    )
}

internal fun Color.isVisuallyCloseTo(other: Color, threshold: Float = 0.04f): Boolean {
    return kotlin.math.abs(red - other.red) +
        kotlin.math.abs(green - other.green) +
        kotlin.math.abs(blue - other.blue) < threshold
}
