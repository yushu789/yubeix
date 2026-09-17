// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Shape

/**
 * Applies background blur to the content behind this composable.
 *
 * Blend colors support both standard SkBlendMode (0-29, GPU hardware) and
 * custom modes (100-121, 200-203, runtime shader). See [BlurBlendMode].
 * Blend color entries, noise dithering and adaptive downsampling are only applied
 * on platforms where [isRuntimeShaderSupported] returns true (Android API 33+);
 * on other platforms they are treated as no-ops and blur degrades to a Compose
 * built-in blur approximation.
 *
 * @param backdrop The [Backdrop] providing the background content to blur.
 * @param shape The shape provider for the blur region clipping.
 * @param blurRadius The blur radius in dp. Internally converted to pixels using display density.
 *   Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param noiseCoefficient Noise dithering coefficient for anti-banding. 0 disables noise.
 * @param colors Color adjustments and blend layers applied after blur.
 * @param maxDownscaleFactor Maximum adaptive downscale factor for the blur pipeline.
 *   Lower values reduce pixelation on small surfaces at the cost of performance.
 * @param contentBlendMode Optional [BlendMode] for compositing content over the blur.
 *   Use [BlendMode.DstIn] for foreground blur (content alpha masks the blur).
 * @param enabled Whether blur is active. When false, the effect is skipped and content draws normally.
 */
expect fun Modifier.textureBlur(
    backdrop: Backdrop,
    shape: Shape,
    blurRadius: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier

/**
 * Applies background blur with independent horizontal and vertical radii.
 *
 * @param backdrop The [Backdrop] providing the background content to blur.
 * @param shape The shape provider for the blur region clipping.
 * @param blurRadiusX The horizontal blur radius in dp. Internally converted to pixels using display density.
 *   Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param blurRadiusY The vertical blur radius in dp. Internally converted to pixels using display density.
 *   Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param noiseCoefficient Noise dithering coefficient for anti-banding. 0 disables noise.
 * @param colors Color adjustments and blend layers applied after blur.
 * @param maxDownscaleFactor Maximum adaptive downscale factor for the blur pipeline.
 *   Lower values reduce pixelation on small surfaces at the cost of performance.
 * @param contentBlendMode Optional [BlendMode] for compositing content over the blur.
 *   Use [BlendMode.DstIn] for foreground blur (content alpha masks the blur).
 * @param enabled Whether blur is active. When false, the effect is skipped and content draws normally.
 */
expect fun Modifier.textureBlur(
    backdrop: Backdrop,
    shape: Shape,
    blurRadiusX: Float,
    blurRadiusY: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier

/**
 * Applies the complete texture effect: backdrop blur + color blending
 * (with all custom blend modes).
 *
 * @param backdrop The [Backdrop] providing the background content to blur.
 * @param shape Shape provider for the blur region clipping.
 * @param blurRadius The blur radius in dp. Internally converted to pixels using display density.
 *   Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param noiseCoefficient Noise dithering coefficient for anti-banding.
 * @param colors Color adjustments and blend layers applied after blur.
 * @param maxDownscaleFactor Maximum adaptive downscale factor for the blur pipeline.
 *   Lower values reduce pixelation on small surfaces at the cost of performance.
 * @param contentBlendMode Optional [BlendMode] for compositing content over the blur.
 *   Use [BlendMode.DstIn] for foreground blur (content alpha masks the blur).
 * @param enabled Whether the effect is active. When false, the effect is skipped and content draws normally.
 */
expect fun Modifier.textureEffect(
    backdrop: Backdrop,
    shape: Shape,
    blurRadius: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier

/**
 * Applies the complete texture effect with independent horizontal and vertical
 * blur radii: backdrop blur + color blending (with all custom blend modes).
 *
 * @param backdrop The [Backdrop] providing the background content to blur.
 * @param shape Shape provider for the blur region clipping.
 * @param blurRadiusX The horizontal blur radius in dp. Internally converted to pixels using display density.
 *   Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param blurRadiusY The vertical blur radius in dp. Internally converted to pixels using display density.
 *   Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param noiseCoefficient Noise dithering coefficient for anti-banding.
 * @param colors Color adjustments and blend layers applied after blur.
 * @param maxDownscaleFactor Maximum adaptive downscale factor for the blur pipeline.
 *   Lower values reduce pixelation on small surfaces at the cost of performance.
 * @param contentBlendMode Optional [BlendMode] for compositing content over the blur.
 *   Use [BlendMode.DstIn] for foreground blur (content alpha masks the blur).
 * @param enabled Whether the effect is active. When false, the effect is skipped and content draws normally.
 */
expect fun Modifier.textureEffect(
    backdrop: Backdrop,
    shape: Shape,
    blurRadiusX: Float,
    blurRadiusY: Float,
    noiseCoefficient: Float,
    colors: BlurColors,
    maxDownscaleFactor: Int,
    contentBlendMode: BlendMode,
    enabled: Boolean,
): Modifier

/**
 * Convenience overload for [textureBlur] matching the most common call shape.
 * Platform default values are applied for the omitted parameters, mirroring the
 * original Android-only API defaults.
 *
 * @param backdrop The [Backdrop] providing the background content to blur.
 * @param shape The shape provider for the blur region clipping.
 * @param blurRadius The blur radius in dp. Clamped to [0, [BlurDefaults.MaxBlurRadius]].
 * @param colors Color adjustments and blend layers applied after blur.
 * @param contentBlendMode Optional [BlendMode] for compositing content over the blur.
 */
fun Modifier.textureBlur(
    backdrop: Backdrop,
    shape: Shape,
    blurRadius: Float = BlurDefaults.BlurRadius,
    colors: BlurColors = BlurColors(),
    contentBlendMode: BlendMode = BlendMode.SrcOver,
): Modifier = textureBlur(
    backdrop = backdrop,
    shape = shape,
    blurRadius = blurRadius,
    noiseCoefficient = BlurDefaults.NoiseCoefficient,
    colors = colors,
    maxDownscaleFactor = BlurDefaults.MaxDownscaleFactor,
    contentBlendMode = contentBlendMode,
    enabled = true,
)
