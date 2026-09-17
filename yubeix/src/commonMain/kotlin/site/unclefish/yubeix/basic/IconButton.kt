// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedCornerStyle
import com.kyant.shapes.RoundedRectangle
import site.unclefish.yubeix.interfaces.HoldDownInteraction
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape

/**
 * A [IconButton] component with Yubeix style.
 *
 * Icon buttons help people take supplementary actions with a single tap. They’re used when a
 * compact button is required, such as in a toolbar or image list.
 *
 * @param onClick The callback when the [IconButton] is clicked.
 * @param modifier The modifier to be applied to the [IconButton]
 * @param enabled Whether the [IconButton] is enabled.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param backgroundColor The background color of of the [IconButton].
 * @param cornerRadius The corner radius of of the [IconButton].
 * @param minHeight The minimum height of of the [IconButton].
 * @param minWidth The minimum width of the [IconButton].
 * @param content The content of this icon button, typically an [Icon].
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDownState: Boolean = false,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    content: @Composable () -> Unit,
) {
    val shape = yubeixShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val holdDown = remember { mutableStateOf<HoldDownInteraction.HoldDown?>(null) }

    LaunchedEffect(holdDownState) {
        if (holdDownState) {
            val interaction = HoldDownInteraction.HoldDown()
            holdDown.value = interaction
            interactionSource.emit(interaction)
        } else {
            holdDown.value?.let { oldValue ->
                interactionSource.emit(HoldDownInteraction.Release(oldValue))
                holdDown.value = null
            }
        }
    }

    val clickableModifier = remember(enabled, interactionSource, onClick) {
        if (enabled) {
            Modifier.clickable(
                role = Role.Button,
                interactionSource = interactionSource,
                onClick = onClick,
            )
        } else {
            Modifier
        }
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .clip(shape)
            .background(backgroundColor)
            .then(clickableModifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

object IconButtonDefaults {

    /**
     * The default minimum width of the [IconButton].
     */
    val MinWidth = 40.dp

    /**
     * The default minimum height of the [IconButton].
     */
    val MinHeight = 40.dp

    /**
     * The default corner radius of the [IconButton].
     */
    val CornerRadius = 40.dp
}

// region App chrome buttons
// Ported from the wordmoment app's chrome: the back chevron glyph, the flat capsule button, and
// the grouped flat-surface icon buttons used in hero titles and session screens.

private var backChevronCache: ImageVector? = null

/**
 * The chevron glyph behind [BackIcon]: a rounded caret pointing backwards, mirrored
 * automatically in right-to-left layouts.
 */
private val BackChevron: ImageVector
    get() {
        if (backChevronCache != null) return backChevronCache!!
        backChevronCache = ImageVector.Builder(
            name = "BackChevron",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            addPath(
                pathData = listOf(
                    PathNode.MoveTo(168.49f, 199.51f),
                    PathNode.RelativeArcTo(12f, 12f, 0f, false, true, -17f, 17f),
                    PathNode.RelativeLineTo(-80f, -80f),
                    PathNode.RelativeArcTo(12f, 12f, 0f, false, true, 0f, -17f),
                    PathNode.RelativeLineTo(80f, -80f),
                    PathNode.RelativeArcTo(12f, 12f, 0f, false, true, 17f, 17f),
                    PathNode.LineTo(97f, 128f),
                    PathNode.Close,
                ),
                fill = SolidColor(Color.Black),
            )
        }.build()
        return backChevronCache!!
    }

/**
 * The standard back chevron of the app chrome.
 *
 * @param modifier The [Modifier] to be applied to the icon.
 * @param size The side length of the icon.
 * @param tint The color of the chevron.
 * @param contentDescription Accessibility description; null when the icon is decorative (e.g.
 *   inside [TopBarBackButton], which carries the description on the button itself).
 */
@Composable
fun BackIcon(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    tint: Color = YubeixTheme.colorScheme.onSurface,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = BackChevron,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/**
 * A flat capsule button of the app chrome: a 42.dp tall capsule tinted with the theme background
 * color (near-opaque on dark surfaces) and an animated crossfade/slide text.
 *
 * @param text The label drawn inside the capsule.
 * @param onClick Invoked when the button is clicked.
 * @param modifier The [Modifier] to be applied to the button.
 */
@Composable
fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = YubeixTheme.colorScheme
    val isDarkSurface = colors.surface.luminance() < 0.5f
    val flatContainerColor = colors.background.copy(alpha = if (isDarkSurface) 0.9f else 0.84f)

    Box(
        modifier = modifier
            .height(42.dp)
            .background(flatContainerColor, Capsule())
            .clip(Capsule())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                val enter = fadeIn(animationSpec = tween(100)) + slideInVertically(
                    animationSpec = tween(115),
                ) { fullHeight -> fullHeight / 3 }
                val exit = fadeOut(animationSpec = tween(80)) + slideOutVertically(
                    animationSpec = tween(90),
                ) { fullHeight -> -fullHeight / 3 }
                enter togetherWith exit using SizeTransform(clip = false)
            },
            label = "CapsuleButtonText",
        ) { label ->
            Text(
                text = label,
                style = YubeixTheme.textStyles.footnote1,
                color = colors.onSurface,
            )
        }
    }
}

/**
 * A row of [IconButtonGroupItem]s sharing one flat capsule surface of the app chrome.
 *
 * @param modifier The [Modifier] to be applied to the group.
 * @param itemSize The side length of the group and of its square members.
 * @param itemSpacing Spacing between members.
 * @param showFlatSurface Whether the shared capsule background is drawn. Keep true for groups on
 *   raw page backgrounds; false when the group sits on a card that already provides the surface.
 * @param content The members, typically [IconButtonGroupItem]s.
 */
@Composable
fun IconButtonGroup(
    modifier: Modifier = Modifier,
    itemSize: Dp = 42.dp,
    itemSpacing: Dp = 0.dp,
    showFlatSurface: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = YubeixTheme.colorScheme
    val isDarkSurface = colors.surface.luminance() < 0.5f
    val flatContainerColor = colors.background.copy(alpha = if (isDarkSurface) 0.9f else 0.84f)
    val groupShape = remember(itemSize) {
        RoundedRectangle(
            cornerRadius = itemSize / 2f,
            style = RoundedCornerStyle.Continuous,
        )
    }

    Box(
        modifier = modifier
            .height(itemSize)
            .then(
                if (showFlatSurface) {
                    Modifier.background(flatContainerColor, groupShape)
                } else {
                    Modifier
                },
            )
            .clip(groupShape),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content,
        )
    }
}

/**
 * One tappable member of an [IconButtonGroup]: a square hit area with a capsule clip and the
 * standard ripple.
 *
 * @param onClick Invoked when the item is clicked.
 * @param modifier The [Modifier] to be applied to the item.
 * @param size The side length of the item.
 * @param contentDescription Accessibility description of the item.
 * @param icon The item's content, typically a [BackIcon]-style glyph or an [Icon].
 */
@Composable
fun IconButtonGroupItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    contentDescription: String? = null,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics {
                        this.contentDescription = contentDescription
                    }
                } else {
                    Modifier
                },
            )
            .clip(Capsule())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
// endregion
