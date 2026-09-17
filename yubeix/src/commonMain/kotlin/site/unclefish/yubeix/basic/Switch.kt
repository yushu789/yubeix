// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.shapes.RoundedCornerStyle
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import site.unclefish.yubeix.anim.DampedDragAnimation
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * A Cupertino style [Switch].
 *
 * The thumb is driven by a damped drag animation, so drags carry the flick velocity of the finger
 * and settle with a spring, while plain taps toggle through the same spring. The commit decision
 * happens on release: the switch ends on whichever side of the midpoint the thumb was left at
 * (or animates to the flipped state for taps). A haptic tick plays on every [checked] change.
 *
 * @param checked The checked state of the [Switch].
 * @param onCheckedChange The callback to be called when the state of the [Switch] changes.
 *   When `null` the [Switch] is a static display and cannot be interacted with.
 * @param modifier The modifier to be applied to the [Switch].
 * @param colors The [SwitchColors] of the [Switch].
 * @param enabled Whether the [Switch] is enabled.
 * @param interactionSource The [MutableInteractionSource] backing press and hover feedback.
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    colors: SwitchColors = SwitchDefaults.switchColors(),
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val currentChecked by rememberUpdatedState(checked)
    val currentHapticFeedback by rememberUpdatedState(LocalHapticFeedback.current)
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    // The thumb widens (not grows) under press/hover so the squeeze stays inside the track.
    val animatedAspectRatio by animateFloatAsState(
        targetValue = if (isPressed || isHovered) 1.25f else 1f,
        label = "YubeixSwitchAspectRatio",
    )
    val animatedBackground by animateColorAsState(
        targetValue = colors.trackColor(enabled, checked),
        label = "YubeixSwitchTrackColor",
    )

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    // Travel of the thumb: track inner width minus the resting thumb width (height minus padding).
    val dragWidth = with(density) { (CupertinoSwitchWidth - CupertinoSwitchHeight).toPx() }
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }
    val dampedDragAnimation = remember(dragWidth) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            // No press scale: the thumb is clipped to the track, so feedback is the aspect squeeze.
            pressedScale = 1f,
            consumeDragChanges = true,
            onDragStarted = {},
            onDragStopped = {
                if (didDrag) {
                    val target = if (targetValue >= 0.5f) 1f else 0f
                    fraction = target
                    currentOnCheckedChange?.invoke(target == 1f)
                    didDrag = false
                }
                // Without a real drag the tap falls through to [Modifier.toggleable] below.
            },
            onDrag = { _, dragAmount ->
                if (!didDrag) {
                    didDrag = dragAmount.x != 0f
                }
                if (didDrag) {
                    val delta = dragAmount.x / dragWidth
                    fraction =
                        if (isLtr) {
                            (fraction + delta).fastCoerceIn(0f, 1f)
                        } else {
                            (fraction - delta).fastCoerceIn(0f, 1f)
                        }
                }
            },
        )
    }

    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { fraction }
            .collectLatest { fraction ->
                dampedDragAnimation.updateValue(fraction)
            }
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { currentChecked }
            .collectLatest { isChecked ->
                val target = if (isChecked) 1f else 0f
                if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { currentChecked }
            .drop(1)
            .collect {
                currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            }
    }

    val switchShape = remember {
        RoundedRectangle(CupertinoSwitchHeight / 2, style = RoundedCornerStyle.Continuous)
    }
    val hasCallback = onCheckedChange != null
    val toggleableModifier = if (hasCallback) {
        Modifier.toggleable(
            value = checked,
            onValueChange = { value -> currentOnCheckedChange?.invoke(value) },
            enabled = enabled,
            role = Role.Switch,
            interactionSource = interactionSource,
            indication = null,
        )
    } else {
        Modifier
    }

    Column(
        modifier
            .then(toggleableModifier)
            .hoverable(
                interactionSource = interactionSource,
                enabled = enabled,
            )
            .wrapContentSize(Alignment.Center)
            .requiredSize(CupertinoSwitchWidth, CupertinoSwitchHeight)
            .clip(switchShape)
            .drawBehind {
                drawRect(animatedBackground)
            }
            .padding(CupertinoSwitchThumbPadding),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .aspectRatio(animatedAspectRatio)
                .align(Alignment.Start)
                .graphicsLayer {
                    val value = dampedDragAnimation.value
                    // Start-anchored when unchecked, end-anchored when checked, so the press
                    // widening grows toward the track interior instead of past its clipped edge.
                    val widening = (size.width - size.height).coerceAtLeast(0f)
                    translationX =
                        if (isLtr) {
                            lerp(0f, dragWidth, value) - widening * value
                        } else {
                            lerp(0f, -dragWidth, value) + widening * value
                        }
                    // Squash the thumb against the direction of travel while it moves fast.
                    val velocity = dampedDragAnimation.velocity / 50f
                    scaleX = 1f / (1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f))
                    scaleY = 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                }
                .then(
                    if (enabled) {
                        Modifier.shadow(
                            elevation = CupertinoSwitchThumbElevation,
                            shape = switchShape,
                        )
                    } else {
                        Modifier.clip(switchShape)
                    },
                )
                .background(colors.thumbColor(enabled), switchShape)
                .then(if (enabled) dampedDragAnimation.modifier else Modifier),
        )
    }
}

object SwitchDefaults {

    /**
     * The default colors for the [Switch], following the Cupertino palette:
     * a white thumb over a primary (checked) / neutral (unchecked) track.
     */
    @Composable
    fun switchColors(
        checkedThumbColor: Color = Color.White,
        uncheckedThumbColor: Color = Color.White,
        disabledCheckedThumbColor: Color = Color.White.copy(alpha = 0.72f),
        disabledUncheckedThumbColor: Color = Color.White.copy(alpha = 0.72f),
        checkedTrackColor: Color = YubeixTheme.colorScheme.primary,
        uncheckedTrackColor: Color = if (YubeixTheme.colorScheme.surface.luminance() >= 0.5f) {
            Color(0xFFE3E3E3)
        } else {
            Color(0xFF4B4B50)
        },
        disabledCheckedTrackColor: Color = checkedTrackColor.copy(alpha = 0.33f),
        disabledUncheckedTrackColor: Color = uncheckedTrackColor.copy(alpha = 0.62f),
    ): SwitchColors = remember(
        checkedThumbColor,
        uncheckedThumbColor,
        disabledCheckedThumbColor,
        disabledUncheckedThumbColor,
        checkedTrackColor,
        uncheckedTrackColor,
        disabledCheckedTrackColor,
        disabledUncheckedTrackColor,
    ) {
        SwitchColors(
            checkedThumbColor = checkedThumbColor,
            uncheckedThumbColor = uncheckedThumbColor,
            disabledCheckedThumbColor = disabledCheckedThumbColor,
            disabledUncheckedThumbColor = disabledUncheckedThumbColor,
            checkedTrackColor = checkedTrackColor,
            uncheckedTrackColor = uncheckedTrackColor,
            disabledCheckedTrackColor = disabledCheckedTrackColor,
            disabledUncheckedTrackColor = disabledUncheckedTrackColor,
        )
    }
}

@Immutable
data class SwitchColors(
    private val checkedThumbColor: Color,
    private val uncheckedThumbColor: Color,
    private val disabledCheckedThumbColor: Color,
    private val disabledUncheckedThumbColor: Color,
    private val checkedTrackColor: Color,
    private val uncheckedTrackColor: Color,
    private val disabledCheckedTrackColor: Color,
    private val disabledUncheckedTrackColor: Color,
) {
    @Stable
    internal fun checkedThumbColor(enabled: Boolean): Color = if (enabled) checkedThumbColor else disabledCheckedThumbColor

    @Stable
    internal fun uncheckedThumbColor(enabled: Boolean): Color = if (enabled) uncheckedThumbColor else disabledUncheckedThumbColor

    @Stable
    internal fun checkedTrackColor(enabled: Boolean): Color = if (enabled) checkedTrackColor else disabledCheckedTrackColor

    @Stable
    internal fun uncheckedTrackColor(enabled: Boolean): Color = if (enabled) uncheckedTrackColor else disabledUncheckedTrackColor

    @Stable
    internal fun thumbColor(enabled: Boolean): Color = if (enabled) checkedThumbColor else disabledCheckedThumbColor

    @Stable
    internal fun trackColor(enabled: Boolean, checked: Boolean): Color = when {
        enabled && checked -> checkedTrackColor
        enabled -> uncheckedTrackColor
        checked -> disabledCheckedTrackColor
        else -> disabledUncheckedTrackColor
    }
}

private val CupertinoSwitchWidth = 51.dp
private val CupertinoSwitchHeight = 31.dp
private val CupertinoSwitchThumbPadding = 2.dp
private val CupertinoSwitchThumbElevation = 4.dp
