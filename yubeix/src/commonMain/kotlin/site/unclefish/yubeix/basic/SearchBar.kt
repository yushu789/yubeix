// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kyant.shapes.RoundedRectangle
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.MagnifyingGlass
import site.unclefish.yubeix.icon.cupertino.outlined.Xmark
import kotlinx.coroutines.delay
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.basic.Search
import site.unclefish.yubeix.icon.basic.SearchCleanup
import site.unclefish.yubeix.theme.LocalContentColor
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.yubeixCapsuleShape
import site.unclefish.yubeix.utils.hasFocusReassignBug

/**
 * A [SearchBar] component with Yubeix style.
 *
 * @param inputField the input field to input a query in the [SearchBar].
 * @param onExpandedChange the callback to be invoked when the [SearchBar]'s expanded state is
 *   changed.
 * @param modifier the [Modifier] to be applied to the [SearchBar].
 * @param insideMargin The margin inside the [SearchBar].
 * @param expanded whether the [SearchBar] is expanded and showing search results.
 * @param outsideEndAction the action to be shown at the end side of the [SearchBar] when it is
 *   expanded.
 * @param content the content to be shown when the [SearchBar] is expanded.
 */
@Composable
fun SearchBar(
    inputField: @Composable () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    insideMargin: DpSize = SearchBarDefaults.InsideMargin,
    expanded: Boolean = false,
    outsideEndAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = insideMargin.height, horizontal = insideMargin.width),
            ) {
                inputField()
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it }),
            ) {
                outsideEndAction?.invoke()
            }
        }

        AnimatedVisibility(
            visible = expanded,
        ) {
            content()
        }
    }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = expanded,
        onBackCompleted = {
            currentOnExpandedChange(false)
        },
    )
}

/**
 * A text field to input a query in a search bar with Yubeix style.
 *
 * @param query the query text to be shown in the input field.
 * @param onQueryChange the callback to be invoked when the input service updates the query. An
 *   updated text comes as a parameter of the callback.
 * @param onSearch the callback to be invoked when the input service triggers the
 *   [ImeAction.Search] action. The current [query] comes as a parameter of the callback.
 * @param expanded whether the search bar is expanded and showing search results.
 * @param onExpandedChange the callback to be invoked when the search bar's expanded state is
 *   changed.
 * @param modifier the [Modifier] to be applied to this input field.
 * @param label the label to be shown when the input field is not focused.
 * @param enabled the enabled state of this input field. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param textStyle Style configuration that applies at character level such as color, font etc.
 * @param leadingIcon the leading icon to be displayed at the start of the input field.
 * @param trailingIcon the trailing icon to be displayed at the end of the input field.
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this input field. You can use this to change the search bar's
 *   appearance or preview the search bar in different states. Note that if `null` is provided,
 *   interactions will still happen internally.
 */
@Composable
fun InputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val currentOnQueryChange by rememberUpdatedState(onQueryChange)
    val currentOnSearch by rememberUpdatedState(onSearch)
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val internalInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val capsuleShape = yubeixCapsuleShape()

    val actualLeadingIcon = leadingIcon ?: {
        Icon(
            modifier = Modifier.padding(start = SearchBarDefaults.LeadingIconStartPadding, end = SearchBarDefaults.LeadingIconEndPadding),
            imageVector = YubeixIcons.Basic.Search,
            tint = YubeixTheme.colorScheme.onSurfaceContainerHigh,
            contentDescription = "Search",
        )
    }

    val actualTrailingIcon = trailingIcon ?: {
        AnimatedVisibility(
            visible = query.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier.padding(start = SearchBarDefaults.TrailingIconStartPadding, end = SearchBarDefaults.TrailingIconEndPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    modifier = Modifier
                        .clip(capsuleShape)
                        .clickable { currentOnQueryChange("") },
                    imageVector = YubeixIcons.Basic.SearchCleanup,
                    tint = YubeixTheme.colorScheme.onSurfaceContainerHighest,
                    contentDescription = "Search Cleanup",
                )
            }
        }
    }

    val focused = internalInteractionSource.collectIsFocusedAsState().value
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val textAlpha = remember { Animatable(1f) }

    val textColor = LocalContentColor.current
    val inputTextStyle = YubeixTheme.textStyles.main
        .copy(fontWeight = FontWeight.Medium)
        .merge(textStyle)
        .copy(color = textColor)

    val cursorBrush = SolidColor(YubeixTheme.colorScheme.primary)
    val labelText by remember(query, expanded, label) {
        derivedStateOf { if (!(query.isNotEmpty() || expanded)) label else "" }
    }

    // On API 26-27, focus is incorrectly reassigned after clearFocus(), preventing the
    // SearchBar from closing. Workaround: disable the TextField when collapsed and use
    // pointerInput to handle tap-to-expand. https://issuetracker.google.com/issues/433382598
    val workaroundEnabled = !hasFocusReassignBug || expanded
    val expandOnTapModifier = if (workaroundEnabled) {
        Modifier
    } else {
        Modifier.pointerInput(Unit) { detectTapGestures { currentOnExpandedChange(true) } }
    }

    BasicTextField(
        value = query,
        onValueChange = currentOnQueryChange,
        modifier = modifier
            .then(expandOnTapModifier)
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) currentOnExpandedChange(true) }
            .semantics {
                onClick {
                    focusRequester.requestFocus()
                    true
                }
            },
        enabled = enabled && workaroundEnabled,
        singleLine = true,
        textStyle = inputTextStyle,
        cursorBrush = cursorBrush,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { currentOnSearch(query) }),
        interactionSource = internalInteractionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(
                        color = YubeixTheme.colorScheme.surfaceContainerHigh,
                        shape = capsuleShape,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actualLeadingIcon()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = SearchBarDefaults.InputFieldMinHeight),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val mergedLabelStyle = remember(textStyle) {
                            TextStyle(fontSize = SearchBarDefaults.InputFieldFontSize, fontWeight = FontWeight.Medium).merge(textStyle)
                        }
                        Text(
                            text = labelText,
                            style = mergedLabelStyle,
                            color = YubeixTheme.colorScheme.onSurfaceContainerHigh,
                        )
                        Box(modifier = Modifier.graphicsLayer { alpha = textAlpha.value }) {
                            innerTextField()
                        }
                    }
                    actualTrailingIcon()
                }
            }
        },
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            // Explicitly request focus when expanded. On API 26-27, the workaround disables
            // the TextField when collapsed, so the initial tap doesn't grant focus — this
            // ensures the keyboard appears after the TextField becomes enabled again.
            focusRequester.requestFocus()
        } else if (focused) {
            delay(100)
            if (query.isNotEmpty()) {
                textAlpha.animateTo(0f)
                currentOnQueryChange("")
                textAlpha.snapTo(1f)
            }
            focusManager.clearFocus()
        }
    }
}

/** Contains default values used by [SearchBar] and [InputField]. */
object SearchBarDefaults {
    /** The default inside margin of the [SearchBar]. */
    val InsideMargin = DpSize(12.dp, 0.dp)

    /** The default minimum height of the [InputField]. */
    val InputFieldMinHeight = 45.dp

    /** The default font size for the [InputField] label. */
    val InputFieldFontSize = 17.sp

    /** The start padding for the default leading icon. */
    val LeadingIconStartPadding = 16.dp

    /** The end padding for the default leading icon. */
    val LeadingIconEndPadding = 8.dp

    /** The start padding for the default trailing icon. */
    val TrailingIconStartPadding = 8.dp

    /** The end padding for the default trailing icon. */
    val TrailingIconEndPadding = 16.dp
}

/**
 * How the placeholder of a [SearchField] is aligned while the query is empty.
 */
enum class SearchFieldPlaceholderAlignment {
    /** The placeholder group sits at the start edge, next to the magnifying glass icon. */
    Start,

    /** The placeholder group is centered in the field, like the iOS system search field. */
    Center,
}

private val SearchFieldPlaceholderAlignment.startProgress: Float
    get() = when (this) {
        SearchFieldPlaceholderAlignment.Start -> 1f
        SearchFieldPlaceholderAlignment.Center -> 0f
    }

/** Contains default values used by [SearchField]. */
object SearchFieldDefaults {
    /** The default height of the [SearchField]. */
    val Height = 36.dp

    /** The default shape of the [SearchField]. */
    val Shape: Shape = RoundedRectangle(10.dp)

    /** The default horizontal padding of the [SearchField] content. */
    val HorizontalPadding = 14.dp

    /** The end padding of the [SearchField] content when the clear button is visible. */
    val ClearButtonEndPadding = 4.dp

    /** The touch target size of the clear button. */
    val ClearButtonTouchSize = 28.dp

    /** The visible chip size of the clear button. */
    val ClearButtonChipSize = 18.dp

    /** The icon size inside the clear button chip. */
    val ClearButtonIconSize = 8.dp

    /** The size of the magnifying glass icon. */
    val IconSize = 21.dp

    /** The gap between the magnifying glass icon and the placeholder or query text. */
    val PlaceholderGap = 8.dp

    /** The default container color of the [SearchField]. */
    @Composable
    fun containerColor(): Color {
        return YubeixTheme.colorScheme.surfaceContainerHigh
    }
}

/**
 * An iOS-style search field with a centered placeholder that slides to the start edge once the
 * query is typed into.
 *
 * The magnifying glass icon always leads the content; while the query is empty the placeholder is
 * aligned per [placeholderAlignment] (centered by default, sliding towards the start edge as
 * [placeholderStartProgress] goes from 0f to 1f), and once there is a query the text follows the
 * icon at the start edge with an optional clear button at the end.
 *
 * @param query the query text to be shown in the field.
 * @param onQueryChange the callback to be invoked when the input service updates the query. An
 *   updated text comes as a parameter of the callback.
 * @param placeholder the placeholder shown while the query is empty.
 * @param modifier the [Modifier] to be applied to the field.
 * @param enabled the enabled state of this field. When `false`, this component will not respond to
 *   user input, and it will appear visually disabled and disabled to accessibility services.
 * @param onSearch the callback to be invoked when the input service triggers the search action.
 * @param onClear the callback invoked when the clear button is tapped. When `null` the clear button
 *   is not shown at all; when non-null the button is visible whenever [query] is not empty and
 *   [queryContentVisible] is true. The query itself is not cleared automatically - clear it inside
 *   the callback.
 * @param placeholderAlignment where the placeholder group sits while the query is empty.
 * @param placeholderStartProgress a continuous override of the placeholder position from centered
 *   (0f) to start-aligned (1f), for hosts that animate the alignment themselves.
 * @param queryContentVisible whether the query text is drawn. Hosts can hide it during their own
 *   transitions; when false the placeholder stays visible regardless of [query].
 * @param height the height of the field.
 * @param shape the shape of the field container.
 * @param horizontalPadding the horizontal padding of the field content.
 * @param iconSize the size of the magnifying glass icon.
 * @param placeholderGap the gap between the icon and the placeholder or query text.
 * @param containerColor the background color of the field.
 * @param textStyle the text style of the query and the placeholder.
 * @param textColor the color of the query text.
 * @param placeholderColor the color of the placeholder text.
 * @param iconColor the tint of the magnifying glass icon.
 * @param cursorColor the color of the text cursor.
 * @param clearButtonContentDescription the content description of the clear button, for
 *   accessibility services.
 * @param textFieldModifier the [Modifier] applied to the inner text field, after the fill and
 *   padding of the field content.
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSearch: () -> Unit = {},
    onClear: (() -> Unit)? = null,
    placeholderAlignment: SearchFieldPlaceholderAlignment = SearchFieldPlaceholderAlignment.Center,
    placeholderStartProgress: Float = placeholderAlignment.startProgress,
    queryContentVisible: Boolean = query.isNotBlank(),
    height: Dp = SearchFieldDefaults.Height,
    shape: Shape = SearchFieldDefaults.Shape,
    horizontalPadding: Dp = SearchFieldDefaults.HorizontalPadding,
    iconSize: Dp = SearchFieldDefaults.IconSize,
    placeholderGap: Dp = SearchFieldDefaults.PlaceholderGap,
    containerColor: Color = SearchFieldDefaults.containerColor(),
    textStyle: TextStyle = YubeixTheme.textStyles.main,
    textColor: Color = YubeixTheme.colorScheme.onSurface,
    placeholderColor: Color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
    iconColor: Color = placeholderColor,
    cursorColor: Color = YubeixTheme.colorScheme.primary,
    clearButtonContentDescription: String = "Clear",
    textFieldModifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val clampedPlaceholderStartProgress = placeholderStartProgress.coerceIn(0f, 1f)
    val placeholderTextWidthPx = remember(placeholder, textStyle, textMeasurer) {
        textMeasurer.measure(
            text = AnnotatedString(placeholder),
            style = textStyle,
            maxLines = 1,
        ).size.width.toFloat()
    }
    val placeholderGroupWidthPx = with(density) {
        iconSize.toPx() + placeholderGap.toPx() + placeholderTextWidthPx
    }
    val showClearButton = onClear != null && query.isNotEmpty() && queryContentVisible
    val currentOnClear by rememberUpdatedState(onClear)
    val endPadding = if (showClearButton) {
        SearchFieldDefaults.ClearButtonEndPadding
    } else {
        horizontalPadding
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxSize()
                .then(textFieldModifier)
                .padding(start = horizontalPadding, end = endPadding),
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.copy(color = textColor),
            cursorBrush = SolidColor(cursorColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val placeholderTranslationX = with(density) {
                        ((maxWidth.toPx() - placeholderGroupWidthPx) / 2f)
                            .coerceAtLeast(0f) * (1f - clampedPlaceholderStartProgress)
                    }

                    if (!queryContentVisible) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .graphicsLayer {
                                    translationX = placeholderTranslationX
                                },
                            horizontalArrangement = Arrangement.spacedBy(placeholderGap),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = CupertinoIcons.Outlined.MagnifyingGlass,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(iconSize),
                            )
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = placeholderColor,
                                maxLines = 1,
                            )
                        }
                    }

                    if (queryContentVisible) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(placeholderGap),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = CupertinoIcons.Outlined.MagnifyingGlass,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(iconSize),
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                innerTextField()
                            }
                            if (showClearButton) {
                                SearchFieldClearButton(
                                    onClick = { currentOnClear?.invoke() },
                                    contentDescription = clearButtonContentDescription,
                                    touchSize = SearchFieldDefaults.ClearButtonTouchSize,
                                    chipSize = SearchFieldDefaults.ClearButtonChipSize,
                                    iconSize = SearchFieldDefaults.ClearButtonIconSize,
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    translationX = placeholderTranslationX
                                    alpha = if (query.isBlank()) 1f else 0f
                                }
                                .padding(start = iconSize + placeholderGap),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            innerTextField()
                        }
                    }
                }
            },
        )
    }
}

/**
 * The circular clear chip at the end of a [SearchField]: a small translucent disc with an xmark,
 * hosted inside a larger invisible touch target.
 */
@Composable
private fun SearchFieldClearButton(
    onClick: () -> Unit,
    contentDescription: String,
    touchSize: Dp,
    chipSize: Dp,
    iconSize: Dp,
) {
    val colors = YubeixTheme.colorScheme
    Box(
        modifier = Modifier
            .size(touchSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(chipSize)
                .clip(CircleShape)
                .background(colors.onSurface.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = CupertinoIcons.Outlined.Xmark,
                contentDescription = contentDescription,
                tint = colors.surface,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
