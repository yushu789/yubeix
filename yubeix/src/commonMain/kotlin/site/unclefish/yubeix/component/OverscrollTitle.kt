// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val OverscrollTitleMaxFontScale = 1.1f
private val OverscrollTitleMaxFontScaleDistance = 150.dp

@Composable
fun OverscrollTitle(
    restingTopInWindowPx: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxFontScale: Float = OverscrollTitleMaxFontScale,
    maxFontScaleDistance: Dp = OverscrollTitleMaxFontScaleDistance,
    content: @Composable () -> Unit,
) {
    require(maxFontScale >= 1f) { "maxFontScale must be >= 1." }

    val density = LocalDensity.current
    val maxFontScaleDistancePx = with(density) { maxFontScaleDistance.toPx() }
    var topInWindowPx by remember { mutableFloatStateOf(restingTopInWindowPx) }
    val fontScale by remember(
        enabled,
        restingTopInWindowPx,
        maxFontScaleDistancePx,
        maxFontScale,
    ) {
        derivedStateOf {
            if (!enabled || !restingTopInWindowPx.isFinite() || maxFontScaleDistancePx <= 0f) {
                1f
            } else {
                1f + (
                    (topInWindowPx - restingTopInWindowPx) / maxFontScaleDistancePx
                    ).coerceIn(0f, 1f) * (maxFontScale - 1f)
            }
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                topInWindowPx = coordinates.positionInWindow().y
            }
            .graphicsLayer {
                scaleX = fontScale
                scaleY = fontScale
                transformOrigin = TransformOrigin(0f, 0f)
            },
    ) {
        content()
    }
}

@Composable
fun overscrollTitleTextStyle(style: TextStyle): TextStyle {
    return style
}
