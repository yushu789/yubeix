// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

/**
 * Skiko (desktop, iOS, macOS, web) actual: the Compose built-in blur primitive
 * ([androidx.compose.ui.graphics.BlurEffect]) is backed by Skia image filters and
 * available on every Skiko target, so the blur pipeline is always enabled.
 */
actual fun isRenderEffectSupported(): Boolean = true

/**
 * Skiko (desktop, iOS, macOS, web) actual: AGSL runtime shaders do not exist outside
 * Android. Effect stages that depend on them (custom blend colors, noise dithering
 * and the LM gaussian shader pipeline) are treated as no-ops by this source set,
 * and blur degrades to the built-in blur approximation.
 */
actual fun isRuntimeShaderSupported(): Boolean = false
