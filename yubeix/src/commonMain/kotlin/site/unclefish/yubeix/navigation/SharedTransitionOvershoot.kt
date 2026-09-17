// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/*
 * How a shared transition reads the scene progress handed to it by [SceneDisplay].
 *
 * That progress is a spring, so it leaves 0..1 at the end of a transition: [navigationSharedElementExitSpring]
 * settles a little past its target, and a released fling pushes further. Split in two, that is the
 * whole of the transition's physics:
 *
 * - [sharedTransitionExpansion] is the clamped 0..1 part. The window's size and everything tonal read
 *   only this - the cross-fades, the host screen's blur and sink - so each of them lands on exactly its
 *   endpoint the moment the transition ends, and nothing snaps when the transition's own flags flip
 *   off.
 * - [sharedTransitionOvershoot] is the part past the endpoints, which [sharedTransitionOvershootPast]
 *   turns into the distance the window carries on past the source it is landing on.
 *
 * [sharedTransitionWindow] is where the two meet: the plain interpolation from the source's rect out to
 * the whole screen, moved bodily by the carry.
 *
 * The carry moves the window and nothing else - every edge of it, and every pixel drawn inside it,
 * travels exactly the same distance. A landing can only hold one distance. It used to hold two, sliding
 * past the source by the carry while both sides gave the carry up as well, and a translation of one
 * distance against a squeeze of the same distance cancels on the edge the window is heading for and
 * doubles on the edge behind it. Which edge is which is only a matter of where the source sits, so a
 * word list row above the middle of the screen landed with its top edge moving a third as far as the
 * same row below it, while the row's own pixels moved the full carry: three distances on one row, the
 * smallest of them on the edge a list is read by, and a settle that flutters where it should bounce.
 * One distance, and every source lands the same way wherever it sits on screen.
 */

/**
 * Progress past an endpoint is capped here so a released fling cannot throw the window absurdly far
 * past its target. Well above the spring's own overshoot, so its bounce is never clipped - a clipped
 * bounce reads as a flat spot in the landing.
 */
private const val SHARED_TRANSITION_OVERSHOOT_LIMIT = 0.2f

/**
 * How far the window carries on past what it is landing on, per unit of the spring's overshoot. With
 * the closing spring's 1.5% this puts the landing's carry at about 8dp.
 *
 * An absolute distance, deliberately. The two scales tried before it were both wrong: a fraction of
 * the distance travelled made a word list row near the middle of the screen land flat while the same
 * row further down bounced, and a fraction of the source's own height made the tall row's bounce
 * overshoot the small button's by ×2.5 - reading as forceful on one and right on the other. The eye
 * measures the bounce in screen distance, not in travel and not in the source's size, so the same
 * handful of dp is what lands the same everywhere.
 */
private val SHARED_TRANSITION_OVERSHOOT_CARRY_PER_UNIT = 520.dp

/** A fling has no natural bound; the carry stops here. */
private val SHARED_TRANSITION_OVERSHOOT_MAX_CARRY = 32.dp

/**
 * Below this much travel there is no direction worth carrying along: the source is where the window
 * ends up anyway, and a fixed carry along a direction made of a few pixels of noise would fling it
 * somewhere arbitrary. The carry fades out over the last of it rather than switching off.
 */
private val SHARED_TRANSITION_OVERSHOOT_MIN_TRAVEL = 64.dp

/** The 0..1 part of [progress]: everything but the spring's overshoot. */
fun sharedTransitionExpansion(progress: Float): Float = progress.coerceIn(0f, 1f)

/** The part of [progress] past an endpoint: negative onto the source, positive past full screen. */
fun sharedTransitionOvershoot(progress: Float): Float = (progress - sharedTransitionExpansion(progress))
    .coerceIn(-SHARED_TRANSITION_OVERSHOOT_LIMIT, SHARED_TRANSITION_OVERSHOOT_LIMIT)

/**
 * How far the window is carried past the source it lands on: along the line it was already travelling
 * ([travel], the vector from the source's centre to the target's), by an absolute distance.
 *
 * Only a landing is carried. Past full screen is off screen, so a window that an interrupted open or a
 * released fling has pushed beyond it - a positive [overshoot] - has nowhere to be carried to; sliding
 * it out of the frame would only uncover the scene behind it, so it settles where it is instead.
 */
fun sharedTransitionOvershootPast(
    travel: Offset,
    overshoot: Float,
    density: Density,
): Offset {
    val landing = overshoot.coerceAtMost(0f)
    val length = travel.getDistance()
    if (length <= 0f || landing == 0f) {
        return Offset.Zero
    }
    val maxCarry = with(density) { SHARED_TRANSITION_OVERSHOOT_MAX_CARRY.toPx() }
    val minTravel = with(density) { SHARED_TRANSITION_OVERSHOOT_MIN_TRAVEL.toPx() }
    val carry = with(density) { SHARED_TRANSITION_OVERSHOOT_CARRY_PER_UNIT.toPx() * landing }
        .coerceIn(-maxCarry, maxCarry) * (length / minTravel).coerceAtMost(1f)
    return travel / length * carry
}

/**
 * The transition's window: [source] grown out to the whole of [full] by [expansion], then moved by
 * [carry].
 *
 * Both ends are exact - the source's own rect at 0, the whole screen at 1 - so the window is never a
 * pixel off the thing it is standing in for on the frames either side of a transition.
 */
fun sharedTransitionWindow(
    source: Rect,
    full: Size,
    expansion: Float,
    carry: Offset = Offset.Zero,
): Rect {
    val fraction = sharedTransitionExpansion(expansion)
    val width = lerp(source.width, full.width, fraction).coerceAtLeast(1f)
    val height = lerp(source.height, full.height, fraction).coerceAtLeast(1f)
    val centerX = lerp(source.center.x, full.width / 2f, fraction) + carry.x
    val centerY = lerp(source.center.y, full.height / 2f, fraction) + carry.y
    return Rect(
        left = centerX - width / 2f,
        top = centerY - height / 2f,
        right = centerX + width / 2f,
        bottom = centerY + height / 2f,
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
