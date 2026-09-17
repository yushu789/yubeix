// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

/**
 * Whether the current platform supports applying [androidx.compose.ui.graphics.RenderEffect]s
 * to graphics layers, which is the foundation of the backdrop blur pipeline.
 *
 * - Android: true on API 31 (S) and above.
 * - Skiko platforms (desktop, iOS, macOS, web): always true; the backdrop blur pipeline
 *   degrades to a Compose built-in blur approximation there.
 */
expect fun isRenderEffectSupported(): Boolean

/**
 * Whether the current platform supports runtime shaders (AGSL on Android), which power
 * the LM gaussian blur pipeline, noise dithering and the full custom blend mode set.
 *
 * - Android: true on API 33 (Tiramisu) and above.
 * - Skiko platforms: always false; effects relying on runtime shaders are no-ops and
 *   blur falls back to the built-in blur approximation.
 */
expect fun isRuntimeShaderSupported(): Boolean
