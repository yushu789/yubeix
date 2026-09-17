// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

internal actual fun Modifier.navigationBitmapClip(
    cornerRadius: Dp,
    enabled: () -> Boolean,
): Modifier = drawWithContent {
    val clipActive = enabled()
    val radiusPx = cornerRadius.toPx().roundToInt().coerceAtLeast(0)
    if (!clipActive || radiusPx == 0 || size.width <= 0f || size.height <= 0f) {
        drawContent()
        return@drawWithContent
    }

    val mask = NavigationShapeMaskCache.get(
        width = size.width.roundToInt().coerceAtLeast(1),
        height = size.height.roundToInt().coerceAtLeast(1),
        radiusPx = radiusPx,
        layoutDirection = layoutDirection,
        density = this,
    )

    val nativeCanvas = drawContext.canvas.nativeCanvas
    val checkpoint = nativeCanvas.saveLayer(
        0f,
        0f,
        size.width,
        size.height,
        null,
    )
    drawContent()
    nativeCanvas.drawBitmap(
        mask.bitmap,
        -mask.paddingPx.toFloat(),
        -mask.paddingPx.toFloat(),
        NavigationShapeMaskCache.maskPaint,
    )
    nativeCanvas.restoreToCount(checkpoint)
}

private object NavigationShapeMaskCache {
    val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        isDither = false
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val cache = object : LinkedHashMap<MaskKey, MaskBitmap>(MASK_CACHE_MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<MaskKey, MaskBitmap>): Boolean {
            val shouldRemove = size > MASK_CACHE_MAX_SIZE
            if (shouldRemove) {
                eldest.value.bitmap.recycle()
            }
            return shouldRemove
        }
    }

    fun get(
        width: Int,
        height: Int,
        radiusPx: Int,
        layoutDirection: LayoutDirection,
        density: Density,
    ): MaskBitmap {
        val paddingPx = (radiusPx * MASK_PADDING_RADIUS_MULTIPLIER).roundToInt()
        val key = MaskKey(
            width = width,
            height = height,
            radiusPx = radiusPx,
            paddingPx = paddingPx,
            layoutDirection = layoutDirection,
        )
        return synchronized(cache) {
            cache.getOrPut(key) {
                createMask(
                    width = width,
                    height = height,
                    radiusPx = radiusPx,
                    paddingPx = paddingPx,
                    layoutDirection = layoutDirection,
                    density = density,
                )
            }
        }
    }

    private fun createMask(
        width: Int,
        height: Int,
        radiusPx: Int,
        paddingPx: Int,
        layoutDirection: LayoutDirection,
        density: Density,
    ): MaskBitmap {
        val bitmap = Bitmap.createBitmap(
            width + paddingPx * 2,
            height + paddingPx * 2,
            Bitmap.Config.ALPHA_8,
        ).apply {
            setHasMipMap(true)
        }
        val shape = with(density) {
            RoundedRectangle(radiusPx.toDp())
        }
        val outlinePath = shape
            .createOutline(
                size = Size(width.toFloat(), height.toFloat()),
                layoutDirection = layoutDirection,
                density = density,
            )
            .toPath()
            .apply {
                translate(Offset(paddingPx.toFloat(), paddingPx.toFloat()))
            }
        Canvas(bitmap).drawPath(outlinePath.asAndroidPath(), fillPaint)
        return MaskBitmap(bitmap, paddingPx)
    }
}

private fun Outline.toPath(): Path = when (this) {
    is Outline.Generic -> path
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
}

private const val MASK_CACHE_MAX_SIZE = 6
private const val MASK_PADDING_RADIUS_MULTIPLIER = 2f

private data class MaskKey(
    val width: Int,
    val height: Int,
    val radiusPx: Int,
    val paddingPx: Int,
    val layoutDirection: LayoutDirection,
)

private data class MaskBitmap(
    val bitmap: Bitmap,
    val paddingPx: Int,
)
