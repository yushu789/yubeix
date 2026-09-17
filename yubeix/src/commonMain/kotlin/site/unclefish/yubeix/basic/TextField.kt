// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.unclefish.yubeix.theme.LocalContentColor
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixShape

/**
 * A Cupertino style [TextField] backed by [TextFieldState].
 *
 * The field renders as a rounded container with a hairline border whose color animates between
 * the unfocused divider color, the focused [borderColor] and the error color, alongside animated
 * text, cursor, selection and icon colors. Text, cursor and selection colors animate on focus,
 * error and enabled state changes.
 *
 * @param state The [TextFieldState] to be shown in the text field.
 * @param modifier The modifier to be applied to the [TextField].
 * @param insideMargin The margin inside the [TextField].
 * @param backgroundColor The background color of the [TextField].
 * @param cornerRadius The corner radius of the [TextField].
 * @param label The label to be displayed when the [TextField] is empty.
 * @param labelColor The color of the label.
 * @param borderColor The color of the border when the [TextField] is focused.
 * @param useLabelAsPlaceholder Whether to use the label as a placeholder.
 * @param enabled Whether the [TextField] is enabled.
 * @param readOnly Whether the [TextField] is read-only.
 * @param inputTransformation The input transformation to be applied to the [TextField].
 * @param textStyle The text style to be applied to the [TextField].
 * @param keyboardOptions The keyboard options to be applied to the [TextField].
 * @param onKeyboardAction The keyboard action handler for the [TextField].
 * @param lineLimits The line limits for the [TextField].
 * @param leadingIcon The leading icon to be displayed in the [TextField].
 * @param trailingIcon The trailing icon to be displayed in the [TextField].
 * @param onTextLayout The callback to be called when the text layout changes.
 * @param interactionSource The interaction source to be applied to the [TextField].
 * @param cursorBrush The brush to be used for the cursor.
 * @param outputTransformation The output transformation for the text field.
 * @param scrollState The scroll state for the text field.
 * @param isError Whether the [TextField] is in error state; error colors animate in and take
 *   precedence over focused ones.
 * @param bordered Whether to draw the hairline border. When `false` the field is borderless and
 *   only shows its background container.
 */
@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    insideMargin: DpSize = TextFieldDefaults.InsideMargin,
    backgroundColor: Color = YubeixTheme.colorScheme.surface,
    cornerRadius: Dp = TextFieldDefaults.CornerRadius,
    label: String = "",
    labelColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
    borderColor: Color = YubeixTheme.colorScheme.primary,
    useLabelAsPlaceholder: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    inputTransformation: InputTransformation? = null,
    textStyle: TextStyle = YubeixTheme.textStyles.main,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(borderColor),
    outputTransformation: OutputTransformation? = null,
    scrollState: ScrollState = rememberScrollState(),
    isError: Boolean = false,
    bordered: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = YubeixTheme.colorScheme.onSurface,
        unfocusedColor = YubeixTheme.colorScheme.onSurface,
        disabledColor = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        errorColor = YubeixTheme.colorScheme.error,
    )
    val resolvedTextColor = textStyle.color.takeOrElse { textColorState.value }
    val resolvedTextStyle = remember(textStyle, resolvedTextColor) {
        textStyle.copy(resolvedTextColor)
    }
    val indicatorColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = borderColor,
        unfocusedColor = YubeixTheme.colorScheme.dividerLine,
        disabledColor = YubeixTheme.colorScheme.dividerLine.copy(alpha = 0.55f),
        errorColor = YubeixTheme.colorScheme.error,
    )
    val iconColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
        unfocusedColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
        disabledColor = YubeixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.55f),
        errorColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
    )
    val selectionHandleColor = if (isError) YubeixTheme.colorScheme.error else YubeixTheme.colorScheme.primary
    val textSelectionColors = remember(selectionHandleColor) {
        TextSelectionColors(selectionHandleColor, selectionHandleColor.copy(alpha = 0.25f))
    }
    val effectiveCursorBrush = if (isError) SolidColor(YubeixTheme.colorScheme.error) else cursorBrush

    val borderShape = yubeixShape(cornerRadius)
    val labelState by remember(label, useLabelAsPlaceholder) {
        derivedStateOf {
            when {
                label.isEmpty() -> LabelAnimState.Hidden
                useLabelAsPlaceholder && state.text.isNotEmpty() -> LabelAnimState.Placeholder
                state.text.isNotEmpty() -> LabelAnimState.Floating
                else -> LabelAnimState.Normal
            }
        }
    }
    val labelAnim by animateDpAsState(
        when (labelState) {
            LabelAnimState.Floating -> -insideMargin.height / 2
            LabelAnimState.Placeholder, LabelAnimState.Normal -> 0.dp
            LabelAnimState.Hidden -> 0.dp
        },
    )
    val labelFontSize by animateDpAsState(
        when (labelState) {
            LabelAnimState.Floating -> TextFieldDefaults.LabelFontSizeFloating
            else -> TextFieldDefaults.LabelFontSizeNormal
        },
    )
    val paddingModifier = remember(leadingIcon, trailingIcon, insideMargin) {
        when {
            leadingIcon == null && trailingIcon == null -> Modifier.padding(insideMargin.width, vertical = insideMargin.height)
            leadingIcon == null -> Modifier.padding(start = insideMargin.width).padding(vertical = insideMargin.height)
            trailingIcon == null -> Modifier.padding(end = insideMargin.width).padding(vertical = insideMargin.height)
            else -> Modifier.padding(vertical = insideMargin.height)
        }
    }

    val currentOnTextLayout by rememberUpdatedState(onTextLayout)

    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        BasicTextField(
            state = state,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = resolvedTextStyle,
            cursorBrush = effectiveCursorBrush,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            onTextLayout = currentOnTextLayout,
            interactionSource = interactionSource,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            scrollState = scrollState,
            decorator = TextFieldDecorator { innerTextField ->
                TextFieldDecorationBox(
                    label = label,
                    labelFontSize = labelFontSize,
                    labelColor = labelColor,
                    labelState = labelState,
                    backgroundColor = backgroundColor,
                    borderColor = { indicatorColorState.value },
                    bordered = bordered,
                    borderShape = borderShape,
                    paddingModifier = paddingModifier,
                    iconColor = { iconColorState.value },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    labelAnim = labelAnim,
                    insideMargin = insideMargin,
                    innerTextField = innerTextField,
                )
            },
        )
    }
}

/**
 * A Cupertino style [TextField].
 *
 * The field renders as a rounded container with a hairline border whose color animates between
 * the unfocused divider color, the focused [borderColor] and the error color, alongside animated
 * text, cursor, selection and icon colors.
 *
 * @param value The input [TextFieldValue] to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates values in
 *   [TextFieldValue]. An updated [TextFieldValue] comes as a parameter of the callback.
 * @param modifier The modifier to be applied to the [TextField].
 * @param insideMargin The margin inside the [TextField].
 * @param backgroundColor The background color of the [TextField].
 * @param cornerRadius The corner radius of the [TextField].
 * @param label The label to be displayed when the [TextField] is empty.
 * @param labelColor The color of the label.
 * @param borderColor The color of the border when the [TextField] is focused.
 * @param useLabelAsPlaceholder Whether to use the label as a placeholder.
 * @param enabled Whether the [TextField] is enabled.
 * @param readOnly Whether the [TextField] is read-only.
 * @param textStyle The text style to be applied to the [TextField].
 * @param keyboardOptions The keyboard options to be applied to the [TextField].
 * @param keyboardActions The keyboard actions to be applied to the [TextField].
 * @param leadingIcon The leading icon to be displayed in the [TextField].
 * @param trailingIcon The trailing icon to be displayed in the [TextField].
 * @param singleLine Whether the text field is single line.
 * @param maxLines The maximum number of lines allowed to be displayed in [TextField].
 * @param minLines The minimum number of lines allowed to be displayed in [TextField]. It is required
 *   that 1 <= [minLines] <= [maxLines].
 * @param visualTransformation The visual transformation to be applied to the [TextField].
 * @param onTextLayout The callback to be called when the text layout changes.
 * @param interactionSource The interaction source to be applied to the [TextField].
 * @param cursorBrush The brush to be used for the cursor.
 * @param isError Whether the [TextField] is in error state; error colors animate in and take
 *   precedence over focused ones.
 * @param bordered Whether to draw the hairline border. When `false` the field is borderless and
 *   only shows its background container.
 */
@Composable
fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    insideMargin: DpSize = TextFieldDefaults.InsideMargin,
    backgroundColor: Color = YubeixTheme.colorScheme.surface,
    cornerRadius: Dp = TextFieldDefaults.CornerRadius,
    label: String = "",
    labelColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
    borderColor: Color = YubeixTheme.colorScheme.primary,
    useLabelAsPlaceholder: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = YubeixTheme.textStyles.main,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(YubeixTheme.colorScheme.primary),
    isError: Boolean = false,
    bordered: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = YubeixTheme.colorScheme.onSurface,
        unfocusedColor = YubeixTheme.colorScheme.onSurface,
        disabledColor = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        errorColor = YubeixTheme.colorScheme.error,
    )
    val resolvedTextColor = textStyle.color.takeOrElse { textColorState.value }
    val resolvedTextStyle = remember(textStyle, resolvedTextColor) {
        textStyle.copy(resolvedTextColor)
    }
    val indicatorColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = borderColor,
        unfocusedColor = YubeixTheme.colorScheme.dividerLine,
        disabledColor = YubeixTheme.colorScheme.dividerLine.copy(alpha = 0.55f),
        errorColor = YubeixTheme.colorScheme.error,
    )
    val iconColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
        unfocusedColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
        disabledColor = YubeixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.55f),
        errorColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
    )
    val selectionHandleColor = if (isError) YubeixTheme.colorScheme.error else YubeixTheme.colorScheme.primary
    val textSelectionColors = remember(selectionHandleColor) {
        TextSelectionColors(selectionHandleColor, selectionHandleColor.copy(alpha = 0.25f))
    }
    val effectiveCursorBrush = if (isError) SolidColor(YubeixTheme.colorScheme.error) else cursorBrush

    val borderShape = yubeixShape(cornerRadius)
    val labelState = remember(value.text, label, useLabelAsPlaceholder) {
        when {
            label.isEmpty() -> LabelAnimState.Hidden
            useLabelAsPlaceholder && value.text.isNotEmpty() -> LabelAnimState.Placeholder
            value.text.isNotEmpty() -> LabelAnimState.Floating
            else -> LabelAnimState.Normal
        }
    }
    val labelAnim by animateDpAsState(
        when (labelState) {
            LabelAnimState.Floating -> -insideMargin.height / 2
            LabelAnimState.Placeholder, LabelAnimState.Normal -> 0.dp
            LabelAnimState.Hidden -> 0.dp
        },
    )
    val labelFontSize by animateDpAsState(
        when (labelState) {
            LabelAnimState.Floating -> TextFieldDefaults.LabelFontSizeFloating
            else -> TextFieldDefaults.LabelFontSizeNormal
        },
    )
    val paddingModifier = remember(leadingIcon, trailingIcon, insideMargin) {
        when {
            leadingIcon == null && trailingIcon == null -> Modifier.padding(insideMargin.width, vertical = insideMargin.height)
            leadingIcon == null -> Modifier.padding(start = insideMargin.width).padding(vertical = insideMargin.height)
            trailingIcon == null -> Modifier.padding(end = insideMargin.width).padding(vertical = insideMargin.height)
            else -> Modifier.padding(vertical = insideMargin.height)
        }
    }

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnTextLayout by rememberUpdatedState(onTextLayout)

    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = currentOnValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = resolvedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = currentOnTextLayout,
            interactionSource = interactionSource,
            cursorBrush = effectiveCursorBrush,
            decorationBox = @Composable { innerTextField ->
                TextFieldDecorationBox(
                    label = label,
                    labelFontSize = labelFontSize,
                    labelColor = labelColor,
                    labelState = labelState,
                    backgroundColor = backgroundColor,
                    borderColor = { indicatorColorState.value },
                    bordered = bordered,
                    borderShape = borderShape,
                    paddingModifier = paddingModifier,
                    iconColor = { iconColorState.value },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    labelAnim = labelAnim,
                    insideMargin = insideMargin,
                    innerTextField = innerTextField,
                )
            },
        )
    }
}

/**
 * A Cupertino style text field.
 *
 * The field renders as a rounded container with a hairline border whose color animates between
 * the unfocused divider color, the focused [borderColor] and the error color, alongside animated
 * text, cursor, selection and icon colors.
 *
 * @param value The text to be displayed in the text field.
 * @param onValueChange The callback to be called when the value changes.
 * @param modifier The modifier to be applied to the [TextField].
 * @param insideMargin The margin inside the [TextField].
 * @param backgroundColor The background color of the [TextField].
 * @param cornerRadius The corner radius of the [TextField].
 * @param label The label to be displayed when the [TextField] is empty.
 * @param labelColor The color of the label.
 * @param borderColor The color of the border when the [TextField] is focused.
 * @param useLabelAsPlaceholder Whether to use the label as a placeholder.
 * @param enabled Whether the [TextField] is enabled.
 * @param readOnly Whether the [TextField] is read-only.
 * @param textStyle The text style to be applied to the [TextField].
 * @param keyboardOptions The keyboard options to be applied to the [TextField].
 * @param keyboardActions The keyboard actions to be applied to the [TextField].
 * @param leadingIcon The leading icon to be displayed in the [TextField].
 * @param trailingIcon The trailing icon to be displayed in the [TextField].
 * @param singleLine Whether the text field is single line.
 * @param maxLines The maximum number of lines allowed to be displayed in [TextField].
 * @param minLines The minimum number of lines allowed to be displayed in [TextField]. It is required
 *   that 1 <= [minLines] <= [maxLines].
 * @param visualTransformation The visual transformation to be applied to the [TextField].
 * @param onTextLayout The callback to be called when the text layout changes.
 * @param interactionSource The interaction source to be applied to the [TextField].
 * @param cursorBrush The brush to be used for the cursor.
 * @param isError Whether the [TextField] is in error state; error colors animate in and take
 *   precedence over focused ones.
 * @param bordered Whether to draw the hairline border. When `false` the field is borderless and
 *   only shows its background container.
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    insideMargin: DpSize = TextFieldDefaults.InsideMargin,
    backgroundColor: Color = YubeixTheme.colorScheme.surface,
    cornerRadius: Dp = TextFieldDefaults.CornerRadius,
    label: String = "",
    labelColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
    borderColor: Color = YubeixTheme.colorScheme.primary,
    useLabelAsPlaceholder: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = YubeixTheme.textStyles.main,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(YubeixTheme.colorScheme.primary),
    isError: Boolean = false,
    bordered: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = YubeixTheme.colorScheme.onSurface,
        unfocusedColor = YubeixTheme.colorScheme.onSurface,
        disabledColor = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        errorColor = YubeixTheme.colorScheme.error,
    )
    val resolvedTextColor = textStyle.color.takeOrElse { textColorState.value }
    val resolvedTextStyle = remember(textStyle, resolvedTextColor) {
        textStyle.copy(resolvedTextColor)
    }
    val indicatorColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = borderColor,
        unfocusedColor = YubeixTheme.colorScheme.dividerLine,
        disabledColor = YubeixTheme.colorScheme.dividerLine.copy(alpha = 0.55f),
        errorColor = YubeixTheme.colorScheme.error,
    )
    val iconColorState = animatedStateColor(
        enabled = enabled,
        isError = isError,
        isFocused = isFocused,
        focusedColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
        unfocusedColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
        disabledColor = YubeixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.55f),
        errorColor = YubeixTheme.colorScheme.onSurfaceVariantActions,
    )
    val selectionHandleColor = if (isError) YubeixTheme.colorScheme.error else YubeixTheme.colorScheme.primary
    val textSelectionColors = remember(selectionHandleColor) {
        TextSelectionColors(selectionHandleColor, selectionHandleColor.copy(alpha = 0.25f))
    }
    val effectiveCursorBrush = if (isError) SolidColor(YubeixTheme.colorScheme.error) else cursorBrush

    val borderShape = yubeixShape(cornerRadius)
    val labelState = remember(value, label, useLabelAsPlaceholder) {
        when {
            label.isEmpty() -> LabelAnimState.Hidden
            useLabelAsPlaceholder && value.isNotEmpty() -> LabelAnimState.Placeholder
            value.isNotEmpty() -> LabelAnimState.Floating
            else -> LabelAnimState.Normal
        }
    }
    val labelAnim by animateDpAsState(
        when (labelState) {
            LabelAnimState.Floating -> -insideMargin.height / 2
            LabelAnimState.Placeholder, LabelAnimState.Normal -> 0.dp
            LabelAnimState.Hidden -> 0.dp
        },
    )
    val labelFontSize by animateDpAsState(
        when (labelState) {
            LabelAnimState.Floating -> TextFieldDefaults.LabelFontSizeFloating
            else -> TextFieldDefaults.LabelFontSizeNormal
        },
    )
    val paddingModifier = remember(leadingIcon, trailingIcon, insideMargin) {
        when {
            leadingIcon == null && trailingIcon == null -> Modifier.padding(insideMargin.width, vertical = insideMargin.height)
            leadingIcon == null -> Modifier.padding(start = insideMargin.width).padding(vertical = insideMargin.height)
            trailingIcon == null -> Modifier.padding(end = insideMargin.width).padding(vertical = insideMargin.height)
            else -> Modifier.padding(vertical = insideMargin.height)
        }
    }

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnTextLayout by rememberUpdatedState(onTextLayout)

    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = currentOnValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = resolvedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = currentOnTextLayout,
            interactionSource = interactionSource,
            cursorBrush = effectiveCursorBrush,
            decorationBox = @Composable { innerTextField ->
                TextFieldDecorationBox(
                    label = label,
                    labelFontSize = labelFontSize,
                    labelColor = labelColor,
                    labelState = labelState,
                    backgroundColor = backgroundColor,
                    borderColor = { indicatorColorState.value },
                    bordered = bordered,
                    borderShape = borderShape,
                    paddingModifier = paddingModifier,
                    iconColor = { iconColorState.value },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    labelAnim = labelAnim,
                    insideMargin = insideMargin,
                    innerTextField = innerTextField,
                )
            },
        )
    }
}

private enum class LabelAnimState { Hidden, Placeholder, Normal, Floating }

/** Contains default values used by [TextField]. */
object TextFieldDefaults {
    /** The default corner radius of the [TextField]. */
    val CornerRadius = 10.dp

    /** The default inside margin of the [TextField]. */
    val InsideMargin = DpSize(14.dp, 10.dp)

    /** The width of the [TextField] border. */
    internal val StrokeWidth = 1.dp

    /** The label font size when the label is floating above the text. */
    internal val LabelFontSizeFloating = 10.dp

    /** The label font size when the label is in its normal position. */
    internal val LabelFontSizeNormal = 17.dp
}

/**
 * A Cupertino style decoration box for the [TextField] component: a rounded container with an
 * animated hairline border, leading/trailing icons tinted by an animated icon color, and the
 * floating label / placeholder machinery on top of the inner text field.
 */
@Composable
private fun TextFieldDecorationBox(
    label: String,
    labelFontSize: Dp,
    labelColor: Color,
    labelState: LabelAnimState,
    backgroundColor: Color,
    borderColor: () -> Color,
    bordered: Boolean,
    borderShape: Shape,
    iconColor: () -> Color,
    paddingModifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    labelAnim: Dp = 0.dp,
    insideMargin: DpSize = TextFieldDefaults.InsideMargin,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, borderShape)
            .then(
                if (bordered) {
                    Modifier.border(TextFieldDefaults.StrokeWidth, borderColor(), borderShape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(vertical = 2.dp)) {
                    CompositionLocalProvider(LocalContentColor provides iconColor()) {
                        leadingIcon()
                    }
                }
            }
            Box(
                modifier = Modifier.weight(1f).then(paddingModifier),
                contentAlignment = Alignment.TopStart,
            ) {
                if (labelState != LabelAnimState.Hidden && labelState != LabelAnimState.Placeholder) {
                    Text(
                        text = label,
                        fontSize = labelFontSize.value.sp,
                        color = labelColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.offset { IntOffset(0, labelAnim.roundToPx()) },
                        textAlign = TextAlign.Start,
                    )
                }
                Box(
                    modifier = Modifier.offset(y = if (labelState == LabelAnimState.Floating) insideMargin.height / 2 else 0.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    innerTextField()
                }
            }
            if (trailingIcon != null) {
                Box(modifier = Modifier.padding(vertical = 2.dp)) {
                    CompositionLocalProvider(LocalContentColor provides iconColor()) {
                        trailingIcon()
                    }
                }
            }
        }
    }
}

/**
 * Resolves the color for a focus/error/enabled state and animates towards it, matching the
 * Cupertino text field behavior where state transitions fade instead of snapping.
 */
@Composable
private fun animatedStateColor(
    enabled: Boolean,
    isError: Boolean,
    isFocused: Boolean,
    focusedColor: Color,
    unfocusedColor: Color,
    disabledColor: Color,
    errorColor: Color,
): State<Color> {
    val target = when {
        !enabled -> disabledColor
        isError -> errorColor
        isFocused -> focusedColor
        else -> unfocusedColor
    }
    return animateColorAsState(target, label = "YubeixTextFieldStateColor")
}
