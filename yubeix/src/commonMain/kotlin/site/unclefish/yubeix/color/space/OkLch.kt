// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.color.space

import androidx.compose.ui.graphics.Color
import site.unclefish.yubeix.color.core.Transforms

/**
 * OkLCH representation with normalized, user-friendly ranges.
 * - `l`: 0..100 (lightness percent)
 * - `c`: 0..100 (chroma percent, mapped to internal [0.0, 0.4])
 * - `h`: hue in degrees [0, 360]
 */
data class OkLch(val l: Float, val c: Float, val h: Float) {
    fun toColor(alpha: Float = 1f): Color {
        val lN = (l / 100f).coerceIn(0f, 1f)
        val cN = ((c / 100f) * 0.4f).coerceIn(0f, 0.4f)
        val hDeg = (((h % 360f) + 360f) % 360f)
        return Transforms.oklchToColor(lN, cN, hDeg, alpha)
    }
}
