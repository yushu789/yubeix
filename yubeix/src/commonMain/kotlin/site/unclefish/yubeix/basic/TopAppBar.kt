// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import site.unclefish.yubeix.anim.yubeixSpring
import site.unclefish.yubeix.basic.TopAppBarState.Companion.Saver
import site.unclefish.yubeix.blur.BlurColors
import site.unclefish.yubeix.blur.LayerBackdrop
import site.unclefish.yubeix.blur.isRenderEffectSupported
import site.unclefish.yubeix.blur.textureBlur
import site.unclefish.yubeix.theme.LocalReducedDynamicEffectsEnabled
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.systemGroupedBackground
import site.unclefish.yubeix.utils.LocalCupertinoOverscrollState
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A [TopAppBar] with Yubeix style that can collapse and expand based on the
 * scroll position of the content below it.
 *
 * The [TopAppBar] can be configured with a title, a navigation icon, and action icons.
 * The large title will collapse when the content is scrolled up and expand when
 * the content is scrolled down.
 *
 * @param title The title of the [TopAppBar].
 * @param modifier The modifier to be applied to the  [TopAppBar].
 * @param color The background color of the [TopAppBar].
 * @param titleColor The color of the collapsed small title text.
 * @param largeTitle The large title of the [TopAppBar].
 * @param largeTitleColor The color of the expanded large title text.
 * @param navigationIcon The [Composable] content that represents the navigation icon.
 * @param actions The [Composable] content that represents the action icons.
 * @param scrollBehavior The [ScrollBehavior] that controls the behavior of the [TopAppBar].
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding to the [TopAppBar].
 * @param horizontalPadding The horizontal padding of the [TopAppBar]'s title & large title.
 */
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = YubeixTheme.colorScheme.surface,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    largeTitle: String = title,
    largeTitleColor: Color = YubeixTheme.colorScheme.onSurface,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    defaultWindowInsetsPadding: Boolean = true,
    horizontalPadding: Dp = TopAppBarDefaults.HorizontalPadding,
) {
    val largeTitleHeight = remember { mutableIntStateOf(0) }
    val expandedHeightPx by remember {
        derivedStateOf {
            largeTitleHeight.intValue.toFloat().coerceAtLeast(0f)
        }
    }

    SideEffect {
        // Sets the app bar's height offset to collapse the entire bar's height when content is scrolled.
        if (scrollBehavior?.state?.heightOffsetLimit != -expandedHeightPx) {
            scrollBehavior?.state?.heightOffsetLimit = -expandedHeightPx
        }
    }

    // Wrap the given actions in a Row.
    val actionsRow =
        @Composable {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }

    // Compose a Surface with a TopAppBarLayout content.
    // The surface's background color is animated as specified above.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    TopAppBarLayout(
        title = title,
        color = color,
        titleColor = titleColor,
        largeTitleColor = largeTitleColor,
        navigationIcon = navigationIcon,
        actions = actionsRow,
        horizontalPadding = horizontalPadding,
        scrolledOffset = { scrollBehavior?.state?.heightOffset ?: 0f },
        expandedHeightPx = expandedHeightPx,
        largeTitleHeight = largeTitleHeight,
        modifier = modifier,
        largeTitle = largeTitle,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
    )
}

/**
 * A [SmallTopAppBar] with Yubeix style.
 *
 * The [SmallTopAppBar] can be configured with a title, a navigation icon, and action icons.
 *
 * @param title The title of the [SmallTopAppBar].
 * @param modifier The modifier to be applied to the  [SmallTopAppBar].
 * @param color The background color of the [SmallTopAppBar].
 * @param titleColor The color of the title text.
 * @param navigationIcon The [Composable] content that represents the navigation icon.
 * @param actions The [Composable] content that represents the action icons.
 * @param scrollBehavior The [ScrollBehavior] that controls the behavior of the [SmallTopAppBar].
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding to the [SmallTopAppBar].
 * @param horizontalPadding The horizontal padding of the [SmallTopAppBar]'s title.
 */
@Composable
@NonRestartableComposable
fun SmallTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = YubeixTheme.colorScheme.surface,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    defaultWindowInsetsPadding: Boolean = true,
    horizontalPadding: Dp = TopAppBarDefaults.HorizontalPadding,
) {
    SideEffect {
        // Sets the height offset limit of the SmallTopAppBar to 0f
        // To ensure that the content can still scroll normally even when scrollBehavior is passed.
        scrollBehavior?.state?.heightOffsetLimit = 0f
    }

    // Wrap the given actions in a Row.
    val actionsRow =
        @Composable {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }

    // Compose a Surface with a SmallTopAppBarLayout content.
    // The surface's background color is animated as specified above.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    SmallTopAppBarLayout(
        title = title,
        color = color,
        titleColor = titleColor,
        navigationIcon = navigationIcon,
        actions = actionsRow,
        horizontalPadding = horizontalPadding,
        modifier = modifier,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
    )
}

/**
 * Returns a [ScrollBehavior] that adjusts its properties to affect the colors and
 * height of the top app bar.
 *
 * A top app bar that is set up with this [ScrollBehavior] will immediately collapse
 * when the nested content is pulled up, and will expand back the collapsed area when the
 * content is pulled all the way down.
 *
 * @param state the state object to be used to control or observe the top app bar's scroll
 *   state. See [rememberTopAppBarState] for a state that is remembered across compositions.
 * @param canScroll a callback used to determine whether scroll events are to be handled by this
 *   [ExitUntilCollapsedScrollBehavior]
 * @param snapAnimationSpec an optional [AnimationSpec] that defines how the top app bar snaps
 *   to either fully collapsed or fully extended state when a fling or a drag scrolled it into
 *   an intermediate position
 * @param flingAnimationSpec an optional [DecayAnimationSpec] that defined how to fling the top
 *   app bar when the user flings the app bar itself, or the content below it
 */
@Suppress("ComposableNaming")
@Composable
fun YubeixScrollBehavior(
    state: TopAppBarState = rememberTopAppBarState(),
    canScroll: () -> Boolean = { true },
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = 2500f),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
): ScrollBehavior = remember(state, canScroll, snapAnimationSpec, flingAnimationSpec) {
    ExitUntilCollapsedScrollBehavior(
        state = state,
        snapAnimationSpec = snapAnimationSpec,
        flingAnimationSpec = flingAnimationSpec,
        canScroll = canScroll,
    )
}

/**
 * Creates a [TopAppBarState] that is remembered across compositions.
 *
 * @param initialHeightOffsetLimit the initial value for [TopAppBarState.heightOffsetLimit], which
 *   represents the pixel limit that a top app bar is allowed to collapse when the scrollable
 *   content is scrolled
 * @param initialHeightOffset the initial value for [TopAppBarState.heightOffset]. The initial
 *   offset height offset should be between zero and [initialHeightOffsetLimit].
 * @param initialContentOffset the initial value for [TopAppBarState.contentOffset]
 */
@Composable
fun rememberTopAppBarState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f,
): TopAppBarState = rememberSaveable(saver = Saver) {
    TopAppBarState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)
}

/**
 * A state object that can be hoisted to control and observe the top app bar state. The state is
 * read and updated by a [ScrollBehavior] implementation.
 *
 * In most cases, this state will be created via [rememberTopAppBarState].
 *
 * @param initialHeightOffsetLimit the initial value for [TopAppBarState.heightOffsetLimit]
 * @param initialHeightOffset the initial value for [TopAppBarState.heightOffset]
 * @param initialContentOffset the initial value for [TopAppBarState.contentOffset]
 */
@Stable
class TopAppBarState(
    initialHeightOffsetLimit: Float,
    initialHeightOffset: Float,
    initialContentOffset: Float,
) {

    /**
     * The top app bar's height offset limit in pixels, which represents the limit that a top app
     * bar is allowed to collapse to.
     *
     * Use this limit to coerce the [heightOffset] value when it's updated.
     */
    var heightOffsetLimit = initialHeightOffsetLimit

    /**
     * The top app bar's current height offset in pixels. This height offset is applied to the fixed
     * height of the app bar to control the displayed height when content is being scrolled.
     *
     * Updates to the [heightOffset] value are coerced between zero and [heightOffsetLimit].
     */
    var heightOffset: Float
        get() = _heightOffset.floatValue
        set(newOffset) {
            _heightOffset.floatValue =
                newOffset.coerceIn(minimumValue = heightOffsetLimit, maximumValue = 0f)
        }

    /**
     * The total offset of the content scrolled under the top app bar.
     *
     * The content offset is used to compute the [overlappedFraction], which can later be read by an
     * implementation.
     *
     * This value is updated by a [ScrollBehavior] whenever a nested scroll connection
     * consumes scroll events. A common implementation would update the value to be the sum of all
     * [NestedScrollConnection.onPostScroll] `consumed.y` values.
     */
    var contentOffset by mutableFloatStateOf(initialContentOffset)

    /**
     * A value that represents the collapsed height percentage of the app bar.
     *
     * A `0.0` represents a fully expanded bar, and `1.0` represents a fully collapsed bar (computed
     * as [heightOffset] / [heightOffsetLimit]).
     */
    val collapsedFraction: Float
        get() =
            if (heightOffsetLimit != 0f) {
                heightOffset / heightOffsetLimit
            } else {
                0f
            }

    /**
     * A value that represents the percentage of the app bar area that is overlapping with the
     * content scrolled behind it.
     *
     * A `0.0` indicates that the app bar does not overlap any content, while `1.0` indicates that
     * the entire visible app bar area overlaps the scrolled content.
     */
    val overlappedFraction: Float
        get() =
            if (heightOffsetLimit != 0f) {
                1 -
                    (
                        (heightOffsetLimit - contentOffset).coerceIn(
                            minimumValue = heightOffsetLimit,
                            maximumValue = 0f,
                        ) / heightOffsetLimit
                        )
            } else {
                0f
            }

    companion object {
        /** The default [Saver] implementation for [TopAppBarState]. */
        val Saver: Saver<TopAppBarState, *> =
            listSaver(
                save = { listOf(it.heightOffsetLimit, it.heightOffset, it.contentOffset) },
                restore = {
                    TopAppBarState(
                        initialHeightOffsetLimit = it[0],
                        initialHeightOffset = it[1],
                        initialContentOffset = it[2],
                    )
                },
            )
    }

    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)
}

/** Contains default values used by [TopAppBar] and [SmallTopAppBar]. */
object TopAppBarDefaults {
    /** The default horizontal padding of the title and large title. */
    val HorizontalPadding = 26.dp

    /** The default collapsed height of the [TopAppBar]. */
    val CollapsedHeight = 56.dp

    /** The vertical center height used for [SmallTopAppBar] layout. */
    val SmallTopAppBarCenterHeight = 60.dp
}

@Stable
interface ScrollBehavior {

    /**
     * A [TopAppBarState] that is attached to this behavior and is read and updated when scrolling
     * happens.
     */
    val state: TopAppBarState

    /**
     * Indicates whether the top app bar is pinned.
     *
     * A pinned app bar will stay fixed in place when content is scrolled and will not react to any
     * drag gestures.
     */
    val isPinned: Boolean

    /**
     * An optional [AnimationSpec] that defines how the top app bar snaps to either fully collapsed
     * or fully extended state when a fling or a drag scrolled it into an intermediate position.
     */
    val snapAnimationSpec: AnimationSpec<Float>?

    /**
     * An optional [DecayAnimationSpec] that defined how to fling the top app bar when the user
     * flings the app bar itself, or the content below it.
     */
    val flingAnimationSpec: DecayAnimationSpec<Float>?

    /**
     * A [NestedScrollConnection] that should be attached to a [Modifier.nestedScroll] in order to
     * keep track of the scroll events.
     */
    val nestedScrollConnection: NestedScrollConnection
}

/**
 * A [ScrollBehavior] that adjusts its properties to affect the colors and height of a top
 * app bar.
 *
 * A top app bar that is set up with this [ScrollBehavior] will immediately collapse when
 * the nested content is pulled up, and will expand back the collapsed area when the content is
 * pulled all the way down.
 *
 * @param state a [TopAppBarState]
 * @param snapAnimationSpec an optional [AnimationSpec] that defines how the top app bar snaps to
 *   either fully collapsed or fully extended state when a fling or a drag scrolled it into an
 *   intermediate position
 * @param flingAnimationSpec an optional [DecayAnimationSpec] that defined how to fling the top app
 *   bar when the user flings the app bar itself, or the content below it
 * @param canScroll a callback used to determine whether scroll events are to be handled by this
 *   [ExitUntilCollapsedScrollBehavior]
 */
private class ExitUntilCollapsedScrollBehavior(
    override val state: TopAppBarState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true },
) : ScrollBehavior {
    override val isPinned: Boolean = false
    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Don't intercept if scrolling down.
                if (!canScroll() || available.y > 0) return Offset.Zero
                val prevHeightOffset = state.heightOffset
                state.heightOffset += available.y
                return if (prevHeightOffset != state.heightOffset) {
                    // We're in the middle of top app bar collapse or expand.
                    // Consume only the scroll on the Y axis.
                    available.copy(x = 0f)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (!canScroll()) return Offset.Zero
                state.contentOffset += consumed.y

                if (available.y < 0f || consumed.y < 0f) {
                    // When scrolling up, just update the state's height offset.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset += consumed.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }

                if (available.y > 0f) {
                    // Adjust the height offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset += available.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 0) {
                    // Reset the total content offset to zero when scrolling all the way down. This
                    // will eliminate some float precision inaccuracies.
                    state.contentOffset = 0f
                }
                val superConsumed = super.onPostFling(consumed, available)
                return superConsumed +
                    settleAppBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }
        }
}

/**
 * Settles the app bar to a stable state (fully expanded or collapsed) by animating
 * its height offset.
 *
 * This function is invoked after a drag or fling gesture, using the provided velocity
 * to drive a decay animation, followed by a snap animation if the bar is left in an
 * intermediate state.
 *
 * @param state The [TopAppBarState] that holds the current and target height offsets.
 * @param velocity The velocity from the fling gesture to be consumed.
 * @param flingAnimationSpec The [DecayAnimationSpec] for the fling animation.
 * @param snapAnimationSpec The [AnimationSpec] for the final snap to a stable state.
 * @return The [Velocity] that was actually consumed by the fling decay animation. This
 * ensures accurate reporting within the nested scroll system, allowing any unconsumed
 * velocity to be propagated to parent consumers.
 */
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(
            flingAnimationSpec,
        ) {
            val delta = value - lastValue
            val initialHeightOffset = state.heightOffset
            state.heightOffset = initialHeightOffset + delta
            val consumed = abs(initialHeightOffset - state.heightOffset)
            lastValue = value
            remainingVelocity = this.velocity
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec,
            ) {
                state.heightOffset = value
            }
        }
    }
    return Velocity(0f, velocity - remainingVelocity)
}

/** A functional interface for providing an app-bar scroll offset. */
private fun interface ScrolledOffset {
    fun offset(): Float
}

/**
 * The base [Layout] for [TopAppBar]. This function lays out a [TopAppBar] navigation icon
 * (leading icon), a title (header), and action icons (trailing icons). Note that the navigation and
 * the actions are optional.
 *
 * @param title the [TopAppBar] title (header).
 * @param color the background color of the [TopAppBar].
 * @param titleColor the color of the collapsed small title text.
 * @param largeTitleColor the color of the expanded large title text.
 * @param navigationIcon a navigation icon [Composable].
 * @param actions actions [Composable].
 * @param horizontalPadding the horizontal padding of the [TopAppBar]'s title & large title.
 * @param scrolledOffset a function that provides the scroll offset of the [TopAppBar].
 * @param largeTitleHeight a mutable state that holds the height of the large title.
 * @param expandedHeightPx the expanded height of the [TopAppBar] in pixels.
 * @param modifier the [Modifier] to be applied to this layout.
 * @param largeTitle the large title of the [TopAppBar], if not specified, it will be the same as title.
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [TopAppBar].
 */
@Composable
private fun TopAppBarLayout(
    title: String,
    color: Color,
    titleColor: Color,
    largeTitleColor: Color,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    horizontalPadding: Dp,
    scrolledOffset: ScrolledOffset,
    expandedHeightPx: Float,
    largeTitleHeight: MutableState<Int>,
    modifier: Modifier = Modifier,
    largeTitle: String = title,
    defaultWindowInsetsPadding: Boolean = true,
) {
    // Subtract the scrolledOffset from the maxHeight
    val heightOffset by remember(scrolledOffset) {
        derivedStateOf {
            val offset = scrolledOffset.offset()
            if (offset.isNaN()) 0 else offset.roundToInt()
        }
    }

    // Small Title Animation
    val extOffset by remember(heightOffset) {
        derivedStateOf {
            abs(heightOffset) / expandedHeightPx * 3
        }
    }

    // Large Title Alpha Animation
    val largeTitleAlpha by remember(heightOffset, expandedHeightPx) {
        derivedStateOf {
            1f - (abs(heightOffset) / expandedHeightPx * 3).coerceIn(0f, 1f)
        }
    }

    // Small title animation is triggered once when the threshold is crossed
    // then runs independently to completion
    val smallTitleVisible = extOffset >= 1f
    val smallTitleAlpha = remember { Animatable(0f) }
    val smallTitleTranslationY = remember { Animatable(20f) }

    LaunchedEffect(smallTitleVisible) {
        if (smallTitleVisible) {
            val showSpec = yubeixSpring<Float>(damping = 1.0f, response = 0.3f)
            launch { smallTitleAlpha.animateTo(1f, showSpec) }
            launch { smallTitleTranslationY.animateTo(0f, showSpec) }
        } else {
            val hideSpec = yubeixSpring<Float>(damping = 1.0f, response = 0.15f)
            launch { smallTitleAlpha.animateTo(0f, hideSpec) }
            launch { smallTitleTranslationY.animateTo(20f, hideSpec) }
        }
    }

    // Title color transition animation
    val animatedTitleColor by animateColorAsState(
        targetValue = titleColor,
        animationSpec = tween(durationMillis = 50),
    )
    val animatedLargeTitleColor by animateColorAsState(
        targetValue = largeTitleColor,
        animationSpec = tween(durationMillis = 50),
    )

    Layout(
        {
            Box(
                Modifier
                    .layoutId("navigationIcon"),
            ) {
                navigationIcon()
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = horizontalPadding)
                    .graphicsLayer {
                        alpha = smallTitleAlpha.value
                        translationY = smallTitleTranslationY.value
                    },
            ) {
                Text(
                    text = title,
                    color = animatedTitleColor,
                    fontSize = YubeixTheme.textStyles.title3.fontSize,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
            Box(
                Modifier
                    .layoutId("actionIcons"),
            ) {
                actions()
            }
            Box(
                Modifier
                    .layoutId("largeTitle")
                    .padding(top = TopAppBarDefaults.CollapsedHeight)
                    .padding(horizontal = horizontalPadding)
                    .graphicsLayer { alpha = largeTitleAlpha },
            ) {
                Text(
                    modifier = Modifier.offset { IntOffset(0, heightOffset) },
                    text = largeTitle,
                    color = animatedLargeTitleColor,
                    fontSize = YubeixTheme.textStyles.title1.fontSize,
                    fontWeight = FontWeight.Normal,
                    onTextLayout = {
                        largeTitleHeight.value = it.size.height
                    },
                )
            }
        },
        modifier = modifier
            .then(Modifier.background(color))
            .then(
                if (defaultWindowInsetsPadding) {
                    Modifier
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                } else {
                    Modifier
                },
            )
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures { /* Consume click */ }
            },
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables
                .fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val actionIconsPlaceable =
            measurables
                .fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val maxTitleWidth = constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width

        val titlePlaceable =
            measurables
                .fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = (maxTitleWidth * 0.9).roundToInt(), minHeight = 0))

        val largeTitlePlaceable =
            measurables
                .fastFirst { it.layoutId == "largeTitle" }
                .measure(
                    constraints.copy(
                        minWidth = 0,
                        minHeight = 0,
                        maxHeight = Constraints.Infinity,
                    ),
                )

        val collapsedHeight = TopAppBarDefaults.CollapsedHeight.roundToPx()
        val expandedHeight = maxOf(
            collapsedHeight,
            largeTitlePlaceable.height,
        )

        val layoutHeight = lerp(
            start = collapsedHeight,
            stop = expandedHeight,
            fraction = if (expandedHeightPx > 0f) {
                val offset = scrolledOffset.offset()
                if (offset.isNaN()) 1f else (1f - (abs(offset) / expandedHeightPx).coerceIn(0f, 1f))
            } else {
                1f
            },
        ).toFloat().roundToInt()

        layout(constraints.maxWidth, layoutHeight) {
            val verticalCenter = collapsedHeight / 2

            // Navigation icon
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = verticalCenter - navigationIconPlaceable.height / 2,
            )

            // Title
            var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
            if (baseX < navigationIconPlaceable.width) {
                baseX += (navigationIconPlaceable.width - baseX)
            } else if (baseX + titlePlaceable.width > constraints.maxWidth - actionIconsPlaceable.width) {
                baseX += ((constraints.maxWidth - actionIconsPlaceable.width) - (baseX + titlePlaceable.width))
            }
            titlePlaceable.placeRelative(
                x = baseX,
                y = verticalCenter - titlePlaceable.height / 2,
            )

            // Action icons
            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = verticalCenter - actionIconsPlaceable.height / 2,
            )

            // Large title
            largeTitlePlaceable.placeRelative(
                x = 0,
                y = 0,
            )
        }
    }
}

/**
 * The base [Layout] for [SmallTopAppBar]. This function lays out a [SmallTopAppBar] navigation icon
 * (leading icon), a title (header), and action icons (trailing icons). Note that the navigation and
 * the actions are optional.
 *
 * @param title the [SmallTopAppBar] title (header).
 * @param color the background color of the [SmallTopAppBar].
 * @param titleColor the color of the title text.
 * @param navigationIcon a navigation icon [Composable].
 * @param actions actions [Composable].
 * @param horizontalPadding the horizontal padding of the [SmallTopAppBar]'s title.
 * @param modifier the [Modifier] to be applied to this layout.
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [SmallTopAppBar].
 */
@Composable
private fun SmallTopAppBarLayout(
    title: String,
    color: Color,
    titleColor: Color,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
    defaultWindowInsetsPadding: Boolean = true,
) {
    val titleModifier = remember(horizontalPadding) {
        Modifier
            .layoutId("title")
            .padding(horizontal = horizontalPadding)
    }

    // Title color transition animation
    val animatedTitleColor by animateColorAsState(
        targetValue = titleColor,
        animationSpec = tween(durationMillis = 50),
    )

    Layout(
        {
            Box(
                Modifier
                    .layoutId("navigationIcon"),
            ) {
                navigationIcon()
            }
            Box(titleModifier) {
                Text(
                    text = title,
                    color = animatedTitleColor,
                    maxLines = 1,
                    fontSize = YubeixTheme.textStyles.title3.fontSize,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
            Box(
                Modifier
                    .layoutId("actionIcons"),
            ) {
                actions()
            }
        },
        modifier = modifier
            .then(Modifier.background(color))
            .then(
                if (defaultWindowInsetsPadding) {
                    Modifier
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                } else {
                    Modifier
                },
            )
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .heightIn(max = TopAppBarDefaults.CollapsedHeight)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures { /* Consume click */ }
            },
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables
                .fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val actionIconsPlaceable =
            measurables
                .fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val maxTitleWidth = constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width

        val titlePlaceable =
            measurables
                .fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = (maxTitleWidth * 0.9).roundToInt(), minHeight = 0))

        val layoutHeight =
            if (constraints.maxHeight == Constraints.Infinity) {
                constraints.maxHeight
            } else {
                constraints.maxHeight
            }

        layout(constraints.maxWidth, layoutHeight) {
            val verticalCenter = TopAppBarDefaults.SmallTopAppBarCenterHeight.roundToPx() / 2

            // Navigation icon
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = verticalCenter - navigationIconPlaceable.height / 2,
            )

            // Title
            var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
            if (baseX < navigationIconPlaceable.width) {
                baseX += (navigationIconPlaceable.width - baseX)
            } else if (baseX + titlePlaceable.width > constraints.maxWidth - actionIconsPlaceable.width) {
                baseX += ((constraints.maxWidth - actionIconsPlaceable.width) - (baseX + titlePlaceable.width))
            }
            titlePlaceable.placeRelative(
                x = baseX,
                y = verticalCenter - titlePlaceable.height / 2,
            )

            // Action icons
            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = verticalCenter - actionIconsPlaceable.height / 2,
            )
        }
    }
}

// region Large-title screen chrome
// Ported from the wordmoment app's title bar. Unlike [TopAppBar] - which collapses itself from a
// nested-scroll [ScrollBehavior] - this chrome is driven explicitly by progress values: the large
// ("hero") title lives in the scroll content (see [ScreenScaffold]) and the bar only shows the
// collapsed centered title as its background fades in. This is why it is a separate public
// composable instead of extra parameters on [TopAppBar]: the two collapse models are irreconcilable.

/**
 * Easing used by progressive blur fades ([verticalProgressiveBlurLayerFade] users pick their own
 * progress curves): a double-smoothstep curve that keeps the fade flat near both ends.
 */
val TitleBarGradientBlurEasing: Easing = Easing { fraction ->
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val smoothstepFraction = clampedFraction * clampedFraction * (3f - 2f * clampedFraction)
    smoothstepFraction * smoothstepFraction
}

private val ReducedTitleBarDividerHeight = 0.6.dp
private val TitleBarTitleEnterOffset = 16.dp
private val TitleBarTitleMaxBlurRadius = 8.dp

// Frosted glass for the flat title bar. Same recipe as the app's glass surfaces: wide radius, no
// contrast change, a mild saturation lift so the blurred picture keeps its structure.
private const val TITLE_BAR_GLASS_BLUR_RADIUS = 56f

private const val PROGRESSIVE_BLUR_LAYER_FADE_SIZE = 0.16f

/**
 * Fades this layer's content out with a gradient alpha mask, for progressive blur stacks where the
 * blur strength ramps along the vertical axis: each layer is drawn at its own blur radius and then
 * masked by this fade, so the composite reads as one continuous gradient blur.
 *
 * @param zeroAtStart When true, the layer is fully transparent at its top edge and fully opaque
 *   towards its bottom; when false, the opposite.
 */
fun Modifier.verticalProgressiveBlurLayerFade(
    zeroAtStart: Boolean,
): Modifier = this
    .graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        val fadeSize = PROGRESSIVE_BLUR_LAYER_FADE_SIZE.coerceIn(0f, 1f)
        val maskStops = if (zeroAtStart) {
            arrayOf(
                0f to Color.Transparent,
                fadeSize to Color.White,
                1f to Color.White,
            )
        } else {
            arrayOf(
                0f to Color.White,
                (1f - fadeSize) to Color.White,
                1f to Color.Transparent,
            )
        }
        drawRect(
            brush = Brush.verticalGradient(colorStops = maskStops),
            blendMode = BlendMode.DstIn,
        )
    }

/** Reports the window Y coordinate of this layout's bottom edge on every position change. */
fun Modifier.onBottomPositionInWindowChanged(
    onBottomChanged: (Float) -> Unit,
): Modifier = onGloballyPositioned { coordinates ->
    onBottomChanged(coordinates.positionInWindow().y + coordinates.size.height)
}

/**
 * Whether the collapsed centered title of a large-title screen should be visible, given the window
 * position of the hero title's bottom edge. Includes a hysteresis band so the title does not
 * flicker when the hero title hovers around the collapse threshold while scrolling.
 *
 * @param heroTitleBottomInWindowPx The hero title's bottom edge in window coordinates, in pixels.
 * @param collapseThresholdPx The bottom position at or below which the collapsed title shows.
 * @param hysteresisPx The extra distance above the threshold within which the collapsed title
 *   stays visible once shown.
 * @param forceVisible When true, always report visible (e.g. pinned titles or scrolled past the
 *   first list item).
 */
@Composable
fun rememberCollapsedTitleVisible(
    heroTitleBottomInWindowPx: Float,
    collapseThresholdPx: Float,
    hysteresisPx: Float,
    forceVisible: Boolean = false,
): Boolean {
    var isVisibleByPosition by remember { mutableStateOf(false) }

    LaunchedEffect(
        heroTitleBottomInWindowPx,
        collapseThresholdPx,
        hysteresisPx,
    ) {
        when {
            heroTitleBottomInWindowPx <= collapseThresholdPx -> isVisibleByPosition = true

            heroTitleBottomInWindowPx >= collapseThresholdPx + hysteresisPx -> {
                isVisibleByPosition = false
            }
        }
    }

    return forceVisible || isVisibleByPosition
}

/**
 * Whether a bottom bar's scrim and blur should be showing.
 *
 * [scrolled] alone misses the overscroll case: the Cupertino effect translates the whole list
 * instead of producing a scroll offset, so dragging content down under the bar leaves the offset
 * at zero and the band would stay hidden exactly when content sits furthest under it. A downward
 * pull counts as scrolled for as long as it lasts.
 */
@Composable
fun rememberBottomBarBackgroundVisible(scrolled: Boolean): Boolean {
    val overscrollState = LocalCupertinoOverscrollState.current
    val pulledDown by remember(overscrollState) {
        derivedStateOf { (overscrollState?.offset?.y ?: 0f) > 0.5f }
    }
    return scrolled || pulledDown
}

@Composable
private fun topBarTitleTextStyle(): TextStyle = YubeixTheme.textStyles.main.copy(fontWeight = FontWeight.SemiBold)

@Composable
private fun topBarSubtitleTextStyle(): TextStyle = YubeixTheme.textStyles.footnote1

/**
 * The standard back affordance of the large-title screen chrome: a chevron glyph inside a
 * 42.dp touch target that dims to 42% alpha while pressed.
 *
 * @param onBack Invoked when the button is clicked.
 * @param modifier The [Modifier] to be applied to the button.
 * @param tint The color of the chevron glyph.
 * @param contentDescription Accessibility description of the button.
 */
@Composable
fun TopBarBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = YubeixTheme.colorScheme.onSurface,
    contentDescription: String = "Back",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val iconAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.42f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "TopBarBackIconAlpha",
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onBack,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BackIcon(
            modifier = Modifier.graphicsLayer { alpha = iconAlpha },
            tint = tint,
            contentDescription = null,
        )
    }
}

/**
 * Makes the title area of a top bar clickable without any visual indication, for screens that
 * open a picker or scroll to top when their title is tapped. A no-op when [onClick] is null.
 */
@Composable
fun Modifier.topBarTitleClickable(
    onClick: (() -> Unit)?,
): Modifier {
    if (onClick == null) return this
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    )
}

private val TOP_BAR_SCROLL_TO_TOP_EASING: Easing = CubicBezierEasing(0.22f, 0f, 0.18f, 1f)
private const val TOP_BAR_SCROLL_TO_TOP_MIN_DURATION_MILLIS = 320
private const val TOP_BAR_SCROLL_TO_TOP_MAX_DURATION_MILLIS = 760
private const val TOP_BAR_SCROLL_TO_TOP_PRE_JUMP_THRESHOLD = 24
private const val TOP_BAR_SCROLL_TO_TOP_PRE_JUMP_INDEX = 8

/**
 * Scrolls a lazy list back to its top with the title bar's scroll-to-top animation: a single
 * distance-proportional tween (instead of Compose's default multi-item fling) that pre-jumps
 * closer to the top for very long distances so the animation duration stays bounded.
 */
suspend fun LazyListState.animateScrollToTopFromTitleBar() {
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return

    if (firstVisibleItemIndex > TOP_BAR_SCROLL_TO_TOP_PRE_JUMP_THRESHOLD) {
        scrollToItem(TOP_BAR_SCROLL_TO_TOP_PRE_JUMP_INDEX)
    }

    val averageVisibleItemSize = layoutInfo.visibleItemsInfo
        .map { it.size }
        .average()
        .takeIf { it.isFinite() && it > 0.0 }
        ?.toFloat()
        ?: (layoutInfo.viewportSize.height * 0.18f).coerceAtLeast(1f)
    val estimatedDistanceToTop =
        (firstVisibleItemIndex * averageVisibleItemSize + firstVisibleItemScrollOffset)
            .coerceAtLeast(1f)
    val durationMillis = (
        TOP_BAR_SCROLL_TO_TOP_MIN_DURATION_MILLIS +
            estimatedDistanceToTop / 11f
        )
        .roundToInt()
        .coerceIn(
            TOP_BAR_SCROLL_TO_TOP_MIN_DURATION_MILLIS,
            TOP_BAR_SCROLL_TO_TOP_MAX_DURATION_MILLIS,
        )

    scroll {
        var previousAnimatedValue = estimatedDistanceToTop
        animate(
            initialValue = estimatedDistanceToTop,
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = TOP_BAR_SCROLL_TO_TOP_EASING,
            ),
        ) { currentAnimatedValue, _ ->
            val delta = currentAnimatedValue - previousAnimatedValue
            previousAnimatedValue = currentAnimatedValue
            scrollBy(delta)
        }
    }

    scrollToItem(0)
}

/**
 * A flat surface layer for the large-title screen chrome: a translucent fill plus a hairline
 * divider at the bottom edge.
 */
@Composable
internal fun FlatTitleBarSurfaceLayer(
    surfaceColor: Color,
    dividerColor: Color,
    modifier: Modifier = Modifier,
    backgroundAlpha: Float = 0.5f,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor.copy(alpha = backgroundAlpha)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(ReducedTitleBarDividerHeight)
                .background(dividerColor),
        )
    }
}

/**
 * The collapsing chrome of a large-title screen, in the app style this library is extracted from.
 *
 * The large title itself is not drawn here: it lives in the scroll content (see
 * [ScreenScaffold], which pairs this bar with a hero title). What this bar draws is the centered
 * collapsed title with an optional subtitle, a back button (or custom start content), and
 * trailing actions. Its collapse is driven explicitly rather than by a [ScrollBehavior]:
 *
 * - [titleAlpha] fades the centered title in as the hero title scrolls away (animate it, e.g.
 *   with [rememberCollapsedTitleVisible]).
 * - [backgroundVisibilityProgress] fades the bar's background in with the same progress, so the
 *   content scrolls underneath a fully transparent bar until collapse begins.
 * - [contentVisibilityProgress] and [contentOffsetY] shift and fade the whole foreground, for
 *   screens that animate the bar out vertically (detail pages, search modes).
 *
 * Background rendering picks the first available mode:
 * 1. [backdrop] + [glassColors] - a texture-blur glass brightened with the given colors.
 * 2. [hazeState] + render effect support - a haze frosted glass sampling the marked content.
 * 3. Otherwise - a flat translucent fill over [YubeixTheme] grouped background color.
 *
 * @param title The collapsed title shown centered in the bar.
 * @param onBack Invoked when the default back button is clicked. Ignored when [startContent] is
 *   provided; pass null for screens without back navigation.
 * @param subtitle Optional smaller line drawn under the title.
 * @param modifier The [Modifier] to be applied to the bar's container.
 * @param actions Content aligned to the end edge of the bar.
 * @param startContent Content aligned to the start edge of the bar, replacing the default back
 *   button when set.
 * @param centerContent Content drawn centered in the bar, replacing the title and subtitle when
 *   set.
 * @param topInset Additional top inset (typically the status bar height) padding the foreground
 *   content down inside the bar.
 * @param titleAlpha Drives the centered title's visibility progress, animated internally.
 * @param backgroundVisibilityProgress Drives the bar background's visibility progress, animated
 *   internally. Defaults to [titleAlpha].
 * @param contentVisibilityProgress Alpha of the bar's foreground content, applied directly.
 * @param contentOffsetY Vertical translation of the bar's foreground content.
 * @param titleColor The color of the title, the default back button, and the action content tint.
 * @param subtitleColor The color of the subtitle. Defaults to [titleColor] dimmed to 72% alpha.
 * @param onTitleClick Invoked when the centered title is clicked, without visual indication.
 * @param hazeState When set (and render effects are supported), the bar blurs the content marked
 *   with this state via haze instead of drawing a flat translucent fill.
 * @param actionSize The square size reserved at the bar edges for actions and the back button.
 * @param sidePadding Horizontal padding between the bar's edges and its start/end content.
 * @param backdrop A [LayerBackdrop] captured from the screen content, enabling the glass
 *   background mode together with [glassColors].
 * @param glassColors Colors for the glass background mode; see [textureBlur].
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
fun LargeTopAppBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: (@Composable () -> Unit)? = null,
    startContent: (@Composable () -> Unit)? = null,
    centerContent: (@Composable () -> Unit)? = null,
    topInset: Dp = 0.dp,
    titleAlpha: Float = 1f,
    backgroundVisibilityProgress: Float = titleAlpha,
    contentVisibilityProgress: Float = 1f,
    contentOffsetY: Dp = 0.dp,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    subtitleColor: Color = titleColor.copy(alpha = 0.72f),
    onTitleClick: (() -> Unit)? = null,
    hazeState: HazeState? = null,
    actionSize: Dp = 42.dp,
    sidePadding: Dp = 16.dp,
    backdrop: LayerBackdrop? = null,
    glassColors: BlurColors? = null,
) {
    val colors = YubeixTheme.colorScheme
    val density = LocalDensity.current
    val titleBarSurfaceColor = colors.systemGroupedBackground
    val reducedDynamicEffectsEnabled = LocalReducedDynamicEffectsEnabled.current
    val useHazeTitleBar =
        hazeState != null &&
            isRenderEffectSupported() &&
            !reducedDynamicEffectsEnabled
    val titleVisibilityProgress by animateFloatAsState(
        targetValue = titleAlpha.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 220),
        label = "AppTitleVisibility",
    )
    val backgroundVisibility by animateFloatAsState(
        targetValue = backgroundVisibilityProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 140),
        label = "AppTitleBackgroundVisibility",
    )
    val hazeStyle = remember(titleBarSurfaceColor) {
        HazeStyle(
            backgroundColor = titleBarSurfaceColor.copy(alpha = 1f),
            tints = emptyList(),
            blurRadius = 18.dp,
            noiseFactor = 0f,
            fallbackTint = HazeTint(titleBarSurfaceColor.copy(alpha = 0.58f)),
        )
    }
    val foregroundAlpha = contentVisibilityProgress.coerceIn(0f, 1f)
    val foregroundOffsetYPx = with(density) { contentOffsetY.toPx() }
    val titleEnterOffsetPx = with(density) { TitleBarTitleEnterOffset.toPx() }
    val titleBlurRadius = if (reducedDynamicEffectsEnabled) {
        0.dp
    } else {
        TitleBarTitleMaxBlurRadius * (1f - titleVisibilityProgress)
    }
    val resolvedSubtitle = subtitle?.takeIf { it.isNotBlank() }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = backgroundVisibility },
        ) {
            if (backdrop != null && glassColors != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .textureBlur(
                            backdrop = backdrop,
                            shape = RectangleShape,
                            blurRadius = TITLE_BAR_GLASS_BLUR_RADIUS,
                            colors = glassColors,
                        ),
                )
            } else if (useHazeTitleBar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeEffect(
                            state = requireNotNull(hazeState),
                            style = hazeStyle,
                        ) {
                            inputScale = HazeInputScale.Auto
                        },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .background(titleBarSurfaceColor.copy(alpha = 0.5f)),
                )
            }

            if (backdrop != null && glassColors != null) {
                // Glass mode keeps only the divider; the opaque fill would wash the brightening blur out.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(ReducedTitleBarDividerHeight)
                        .background(colors.dividerLine),
                )
            } else {
                FlatTitleBarSurfaceLayer(
                    surfaceColor = titleBarSurfaceColor,
                    dividerColor = colors.dividerLine,
                    backgroundAlpha = if (reducedDynamicEffectsEnabled) 0.5f else 0.75f,
                )
            }
        }

        if (foregroundAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset)
                    .padding(horizontal = sidePadding)
                    .graphicsLayer {
                        alpha = foregroundAlpha
                        translationY = foregroundOffsetYPx
                    },
            ) {
                if (startContent != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .zIndex(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        startContent()
                    }
                } else if (onBack != null) {
                    TopBarBackButton(
                        onBack = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .zIndex(1f),
                        tint = titleColor,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = actionSize + 12.dp)
                        .topBarTitleClickable(onTitleClick),
                    contentAlignment = Alignment.Center,
                ) {
                    if (centerContent != null) {
                        centerContent()
                    } else {
                        Column(
                            modifier = Modifier
                                .blur(
                                    radius = titleBlurRadius,
                                    edgeTreatment = BlurredEdgeTreatment.Unbounded,
                                )
                                .graphicsLayer {
                                    alpha = titleVisibilityProgress
                                    translationY = lerp(
                                        titleEnterOffsetPx,
                                        0f,
                                        titleVisibilityProgress,
                                    )
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = topBarTitleTextStyle(),
                                color = titleColor,
                            )
                            if (resolvedSubtitle != null) {
                                Text(
                                    text = resolvedSubtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = topBarSubtitleTextStyle(),
                                    color = subtitleColor,
                                )
                            }
                        }
                    }
                }

                if (actions != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .zIndex(1f),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        actions()
                    }
                }
            }
        }
    }
}
// endregion
