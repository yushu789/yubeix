// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import site.unclefish.yubeix.anim.yubeixSpring
import site.unclefish.yubeix.blur.isRenderEffectSupported
import site.unclefish.yubeix.theme.LocalReducedDynamicEffectsEnabled
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape
import site.unclefish.yubeix.utils.Platform
import site.unclefish.yubeix.utils.platform

/**
 * A [NavigationBar] that with 2 to 5 items, styled after the iOS bottom tab bar: a translucent
 * (optionally blurred) glass surface with a hairline on its top edge.
 *
 * @param modifier The modifier to be applied to the [NavigationBar].
 * @param color The color of the [NavigationBar]. The default is a translucent tint, matching the
 *   iOS tab bar; it is also used as the haze tint when [hazeState] is provided.
 * @param showDivider Whether to show the hairline divider on the top edge of the [NavigationBar].
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [NavigationBar].
 * @param mode The mode for displaying items in the [NavigationBar]. It can show icons, text or both.
 * @param hazeState The [HazeState] of the content scrolled behind the bar. When non-null (and
 *   reduced dynamic effects are not requested) the bar blurs that content behind its translucent
 *   surface; otherwise it falls back to the translucent [color] alone.
 * @param content The content of the [NavigationBar], usually [NavigationBarItem]s.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    color: Color = YubeixTheme.colorScheme.background.copy(alpha = NavigationBarDefaults.BACKGROUND_ALPHA),
    showDivider: Boolean = true,
    defaultWindowInsetsPadding: Boolean = true,
    mode: NavigationBarDisplayMode = NavigationBarDisplayMode.IconAndText,
    hazeState: HazeState? = null,
    content: @Composable RowScope.() -> Unit,
) {
    CupertinoNavigationBar(
        modifier = modifier,
        color = color,
        showDivider = showDivider,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        hazeState = hazeState,
    ) {
        CompositionLocalProvider(LocalNavigationBarDisplayMode provides mode) {
            content()
        }
    }
}

/**
 * A [NavigationBar] driven by a list of [NavigationBarItemSpec]s, styled after the iOS bottom tab
 * bar: a translucent (optionally blurred) glass surface with a hairline on its top edge, a selected
 * item tinted with the theme accent, and a tick of haptic feedback on every tap.
 *
 * @param selectedIndex The index of the currently selected item.
 * @param items The items to display, at least two.
 * @param onItemSelected The callback when an item is selected.
 * @param modifier The modifier to be applied to the [NavigationBar].
 * @param hazeState The [HazeState] of the content scrolled behind the bar. When non-null (and
 *   reduced dynamic effects are not requested) the bar blurs that content behind its translucent
 *   surface; otherwise it falls back to the translucent background color alone.
 * @param hapticFeedbackEnabled Whether item taps trigger haptic feedback.
 * @param showDivider Whether to show the hairline divider on the top edge of the [NavigationBar].
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [NavigationBar].
 */
@Composable
fun NavigationBar(
    selectedIndex: Int,
    items: List<NavigationBarItemSpec>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    hapticFeedbackEnabled: Boolean = true,
    showDivider: Boolean = true,
    defaultWindowInsetsPadding: Boolean = true,
) {
    require(items.size >= 2) { "Cupertino tab bars need at least two items." }

    val currentIndex = selectedIndex.coerceIn(items.indices)
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)

    CupertinoNavigationBar(
        modifier = modifier,
        color = YubeixTheme.colorScheme.background.copy(alpha = NavigationBarDefaults.BACKGROUND_ALPHA),
        showDivider = showDivider,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        hazeState = hazeState,
    ) {
        items.forEachIndexed { index, item ->
            CupertinoTabBarItem(
                item = item,
                selected = index == currentIndex,
                hapticFeedbackEnabled = currentHapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { currentOnItemSelected(index) },
            )
        }
    }
}

/**
 * The shared iOS tab bar container: a translucent or haze-blurred surface of [NavigationBarDefaults.BAR_HEIGHT]
 * plus the bottom navigation bar inset, a row of items top-aligned inside it, and an optional
 * hairline along the top edge.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun CupertinoNavigationBar(
    color: Color,
    showDivider: Boolean,
    defaultWindowInsetsPadding: Boolean,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val reducedDynamicEffectsEnabled = LocalReducedDynamicEffectsEnabled.current
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val insetPadding = if (defaultWindowInsetsPadding) navigationBarBottom else 0.dp
    val hazeStyle = remember(color) {
        HazeStyle(
            backgroundColor = color,
            tints = listOf(HazeTint(color)),
            blurRadius = NavigationBarDefaults.BLUR_RADIUS,
            noiseFactor = 0f,
            fallbackTint = HazeTint(color),
        )
    }
    // Without RenderEffect support (e.g. Android below API 31) hazeEffect is a silent no-op and
    // would leave the bar fully transparent, so fall back to the translucent background there.
    val useHaze = hazeState != null && isRenderEffectSupported() && !reducedDynamicEffectsEnabled

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NavigationBarDefaults.BAR_HEIGHT + insetPadding)
            .then(
                when {
                    useHaze -> {
                        Modifier.hazeEffect(
                            state = requireNotNull(hazeState),
                            style = hazeStyle,
                        ) {
                            inputScale = HazeInputScale.Auto
                        }
                    }

                    else -> {
                        Modifier.background(color)
                    }
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(NavigationBarDefaults.BAR_HEIGHT)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(cupertinoHairline())
                    .background(YubeixTheme.colorScheme.dividerLine),
            )
        }
    }
}

/**
 * A hairline that is one physical pixel tall, like the iOS tab bar's top edge.
 */
@Composable
private fun cupertinoHairline(): Dp = (1f / LocalDensity.current.density).dp

/**
 * The item model for the list-driven [NavigationBar] overload.
 *
 * @param label The label of the item, also used as its content description.
 * @param icon The icon of the item.
 * @param selectedIcon The icon of the item when selected; defaults to [icon].
 */
@Immutable
data class NavigationBarItemSpec(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

@Composable
private fun CupertinoTabBarItem(
    item: NavigationBarItemSpec,
    selected: Boolean,
    hapticFeedbackEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        YubeixTheme.colorScheme.primary
    } else {
        YubeixTheme.colorScheme.onSurface.copy(alpha = NavigationBarDefaults.INACTIVE_ALPHA)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnClick = rememberUpdatedState(onClick)
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
    val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = {
                    if (currentHapticEnabled) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                    currentOnClick.value()
                },
            )
            .padding(vertical = NavigationBarDefaults.ITEM_VERTICAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(NavigationBarDefaults.CUPERTINO_ICON_SIZE),
        )
        Spacer(modifier = Modifier.height(NavigationBarDefaults.ICON_LABEL_SPACING))
        Text(
            text = item.label,
            color = contentColor,
            textAlign = TextAlign.Center,
            fontSize = NavigationBarDefaults.LABEL_FONT_SIZE,
            fontWeight = NavigationBarDefaults.LABEL_FONT_WEIGHT,
            letterSpacing = NavigationBarDefaults.LABEL_LETTER_SPACING,
            maxLines = 1,
        )
    }
}

/**
 * A [NavigationBarItem] that is suitable for [NavigationBar], styled after the iOS tab bar item:
 * the selected item is tinted with the theme accent while unselected ones sit at a resting alpha,
 * the label uses the iOS tab bar type scale, and a tap plays a tick of haptic feedback with a
 * subtle press-down scale.
 *
 * @param selected Whether the item is selected.
 * @param onClick The callback when the item is clicked.
 * @param icon The icon of the item.
 * @param label The label of the item.
 * @param modifier The modifier to be applied to the [NavigationBarItem].
 * @param enabled Whether the item is enabled.
 * @param hapticFeedbackEnabled Whether item taps trigger haptic feedback.
 */
@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticFeedbackEnabled: Boolean = true,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnClick by rememberUpdatedState(onClick)
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
    var isPressed by remember { mutableStateOf(false) }
    val mode = LocalNavigationBarDisplayMode.current

    val tint = if (selected) {
        YubeixTheme.colorScheme.primary
    } else {
        YubeixTheme.colorScheme.onSurface.copy(alpha = NavigationBarDefaults.INACTIVE_ALPHA)
    }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) NavigationBarDefaults.PRESSED_ITEM_SCALE else 1f,
        animationSpec = yubeixSpring(damping = 0.6f, response = 0.25f),
        label = "navigationBarItemSelectedScale",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .weight(1f)
            .semantics { this.selected = selected }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .pointerInput(enabled, hapticFeedbackEnabled) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (enabled) {
                            if (hapticFeedbackEnabled) {
                                currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                            currentOnClick()
                        }
                    },
                )
            },
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (mode) {
            NavigationBarDisplayMode.IconAndText -> {
                Icon(
                    modifier = Modifier.size(NavigationBarDefaults.CUPERTINO_ICON_SIZE),
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                )
                Spacer(modifier = Modifier.height(NavigationBarDefaults.ICON_LABEL_SPACING))
                Text(
                    text = label,
                    color = tint,
                    textAlign = TextAlign.Center,
                    fontSize = NavigationBarDefaults.LABEL_FONT_SIZE,
                    fontWeight = NavigationBarDefaults.LABEL_FONT_WEIGHT,
                    letterSpacing = NavigationBarDefaults.LABEL_LETTER_SPACING,
                    maxLines = 1,
                )
            }

            NavigationBarDisplayMode.IconWithSelectedLabel -> {
                // The label keeps its slot reserved so the icon never shifts; it only fades in
                // when the item is selected.
                val labelAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(durationMillis = 300),
                    label = "navigationBarItemSelectedLabelAlpha",
                )
                Icon(
                    modifier = Modifier.size(NavigationBarDefaults.CUPERTINO_ICON_SIZE),
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                )
                Spacer(modifier = Modifier.height(NavigationBarDefaults.ICON_LABEL_SPACING))
                Text(
                    modifier = Modifier.graphicsLayer { alpha = labelAlpha },
                    text = label,
                    color = tint,
                    textAlign = TextAlign.Center,
                    fontSize = NavigationBarDefaults.LABEL_FONT_SIZE,
                    fontWeight = NavigationBarDefaults.LABEL_FONT_WEIGHT,
                    letterSpacing = NavigationBarDefaults.LABEL_LETTER_SPACING,
                    maxLines = 1,
                )
            }

            NavigationBarDisplayMode.TextOnly -> {
                Text(
                    text = label,
                    color = tint,
                    textAlign = TextAlign.Center,
                    fontSize = NavigationBarDefaults.LABEL_FONT_SIZE,
                    fontWeight = NavigationBarDefaults.LABEL_FONT_WEIGHT,
                    letterSpacing = NavigationBarDefaults.LABEL_LETTER_SPACING,
                    maxLines = 1,
                )
            }

            NavigationBarDisplayMode.IconOnly -> {
                Icon(
                    modifier = Modifier.size(NavigationBarDefaults.CUPERTINO_ICON_SIZE),
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                )
            }
        }
    }
}

/**
 * A floating navigation bar that supports 2 to 5 items.
 *
 * @param modifier A [Modifier] to be applied to the [FloatingNavigationBar] for additional customization.
 * @param color The background color of the [FloatingNavigationBar].
 * @param cornerRadius The corner radius of the [FloatingNavigationBar], used for rounded corners.
 * @param horizontalAlignment The alignment of the [FloatingNavigationBar] within its parent, typically used to center it horizontally.
 * @param horizontalOutSidePadding The horizontal padding to be applied outside the [FloatingNavigationBar].
 * @param shadowElevation The shadow elevation of the [FloatingNavigationBar].
 * @param showDivider Whether to show the divider line around the [FloatingNavigationBar].
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [FloatingNavigationBar].
 * @param mode The mode for displaying items in the [FloatingNavigationBar]. It can show icons, text or both.
 * @param content The content of the [FloatingNavigationBar], usually [FloatingNavigationBarItem]s.
 */
@Composable
fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    color: Color = YubeixTheme.colorScheme.surfaceContainer,
    cornerRadius: Dp = FloatingToolbarDefaults.CornerRadius,
    horizontalAlignment: Alignment.Horizontal = CenterHorizontally,
    horizontalOutSidePadding: Dp = FloatingNavigationBarDefaults.HorizontalOutSidePadding,
    shadowElevation: Dp = FloatingNavigationBarDefaults.ShadowElevation,
    showDivider: Boolean = false,
    defaultWindowInsetsPadding: Boolean = true,
    mode: FloatingNavigationBarDisplayMode = FloatingNavigationBarDisplayMode.IconOnly,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val shape = yubeixShape(cornerRadius)

    val platform = platform()
    val bottomPaddingValue = when (platform) {
        Platform.IOS -> 8.dp

        Platform.Android -> {
            val navBarBottomPadding =
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding()
            if (navBarBottomPadding != 0.dp) 8.dp + navBarBottomPadding else 36.dp
        }

        else -> 36.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (horizontalAlignment == Alignment.Start) horizontalOutSidePadding else 0.dp,
                end = if (horizontalAlignment == Alignment.End) horizontalOutSidePadding else 0.dp,
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = bottomPaddingValue)
                .then(
                    if (defaultWindowInsetsPadding) {
                        Modifier
                            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Bottom))
                            .windowInsetsPadding(WindowInsets.captionBar.only(WindowInsetsSides.Bottom))
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (showDivider) {
                        Modifier
                            .background(
                                color = YubeixTheme.colorScheme.dividerLine,
                                shape = shape,
                            )
                            .padding(0.75.dp)
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (shadowElevation > 0.dp) {
                        Modifier.graphicsLayer(
                            shadowElevation = with(density) { shadowElevation.toPx() },
                            shape = shape,
                            clip = cornerRadius > 0.dp,
                        )
                    } else if (cornerRadius > 0.dp) {
                        Modifier.clip(shape)
                    } else {
                        Modifier
                    },
                )
                .background(color)
                .then(modifier)
                .padding(horizontal = FloatingNavigationBarDefaults.HorizontalPadding)
                .align(horizontalAlignment)
                .pointerInput(Unit) {
                    detectTapGestures { /* Consume click */ }
                },
            horizontalArrangement = Arrangement.spacedBy(FloatingNavigationBarDefaults.ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalFloatingNavigationBarDisplayMode provides mode) {
                content()
            }
        }
    }
}

/**
 * A [FloatingNavigationBarItem] that is suitable for [FloatingNavigationBar].
 *
 * @param selected Whether the item is selected.
 * @param onClick The callback when the item is clicked.
 * @param icon The icon of the item.
 * @param label The label of the item.
 * @param modifier The modifier to be applied to the [FloatingNavigationBarItem].
 * @param enabled Whether the item is enabled.
 */
@Composable
fun FloatingNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var isPressed by remember { mutableStateOf(false) }

    val onSurfaceContainerColor = YubeixTheme.colorScheme.onSurfaceContainer
    val tint = when {
        isPressed -> if (selected) {
            onSurfaceContainerColor.copy(alpha = FloatingNavigationBarDefaults.SelectedPressedAlpha)
        } else {
            onSurfaceContainerColor.copy(alpha = FloatingNavigationBarDefaults.UnselectedPressedAlpha)
        }

        selected -> onSurfaceContainerColor

        else -> onSurfaceContainerColor.copy(FloatingNavigationBarDefaults.UnselectedAlpha)
    }
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    val mode = LocalFloatingNavigationBarDisplayMode.current

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = { if (enabled) onClick() },
                )
            },
        horizontalAlignment = CenterHorizontally,
    ) {
        when (mode) {
            FloatingNavigationBarDisplayMode.IconAndText -> {
                Image(
                    modifier = Modifier.padding(top = FloatingNavigationBarDefaults.VerticalPadding).size(FloatingNavigationBarDefaults.IconSize),
                    imageVector = icon,
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(tint),
                )
                Box(
                    modifier = Modifier.padding(bottom = FloatingNavigationBarDefaults.VerticalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    // Invisible text for layout calculation (always bold)
                    Text(
                        modifier = Modifier.alpha(0f),
                        text = label,
                        textAlign = TextAlign.Center,
                        fontSize = FloatingNavigationBarDefaults.LABEL_FONT_SIZE,
                        fontWeight = FontWeight.Bold, // Always bold for layout
                    )
                    // Visible text
                    Text(
                        text = label,
                        color = tint,
                        textAlign = TextAlign.Center,
                        fontSize = FloatingNavigationBarDefaults.LABEL_FONT_SIZE,
                        fontWeight = fontWeight,
                    )
                }
            }

            FloatingNavigationBarDisplayMode.TextOnly -> {
                Box(
                    modifier = Modifier.padding(vertical = FloatingNavigationBarDefaults.TextVerticalPadding, horizontal = FloatingNavigationBarDefaults.TextHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    // Invisible text for layout calculation
                    Text(
                        modifier = Modifier.alpha(0f),
                        text = label,
                        textAlign = TextAlign.Center,
                        fontSize = FloatingNavigationBarDefaults.TextFontSize,
                        fontWeight = FontWeight.Bold, // Always bold for layout
                    )
                    // Visible text
                    Text(
                        text = label,
                        color = tint,
                        textAlign = TextAlign.Center,
                        fontSize = FloatingNavigationBarDefaults.TextFontSize,
                        fontWeight = fontWeight,
                    )
                }
            }

            FloatingNavigationBarDisplayMode.IconOnly -> {
                Image(
                    modifier = Modifier.padding(vertical = FloatingNavigationBarDefaults.IconOnlyPadding, horizontal = FloatingNavigationBarDefaults.IconOnlyPadding).size(FloatingNavigationBarDefaults.IconOnlySize),
                    imageVector = icon,
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(tint),
                )
            }
        }
    }
}

/** Contains default values used by [NavigationBar] and [NavigationBarItem]. */
object NavigationBarDefaults {

    /** The height of the iOS-style tab bar, above any bottom window inset. */
    val BAR_HEIGHT = 64.dp

    /** The vertical padding inside each item. */
    val ITEM_VERTICAL_PADDING = 7.dp

    /** The size of the item icon. */
    val CUPERTINO_ICON_SIZE = 28.dp

    /** The spacing between the item icon and its label. */
    val ICON_LABEL_SPACING = 2.dp

    /** The label font size, matching the iOS tab bar type scale. */
    val LABEL_FONT_SIZE = 10.sp

    /** The label font weight. */
    val LABEL_FONT_WEIGHT = FontWeight.Medium

    /** The label letter spacing, matching the iOS tab bar type scale. */
    val LABEL_LETTER_SPACING = (-0.24).sp

    /** The blur radius of the glass surface when a [HazeState] is provided. */
    val BLUR_RADIUS = 10.dp

    /** The alpha of the bar's translucent background when no blur is applied. */
    const val BACKGROUND_ALPHA = 0.6f

    /** The alpha of an unselected item's content color. */
    const val INACTIVE_ALPHA = 0.52f

    /** The scale an item shrinks to while pressed. */
    const val PRESSED_ITEM_SCALE = 0.92f

    /** The alpha value for the selected item when pressed. */
    val SelectedPressedAlpha = 0.5f

    /** The alpha value for an unselected item when pressed. */
    val UnselectedPressedAlpha = 0.6f

    /** The alpha value for an unselected item. */
    val UnselectedAlpha = 0.4f

    /** The default item height on non-iOS platforms. Kept for compatibility. */
    val ItemHeight = 64.dp

    /** The default item height on iOS. Kept for compatibility. */
    val ItemHeightIOS = 48.dp

    /** The icon size used before the cupertino tab bar visuals. Kept for compatibility. */
    val IconSize = 26.dp

    /** The default top padding for the icon. Kept for compatibility. */
    val IconTopPadding = 8.dp

    /** The default bottom padding for the label. Kept for compatibility. */
    val BottomPadding = 8.dp
}

/** Contains default values used by [FloatingNavigationBar] and [FloatingNavigationBarItem]. */
object FloatingNavigationBarDefaults {
    /** The default horizontal outside padding. */
    val HorizontalOutSidePadding = 36.dp

    /** The default shadow elevation. */
    val ShadowElevation = 1.dp

    /** The default horizontal padding inside the bar. */
    val HorizontalPadding = 12.dp

    /** The default spacing between items. */
    val ItemSpacing = 12.dp

    /** The icon size in [FloatingNavigationBarDisplayMode.IconAndText] mode. */
    val IconSize = 24.dp

    /** The label font size in [FloatingNavigationBarDisplayMode.IconAndText] mode. */
    val LABEL_FONT_SIZE = 12.sp

    /** The vertical padding in [FloatingNavigationBarDisplayMode.IconAndText] mode. */
    val VerticalPadding = 6.dp

    /** The vertical padding in [FloatingNavigationBarDisplayMode.TextOnly] mode. */
    val TextVerticalPadding = 16.dp

    /** The horizontal padding in [FloatingNavigationBarDisplayMode.TextOnly] mode. */
    val TextHorizontalPadding = 2.dp

    /** The font size in [FloatingNavigationBarDisplayMode.TextOnly] mode. */
    val TextFontSize = 14.sp

    /** The icon size in [FloatingNavigationBarDisplayMode.IconOnly] mode. */
    val IconOnlySize = 28.dp

    /** The padding in [FloatingNavigationBarDisplayMode.IconOnly] mode. */
    val IconOnlyPadding = 10.dp

    /** The alpha value for the selected item when pressed. */
    val SelectedPressedAlpha = 0.5f

    /** The alpha value for an unselected item when pressed. */
    val UnselectedPressedAlpha = 0.6f

    /** The alpha value for an unselected item. */
    val UnselectedAlpha = 0.4f
}

/**
 * Defines the display mode for items in a NavigationBar.
 *
 * This controls whether to show both icon and text, icon only, or text only.
 */
enum class NavigationBarDisplayMode {
    /** Show both icon and text. */
    IconAndText,

    /** Show icon only. */
    IconOnly,

    /** Show text only. */
    TextOnly,

    /** Show icon always, show text only when selected. */
    IconWithSelectedLabel,
}

/**
 * A composition local to control the display mode for items in a NavigationBar.
 */
val LocalNavigationBarDisplayMode = compositionLocalOf { NavigationBarDisplayMode.IconAndText }

/**
 * Defines the display mode for items in a [FloatingNavigationBar].
 *
 * This controls whether to show both icon and text, icon only, or text only.
 */
enum class FloatingNavigationBarDisplayMode {
    /** Show both icon and text. */
    IconAndText,

    /** Show icon only. */
    IconOnly,

    /** Show text only. */
    TextOnly,
}

/**
 * A composition local to control the display mode for items in a [FloatingNavigationBar].
 */
val LocalFloatingNavigationBarDisplayMode = compositionLocalOf { FloatingNavigationBarDisplayMode.IconOnly }

/**
 * The data class for [NavigationBar].
 *
 * @param label The label of the item.
 * @param icon The icon of the item.
 */
@Immutable
data class NavigationItem(
    val label: String,
    val icon: ImageVector,
)
