// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kyant.shapes.RoundedCornerStyle
import com.kyant.shapes.RoundedRectangle
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import site.unclefish.yubeix.basic.CircularProgressIndicator
import site.unclefish.yubeix.basic.LinearProgressIndicator
import site.unclefish.yubeix.basic.ProgressIndicatorDefaults
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.LocalDismissState
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.utils.RemovePlatformDialogDefaultEffects
import site.unclefish.yubeix.utils.YubeixPopupUtils.Companion.DialogLayout
import site.unclefish.yubeix.utils.platformDialogProperties

/**
 * A dialog with a title, a summary, and other contents, rendered as an iOS-style alert card.
 *
 * The default rendering follows the cupertino alert: a 270.dp wide card with continuous rounded
 * corners, centered iOS typography, and a soft shadow, presented with a scale-up-from-120% +
 * fade-in transition over a fading black scrim. [backgroundColor] defaults to the alert container
 * grey that follows the dark/light appearance, and [insideMargin] defaults to the alert's 18.dp
 * content padding. When [show] turns false the card fades out before [onDismissFinished] fires.
 *
 * Use [LocalDismissState] inside `content` to request dismissal from inner composables.
 *
 * @param show Whether the [SuperDialog] is shown.
 * @param modifier The modifier to be applied to the [SuperDialog] card.
 * @param title The title of the [SuperDialog].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperDialog].
 * @param summaryColor The color of the summary.
 * @param backgroundColor The background color of the [SuperDialog] card. Defaults to the cupertino
 *   alert container color, which resolves from the dark/light appearance.
 * @param enableWindowDim Whether to draw the cupertino scrim over the content behind the [SuperDialog].
 * @param onDismissRequest Will called when the user tries to dismiss the Dialog by clicking outside or pressing the back button.
 * @param onDismissFinished The callback when the [SuperDialog] is completely dismissed.
 * @param outsideMargin The margin outside the [SuperDialog] card.
 * @param insideMargin The margin inside the [SuperDialog] card, around the title, the summary, and the content.
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding to the [SuperDialog].
 * @param renderInRootScaffold Whether to render the dialog in the root (outermost) Scaffold.
 *   When true (default), the dialog covers the full screen. When false, it renders within the
 *   current Scaffold's bounds.
 * @param content The [Composable] content of the [SuperDialog].
 */
@Composable
@Suppress("ComposeModifierNotUsedAtRoot")
fun SuperDialog(
    show: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = DialogDefaults.titleColor(),
    summary: String? = null,
    summaryColor: Color = DialogDefaults.summaryColor(),
    backgroundColor: Color = superDialogContainerColor(),
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DialogDefaults.outsideMargin,
    insideMargin: DpSize = SuperDialogInsideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    renderInRootScaffold: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = YubeixTheme.colorScheme.background.luminance() < 0.5f
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val currentOnDismissFinished by rememberUpdatedState(onDismissFinished)
    // The cupertino alert dims the page behind the card with a plain black scrim rather than the
    // theme's window-dimming color: lighter in light mode so the dimmed page stays readable.
    val scrimColor = Color.Black.copy(alpha = if (dark) 0.4f else 0.2f)
    val visibleState = remember { mutableStateOf(false) }
    visibleState.value = show

    val requestDismiss: () -> Unit = { currentOnDismissRequest?.invoke() }

    DialogLayout(
        visible = visibleState,
        enableWindowDim = false,
        enterTransition = SuperDialogEnterTransition,
        exitTransition = SuperDialogExitTransition,
        enableAutoLargeScreen = false,
        renderInRootScaffold = renderInRootScaffold,
        onDismissFinished = { currentOnDismissFinished?.invoke() },
    ) {
        val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = navigationEventState,
            isBackEnabled = show,
            onBackCompleted = requestDismiss,
        )

        CompositionLocalProvider(LocalDismissState provides requestDismiss) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (enableWindowDim) {
                    // The scrim fades on its own schedule (faster than the card fade so the page
                    // behind reads through earlier while the card is still settling in).
                    AnimatedVisibility(
                        visible = visibleState.value,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = CUPERTINO_ALERT_SCRIM_ENTER_DURATION_MILLIS),
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = CUPERTINO_ALERT_SCRIM_EXIT_DURATION_MILLIS),
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind { drawRect(scrimColor) }
                                .pointerInput(Unit) {
                                    detectTapGestures { requestDismiss() }
                                },
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { requestDismiss() }
                            },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (defaultWindowInsetsPadding) {
                                Modifier
                                    .systemBarsPadding()
                                    .imePadding()
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = outsideMargin.width, vertical = outsideMargin.height),
                    contentAlignment = Alignment.Center,
                ) {
                    SuperDialogAlertCard(
                        title = title,
                        titleColor = titleColor,
                        summary = summary,
                        summaryColor = summaryColor,
                        containerColor = backgroundColor,
                        insideMargin = insideMargin,
                        modifier = modifier,
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * A dialog with a title, a summary, and other contents.
 */
@Deprecated(
    message = "Use SuperDialog with show: Boolean parameter instead for unidirectional data flow.",
    replaceWith = ReplaceWith(
        "SuperDialog(show = show.value, modifier = modifier, title = title, titleColor = titleColor, summary = summary, summaryColor = summaryColor, backgroundColor = backgroundColor, enableWindowDim = enableWindowDim, onDismissRequest = onDismissRequest, onDismissFinished = onDismissFinished, outsideMargin = outsideMargin, insideMargin = insideMargin, defaultWindowInsetsPadding = defaultWindowInsetsPadding, renderInRootScaffold = renderInRootScaffold, content = content)",
    ),
)
@Composable
@Suppress("ComposeModifierNotUsedAtRoot")
fun SuperDialog(
    show: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = DialogDefaults.titleColor(),
    summary: String? = null,
    summaryColor: Color = DialogDefaults.summaryColor(),
    backgroundColor: Color = superDialogContainerColor(),
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DialogDefaults.outsideMargin,
    insideMargin: DpSize = SuperDialogInsideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    renderInRootScaffold: Boolean = true,
    content: @Composable () -> Unit,
) {
    SuperDialog(
        show = show.value,
        modifier = modifier,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        backgroundColor = backgroundColor,
        enableWindowDim = enableWindowDim,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        renderInRootScaffold = renderInRootScaffold,
        content = content,
    )
}

/**
 * The visual role of a [CupertinoAlertAction], following the iOS alert conventions.
 */
enum class CupertinoAlertActionStyle {
    /** A regular confirmation action, drawn with the theme primary color. */
    Default,

    /** The cancel action, drawn as a neutral filled button with bold text. */
    Cancel,

    /** A destructive action, drawn with the theme error color. */
    Destructive,
}

/**
 * One button of an iOS-style alert presented by the actions-list overload of [SuperDialog].
 *
 * @param label The text displayed on the button.
 * @param role The visual role of the button, see [CupertinoAlertActionStyle].
 * @param onClick The callback when the button is clicked.
 * @param enabled Whether the button responds to clicks. Disabled buttons are drawn dimmed.
 */
data class CupertinoAlertAction(
    val label: String,
    val role: CupertinoAlertActionStyle = CupertinoAlertActionStyle.Default,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/**
 * An iOS-style alert dialog with a frosted-glass container, a centered title, an optional message,
 * and a row (or column) of [CupertinoAlertAction] buttons.
 *
 * The dialog scales up from 120% and fades in like the compose-cupertino alert, and renders at
 * window level like [WindowDialog]. When [hazeState] is provided, the container blurs the content
 * behind it (haze backdrop); otherwise it falls back to an opaque container color.
 *
 * @param show Whether the dialog is shown.
 * @param title The title of the dialog.
 * @param actions The buttons to display. May be empty for dialogs without actions.
 * @param modifier The modifier to be applied to the dialog container.
 * @param message The optional secondary message below the title.
 * @param onDismissRequest Will be called when the user tries to dismiss the dialog by clicking
 *   outside or pressing the back button (when [dismissOnBackPress] is enabled).
 * @param onDismissFinished The callback when the dialog is completely dismissed (exit animation done).
 * @param buttonsOrientation Whether the action buttons are laid out horizontally in a row or
 *   vertically in a column.
 * @param hazeState The haze state of the content behind the dialog. When non-null, the dialog
 *   container blurs that backdrop instead of drawing an opaque background.
 * @param dialogWidth The fixed width of the dialog container.
 * @param minHeight The minimum height of the dialog container.
 * @param contentPadding The padding applied inside the dialog container, around title, message,
 *   and custom content.
 * @param titleLeadingContent An optional composable (e.g. a progress spinner) shown before the title.
 * @param content An optional custom content shown below the message, above the action buttons.
 * @param dismissOnBackPress Whether the back button dismisses the dialog.
 * @param dismissOnClickOutside Whether clicking the scrim dismisses the dialog.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
@Suppress("ComposeModifierNotUsedAtRoot")
fun SuperDialog(
    show: Boolean,
    title: String,
    actions: List<CupertinoAlertAction>,
    modifier: Modifier = Modifier,
    message: String? = null,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    buttonsOrientation: Orientation = Orientation.Horizontal,
    hazeState: HazeState? = null,
    dialogWidth: Dp = CupertinoAlertDialogWidth,
    minHeight: Dp = CupertinoAlertDialogMinHeight,
    contentPadding: PaddingValues = CupertinoAlertDialogPadding,
    titleLeadingContent: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    val dark = YubeixTheme.colorScheme.background.luminance() < 0.5f
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val scrimColor = Color.Black.copy(alpha = if (dark) 0.4f else 0.2f)
    val containerColor = if (dark) {
        CupertinoAlertDialogDarkContainerColor
    } else {
        CupertinoAlertDialogLightContainerColor
    }
    val resolvedContainerColor = if (hazeState != null) {
        containerColor.copy(
            alpha = if (dark) {
                CUPERTINO_ALERT_DIALOG_DARK_BACKDROP_ALPHA
            } else {
                CUPERTINO_ALERT_DIALOG_LIGHT_BACKDROP_ALPHA
            },
        )
    } else {
        containerColor
    }
    val labelColor = YubeixTheme.colorScheme.onSurface

    CupertinoAlertHost(
        show = show,
        onDismissRequest = currentOnDismissRequest,
        onDismissFinished = onDismissFinished,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        scrimColor = scrimColor,
    ) {
        CupertinoAlertDialogCard(
            title = title,
            titleColor = labelColor,
            summary = message,
            summaryColor = labelColor,
            containerColor = resolvedContainerColor,
            hazeBackdropColor = containerColor,
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.Center).then(modifier),
            dialogWidth = dialogWidth,
            minHeight = minHeight,
            contentPadding = contentPadding,
            actions = actions,
            buttonsOrientation = buttonsOrientation,
            titleLeadingContent = titleLeadingContent,
            content = content,
        )
    }
}

/**
 * The cupertino alert card shared by the [SuperDialog] cupertino overloads and the window-level
 * [WindowDialog]: the rounded, shadowed (optionally frosted) container with a centered title, an
 * optional message, custom content, and a row (or column) of action buttons.
 *
 * Hosts place it inside a centered full-screen box; pass `Modifier.align(Alignment.Center)` via
 * [modifier].
 *
 * @param title The title of the card; null hides the title row (unless [titleLeadingContent] is set).
 * @param titleColor The color of the title text.
 * @param summary The optional message below the title.
 * @param summaryColor The color of the message text.
 * @param containerColor The drawn container color (with backdrop alpha already applied when hazing).
 * @param hazeBackdropColor The base container color the frost style is derived from; defaults to
 *   [containerColor].
 * @param hazeState When non-null, the card blurs that backdrop instead of drawing an opaque background.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun CupertinoAlertDialogCard(
    title: String?,
    titleColor: Color,
    summary: String?,
    summaryColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    hazeBackdropColor: Color = containerColor,
    hazeState: HazeState? = null,
    dialogWidth: Dp = CupertinoAlertDialogWidth,
    minHeight: Dp = CupertinoAlertDialogMinHeight,
    contentPadding: PaddingValues = CupertinoAlertDialogPadding,
    actions: List<CupertinoAlertAction> = emptyList(),
    buttonsOrientation: Orientation = Orientation.Horizontal,
    titleLeadingContent: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val useHazeBackdrop = hazeState != null
    val hazeStyle = remember(hazeBackdropColor) {
        HazeStyle(
            backgroundColor = hazeBackdropColor,
            tints = emptyList(),
            blurRadius = CupertinoAlertDialogBlurRadius,
            noiseFactor = 0f,
            fallbackTint = HazeTint(hazeBackdropColor),
        )
    }

    Column(
        modifier = modifier
            .width(dialogWidth)
            .heightIn(min = minHeight)
            .shadow(
                elevation = CupertinoAlertDialogShadowElevation,
                shape = CupertinoAlertDialogShape,
                clip = true,
            )
            .then(
                if (useHazeBackdrop) {
                    Modifier.hazeEffect(
                        state = requireNotNull(hazeState),
                        style = hazeStyle,
                    ) {
                        inputScale = HazeInputScale.Auto
                    }
                } else {
                    Modifier
                },
            )
            .background(containerColor),
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CupertinoAlertDialogTitleMessageSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (titleLeadingContent == null) {
                title?.let {
                    Text(
                        text = it,
                        style = CupertinoAlertDialogTitleStyle,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 10.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    titleLeadingContent()
                    title?.let {
                        Text(
                            text = it,
                            style = CupertinoAlertDialogTitleStyle,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            summary?.let {
                Text(
                    text = it,
                    style = CupertinoAlertDialogMessageStyle,
                    color = summaryColor,
                    textAlign = TextAlign.Center,
                )
            }
            content?.invoke()
        }

        if (actions.isNotEmpty()) {
            CupertinoAlertDialogActions(
                actions = actions,
                orientation = buttonsOrientation,
            )
        }
    }
}

/**
 * An iOS-style progress dialog with a circular spinner next to the title, for work whose extent
 * cannot be measured.
 *
 * Back dismissal is disabled by default; pass [dismissOnBackPress] = true together with
 * [onDismissRequest] to let the user abandon the work.
 *
 * @param show Whether the dialog is shown.
 * @param title The title of the dialog.
 * @param modifier The modifier to be applied to the dialog container.
 * @param onDismissRequest Will be called when the user tries to dismiss the dialog.
 * @param dismissOnBackPress Whether the back button dismisses the dialog.
 */
@Composable
fun CupertinoProgressDialog(
    show: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = false,
) {
    SuperDialog(
        show = show,
        title = title,
        actions = emptyList(),
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        dismissOnBackPress = dismissOnBackPress,
        dialogWidth = CupertinoProgressDialogWidth,
        minHeight = 0.dp,
        contentPadding = CupertinoProgressDialogContentPadding,
        titleLeadingContent = {
            Box(
                modifier = Modifier.size(CupertinoProgressDialogIndicatorSize),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = null,
                    colors = ProgressIndicatorDefaults.progressIndicatorColors(
                        foregroundColor = YubeixTheme.colorScheme.primary,
                        backgroundColor = YubeixTheme.colorScheme.primary.copy(alpha = 0.18f),
                    ),
                    strokeWidth = 3.dp,
                    size = CupertinoProgressDialogIndicatorSize,
                    modifier = Modifier.size(CupertinoProgressDialogIndicatorSize),
                )
            }
        },
    )
}

/**
 * An iOS-style progress dialog that shows a bar rather than a spinner, for work whose extent is known.
 *
 * [progress] is a 0..1 fraction, or null for work whose length cannot be measured - the bar runs
 * indeterminate then. Supply [onDismissRequest] together with [dismissOnBackPress] = true to let
 * the user abandon the work.
 *
 * @param show Whether the dialog is shown.
 * @param title The title of the dialog.
 * @param progress The 0..1 progress fraction, or null for an indeterminate bar.
 * @param modifier The modifier to be applied to the dialog container.
 * @param onDismissRequest Will be called when the user tries to dismiss the dialog.
 * @param dismissOnBackPress Whether the back button dismisses the dialog.
 */
@Composable
fun CupertinoLinearProgressDialog(
    show: Boolean,
    title: String,
    progress: Float?,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = false,
) {
    // Keep the last determinate value while the operation is finishing. Callers commonly clear
    // their progress in finally immediately after emitting 1.0, while the result/navigation
    // callback may need a frame to dismiss this dialog; mapping null straight to 0.0 makes a
    // completed bar visibly jump back to the midpoint/starting state.
    var lastDeterminateProgress by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(show) {
        if (!show) lastDeterminateProgress = null
    }
    LaunchedEffect(progress) {
        if (progress != null) {
            lastDeterminateProgress = progress.coerceIn(0f, 1f)
        }
    }
    // Once the work has finished, the dialog can remain mounted for its fade-out while the caller
    // clears the progress value. Keep that exit frame at 100%; otherwise the nullable progress
    // briefly becomes an indeterminate/zero bar and visually falls back to about 50%.
    val displayedProgress = when {
        !show -> 1f
        progress != null -> progress.coerceIn(0f, 1f)
        else -> lastDeterminateProgress
    }
    val animatedProgress by animateFloatAsState(
        targetValue = displayedProgress ?: 0f,
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "CupertinoLinearProgressDialogProgress",
    )
    SuperDialog(
        show = show,
        title = title,
        actions = emptyList(),
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        dismissOnBackPress = dismissOnBackPress,
        dialogWidth = CupertinoProgressDialogWidth,
        minHeight = 0.dp,
        contentPadding = CupertinoProgressDialogContentPadding,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CupertinoProgressDialogBarTopPadding),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = displayedProgress?.let { animatedProgress },
                    colors = ProgressIndicatorDefaults.progressIndicatorColors(
                        foregroundColor = YubeixTheme.colorScheme.primary,
                        backgroundColor = YubeixTheme.colorScheme.primary.copy(alpha = 0.18f),
                    ),
                    height = CupertinoProgressDialogBarHeight,
                )
            }
        },
    )
}

/**
 * The default [SuperDialog] container color: the cupertino alert's container grey, which stays one
 * step above the pure system backgrounds so the card reads against the dimmed page behind it.
 */
@Composable
private fun superDialogContainerColor(): Color {
    val dark = YubeixTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        CupertinoAlertDialogDarkContainerColor
    } else {
        CupertinoAlertDialogLightContainerColor
    }
}

/**
 * The alert card shown by the content-lambda [SuperDialog]: a fixed-width column with continuous
 * rounded corners and a soft shadow, holding the title, the summary, and the content in one
 * centered stack.
 */
@Composable
private fun SuperDialogAlertCard(
    title: String?,
    titleColor: Color,
    summary: String?,
    summaryColor: Color,
    containerColor: Color,
    insideMargin: DpSize,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .width(CupertinoAlertDialogWidth)
            .heightIn(min = CupertinoAlertDialogMinHeight)
            .shadow(
                elevation = CupertinoAlertDialogShadowElevation,
                shape = CupertinoAlertDialogShape,
                clip = true,
            )
            .background(containerColor)
            .pointerInput(Unit) {
                // Consume taps on the card body so they do not fall through to the scrim.
                detectTapGestures { }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = insideMargin.width, vertical = insideMargin.height),
            verticalArrangement = Arrangement.spacedBy(CupertinoAlertDialogTitleMessageSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            title?.let {
                Text(
                    text = it,
                    style = CupertinoAlertDialogTitleStyle,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                )
            }
            summary?.let {
                Text(
                    text = it,
                    style = CupertinoAlertDialogMessageStyle,
                    color = summaryColor,
                    textAlign = TextAlign.Center,
                )
            }
            content()
        }
    }
}

/**
 * The window-level host of the Cupertino alert family.
 *
 * Uses the same window-level Dialog host idea as [WindowDialog] (yubeix platform dialog properties,
 * navigation-event back handling), then keeps the compose-cupertino alert scale/fade transition
 * inside that full-screen host.
 */
@Composable
internal fun CupertinoAlertHost(
    show: Boolean,
    onDismissRequest: (() -> Unit)?,
    onDismissFinished: (() -> Unit)?,
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    scrimColor: Color,
    scrimEnabled: Boolean = true,
    applyWindowInsetsPadding: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val currentOnDismissFinished by rememberUpdatedState(onDismissFinished)
    val visibleState = remember { MutableTransitionState(false) }
    var hasBeenShown by remember { mutableStateOf(false) }

    visibleState.targetState = show

    LaunchedEffect(show) {
        if (show) {
            hasBeenShown = true
        }
    }

    LaunchedEffect(visibleState.isIdle, visibleState.currentState, show, hasBeenShown) {
        if (hasBeenShown && !show && visibleState.isIdle && !visibleState.currentState) {
            hasBeenShown = false
            currentOnDismissFinished?.invoke()
        }
    }

    if (!visibleState.currentState && !visibleState.targetState) return

    val dismiss: () -> Unit = { currentOnDismissRequest?.invoke() }

    Dialog(
        onDismissRequest = dismiss,
        properties = platformDialogProperties(),
    ) {
        RemovePlatformDialogDefaultEffects()

        val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = navigationEventState,
            isBackEnabled = dismissOnBackPress,
            onBackCompleted = dismiss,
        )

        CompositionLocalProvider(
            LocalHapticFeedback provides haptic,
            LocalDismissState provides dismiss,
        ) {
            val transition = rememberTransition(
                transitionState = visibleState,
                label = "CupertinoAlertDialog",
            )
            val scrimAlpha by transition.animateFloat(
                transitionSpec = {
                    tween(
                        durationMillis = if (targetState) {
                            CUPERTINO_ALERT_SCRIM_ENTER_DURATION_MILLIS
                        } else {
                            CUPERTINO_ALERT_SCRIM_EXIT_DURATION_MILLIS
                        },
                    )
                },
                label = "CupertinoAlertScrimAlpha",
            ) { visible ->
                if (visible) 1f else 0f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (scrimEnabled) {
                            Modifier.drawBehind {
                                drawRect(scrimColor.copy(alpha = scrimColor.alpha * scrimAlpha))
                            }
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (dismissOnClickOutside) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures { dismiss() }
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = scaleIn(
                        initialScale = 1.2f,
                        animationSpec = tween(
                            durationMillis = CUPERTINO_ALERT_ENTER_DURATION_MILLIS,
                            easing = LinearOutSlowInEasing,
                        ),
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = CUPERTINO_ALERT_ENTER_DURATION_MILLIS,
                            easing = LinearOutSlowInEasing,
                        ),
                    ),
                    exit = fadeOut(animationSpec = tween(CUPERTINO_ALERT_EXIT_DURATION_MILLIS)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (applyWindowInsetsPadding) {
                                    Modifier.systemBarsPadding().imePadding()
                                } else {
                                    Modifier
                                },
                            ),
                        content = content,
                    )
                }
            }
        }
    }
}

@Composable
private fun CupertinoAlertDialogActions(
    actions: List<CupertinoAlertAction>,
    orientation: Orientation,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = CupertinoAlertDialogActionsHorizontalPadding,
                end = CupertinoAlertDialogActionsHorizontalPadding,
                bottom = CupertinoAlertDialogActionsBottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(CupertinoAlertDialogButtonSpacing),
    ) {
        if (orientation == Orientation.Horizontal) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CupertinoAlertDialogButtonHeight),
                horizontalArrangement = Arrangement.spacedBy(CupertinoAlertDialogButtonSpacing),
            ) {
                actions.forEach { action ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        CupertinoAlertDialogAction(action = action)
                    }
                }
            }
        } else {
            actions.forEach { action ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CupertinoAlertDialogButtonHeight),
                ) {
                    CupertinoAlertDialogAction(action = action)
                }
            }
        }
    }
}

@Composable
private fun CupertinoAlertDialogAction(action: CupertinoAlertAction) {
    val colors = YubeixTheme.colorScheme
    val fontWeight = when (action.role) {
        CupertinoAlertActionStyle.Cancel -> FontWeight.Bold
        else -> FontWeight.Normal
    }
    val enabledButtonColor = when (action.role) {
        CupertinoAlertActionStyle.Default -> colors.primary
        CupertinoAlertActionStyle.Cancel -> colors.onSurface.copy(alpha = 0.08f)
        CupertinoAlertActionStyle.Destructive -> colors.error
    }
    val enabledContentColor = when (action.role) {
        CupertinoAlertActionStyle.Default,
        CupertinoAlertActionStyle.Destructive,
        -> colors.onPrimary

        CupertinoAlertActionStyle.Cancel -> colors.onSurface
    }
    val buttonColor = if (action.enabled) {
        enabledButtonColor
    } else {
        enabledButtonColor.copy(alpha = 0.36f)
    }
    val contentColor = if (action.enabled) {
        enabledContentColor
    } else {
        enabledContentColor.copy(alpha = 0.48f)
    }

    Box(
        modifier = Modifier
            .clip(CupertinoAlertDialogActionShape)
            .background(buttonColor, CupertinoAlertDialogActionShape)
            .clickable(
                enabled = action.enabled,
                onClick = action.onClick,
                role = Role.Button,
            )
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = action.label,
            style = CupertinoAlertDialogActionStyle.copy(fontWeight = fontWeight),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

private val CupertinoAlertDialogShape = RoundedRectangle(18.dp, style = RoundedCornerStyle.Continuous)
private val CupertinoAlertDialogActionShape = RoundedRectangle(10.dp, style = RoundedCornerStyle.Continuous)
internal val CupertinoAlertDialogWidth = 270.dp
internal val CupertinoAlertDialogMinHeight = 110.dp
internal val CupertinoAlertDialogPadding = PaddingValues(18.dp)
private val CupertinoAlertDialogTitleMessageSpacing = 4.dp
private val CupertinoAlertDialogButtonHeight = 45.dp
private val CupertinoAlertDialogButtonSpacing = 8.dp
private val CupertinoAlertDialogActionsHorizontalPadding = 10.dp
private val CupertinoAlertDialogActionsBottomPadding = 10.dp
private val CupertinoAlertDialogShadowElevation = 18.dp
private val CupertinoAlertDialogBlurRadius = 24.dp
internal val CupertinoAlertDialogDarkContainerColor = Color(0xFF232323)
internal val CupertinoAlertDialogLightContainerColor = Color(0xFFEEEEEE)
private val CupertinoProgressDialogWidth = 220.dp
private val CupertinoProgressDialogContentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
private val CupertinoProgressDialogIndicatorSize = 24.dp
private val CupertinoProgressDialogBarHeight = 4.dp
private val CupertinoProgressDialogBarTopPadding = 14.dp
private val SuperDialogInsideMargin = DpSize(18.dp, 18.dp)
private val SuperDialogEnterTransition: EnterTransition = scaleIn(
    initialScale = 1.2f,
    animationSpec = tween(
        durationMillis = CUPERTINO_ALERT_ENTER_DURATION_MILLIS,
        easing = LinearOutSlowInEasing,
    ),
) + fadeIn(
    animationSpec = tween(
        durationMillis = CUPERTINO_ALERT_ENTER_DURATION_MILLIS,
        easing = LinearOutSlowInEasing,
    ),
)
private val SuperDialogExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(durationMillis = CUPERTINO_ALERT_EXIT_DURATION_MILLIS),
)
private const val CUPERTINO_ALERT_DIALOG_LIGHT_BACKDROP_ALPHA = 0.78f
private const val CUPERTINO_ALERT_DIALOG_DARK_BACKDROP_ALPHA = 0.72f
private const val CUPERTINO_ALERT_SCRIM_ENTER_DURATION_MILLIS = 300
private const val CUPERTINO_ALERT_SCRIM_EXIT_DURATION_MILLIS = 250
private const val CUPERTINO_ALERT_ENTER_DURATION_MILLIS = 420
private const val CUPERTINO_ALERT_EXIT_DURATION_MILLIS = 160
private val CupertinoAlertDialogTitleStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 22.sp,
    fontWeight = FontWeight.SemiBold,
)
private val CupertinoAlertDialogMessageStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 18.sp,
    fontWeight = FontWeight.Normal,
)
private val CupertinoAlertDialogActionStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 22.sp,
)
