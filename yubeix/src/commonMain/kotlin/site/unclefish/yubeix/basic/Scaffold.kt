// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.zIndex
import com.kyant.shapes.Capsule
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import site.unclefish.yubeix.blur.isRenderEffectSupported
import site.unclefish.yubeix.component.OverscrollTitle
import site.unclefish.yubeix.component.overscrollTitleTextStyle
import site.unclefish.yubeix.component.paddingItem
import site.unclefish.yubeix.extra.SuperDialog
import site.unclefish.yubeix.extra.SuperDropdown
import site.unclefish.yubeix.extra.WindowListPopup
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.Checkmark
import site.unclefish.yubeix.icon.cupertino.outlined.Ellipsis
import site.unclefish.yubeix.theme.LocalReducedDynamicEffectsEnabled
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.utils.LocalDialogStates
import site.unclefish.yubeix.utils.LocalPopupStates
import site.unclefish.yubeix.utils.LocalRootDialogStates
import site.unclefish.yubeix.utils.LocalRootPopupStates
import site.unclefish.yubeix.utils.YubeixPopupUtils
import site.unclefish.yubeix.utils.YubeixPopupUtils.Companion.YubeixPopupHost

/**
 * A [Scaffold] component with Yubeix style.
 *
 * This implements the basic Yubeix design visual layout structure.
 *
 * To show a [Snackbar], use [SnackbarHostState.showSnackbar].
 *
 * @param modifier the [Modifier] to be applied to this scaffold.
 * @param topBar top app bar of the screen.
 * @param bottomBar bottom bar of the screen.
 * @param floatingActionButton floating action button of the screen.
 * @param floatingActionButtonPosition position of the floating action button.
 * @param floatingToolbar floating toolbar of the screen.
 * @param floatingToolbarPosition position of the floating toolbar.
 * @param snackbarHost component to host [Snackbar]s that are pushed to be shown via
 *   [SnackbarHostState.showSnackbar], typically a [SnackbarHost].
 * @param popupHost component to host [SuperDropdown]s & [SuperDialog]s that are pushed to
 * be show, typically a [YubeixPopupHost].
 * @param containerColor the color used for the background of this scaffold. Defaults to the
 *   theme's grouped page background, matching [ScreenScaffold]'s visual language. Use
 *   [Color.Transparent] to have no color.
 * @param contentWindowInsets window insets to be passed to [content] slot via [PaddingValues]
 *   params. Scaffold will take the insets into account from the top/bottom only if the [topBar]/
 *   [bottomBar] are not present, as the scaffold expect [topBar]/[bottomBar] to handle insets
 *   instead. Any insets consumed by other insets padding modifiers or [consumeWindowInsets] on a
 *   parent layout will be excluded from [contentWindowInsets].
 * @param content content of the screen. The lambda receives a [PaddingValues] that should be
 *   applied to the content root via [Modifier.padding] and [Modifier.consumeWindowInsets] to
 *   properly offset top and bottom bars. If using [Modifier.verticalScroll], apply this modifier to
 *   the child of the scroll, and not on the scroll itself.
 */
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    floatingToolbar: @Composable () -> Unit = {},
    floatingToolbarPosition: ToolbarPosition = ToolbarPosition.BottomCenter,
    snackbarHost: @Composable () -> Unit = {},
    popupHost: @Composable () -> Unit = { YubeixPopupHost() },
    containerColor: Color = YubeixTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    content: @Composable (PaddingValues) -> Unit,
) {
    val safeInsets = remember(contentWindowInsets) { MutableWindowInsets(contentWindowInsets) }
    val popupStates = remember { mutableStateListOf<YubeixPopupUtils.PopupState>() }
    val dialogStates = remember { mutableStateListOf<YubeixPopupUtils.DialogState>() }
    val parentRootDialogStates = LocalRootDialogStates.current
    val rootDialogStates = parentRootDialogStates ?: dialogStates
    val parentRootPopupStates = LocalRootPopupStates.current
    val rootPopupStates = parentRootPopupStates ?: popupStates
    Surface(
        modifier = modifier.onConsumedWindowInsetsChanged { consumedWindowInsets ->
            // Exclude currently consumed window insets from user provided contentWindowInsets
            safeInsets.insets = contentWindowInsets.exclude(consumedWindowInsets)
        },
        color = containerColor,
    ) {
        CompositionLocalProvider(
            LocalPopupStates provides popupStates,
            LocalDialogStates provides dialogStates,
            LocalRootDialogStates provides rootDialogStates,
            LocalRootPopupStates provides rootPopupStates,
        ) {
            ScaffoldLayout(
                topBar = topBar,
                bottomBar = bottomBar,
                content = content,
                snackbar = snackbarHost,
                floatingActionButton = floatingActionButton,
                floatingActionButtonPosition = floatingActionButtonPosition,
                floatingToolbar = floatingToolbar,
                floatingToolbarPosition = floatingToolbarPosition,
                popup = popupHost,
                contentWindowInsets = safeInsets,
            )
        }
    }
}

/**
 * Layout for a [Scaffold]'s content.
 *
 * @param topBar the content to place at the top of the [Scaffold], typically a [TopAppBar]
 * @param snackbar the [Snackbar] displayed on top of the [content].
 * @param bottomBar the content to place at the bottom of the [Scaffold], on top of the [content],
 *   typically a [NavigationBar].
 * @param floatingActionButton the [FloatingActionButton] displayed on top of the [content], below the [snackbar] and
 *   above the [NavigationBar]
 * @param floatingActionButtonPosition [FabPosition] for the FAB (if present).
 * @param floatingToolbar the [FloatingToolbar] displayed on top of the [content].
 * @param floatingToolbarPosition [ToolbarPosition] for the floating toolbar (if present).
 * @param popup the [YubeixPopupHost] displayed on top of the [content].
 * @param content the main 'body' of the [Scaffold].
 * @param contentWindowInsets the [WindowInsets] to apply to the [content].
 */
@Composable
private fun ScaffoldLayout(
    topBar: @Composable () -> Unit,
    snackbar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    floatingActionButtonPosition: FabPosition,
    floatingToolbar: @Composable () -> Unit,
    floatingToolbarPosition: ToolbarPosition,
    popup: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    // Create the backing value for the content padding
    // These values will be updated during measurement, but before subcomposing the body content
    // Remembering and updating a single PaddingValues avoids needing to recompose when the values
    // change
    val contentPadding = remember {
        object : PaddingValues {
            var paddingHolder by mutableStateOf(PaddingValues(0.dp))

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp = paddingHolder.calculateLeftPadding(layoutDirection)

            override fun calculateTopPadding(): Dp = paddingHolder.calculateTopPadding()

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp = paddingHolder.calculateRightPadding(layoutDirection)

            override fun calculateBottomPadding(): Dp = paddingHolder.calculateBottomPadding()
        }
    }
    val popupContent: @Composable () -> Unit = remember(popup) { { Box { popup() } } }
    val topBarContent: @Composable () -> Unit = remember(topBar) { { Box { topBar() } } }
    val snackbarContent: @Composable () -> Unit = remember(snackbar) { { Box { snackbar() } } }
    val floatingActionButtonContent: @Composable () -> Unit =
        remember(floatingActionButton) { { Box { floatingActionButton() } } }
    val floatingToolbarContent: @Composable () -> Unit = remember(floatingToolbar) { { Box { floatingToolbar() } } }
    val bodyContent: @Composable () -> Unit = remember(content, contentPadding) { { Box { content(contentPadding) } } }
    val bottomBarContent: @Composable () -> Unit = remember(bottomBar) { { Box { bottomBar() } } }
    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val topInset = contentWindowInsets.getTop(this@SubcomposeLayout)
        val leftInset = contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection)
        val rightInset = contentWindowInsets.getRight(this@SubcomposeLayout, layoutDirection)
        val bottomInset = contentWindowInsets.getBottom(this@SubcomposeLayout)

        // Measure Popups first (highest z-index)
        val popupPlaceables =
            subcompose(ScaffoldLayoutContent.Popup, popupContent)
                .first()
                .measure(looseConstraints)

        // Measure TopBar
        val topBarPlaceable =
            subcompose(ScaffoldLayoutContent.TopBar, topBarContent)
                .first()
                .measure(looseConstraints)

        // Measure Snackbar
        val snackbarPlaceable =
            subcompose(ScaffoldLayoutContent.Snackbar, snackbarContent)
                .first()
                .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))

        // Measure FAB
        val fabPlaceable =
            subcompose(ScaffoldLayoutContent.Fab, floatingActionButtonContent)
                .first()
                .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))
        val isFabEmpty = fabPlaceable.width == 0 && fabPlaceable.height == 0
        val fabPlacement = if (!isFabEmpty) {
            val fabWidth = fabPlaceable.width
            val fabHeight = fabPlaceable.height
            // FAB distance from the left of the layout, taking into account LTR / RTL
            val fabLeftOffset =
                when (floatingActionButtonPosition) {
                    FabPosition.Start -> {
                        if (layoutDirection == LayoutDirection.Ltr) {
                            FabSpacing.roundToPx() + leftInset
                        } else {
                            layoutWidth - FabSpacing.roundToPx() - fabWidth - rightInset
                        }
                    }

                    FabPosition.End,
                    FabPosition.EndOverlay,
                    -> {
                        if (layoutDirection == LayoutDirection.Ltr) {
                            layoutWidth - FabSpacing.roundToPx() - fabWidth - rightInset
                        } else {
                            FabSpacing.roundToPx() + leftInset
                        }
                    }

                    else -> (layoutWidth - fabWidth + leftInset - rightInset) / 2
                }

            FabPlacement(left = fabLeftOffset, width = fabWidth, height = fabHeight)
        } else {
            null
        }

        // Measure BottomBar
        val bottomBarPlaceable =
            subcompose(ScaffoldLayoutContent.BottomBar, bottomBarContent)
                .first()
                .measure(looseConstraints)
        val isBottomBarEmpty = bottomBarPlaceable.width == 0 && bottomBarPlaceable.height == 0
        val fabOffsetFromBottom = fabPlacement?.let {
            if (isBottomBarEmpty || floatingActionButtonPosition == FabPosition.EndOverlay) {
                it.height + FabSpacing.roundToPx() + contentWindowInsets.getBottom(this@SubcomposeLayout)
            } else {
                // Total height is the bottom bar height + the FAB height + the padding
                // between the FAB and bottom bar
                bottomBarPlaceable.height + it.height + FabSpacing.roundToPx()
            }
        }

        val snackbarHeight = snackbarPlaceable.height
        val snackbarOffsetFromBottom =
            if (snackbarHeight != 0) {
                snackbarHeight +
                    (
                        fabOffsetFromBottom
                            ?: bottomBarPlaceable.height.takeIf { !isBottomBarEmpty }
                            ?: contentWindowInsets.getBottom(this@SubcomposeLayout)
                        )
            } else {
                0
            }

        // Measure FloatingToolbar
        val floatingToolbarPlaceable =
            subcompose(ScaffoldLayoutContent.FloatingToolbar, floatingToolbarContent)
                .first()
                .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))

        val isFloatingToolbarEmpty = floatingToolbarPlaceable.width == 0 && floatingToolbarPlaceable.height == 0

        // Update the backing state for the content padding before subcomposing the body
        val insets = contentWindowInsets.asPaddingValues(this)
        contentPadding.paddingHolder =
            PaddingValues(
                top =
                if (topBarPlaceable.width == 0 && topBarPlaceable.height == 0) {
                    insets.calculateTopPadding()
                } else {
                    topBarPlaceable.height.toDp()
                },
                bottom =
                if (isBottomBarEmpty) {
                    insets.calculateBottomPadding()
                } else {
                    bottomBarPlaceable.height.toDp()
                },
                start = insets.calculateStartPadding(layoutDirection),
                end = insets.calculateEndPadding(layoutDirection),
            )

        // Measure Main Content
        val bodyContentPlaceable =
            subcompose(ScaffoldLayoutContent.MainContent, bodyContent)
                .first()
                .measure(looseConstraints)

        layout(layoutWidth, layoutHeight) {
            // Placing to control drawing order to match default elevation of each placeable
            bodyContentPlaceable.place(0, 0)
            // Place TopBar
            topBarPlaceable.place(0, 0)
            // Place Snackbar
            snackbarPlaceable.place(
                (
                    layoutWidth - snackbarPlaceable.width +
                        contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection) -
                        contentWindowInsets.getRight(this@SubcomposeLayout, layoutDirection)
                    ) / 2,
                layoutHeight - snackbarOffsetFromBottom,
            )
            // Place BottomBar
            bottomBarPlaceable.place(0, layoutHeight - bottomBarPlaceable.height)
            // Place FloatingToolbar
            if (!isFloatingToolbarEmpty) {
                val floatingToolbarWidth = floatingToolbarPlaceable.width
                val floatingToolbarHeight = floatingToolbarPlaceable.height

                val alignment = floatingToolbarPosition.toAlignment()

                val availableWidth = layoutWidth - leftInset - rightInset
                val availableHeight = layoutHeight - topBarPlaceable.height - topInset - bottomInset

                val position = alignment.align(
                    IntSize(floatingToolbarWidth, floatingToolbarHeight),
                    IntSize(availableWidth, availableHeight),
                    layoutDirection,
                )

                val x = leftInset + position.x
                val y = topBarPlaceable.height + topInset + position.y - FloatingToolbarSpacing.roundToPx()

                floatingToolbarPlaceable.place(x, y)
            }
            // Place FAB
            fabPlacement?.let { placement ->
                fabPlaceable.place(placement.left, layoutHeight - fabOffsetFromBottom!!)
            }
            // Place Popup
            popupPlaceables.place(0, 0)
        }
    }
}

private enum class ScaffoldLayoutContent {
    TopBar,
    BottomBar,
    Snackbar,
    FloatingToolbar,
    Fab,
    Popup,
    MainContent,
}

/**
 * A [WindowInsets] whose values can change without changing the instance. This is useful to avoid
 * recomposition when [WindowInsets] can change.
 *
 * Copied from [androidx.compose.foundation.layout.MutableWindowInsets], which is marked as
 * experimental and thus cannot be used cross-module.
 */
internal class MutableWindowInsets(initialInsets: WindowInsets = WindowInsets(0, 0, 0, 0)) : WindowInsets {
    /**
     * The [WindowInsets] that are used for [left][getLeft], [top][getTop], [right][getRight], and
     * [bottom][getBottom] values.
     */
    var insets by mutableStateOf(initialInsets)

    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = insets.getLeft(density, layoutDirection)

    override fun getTop(density: Density): Int = insets.getTop(density)

    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = insets.getRight(density, layoutDirection)

    override fun getBottom(density: Density): Int = insets.getBottom(density)
}

@kotlin.jvm.JvmInline
value class FabPosition internal constructor(@Suppress("unused") private val value: Int) {
    companion object {
        /**
         * Position FAB at the bottom of the screen at the start, above the [NavigationBar] (if it
         * exists)
         */
        val Start = FabPosition(0)

        /**
         * Position FAB at the bottom of the screen in the center, above the [NavigationBar] (if it
         * exists)
         */
        val Center = FabPosition(1)

        /**
         * Position FAB at the bottom of the screen at the end, above the [NavigationBar] (if it
         * exists)
         */
        val End = FabPosition(2)

        /**
         * Position FAB at the bottom of the screen at the end, overlaying the [NavigationBar] (if
         * it exists)
         */
        val EndOverlay = FabPosition(3)
    }

    override fun toString(): String = when (this) {
        Start -> "FabPosition.Start"
        Center -> "FabPosition.Center"
        End -> "FabPosition.End"
        else -> "FabPosition.EndOverlay"
    }
}

/**
 * Placement information for a [FloatingActionButton] inside a [Scaffold].
 *
 * @property left the FAB's offset from the left edge of the bottom bar, already adjusted for RTL
 *   support
 * @property width the width of the FAB
 * @property height the height of the FAB
 */
@Immutable
internal class FabPlacement(val left: Int, val width: Int, val height: Int)

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 12.dp

// FloatingToolbar spacing above the bottom of the Scaffold
private val FloatingToolbarSpacing = 4.dp

// Add Alignment.vertical property helper if not available
internal val Alignment.vertical: Alignment.Vertical
    get() = when (this) {
        Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> Alignment.Top
        Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd -> Alignment.CenterVertically
        Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> Alignment.Bottom
        else -> Alignment.CenterVertically // Default or throw error
    }

// Add Alignment.horizontal property helper if not available
internal val Alignment.horizontal: Alignment.Horizontal
    get() = when (this) {
        Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> Alignment.Start
        Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> Alignment.CenterHorizontally
        Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> Alignment.End
        else -> Alignment.CenterHorizontally // Default or throw error
    }

// Keep the internal toAlignment function for ToolbarPosition here
internal fun ToolbarPosition.toAlignment(): Alignment = when (this) {
    ToolbarPosition.TopStart -> Alignment.TopStart

    ToolbarPosition.CenterStart -> Alignment.CenterStart

    ToolbarPosition.BottomStart -> Alignment.BottomStart

    ToolbarPosition.TopEnd -> Alignment.TopEnd

    ToolbarPosition.CenterEnd -> Alignment.CenterEnd

    ToolbarPosition.BottomEnd -> Alignment.BottomEnd

    ToolbarPosition.TopCenter -> Alignment.TopCenter

    // Added
    ToolbarPosition.BottomCenter -> Alignment.BottomCenter

    else -> Alignment.BottomCenter // Default or throw error
}

/**
 * Represents the position of a floating toolbar within the Scaffold.
 * Used by Scaffold for placement calculations.
 */
@kotlin.jvm.JvmInline
value class ToolbarPosition internal constructor(@Suppress("unused") private val value: Int) {
    companion object {
        /** Position Toolbar at the top start corner. */
        val TopStart = ToolbarPosition(0)

        /** Position Toolbar vertically centered on the start edge. */
        val CenterStart = ToolbarPosition(1)

        /** Position Toolbar at the bottom start corner. */
        val BottomStart = ToolbarPosition(2)

        /** Position Toolbar at the top end corner. */
        val TopEnd = ToolbarPosition(3)

        /** Position Toolbar vertically centered on the end edge. */
        val CenterEnd = ToolbarPosition(4)

        /** Position Toolbar at the bottom end corner. */
        val BottomEnd = ToolbarPosition(5)

        /** Position Toolbar horizontally centered along the top edge. */
        // Added KDoc
        val TopCenter = ToolbarPosition(6)

        /** Position Toolbar horizontally centered along the bottom edge. */
        val BottomCenter = ToolbarPosition(7)
    }

    override fun toString(): String = when (this) {
        TopStart -> "ToolbarPosition.TopStart"
        CenterStart -> "ToolbarPosition.CenterStart"
        BottomStart -> "ToolbarPosition.BottomStart"
        TopEnd -> "ToolbarPosition.TopEnd"
        CenterEnd -> "ToolbarPosition.CenterEnd"
        BottomEnd -> "ToolbarPosition.BottomEnd"
        TopCenter -> "ToolbarPosition.TopCenter"
        else -> "ToolbarPosition.BottomCenter"
    }
}

// region Screen scaffold with app chrome top bar
// Ported from the wordmoment app's screen chrome: a Scaffold paired with the large-title chrome
// ([LargeTopAppBar]) - hero title in the scroll content, collapsed centered title fading in on
// top, spec-driven actions with an overflow menu.

/**
 * When true, full-screen scaffolds are hosted inside a window that already clears the status bar
 * (e.g. an onboarding bottom sheet), so they must not add the status-bar inset again.
 */
val LocalSuppressStatusBarInset = staticCompositionLocalOf { false }

/**
 * Defines the large and collapsed title behavior as one page-level setting.
 *
 * @property showsHeroTitle Whether the hero (large) title is rendered as the first list item.
 * @property pinsCollapsedTitle Whether the collapsed centered title is always visible.
 */
enum class ScreenTitleMode(
    internal val showsHeroTitle: Boolean,
    internal val pinsCollapsedTitle: Boolean,
) {
    /** Hero title in the content; collapsed title appears only while scrolled. */
    Hero(showsHeroTitle = true, pinsCollapsedTitle = false),

    /** No hero title; collapsed title is pinned visible. */
    Pinned(showsHeroTitle = false, pinsCollapsedTitle = true),

    /** No hero title; collapsed title appears once the content scrolls. */
    ScrollAware(showsHeroTitle = false, pinsCollapsedTitle = false),
}

/** The kind of visual a [TopBarActionSpec] renders as. */
enum class TopBarActionKind {
    /** A square icon button. */
    Icon,

    /** A capsule with a text label. */
    Capsule,
}

/**
 * Declarative description of one top bar action shown by [ScreenScaffold].
 *
 * The first [ScreenScaffoldDefaults.MaxVisibleActions] actions render as buttons; the rest fold
 * into the overflow menu (together with [ScreenScaffold.menuItems]) behind an ellipsis button.
 *
 * @param key Stable identity used for item recomposition and menu keys.
 * @param contentDescription Accessibility description; also the menu title when [label] is blank.
 * @param onClick Invoked when the action is activated.
 * @param icon The icon shown in the icon-button form.
 * @param enabled Whether the action is interactive.
 * @param alpha Visual and semantic fade applied to the whole action.
 * @param kind Whether the action renders as an icon button or a labeled capsule.
 * @param label The capsule text and the overflow menu title; falls back to [contentDescription].
 * @param popupContent Additional content (e.g. a popup) rendered in the action's layout slot.
 * @param customContent Fully custom rendering replacing the icon/capsule forms.
 */
data class TopBarActionSpec(
    val key: Any,
    val contentDescription: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val alpha: Float = 1f,
    val kind: TopBarActionKind = TopBarActionKind.Icon,
    val label: String? = null,
    val popupContent: (@Composable () -> Unit)? = null,
    val customContent: (@Composable (titleColor: Color, actionSize: Dp) -> Unit)? = null,
)

/**
 * Declarative description of one entry in the top bar overflow menu of [ScreenScaffold].
 *
 * @property key Stable identity for the item.
 * @property title The item's title.
 * @property supportingText Optional smaller line under the title.
 * @property enabled Whether the item is interactive.
 * @property leadingContent Optional leading slot receiving the reveal progress (0..1).
 */
sealed interface TopBarMenuItemSpec {
    val key: Any
    val title: String
    val supportingText: String?
    val enabled: Boolean
    val leadingContent: (@Composable (Float) -> Unit)?

    /** A menu item that performs an action when clicked. */
    data class Action(
        override val key: Any,
        override val title: String,
        val onClick: () -> Unit,
        override val supportingText: String? = null,
        override val enabled: Boolean = true,
        override val leadingContent: (@Composable (Float) -> Unit)? = null,
    ) : TopBarMenuItemSpec

    /** A menu item that toggles a boolean, showing a checkmark while checked. */
    data class Toggle(
        override val key: Any,
        override val title: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        override val supportingText: String? = null,
        override val enabled: Boolean = true,
        override val leadingContent: (@Composable (Float) -> Unit)? = null,
    ) : TopBarMenuItemSpec

    /** A menu item representing one option, showing a checkmark while selected. */
    data class Choice(
        override val key: Any,
        override val title: String,
        val selected: Boolean,
        val onSelect: () -> Unit,
        override val supportingText: String? = null,
        override val enabled: Boolean = true,
        override val leadingContent: (@Composable (Float) -> Unit)? = null,
    ) : TopBarMenuItemSpec
}

private val DefaultScreenHeroTitlePadding = PaddingValues(
    start = 12.dp,
    end = 16.dp,
    top = 4.dp,
    bottom = 4.dp,
)

private const val OVERFLOW_MENU_CONTENT_DESCRIPTION = "More"

private val TopBarMenuIconSize = 18.dp

/** Contains default values used by [ScreenScaffold]. */
object ScreenScaffoldDefaults {

    /** The visual height of the chrome top bar, excluding the status bar inset. */
    val TopBarVisualHeight = 58.dp

    /** The maximum number of actions rendered as buttons before they overflow into the menu. */
    internal val MaxVisibleActions = 2

    /** The spacing between top bar action buttons. */
    internal val TopBarActionSpacing = 6.dp
}

/**
 * A full-screen scaffold with the app chrome top bar built in: a [Scaffold] whose content hosts a
 * large-title hero (or custom body), with [LargeTopAppBar] layered on top, spec-driven
 * [actions] and a [WindowListPopup] overflow [menuItems] menu.
 *
 * The collapse is position-driven: the hero title is tracked in window coordinates and the
 * collapsed centered title (plus the bar background) fades in once the hero title scrolls under
 * the status bar, with hysteresis from [collapseTriggerOffset].
 *
 * Note: unlike the app this was extracted from, no scroll indicator is drawn over the content.
 *
 * @param title The screen title: the hero title in [ScreenTitleMode.Hero], and the collapsed
 *   centered title otherwise.
 * @param onBack Invoked by the top bar's back button; null for root screens.
 * @param subtitle Optional smaller line under the collapsed centered title.
 * @param modifier The [Modifier] applied to the scaffold's content container.
 * @param actions Top bar actions; overflow folds into the menu past
 *   [ScreenScaffoldDefaults.MaxVisibleActions].
 * @param menuItems Entries always shown in the top bar overflow menu.
 * @param topBarStartContent Start-edge content of the top bar, replacing the back button.
 * @param topBarCenterContent Center content of the top bar, replacing title and subtitle.
 * @param topBarMaxVisibleActions Overrides how many [actions] render as buttons.
 * @param scaffoldContainerColor The color passed to the underlying [Scaffold]. Transparent by
 *   default so [backgroundColor] controls the page.
 * @param backgroundColor The page background drawn behind the content.
 * @param topBarVisualHeight The chrome bar height, excluding the status bar inset.
 * @param topBarContentGap Gap between the chrome bar's bottom edge and the content's top padding.
 * @param contentTopPadding Overrides the content's top padding; defaults to the chrome bar height
 *   plus [topBarContentGap].
 * @param collapseTriggerOffset Collapse hysteresis distance for the collapsed title.
 * @param contentMaxWidth Optional max width centering the content (e.g. tablet layouts).
 * @param topBarMaxWidth Optional max width centering the chrome bar; defaults to [contentMaxWidth].
 * @param horizontalContentPadding Horizontal content padding.
 * @param bottomContentPadding Bottom content padding, before the scaffold's bottom inset.
 * @param itemSpacing Vertical spacing between list items.
 * @param heroTitlePadding Padding around the hero title.
 * @param titleMode The large/collapsed title behavior; see [ScreenTitleMode].
 * @param heroTitleOverscrollScale Whether the hero title scales during overscroll. Centered
 *   heroes should opt out: the overscroll grow anchors at the title's top-left, so it drags a
 *   centered title sideways as it scales.
 * @param listState Optional hoisted list state; one is allocated when null.
 * @param minimumScrollDistance Extra scroll distance appended by the trailing filler item.
 * @param showTrailingFiller When false, the trailing viewport-filling spacer is omitted. Turning
 *   it off matters for paged content (e.g. a HorizontalPager sized to the focused page): the
 *   filler would otherwise pad short pages up to viewport+1px, which reads as a near-full
 *   scrollbar thumb and a dead scrollable blank at the bottom.
 * @param titleColor The top bar content color.
 * @param hazeState Optional haze state; when set (and render effects are supported) the content
 *   is marked as the blur source and the top bar frosts it while collapsed.
 * @param topBarCollapseProgress Overrides the position-driven collapse with an explicit value.
 * @param topBarContentVisibilityProgress Alpha of the top bar's foreground content.
 * @param topBarContentOffsetY Vertical translation of the top bar's foreground content.
 * @param topBarTitleClickEnabled Whether the collapsed title is clickable.
 * @param onTitleClick Invoked on collapsed title clicks; defaults to scrolling the list to top.
 * @param heroTitle The hero title content; receives the modifier to apply to it.
 * @param floatingBottomContent Content overlaid above the bottom edge (FABs, snackbars, bars).
 * @param bodyContent Custom body replacing the built-in lazy list; receives the content modifier.
 * @param content List items of the built-in lazy list, below the hero title.
 */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: List<TopBarActionSpec> = emptyList(),
    menuItems: List<TopBarMenuItemSpec> = emptyList(),
    topBarStartContent: (@Composable () -> Unit)? = null,
    topBarCenterContent: (@Composable () -> Unit)? = null,
    topBarMaxVisibleActions: Int = ScreenScaffoldDefaults.MaxVisibleActions,
    scaffoldContainerColor: Color = YubeixTheme.colorScheme.background,
    backgroundColor: Color = scaffoldContainerColor,
    topBarVisualHeight: Dp = ScreenScaffoldDefaults.TopBarVisualHeight,
    topBarContentGap: Dp = 8.dp,
    contentTopPadding: Dp? = null,
    collapseTriggerOffset: Dp = 12.dp,
    contentMaxWidth: Dp? = null,
    topBarMaxWidth: Dp? = contentMaxWidth,
    horizontalContentPadding: Dp = 16.dp,
    bottomContentPadding: Dp = 32.dp,
    itemSpacing: Dp = 8.dp,
    heroTitlePadding: PaddingValues = DefaultScreenHeroTitlePadding,
    titleMode: ScreenTitleMode = ScreenTitleMode.Hero,
    heroTitleOverscrollScale: Boolean = true,
    listState: LazyListState? = null,
    minimumScrollDistance: Dp = 0.dp,
    showTrailingFiller: Boolean = true,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    hazeState: HazeState? = null,
    topBarCollapseProgress: Float? = null,
    topBarContentVisibilityProgress: Float = 1f,
    topBarContentOffsetY: Dp = 0.dp,
    topBarTitleClickEnabled: Boolean = true,
    onTitleClick: (() -> Unit)? = null,
    heroTitle: @Composable (Modifier) -> Unit = { heroModifier ->
        val titleStyle = YubeixTheme.textStyles.title1.copy(fontWeight = FontWeight.SemiBold)
        Text(
            text = title,
            style = overscrollTitleTextStyle(titleStyle),
            color = YubeixTheme.colorScheme.onSurface,
            modifier = heroModifier,
        )
    },
    floatingBottomContent: @Composable BoxScope.() -> Unit = {},
    bodyContent: (@Composable BoxScope.(Modifier) -> Unit)? = null,
    content: LazyListScope.() -> Unit = {},
) {
    val density = LocalDensity.current
    val ownedListState = rememberLazyListState()
    val resolvedListState = listState ?: ownedListState
    val coroutineScope = rememberCoroutineScope()
    val ownedChromeHazeState = rememberHazeState()
    val chromeHazeState = hazeState ?: ownedChromeHazeState
    val reducedDynamicEffectsEnabled = LocalReducedDynamicEffectsEnabled.current
    val shouldProvideChromeHazeSource =
        isRenderEffectSupported() &&
            !reducedDynamicEffectsEnabled
    val topInset = if (LocalSuppressStatusBarInset.current) {
        0.dp
    } else {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    }
    val topBarHeight = topInset + topBarVisualHeight
    val effectiveContentTopPadding = contentTopPadding ?: (topBarHeight + topBarContentGap)
    var scaffoldTopInWindowPx by remember { mutableFloatStateOf(0f) }
    val heroTitleRestingTopInWindowPx = scaffoldTopInWindowPx + with(density) {
        (topBarHeight + topBarContentGap).toPx()
    }
    var heroTitleBottomInWindowPx by remember { mutableFloatStateOf(Float.POSITIVE_INFINITY) }
    val collapsedTitleTriggerPx = with(density) {
        topInset.toPx() + collapseTriggerOffset.toPx()
    }
    val collapsedTitleVisible = rememberCollapsedTitleVisible(
        heroTitleBottomInWindowPx = heroTitleBottomInWindowPx,
        collapseThresholdPx = collapsedTitleTriggerPx,
        hysteresisPx = with(density) { collapseTriggerOffset.toPx() },
        forceVisible = titleMode.pinsCollapsedTitle || resolvedListState.firstVisibleItemIndex > 0,
    )
    val resolvedTopBarCollapseProgress = topBarCollapseProgress?.coerceIn(0f, 1f)
    val resolvedCollapsedTitleProgress = resolvedTopBarCollapseProgress
        ?: if (collapsedTitleVisible) 1f else 0f
    val resolvedOnTitleClick: (() -> Unit)? = if (topBarTitleClickEnabled) {
        onTitleClick ?: {
            coroutineScope.launch {
                resolvedListState.animateScrollToTopFromTitleBar()
            }
            Unit
        }
    } else {
        null
    }

    Scaffold(modifier = modifier, containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    scaffoldTopInWindowPx = coordinates.positionInWindow().y
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
            ) {
                val contentModifier = if (contentMaxWidth != null) {
                    Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = contentMaxWidth)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .then(
                            if (shouldProvideChromeHazeSource) {
                                Modifier.hazeSource(state = chromeHazeState)
                            } else {
                                Modifier
                            },
                        )
                } else {
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (shouldProvideChromeHazeSource) {
                                Modifier.hazeSource(state = chromeHazeState)
                            } else {
                                Modifier
                            },
                        )
                }
                val customBodyContent = bodyContent
                if (customBodyContent != null) {
                    customBodyContent(contentModifier)
                } else {
                    LazyColumn(
                        state = resolvedListState,
                        modifier = contentModifier,
                        contentPadding = PaddingValues(
                            top = effectiveContentTopPadding,
                            bottom = bottomContentPadding + paddingValues.calculateBottomPadding(),
                            start = horizontalContentPadding,
                            end = horizontalContentPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(itemSpacing),
                    ) {
                        if (titleMode.showsHeroTitle) {
                            item {
                                OverscrollTitle(
                                    restingTopInWindowPx = heroTitleRestingTopInWindowPx,
                                    enabled = heroTitleOverscrollScale,
                                    modifier = Modifier.onBottomPositionInWindowChanged {
                                        heroTitleBottomInWindowPx = it
                                    },
                                ) {
                                    heroTitle(Modifier.padding(heroTitlePadding))
                                }
                            }
                        }

                        content()

                        if (showTrailingFiller) {
                            paddingItem(
                                state = resolvedListState,
                                minimumScrollDistance = minimumScrollDistance,
                            )
                        }
                    }
                }
            }

            ScreenChromeTopBar(
                title = title,
                onBack = onBack,
                subtitle = subtitle,
                modifier = Modifier.fillMaxSize(),
                barModifier = if (topBarMaxWidth != null) {
                    Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = topBarMaxWidth)
                        .fillMaxWidth()
                        .height(topBarHeight)
                        .clipToBounds()
                        .zIndex(1f)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(topBarHeight)
                        .clipToBounds()
                        .zIndex(1f)
                },
                actions = actions,
                menuItems = menuItems,
                startContent = topBarStartContent,
                centerContent = topBarCenterContent,
                topInset = topInset,
                titleAlpha = resolvedCollapsedTitleProgress,
                backgroundVisibilityProgress = resolvedCollapsedTitleProgress,
                contentVisibilityProgress = topBarContentVisibilityProgress,
                contentOffsetY = topBarContentOffsetY,
                onTitleClick = resolvedOnTitleClick,
                titleColor = titleColor,
                hazeState = chromeHazeState.takeIf { shouldProvideChromeHazeSource },
                maxVisibleActions = topBarMaxVisibleActions,
            )

            floatingBottomContent()
        }
    }
}

/**
 * The chrome top bar with spec-driven actions and a [WindowListPopup] overflow menu, layered over
 * [LargeTopAppBar]. Internal: hosts [ScreenScaffold]'s action and menu plumbing.
 */
@Composable
internal fun ScreenChromeTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    barModifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: List<TopBarActionSpec> = emptyList(),
    menuItems: List<TopBarMenuItemSpec> = emptyList(),
    startContent: (@Composable () -> Unit)? = null,
    centerContent: (@Composable () -> Unit)? = null,
    topInset: Dp = 0.dp,
    titleAlpha: Float = 1f,
    backgroundVisibilityProgress: Float = titleAlpha,
    contentVisibilityProgress: Float = 1f,
    contentOffsetY: Dp = 0.dp,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    onTitleClick: (() -> Unit)? = null,
    hazeState: HazeState? = null,
    actionSize: Dp = 42.dp,
    sidePadding: Dp = 16.dp,
    menuMaxHeight: Dp = 320.dp,
    maxVisibleActions: Int = ScreenScaffoldDefaults.MaxVisibleActions,
) {
    val visibleActionCount = maxVisibleActions.coerceAtLeast(0)
    val visibleActions = remember(actions, visibleActionCount) {
        actions.take(visibleActionCount)
    }
    val overflowSpecs = remember(actions, menuItems, visibleActionCount) {
        buildTopBarOverflowMenuItems(
            actions = actions,
            menuItems = menuItems,
            maxVisibleActions = visibleActionCount,
        )
    }
    var showOverflowMenu by remember { mutableStateOf(false) }

    LaunchedEffect(overflowSpecs) {
        if (overflowSpecs.isEmpty()) {
            showOverflowMenu = false
        }
    }

    Box(modifier = modifier) {
        LargeTopAppBar(
            title = title,
            onBack = onBack,
            subtitle = subtitle,
            modifier = barModifier,
            startContent = startContent,
            centerContent = centerContent,
            actions = if (visibleActions.isNotEmpty() || overflowSpecs.isNotEmpty()) {
                {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ScreenScaffoldDefaults.TopBarActionSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        visibleActions.forEach { action ->
                            ScreenChromeActionButton(
                                spec = action,
                                titleColor = titleColor,
                                actionSize = actionSize,
                            )
                        }

                        if (overflowSpecs.isNotEmpty()) {
                            Box {
                                ScreenChromeActionButton(
                                    spec = TopBarActionSpec(
                                        key = "overflow-menu",
                                        contentDescription = OVERFLOW_MENU_CONTENT_DESCRIPTION,
                                        onClick = {
                                            if (!showOverflowMenu) {
                                                showOverflowMenu = true
                                            }
                                        },
                                        icon = CupertinoIcons.Outlined.Ellipsis,
                                    ),
                                    titleColor = titleColor,
                                    actionSize = actionSize,
                                )
                                WindowListPopup(
                                    show = showOverflowMenu,
                                    alignment = PopupPositionProvider.Align.End,
                                    onDismissRequest = { showOverflowMenu = false },
                                    minWidth = 176.dp,
                                    maxHeight = menuMaxHeight,
                                ) {
                                    TopBarWindowMenuContent(
                                        items = overflowSpecs,
                                        onDismissRequest = { showOverflowMenu = false },
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                null
            },
            topInset = topInset,
            titleAlpha = titleAlpha,
            backgroundVisibilityProgress = backgroundVisibilityProgress,
            contentVisibilityProgress = contentVisibilityProgress,
            contentOffsetY = contentOffsetY,
            titleColor = titleColor,
            onTitleClick = onTitleClick,
            hazeState = hazeState,
            actionSize = actionSize,
            sidePadding = sidePadding,
        )
    }
}

internal fun buildTopBarOverflowMenuItems(
    actions: List<TopBarActionSpec>,
    menuItems: List<TopBarMenuItemSpec>,
    maxVisibleActions: Int = ScreenScaffoldDefaults.MaxVisibleActions,
): List<TopBarMenuItemSpec> {
    val visibleCount = maxVisibleActions.coerceAtLeast(0)
    val overflowActions = actions
        .drop(visibleCount)
        .map { action -> action.asOverflowMenuItem() }
    return overflowActions + menuItems
}

private fun TopBarActionSpec.asOverflowMenuItem(): TopBarMenuItemSpec.Action {
    val actionTitle = label?.takeIf { it.isNotBlank() } ?: contentDescription
    return TopBarMenuItemSpec.Action(
        key = key,
        title = actionTitle,
        onClick = onClick,
        enabled = enabled,
        leadingContent = icon?.let { imageVector ->
            { revealProgress ->
                TopBarMenuIcon(
                    imageVector = imageVector,
                    enabled = enabled,
                    revealProgress = revealProgress,
                )
            }
        },
    )
}

@Composable
private fun TopBarWindowMenuContent(
    items: List<TopBarMenuItemSpec>,
    onDismissRequest: () -> Unit,
) {
    ListPopupColumn {
        items.forEachIndexed { index, item ->
            TopBarWindowMenuItem(
                item = item,
                onDismissRequest = onDismissRequest,
            )
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = YubeixTheme.colorScheme.dividerLine,
                )
            }
        }
    }
}

@Composable
private fun TopBarWindowMenuItem(
    item: TopBarMenuItemSpec,
    onDismissRequest: () -> Unit,
) {
    val colors = YubeixTheme.colorScheme
    val enabled = item.enabled
    val contentColor = if (enabled) colors.onSurface else colors.disabledOnSecondaryVariant
    val supportingColor = if (enabled) {
        colors.onSurfaceVariantSummary
    } else {
        colors.disabledOnSecondaryVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = {
                    item.performAction(onDismissRequest)
                },
            )
            .padding(start = 14.dp, end = 16.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                item is TopBarMenuItemSpec.Choice && item.selected -> {
                    Icon(
                        imageVector = CupertinoIcons.Outlined.Checkmark,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(TopBarMenuIconSize),
                    )
                }

                item is TopBarMenuItemSpec.Toggle && item.checked -> {
                    Icon(
                        imageVector = CupertinoIcons.Outlined.Checkmark,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(TopBarMenuIconSize),
                    )
                }

                item.leadingContent != null -> {
                    item.leadingContent?.invoke(1f)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = YubeixTheme.textStyles.body1,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.supportingText?.takeIf { it.isNotBlank() }?.let { supportingText ->
                Text(
                    text = supportingText,
                    style = YubeixTheme.textStyles.footnote1,
                    color = supportingColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun TopBarMenuItemSpec.performAction(
    onDismissRequest: () -> Unit,
) {
    when (this) {
        is TopBarMenuItemSpec.Action -> onClick()
        is TopBarMenuItemSpec.Choice -> onSelect()
        is TopBarMenuItemSpec.Toggle -> onCheckedChange(!checked)
    }
    onDismissRequest()
}

@Composable
private fun ScreenChromeActionButton(
    spec: TopBarActionSpec,
    titleColor: Color,
    actionSize: Dp,
) {
    val actionAlpha = spec.alpha.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .then(
                if (actionAlpha < 0.999f) {
                    Modifier.alpha(actionAlpha)
                } else {
                    Modifier
                },
            )
            .then(
                if (actionAlpha < 0.01f) Modifier.clearAndSetSemantics {} else Modifier,
            ),
    ) {
        val customContent = spec.customContent
        if (customContent != null) {
            customContent(titleColor, actionSize)
        } else {
            when (spec.kind) {
                TopBarActionKind.Icon -> {
                    ScreenChromeIconActionButton(
                        spec = spec,
                        titleColor = titleColor,
                        actionSize = actionSize,
                    )
                }

                TopBarActionKind.Capsule -> {
                    ScreenChromeCapsuleActionButton(
                        spec = spec,
                        titleColor = titleColor,
                        actionSize = actionSize,
                    )
                }
            }
        }
        spec.popupContent?.invoke()
    }
}

@Composable
private fun ScreenChromeIconActionButton(
    spec: TopBarActionSpec,
    titleColor: Color,
    actionSize: Dp,
) {
    val colors = YubeixTheme.colorScheme
    val iconTint = if (spec.enabled) titleColor else colors.disabledOnSecondaryVariant
    val actionIcon = spec.icon ?: return

    when {
        spec.enabled -> {
            IconButton(
                modifier = Modifier.size(actionSize),
                onClick = spec.onClick,
            ) {
                ScreenChromeActionIcon(
                    imageVector = actionIcon,
                    contentDescription = spec.contentDescription,
                    tint = iconTint,
                )
            }
        }

        else -> {
            ScreenChromeDisabledSurface(
                modifier = Modifier.size(actionSize),
            ) {
                ScreenChromeActionIcon(
                    imageVector = actionIcon,
                    contentDescription = spec.contentDescription,
                    tint = iconTint,
                )
            }
        }
    }
}

@Composable
private fun ScreenChromeActionIcon(
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color,
) {
    Crossfade(
        targetState = imageVector,
        animationSpec = tween(durationMillis = 160),
        label = "ScreenChromeActionIconFade",
    ) { icon ->
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

@Composable
private fun ScreenChromeCapsuleActionButton(
    spec: TopBarActionSpec,
    titleColor: Color,
    actionSize: Dp,
) {
    val colors = YubeixTheme.colorScheme
    val label = spec.label?.takeIf { it.isNotBlank() } ?: spec.contentDescription
    val horizontalPadding = 14.dp
    val minWidth = actionSize + horizontalPadding * 2

    when {
        !spec.enabled -> {
            ScreenChromeDisabledSurface(
                modifier = Modifier
                    .height(actionSize)
                    .width(minWidth),
            ) {
                Text(
                    text = label,
                    style = YubeixTheme.textStyles.body2,
                    color = colors.disabledOnSecondaryVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        else -> {
            ScreenChromeFlatCapsuleButton(
                label = label,
                onClick = spec.onClick,
                modifier = Modifier.height(actionSize),
                backgroundColor = colors.background.copy(alpha = 0.84f),
                contentColor = titleColor,
                minWidth = minWidth,
            )
        }
    }
}

@Composable
private fun ScreenChromeDisabledSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = YubeixTheme.colorScheme
    val backgroundAlpha = if (colors.surface.luminance() < 0.5f) 0.82f else 0.74f
    Box(
        modifier = modifier
            .background(
                color = colors.background.copy(alpha = backgroundAlpha),
                shape = Capsule(),
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun ScreenChromeFlatCapsuleButton(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    minWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(minWidth)
            .background(
                color = backgroundColor,
                shape = Capsule(),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = YubeixTheme.textStyles.body2,
            color = contentColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TopBarMenuIcon(
    imageVector: ImageVector,
    enabled: Boolean,
    revealProgress: Float,
) {
    val colors = YubeixTheme.colorScheme
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = if (enabled) {
            colors.onSurface.copy(alpha = 0.92f * revealProgress.coerceIn(0.6f, 1f))
        } else {
            colors.disabledOnSecondaryVariant
        },
        modifier = Modifier.size(TopBarMenuIconSize),
    )
}
// endregion
