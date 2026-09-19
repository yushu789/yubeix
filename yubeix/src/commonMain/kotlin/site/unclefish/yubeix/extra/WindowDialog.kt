// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.theme.LocalContentColor
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * A dialog rendered at window level with the cupertino alert card: a centered 270.dp card with
 * 18.dp continuous corners, a centered title, an optional summary, custom content, and optional
 * action buttons, over a plain scrim.
 *
 * Use [LocalDismissState] inside `content` to request dismissal from inner composables.
 *
 * @param show Whether the [WindowDialog] is shown.
 * @param modifier The modifier to be applied to the [WindowDialog] card.
 * @param title The title of the [WindowDialog].
 * @param titleColor The color of the title.
 * @param summary The summary of the [WindowDialog].
 * @param summaryColor The color of the summary.
 * @param backgroundColor The background color of the [WindowDialog] card. Defaults to the cupertino
 *   alert container color, which resolves from the dark/light appearance.
 * @param contentColor The preferred content color provided to the [WindowDialog] content.
 * @param enableWindowDim Whether to draw the scrim over the content behind the [WindowDialog].
 * @param onDismissRequest Will called when the user tries to dismiss the Dialog by clicking outside or pressing the back button.
 * @param onDismissFinished The callback when the [WindowDialog] is completely dismissed.
 * @param outsideMargin The margin outside the [WindowDialog] card.
 * @param insideMargin The margin inside the [WindowDialog] card, around the title, the summary, and the content.
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding to the [WindowDialog].
 * @param content The [Composable] content of the [WindowDialog].
 */
@Composable
fun WindowDialog(
    show: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    summary: String? = null,
    summaryColor: Color = YubeixTheme.colorScheme.onSurfaceSecondary,
    backgroundColor: Color = windowDialogContainerColor(),
    contentColor: Color = YubeixTheme.colorScheme.onSurface,
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DialogDefaults.outsideMargin,
    insideMargin: DpSize = WindowDialogInsideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = YubeixTheme.colorScheme.background.luminance() < 0.5f
    val scrimColor = Color.Black.copy(alpha = if (dark) 0.4f else 0.2f)

    CupertinoAlertHost(
        show = show,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        scrimColor = scrimColor,
        scrimEnabled = enableWindowDim,
        applyWindowInsetsPadding = defaultWindowInsetsPadding,
    ) {
        CupertinoAlertDialogCard(
            title = title,
            titleColor = titleColor,
            summary = summary,
            summaryColor = summaryColor,
            containerColor = backgroundColor,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = outsideMargin.width, vertical = outsideMargin.height)
                .then(modifier),
            contentPadding = PaddingValues(horizontal = insideMargin.width, vertical = insideMargin.height),
            content = {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    content()
                }
            },
        )
    }
}

/**
 * A window-level dialog with a centered title, an optional message, and a row (or column) of
 * cupertino action buttons - the same rendering as the actions-list overload of [SuperDialog],
 * hosted at window level.
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
 * @param enableWindowDim Whether to draw the scrim over the content behind the dialog.
 * @param dialogWidth The fixed width of the dialog container.
 * @param minHeight The minimum height of the dialog container.
 * @param contentPadding The padding applied inside the dialog container, around title, message,
 *   and custom content.
 * @param titleLeadingContent An optional composable (e.g. a progress spinner) shown before the title.
 * @param content An optional custom content shown below the message, above the action buttons.
 * @param dismissOnBackPress Whether the back button dismisses the dialog.
 * @param dismissOnClickOutside Whether clicking the scrim dismisses the dialog.
 */
@Composable
fun WindowDialog(
    show: Boolean,
    title: String,
    actions: List<CupertinoAlertAction>,
    modifier: Modifier = Modifier,
    message: String? = null,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    buttonsOrientation: Orientation = Orientation.Horizontal,
    enableWindowDim: Boolean = true,
    dialogWidth: Dp = CupertinoAlertDialogWidth,
    minHeight: Dp = CupertinoAlertDialogMinHeight,
    contentPadding: PaddingValues = CupertinoAlertDialogPadding,
    titleLeadingContent: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    val dark = YubeixTheme.colorScheme.background.luminance() < 0.5f
    val scrimColor = Color.Black.copy(alpha = if (dark) 0.4f else 0.2f)

    CupertinoAlertHost(
        show = show,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        scrimColor = scrimColor,
        scrimEnabled = enableWindowDim,
    ) {
        CupertinoAlertDialogCard(
            title = title,
            titleColor = YubeixTheme.colorScheme.onSurface,
            summary = message,
            summaryColor = YubeixTheme.colorScheme.onSurface,
            containerColor = windowDialogContainerColor(),
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
 * A dialog with a title, a summary, and other contents, rendered at window level.
 */
@Deprecated(
    message = "Use WindowDialog with show: Boolean parameter instead for unidirectional data flow.",
    replaceWith = ReplaceWith(
        "WindowDialog(show = show.value, modifier = modifier, title = title, titleColor = titleColor, summary = summary, summaryColor = summaryColor, backgroundColor = backgroundColor, contentColor = contentColor, enableWindowDim = enableWindowDim, onDismissRequest = onDismissRequest, onDismissFinished = onDismissFinished, outsideMargin = outsideMargin, insideMargin = insideMargin, defaultWindowInsetsPadding = defaultWindowInsetsPadding, content = content)",
    ),
)
@Composable
fun WindowDialog(
    show: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = YubeixTheme.colorScheme.onSurface,
    summary: String? = null,
    summaryColor: Color = YubeixTheme.colorScheme.onSurfaceSecondary,
    backgroundColor: Color = windowDialogContainerColor(),
    contentColor: Color = YubeixTheme.colorScheme.onSurface,
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DialogDefaults.outsideMargin,
    insideMargin: DpSize = WindowDialogInsideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    WindowDialog(
        show = show.value,
        modifier = modifier,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        enableWindowDim = enableWindowDim,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        content = content,
    )
}

/** The resolved cupertino container color of the window dialog card. */
@Composable
private fun windowDialogContainerColor(): Color = if (YubeixTheme.colorScheme.background.luminance() < 0.5f) {
    CupertinoAlertDialogDarkContainerColor
} else {
    CupertinoAlertDialogLightContainerColor
}

private val WindowDialogInsideMargin = DpSize(18.dp, 18.dp)
