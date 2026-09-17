// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Density
import site.unclefish.yubeix.blur.internal.InverseLayerScope

/**
 * Skiko (desktop, iOS, macOS, web) actual of [rememberLayerBackdrop].
 *
 * Graphics layers are fully supported by the Skiko backend, so backdrop content
 * recording behaves the same as on Android.
 */
@Composable
actual fun rememberLayerBackdrop(
    graphicsLayer: GraphicsLayer,
    onDraw: ContentDrawScope.() -> Unit,
): LayerBackdrop = remember(graphicsLayer, onDraw) {
    LayerBackdrop(graphicsLayer, onDraw)
}

/**
 * Skiko (desktop, iOS, macOS, web) actual of [LayerBackdrop].
 *
 * Recording via [Modifier.layerBackdrop][layerBackdrop] works on Skiko, so the
 * captured layer is drawn with correct relative positioning. Only the blur and
 * color-effect stages applied on top of it degrade on these platforms.
 */
@Stable
actual class LayerBackdrop internal constructor(
    val graphicsLayer: GraphicsLayer,
    internal val onDraw: ContentDrawScope.() -> Unit,
) : Backdrop {

    override val isCoordinatesDependent: Boolean = true

    internal var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)

    private var offsetResidualXValue = 0f
    override val offsetResidualX: Float get() = offsetResidualXValue
    private var offsetResidualYValue = 0f
    override val offsetResidualY: Float get() = offsetResidualYValue

    private var inverseLayerScope: InverseLayerScope? = null

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
        downscaleFactor: Int,
    ) {
        val coordinates = coordinates ?: return
        val layerCoordinates = layerCoordinates ?: return

        val offset = try {
            layerCoordinates.localPositionOf(coordinates)
        } catch (_: Exception) {
            coordinates.positionInWindow() - layerCoordinates.positionInWindow()
        }

        withTransform({
            if (layerBlock != null) {
                with(obtainInverseLayerScope()) { inverseTransform(density, layerBlock) }
            }
            if (downscaleFactor > 1) {
                // The Skiko pipeline records at full resolution, but keep the
                // positioning math correct if a downscale factor is ever requested.
                val inv = 1f / downscaleFactor
                translate(-offset.x * inv, -offset.y * inv)
                scale(inv, inv, Offset.Zero)
            } else {
                offsetResidualXValue = 0f
                offsetResidualYValue = 0f
                translate(-offset.x, -offset.y)
            }
        }) {
            drawLayer(graphicsLayer)
        }
    }

    /**
     * Utility for [LayoutCoordinates.localPositionOf] that returns the position
     * of [other] relative to this coordinate system.
     */
    private fun LayoutCoordinates.localPositionOf(other: LayoutCoordinates): Offset = localPositionOf(other, Offset.Zero)

    private fun obtainInverseLayerScope(): InverseLayerScope = inverseLayerScope?.apply { reset() }
        ?: InverseLayerScope().also { inverseLayerScope = it }
}
