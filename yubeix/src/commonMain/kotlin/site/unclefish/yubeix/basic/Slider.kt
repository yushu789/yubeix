// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.annotation.IntRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.shapes.RoundedCornerStyle
import com.kyant.shapes.RoundedRectangle
import site.unclefish.yubeix.anim.yubeixSpring
import site.unclefish.yubeix.theme.YubeixTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A Cupertino style [Slider].
 *
 * A thin track with a floating thumb: the value follows the finger position directly (tap
 * anywhere to jump, drag to scrub) instead of animating from the previous position.
 *
 * [detentValues] snap the thumb: while dragging or tapping, the value locks onto the nearest
 * detent within [detentThreshold] (in value units), a haptic tick fires on each engagement when
 * [detentHapticsEnabled] is true, and the detents are drawn as small track markers. The color of
 * a marker switches from [SliderColors.keyPointColor] to [SliderColors.keyPointForegroundColor]
 * once the value has passed it.
 *
 * The legacy discrete parameters fold into the same snap model: [steps] greater than 0 snaps to
 * evenly spaced values across [valueRange], and [keyPoints] snap magnetically when within
 * [magnetThreshold] (as a fraction of the range). Markers for steps/key points are drawn only
 * when [showKeyPoints] is true.
 *
 * @param value The current value of the [Slider]. If outside of [valueRange] provided, value will be coerced to this range.
 * @param onValueChange The callback to be called when the value changes.
 * @param modifier The modifier to be applied to the [Slider].
 * @param enabled Whether the [Slider] is enabled.
 * @param valueRange Range of values that this slider can take. The passed [value] will be coerced to this range.
 * @param steps If positive, snaps to the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). Must not be negative.
 * @param onValueChangeFinished Called when value change has ended. This should not be used to update the slider value
 *   (use [onValueChange] instead), but rather to know when the user has completed selecting a new value by ending a drag or a click.
 * @param reverseDirection Controls the direction of this slider. When false (default), the slider
 *   increases from left to right in LTR layouts (mirrored in RTL). When true, the direction flips.
 * @param height The height of the interactive [Slider] container; the thin track and thumb are centered inside it.
 *   Defaults to [SliderDefaults.MinHeight] (32.dp), the Cupertino reference container height.
 * @param colors The [SliderColors] of the [Slider]. The foreground/background, thumb and key point
 *   colors double as the Cupertino active/inactive track, thumb and detent marker colors.
 * @param hapticEffect The haptic effect of the [Slider].
 * @param showKeyPoints Whether to show markers at the [keyPoints] / [steps] positions on the track.
 * @param keyPoints Custom key point values to magnetically snap to and (with [showKeyPoints]) display.
 *   Values should be within [valueRange]. For example, for a range of 0f..100f, you might specify listOf(0f, 25f, 50f, 75f, 100f).
 * @param magnetThreshold The magnetic snap threshold as a fraction (0.0 to 1.0). A key point captures
 *   the value when it is within this distance of it. Default is 0.02 (2%). Only applies when [keyPoints] is set.
 * @param detentValues Values the slider snaps onto while dragging or tapping. An empty list (default)
 *   disables detents. Values should be within [valueRange].
 * @param detentThreshold Maximum distance (in value units) at which the nearest detent engages and captures the value.
 * @param detentHapticsEnabled Whether a haptic tick plays whenever a detent engages.
 */
@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    reverseDirection: Boolean = false,
    height: Dp = SliderDefaults.MinHeight,
    colors: SliderColors = SliderDefaults.sliderColors(),
    hapticEffect: SliderDefaults.SliderHapticEffect = SliderDefaults.DefaultHapticEffect,
    showKeyPoints: Boolean = false,
    keyPoints: List<Float>? = null,
    magnetThreshold: Float = 0.02f,
    detentValues: List<Float> = emptyList(),
    detentThreshold: Float = 0f,
    detentHapticsEnabled: Boolean = true,
) {
    require(steps >= 0) { "steps should be >= 0" }
    require(valueRange.start < valueRange.endInclusive) { "valueRange start should be less than end" }

    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val mirrored = isRtl != reverseDirection
    val onValueChangeState by rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState by rememberUpdatedState(onValueChangeFinished)
    val hapticState = remember { SliderHapticState() }

    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val coercedDetentThreshold = detentThreshold.coerceAtLeast(0f)
    val effectiveDetents = remember(detentValues, valueRange) {
        detentValues.distinct().filter { detent -> detent in valueRange }
    }
    val stepValues = remember(steps, valueRange) {
        if (steps > 0) {
            List(steps + 1) { index ->
                valueRange.start + (valueRange.endInclusive - valueRange.start) * index / (steps + 1)
            }
        } else {
            emptyList()
        }
    }
    val keyPointValues = remember(keyPoints, valueRange) {
        keyPoints?.distinct()?.filter { keyPoint -> keyPoint in valueRange } ?: emptyList()
    }

    // Tracks which detent the value is currently locked onto as an index, mirroring the
    // Cupertino reference; -1 means no detent is engaged.
    var engagedDetentIndex by remember(effectiveDetents) {
        mutableIntStateOf(
            effectiveDetents.indexOfFirst { detent ->
                abs(coercedValue - detent) <= coercedDetentThreshold
            },
        )
    }

    val fraction = remember(coercedValue, valueRange) {
        val range = valueRange.endInclusive - valueRange.start
        if (range == 0f) {
            0f
        } else {
            ((coercedValue - valueRange.start) / range).coerceIn(0f, 1f)
        }
    }

    val markerValues = when {
        effectiveDetents.isNotEmpty() -> effectiveDetents
        showKeyPoints && keyPointValues.isNotEmpty() -> keyPointValues
        showKeyPoints && stepValues.isNotEmpty() -> stepValues
        else -> emptyList()
    }
    val hapticKeyPointFractions = remember(keyPointValues, steps, valueRange) {
        if (keyPoints != null) pointsToFractions(keyPointValues, valueRange) else stepsToTickFractions(steps)
    }
    val hasCustomKeyPoints = keyPoints != null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    coercedValue,
                    valueRange.start..valueRange.endInclusive,
                    if (steps > 0) steps else 0,
                )
                setProgress { target ->
                    val clamped = target.coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChangeState(clamped)
                    true
                }
            }
            .height(height),
        contentAlignment = Alignment.CenterStart,
    ) {
        val thumbSizePx = with(density) { CupertinoSliderThumbSize.toPx() }
        val detentMarkerWidthPx = with(density) { CupertinoSliderDetentMarkerWidth.toPx() }
        val sliderWidthPx =
            if (constraints.maxWidth != Constraints.Infinity) constraints.maxWidth.toFloat() else 0f
        val usableWidthPx = (sliderWidthPx - thumbSizePx).coerceAtLeast(1f)
        val visualFraction = if (mirrored) 1f - fraction else fraction
        val thumbOffsetPx = usableWidthPx * visualFraction
        val trackShape = remember {
            RoundedRectangle(CupertinoSliderTrackHeight / 2, style = RoundedCornerStyle.Continuous)
        }
        val thumbShape = remember {
            RoundedRectangle(CupertinoSliderThumbSize / 2, style = RoundedCornerStyle.Continuous)
        }
        val detentMarkerShape = remember {
            RoundedRectangle(
                CupertinoSliderDetentMarkerWidth / 2,
                style = RoundedCornerStyle.Continuous,
            )
        }

        fun valueForPosition(position: Offset): Float {
            val rawFraction = ((position.x - thumbSizePx / 2f) / usableWidthPx).coerceIn(0f, 1f)
            val resolvedFraction = if (mirrored) 1f - rawFraction else rawFraction
            return valueRange.start +
                (valueRange.endInclusive - valueRange.start) * resolvedFraction
        }

        fun emitValueForPosition(position: Offset) {
            val rawValue = valueForPosition(position)
            // Detent engagement follows the Cupertino reference: lock onto the nearest detent
            // within the threshold and tick once per engagement change.
            val detentIndex = effectiveDetents.indices
                .minByOrNull { index -> abs(rawValue - effectiveDetents[index]) }
                ?.takeIf { index ->
                    abs(rawValue - effectiveDetents[index]) <= coercedDetentThreshold
                }
                ?: -1
            if (detentIndex != engagedDetentIndex) {
                if (detentIndex >= 0 && detentHapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
                engagedDetentIndex = detentIndex
            }
            // The legacy discrete parameters fold into the same position-driven model: with no
            // detents engaged, steps snap to evenly spaced values and key points magnetize when
            // within magnetThreshold (as a fraction of the range).
            var emittedValue = if (detentIndex >= 0) effectiveDetents[detentIndex] else rawValue
            if (detentIndex < 0) {
                when {
                    stepValues.isNotEmpty() -> {
                        emittedValue =
                            stepValues.minByOrNull { step -> abs(step - emittedValue) } ?: emittedValue
                    }

                    keyPointValues.isNotEmpty() -> {
                        val range = valueRange.endInclusive - valueRange.start
                        val rawFraction = if (range == 0f) 0f else (emittedValue - valueRange.start) / range
                        val nearest = keyPointValues.minByOrNull { keyPoint -> abs(keyPoint - emittedValue) }
                        if (nearest != null) {
                            val nearestFraction = if (range == 0f) 0f else (nearest - valueRange.start) / range
                            if (abs(nearestFraction - rawFraction) <= magnetThreshold) {
                                emittedValue = nearest
                            }
                        }
                    }
                }
            }
            hapticState.handleHapticFeedback(
                emittedValue,
                valueRange,
                hapticEffect,
                hapticFeedback,
                hapticKeyPointFractions,
                hasCustomKeyPoints = hasCustomKeyPoints,
            )
            onValueChangeState(emittedValue)
        }

        val inputModifier = if (enabled) {
            Modifier
                .pointerInput(
                    valueRange,
                    mirrored,
                    sliderWidthPx,
                    effectiveDetents,
                    coercedDetentThreshold,
                    detentHapticsEnabled,
                ) {
                    detectTapGestures { position ->
                        emitValueForPosition(position)
                        onValueChangeFinishedState?.invoke()
                    }
                }
                .pointerInput(
                    valueRange,
                    mirrored,
                    sliderWidthPx,
                    effectiveDetents,
                    coercedDetentThreshold,
                    detentHapticsEnabled,
                ) {
                    detectDragGestures(
                        onDragStart = { position ->
                            emitValueForPosition(position)
                        },
                        onDragEnd = {
                            onValueChangeFinishedState?.invoke()
                        },
                        onDragCancel = {
                            onValueChangeFinishedState?.invoke()
                        },
                    ) { change, _ ->
                        emitValueForPosition(change.position)
                    }
                }
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .then(inputModifier),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = CupertinoSliderThumbSize / 2)
                    .fillMaxWidth()
                    .height(CupertinoSliderTrackHeight)
                    .clip(trackShape)
                    .background(colors.backgroundColor(enabled)),
            ) {
                Box(
                    modifier = Modifier
                        .align(if (mirrored) Alignment.CenterEnd else Alignment.CenterStart)
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(colors.foregroundColor(enabled)),
                )
            }

            markerValues.forEach { markerValue ->
                val markerFraction = if (valueRange.endInclusive == valueRange.start) {
                    0f
                } else {
                    (
                        (markerValue - valueRange.start) /
                            (valueRange.endInclusive - valueRange.start)
                        ).coerceIn(0f, 1f)
                }
                val markerVisualFraction = if (mirrored) 1f - markerFraction else markerFraction
                val markerCenterPx = thumbSizePx / 2f + usableWidthPx * markerVisualFraction
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            IntOffset(
                                x = (markerCenterPx - detentMarkerWidthPx / 2f).roundToInt(),
                                y = 0,
                            )
                        }
                        .size(
                            width = CupertinoSliderDetentMarkerWidth,
                            height = CupertinoSliderDetentMarkerHeight,
                        )
                        .background(
                            color = if (markerValue <= coercedValue) {
                                colors.keyPointForegroundColor()
                            } else {
                                colors.keyPointColor()
                            },
                            shape = detentMarkerShape,
                        ),
                )
            }

            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                    .size(CupertinoSliderThumbSize)
                    .shadow(
                        elevation = if (enabled) CupertinoSliderThumbElevation else 0.dp,
                        shape = thumbShape,
                        clip = false,
                    )
                    .background(colors.thumbColor(enabled), thumbShape),
            )
        }
    }
}

/**
 * A vertical [Slider] component with Yubeix style: a thin 4dp track with a 20dp floating thumb
 * (8dp elevation shadow), sharing the color tokens of the horizontal [Slider].
 *
 * @param value The current value of the [Slider]. If outside of [valueRange] provided, value will be coerced to this range.
 * @param onValueChange The callback to be called when the value changes.
 * @param modifier The modifier to be applied to the [Slider].
 * @param enabled Whether the [Slider] is enabled.
 * @param valueRange Range of values that this slider can take. The passed [value] will be coerced to this range.
 * @param steps If positive, specifies the amount of discrete allowable values between the endpoints of [valueRange].
 * @param onValueChangeFinished Called when value change has ended.
 * @param reverseDirection Controls the direction of this slider. When false (default), slider increases from bottom to top.
 *   When true, slider increases from top to bottom.
 * @param width The width of the vertical [Slider].
 * @param colors The [SliderColors] of the [Slider].
 * @param effect Unused; kept for backward compatibility.
 * @param hapticEffect The haptic effect of the [Slider].
 * @param showKeyPoints Whether to show the key points (step indicators) on the slider. When false
 *   and [keyPoints] is null, no markers are drawn.
 * @param keyPoints Custom key point values to display on the slider. If null, uses step positions from [steps] parameter.
 *   Values should be within [valueRange].
 * @param magnetThreshold The magnetic snap threshold as a fraction (0.0 to 1.0). Only applies when [keyPoints] is set.
 */
@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    reverseDirection: Boolean = false,
    width: Dp = SliderDefaults.MinHeight,
    colors: SliderColors = SliderDefaults.sliderColors(),
    effect: Boolean = false,
    hapticEffect: SliderDefaults.SliderHapticEffect = SliderDefaults.DefaultHapticEffect,
    showKeyPoints: Boolean = false,
    keyPoints: List<Float>? = null,
    magnetThreshold: Float = 0.02f,
) {
    require(steps >= 0) { "steps should be >= 0" }
    require(valueRange.start < valueRange.endInclusive) { "valueRange start should be less than end" }

    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val onValueChangeState by rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState by rememberUpdatedState(onValueChangeFinished)
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isHoveringThumb by remember { mutableStateOf(false) }
    val hapticState = remember { SliderHapticState() }
    val interactionSource = remember { MutableInteractionSource() }
    var layoutWidth by remember { mutableIntStateOf(0) }
    var layoutHeight by remember { mutableIntStateOf(0) }
    val isPressed by interactionSource.collectIsPressedAsState()

    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)

    val progressAnimationSpec: AnimationSpec<Float> = remember(isDragging) {
        if (isDragging) {
            yubeixSpring(damping = 0.9f, response = 0.15f)
        } else {
            yubeixSpring(damping = 0.96f, response = 0.35f)
        }
    }

    val animatedValueState = animateFloatAsState(coercedValue, progressAnimationSpec)
    val thumbScaleState = animateFloatAsState(if (isPressed || isDragging || isHoveringThumb) 1.127f else 1f, ThumbScaleAnimationSpec)

    val stepFractions = remember(steps) { stepsToTickFractions(steps) }

    val keyPointFractions = remember(keyPoints, stepFractions, valueRange, showKeyPoints) {
        computeKeyPointFractions(keyPoints, stepFractions, valueRange, showKeyPoints)
    }

    val allKeyPointFractions = remember(keyPoints, stepFractions, valueRange) {
        computeAllKeyPointFractions(keyPoints, stepFractions, valueRange)
    }

    val fractionToValueVertical = remember(valueRange, steps, stepFractions, allKeyPointFractions, magnetThreshold) {
        { fraction: Float ->
            resolveValueFromFraction(
                fraction = fraction,
                valueRange = valueRange,
                steps = steps,
                allKeyPointFractions = allKeyPointFractions,
                magnetThreshold = magnetThreshold,
            )
        }
    }

    val currentLayoutWidth by rememberUpdatedState(layoutWidth)
    val currentLayoutHeight by rememberUpdatedState(layoutHeight)

    BoxWithConstraints(
        modifier = modifier
            .width(width)
            .then(
                if (enabled) {
                    Modifier
                        .onSizeChanged {
                            layoutWidth = it.width
                            layoutHeight = it.height
                        }
                        .pointerInput(reverseDirection, valueRange) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.last()

                                    if (event.type == PointerEventType.Exit ||
                                        event.type == PointerEventType.Release ||
                                        change.type != PointerType.Mouse
                                    ) {
                                        isHoveringThumb = false
                                        continue
                                    }

                                    val thumbRadius = currentLayoutWidth / 2f
                                    val availableHeight = (currentLayoutHeight - 2f * thumbRadius).coerceAtLeast(0f)
                                    val knobRadius = thumbRadius * 0.72f
                                    val hitRadius = knobRadius + (thumbRadius * 0.5f)

                                    val position = change.position
                                    val fraction =
                                        (animatedValueState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                                    val effectiveFraction = if (reverseDirection) fraction else 1f - fraction
                                    val thumbY = thumbRadius + effectiveFraction * availableHeight

                                    val isOver = abs(position.y - thumbY) <= hitRadius
                                    if (isHoveringThumb != isOver) {
                                        isHoveringThumb = isOver
                                    }
                                }
                            }
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { dragAmount ->
                                dragOffset += dragAmount
                                val visualFraction = verticalVisualFraction(dragOffset, layoutHeight, layoutWidth)
                                val fractionForValue = if (reverseDirection) visualFraction else 1f - visualFraction
                                val calculatedValue = fractionToValueVertical(fractionForValue)
                                onValueChangeState(calculatedValue)
                                hapticState.handleHapticFeedback(
                                    calculatedValue,
                                    valueRange,
                                    hapticEffect,
                                    hapticFeedback,
                                    allKeyPointFractions,
                                    hasCustomKeyPoints = keyPoints != null,
                                )
                            },
                            onDragStarted = { offset ->
                                isDragging = true
                                dragOffset = offset.y
                                val visualFraction = verticalVisualFraction(offset.y, layoutHeight, layoutWidth)
                                val fractionForValue = if (reverseDirection) visualFraction else 1f - visualFraction
                                val calculatedValue = fractionToValueVertical(fractionForValue)
                                onValueChangeState(calculatedValue)
                                hapticState.reset(calculatedValue)
                            },
                            onDragStopped = {
                                isDragging = false
                                onValueChangeFinishedState?.invoke()
                            },
                        )
                        .indication(interactionSource, null)
                } else {
                    Modifier
                },
            )
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    coercedValue,
                    valueRange.start..valueRange.endInclusive,
                    if (steps > 0) steps else 0,
                )
                setProgress { target ->
                    val clamped = target.coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChangeState(clamped)
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val trackShape = remember {
            RoundedRectangle(CupertinoSliderTrackHeight / 2, style = RoundedCornerStyle.Continuous)
        }
        val thumbShape = remember {
            RoundedRectangle(CupertinoSliderThumbSize / 2, style = RoundedCornerStyle.Continuous)
        }
        val detentMarkerShape = remember {
            RoundedRectangle(
                CupertinoSliderDetentMarkerWidth / 2,
                style = RoundedCornerStyle.Continuous,
            )
        }
        val thumbSizePx = with(density) { CupertinoSliderThumbSize.toPx() }
        val containerHeightPx =
            if (constraints.maxHeight != Constraints.Infinity) constraints.maxHeight.toFloat() else 0f
        val usableHeightPx = (containerHeightPx - thumbSizePx).coerceAtLeast(1f)
        val animatedFraction =
            (animatedValueState.value - valueRange.start) /
                (valueRange.endInclusive - valueRange.start)

        // Thumb and markers are placed by their distance from the top edge as a fraction of the
        // thumb travel; the value grows bottom-up unless reverseDirection flips it top-down.
        fun distanceFromTopFraction(fraction: Float): Float = if (reverseDirection) fraction else 1f - fraction
        val thumbCenterY = thumbSizePx / 2f + distanceFromTopFraction(animatedFraction) * usableHeightPx

        // Thin track, inset vertically by half a thumb so the fill spans thumb-center to
        // thumb-center, matching the horizontal slider.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = CupertinoSliderThumbSize / 2)
                .width(CupertinoSliderTrackHeight)
                .clip(trackShape)
                .background(colors.backgroundColor(enabled)),
            contentAlignment = if (reverseDirection) Alignment.TopCenter else Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFraction)
                    .background(colors.foregroundColor(enabled)),
            )
        }

        if (showKeyPoints) {
            keyPointFractions.forEach { stepFraction ->
                val stepCenterY = thumbSizePx / 2f + distanceFromTopFraction(stepFraction) * usableHeightPx
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, (stepCenterY - containerHeightPx / 2f).roundToInt()) }
                        // Ticks cross the vertical track: wide and short, unlike the horizontal
                        // slider's tall-and-narrow markers.
                        .size(
                            width = CupertinoSliderDetentMarkerHeight,
                            height = CupertinoSliderDetentMarkerWidth,
                        )
                        .background(
                            color = if (stepFraction <= animatedFraction) {
                                colors.keyPointForegroundColor()
                            } else {
                                colors.keyPointColor()
                            },
                            shape = detentMarkerShape,
                        ),
                )
            }
        }

        Spacer(
            modifier = Modifier
                .offset { IntOffset(0, (thumbCenterY - containerHeightPx / 2f).roundToInt()) }
                .size(CupertinoSliderThumbSize)
                .graphicsLayer {
                    scaleX = thumbScaleState.value
                    scaleY = thumbScaleState.value
                }
                .shadow(
                    elevation = if (enabled) CupertinoSliderThumbElevation else 0.dp,
                    shape = thumbShape,
                    clip = false,
                )
                .background(colors.thumbColor(enabled), thumbShape),
        )
    }
}

/**
 * A [RangeSlider] component with Yubeix style: a thin 4dp track with two 20dp floating thumbs
 * (8dp elevation shadow), sharing the color tokens of the horizontal [Slider].
 *
 * Range Sliders expand upon [Slider] using the same concepts but allow the user to select 2 values.
 * The two values are still bounded by the value range but they also cannot cross each other.
 *
 * @param value Current values of the RangeSlider. If either value is outside of [valueRange] provided, it will be coerced to this range.
 * @param onValueChange Lambda in which values should be updated.
 * @param modifier The modifier to be applied to the [RangeSlider].
 * @param enabled Whether the [RangeSlider] is enabled.
 * @param valueRange Range of values that Range Slider values can take. Passed [value] will be coerced to this range.
 * @param steps If positive, specifies the amount of discrete allowable values between the endpoints of [valueRange].
 * @param onValueChangeFinished Lambda to be invoked when value change has ended.
 * @param height The height of the [RangeSlider].
 * @param colors The [SliderColors] of the [RangeSlider].
 * @param hapticEffect The haptic effect of the [RangeSlider].
 * @param showKeyPoints Whether to show the key points (step indicators) on the slider. When false
 *   and [keyPoints] is null, no markers are drawn.
 * @param keyPoints Custom key point values to display on the slider. If null, uses step positions from [steps] parameter.
 *   Values should be within [valueRange].
 * @param magnetThreshold The magnetic snap threshold as a fraction (0.0 to 1.0). When the slider value is within this
 *   distance from a key point, it will snap to that point. Default is 0.02 (2%). Only applies when [keyPoints] is set.
 */
@Composable
fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    height: Dp = SliderDefaults.MinHeight,
    colors: SliderColors = SliderDefaults.sliderColors(),
    hapticEffect: SliderDefaults.SliderHapticEffect = SliderDefaults.DefaultHapticEffect,
    showKeyPoints: Boolean = false,
    keyPoints: List<Float>? = null,
    magnetThreshold: Float = 0.02f,
) {
    require(steps >= 0) { "steps should be >= 0" }
    require(valueRange.start < valueRange.endInclusive) { "valueRange start should be less than end" }

    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val onValueChangeState by rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState by rememberUpdatedState(onValueChangeFinished)
    var startDragOffset by remember { mutableFloatStateOf(0f) }
    var endDragOffset by remember { mutableFloatStateOf(0f) }
    var isDraggingStart by remember { mutableStateOf(false) }
    var isDraggingEnd by remember { mutableStateOf(false) }
    var isHoveringStartThumb by remember { mutableStateOf(false) }
    var isHoveringEndThumb by remember { mutableStateOf(false) }
    val isDragging by remember { derivedStateOf { isDraggingStart || isDraggingEnd } }
    val hapticState = remember { RangeSliderHapticState() }
    val interactionSource = remember { MutableInteractionSource() }
    var lastDraggedIsStart by remember { mutableStateOf(true) }
    var layoutWidth by remember { mutableIntStateOf(0) }
    var layoutHeight by remember { mutableIntStateOf(0) }
    val isPressed by interactionSource.collectIsPressedAsState()

    var currentStartValue by remember { mutableFloatStateOf(value.start) }
    var currentEndValue by remember { mutableFloatStateOf(value.endInclusive) }

    if (!isDragging) {
        currentStartValue = value.start
        currentEndValue = value.endInclusive
    }

    val coercedStart = currentStartValue.coerceIn(valueRange.start, valueRange.endInclusive)
    val coercedEnd = currentEndValue.coerceIn(valueRange.start, valueRange.endInclusive)

    val progressAnimationSpec: AnimationSpec<Float> = remember(isDragging) {
        if (isDragging) {
            yubeixSpring(damping = 0.9f, response = 0.15f)
        } else {
            yubeixSpring(damping = 0.96f, response = 0.35f)
        }
    }

    val animatedStartValueState = animateFloatAsState(coercedStart, progressAnimationSpec)
    val animatedEndValueState = animateFloatAsState(coercedEnd, progressAnimationSpec)
    val startThumbScaleState = animateFloatAsState(
        if (isDraggingStart || isPressed || isHoveringStartThumb) 1.127f else 1f,
        ThumbScaleAnimationSpec,
    )
    val endThumbScaleState = animateFloatAsState(if (isDraggingEnd || isPressed || isHoveringEndThumb) 1.127f else 1f, ThumbScaleAnimationSpec)

    val stepFractions = remember(steps) { stepsToTickFractions(steps) }

    val keyPointFractions = remember(keyPoints, stepFractions, valueRange, showKeyPoints) {
        computeKeyPointFractions(keyPoints, stepFractions, valueRange, showKeyPoints)
    }

    val allKeyPointFractions = remember(keyPoints, stepFractions, valueRange) {
        computeAllKeyPointFractions(keyPoints, stepFractions, valueRange)
    }

    val fractionToValueRange = remember(valueRange, steps, stepFractions, allKeyPointFractions, magnetThreshold) {
        { fraction: Float ->
            resolveValueFromFraction(
                fraction = fraction,
                valueRange = valueRange,
                steps = steps,
                allKeyPointFractions = allKeyPointFractions,
                magnetThreshold = magnetThreshold,
            )
        }
    }

    val currentLayoutWidth by rememberUpdatedState(layoutWidth)
    val currentLayoutHeight by rememberUpdatedState(layoutHeight)

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier
                        .onSizeChanged {
                            layoutWidth = it.width
                            layoutHeight = it.height
                        }
                        .pointerInput(isRtl, valueRange) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.last()

                                    if (event.type == PointerEventType.Exit ||
                                        event.type == PointerEventType.Release ||
                                        change.type != PointerType.Mouse
                                    ) {
                                        isHoveringStartThumb = false
                                        isHoveringEndThumb = false
                                        continue
                                    }

                                    val thumbRadius = currentLayoutHeight / 2f
                                    val availableWidth = (currentLayoutWidth - 2f * thumbRadius).coerceAtLeast(0f)
                                    val knobRadius = thumbRadius * 0.72f
                                    val hitRadius = knobRadius + (thumbRadius * 0.5f)

                                    val position = change.position
                                    val startFraction = (animatedStartValueState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                                    val endFraction = (animatedEndValueState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                                    val effectiveStartFraction = if (isRtl) 1f - startFraction else startFraction
                                    val effectiveEndFraction = if (isRtl) 1f - endFraction else endFraction
                                    val startThumbX = thumbRadius + effectiveStartFraction * availableWidth
                                    val endThumbX = thumbRadius + effectiveEndFraction * availableWidth

                                    val isOverStart = abs(position.x - startThumbX) <= hitRadius
                                    val isOverEnd = abs(position.x - endThumbX) <= hitRadius

                                    if (isHoveringStartThumb != isOverStart) {
                                        isHoveringStartThumb = isOverStart
                                    }
                                    if (isHoveringEndThumb != isOverEnd) {
                                        isHoveringEndThumb = isOverEnd
                                    }
                                }
                            }
                        }
                        .hoverable(interactionSource = interactionSource, enabled = enabled)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { dragAmount ->
                                if (isDraggingStart) {
                                    lastDraggedIsStart = true
                                    val tentativeStartOffset = startDragOffset + dragAmount
                                    val visualFractionStart = horizontalVisualFraction(tentativeStartOffset, layoutWidth, layoutHeight)
                                    val fractionForValue = if (isRtl) 1f - visualFractionStart else visualFractionStart
                                    val newStart = fractionToValueRange(fractionForValue).coerceAtMost(currentEndValue)
                                    val crossCondition = if (isRtl) dragAmount < 0f else dragAmount > 0f

                                    if (newStart >= currentEndValue && crossCondition && currentStartValue == currentEndValue) {
                                        isDraggingStart = false
                                        isDraggingEnd = true

                                        endDragOffset = tentativeStartOffset
                                        hapticState.resetEnd(currentEndValue)
                                        hapticState.inheritEndKeyPoint()

                                        val visualFractionEnd = horizontalVisualFraction(endDragOffset, layoutWidth, layoutHeight)
                                        val fractionForValueEnd = if (isRtl) 1f - visualFractionEnd else visualFractionEnd
                                        val newEnd = fractionToValueRange(fractionForValueEnd).coerceAtLeast(currentStartValue)
                                        currentEndValue = newEnd
                                        onValueChangeState(currentStartValue..newEnd)
                                        hapticState.handleEndHapticFeedback(
                                            newEnd,
                                            valueRange,
                                            hapticEffect,
                                            hapticFeedback,
                                            allKeyPointFractions,
                                            hasCustomKeyPoints = keyPoints != null,
                                        )
                                    } else {
                                        startDragOffset = tentativeStartOffset
                                        currentStartValue = newStart
                                        onValueChangeState(newStart..currentEndValue)
                                        hapticState.handleStartHapticFeedback(
                                            newStart,
                                            valueRange,
                                            hapticEffect,
                                            hapticFeedback,
                                            allKeyPointFractions,
                                            hasCustomKeyPoints = keyPoints != null,
                                        )
                                    }
                                } else if (isDraggingEnd) {
                                    lastDraggedIsStart = false
                                    val tentativeEndOffset = endDragOffset + dragAmount
                                    val visualFractionEnd = horizontalVisualFraction(tentativeEndOffset, layoutWidth, layoutHeight)
                                    val fractionForValue = if (isRtl) 1f - visualFractionEnd else visualFractionEnd
                                    val newEnd = fractionToValueRange(fractionForValue).coerceAtLeast(currentStartValue)
                                    val crossCondition = if (isRtl) dragAmount > 0f else dragAmount < 0f

                                    if (newEnd <= currentStartValue && crossCondition && currentStartValue == currentEndValue) {
                                        isDraggingEnd = false
                                        isDraggingStart = true
                                        startDragOffset = tentativeEndOffset
                                        hapticState.resetStart(currentStartValue)
                                        hapticState.inheritStartKeyPoint()

                                        val visualFractionStart = horizontalVisualFraction(startDragOffset, layoutWidth, layoutHeight)
                                        val fractionForValueStart = if (isRtl) 1f - visualFractionStart else visualFractionStart
                                        val newStart = fractionToValueRange(fractionForValueStart).coerceAtMost(currentEndValue)
                                        currentStartValue = newStart
                                        onValueChangeState(newStart..currentEndValue)
                                        hapticState.handleStartHapticFeedback(
                                            newStart,
                                            valueRange,
                                            hapticEffect,
                                            hapticFeedback,
                                            allKeyPointFractions,
                                            hasCustomKeyPoints = keyPoints != null,
                                        )
                                    } else {
                                        endDragOffset = tentativeEndOffset
                                        currentEndValue = newEnd
                                        onValueChangeState(currentStartValue..newEnd)
                                        hapticState.handleEndHapticFeedback(
                                            newEnd,
                                            valueRange,
                                            hapticEffect,
                                            hapticFeedback,
                                            allKeyPointFractions,
                                            hasCustomKeyPoints = keyPoints != null,
                                        )
                                    }
                                }
                            },
                            onDragStarted = { offset ->
                                val thumbRadius = layoutHeight / 2f
                                val availableWidth = (layoutWidth - 2f * thumbRadius).coerceAtLeast(0f)
                                val startFraction =
                                    (currentStartValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                                val endFraction =
                                    (currentEndValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                                val effectiveStartFraction = if (isRtl) 1f - startFraction else startFraction
                                val effectiveEndFraction = if (isRtl) 1f - endFraction else endFraction
                                val startPos = thumbRadius + effectiveStartFraction * availableWidth
                                val endPos = thumbRadius + effectiveEndFraction * availableWidth

                                val knobRadius = thumbRadius * 0.72f
                                val hitRadius = knobRadius + (thumbRadius * 0.5f)
                                val isOnStartThumb = abs(offset.x - startPos) <= hitRadius
                                val isOnEndThumb = abs(offset.x - endPos) <= hitRadius

                                when {
                                    isOnStartThumb && !isOnEndThumb -> {
                                        isDraggingStart = true
                                        startDragOffset = offset.x
                                        hapticState.resetStart(coercedStart)
                                    }

                                    !isOnStartThumb && isOnEndThumb -> {
                                        isDraggingEnd = true
                                        endDragOffset = offset.x
                                        hapticState.resetEnd(coercedEnd)
                                    }

                                    isOnStartThumb && isOnEndThumb -> {
                                        if (lastDraggedIsStart) {
                                            isDraggingStart = true
                                            startDragOffset = offset.x
                                            hapticState.resetStart(coercedStart)
                                        } else {
                                            isDraggingEnd = true
                                            endDragOffset = offset.x
                                            hapticState.resetEnd(coercedEnd)
                                        }
                                    }

                                    else -> {
                                        val diffStart = abs(offset.x - startPos)
                                        val diffEnd = abs(offset.x - endPos)
                                        if (diffStart <= diffEnd) {
                                            isDraggingStart = true
                                            startDragOffset = offset.x
                                            hapticState.resetStart(coercedStart)
                                        } else {
                                            isDraggingEnd = true
                                            endDragOffset = offset.x
                                            hapticState.resetEnd(coercedEnd)
                                        }
                                    }
                                }
                            },
                            onDragStopped = {
                                isDraggingStart = false
                                isDraggingEnd = false
                                onValueChangeFinishedState?.invoke()
                            },
                        )
                        .indication(interactionSource, null)
                } else {
                    Modifier
                },
            )
            .semantics {
                stateDescription = "$coercedStart-$coercedEnd"
            }
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackShape = remember {
            RoundedRectangle(CupertinoSliderTrackHeight / 2, style = RoundedCornerStyle.Continuous)
        }
        val thumbShape = remember {
            RoundedRectangle(CupertinoSliderThumbSize / 2, style = RoundedCornerStyle.Continuous)
        }
        val detentMarkerShape = remember {
            RoundedRectangle(
                CupertinoSliderDetentMarkerWidth / 2,
                style = RoundedCornerStyle.Continuous,
            )
        }
        val thumbSizePx = with(density) { CupertinoSliderThumbSize.toPx() }
        val markerWidthPx = with(density) { CupertinoSliderDetentMarkerWidth.toPx() }
        val containerWidthPx =
            if (constraints.maxWidth != Constraints.Infinity) constraints.maxWidth.toFloat() else 0f
        val usableWidthPx = (containerWidthPx - thumbSizePx).coerceAtLeast(1f)
        val startFraction =
            (animatedStartValueState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        val endFraction =
            (animatedEndValueState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        // Visual fractions flip in RTL so the thumb travel stays left-to-right on screen.
        val startVisualFraction = if (isRtl) 1f - startFraction else startFraction
        val endVisualFraction = if (isRtl) 1f - endFraction else endFraction
        val rangeStartVisualFraction = minOf(startVisualFraction, endVisualFraction)
        val rangeWidthVisualFraction = abs(endVisualFraction - startVisualFraction)

        // Thin track, inset horizontally by half a thumb so the fill spans thumb-center to
        // thumb-center, matching the horizontal slider.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = CupertinoSliderThumbSize / 2)
                .fillMaxWidth()
                .height(CupertinoSliderTrackHeight)
                .clip(trackShape)
                .background(colors.backgroundColor(enabled)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset((usableWidthPx * rangeStartVisualFraction).roundToInt(), 0) }
                    .fillMaxWidth(rangeWidthVisualFraction)
                    .fillMaxHeight()
                    .background(colors.foregroundColor(enabled)),
            )
        }

        if (showKeyPoints) {
            keyPointFractions.forEach { stepFraction ->
                val stepVisualFraction = if (isRtl) 1f - stepFraction else stepFraction
                val stepCenterX = thumbSizePx / 2f + stepVisualFraction * usableWidthPx
                val isInsideRange = stepFraction >= minOf(startFraction, endFraction) &&
                    stepFraction <= maxOf(startFraction, endFraction)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset((stepCenterX - markerWidthPx / 2f).roundToInt(), 0) }
                        .size(
                            width = CupertinoSliderDetentMarkerWidth,
                            height = CupertinoSliderDetentMarkerHeight,
                        )
                        .background(
                            color = if (isInsideRange) {
                                colors.keyPointForegroundColor()
                            } else {
                                colors.keyPointColor()
                            },
                            shape = detentMarkerShape,
                        ),
                )
            }
        }

        Spacer(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((usableWidthPx * startVisualFraction).roundToInt(), 0) }
                .size(CupertinoSliderThumbSize)
                .graphicsLayer {
                    scaleX = startThumbScaleState.value
                    scaleY = startThumbScaleState.value
                }
                .shadow(
                    elevation = if (enabled) CupertinoSliderThumbElevation else 0.dp,
                    shape = thumbShape,
                    clip = false,
                )
                .background(colors.thumbColor(enabled), thumbShape),
        )
        Spacer(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((usableWidthPx * endVisualFraction).roundToInt(), 0) }
                .size(CupertinoSliderThumbSize)
                .graphicsLayer {
                    scaleX = endThumbScaleState.value
                    scaleY = endThumbScaleState.value
                }
                .shadow(
                    elevation = if (enabled) CupertinoSliderThumbElevation else 0.dp,
                    shape = thumbShape,
                    clip = false,
                )
                .background(colors.thumbColor(enabled), thumbShape),
        )
    }
}

/**
 * Manages haptic feedback state for the slider.
 */
@Stable
internal class SliderHapticState {
    private var edgeFeedbackTriggered: Boolean = false
    private var lastStep: Float = 0f
    private var isAtKeyPoint: Boolean = false

    fun reset(currentValue: Float) {
        edgeFeedbackTriggered = false
        lastStep = currentValue
        isAtKeyPoint = false
    }

    fun handleHapticFeedback(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticEffect: SliderDefaults.SliderHapticEffect,
        hapticFeedback: HapticFeedback,
        keyPointFractions: FloatArray = floatArrayOf(),
        hasCustomKeyPoints: Boolean = false,
    ) {
        if (hapticEffect == SliderDefaults.SliderHapticEffect.None) return

        handleEdgeHaptic(currentValue, valueRange, hapticFeedback)

        if (hapticEffect == SliderDefaults.SliderHapticEffect.Step) {
            handleStepHaptic(currentValue, valueRange, hapticFeedback, keyPointFractions, hasCustomKeyPoints)
        }
    }

    private fun handleEdgeHaptic(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticFeedback: HapticFeedback,
    ) {
        val isAtEdge = currentValue == valueRange.start || currentValue == valueRange.endInclusive
        if (isAtEdge && !edgeFeedbackTriggered) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            edgeFeedbackTriggered = true
        } else if (!isAtEdge) {
            edgeFeedbackTriggered = false
        }
    }

    private fun handleStepHaptic(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticFeedback: HapticFeedback,
        keyPointFractions: FloatArray,
        hasCustomKeyPoints: Boolean,
    ) {
        val isNotAtEdge = currentValue != valueRange.start && currentValue != valueRange.endInclusive

        if (hasCustomKeyPoints && keyPointFractions.isNotEmpty()) {
            handleKeyPointHaptic(currentValue, valueRange, hapticFeedback, keyPointFractions, isNotAtEdge)
        } else if (currentValue != lastStep && isNotAtEdge) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastStep = currentValue
        }
    }

    private fun handleKeyPointHaptic(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticFeedback: HapticFeedback,
        keyPointFractions: FloatArray,
        isNotAtEdge: Boolean,
    ) {
        val fraction = (currentValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        val threshold = 0.005f

        var nearestDist = Float.MAX_VALUE
        for (i in keyPointFractions.indices) {
            val dist = abs(keyPointFractions[i] - fraction)
            if (dist < nearestDist) nearestDist = dist
        }
        val currentlyAtKeyPoint = nearestDist < threshold

        if (currentlyAtKeyPoint && !isAtKeyPoint && isNotAtEdge) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }

        isAtKeyPoint = currentlyAtKeyPoint
    }
}

/**
 * Manages haptic feedback state for the range slider.
 */
@Stable
internal class RangeSliderHapticState {
    private var startEdgeFeedbackTriggered: Boolean = false
    private var endEdgeFeedbackTriggered: Boolean = false
    private var startLastStep: Float = 0f
    private var endLastStep: Float = 0f
    private var startIsAtKeyPoint: Boolean = false
    private var endIsAtKeyPoint: Boolean = false

    fun resetStart(currentValue: Float) {
        startEdgeFeedbackTriggered = false
        startLastStep = currentValue
        startIsAtKeyPoint = false
    }

    fun resetEnd(currentValue: Float) {
        endEdgeFeedbackTriggered = false
        endLastStep = currentValue
        endIsAtKeyPoint = false
    }

    fun inheritStartKeyPoint() {
        startIsAtKeyPoint = endIsAtKeyPoint
    }

    fun inheritEndKeyPoint() {
        endIsAtKeyPoint = startIsAtKeyPoint
    }

    fun handleStartHapticFeedback(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticEffect: SliderDefaults.SliderHapticEffect,
        hapticFeedback: HapticFeedback,
        keyPointFractions: FloatArray = floatArrayOf(),
        hasCustomKeyPoints: Boolean = false,
    ) {
        handleHapticFeedbackInternal(
            currentValue = currentValue,
            valueRange = valueRange,
            hapticEffect = hapticEffect,
            hapticFeedback = hapticFeedback,
            keyPointFractions = keyPointFractions,
            edgeFeedbackTriggered = startEdgeFeedbackTriggered,
            lastStep = startLastStep,
            isAtKeyPoint = startIsAtKeyPoint,
            isStartEdge = true,
            hasCustomKeyPoints = hasCustomKeyPoints,
            onEdgeFeedbackUpdate = { startEdgeFeedbackTriggered = it },
            onLastStepUpdate = { startLastStep = it },
            onKeyPointUpdate = { startIsAtKeyPoint = it },
        )
    }

    fun handleEndHapticFeedback(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticEffect: SliderDefaults.SliderHapticEffect,
        hapticFeedback: HapticFeedback,
        keyPointFractions: FloatArray = floatArrayOf(),
        hasCustomKeyPoints: Boolean = false,
    ) {
        handleHapticFeedbackInternal(
            currentValue = currentValue,
            valueRange = valueRange,
            hapticEffect = hapticEffect,
            hapticFeedback = hapticFeedback,
            keyPointFractions = keyPointFractions,
            edgeFeedbackTriggered = endEdgeFeedbackTriggered,
            lastStep = endLastStep,
            isAtKeyPoint = endIsAtKeyPoint,
            isStartEdge = false,
            hasCustomKeyPoints = hasCustomKeyPoints,
            onEdgeFeedbackUpdate = { endEdgeFeedbackTriggered = it },
            onLastStepUpdate = { endLastStep = it },
            onKeyPointUpdate = { endIsAtKeyPoint = it },
        )
    }

    private fun handleHapticFeedbackInternal(
        currentValue: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        hapticEffect: SliderDefaults.SliderHapticEffect,
        hapticFeedback: HapticFeedback,
        keyPointFractions: FloatArray,
        edgeFeedbackTriggered: Boolean,
        lastStep: Float,
        isAtKeyPoint: Boolean,
        isStartEdge: Boolean,
        hasCustomKeyPoints: Boolean,
        onEdgeFeedbackUpdate: (Boolean) -> Unit,
        onLastStepUpdate: (Float) -> Unit,
        onKeyPointUpdate: (Boolean) -> Unit,
    ) {
        if (hapticEffect == SliderDefaults.SliderHapticEffect.None) return

        val targetEdge = if (isStartEdge) valueRange.start else valueRange.endInclusive
        val isAtEdge = currentValue == targetEdge

        if (isAtEdge && !edgeFeedbackTriggered) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            onEdgeFeedbackUpdate(true)
        } else if (!isAtEdge) {
            onEdgeFeedbackUpdate(false)
        }

        if (hapticEffect == SliderDefaults.SliderHapticEffect.Step) {
            val isNotAtEdge = currentValue != targetEdge

            if (hasCustomKeyPoints && keyPointFractions.isNotEmpty()) {
                val fraction = (currentValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                val threshold = 0.005f

                var nearestDist = Float.MAX_VALUE
                for (i in keyPointFractions.indices) {
                    val dist = abs(keyPointFractions[i] - fraction)
                    if (dist < nearestDist) nearestDist = dist
                }
                val currentlyAtKeyPoint = nearestDist < threshold

                if (currentlyAtKeyPoint && !isAtKeyPoint && isNotAtEdge) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                onKeyPointUpdate(currentlyAtKeyPoint)
            } else if (currentValue != lastStep && isNotAtEdge) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onLastStepUpdate(currentValue)
            }
        }
    }
}

private fun stepsToTickFractions(steps: Int): FloatArray = if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }

private fun resolveValueFromFraction(
    fraction: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    allKeyPointFractions: FloatArray,
    magnetThreshold: Float,
): Float {
    val f = fraction.coerceIn(0f, 1f)
    val base = lerp(valueRange.start, valueRange.endInclusive, f)
    return when {
        steps > 0 -> {
            val stepCount = steps + 1
            val start = valueRange.start.toDouble()
            val end = valueRange.endInclusive.toDouble()
            val stepIndex = (f * stepCount).roundToInt().coerceIn(0, stepCount)
            (start + (end - start) * stepIndex / stepCount).toFloat()
        }

        allKeyPointFractions.isNotEmpty() -> {
            var closest = allKeyPointFractions[0]
            var bestDist = abs(closest - f)
            for (i in 1 until allKeyPointFractions.size) {
                val cand = allKeyPointFractions[i]
                val dist = abs(cand - f)
                if (dist < bestDist) {
                    bestDist = dist
                    closest = cand
                }
            }
            if (bestDist < magnetThreshold) {
                lerp(valueRange.start, valueRange.endInclusive, closest)
            } else {
                base
            }
        }

        else -> base
    }
}

// Thumb press/hover/drag scale spring; equivalent to the previous stiffness 987f, damping 0.6f.
private val ThumbScaleAnimationSpec = yubeixSpring<Float>(damping = 0.6f, response = 0.2f)

private fun horizontalVisualFraction(offsetX: Float, sizeWidth: Int, sizeHeight: Int): Float {
    val thumbRadius = sizeHeight / 2f
    val availableWidth = (sizeWidth.toFloat() - 2f * thumbRadius).coerceAtLeast(0f)
    return if (availableWidth == 0f) 0f else ((offsetX - thumbRadius) / availableWidth).coerceIn(0f, 1f)
}

private fun verticalVisualFraction(offsetY: Float, sizeHeight: Int, sizeWidth: Int): Float {
    val thumbRadius = sizeWidth / 2f
    val availableHeight = (sizeHeight.toFloat() - 2f * thumbRadius).coerceAtLeast(0f)
    return if (availableHeight == 0f) 0f else ((offsetY - thumbRadius) / availableHeight).coerceIn(0f, 1f)
}

/**
 * Converts point values to normalized fractions within the value range.
 */
private fun pointsToFractions(
    points: List<Float>,
    valueRange: ClosedFloatingPointRange<Float>,
): FloatArray = points.map { point ->
    ((point - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
}.toFloatArray()

/**
 * Computes key point fractions for slider display.
 * Filters out points too close to edges.
 */
private fun computeKeyPointFractions(
    keyPoints: List<Float>?,
    stepFractions: FloatArray,
    valueRange: ClosedFloatingPointRange<Float>,
    showKeyPoints: Boolean,
): FloatArray = when {
    keyPoints != null -> pointsToFractions(keyPoints, valueRange)
    showKeyPoints -> stepFractions
    else -> floatArrayOf()
}

/**
 * Computes all key point fractions including edge points.
 * Used for haptic feedback and magnetic snapping.
 */
private fun computeAllKeyPointFractions(
    keyPoints: List<Float>?,
    stepFractions: FloatArray,
    valueRange: ClosedFloatingPointRange<Float>,
): FloatArray = when {
    keyPoints != null -> pointsToFractions(keyPoints, valueRange)
    stepFractions.isNotEmpty() -> stepFractions
    else -> floatArrayOf()
}

object SliderDefaults {
    /**
     * The default height of the [Slider] and [RangeSlider] containers. Matches the Cupertino
     * reference: a 20.dp floating thumb plus 12.dp of breathing room around the 4.dp track.
     */
    val MinHeight = 32.dp

    /**
     * The radius of the key points on the [Slider] and [RangeSlider].
     */
    val KeyPointRadius = 3.855.dp

    /**
     * The type of haptic feedback to be used for the slider.
     */
    enum class SliderHapticEffect {
        /** No haptic feedback. */
        None,

        /** Haptic feedback at 0% and 100%. */
        Edge,

        /** Haptic feedback at steps. */
        Step,
    }

    /**
     * The default haptic effect of the [Slider] and [RangeSlider].
     */
    val DefaultHapticEffect = SliderHapticEffect.Edge

    @Composable
    fun sliderColors(
        foregroundColor: Color = YubeixTheme.colorScheme.primary,
        disabledForegroundColor: Color = YubeixTheme.colorScheme.disabledPrimarySlider,
        backgroundColor: Color = YubeixTheme.colorScheme.sliderBackground,
        disabledBackgroundColor: Color = YubeixTheme.colorScheme.disabledSecondary,
        thumbColor: Color = YubeixTheme.colorScheme.onPrimary,
        disabledThumbColor: Color = YubeixTheme.colorScheme.disabledOnPrimary,
        keyPointColor: Color = YubeixTheme.colorScheme.sliderKeyPoint,
        keyPointForegroundColor: Color = YubeixTheme.colorScheme.sliderKeyPointForeground,
    ): SliderColors = remember(
        foregroundColor,
        disabledForegroundColor,
        backgroundColor,
        disabledBackgroundColor,
        thumbColor,
        disabledThumbColor,
        keyPointColor,
        keyPointForegroundColor,
    ) {
        SliderColors(
            foregroundColor = foregroundColor,
            disabledForegroundColor = disabledForegroundColor,
            backgroundColor = backgroundColor,
            disabledBackgroundColor = disabledBackgroundColor,
            thumbColor = thumbColor,
            disabledThumbColor = disabledThumbColor,
            keyPointColor = keyPointColor,
            keyPointForegroundColor = keyPointForegroundColor,
        )
    }
}

@Immutable
data class SliderColors(
    private val foregroundColor: Color,
    private val disabledForegroundColor: Color,
    private val backgroundColor: Color,
    private val disabledBackgroundColor: Color,
    private val thumbColor: Color,
    private val disabledThumbColor: Color,
    private val keyPointColor: Color,
    private val keyPointForegroundColor: Color,
) {
    @Stable
    internal fun foregroundColor(enabled: Boolean): Color = if (enabled) foregroundColor else disabledForegroundColor

    @Stable
    internal fun backgroundColor(enabled: Boolean): Color = if (enabled) backgroundColor else disabledBackgroundColor

    @Stable
    internal fun thumbColor(enabled: Boolean): Color = if (enabled) thumbColor else disabledThumbColor

    @Stable
    internal fun keyPointColor(): Color = keyPointColor

    @Stable
    internal fun keyPointForegroundColor(): Color = keyPointForegroundColor
}

// Cupertino slider metrics.
private val CupertinoSliderThumbSize = 20.dp
private val CupertinoSliderTrackHeight = 4.dp
private val CupertinoSliderDetentMarkerWidth = 3.dp
private val CupertinoSliderDetentMarkerHeight = 10.dp
private val CupertinoSliderThumbElevation = 8.dp
