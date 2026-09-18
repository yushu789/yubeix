// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import site.unclefish.yubeix.anim.yubeixSpring
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.IconButton
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.component.SheetBackgroundTransformSource
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.Checkmark
import site.unclefish.yubeix.icon.cupertino.outlined.Xmark
import site.unclefish.yubeix.theme.LocalContentColor
import site.unclefish.yubeix.theme.LocalDismissState
import site.unclefish.yubeix.theme.YubeixSheetElevatedTheme
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.elevatedSheetSurface
import site.unclefish.yubeix.theme.yubeixShape
import site.unclefish.yubeix.theme.yubeixUnevenShape
import site.unclefish.yubeix.utils.RemovePlatformDialogDefaultEffects
import site.unclefish.yubeix.utils.platformDialogProperties
import site.unclefish.yubeix.utils.rememberSafeTopInset
import kotlin.coroutines.cancellation.CancellationException

/**
 * A bottom sheet that slides up from the bottom of the screen, rendered at window level without `Scaffold`.
 *
 * The sheet presents with a slow, non-bouncy spring, hosts a floating drag handle and a title row
 * that can hold [WindowBottomSheetCancelAction]/[WindowBottomSheetConfirmAction] header buttons via
 * [startAction]/[endAction], and runs its body at the elevated interface level so inner components
 * pick up the raised sheet palette ([YubeixSheetElevatedTheme]).
 *
 * Use [LocalDismissState] inside `content` to request dismissal from inner composables.
 *
 * @param show Whether the [WindowBottomSheet] is shown.
 * @param modifier The modifier to be applied to the [WindowBottomSheet].
 * @param title Optional title to display at the top of the [WindowBottomSheet].
 * @param summary Optional summary displayed above the content, below the title row.
 * @param startAction Optional [Composable] to display on the start side of the title (e.g. a close button).
 * @param endAction Optional [Composable] to display on the end side of the title (e.g. a submit button).
 * @param backgroundColor The background color of the [WindowBottomSheet]. Defaults to the elevated
 *   sheet surface so a dark sheet stays readable over the dimmed page behind it.
 * @param contentColor The preferred content color provided to the [WindowBottomSheet] content.
 * @param enableWindowDim Whether to dim the window behind the [WindowBottomSheet].
 * @param cornerRadius The corner radius of the top corners of the [WindowBottomSheet].
 * @param sheetMaxWidth The maximum width of the [WindowBottomSheet].
 * @param onDismissRequest Will called when the user tries to dismiss the Dialog by clicking outside or pressing the back button.
 * @param onDismissFinished The callback when the [WindowBottomSheet] is completely dismissed.
 * @param outsideMargin The margin outside the [WindowBottomSheet].
 * @param insideMargin The margin inside the [WindowBottomSheet]. Its width is also used as the
 *   padding above the title row.
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding.
 * @param dragHandleColor The color of the drag handle at the top.
 * @param allowDismiss Whether to allow dismissing the sheet via drag or back gesture.
 * @param enableNestedScroll Whether to enable nested scrolling for the content.
 * @param content The [Composable] content of the [WindowBottomSheet].
 */
@Composable
fun WindowBottomSheet(
    show: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    summary: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    backgroundColor: Color = YubeixTheme.colorScheme.background.elevatedSheetSurface(),
    contentColor: Color = YubeixTheme.colorScheme.onSurface,
    enableWindowDim: Boolean = true,
    cornerRadius: Dp = WindowBottomSheetDefaultCornerRadius,
    sheetMaxWidth: Dp = WindowBottomSheetDefaultMaxWidth,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = BottomSheetDefaults.outsideMargin,
    insideMargin: DpSize = WindowBottomSheetDefaultInsideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = BottomSheetDefaults.dragHandleColor(),
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    content: @Composable () -> Unit,
) {
    val safeTopInset = rememberSafeTopInset()

    val currentOnDismissRequest = rememberUpdatedState(onDismissRequest)

    WindowBottomSheetContentLayout(
        show = show,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        sheetMaxWidth = sheetMaxWidth,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        dragHandleColor = dragHandleColor,
        popupHost = { visible, hostContent ->
            if (visible) {
                Dialog(
                    onDismissRequest = {
                        if (allowDismiss) {
                            currentOnDismissRequest.value?.invoke()
                        }
                    },
                    properties = platformDialogProperties(),
                ) {
                    RemovePlatformDialogDefaultEffects()
                    hostContent()
                }
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
        topInset = safeTopInset,
        content = {
            CompositionLocalProvider(
                LocalDismissState provides {
                    currentOnDismissRequest.value?.invoke()
                },
                LocalContentColor provides contentColor,
            ) {
                Column {
                    summary?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = YubeixTheme.textStyles.footnote1,
                            color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    content()
                }
            }
        },
    )
}

/**
 * A bottom sheet that slides up from the bottom of the screen, rendered at window level without `Scaffold`.
 */
@Deprecated(
    message = "Use WindowBottomSheet with show: Boolean parameter instead for unidirectional data flow.",
    replaceWith = ReplaceWith(
        "WindowBottomSheet(show = show.value, modifier = modifier, title = title, summary = summary, startAction = startAction, endAction = endAction, backgroundColor = backgroundColor, contentColor = contentColor, enableWindowDim = enableWindowDim, cornerRadius = cornerRadius, sheetMaxWidth = sheetMaxWidth, onDismissRequest = onDismissRequest, onDismissFinished = onDismissFinished, outsideMargin = outsideMargin, insideMargin = insideMargin, defaultWindowInsetsPadding = defaultWindowInsetsPadding, dragHandleColor = dragHandleColor, allowDismiss = allowDismiss, enableNestedScroll = enableNestedScroll, content = content)",
    ),
)
@Composable
fun WindowBottomSheet(
    show: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    title: String? = null,
    summary: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    backgroundColor: Color = YubeixTheme.colorScheme.background.elevatedSheetSurface(),
    contentColor: Color = YubeixTheme.colorScheme.onSurface,
    enableWindowDim: Boolean = true,
    cornerRadius: Dp = WindowBottomSheetDefaultCornerRadius,
    sheetMaxWidth: Dp = WindowBottomSheetDefaultMaxWidth,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = BottomSheetDefaults.outsideMargin,
    insideMargin: DpSize = WindowBottomSheetDefaultInsideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = BottomSheetDefaults.dragHandleColor(),
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    content: @Composable () -> Unit,
) {
    WindowBottomSheet(
        show = show.value,
        modifier = modifier,
        title = title,
        summary = summary,
        startAction = startAction,
        endAction = endAction,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
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
        content = content,
    )
}

@Deprecated("Use BottomSheetDefaults instead", ReplaceWith("BottomSheetDefaults"))
object WindowBottomSheetDefaults {

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
 * CompositionLocal that provides a dismiss request function for [WindowBottomSheet].
 *
 * Call the provided function to request dismissal from inside bottom sheet content.
 */
@Deprecated(
    "Use LocalDismissState instead, which is provided by all overlay components.",
    ReplaceWith("LocalDismissState", "site.unclefish.yubeix.theme.LocalDismissState"),
)
val LocalWindowBottomSheetState = staticCompositionLocalOf<(() -> Unit)?> { null }

/** The visual style of a [WindowBottomSheetHeaderAction]. */
enum class WindowBottomSheetHeaderActionStyle {
    /** A neutral action, drawn as a grey filled circle (e.g. a close button). */
    Neutral,

    /** A primary action, drawn with the theme primary color (e.g. a confirm button). */
    Primary,
}

/**
 * The title text of a [WindowBottomSheet] title row. Single line, centered, ellipsized.
 *
 * @param text The title text.
 * @param modifier The modifier to be applied to the title.
 */
@Composable
fun WindowBottomSheetTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = YubeixTheme.textStyles.title4.fontSize,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        color = YubeixTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * A round header action button for the [WindowBottomSheet] title row, e.g. as [startAction] or
 * [endAction]. Use [WindowBottomSheetCancelAction] or [WindowBottomSheetConfirmAction] for the
 * standard close/confirm icons.
 *
 * @param imageVector The icon to display.
 * @param contentDescription The content description of the icon.
 * @param onClick The callback when the action is clicked.
 * @param modifier The modifier to be applied to the action.
 * @param enabled Whether the action responds to clicks. Disabled actions are drawn dimmed.
 * @param style The visual style of the action, see [WindowBottomSheetHeaderActionStyle].
 */
@Composable
fun WindowBottomSheetHeaderAction(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: WindowBottomSheetHeaderActionStyle = WindowBottomSheetHeaderActionStyle.Neutral,
) {
    val colors = YubeixTheme.colorScheme
    val containerColor = when {
        !enabled -> colors.disabledPrimaryButton
        style == WindowBottomSheetHeaderActionStyle.Primary -> colors.primary
        else -> colors.surfaceContainerHigh
    }
    val contentColor = when {
        !enabled -> colors.disabledOnPrimaryButton
        style == WindowBottomSheetHeaderActionStyle.Primary -> colors.onPrimary
        else -> colors.onSurface
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(WindowBottomSheetHeaderActionSize),
        enabled = enabled,
        backgroundColor = containerColor,
        cornerRadius = WindowBottomSheetHeaderActionCornerRadius,
        minWidth = WindowBottomSheetHeaderActionSize,
        minHeight = WindowBottomSheetHeaderActionSize,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(WindowBottomSheetHeaderActionIconSize),
        )
    }
}

/**
 * A neutral close (x-mark) header action for the [WindowBottomSheet] title row.
 *
 * @param contentDescription The content description of the icon.
 * @param onClick The callback when the action is clicked.
 * @param modifier The modifier to be applied to the action.
 */
@Composable
fun WindowBottomSheetCancelAction(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowBottomSheetHeaderAction(
        imageVector = CupertinoIcons.Outlined.Xmark,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        style = WindowBottomSheetHeaderActionStyle.Neutral,
    )
}

/**
 * A primary confirm (checkmark) header action for the [WindowBottomSheet] title row.
 *
 * @param contentDescription The content description of the icon.
 * @param onClick The callback when the action is clicked.
 * @param modifier The modifier to be applied to the action.
 * @param enabled Whether the action responds to clicks. Disabled actions are drawn dimmed.
 */
@Composable
fun WindowBottomSheetConfirmAction(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    WindowBottomSheetHeaderAction(
        imageVector = CupertinoIcons.Outlined.Checkmark,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = WindowBottomSheetHeaderActionStyle.Primary,
    )
}

/**
 * Internal shared layout logic for [SuperBottomSheet] and [WindowBottomSheet], adapted from
 * Miuix 0.8.8 BottomSheetContentLayout.
 *
 * @param show Whether the bottom sheet is currently shown.
 * @param backgroundColor The background color of the bottom sheet.
 * @param cornerRadius The corner radius of the top corners of the bottom sheet.
 * @param sheetMaxWidth The maximum width of the bottom sheet.
 * @param outsideMargin The margin outside the bottom sheet.
 * @param insideMargin The margin inside the bottom sheet.
 * @param dragHandleColor The color of the drag handle.
 * @param popupHost A composable that provides the container (e.g., DialogLayout or Dialog).
 *   It receives the visibility state and the inner content composable.
 * @param modifier The modifier to be applied to the bottom sheet content.
 * @param title Optional title to display at the top of the bottom sheet.
 * @param startAction Optional [Composable] to display on the start side of the title.
 * @param endAction Optional [Composable] to display on the end side of the title.
 * @param enableWindowDim Whether to dim the window behind the bottom sheet.
 * @param onDismissRequest The callback when the user tries to dismiss the bottom sheet.
 * @param onDismissFinished The callback when the bottom sheet is completely dismissed.
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding.
 * @param allowDismiss Whether to allow dismissing the sheet via drag or back gesture.
 * @param enableNestedScroll Whether to enable nested scrolling for the content.
 * @param enableBackgroundTransform Whether the page behind the sheet shrinks while it is shown.
 * @param topInset Optional top inset override. If null, calculated from window insets.
 * @param content The content of the bottom sheet.
 */
@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
internal fun WindowBottomSheetContentLayout(
    show: Boolean,
    backgroundColor: Color,
    cornerRadius: Dp,
    sheetMaxWidth: Dp,
    outsideMargin: DpSize,
    insideMargin: DpSize,
    dragHandleColor: Color,
    popupHost: @Composable (visible: Boolean, content: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    defaultWindowInsetsPadding: Boolean = true,
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    enableBackgroundTransform: Boolean = true,
    topInset: Dp? = null,
    content: @Composable () -> Unit,
) {
    val animationProgress = remember {
        Animatable(0f, visibilityThreshold = WINDOW_BOTTOM_SHEET_ANIMATION_VISIBILITY_THRESHOLD)
    }
    val dragOffsetY = remember { Animatable(0f) }
    val currentOnDismissFinished by rememberUpdatedState(onDismissFinished)
    val internalVisible = remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            internalVisible.value = true
            dragOffsetY.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = WindowBottomSheetEnterAnimationSpec,
            )
        } else {
            if (!internalVisible.value) return@LaunchedEffect
            if (dragOffsetY.value > 0f) {
                // Sheet already dragged off-screen; snap immediately.
                animationProgress.snapTo(0f)
            } else {
                // Button/back dismiss; animate normally.
                animationProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = yubeixSpring(damping = 0.9f, response = 0.38f),
                )
            }
            internalVisible.value = false
            currentOnDismissFinished?.invoke()
        }
    }

    if (!show && !internalVisible.value) return

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val coroutineScope = rememberCoroutineScope()
    val sheetHeightPx = remember { mutableIntStateOf(0) }
    val dimAlpha = remember { mutableFloatStateOf(1f) }
    val dragSnapChannel = remember { Channel<Float>(capacity = Channel.CONFLATED) }
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

    // Push the page behind the sheet back in step with the dim, so dragging the sheet down
    // brings the page forward again.
    SheetBackgroundTransformSource(enabled = enableBackgroundTransform) {
        animationProgress.value.coerceIn(0f, 1f) * dimAlpha.floatValue.coerceIn(0f, 1f)
    }

    val requestDismiss: () -> Unit = remember {
        { currentOnDismissRequest?.invoke() }
    }

    val resetGesture: suspend () -> Unit = {
        dragOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 150))
        animate(dimAlpha.floatValue, 1f, animationSpec = tween(durationMillis = 150)) { value, _ ->
            dimAlpha.floatValue = value
        }
    }

    LaunchedEffect(dragOffsetY) {
        for (target in dragSnapChannel) dragOffsetY.snapTo(target)
    }

    popupHost(internalVisible.value) {
        val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = navigationEventState,
            isBackEnabled = show,
            onBackCancelled = { coroutineScope.launch { resetGesture() } },
            onBackCompleted = {
                if (allowDismiss) {
                    requestDismiss()
                } else {
                    coroutineScope.launch { resetGesture() }
                }
            },
        )

        LaunchedEffect(navigationEventState.transitionState, allowDismiss) {
            val transitionState = navigationEventState.transitionState
            if (
                transitionState is NavigationEventTransitionState.InProgress &&
                transitionState.direction == NavigationEventTransitionState.TRANSITIONING_BACK
            ) {
                val maxOffset = if (sheetHeightPx.intValue > 0) sheetHeightPx.intValue.toFloat() else 500f
                val offset = transitionState.latestEvent.progress * maxOffset
                val finalOffset = if (!allowDismiss) offset * 0.1f else offset
                dragSnapChannel.trySend(finalOffset)
                if (allowDismiss) dimAlpha.floatValue = 1f - transitionState.latestEvent.progress
            }
        }

        if (enableWindowDim) {
            val baseColor = YubeixTheme.colorScheme.windowDimming
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val dimProgress = animationProgress.value.coerceIn(0f, 1f)
                        drawRect(baseColor.copy(alpha = baseColor.alpha * dimAlpha.floatValue * dimProgress))
                    },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (allowDismiss) {
                                requestDismiss()
                            }
                        },
                    )
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            val sheetModifier = modifier.graphicsLayer {
                val progress = animationProgress.value
                val currentHeight = sheetHeightPx.intValue.toFloat()
                val windowHeightPx = with(density) { windowInfo.containerDpSize.height.toPx() }
                val baseOffset = if (currentHeight > 0) currentHeight else windowHeightPx
                translationY = baseOffset * (1f - progress) + dragOffsetY.value
            }

            WindowBottomSheetContent(
                title = title,
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                sheetMaxWidth = sheetMaxWidth,
                outsideMargin = outsideMargin,
                insideMargin = insideMargin,
                defaultWindowInsetsPadding = defaultWindowInsetsPadding,
                dragHandleColor = dragHandleColor,
                allowDismiss = allowDismiss,
                sheetHeightPx = sheetHeightPx,
                animationProgress = animationProgress,
                dragOffsetY = dragOffsetY,
                dimAlpha = dimAlpha,
                dragSnapChannel = dragSnapChannel,
                onDismissRequest = {
                    if (allowDismiss) {
                        requestDismiss()
                    }
                },
                modifier = sheetModifier,
                topInset = topInset,
                enableNestedScroll = enableNestedScroll,
                startAction = startAction?.let { action ->
                    { CompositionLocalProvider(LocalDismissState provides requestDismiss) { action() } }
                },
                endAction = endAction?.let { action ->
                    { CompositionLocalProvider(LocalDismissState provides requestDismiss) { action() } }
                },
                content = {
                    CompositionLocalProvider(LocalDismissState provides requestDismiss) {
                        content()
                    }
                },
            )
        }
    }
}

@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
private fun WindowBottomSheetContent(
    title: String?,
    backgroundColor: Color,
    cornerRadius: Dp,
    sheetMaxWidth: Dp,
    outsideMargin: DpSize,
    insideMargin: DpSize,
    defaultWindowInsetsPadding: Boolean,
    dragHandleColor: Color,
    allowDismiss: Boolean,
    sheetHeightPx: MutableIntState,
    animationProgress: Animatable<Float, *>,
    dragOffsetY: Animatable<Float, *>,
    dimAlpha: MutableFloatState,
    dragSnapChannel: Channel<Float>,
    onDismissRequest: (() -> Unit)?,
    modifier: Modifier = Modifier,
    topInset: Dp? = null,
    enableNestedScroll: Boolean = true,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val windowHeight = windowInfo.containerDpSize.height
    // Do not use windowHeight as a remember key; IME resize would recreate handlers and trigger dismiss.
    val currentWindowHeight by rememberUpdatedState(windowHeight)
    val coroutineScope = rememberCoroutineScope()

    val settlingJob = remember { mutableStateOf<Job?>(null) }
    val isSettling = remember { mutableStateOf(false) }
    val calculatedTopInset = topInset ?: rememberSafeTopInset()

    val calculateNewOffset = remember(allowDismiss) {
        { current: Float, delta: Float ->
            val newOffset = current + delta
            if (newOffset < 0) {
                val dampingFactor = 0.1f
                (current + delta * dampingFactor).coerceAtMost(0f)
            } else if (newOffset >= 0 && !allowDismiss) {
                val dampingFactor = 0.1f
                val dampedAmount = if (delta > 0) delta * dampingFactor else delta
                (current + dampedAmount).coerceAtLeast(0f)
            } else {
                newOffset
            }
        }
    }

    val updateDimAlpha = remember(allowDismiss) {
        { offset: Float ->
            val thresholdPx = if (sheetHeightPx.intValue > 0) sheetHeightPx.intValue.toFloat() else 500f
            val alpha = if (offset >= 0 && allowDismiss) {
                1f - (offset / thresholdPx).coerceIn(0f, 1f)
            } else {
                1f
            }
            dimAlpha.floatValue = alpha
        }
    }

    // Settlement logic
    val performSettle: (Float) -> Unit = remember(allowDismiss, density) {
        { velocity ->
            settlingJob.value?.cancel()
            isSettling.value = true
            settlingJob.value = coroutineScope.launch {
                val currentOffset = dragOffsetY.value
                val dismissThresholdPx = with(density) { 150.dp.toPx() }
                val velocityThresholdPx = with(density) { 800.dp.toPx() }
                val windowHeightPx = with(density) { currentWindowHeight.toPx() }

                val shouldDismiss = allowDismiss && (
                    (velocity > velocityThresholdPx) ||
                        (currentOffset > dismissThresholdPx && velocity > -velocityThresholdPx)
                    )

                try {
                    if (shouldDismiss) {
                        if (currentOffset >= windowHeightPx) {
                            onDismissRequest?.invoke()
                        } else {
                            val sheetHeight = sheetHeightPx.intValue.toFloat()
                            val settleJob = launch {
                                dragOffsetY.animateTo(
                                    targetValue = windowHeightPx,
                                    animationSpec = yubeixSpring(damping = 0.85f, response = 0.4f),
                                    initialVelocity = velocity,
                                ) {
                                    updateDimAlpha(value)
                                }
                            }
                            // Wait until sheet leaves viewport, then dismiss immediately
                            snapshotFlow { dragOffsetY.value }
                                .first { sheetHeight > 0 && it >= sheetHeight }
                            settleJob.cancel()
                            onDismissRequest?.invoke()
                        }
                    } else {
                        val effectiveVelocity = if (!allowDismiss && velocity > 0) 0f else velocity
                        dragOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = yubeixSpring(damping = 0.85f, response = 0.4f),
                            initialVelocity = effectiveVelocity,
                        ) {
                            updateDimAlpha(value)
                        }
                        dimAlpha.floatValue = 1f
                    }
                } catch (_: CancellationException) {
                    // Animation is interrupted
                } finally {
                    // Reset state after animation completes
                    isSettling.value = false
                }
            }
        }
    }

    // Nested scroll logic
    val nestedScrollConnection = remember(enableNestedScroll, allowDismiss, density) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!enableNestedScroll) return Offset.Zero

                // Allow interruption whenever settling
                if (isSettling.value) {
                    settlingJob.value?.cancel()
                    isSettling.value = false
                }

                val delta = available.y
                // If the sheet is offset, prioritize restoring its position
                if (delta < 0 && dragOffsetY.value > 0) {
                    val newOffset = calculateNewOffset(dragOffsetY.value, delta).coerceAtLeast(0f)
                    val consumedY = dragOffsetY.value - newOffset
                    if (consumedY != 0f) {
                        dragSnapChannel.trySend(newOffset)
                        updateDimAlpha(newOffset)
                        return Offset(0f, consumedY * -1f)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (!enableNestedScroll) return Offset.Zero

                val delta = available.y
                if (delta > 0) {
                    if (!allowDismiss) return Offset.Zero

                    if (isSettling.value) {
                        settlingJob.value?.cancel()
                        isSettling.value = false
                    }

                    val newOffset = calculateNewOffset(dragOffsetY.value, delta)
                    dragSnapChannel.trySend(newOffset)
                    updateDimAlpha(newOffset)

                    // Dismiss immediately if dragged beyond window height
                    val windowHeightPx = with(density) { currentWindowHeight.toPx() }
                    if (newOffset > windowHeightPx) {
                        performSettle(0f)
                        return available
                    }

                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!enableNestedScroll || isSettling.value) return Velocity.Zero

                // Take over fling if the sheet is offset
                if (dragOffsetY.value > 0) {
                    performSettle(available.y)
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!enableNestedScroll || isSettling.value) return Velocity.Zero

                if (dragOffsetY.value > 0) {
                    performSettle(available.y)
                    return available
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    WindowBottomSheetColumn(
        title = title,
        startAction = startAction,
        endAction = endAction,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        sheetMaxWidth = sheetMaxWidth,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        dragHandleColor = dragHandleColor,
        allowDismiss = allowDismiss,
        windowHeight = windowHeight,
        topInset = calculatedTopInset,
        enableNestedScroll = enableNestedScroll,
        sheetHeightPx = sheetHeightPx,
        animationProgress = animationProgress,
        dragOffsetY = dragOffsetY,
        nestedScrollConnection = nestedScrollConnection,
        coroutineScope = coroutineScope,
        dragSnapChannel = dragSnapChannel,
        onSettle = performSettle,
        onUpdateAlpha = updateDimAlpha,
        modifier = modifier,
        content = content,
    )
}

@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
private fun WindowBottomSheetColumn(
    title: String?,
    startAction: @Composable (() -> Unit?)?,
    endAction: @Composable (() -> Unit?)?,
    backgroundColor: Color,
    cornerRadius: Dp,
    sheetMaxWidth: Dp,
    outsideMargin: DpSize,
    insideMargin: DpSize,
    defaultWindowInsetsPadding: Boolean,
    dragHandleColor: Color,
    allowDismiss: Boolean,
    windowHeight: Dp,
    topInset: Dp,
    enableNestedScroll: Boolean,
    sheetHeightPx: MutableIntState,
    animationProgress: Animatable<Float, *>,
    dragOffsetY: Animatable<Float, *>,
    nestedScrollConnection: NestedScrollConnection,
    coroutineScope: CoroutineScope,
    dragSnapChannel: Channel<Float>,
    onSettle: (velocity: Float) -> Unit,
    onUpdateAlpha: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val topCornerShape = yubeixUnevenShape(topStart = cornerRadius, topEnd = cornerRadius)
    val bottomFillOffsetPx by remember {
        derivedStateOf {
            val dragOverscrollOffsetPx = (-dragOffsetY.value).coerceAtLeast(0f)
            val currentHeight = sheetHeightPx.intValue.toFloat()
            val windowHeightPx = with(density) { windowHeight.toPx() }
            val baseOffset = if (currentHeight > 0f) currentHeight else windowHeightPx
            val springOvershootOffsetPx = (baseOffset * (animationProgress.value - 1f)).coerceAtLeast(0f)
            maxOf(dragOverscrollOffsetPx, springOvershootOffsetPx)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Background fill for the area revealed when dragging or springing above the bottom edge.
        if (bottomFillOffsetPx > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = sheetMaxWidth)
                    .fillMaxWidth()
                    .height(with(density) { bottomFillOffsetPx.toDp() } + 1.dp)
                    .padding(horizontal = outsideMargin.width)
                    .background(backgroundColor),
            )
        }

        // The sheet body is one level above the page, so everything it hosts moves up with it.
        YubeixSheetElevatedTheme {
            Box(
                modifier = modifier
                    .pointerInput(Unit) { detectTapGestures { /* Consume click */ } }
                    .then(if (enableNestedScroll) Modifier.nestedScroll(nestedScrollConnection) else Modifier)
                    .widthIn(max = sheetMaxWidth)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = windowHeight - topInset)
                    .onGloballyPositioned { coordinates ->
                        sheetHeightPx.intValue = coordinates.size.height
                    }
                    .then(if (defaultWindowInsetsPadding) Modifier.imePadding() else Modifier)
                    .padding(horizontal = outsideMargin.width)
                    .clip(topCornerShape)
                    .background(backgroundColor)
                    .padding(horizontal = insideMargin.width)
                    .padding(bottom = insideMargin.height),
            ) {
                DragHandleArea(
                    dragHandleColor = dragHandleColor,
                    allowDismiss = allowDismiss,
                    dragOffsetY = dragOffsetY,
                    coroutineScope = coroutineScope,
                    dragSnapChannel = dragSnapChannel,
                    onSettle = onSettle,
                    onUpdateAlpha = onUpdateAlpha,
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleAndActionsRow(
                        title = title,
                        startAction = startAction,
                        endAction = endAction,
                        topPadding = insideMargin.width,
                    )

                    content()
                }
            }
        }
    }
}

@Composable
private fun DragHandleArea(
    dragHandleColor: Color,
    allowDismiss: Boolean,
    dragOffsetY: Animatable<Float, *>,
    coroutineScope: CoroutineScope,
    dragSnapChannel: Channel<Float>,
    onSettle: (velocity: Float) -> Unit,
    onUpdateAlpha: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPressing = remember { mutableFloatStateOf(0f) }
    val pressScale = remember { Animatable(1f) }
    val pressWidth = remember { Animatable(45f) }
    val handleShape = yubeixShape(2.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressing.floatValue = 1f
                        coroutineScope.launch {
                            pressScale.animateTo(
                                targetValue = 1.15f,
                                animationSpec = tween(durationMillis = 100),
                            )
                        }
                        coroutineScope.launch {
                            pressWidth.animateTo(
                                targetValue = 55f,
                                animationSpec = tween(durationMillis = 100),
                            )
                        }

                        val released = tryAwaitRelease()
                        if (released) {
                            isPressing.floatValue = 0f
                            coroutineScope.launch {
                                pressScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 150),
                                )
                            }
                            coroutineScope.launch {
                                pressWidth.animateTo(
                                    targetValue = 45f,
                                    animationSpec = tween(durationMillis = 150),
                                )
                            }
                        }
                    },
                )
            }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { dragAmount ->
                    val newOffset = dragOffsetY.value + dragAmount
                    val finalOffset = if (newOffset < 0) {
                        // Damping up
                        (dragOffsetY.value + dragAmount * 0.1f).coerceAtMost(0f)
                    } else if (newOffset >= 0 && !allowDismiss) {
                        // Damping down if not dismissible
                        val dampedAmount = if (dragAmount > 0) dragAmount * 0.1f else dragAmount
                        (dragOffsetY.value + dampedAmount).coerceAtLeast(0f)
                    } else {
                        newOffset
                    }

                    dragSnapChannel.trySend(finalOffset)
                    onUpdateAlpha(finalOffset)
                },
                onDragStarted = {
                    isPressing.floatValue = 1f
                    coroutineScope.launch {
                        pressScale.animateTo(1.15f, animationSpec = tween(durationMillis = 100))
                    }
                    coroutineScope.launch {
                        pressWidth.animateTo(55f, animationSpec = tween(durationMillis = 100))
                    }
                },
                onDragStopped = { velocity ->
                    isPressing.floatValue = 0f
                    coroutineScope.launch {
                        pressScale.animateTo(1f, animationSpec = tween(durationMillis = 150))
                    }
                    coroutineScope.launch {
                        pressWidth.animateTo(45f, animationSpec = tween(durationMillis = 150))
                    }

                    // Delegate the settle logic to the shared function
                    onSettle(velocity)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(pressWidth.value.dp)
                .height(4.dp)
                .graphicsLayer {
                    scaleY = pressScale.value
                }
                .clip(handleShape)
                .drawBehind {
                    val handleAlpha = lerp(0.2f, 0.35f, isPressing.floatValue)
                    drawRect(dragHandleColor.copy(alpha = handleAlpha))
                },
        )
    }
}

@Composable
private fun TitleAndActionsRow(
    title: String?,
    startAction: @Composable (() -> Unit?)?,
    endAction: @Composable (() -> Unit?)?,
    topPadding: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topPadding + WindowBottomSheetHeaderActionSize + 8.dp)
            .padding(top = topPadding, bottom = 8.dp),
    ) {
        // Start action (e.g. close button)
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            startAction?.invoke()
        }

        // Title text
        title?.let {
            WindowBottomSheetTitle(
                text = it,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // End action (e.g. submit button)
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            endAction?.invoke()
        }
    }
}

private val WindowBottomSheetDefaultCornerRadius = 32.dp
private val WindowBottomSheetDefaultMaxWidth = 560.dp
private val WindowBottomSheetDefaultInsideMargin = DpSize(16.dp, 14.dp)
private val WindowBottomSheetHeaderActionSize: Dp = 44.dp
private val WindowBottomSheetHeaderActionCornerRadius: Dp = 12.dp
private val WindowBottomSheetHeaderActionIconSize: Dp = 20.dp
private const val WINDOW_BOTTOM_SHEET_ANIMATION_VISIBILITY_THRESHOLD = 0.0001f

/** The default smooth, non-bouncy spring used when presenting a [WindowBottomSheet]. */
private val WindowBottomSheetEnterAnimationSpec: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = WINDOW_BOTTOM_SHEET_ANIMATION_VISIBILITY_THRESHOLD,
)
