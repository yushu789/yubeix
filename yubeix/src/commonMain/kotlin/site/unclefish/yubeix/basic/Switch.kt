// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedCornerStyle
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * A Cupertino style [Switch].
 *
 * The thumb slides between the track's two ends through an alignment spring; pressing or hovering
 * widens the thumb to 1.25x its resting aspect ratio, the way UISwitch does. A horizontal drag
 * follows the finger directly (no liquid spring) and commits the toggle the moment the thumb
 * crosses the track's midpoint threshold. A haptic tick plays on every [checked] change.
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
    val switchColors = colors
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedAspectRatio by animateFloatAsState(
        targetValue = if (isPressed || isHovered) 1.25f else 1f,
        label = "YubeixSwitchAspectRatio",
    )
    val animatedBackground by animateColorAsState(
        targetValue = switchColors.trackColor(enabled, checked),
        label = "YubeixSwitchTrackColor",
    )
    val animatedAlignment by animateFloatAsState(
        targetValue = if (checked) 1f else -1f,
        label = "YubeixSwitchAlignment",
    )
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val updatedChecked by rememberUpdatedState(checked)
    val updatedOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val dragThreshold = with(density) {
        (CUPERTINO_SWITCH_WIDTH - CUPERTINO_SWITCH_THUMB_PADDING * 2 - CUPERTINO_SWITCH_HEIGHT).toPx()
    }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val switchShape = remember {
        RoundedRectangle(CUPERTINO_SWITCH_HEIGHT / 2, style = RoundedCornerStyle.Continuous)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { updatedChecked }
            .drop(1)
            .collect {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            }
    }

    LaunchedEffect(dragThreshold) {
        snapshotFlow {
            when {
                dragDistance < 0f -> false
                dragDistance > dragThreshold -> true
                else -> null
            }
        }
            .filterNotNull()
            .collect { value -> updatedOnCheckedChange?.invoke(value) }
    }

    Column(
        modifier
            .wrapContentSize(Alignment.Center)
            .requiredSize(CUPERTINO_SWITCH_WIDTH, CUPERTINO_SWITCH_HEIGHT)
            .clip(switchShape)
            .drawBehind {
                drawRect(animatedBackground)
            }
            .padding(CUPERTINO_SWITCH_THUMB_PADDING)
            .toggleable(
                value = checked,
                onValueChange = { value -> updatedOnCheckedChange?.invoke(value) },
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
            )
            .hoverable(
                interactionSource = interactionSource,
                enabled = enabled,
            ),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .aspectRatio(animatedAspectRatio)
                .pointerInput(dragThreshold) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragDistance = if (updatedChecked) dragThreshold else 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragDistance += dragAmount
                        },
                    )
                }
                .align(BiasAlignment.Horizontal(animatedAlignment))
                .then(
                    if (enabled) {
                        Modifier.shadow(
                            elevation = CUPERTINO_SWITCH_THUMB_ELEVATION,
                            shape = switchShape,
                        )
                    } else {
                        Modifier.clip(switchShape)
                    },
                )
                .background(switchColors.thumbColor(enabled), switchShape),
        )
    }
}

private val CUPERTINO_SWITCH_WIDTH = 51.dp
private val CUPERTINO_SWITCH_HEIGHT = 31.dp
private val CUPERTINO_SWITCH_THUMB_PADDING = 2.dp
private val CUPERTINO_SWITCH_THUMB_ELEVATION = 4.dp

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
