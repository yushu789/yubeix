// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.anim

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import kotlin.math.PI
import kotlin.math.roundToInt

/**
 * A spring described the way duration-and-bounce describes one: [durationMillis] is how long the
 * animation takes in milliseconds, and [bounce] is how much it overshoots (positive) or how much
 * it relaxes toward the target (negative). 0 is a critically damped spring, so it never
 * overshoots or bounces; smaller [durationMillis] is faster.
 *
 * [visibilityThreshold] has to be sized to the value being animated rather than left at Compose's
 * 0.01 default. A spring animation ends the moment it is within the threshold of its target and the
 * value then jumps straight there, so a threshold that is coarse next to the animation's range
 * shows up as a snap in the final frame - 0.01 of a 0..1 progress is a whole percent of the travel.
 *
 * See also [yubeixSpring], the damping-and-response form of the same idea.
 */
fun <T> bounceSpring(
    durationMillis: Int = 500,
    bounce: Double = 0.0,
    visibilityThreshold: T? = null,
): SpringSpec<T> {
    require(durationMillis > 0) { "Duration must be positive" }

    val safeBounce = bounce.coerceIn(-0.99999, 1.0)
    val duration = durationMillis / 1000.0

    val stiffness = (2.0 * PI / duration).let { it * it }.toFloat()

    val dampingRatio = if (safeBounce >= 0) {
        (1.0 - safeBounce).toFloat()
    } else {
        (1.0 / (1.0 + safeBounce)).toFloat()
    }

    return spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
        visibilityThreshold = visibilityThreshold,
    )
}

/**
 * A critically damped spring - it never overshoots or bounces - described the way response time
 * describes one: [response] is the response time in seconds, and smaller is faster. This is the
 * no-bounce form of [bounceSpring]: [response] seconds maps straight to its durationMillis.
 */
fun noBounceSpring(
    response: Float,
    visibilityThreshold: Float,
): SpringSpec<Float> = bounceSpring(
    durationMillis = (response * 1000f).roundToInt(),
    bounce = 0.0,
    visibilityThreshold = visibilityThreshold,
)
