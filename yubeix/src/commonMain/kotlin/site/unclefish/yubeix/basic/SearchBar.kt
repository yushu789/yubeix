// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.basic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.delay
import site.unclefish.yubeix.anim.bounceSpring
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.MagnifyingGlass
import site.unclefish.yubeix.icon.cupertino.outlined.Xmark
import site.unclefish.yubeix.theme.YubeixTheme
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
 * A text field to input a query in a search bar, rendered like the iOS system search field: a grey
 * rounded container with a leading magnifying glass, a placeholder that rests centered and slides
 * to the start edge while the bar expands, and a circular clear chip once a query is typed.
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
 * @param label the placeholder to be shown while the query is empty.
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
    val currentQuery by rememberUpdatedState(query)
    val internalInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused = internalInteractionSource.collectIsFocusedAsState().value
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val baseTextStyle = YubeixTheme.textStyles.main
    val resolvedTextStyle = remember(textStyle, baseTextStyle) {
        if (textStyle == null) {
            baseTextStyle
        } else {
            baseTextStyle.merge(textStyle)
        }
    }

    // Like the iOS system search field, the placeholder rests centered while the bar is collapsed
    // and slides to the start edge while it expands.
    val placeholderStartProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = bounceSpring(durationMillis = PLACEHOLDER_SLIDE_DURATION_MILLIS),
        label = "searchBarPlaceholderStartProgress",
    )

    val resolvedLeading: @Composable () -> Unit = leadingIcon ?: {
        Icon(
            imageVector = CupertinoIcons.Outlined.MagnifyingGlass,
            contentDescription = null,
            tint = YubeixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.size(SearchFieldDefaults.IconSize),
        )
    }
    var measuredLeadingWidth by remember { mutableStateOf(SearchFieldDefaults.IconSize) }
    val resolvedTrailing: (@Composable () -> Unit)? = trailingIcon ?: if (query.isNotEmpty()) {
        {
            SearchFieldClearButton(
                onClick = { currentOnQueryChange("") },
                contentDescription = "Clear",
                touchSize = SearchFieldDefaults.ClearButtonTouchSize,
                chipSize = SearchFieldDefaults.ClearButtonChipSize,
                iconSize = SearchFieldDefaults.ClearButtonIconSize,
            )
        }
    } else {
        null
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

    CupertinoSearchField(
        query = query,
        onQueryChange = { currentOnQueryChange(it) },
        placeholder = label,
        fieldModifier = modifier
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
        onSearch = { currentOnSearch(query) },
        leadingContent = {
            Box(
                modifier = Modifier.onSizeChanged {
                    measuredLeadingWidth = with(density) { it.width.toDp() }
                },
            ) {
                resolvedLeading()
            }
        },
        leadingWidth = if (leadingIcon != null) measuredLeadingWidth else SearchFieldDefaults.IconSize,
        trailingContent = resolvedTrailing,
        placeholderStartProgress = placeholderStartProgress,
        queryContentVisible = query.isNotBlank(),
        containerColor = SearchFieldDefaults.containerColor(),
        textStyle = resolvedTextStyle,
        textColor = YubeixTheme.colorScheme.onSurface,
        placeholderColor = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        cursorColor = YubeixTheme.colorScheme.primary,
        interactionSource = internalInteractionSource,
        height = SearchFieldDefaults.Height,
        shape = SearchFieldDefaults.Shape,
        horizontalPadding = SearchFieldDefaults.HorizontalPadding,
        placeholderGap = SearchFieldDefaults.PlaceholderGap,
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            // Explicitly request focus when expanded. On API 26-27, the workaround disables
            // the TextField when collapsed, so the initial tap doesn't grant focus — this
            // ensures the keyboard appears after the TextField becomes enabled again.
            focusRequester.requestFocus()
        } else if (focused) {
            delay(100)
            if (currentQuery.isNotEmpty()) {
                currentOnQueryChange("")
            }
            focusManager.clearFocus()
        }
    }
}

/** Contains default values used by [SearchBar] and [InputField]. */
object SearchBarDefaults {
    /** The default inside margin of the [SearchBar]. */
    val InsideMargin = DpSize(12.dp, 0.dp)

    /** The default minimum height of the [InputField]. Kept for compatibility; the field now uses [SearchFieldDefaults.Height]. */
    val InputFieldMinHeight = 45.dp

    /** The default font size for the [InputField] label. Kept for compatibility. */
    val InputFieldFontSize = 17.sp

    /** The start padding for the default leading icon. Kept for compatibility. */
    val LeadingIconStartPadding = 16.dp

    /** The end padding for the default leading icon. Kept for compatibility. */
    val LeadingIconEndPadding = 8.dp

    /** The start padding for the default trailing icon. Kept for compatibility. */
    val TrailingIconStartPadding = 8.dp

    /** The end padding for the default trailing icon. Kept for compatibility. */
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
    fun containerColor(): Color = YubeixTheme.colorScheme.surfaceContainerHigh
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
 * @param textFieldModifier the [Modifier] applied to the inner text field, after the fill and
 *   padding of the field content.
 * @param enabled the enabled state of this field. When `false`, this field will not respond to
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
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
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
) {
    val currentOnClear by rememberUpdatedState(onClear)
    val showClearButton = onClear != null && query.isNotEmpty() && queryContentVisible

    CupertinoSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        modifier = modifier,
        textFieldModifier = textFieldModifier,
        enabled = enabled,
        onSearch = onSearch,
        leadingContent = {
            Icon(
                imageVector = CupertinoIcons.Outlined.MagnifyingGlass,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
        },
        leadingWidth = iconSize,
        trailingContent = if (showClearButton) {
            {
                SearchFieldClearButton(
                    onClick = { currentOnClear?.invoke() },
                    contentDescription = clearButtonContentDescription,
                    touchSize = SearchFieldDefaults.ClearButtonTouchSize,
                    chipSize = SearchFieldDefaults.ClearButtonChipSize,
                    iconSize = SearchFieldDefaults.ClearButtonIconSize,
                )
            }
        } else {
            null
        },
        placeholderStartProgress = placeholderStartProgress,
        queryContentVisible = queryContentVisible,
        height = height,
        shape = shape,
        horizontalPadding = horizontalPadding,
        placeholderGap = placeholderGap,
        containerColor = containerColor,
        textStyle = textStyle,
        textColor = textColor,
        placeholderColor = placeholderColor,
        cursorColor = cursorColor,
        interactionSource = remember { MutableInteractionSource() },
    )
}

/**
 * The shared drawing engine behind [SearchField] and [InputField]: a grey rounded container with a
 * leading icon, a placeholder that can rest centered and slide to the start edge, the query text,
 * and optional trailing content such as the circular clear chip.
 *
 * @param query the query text to be shown in the field.
 * @param onQueryChange the callback to be invoked when the input service updates the query.
 * @param placeholder the placeholder shown while the query is empty.
 * @param modifier the [Modifier] to be applied to the field container, which owns the field's
 *   visual size, shape and color.
 * @param fieldModifier the [Modifier] applied to the inner text field before it fills the
 *   container; hosts that let callers own the field's layout put their sizing and focus modifiers
 *   here.
 * @param textFieldModifier the [Modifier] applied to the inner text field, after the fill and
 *   padding of the field content.
 * @param enabled the enabled state of the inner text field.
 * @param onSearch the callback invoked when the input service triggers the search action.
 * @param leadingContent the content drawn at the start of both the placeholder row and the query
 *   row, typically the magnifying glass icon.
 * @param leadingWidth the estimated width of [leadingContent], used to center the placeholder group
 *   and to indent the hidden query text.
 * @param trailingContent the optional content drawn at the end of the query row, e.g. the clear
 *   chip; when present the query row reserves [SearchFieldDefaults.ClearButtonEndPadding] for it.
 * @param placeholderStartProgress the placeholder position from centered (0f) to start-aligned
 *   (1f).
 * @param queryContentVisible whether the query text is drawn; when false the placeholder row is
 *   drawn instead and the query text is hidden.
 * @param height the height of the field container.
 * @param shape the shape of the field container.
 * @param horizontalPadding the horizontal padding of the field content.
 * @param placeholderGap the gap between the leading content and the placeholder or query text.
 * @param containerColor the background color of the field container.
 * @param textStyle the text style of the query and the placeholder.
 * @param textColor the color of the query text.
 * @param placeholderColor the color of the placeholder text.
 * @param cursorColor the color of the text cursor.
 * @param interactionSource the [MutableInteractionSource] of the inner text field.
 */
@Composable
private fun CupertinoSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    onSearch: () -> Unit,
    leadingContent: @Composable () -> Unit,
    leadingWidth: Dp,
    trailingContent: (@Composable () -> Unit)?,
    placeholderStartProgress: Float,
    queryContentVisible: Boolean,
    height: Dp,
    shape: Shape,
    horizontalPadding: Dp,
    placeholderGap: Dp,
    containerColor: Color,
    textStyle: TextStyle,
    textColor: Color,
    placeholderColor: Color,
    cursorColor: Color,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
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
        leadingWidth.toPx() + placeholderGap.toPx() + placeholderTextWidthPx
    }
    val endPadding = if (trailingContent != null) {
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
                .then(fieldModifier)
                .fillMaxSize()
                .then(textFieldModifier)
                .padding(start = horizontalPadding, end = endPadding),
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.copy(color = textColor),
            cursorBrush = SolidColor(cursorColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val placeholderTranslationX = with(density) {
                        ((maxWidth.toPx() - placeholderGroupWidthPx) / 2f)
                            .coerceAtLeast(0f) * (1f - clampedPlaceholderStartProgress)
                    }

                    // One Row hosting the leading slot exactly once: while collapsed the
                    // leading+placeholder group sits translated to the horizontal center and
                    // slides to the start edge as the field expands.
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(placeholderGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.graphicsLayer {
                                translationX = if (!queryContentVisible) {
                                    placeholderTranslationX
                                } else {
                                    0f
                                }
                            },
                            horizontalArrangement = Arrangement.spacedBy(placeholderGap),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            leadingContent()
                            if (!queryContentVisible) {
                                Text(
                                    text = placeholder,
                                    style = textStyle,
                                    color = placeholderColor,
                                    maxLines = 1,
                                )
                            }
                        }
                        if (queryContentVisible) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                innerTextField()
                            }
                            if (trailingContent != null) {
                                trailingContent()
                            }
                        }
                    }

                    // The field stays composed while collapsed (invisible when the query is
                    // blank) so the expanded flow can request focus on it immediately.
                    if (!queryContentVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    translationX = placeholderTranslationX
                                    alpha = if (query.isBlank()) 1f else 0f
                                }
                                .padding(start = leadingWidth + placeholderGap),
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

/** How long the [InputField] placeholder takes to slide from centered to the start edge. */
private const val PLACEHOLDER_SLIDE_DURATION_MILLIS = 250
