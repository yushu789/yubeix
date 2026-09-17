// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * A [Backdrop] that draws from a captured [GraphicsLayer].
 * The layer content is captured via [Modifier.layerBackdrop][layerBackdrop].
 *
 * Instances are created with [rememberLayerBackdrop]; construction is platform-internal.
 */
@Stable
expect class LayerBackdrop : Backdrop

/**
 * Creates and remembers a [LayerBackdrop] that captures content from a [GraphicsLayer].
 *
 * Use [Modifier.layerBackdrop][layerBackdrop] on the content container to capture its
 * rendered output, then pass this [LayerBackdrop] to blur modifiers.
 *
 * @param graphicsLayer The graphics layer to record content into.
 * @param onDraw Custom draw logic for the layer content.
 */
@Composable
expect fun rememberLayerBackdrop(
    graphicsLayer: GraphicsLayer,
    onDraw: ContentDrawScope.() -> Unit,
): LayerBackdrop

private val DefaultOnDraw: ContentDrawScope.() -> Unit = { drawContent() }

/**
 * Creates and remembers a [LayerBackdrop] with a freshly allocated graphics layer
 * and default content drawing.
 *
 * Use [Modifier.layerBackdrop][layerBackdrop] on the content container to capture its
 * rendered output, then pass this [LayerBackdrop] to blur modifiers.
 */
@Composable
fun rememberLayerBackdrop(): LayerBackdrop {
    val graphicsLayer = rememberGraphicsLayer()
    return rememberLayerBackdrop(
        graphicsLayer = graphicsLayer,
        onDraw = DefaultOnDraw,
    )
}
