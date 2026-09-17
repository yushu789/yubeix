// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable

private const val PAGER_SNAP_STIFFNESS = 220f
private const val PAGER_SNAP_VISIBILITY_THRESHOLD_PX = 0.5f

/**
 * Moves a pager after a tab or navigation-button tap.
 *
 * Shares the fling snap's no-bounce spring so a tap and a flick settle the page the same way, and a
 * full-width change decelerates physically instead of riding a fixed-duration easing curve.
 */
suspend fun PagerState.animatePagerToPage(page: Int) {
    if (pageCount <= 0) return

    val targetPage = page.coerceIn(0, pageCount - 1)
    animateScrollToPage(
        page = targetPage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = PAGER_SNAP_STIFFNESS,
            visibilityThreshold = PAGER_SNAP_VISIBILITY_THRESHOLD_PX,
        ),
    )
}

/**
 * Keeps a finger-driven pager fully interactive, then uses a softer no-bounce spring for its final
 * snap. The spring preserves release velocity while avoiding the default pager's abrupt settle.
 */
@Composable
fun rememberPagerFlingBehavior(state: PagerState): TargetedFlingBehavior = PagerDefaults.flingBehavior(
    state = state,
    snapAnimationSpec = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = PAGER_SNAP_STIFFNESS,
        visibilityThreshold = PAGER_SNAP_VISIBILITY_THRESHOLD_PX,
    ),
)
