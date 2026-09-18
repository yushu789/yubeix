// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import site.unclefish.yubeix.anim.DampedDragAnimation
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixCapsuleShape
import site.unclefish.yubeix.theme.yubeixShape
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val TAB_ROW_INDICATOR_SPRING_VISIBILITY_THRESHOLD = 0.0001f
private const val TAB_ROW_INDICATOR_STIFFNESS = 300f
private const val TAB_ROW_COMMIT_HANDOFF_TIMEOUT_MILLIS = 700L

// A press that lands on the indicator has already declared its intent, so it only needs a fraction
// of the usual slop before the drag is claimed.
private const val TAB_ROW_INDICATOR_GRAB_SLOP_FACTOR = 0.35f

// How far the finger travels to step one tab, as a fraction of that tab's width. Below 1 so a flick
// switches without dragging the whole way across.
private const val TAB_ROW_STEP_STRIDE_FACTOR = 0.45f
private const val TAB_ROW_SHADOW_LAYER_COUNT = 5

// Per-layer alpha weights, innermost first: most of the shadow's weight hugs the indicator edge
// and falls off fast, approximating a Gaussian blur instead of a flat halo.
private val TAB_ROW_SHADOW_LAYER_ALPHAS = floatArrayOf(0.14f, 0.12f, 0.10f, 0.07f, 0.04f)
private val TAB_ROW_LABEL_WEIGHT = FontWeight.Medium

// Every tab row shares this curve so the indicator reads the same everywhere. Callers that want a
// different pace should not hand-roll one - change it here.
private val TabRowIndicatorAnimationSpec: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = TAB_ROW_INDICATOR_STIFFNESS,
    visibilityThreshold = TAB_ROW_INDICATOR_SPRING_VISIBILITY_THRESHOLD,
)

// A critically damped spring released from rest starts at zero velocity, which over the short hop
// between two tabs reads as the indicator hesitating before it sets off. Launching it at exactly
// omega x distance cancels the linear term of the critically damped solution and leaves a pure
// exponential decay: fastest on the very first frame, then a long natural settle. Compose springs
// use unit mass, so omega = sqrt(k).
private val TabRowIndicatorInitialVelocityFactor = sqrt(TAB_ROW_INDICATOR_STIFFNESS)

/**
 * A segmented [TabRow] with an iOS-style sliding indicator.
 *
 * The indicator animates between tabs on a shared spring, the tab labels recolour in step with the
 * travelling indicator, and - when every tab fits without scrolling - the indicator can be grabbed
 * and dragged to step through the tabs with haptic ticks.
 *
 * @param tabs The text to be displayed in the [TabRow].
 * @param selectedTabIndex The selected tab index of the [TabRow].
 * @param onTabSelected The callback when a tab is selected.
 * @param modifier The modifier to be applied to the [TabRow].
 * @param colors The colors of the [TabRow].
 * @param minWidth The minimum width of the tab in [TabRow].
 * @param maxWidth The maximum width of the tab in [TabRow].
 * @param height The height of the [TabRow].
 * @param cornerRadius The round corner radius of the tab in [TabRow].
 * @param itemSpacing The spacing between tabs in [TabRow].
 * @param contentAlignment The content alignment of the tab in [TabRow].
 * @param interactionSource The [MutableInteractionSource] to be used for the [TabRow].
 * @param indication The [Indication] to be used for the [TabRow].
 * @param widthMode How tab widths are computed: equal widths or adaptive to the text content.
 * @param itemHorizontalPadding The horizontal padding inside each tab, used by
 *   [TabRowWidthMode.ContentAdaptive] width measurement.
 * @param textStyle The text style of the tab labels.
 * @param hapticFeedbackEnabled Whether tab taps and drag steps trigger haptic feedback.
 * @param style The visual style: a filled sliding pill ([TabRowStyle.Filled]) or a bottom
 *   underline ([TabRowStyle.Flat]).
 * @param filledContourPadding The padding between the track edge and the tabs in
 *   [TabRowStyle.Filled].
 * @param selectedShadowBlurRadius The blur radius of the selected indicator's drop shadow.
 *   Zero disables the shadow.
 * @param selectedShadowOffsetY The vertical offset of the selected indicator's drop shadow.
 * @param selectedShadowColor The color of the selected indicator's drop shadow.
 * @param indicatorPosition An optional continuous indicator position in tab units. When non-null
 *   the indicator snaps there instead of animating; see [rememberPagerTabIndicatorPosition].
 */
@Composable
fun TabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: TabRowColors = TabRowDefaults.tabRowColors(),
    minWidth: Dp = TabRowDefaults.TabRowMinWidth,
    maxWidth: Dp = TabRowDefaults.TabRowMaxWidth,
    height: Dp = TabRowDefaults.TabRowHeight,
    cornerRadius: Dp = TabRowDefaults.TabRowCornerRadius,
    itemSpacing: Dp = TabRowDefaults.ItemSpacing,
    contentAlignment: Alignment = Alignment.Center,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    widthMode: TabRowWidthMode = TabRowWidthMode.Equal,
    itemHorizontalPadding: Dp = TabRowDefaults.ItemHorizontalPadding,
    textStyle: TextStyle = YubeixTheme.textStyles.body2,
    hapticFeedbackEnabled: Boolean = true,
    style: TabRowStyle = TabRowStyle.Filled,
    filledContourPadding: Dp = TabRowDefaults.FilledContourPadding,
    selectedShadowBlurRadius: Dp = TabRowDefaults.SelectedShadowBlurRadius,
    selectedShadowOffsetY: Dp = TabRowDefaults.SelectedShadowOffsetY,
    selectedShadowColor: Color = TabRowDefaults.SelectedShadowColor,
    indicatorPosition: Float? = null,
) {
    YubeixTabRow(
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        modifier = modifier,
        colors = colors,
        widthMode = widthMode,
        minWidth = minWidth,
        maxWidth = maxWidth,
        height = height,
        cornerRadius = cornerRadius,
        itemSpacing = itemSpacing,
        itemHorizontalPadding = itemHorizontalPadding,
        contentAlignment = contentAlignment,
        textStyle = textStyle,
        interactionSource = interactionSource,
        indication = indication,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        style = style,
        filledContourPadding = filledContourPadding,
        selectedShadowBlurRadius = selectedShadowBlurRadius,
        selectedShadowOffsetY = selectedShadowOffsetY,
        selectedShadowColor = selectedShadowColor,
        indicatorPosition = indicatorPosition,
    )
}

/**
 * A [TabRowWithContour] with Yubeix style: a filled sliding pill inside an inset track contour.
 *
 * @param tabs The text to be displayed in the [TabRowWithContour].
 * @param selectedTabIndex The selected tab index of the [TabRowWithContour].
 * @param onTabSelected The callback when a tab is selected.
 * @param modifier The modifier to be applied to the [TabRowWithContour].
 * @param colors The colors of the [TabRowWithContour].
 * @param minWidth The minimum width of the tab in [TabRowWithContour].
 * @param maxWidth The maximum width of the tab in [TabRowWithContour].
 * @param height The height of the [TabRowWithContour].
 * @param cornerRadius The round corner radius of the tab in [TabRowWithContour].
 * @param itemSpacing The spacing between tabs in [TabRowWithContour].
 * @param contentAlignment The content alignment of the tab in [TabRowWithContour].
 * @param interactionSource The [MutableInteractionSource] to be used for the [TabRowWithContour].
 * @param indication The [Indication] to be used for the [TabRowWithContour].
 */
@Composable
fun TabRowWithContour(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: TabRowColors = TabRowDefaults.tabRowColors(),
    minWidth: Dp = TabRowDefaults.TabRowWithContourMinWidth,
    maxWidth: Dp = TabRowDefaults.TabRowWithContourMaxWidth,
    height: Dp = TabRowDefaults.TabRowWithContourHeight,
    cornerRadius: Dp = TabRowDefaults.TabRowWithContourCornerRadius,
    itemSpacing: Dp = 5.dp,
    contentAlignment: Alignment = Alignment.Center,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
) {
    YubeixTabRow(
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        modifier = modifier,
        colors = colors,
        widthMode = TabRowWidthMode.Equal,
        minWidth = minWidth,
        maxWidth = maxWidth,
        height = height,
        cornerRadius = cornerRadius,
        itemSpacing = itemSpacing,
        itemHorizontalPadding = TabRowDefaults.ItemHorizontalPadding,
        contentAlignment = contentAlignment,
        textStyle = YubeixTheme.textStyles.body2,
        interactionSource = interactionSource,
        indication = indication,
        hapticFeedbackEnabled = true,
        style = TabRowStyle.Filled,
        filledContourPadding = 5.dp,
        selectedShadowBlurRadius = 0.dp,
        selectedShadowOffsetY = 0.dp,
        selectedShadowColor = Color.Transparent,
        indicatorPosition = null,
    )
}

/**
 * A segmented [TextTabs] component with an iOS-style capsule track: the selected tab is a capsule
 * whose indicator follows taps with a damped spring. Tabs share the width equally.
 *
 * @param tabs The text to be displayed in [TextTabs].
 * @param selectedTabIndex The selected tab index of [TextTabs].
 * @param onTabSelected The callback when a tab is selected.
 * @param modifier The modifier to be applied to [TextTabs].
 * @param height The height of [TextTabs].
 * @param textStyle The text style of the tab labels.
 */
@Composable
fun TextTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = TextTabsDefaults.Height,
    textStyle: TextStyle = YubeixTheme.textStyles.body2.copy(fontWeight = FontWeight.Medium),
) {
    if (tabs.isEmpty()) return

    val colors = YubeixTheme.colorScheme
    val isLightTheme = colors.surface.luminance() >= 0.5f
    val accentColor = if (isLightTheme) {
        androidx.compose.ui.graphics.lerp(colors.primary, Color.Black, 0.22f)
    } else {
        colors.primary
    }
    val contentColor = colors.onSurface
    val flatTrackColor = colors.surface
    val flatIndicatorColor = androidx.compose.ui.graphics.lerp(
        start = colors.surface,
        stop = accentColor,
        fraction = if (isLightTheme) 0.14f else 0.22f,
    )
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val animationScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val tabsCount = tabs.size
        val horizontalInset = TextTabsDefaults.HorizontalInset
        val tabWidth = with(density) {
            (this@BoxWithConstraints.constraints.maxWidth.toFloat() - horizontalInset.toPx() * 2f) / tabsCount
        }
        val capsuleShape = yubeixCapsuleShape()
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

        // Click-driven indicator: each selection springs to its tab on the same critically damped
        // curve the tab row uses, so the two components read identically in motion.
        val indicatorPosition = remember { Animatable(selectedTabIndex.fastCoerceIn(0, tabs.lastIndex).toFloat()) }
        LaunchedEffect(selectedTabIndex, tabsCount) {
            indicatorPosition.animateTo(
                targetValue = selectedTabIndex.fastCoerceIn(0, tabs.lastIndex).toFloat(),
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = 1000f,
                    visibilityThreshold = 0.001f,
                ),
            )
        }

        Box(
            modifier = Modifier
                .height(height)
                .fillMaxWidth()
                .clip(capsuleShape)
                .background(flatTrackColor)
                .padding(horizontalInset),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f / tabsCount)
                    .graphicsLayer {
                        translationX =
                            if (isLtr) {
                                indicatorPosition.value * tabWidth
                            } else {
                                size.width - (indicatorPosition.value + 1f) * tabWidth
                            }
                    }
                    .clip(capsuleShape)
                    .background(flatIndicatorColor),
            )

            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, title ->
                    TextTab(
                        text = title,
                        textColor = if (index == selectedTabIndex) accentColor else contentColor.copy(alpha = 0.72f),
                        textStyle = textStyle,
                        onClick = {
                            // Animate straight away so the pill moves even when the caller keeps the
                            // selection uncontrolled; the LaunchedEffect above settles any external
                            // change onto the same curve.
                            animationScope.launch {
                                indicatorPosition.animateTo(
                                    targetValue = index.fastCoerceIn(0, tabs.lastIndex).toFloat(),
                                    animationSpec = spring(
                                        dampingRatio = 1f,
                                        stiffness = 1000f,
                                        visibilityThreshold = 0.001f,
                                    ),
                                )
                            }
                            currentOnTabSelected(index)
                        },
                    )
                }
            }
        }
    }
}

/** Contains default values used by [TextTabs]. */
object TextTabsDefaults {
    /** The default height of [TextTabs]. */
    val Height = 46.dp

    /** The default horizontal inset between the track edge and the tabs. */
    val HorizontalInset = 4.dp

    /** The default horizontal padding inside each tab. */
    val TextPadding = 8.dp
}

@Composable
private fun RowScope.TextTab(
    text: String,
    textColor: Color,
    textStyle: TextStyle,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(yubeixCapsuleShape())
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .fillMaxHeight()
            .weight(1f)
            .padding(horizontal = TextTabsDefaults.TextPadding),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = textStyle.copy(color = textColor),
            maxLines = 1,
        )
    }
}

/**
 * A position for [TabRow]'s `indicatorPosition` that is non-null **only while the pager is
 * following the user's finger**.
 *
 * Binding the indicator to pager progress unconditionally cannot tell a swipe from a tab tap. A tap
 * scrolls the pager a whole page width, and an indicator tied to that progress crosses one tab in
 * the time the page crosses the screen - which reads far quicker than every other tab row. Returning
 * null for the tap case hands the indicator back to [TabRow]'s own animation, so only a real swipe
 * drags it 1:1.
 */
@Composable
fun rememberPagerTabIndicatorPosition(pagerState: PagerState): Float? {
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var followsFinger by remember(pagerState) { mutableStateOf(false) }

    LaunchedEffect(pagerState, isDragged) {
        if (isDragged) {
            followsFinger = true
        } else if (followsFinger) {
            // Stay latched through the fling that settles the page, or lifting a finger mid-swipe
            // would snap the indicator back to the outgoing tab before the page has landed.
            snapshotFlow { pagerState.isScrollInProgress }.first { !it }
            followsFinger = false
        }
    }

    return if (followsFinger) {
        pagerState.currentPage + pagerState.currentPageOffsetFraction
    } else {
        null
    }
}

/**
 * The shared engine behind [TabRow] and [TabRowWithContour].
 */
@Composable
private fun YubeixTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    colors: TabRowColors,
    widthMode: TabRowWidthMode,
    minWidth: Dp,
    maxWidth: Dp,
    height: Dp,
    cornerRadius: Dp,
    itemSpacing: Dp,
    itemHorizontalPadding: Dp,
    contentAlignment: Alignment,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
    hapticFeedbackEnabled: Boolean,
    style: TabRowStyle,
    filledContourPadding: Dp,
    selectedShadowBlurRadius: Dp,
    selectedShadowOffsetY: Dp,
    selectedShadowColor: Color,
    indicatorPosition: Float?,
    modifier: Modifier = Modifier,
) {
    if (tabs.isEmpty()) return

    val selectedIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex)
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val contourPadding = if (style == TabRowStyle.Filled) filledContourPadding else 0.dp
    val outerShape = yubeixShape(cornerRadius + contourPadding)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .height(height),
    ) {
        val contentWidth = this.maxWidth - contourPadding * 2
        val config = rememberTabRowConfig(
            tabs = tabs,
            widthMode = widthMode,
            minWidth = minWidth,
            maxWidth = maxWidth,
            cornerRadius = cornerRadius,
            spacing = itemSpacing,
            itemHorizontalPadding = itemHorizontalPadding,
            availableWidth = contentWidth,
            textStyle = textStyle,
        )
        val density = LocalDensity.current
        val selectedTabTarget = tabRowIndicatorTarget(
            config = config,
            position = selectedIndex.toFloat(),
            density = density,
        )

        // Press-and-slide selection: the indicator is dragged to a continuous position in tab units
        // and the tab it rounds to is committed on release. Only offered when every tab fits, because
        // a row that overflows owes a horizontal drag to its own scrolling.
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val hapticFeedback = LocalHapticFeedback.current
        val animationScope = rememberCoroutineScope()
        val currentConfig by rememberUpdatedState(config)
        val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
        val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)
        // The gesture lambda outlives the composition that created it, so the selection it falls
        // back to has to be read live.
        val currentSelectedIndex by rememberUpdatedState(selectedIndex)
        val dragEnabled = tabs.size > 1 && !config.isScrollable
        // Which tab a drag in progress has stepped to. The indicator is never parked between tabs:
        // the drag measures how far the finger has travelled and steps a whole tab at a time, each
        // hop animated on the shared curve.
        var steppedIndex by remember { mutableStateOf<Int?>(null) }
        // What a just-released drag committed, held until the caller's own selection reports back.
        // The selection is hoisted, so it is still the outgoing tab on the frame the finger lifts;
        // without this the indicator would set off towards the old tab for a frame and snap back.
        var committedIndex by remember { mutableStateOf<Int?>(null) }
        // Drives the press feedback only.
        val pressAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = 0f,
                valueRange = 0f..1f,
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = TabRowDefaults.PRESSED_INDICATOR_SCALE,
                onDragStarted = {},
                onDragStopped = {},
                onDrag = { _, _ -> },
            )
        }

        LaunchedEffect(committedIndex) {
            val committed = committedIndex ?: return@LaunchedEffect
            // Bounded, because a caller is free to ignore the selection; when it does, the indicator
            // should animate back to where the caller actually is rather than stay parked.
            withTimeoutOrNull(TAB_ROW_COMMIT_HANDOFF_TIMEOUT_MILLIS) {
                snapshotFlow { selectedIndex }.first { it == committed }
            }
            committedIndex = null
        }

        // A pager following a finger is the only thing that positions the indicator between tabs,
        // and it is snapped to because it is tracking a gesture 1:1. Everything else is a whole tab
        // and is animated.
        val activeIndicatorTarget = tabRowIndicatorTarget(
            config = config,
            position = indicatorPosition
                ?: (steppedIndex ?: committedIndex ?: selectedIndex).toFloat(),
            density = density,
        )
        val indicatorOffset = remember {
            Animatable(activeIndicatorTarget.offsetPx)
        }
        val indicatorWidth = remember {
            Animatable(activeIndicatorTarget.widthPx)
        }

        LaunchedEffect(
            indicatorPosition,
            activeIndicatorTarget.offsetPx,
            activeIndicatorTarget.widthPx,
        ) {
            if (indicatorPosition != null) {
                indicatorOffset.snapTo(activeIndicatorTarget.offsetPx)
                indicatorWidth.snapTo(activeIndicatorTarget.widthPx)
            } else {
                launch {
                    indicatorOffset.animateTo(
                        targetValue = activeIndicatorTarget.offsetPx,
                        animationSpec = TabRowIndicatorAnimationSpec,
                        initialVelocity = (activeIndicatorTarget.offsetPx - indicatorOffset.value) *
                            TabRowIndicatorInitialVelocityFactor,
                    )
                }
                launch {
                    indicatorWidth.animateTo(
                        targetValue = activeIndicatorTarget.widthPx,
                        animationSpec = TabRowIndicatorAnimationSpec,
                        initialVelocity = (activeIndicatorTarget.widthPx - indicatorWidth.value) *
                            TabRowIndicatorInitialVelocityFactor,
                    )
                }
            }
        }

        val dragModifier = if (!dragEnabled) {
            Modifier
        } else {
            Modifier.pointerInput(tabs.size, isLtr) {
                awaitEachGesture {
                    // requireUnconsumed = false: a tab's clickable consumes the down, and it should
                    // keep doing so - a press is still a tap until it travels past the slop.
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var pointerId = down.id
                    var travelled = 0f
                    var isDrag = false
                    var isPressed = false
                    // The tab the drag is currently on, and the finger position that put it there.
                    // Distance is measured from that anchor rather than from the start of the
                    // gesture, so each further step needs its own full stride of travel.
                    var currentIndex = currentSelectedIndex
                    var anchorX = if (isLtr) down.position.x else size.width - down.position.x

                    // Grabbing the indicator itself earns the press feedback straight away; a press
                    // anywhere else is a tap until proven otherwise.
                    val indicatorStart = indicatorOffset.value
                    val grabbedIndicator =
                        down.position.x in indicatorStart..(indicatorStart + indicatorWidth.value)
                    if (grabbedIndicator) {
                        isPressed = true
                        pressAnimation.press()
                    }
                    // A finger already on the indicator has declared its intent, so it earns a much
                    // shorter threshold. Making it wait the full slop is what reads as the drag not
                    // responding. Elsewhere the full slop still protects the tap.
                    val dragThreshold = if (grabbedIndicator) {
                        viewConfiguration.touchSlop * TAB_ROW_INDICATOR_GRAB_SLOP_FACTOR
                    } else {
                        viewConfiguration.touchSlop
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.fastFirstOrNull { it.id == pointerId }
                            ?: event.changes.fastFirstOrNull { it.pressed }
                            ?: break
                        pointerId = change.id
                        if (change.changedToUpIgnoreConsumed()) break

                        val logicalX =
                            if (isLtr) change.position.x else size.width - change.position.x
                        travelled += change.positionChange().x
                        if (!isDrag && abs(travelled) > dragThreshold) {
                            isDrag = true
                            // Measure from where the drag was claimed, not from the touch down, or
                            // the slop already spent would count towards the first step.
                            anchorX = logicalX
                            if (!isPressed) {
                                isPressed = true
                                pressAnimation.press()
                            }
                        }
                        if (!isDrag) continue

                        // Consuming cancels the tab's own click, so a drag never also fires the tap
                        // it started life as.
                        change.consume()

                        // A stride is a fraction of a tab's width, so a short flick switches without
                        // having to haul the finger the whole way across.
                        val stridePx = currentConfig.tabWidths.getOrNull(currentIndex)
                            ?.toPx()
                            ?.times(TAB_ROW_STEP_STRIDE_FACTOR)
                            ?: continue
                        val steps = ((logicalX - anchorX) / stridePx).toInt()
                        if (steps == 0) continue

                        val next = (currentIndex + steps).fastCoerceIn(0, tabs.lastIndex)
                        if (next == currentIndex) continue
                        // Re-anchor by whole strides only, so the leftover travel within the current
                        // stride carries into the next step and the feel stays even.
                        anchorX += (next - currentIndex) * stridePx
                        currentIndex = next
                        steppedIndex = next
                        if (currentHapticEnabled) {
                            currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        }
                    }

                    if (isDrag) {
                        val target = currentIndex
                        // Both in the same frame: the indicator carries on to the committed tab
                        // without the resting position dropping back to the stale selection.
                        committedIndex = target
                        steppedIndex = null
                        currentOnTabSelected(target)
                    }
                    if (isPressed) pressAnimation.release()
                }
            }
        }

        LaunchedEffect(selectedIndex, contentWidth, selectedTabTarget.widthPx) {
            val selectedTabWidth = with(density) { selectedTabTarget.widthPx.toDp() }
            val centerOffset = (contentWidth - selectedTabWidth) / 2
            val offsetPx = with(density) { -centerOffset.toPx() }.roundToInt()
            config.listState.animateScrollToItem(selectedIndex, offsetPx)
        }

        val scrollOffset by remember(config) {
            derivedStateOf {
                val state = config.listState
                config.tabOffsetsPx.getOrElse(state.firstVisibleItemIndex) { 0f } +
                    state.firstVisibleItemScrollOffset
            }
        }

        // Read back off the indicator's own animation so the labels recolour in step with the
        // travelling indicator instead of snapping the moment the selection changes.
        val indicatorVisualPosition by remember(config) {
            derivedStateOf {
                tabRowPositionForX(
                    config = config,
                    xPx = indicatorOffset.value + indicatorWidth.value / 2f,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Keeps the selected indicator's drop shadow inside the track. The shadow is sized
                // to fade out within [filledContourPadding], so the clip has little left to cut.
                .then(
                    if (style == TabRowStyle.Filled) {
                        Modifier.clip(outerShape)
                    } else {
                        Modifier
                    },
                )
                .background(colors.backgroundColor(selected = false))
                .padding(contourPadding)
                .then(dragModifier),
        ) {
            when (style) {
                TabRowStyle.Filled -> {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset((indicatorOffset.value - scrollOffset).roundToInt(), 0)
                            }
                            .width(with(density) { indicatorWidth.value.toDp() })
                            .fillMaxHeight()
                            .then(
                                if (dragEnabled) {
                                    Modifier.graphicsLayer {
                                        scaleX = pressAnimation.scaleX
                                        scaleY = pressAnimation.scaleY
                                    }
                                } else {
                                    Modifier
                                },
                            )
                            .then(
                                if (selectedShadowBlurRadius > 0.dp) {
                                    Modifier.tabRowIndicatorShadow(
                                        shape = config.shape,
                                        blurRadius = selectedShadowBlurRadius,
                                        offsetY = selectedShadowOffsetY,
                                        color = selectedShadowColor,
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .clip(config.shape)
                            .background(colors.backgroundColor(selected = true)),
                    )
                }

                TabRowStyle.Flat -> {
                    val indicatorHorizontalInset = itemHorizontalPadding
                    val flatIndicatorWidth = maxOf(
                        TabRowDefaults.FlatIndicatorMinWidth,
                        with(density) { indicatorWidth.value.toDp() } - indicatorHorizontalInset * 2,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset {
                                IntOffset(
                                    (
                                        indicatorOffset.value - scrollOffset +
                                            with(density) { indicatorHorizontalInset.toPx() }
                                        ).roundToInt(),
                                    0,
                                )
                            }
                            .width(flatIndicatorWidth)
                            .height(TabRowDefaults.FlatIndicatorHeight)
                            .clip(yubeixShape(TabRowDefaults.FlatIndicatorHeight / 2))
                            .background(colors.backgroundColor(selected = true)),
                    )
                }
            }

            LazyRow(
                state = config.listState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                userScrollEnabled = !dragEnabled,
                overscrollEffect = null,
            ) {
                itemsIndexed(tabs) { index, title ->
                    val selectedProgress = tabRowTabSelectedProgress(
                        index = index,
                        indicatorPosition = indicatorVisualPosition,
                    )
                    TabItem(
                        text = title,
                        onClick = { currentOnTabSelected(index) },
                        shape = config.shape,
                        width = config.tabWidths[index],
                        color = colors.contentColor(selectedProgress),
                        contentAlignment = contentAlignment,
                        textStyle = textStyle,
                        interactionSource = interactionSource,
                        indication = indication,
                        hapticFeedbackEnabled = hapticFeedbackEnabled,
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    onClick: () -> Unit,
    shape: Shape,
    width: Dp,
    color: Color,
    contentAlignment: Alignment,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
    hapticFeedbackEnabled: Boolean,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val hapticFeedback = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = {
                    if (hapticFeedbackEnabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                    currentOnClick()
                },
            )
            .semantics { role = Role.Tab },
        contentAlignment = contentAlignment,
    ) {
        Text(
            text = text,
            color = color,
            // One weight for every tab, selected or not. Selection is carried by colour alone: a
            // family only ships discrete faces, so anything that animates the weight either steps
            // between the nearest real ones or has to cross-fade two of them, and neither is worth
            // the cost of what it conveys.
            fontWeight = TAB_ROW_LABEL_WEIGHT,
            fontSize = textStyle.fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Draws a soft drop shadow behind the indicator with layered outlines. Unlike a platform elevation
 * shadow the blur radius and the downward offset are controlled directly, so the shadow can read
 * heavier and sit lower than the platform one. Each layer expands the indicator shape outwards and
 * draws at a fraction of the shadow alpha; the overlap near the shape edge accumulates into a soft
 * falloff that approximates a Gaussian blur.
 */
private fun Modifier.tabRowIndicatorShadow(
    shape: Shape,
    blurRadius: Dp,
    offsetY: Dp,
    color: Color,
): Modifier = drawWithCache {
    val blurPx = blurRadius.toPx()
    val offsetYPx = offsetY.toPx()
    val expandedOutlines = (TAB_ROW_SHADOW_LAYER_COUNT downTo 1).map { layer ->
        val expandPx = blurPx * layer / TAB_ROW_SHADOW_LAYER_COUNT
        val outline = shape.createOutline(
            size = Size(size.width + expandPx * 2f, size.height + expandPx * 2f),
            layoutDirection = layoutDirection,
            density = this,
        )
        expandPx to outline
    }
    onDrawBehind {
        expandedOutlines.forEachIndexed { index, (expandPx, outline) ->
            val weight = TAB_ROW_SHADOW_LAYER_ALPHAS[TAB_ROW_SHADOW_LAYER_COUNT - 1 - index]
            translate(-expandPx, offsetYPx - expandPx) {
                drawOutline(
                    outline = outline,
                    color = color.copy(alpha = color.alpha * weight),
                )
            }
        }
    }
}

@Composable
private fun rememberTabRowConfig(
    tabs: List<String>,
    widthMode: TabRowWidthMode,
    minWidth: Dp,
    maxWidth: Dp,
    cornerRadius: Dp,
    spacing: Dp,
    itemHorizontalPadding: Dp,
    availableWidth: Dp,
    textStyle: TextStyle,
): TabRowConfig {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val measurementTextStyle = textStyle.copy(
        fontWeight = TAB_ROW_LABEL_WEIGHT,
    )
    val tabWidths = remember(
        tabs,
        widthMode,
        minWidth,
        maxWidth,
        availableWidth,
        spacing,
        itemHorizontalPadding,
        density,
        measurementTextStyle,
    ) {
        when (widthMode) {
            TabRowWidthMode.Equal -> {
                val tabWidth = calculateEqualTabWidth(
                    tabCount = tabs.size,
                    minWidth = minWidth,
                    maxWidth = maxWidth,
                    spacing = spacing,
                    availableWidth = availableWidth,
                )
                List(tabs.size) { tabWidth }
            }

            TabRowWidthMode.ContentAdaptive -> calculateContentAdaptiveTabWidths(
                tabs = tabs,
                textMeasurer = textMeasurer,
                textStyle = measurementTextStyle,
                density = density,
                minWidth = minWidth,
                spacing = spacing,
                itemHorizontalPadding = itemHorizontalPadding,
                availableWidth = availableWidth,
            )
        }
    }
    val spacingPx = with(density) { spacing.toPx() }
    val tabWidthsPx = remember(tabWidths, density) {
        tabWidths.map { with(density) { it.toPx() } }
    }
    val tabOffsetsPx = remember(tabWidthsPx, spacingPx) {
        calculateTabOffsetsPx(
            widthsPx = tabWidthsPx,
            spacingPx = spacingPx,
        )
    }
    val tabCentersPx = remember(tabOffsetsPx, tabWidthsPx) {
        tabOffsetsPx.mapIndexed { index, offsetPx -> offsetPx + tabWidthsPx[index] / 2f }
    }
    val isScrollable = remember(tabWidthsPx, spacingPx, availableWidth, density) {
        val totalSpacingPx = if (tabWidthsPx.size > 1) spacingPx * (tabWidthsPx.size - 1) else 0f
        val contentWidthPx = tabWidthsPx.sum() + totalSpacingPx
        contentWidthPx > with(density) { availableWidth.toPx() } + 0.5f
    }
    return TabRowConfig(
        tabWidths = tabWidths,
        tabOffsetsPx = tabOffsetsPx,
        tabCentersPx = tabCentersPx,
        isScrollable = isScrollable,
        shape = yubeixShape(cornerRadius),
        listState = listState,
    )
}

/**
 * Base configuration for TabRow implementations.
 */
private data class TabRowConfig(
    val tabWidths: List<Dp>,
    val tabOffsetsPx: List<Float>,
    val tabCentersPx: List<Float>,
    val isScrollable: Boolean,
    val shape: Shape,
    val listState: LazyListState,
)

/**
 * Inverse of [tabRowIndicatorTarget]'s centre: the fractional tab position whose indicator
 * centre sits at [xPx]. Interpolating between tab centres rather than dividing by a nominal tab
 * width keeps the indicator under the finger when tabs are sized to their content.
 */
private fun tabRowPositionForX(
    config: TabRowConfig,
    xPx: Float,
): Float {
    val centers = config.tabCentersPx
    if (centers.size <= 1) return 0f
    if (xPx <= centers.first()) return 0f
    if (xPx >= centers.last()) return centers.lastIndex.toFloat()

    for (index in 0 until centers.lastIndex) {
        val start = centers[index]
        val end = centers[index + 1]
        if (xPx <= end) {
            val span = end - start
            return if (span <= 0f) index.toFloat() else index + (xPx - start) / span
        }
    }
    return centers.lastIndex.toFloat()
}

private data class TabRowIndicatorTarget(
    val offsetPx: Float,
    val widthPx: Float,
)

/**
 * How selected [index] reads, from where the indicator **currently is** rather than from where it is
 * heading. Keying this off the target index instead would flip the label to its selected colour a
 * whole animation ahead of the indicator arriving under it.
 */
private fun tabRowTabSelectedProgress(
    index: Int,
    indicatorPosition: Float,
): Float = (1f - abs(indicatorPosition - index)).coerceIn(0f, 1f)

private fun tabRowIndicatorTarget(
    config: TabRowConfig,
    position: Float,
    density: androidx.compose.ui.unit.Density,
): TabRowIndicatorTarget {
    if (config.tabWidths.isEmpty()) {
        return TabRowIndicatorTarget(offsetPx = 0f, widthPx = 0f)
    }

    val coercedPosition = position.coerceIn(0f, config.tabWidths.lastIndex.toFloat())
    val startIndex = floor(coercedPosition).toInt().coerceIn(config.tabWidths.indices)
    val endIndex = (startIndex + 1).coerceAtMost(config.tabWidths.lastIndex)
    val fraction = coercedPosition - startIndex
    val startOffsetPx = config.tabOffsetsPx[startIndex]
    val endOffsetPx = config.tabOffsetsPx[endIndex]
    val startWidthPx = with(density) { config.tabWidths[startIndex].toPx() }
    val endWidthPx = with(density) { config.tabWidths[endIndex].toPx() }

    return TabRowIndicatorTarget(
        offsetPx = lerp(startOffsetPx, endOffsetPx, fraction),
        widthPx = lerp(startWidthPx, endWidthPx, fraction),
    )
}

private fun calculateEqualTabWidth(
    tabCount: Int,
    minWidth: Dp,
    maxWidth: Dp,
    spacing: Dp,
    availableWidth: Dp,
): Dp {
    if (tabCount == 0) return minWidth

    val totalSpacing = if (tabCount > 1) spacing * (tabCount - 1) else 0.dp
    val contentWidth = availableWidth - totalSpacing
    if (contentWidth <= 0.dp) return minWidth

    val idealWidth = contentWidth / tabCount
    return when {
        idealWidth < minWidth -> minWidth

        idealWidth > maxWidth -> {
            val totalMaxWidth = maxWidth * tabCount + totalSpacing
            if (totalMaxWidth < availableWidth) idealWidth else maxWidth
        }

        else -> idealWidth
    }
}

private fun calculateContentAdaptiveTabWidths(
    tabs: List<String>,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    density: androidx.compose.ui.unit.Density,
    minWidth: Dp,
    spacing: Dp,
    itemHorizontalPadding: Dp,
    availableWidth: Dp,
): List<Dp> {
    if (tabs.isEmpty()) return emptyList()

    val measuredWidths = tabs.map { title ->
        val textWidth = with(density) {
            textMeasurer.measure(
                text = AnnotatedString(title),
                style = textStyle,
            ).size.width.toDp()
        }
        maxOf(minWidth, textWidth + itemHorizontalPadding * 2)
    }
    val totalSpacing = if (tabs.size > 1) spacing * (tabs.size - 1) else 0.dp
    val measuredContentWidth = measuredWidths.fold(0.dp) { total, width -> total + width } + totalSpacing
    if (measuredContentWidth >= availableWidth || availableWidth <= totalSpacing) {
        return measuredWidths
    }

    val extraPerTab = (availableWidth - measuredContentWidth) / tabs.size
    return measuredWidths.map { width -> width + extraPerTab }
}

private fun calculateTabOffsetsPx(
    widthsPx: List<Float>,
    spacingPx: Float,
): List<Float> {
    var offsetPx = 0f
    return widthsPx.map { widthPx ->
        val currentOffset = offsetPx
        offsetPx += widthPx + spacingPx
        currentOffset
    }
}

/**
 * How tab widths are computed in a [TabRow].
 */
enum class TabRowWidthMode {
    /** Every tab gets the same width. */
    Equal,

    /** Each tab is sized to its text content, then expanded to fill the track when it fits. */
    ContentAdaptive,
}

/**
 * The visual style of a [TabRow].
 */
enum class TabRowStyle {
    /** A sliding pill fills the selected tab inside an inset track. */
    Filled,

    /** A short underline slides along the bottom edge of the track. */
    Flat,
}

/** Contains default values used by [TabRow], [TabRowWithContour] and [TextTabs]. */
object TabRowDefaults {

    /** The default height of the [TabRow]. */
    val TabRowHeight = 46.dp

    /** The default height of the [TabRowWithContour]. */
    val TabRowWithContourHeight = 45.dp

    /** The default height of the [TabRow] in [TabRowStyle.Flat]. */
    val FlatHeight = 48.dp

    /** The height of the sliding underline in [TabRowStyle.Flat]. */
    val FlatIndicatorHeight = 3.dp

    /** The minimum width of the sliding underline in [TabRowStyle.Flat]. */
    val FlatIndicatorMinWidth = 18.dp

    /** The default corner radius of the [TabRow]. */
    val TabRowCornerRadius = 18.dp

    /** The default corner radius of the [TabRowWithContour]. */
    val TabRowWithContourCornerRadius = 8.dp

    /** The default minimum width of the tab in the [TabRow]. */
    val TabRowMinWidth = 56.dp

    /** The default minimum width of the tab in the [TabRowWithContour]. */
    val TabRowWithContourMinWidth = 62.dp

    /** The default maximum width of the tab in the [TabRow]. */
    val TabRowMaxWidth = 72.dp

    /** The default maximum width of the tab in the [TabRowWithContour]. */
    val TabRowWithContourMaxWidth = 84.dp

    /** The default spacing between tabs. */
    val ItemSpacing = 6.dp

    /** The default horizontal padding inside each tab. */
    val ItemHorizontalPadding = 18.dp

    /** The default padding between the track edge and the tabs in [TabRowStyle.Filled]. */
    val FilledContourPadding = 5.dp

    /** The blur radius of the selected indicator's drop shadow. */
    val SelectedShadowBlurRadius = 3.dp

    /** The vertical offset of the selected indicator's drop shadow. */
    val SelectedShadowOffsetY = 1.5.dp

    /** The color of the selected indicator's drop shadow. */
    val SelectedShadowColor = Color.Black.copy(alpha = 0.4f)

    /**
     * The indicator shrinks under the finger rather than swelling: it already fills the track's
     * height, so growing it would spill over the track's edges.
     */
    const val PRESSED_INDICATOR_SCALE = 0.94f

    /**
     * The default colors for the [TabRow] and [TabRowWithContour].
     */
    @Composable
    fun tabRowColors(
        backgroundColor: Color = YubeixTheme.colorScheme.surfaceContainerHigh,
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        selectedBackgroundColor: Color = YubeixTheme.colorScheme.surface,
        selectedContentColor: Color = YubeixTheme.colorScheme.onSurface,
    ): TabRowColors = remember(backgroundColor, contentColor, selectedBackgroundColor, selectedContentColor) {
        TabRowColors(
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            selectedBackgroundColor = selectedBackgroundColor,
            selectedContentColor = selectedContentColor,
        )
    }

    /**
     * Colors for a [TabRow] whose selected indicator and label use the theme's accent color,
     * tinted over a translucent wash of itself.
     */
    @Composable
    fun primarySelectedColors(
        backgroundColor: Color = YubeixTheme.colorScheme.surface,
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        selectedBackgroundAlpha: Float = 0.16f,
        selectedContentColor: Color = tabRowSelectedAccentColor(),
    ): TabRowColors {
        val selectedBackgroundColor = selectedContentColor.copy(
            alpha = selectedBackgroundAlpha.coerceIn(0f, 1f),
        )
        return tabRowColors(
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            selectedBackgroundColor = selectedBackgroundColor,
            selectedContentColor = selectedContentColor,
        )
    }

    /**
     * Colors for a [TabRow] in [TabRowStyle.Flat]: a transparent track with an accent underline.
     */
    @Composable
    fun flatColors(
        backgroundColor: Color = Color.Transparent,
        contentColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        indicatorColor: Color = tabRowSelectedAccentColor(),
        selectedContentColor: Color = indicatorColor,
    ): TabRowColors = tabRowColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        selectedBackgroundColor = indicatorColor,
        selectedContentColor = selectedContentColor,
    )
}

@Composable
private fun tabRowSelectedAccentColor(): Color {
    val colors = YubeixTheme.colorScheme
    return if (colors.surface.luminance() >= 0.5f) {
        androidx.compose.ui.graphics.lerp(colors.primary, Color.Black, 0.22f)
    } else {
        colors.primary
    }
}

/**
 * Colors for the [TabRow] and [TabRowWithContour].
 *
 * The selected content color can also be sampled continuously: [contentColor] with a progress
 * argument interpolates between the resting and selected colors, so tab labels recolour in step
 * with the travelling indicator.
 *
 * @param backgroundColor The track color behind all tabs.
 * @param contentColor The label color of an unselected tab.
 * @param selectedBackgroundColor The sliding indicator color behind the selected tab.
 * @param selectedContentColor The label color of the selected tab.
 */
@Immutable
data class TabRowColors(
    private val backgroundColor: Color,
    private val contentColor: Color,
    private val selectedBackgroundColor: Color,
    private val selectedContentColor: Color,
) {
    /**
     * The track color behind all tabs, or the sliding indicator color when [selected] is true.
     */
    @Stable
    fun backgroundColor(selected: Boolean): Color = if (selected) selectedBackgroundColor else backgroundColor

    /**
     * The label color of an unselected tab, or of the selected tab when [selected] is true.
     */
    @Stable
    fun contentColor(selected: Boolean): Color = if (selected) selectedContentColor else contentColor

    /**
     * The label color interpolated along [selectedProgress] (0f unselected, 1f selected), so a
     * label recolours continuously while the indicator travels towards it.
     */
    @Stable
    fun contentColor(selectedProgress: Float): Color = androidx.compose.ui.graphics.lerp(
        contentColor,
        selectedContentColor,
        selectedProgress.coerceIn(0f, 1f),
    )
}
