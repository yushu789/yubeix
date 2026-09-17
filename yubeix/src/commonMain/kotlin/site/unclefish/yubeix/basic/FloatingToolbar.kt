// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.blur.BlendColorEntry
import site.unclefish.yubeix.blur.BlurBlendMode
import site.unclefish.yubeix.blur.BlurColors
import site.unclefish.yubeix.blur.LayerBackdrop
import site.unclefish.yubeix.blur.isRenderEffectSupported
import site.unclefish.yubeix.blur.textureBlur
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape

/**
 * A [FloatingToolbar] that renders its content in a Card, arranged either horizontally or vertically.
 * The actual placement on screen is handled by the parent, typically Scaffold.
 *
 * @param modifier The modifier to be applied to the [FloatingToolbar].
 * @param color Background color of the [FloatingToolbar].
 * @param cornerRadius Corner radius of the [FloatingToolbar].
 * @param outSidePadding Padding outside the [FloatingToolbar].
 * @param shadowElevation The shadow elevation of the [FloatingToolbar].
 * @param showDivider Whether to show the divider line around the [FloatingToolbar].
 * @param content The [Composable] content of the [FloatingToolbar].
 */
@Composable
@NonRestartableComposable
fun FloatingToolbar(
    modifier: Modifier = Modifier,
    color: Color = FloatingToolbarDefaults.defaultColor(),
    cornerRadius: Dp = FloatingToolbarDefaults.CornerRadius,
    outSidePadding: PaddingValues = FloatingToolbarDefaults.OutSidePadding,
    shadowElevation: Dp = 4.dp,
    showDivider: Boolean = false,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val roundedCornerShape = yubeixShape(cornerRadius)
    val dividerColor = YubeixTheme.colorScheme.dividerLine

    val clipRequired = cornerRadius > 0.dp
    val layerOrClipModifier = remember(shadowElevation, clipRequired, roundedCornerShape, density) {
        when {
            shadowElevation > 0.dp -> Modifier.graphicsLayer(
                shadowElevation = with(density) { shadowElevation.toPx() },
                shape = roundedCornerShape,
                clip = clipRequired,
            )

            clipRequired -> Modifier.clip(roundedCornerShape)

            else -> Modifier
        }
    }
    val dividerModifier = remember(showDivider, roundedCornerShape, dividerColor) {
        if (showDivider) {
            Modifier
                .background(
                    color = dividerColor,
                    shape = roundedCornerShape,
                )
                .padding(0.75.dp)
        } else {
            Modifier
        }
    }

    Box(
        modifier = modifier
            .padding(outSidePadding)
            .then(dividerModifier)
            .then(layerOrClipModifier)
            .background(color = color)
            .pointerInput(Unit) {
                detectTapGestures { /* Consume click */ }
            },
    ) {
        content()
    }
}

object FloatingToolbarDefaults {

    /**
     * Default corner radius of the [FloatingToolbar].
     */
    val CornerRadius = 50.dp

    /**
     * Default color of the [FloatingToolbar].
     */
    @Composable
    fun defaultColor() = YubeixTheme.colorScheme.surfaceContainer

    /**
     * Default padding outside the [FloatingToolbar].
     */
    val OutSidePadding = PaddingValues(12.dp, 8.dp)
}

// region Item-list floating toolbar
// Ported from the wordmoment app's floating button bar: a stretched floating toolbar with a
// spec-driven icon+label grid, texture-blur glass when a LayerBackdrop is available, and a
// layered highlight border.

/**
 * Declarative description of one item in the item-list [FloatingToolbar].
 *
 * @param key Stable identity used for item recomposition.
 * @param label The label drawn under the icon.
 * @param icon The icon drawn above the label.
 * @param onClick Invoked when the item is clicked.
 * @param enabled Whether the item is interactive.
 */
@Immutable
data class FloatingToolbarItemSpec(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/**
 * A floating toolbar driven by a list of [FloatingToolbarItemSpec]s: one stretched column per
 * item (icon above label), arranged horizontally, filling the available width.
 *
 * When [backdrop] is set and the platform supports render effects, the toolbar's background is a
 * texture blur sampling the backdrop, overlaid with a translucent [color] tint; otherwise it
 * falls back to the flat [color] fill. A gradient highlight border and tinted shadow are applied
 * in both modes.
 *
 * The actual placement on screen is handled by the parent, typically [Scaffold].
 *
 * @param items The items to render. The composable emits nothing when the list is empty.
 * @param modifier The [Modifier] to be applied to the toolbar.
 * @param backdrop Optional [LayerBackdrop] captured from the content behind the toolbar,
 *   enabling the blurred-glass background.
 * @param color Background color of the toolbar, and the tint over the blur when glass is active.
 * @param cornerRadius Corner radius of the toolbar; defaults to half the resolved height.
 * @param outSidePadding Padding outside the toolbar.
 * @param shadowElevation The shadow elevation of the toolbar.
 * @param showDivider Whether to show the hairline divider border around the toolbar.
 * @param contentPadding Padding around the item row inside the toolbar.
 * @param itemSpacing Spacing between items.
 * @param itemMinHeight The minimum height of each item.
 * @param itemCornerRadius Corner radius of each item; defaults to [cornerRadius] inset by the
 *   content padding.
 * @param itemIconSize The side length of each item's icon.
 */
@Composable
fun FloatingToolbar(
    items: List<FloatingToolbarItemSpec>,
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop? = null,
    color: Color = FloatingToolbarDefaults.defaultColor(),
    cornerRadius: Dp? = null,
    outSidePadding: PaddingValues = FloatingToolbarDefaults.OutSidePadding,
    shadowElevation: Dp = 10.dp,
    showDivider: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(6.dp),
    itemSpacing: Dp = 6.dp,
    itemMinHeight: Dp = 52.dp,
    itemCornerRadius: Dp? = null,
    itemIconSize: Dp = 21.dp,
) {
    if (items.isEmpty()) return

    val colors = YubeixTheme.colorScheme
    val contentTopPadding = contentPadding.calculateTopPadding()
    val contentBottomPadding = contentPadding.calculateBottomPadding()
    val contentVerticalPadding = contentTopPadding + contentBottomPadding
    val resolvedCornerRadius = cornerRadius ?: ((itemMinHeight + contentVerticalPadding) / 2)
    val resolvedItemCornerRadius = itemCornerRadius
        ?: (resolvedCornerRadius - minOf(contentTopPadding, contentBottomPadding)).coerceAtLeast(0.dp)
    val shape = yubeixShape(resolvedCornerRadius)
    val isDark = colors.background.luminance() < 0.5f
    val blurEnabled = isRenderEffectSupported() && backdrop != null
    val blurBackdrop = backdrop.takeIf { blurEnabled }
    val blurColors = remember(isDark, color) {
        BlurColors(
            blendColors = listOf(
                BlendColorEntry(
                    color = color.copy(alpha = if (isDark) 0.56f else 0.5f),
                    mode = BlurBlendMode.SrcOver,
                ),
                if (isDark) {
                    BlendColorEntry(Color.White.copy(alpha = 0.06f), BlurBlendMode.PlusLighter)
                } else {
                    BlendColorEntry(Color.White.copy(alpha = 0.16f), BlurBlendMode.Plus)
                },
            ),
            brightness = if (isDark) -0.02f else 0.04f,
            contrast = 1.04f,
            saturation = 1.08f,
        )
    }
    val highlightBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.18f else 0.78f),
                Color.White.copy(alpha = if (isDark) 0.05f else 0.24f),
                Color.Black.copy(alpha = if (isDark) 0.24f else 0.07f),
            ),
        )
    }
    val shadowModifier = if (shadowElevation > 0.dp) {
        Modifier.shadow(
            elevation = shadowElevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.56f else 0.34f),
            spotColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.22f),
        )
    } else {
        Modifier
    }
    val clipModifier = if (resolvedCornerRadius > 0.dp) Modifier.clip(shape) else Modifier

    Box(
        modifier = modifier
            .padding(outSidePadding)
            .then(shadowModifier)
            .then(clipModifier)
            .then(
                if (blurBackdrop != null) {
                    Modifier.textureBlur(
                        backdrop = blurBackdrop,
                        shape = shape,
                        blurRadius = 25f,
                        colors = blurColors,
                    )
                } else {
                    Modifier.background(color = color, shape = shape)
                },
            )
            .then(
                if (blurEnabled) {
                    Modifier.background(
                        color = color.copy(alpha = if (isDark) 0.30f else 0.22f),
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .then(
                if (showDivider) {
                    Modifier.border(
                        width = 0.75.dp,
                        color = colors.dividerLine,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .border(
                width = 0.65.dp,
                brush = highlightBrush,
                shape = shape,
            )
            .pointerInput(Unit) {
                detectTapGestures { /* Consume taps on the floating surface. */ }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                key(item.key) {
                    Button(
                        onClick = item.onClick,
                        enabled = item.enabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = Color.Transparent,
                            disabledColor = Color.Transparent,
                            contentColor = colors.onSurface,
                            disabledContentColor = colors.disabledOnSecondaryVariant,
                        ),
                        cornerRadius = resolvedItemCornerRadius,
                        minWidth = 0.dp,
                        minHeight = itemMinHeight,
                        insideMargin = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(itemIconSize),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                style = YubeixTheme.textStyles.footnote2.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
// endregion
