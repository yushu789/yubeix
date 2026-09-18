// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.elevatedSheetSurface
import site.unclefish.yubeix.utils.YubeixPopupUtils.Companion.DialogLayout

/**
 * A bottom sheet that slides up from the bottom of the screen.
 * The height adapts to the content size, but will not cover the status bar area.
 *
 * The sheet presents with the same fish-back visual as [WindowBottomSheet]: a floating card with
 * 32.dp continuous top corners at the elevated interface level ([elevatedSheetSurface]), a
 * centered drag handle above a fixed-height title row, and a window dim whose alpha follows the
 * presentation progress and the drag offset.
 *
 * @param show Whether the [SuperBottomSheet] is shown.
 * @param modifier The modifier to be applied to the [SuperBottomSheet].
 * @param title Optional title to display at the top of the [SuperBottomSheet].
 * @param startAction Optional [Composable] to display on the start side of the title (e.g. a close button).
 * @param endAction Optional [Composable] to display on the end side of the title (e.g. a submit button).
 * @param backgroundColor The background color of the [SuperBottomSheet]. Defaults to the elevated
 *   sheet surface so a dark sheet stays readable over the dimmed page behind it.
 * @param enableWindowDim Whether to dim the window behind the [SuperBottomSheet].
 * @param cornerRadius The corner radius of the top corners of the [SuperBottomSheet].
 * @param sheetMaxWidth The maximum width of the [SuperBottomSheet].
 * @param onDismissRequest Will called when the user tries to dismiss the Dialog by clicking outside or pressing the back button.
 * @param onDismissFinished The callback when the [SuperBottomSheet] is completely dismissed.
 * @param outsideMargin The margin outside the [SuperBottomSheet].
 * @param insideMargin The margin inside the [SuperBottomSheet]. Its width is also used as the
 *   padding above the title row.
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding.
 * @param dragHandleColor The color of the drag handle at the top.
 * @param allowDismiss Whether to allow dismissing the sheet via drag or back gesture.
 * @param enableNestedScroll Whether to enable nested scrolling for the content.
 * @param renderInRootScaffold Whether to render the bottom sheet in the root (outermost) Scaffold.
 *   When true (default), the bottom sheet covers the full screen. When false, it renders within the
 *   current Scaffold's bounds.
 * @param content The [Composable] content of the [SuperBottomSheet].
 */
@Composable
fun SuperBottomSheet(
    show: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    backgroundColor: Color = BottomSheetDefaults.backgroundColor(),
    enableWindowDim: Boolean = true,
    cornerRadius: Dp = BottomSheetDefaults.cornerRadius,
    sheetMaxWidth: Dp = BottomSheetDefaults.maxWidth,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = BottomSheetDefaults.outsideMargin,
    insideMargin: DpSize = BottomSheetDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = BottomSheetDefaults.dragHandleColor(),
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    renderInRootScaffold: Boolean = true,
    content: @Composable () -> Unit,
) {
    WindowBottomSheetContentLayout(
        show = show,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        sheetMaxWidth = sheetMaxWidth,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        dragHandleColor = dragHandleColor,
        popupHost = { visible, hostContent ->
            val visibleState = remember { mutableStateOf(false) }
            visibleState.value = visible
            DialogLayout(
                visible = visibleState,
                enableWindowDim = false,
                enterTransition = EnterTransition.None,
                exitTransition = ExitTransition.None,
                enableAutoLargeScreen = false,
                renderInRootScaffold = renderInRootScaffold,
            ) {
                hostContent()
            }
        },
        modifier = modifier,
        title = title,
        startAction = startAction,
        endAction = endAction,
        enableWindowDim = enableWindowDim,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        allowDismiss = allowDismiss,
        enableNestedScroll = enableNestedScroll,
        content = content,
    )
}

/**
 * A bottom sheet that slides up from the bottom of the screen.
 */
@Deprecated(
    message = "Use SuperBottomSheet with show: Boolean parameter instead for unidirectional data flow.",
    replaceWith = ReplaceWith(
        "SuperBottomSheet(show = show.value, modifier = modifier, title = title, startAction = startAction, endAction = endAction, backgroundColor = backgroundColor, enableWindowDim = enableWindowDim, cornerRadius = cornerRadius, sheetMaxWidth = sheetMaxWidth, onDismissRequest = onDismissRequest, onDismissFinished = onDismissFinished, outsideMargin = outsideMargin, insideMargin = insideMargin, defaultWindowInsetsPadding = defaultWindowInsetsPadding, dragHandleColor = dragHandleColor, allowDismiss = allowDismiss, enableNestedScroll = enableNestedScroll, renderInRootScaffold = renderInRootScaffold, content = content)",
    ),
)
@Composable
fun SuperBottomSheet(
    show: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    title: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    backgroundColor: Color = BottomSheetDefaults.backgroundColor(),
    enableWindowDim: Boolean = true,
    cornerRadius: Dp = BottomSheetDefaults.cornerRadius,
    sheetMaxWidth: Dp = BottomSheetDefaults.maxWidth,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = BottomSheetDefaults.outsideMargin,
    insideMargin: DpSize = BottomSheetDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = BottomSheetDefaults.dragHandleColor(),
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    renderInRootScaffold: Boolean = true,
    content: @Composable () -> Unit,
) {
    SuperBottomSheet(
        show = show.value,
        modifier = modifier,
        title = title,
        startAction = startAction,
        endAction = endAction,
        backgroundColor = backgroundColor,
        enableWindowDim = enableWindowDim,
        cornerRadius = cornerRadius,
        sheetMaxWidth = sheetMaxWidth,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        dragHandleColor = dragHandleColor,
        allowDismiss = allowDismiss,
        enableNestedScroll = enableNestedScroll,
        renderInRootScaffold = renderInRootScaffold,
        content = content,
    )
}

@Deprecated("Use BottomSheetDefaults instead", ReplaceWith("BottomSheetDefaults"))
object SuperBottomSheetDefaults {

    @Composable
    fun backgroundColor() = BottomSheetDefaults.backgroundColor()

    @Composable
    fun dragHandleColor() = BottomSheetDefaults.dragHandleColor()

    val cornerRadius get() = BottomSheetDefaults.cornerRadius

    val maxWidth get() = BottomSheetDefaults.maxWidth

    val outsideMargin get() = BottomSheetDefaults.outsideMargin

    val insideMargin get() = BottomSheetDefaults.insideMargin
}

/**
 * The defaults for [SuperBottomSheet] and [WindowBottomSheet].
 */
object BottomSheetDefaults {

    /**
     * The default background color of the bottom sheet.
     *
     * A sheet sits at the elevated interface level, so in dark mode this is lifted one step above
     * the page background - see [elevatedSheetSurface].
     */
    @Composable
    fun backgroundColor() = YubeixTheme.colorScheme.background.elevatedSheetSurface()

    /**
     * The default color of the drag handle.
     */
    @Composable
    fun dragHandleColor() = YubeixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f)

    /**
     * The default corner radius of the top corners of the bottom sheet.
     */
    val cornerRadius = 32.dp

    /**
     * The default maximum width of the bottom sheet.
     */
    val maxWidth = 560.dp

    /**
     * The default margin outside the bottom sheet.
     */
    val outsideMargin = DpSize(0.dp, 0.dp)

    /**
     * The default margin inside the bottom sheet. Its width is also used as the padding above the
     * title row.
     */
    val insideMargin = DpSize(16.dp, 14.dp)
}
