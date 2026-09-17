// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.animation.core.SpringSpec
import site.unclefish.yubeix.anim.bounceSpring

/**
 * Springs for every scene transition in [SceneDisplay]: page pushes and pops run on
 * [navigationEnterExitSpring], and the back-gesture settle on [navigationDragSettleSpring]. The
 * welcome flow's navigation rides the same springs; shared-element pairs ride the elastic
 * [navigationSharedElementSpring] and [navigationSharedElementDragSettleSpring] instead.
 *
 * The plain pair are no-bounce [AppSpring]s, so they never overshoot; the point of the spring is
 * only that it can absorb an interruption's velocity mid-flight. Their durations are picked so each
 * has 1% of the distance left at the same moment the equivalent tween does, which is where the
 * motion reads as finished.
 */

/** Progress is 0..1 across the whole page, so this is about a pixel of travel. */
private const val NavigationSpringProgressThreshold = 0.001f

/** Paced like the 430ms push/pop tween. */
fun navigationEnterExitSpring(): SpringSpec<Float> = bounceSpring(
    durationMillis = 340,
    bounce = 0.0,
    visibilityThreshold = NavigationSpringProgressThreshold,
)

/** Paced like the 320ms back-gesture settle tween. */
fun navigationDragSettleSpring(): SpringSpec<Float> = bounceSpring(
    durationMillis = 260,
    bounce = 0.0,
    visibilityThreshold = NavigationSpringProgressThreshold,
)

/**
 * How much a shared transition's spring overshoots its target. [AppSpring] maps this to a damping
 * ratio of `1 - bounce`, so 0.2 is a ratio of 0.8 and settles about 1.5% past the target.
 *
 * Deliberately shallow, because a single spring's overshoot arrives *after* its primary move: an
 * underdamped spring rushes to the target and then spends half a period coming back. At this period
 * the window is on the button by about 300ms and the overshoot lobe runs from there out to roughly
 * 380ms, so anything deep enough to read clearly - 11% and 16% were both tried - comes across as the
 * transition finishing and then jumping once more on its own. Shallow keeps it as the tail of the
 * landing instead. Elasticity that is meant to be *seen* cannot come from a terminal overshoot at
 * all; it would have to be a squash applied during the move, zero at both ends by construction.
 *
 * Only the closing springs carry it. A page growing into place has nowhere to overshoot to - past
 * full screen is off screen - so the open stays critically damped; the elasticity belongs to the
 * landing, where the window settles onto the button it came from.
 *
 * It lives in the scene's own spring rather than in a second one alongside it, because a shared
 * transition's window, its source and the page inside it all read the same scene progress: putting
 * the elasticity in that one clock is what makes them land elastically *together*. A spring of its
 * own would drift out of step, and it would keep running during a back gesture, where the progress
 * must be the finger and nothing else.
 */
private const val NavigationSharedElementBounce = 0.2

/**
 * Shared transitions are paced slower than a page push: the window travels much further - a button's
 * worth of screen out to the whole of it - and covers the distance in one continuous shape, so the
 * page's pace reads as a snap.
 *
 * The close is slower again. It is the direction that has somewhere to arrive: the window has to be
 * seen finding the button it came from, and it carries the bounce, which needs room to read as one.
 */
private const val NavigationSharedElementEnterDurationMillis = 460
private const val NavigationSharedElementExitDurationMillis = 560
private const val NavigationSharedElementDragSettleDurationMillis = 400

/** Opening: no bounce, so the page grows into place instead of past it. */
fun navigationSharedElementEnterSpring(): SpringSpec<Float> = bounceSpring(
    durationMillis = NavigationSharedElementEnterDurationMillis,
    bounce = 0.0,
    visibilityThreshold = NavigationSpringProgressThreshold,
)

/** Closing: the elastic settle onto the source the window is collapsing back onto. */
fun navigationSharedElementExitSpring(): SpringSpec<Float> = bounceSpring(
    durationMillis = NavigationSharedElementExitDurationMillis,
    bounce = NavigationSharedElementBounce,
    visibilityThreshold = NavigationSpringProgressThreshold,
)

/** A released back gesture that closes: the same landing, at the gesture's own settle pace. */
fun navigationSharedElementDragSettleSpring(): SpringSpec<Float> = bounceSpring(
    durationMillis = NavigationSharedElementDragSettleDurationMillis,
    bounce = NavigationSharedElementBounce,
    visibilityThreshold = NavigationSpringProgressThreshold,
)

/** A released back gesture that snaps the page back open: no bounce, like the open. */
fun navigationSharedElementDragCancelSpring(): SpringSpec<Float> = bounceSpring(
    durationMillis = NavigationSharedElementDragSettleDurationMillis,
    bounce = 0.0,
    visibilityThreshold = NavigationSpringProgressThreshold,
)
