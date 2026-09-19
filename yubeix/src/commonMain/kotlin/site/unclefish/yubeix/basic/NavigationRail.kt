// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape

/**
 * A [NavigationRail] for wide screens, styled after the iPadOS sidebar: a wide surface column
 * with an optional large title, whose entries are horizontal icon-and-label rows, the selected
 * one highlighted with a filled rounded pill in the theme accent.
 *
 * @param modifier The modifier to be applied to the [NavigationRail].
 * @param title The large title displayed at the top of the [NavigationRail], styled after the
 *   iPadOS sidebar's large title (e.g. "Files"); pass null for no title.
 * @param header The header of the [NavigationRail], usually a [FloatingActionButton] or a logo.
 * @param color The color of the [NavigationRail].
 * @param dividerColor The color of the divider line between the [NavigationRail] and the content;
 *   defaults to the theme divider color.
 * @param showDivider Whether to show the divider line between the [NavigationRail] and the content.
 * @param defaultWindowInsetsPadding whether to apply default window insets padding to the [NavigationRail].
 * @param minWidth The width of the [NavigationRail].
 * @param mode The mode for displaying items in the [NavigationRail]. It can show icons, text or both.
 * @param content The content of the [NavigationRail], usually [NavigationRailItem]s.
 */
@Composable
fun NavigationRail(
    modifier: Modifier = Modifier,
    title: String? = null,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    color: Color = YubeixTheme.colorScheme.background,
    dividerColor: Color? = null,
    showDivider: Boolean = true,
    defaultWindowInsetsPadding: Boolean = true,
    minWidth: Dp = NavigationRailDefaults.MinWidth,
    mode: NavigationRailDisplayMode = NavigationRailDisplayMode.IconAndText,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (defaultWindowInsetsPadding) {
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Vertical))
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Start))
                } else {
                    Modifier
                },
            )
            .background(color),
    ) {
        Column(
            modifier = Modifier
                .width(minWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = NavigationRailDefaults.ItemHorizontalPadding,
                    vertical = NavigationRailDefaults.VerticalPadding,
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            if (header != null) {
                header()
                Spacer(modifier = Modifier.height(NavigationRailDefaults.HeaderSpacing))
            }
            if (title != null) {
                Text(
                    text = title,
                    color = YubeixTheme.colorScheme.onSurface,
                    fontSize = NavigationRailDefaults.TitleFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = NavigationRailDefaults.ItemContentHorizontalPadding),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(NavigationRailDefaults.TitleSpacing))
            }
            CompositionLocalProvider(LocalNavigationRailDisplayMode provides mode) {
                content()
            }
        }
        if (showDivider) {
            VerticalDivider(color = dividerColor ?: DividerDefaults.DividerColor)
        }
    }
}

/**
 * A [NavigationRailItem] that is suitable for [NavigationRail], styled after the iPadOS sidebar
 * row: a leading icon tinted with the theme accent and a regular-weight label, or, when selected,
 * a filled rounded pill in the accent with both icon and label in the on-accent color. The pill
 * and the content colors crossfade in sync, and rows carry the composition's ripple indication
 * while pressed.
 *
 * @param selected Whether the item is selected.
 * @param onClick The callback when the item is clicked.
 * @param icon The icon of the item.
 * @param label The label of the item.
 * @param modifier The modifier to be applied to the [NavigationRailItem].
 * @param enabled Whether the item is enabled.
 */
@Composable
fun NavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colorScheme = YubeixTheme.colorScheme
    val itemShape = yubeixShape(NavigationRailDefaults.ItemCornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnClick by rememberUpdatedState(onClick)
    val currentEnabled by rememberUpdatedState(enabled)

    // One shared progress drives BOTH the pill and the content colors. The pill must be painted
    // as primary.copy(alpha = progress), never lerped from Color.Transparent: Transparent is
    // zero-alpha BLACK, and a channel-wise lerp from it yields a color whose effective opacity
    // is roughly progress-squared — visually near-invisible for the first half while the label
    // is already lightening, which reads as white text floating on the bare background. With a
    // true-blue alpha fade and the label lerp on the same progress, the two are always in phase.
    val selectionProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = NavigationRailDefaults.SelectionAnimationDurationMillis),
        label = "navigationRailSelectionProgress",
    )
    val backgroundColor = when {
        selected -> colorScheme.primary.copy(alpha = selectionProgress)
        pressed -> colorScheme.onSurface.copy(alpha = NavigationRailDefaults.UnselectedPressedBackgroundAlpha)
        else -> colorScheme.primary.copy(alpha = selectionProgress)
    }
    val iconTint = lerp(colorScheme.primary, colorScheme.onPrimary, selectionProgress)
    val labelColor = lerp(colorScheme.onSurface, colorScheme.onPrimary, selectionProgress)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = NavigationRailDefaults.ItemSpacing)
            .clip(itemShape)
            .background(backgroundColor, itemShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Tab,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    currentOnClick()
                },
            )
            .semantics { this.selected = selected }
            .animateContentSize()
            .padding(
                horizontal = NavigationRailDefaults.ItemContentHorizontalPadding,
                vertical = NavigationRailDefaults.ItemContentVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val mode = LocalNavigationRailDisplayMode.current
        when (mode) {
            NavigationRailDisplayMode.IconAndText -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(NavigationRailDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(NavigationRailDefaults.IconTextSpacing))
                Text(
                    text = label,
                    color = labelColor,
                    fontSize = NavigationRailDefaults.LabelFontSize,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
            }

            NavigationRailDisplayMode.IconWithSelectedLabel -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(NavigationRailDefaults.IconSize),
                )
                if (selected) {
                    Spacer(modifier = Modifier.width(NavigationRailDefaults.IconTextSpacing))
                    Text(
                        text = label,
                        color = labelColor,
                        fontSize = NavigationRailDefaults.LabelFontSize,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }

            NavigationRailDisplayMode.TextOnly -> {
                Text(
                    text = label,
                    color = labelColor,
                    fontSize = NavigationRailDefaults.LabelFontSize,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
            }

            NavigationRailDisplayMode.IconOnly -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(NavigationRailDefaults.IconSize),
                )
            }
        }
    }
}

/** Contains default values used by [NavigationRail] and [NavigationRailItem]. */
object NavigationRailDefaults {
    /** The default width of the [NavigationRail], matching the iPadOS sidebar. */
    val MinWidth = 250.dp

    /** The default vertical padding of the [NavigationRail] content. */
    val VerticalPadding = 8.dp

    /** The default spacing after the header. */
    val HeaderSpacing = 12.dp

    /** The font size of the large title, matching the iOS large title type scale. */
    val TitleFontSize = 28.sp

    /** The spacing between the large title and the items. */
    val TitleSpacing = 16.dp

    /** The horizontal inset of the items from the [NavigationRail] edges. */
    val ItemHorizontalPadding = 10.dp

    /** The horizontal padding inside an item's highlight pill. */
    val ItemContentHorizontalPadding = 8.dp

    /** The vertical padding inside an item's highlight pill. */
    val ItemContentVerticalPadding = 7.dp

    /** The vertical gap between items. */
    val ItemSpacing = 2.dp

    /** The corner radius of an item's highlight pill. */
    val ItemCornerRadius = 8.dp

    /** The default icon size. */
    val IconSize = 26.dp

    /** The default spacing between icon and text. */
    val IconTextSpacing = 10.dp

    /** The default label font size. */
    val LabelFontSize = 16.sp

    /** The alpha of the pressed unselected item's highlight. */
    val UnselectedPressedBackgroundAlpha = 0.08f

    /** The duration of the selection pill and content color transition, in milliseconds. */
    const val SelectionAnimationDurationMillis = 150
}

/**
 * Defines the display mode for items in a [NavigationRail].
 *
 * This controls whether to show both icon and text, icon only, text only,
 * or icon with text only when selected.
 */
enum class NavigationRailDisplayMode {
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
 * A composition local to control the display mode for items in a [NavigationRail].
 */
val LocalNavigationRailDisplayMode = compositionLocalOf { NavigationRailDisplayMode.IconAndText }
