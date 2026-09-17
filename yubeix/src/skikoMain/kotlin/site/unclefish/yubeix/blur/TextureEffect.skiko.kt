// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import site.unclefish.yubeix.blur.internal.recordLayer

/**
 * Applies background blur to the content behind this composable.
 *
 * Skiko (desktop, iOS, macOS, web) actual. The blur itself is approximated with
 * Compose's built-in [BlurEffect] applied to a recorded backdrop layer; see
 * [textureEffect] for the exact degradation notes of each parameter.
 */
actual fun Modifier.textureBlur(
    backdrop: Backdrop,
    shape: Shape,
    blurRadius: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier = textureEffect(
    backdrop = backdrop,
    shape = shape,
    blurRadiusX = blurRadius,
    blurRadiusY = blurRadius,
    noiseCoefficient = noiseCoefficient,
    colors = colors,
    maxDownscaleFactor = maxDownscaleFactor,
    contentBlendMode = contentBlendMode,
    enabled = enabled,
)

/**
 * Applies background blur with independent horizontal and vertical radii.
 *
 * Skiko (desktop, iOS, macOS, web) actual; see [textureEffect] for degradation notes.
 */
actual fun Modifier.textureBlur(
    backdrop: Backdrop,
    shape: Shape,
    blurRadiusX: Float,
    blurRadiusY: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier = textureEffect(
    backdrop = backdrop,
    shape = shape,
    blurRadiusX = blurRadiusX,
    blurRadiusY = blurRadiusY,
    noiseCoefficient = noiseCoefficient,
    colors = colors,
    maxDownscaleFactor = maxDownscaleFactor,
    contentBlendMode = contentBlendMode,
    enabled = enabled,
)

/**
 * Applies the complete texture effect: backdrop blur + color blending.
 *
 * Skiko (desktop, iOS, macOS, web) actual; see [textureEffect] for degradation notes.
 */
actual fun Modifier.textureEffect(
    backdrop: Backdrop,
    shape: Shape,
    blurRadius: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier = textureEffect(
    backdrop = backdrop,
    shape = shape,
    blurRadiusX = blurRadius,
    blurRadiusY = blurRadius,
    noiseCoefficient = noiseCoefficient,
    colors = colors,
    maxDownscaleFactor = maxDownscaleFactor,
    contentBlendMode = contentBlendMode,
    enabled = enabled,
)

/**
 * Applies the complete texture effect with independent horizontal and vertical
 * blur radii.
 *
 * Skiko (desktop, iOS, macOS, web) degradation strategy:
 * - The backdrop is recorded at full resolution into a graphics layer and blurred
 *   with Compose's built-in [BlurEffect] (Skia-backed on all Skiko targets). This
 *   is a visual approximation of the Android LM gaussian shader pipeline; no
 *   adaptive downsampling is performed and [maxDownscaleFactor] is ignored.
 * - [colors].brightness/contrast/saturation are approximated with a color matrix
 *   applied when compositing the blurred layer.
 * - [colors].blendColors (standard and custom blend modes) require AGSL runtime
 *   shaders and are treated as no-ops on Skiko.
 * - [noiseCoefficient] (noise dithering) requires runtime shaders and is ignored.
 * - When [enabled] is false or both radii are non-positive, this modifier is a
 *   no-op and content draws normally.
 */
actual fun Modifier.textureEffect(
    backdrop: Backdrop,
    shape: Shape,
    blurRadiusX: Float,
    blurRadiusY: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier {
    val clampedX = blurRadiusX.coerceIn(0f, BlurDefaults.MaxBlurRadius)
    val clampedY = blurRadiusY.coerceIn(0f, BlurDefaults.MaxBlurRadius)
    if (!enabled || (clampedX <= 0f && clampedY <= 0f)) return this

    return this.then(
        BackdropBlurElement(
            backdrop = backdrop,
            shape = shape,
            radiusX = clampedX,
            radiusY = clampedY,
            colors = colors,
            contentBlendMode = contentBlendMode,
        ),
    )
}

private class BackdropBlurElement(
    val backdrop: Backdrop,
    val shape: Shape,
    val radiusX: Float,
    val radiusY: Float,
    val colors: BlurColors,
    val contentBlendMode: BlendMode,
) : ModifierNodeElement<BackdropBlurNode>() {

    override fun create(): BackdropBlurNode = BackdropBlurNode(
        backdrop = backdrop,
        blurShape = shape,
        radiusX = radiusX,
        radiusY = radiusY,
        colors = colors,
        contentBlendMode = contentBlendMode,
    )

    override fun update(node: BackdropBlurNode) {
        node.backdrop = backdrop
        node.blurShape = shape
        node.radiusX = radiusX
        node.radiusY = radiusY
        node.colors = colors
        node.contentBlendMode = contentBlendMode
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "textureEffect"
        properties["backdrop"] = backdrop
        properties["radiusX"] = radiusX
        properties["radiusY"] = radiusY
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BackdropBlurElement) return false
        if (backdrop != other.backdrop) return false
        if (shape != other.shape) return false
        if (radiusX != other.radiusX) return false
        if (radiusY != other.radiusY) return false
        if (colors != other.colors) return false
        if (contentBlendMode != other.contentBlendMode) return false
        return true
    }

    override fun hashCode(): Int {
        var result = backdrop.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + radiusX.hashCode()
        result = 31 * result + radiusY.hashCode()
        result = 31 * result + colors.hashCode()
        result = 31 * result + contentBlendMode.hashCode()
        return result
    }
}

private class BackdropBlurNode(
    var backdrop: Backdrop,
    var blurShape: Shape,
    var radiusX: Float,
    var radiusY: Float,
    var colors: BlurColors,
    var contentBlendMode: BlendMode,
) : Modifier.Node(),
    LayoutModifierNode,
    DrawModifierNode,
    GlobalPositionAwareModifierNode {

    private var blurLayer: GraphicsLayer? = null

    private val layoutLayerBlock: GraphicsLayerScope.() -> Unit = {
        clip = true
        shape = blurShape
        compositingStrategy = CompositingStrategy.Offscreen
    }

    private var layoutCoordinates: LayoutCoordinates? by mutableStateOf(null, neverEqualPolicy())

    private val contentPaint = Paint()
    private val adjustmentPaint = Paint()

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(IntOffset.Zero, layerBlock = layoutLayerBlock)
        }
    }

    override fun ContentDrawScope.draw() {
        val layer = blurLayer
        if (layer != null) {
            // Pad the recording area so the blur kernel can sample beyond the
            // content bounds without producing edge fade (mirrors the Android
            // platform-blur fallback on API 31/32).
            val currentPadding = maxOf(radiusX, radiusY) * density
            val fullWidth = (size.width + currentPadding * 2f).toInt().coerceAtLeast(1)
            val fullHeight = (size.height + currentPadding * 2f).toInt().coerceAtLeast(1)

            recordLayer(this@BackdropBlurNode, layer, size = IntSize(fullWidth, fullHeight)) {
                if (currentPadding != 0f) {
                    translate(currentPadding, currentPadding) {
                        with(backdrop) {
                            drawBackdrop(density = this@draw, coordinates = layoutCoordinates)
                        }
                    }
                } else {
                    with(backdrop) {
                        drawBackdrop(density = this@draw, coordinates = layoutCoordinates)
                    }
                }
            }
            layer.topLeft =
                if (currentPadding != 0f) {
                    IntOffset(-currentPadding.toInt(), -currentPadding.toInt())
                } else {
                    IntOffset.Zero
                }
            layer.renderEffect = BlurEffect(
                radiusX * density,
                radiusY * density,
            )
            drawBackdropLayer(layer)
            layer.renderEffect = null
        }

        if (contentBlendMode == BlendMode.SrcOver) {
            drawContent()
        } else {
            // The paint passed to saveLayer is applied when the layer is
            // composited back, so the content alpha masks the blurred backdrop
            // (foreground blur) exactly like the Android implementation.
            contentPaint.blendMode = contentBlendMode
            drawContext.canvas.saveLayer(
                Rect(0f, 0f, size.width, size.height),
                contentPaint,
            )
            drawContent()
            drawContext.canvas.restore()
        }
    }

    /**
     * Draws the blurred [layer], applying brightness/contrast/saturation through a
     * color matrix if requested. Blend color entries are skipped on Skiko because
     * they require runtime shaders.
     */
    private fun DrawScope.drawBackdropLayer(layer: GraphicsLayer) {
        val hasAdjustments = colors.brightness != 0f || colors.contrast != 1f || colors.saturation != 1f
        if (!hasAdjustments) {
            drawLayer(layer)
            return
        }
        adjustmentPaint.colorFilter = colorControlsColorFilter(
            brightness = colors.brightness,
            contrast = colors.contrast,
            saturation = colors.saturation,
        )
        drawContext.canvas.saveLayer(
            Rect(0f, 0f, size.width, size.height),
            adjustmentPaint,
        )
        drawLayer(layer)
        drawContext.canvas.restore()
    }

    private fun colorControlsColorFilter(
        brightness: Float,
        contrast: Float,
        saturation: Float,
    ): ColorFilter {
        val invSat = 1f - saturation
        val r = 0.213f * invSat
        val g = 0.715f * invSat
        val b = 0.072f * invSat

        val c = contrast
        val t = (0.5f - c * 0.5f + brightness) * 255f
        val s = saturation

        val cr = c * r
        val cg = c * g
        val cb = c * b
        val cs = c * s

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                cr + cs, cg, cb, 0f, t,
                cr, cg + cs, cb, 0f, t,
                cr, cg, cb + cs, 0f, t,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        return ColorMatrixColorFilter(colorMatrix)
    }

    private fun ensureBlurLayer(): GraphicsLayer = blurLayer
        ?: requireGraphicsContext().createGraphicsLayer().also { blurLayer = it }

    override fun onAttach() {
        ensureBlurLayer()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            if (backdrop.isCoordinatesDependent) {
                layoutCoordinates = coordinates
            } else {
                if (layoutCoordinates != null) {
                    layoutCoordinates = null
                }
            }
        }
    }

    override fun onDetach() {
        val ctx = requireGraphicsContext()
        blurLayer?.let { ctx.releaseGraphicsLayer(it) }
        blurLayer = null
        layoutCoordinates = null
    }
}
