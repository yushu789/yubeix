// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur.internal

import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import site.unclefish.yubeix.blur.RuntimeShader
import site.unclefish.yubeix.blur.asAndroidRuntimeShader

@RequiresApi(Build.VERSION_CODES.S)
internal fun RenderEffect?.chain(other: RenderEffect): RenderEffect = if (this != null) {
    android.graphics.RenderEffect.createChainEffect(
        other.asAndroidRenderEffect(),
        this.asAndroidRenderEffect()
    ).asComposeRenderEffect()
} else {
    other
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun runtimeShaderEffect(
    runtimeShader: RuntimeShader,
    uniformShaderName: String
): RenderEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(
    runtimeShader.asAndroidRuntimeShader(),
    uniformShaderName
).asComposeRenderEffect()

@RequiresApi(Build.VERSION_CODES.S)
internal fun blurEffect(
    radiusX: Float,
    radiusY: Float,
): RenderEffect = android.graphics.RenderEffect.createBlurEffect(
    radiusX,
    radiusY,
    Shader.TileMode.CLAMP
).asComposeRenderEffect()

@RequiresApi(Build.VERSION_CODES.S)
internal fun colorFilterEffect(
    renderEffect: RenderEffect?,
    colorFilter: ColorFilter
): RenderEffect = if (renderEffect != null) {
    android.graphics.RenderEffect.createColorFilterEffect(
        colorFilter.asAndroidColorFilter(),
        renderEffect.asAndroidRenderEffect()
    ).asComposeRenderEffect()
} else {
    android.graphics.RenderEffect.createColorFilterEffect(
        colorFilter.asAndroidColorFilter()
    ).asComposeRenderEffect()
}
