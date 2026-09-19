// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import com.kyant.shapes.UnevenRoundedRectangle

/**
 * Returns a kyant [RoundedRectangle] with G2-continuous corners, the standard yubeix corner
 * treatment. kyant shapes default to [com.kyant.shapes.RoundedCornerStyle.Continuous], so the
 * corners stay round instead of the sharper miuix shape.
 */
@Composable
fun yubeixShape(cornerRadius: Dp): Shape = remember(cornerRadius) { RoundedRectangle(cornerRadius) }

/**
 * Returns a kyant [Capsule] (stadium) shape.
 */
@Composable
fun yubeixCapsuleShape(): Shape = remember { Capsule() }

/**
 * Returns a kyant [UnevenRoundedRectangle] with G2-continuous corners.
 */
@Composable
fun yubeixUnevenShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
): Shape = remember(topStart, topEnd, bottomEnd, bottomStart) {
    UnevenRoundedRectangle(
        topStart = topStart,
        topEnd = topEnd,
        bottomEnd = bottomEnd,
        bottomStart = bottomStart,
    )
}
