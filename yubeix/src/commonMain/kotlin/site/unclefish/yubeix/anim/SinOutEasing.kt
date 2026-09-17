// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.anim

import androidx.compose.animation.core.Easing
import kotlin.math.PI
import kotlin.math.sin

val SinOutEasing: Easing = Easing { fraction ->
    sin((fraction * PI / 2).toFloat())
}
