// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import site.unclefish.yubeix.utils.CupertinoOverscrollState
import site.unclefish.yubeix.utils.LocalCupertinoOverscrollState
import site.unclefish.yubeix.utils.rememberCupertinoOverscrollState
import site.unclefish.yubeix.utils.YubeixIndication
import site.unclefish.yubeix.utils.rememberCupertinoOverscrollFactory

/**
 * The Yubeix theme that provides color and text styles for the Yubeix components.
 * This theme supports dynamic color schemes through the [ThemeController].
 *
 * @param controller The [ThemeController] that controls the current color scheme.
 * @param textStyles The text styles for the Yubeix components.
 * @param smoothRounding Whether to use G2-continuity smooth rounded corners. Set to `false`
 *  to fall back to standard [androidx.compose.foundation.shape.RoundedCornerShape] for better
 *  HWUI performance on lower-end devices.
 * @param overscrollState Optional [CupertinoOverscrollState] to observe the live overscroll
 *  offset; pass one explicitly to share it, otherwise the provider-scoped one is used.
 * @param content The content of the Yubeix theme.
 */
@Composable
fun YubeixTheme(
    controller: ThemeController,
    textStyles: TextStyles = YubeixTheme.textStyles,
    smoothRounding: Boolean = true,
    overscrollState: CupertinoOverscrollState? = null,
    content: @Composable () -> Unit,
) {
    val rawColors = controller.currentColors()
    val yubeixColors = remember { rawColors.copy() }.apply { updateColorsFrom(rawColors) }
    val yubeixTextStyles = remember { textStyles.copy() }.apply { updateTextStylesFrom(textStyles) }
    val yubeixIndication = remember(yubeixColors.onBackground) { YubeixIndication(color = yubeixColors.onBackground) }
    val resolvedOverscrollState = overscrollState
        ?: LocalCupertinoOverscrollState.current
        ?: rememberCupertinoOverscrollState()
    val overscrollFactory = rememberCupertinoOverscrollFactory(state = resolvedOverscrollState)
    CompositionLocalProvider(
        LocalCupertinoOverscrollState provides resolvedOverscrollState,
        LocalColors provides yubeixColors,
        LocalTextStyles provides yubeixTextStyles,
        LocalIndication provides yubeixIndication,
        LocalColorSchemeMode provides controller.colorSchemeMode,
        LocalOverscrollFactory provides overscrollFactory,
        LocalSmoothRounding provides smoothRounding,
    ) {
        content()
    }
}

/**
 * The Yubeix theme that provides color and text styles for the Yubeix components.
 * This theme uses the provided [colors] and [textStyles].
 *
 * @param colors The color scheme for the Yubeix components.
 * @param textStyles The text styles for the Yubeix components.
 * @param smoothRounding Whether to use G2-continuity smooth rounded corners. Set to `false`
 *  to fall back to standard [androidx.compose.foundation.shape.RoundedCornerShape] for better
 *  HWUI performance on lower-end devices.
 * @param overscrollState Optional [CupertinoOverscrollState] to observe the live overscroll
 *  offset; pass one explicitly to share it, otherwise the provider-scoped one is used.
 * @param content The content of the Yubeix theme.
 */
@Composable
fun YubeixTheme(
    colors: Colors = YubeixTheme.colorScheme,
    textStyles: TextStyles = YubeixTheme.textStyles,
    smoothRounding: Boolean = true,
    overscrollState: CupertinoOverscrollState? = null,
    content: @Composable () -> Unit,
) {
    val yubeixColors = remember { colors.copy() }.apply { updateColorsFrom(colors) }
    val yubeixTextStyles = remember { textStyles.copy() }.apply { updateTextStylesFrom(textStyles) }
    val yubeixIndication = remember(yubeixColors.onBackground) { YubeixIndication(color = yubeixColors.onBackground) }
    val resolvedOverscrollState = overscrollState
        ?: LocalCupertinoOverscrollState.current
        ?: rememberCupertinoOverscrollState()
    val overscrollFactory = rememberCupertinoOverscrollFactory(state = resolvedOverscrollState)
    CompositionLocalProvider(
        LocalCupertinoOverscrollState provides resolvedOverscrollState,
        LocalColors provides yubeixColors,
        LocalTextStyles provides yubeixTextStyles,
        LocalIndication provides yubeixIndication,
        LocalOverscrollFactory provides overscrollFactory,
        LocalSmoothRounding provides smoothRounding,
    ) {
        content()
    }
}

object YubeixTheme {
    val colorScheme: Colors
        @Composable @ReadOnlyComposable
        get() = LocalColors.current

    val textStyles: TextStyles
        @Composable @ReadOnlyComposable
        get() = LocalTextStyles.current

    val colorSchemeMode: ColorSchemeMode?
        @Composable @ReadOnlyComposable
        get() = LocalColorSchemeMode.current

    val smoothRounding: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalSmoothRounding.current

    val isDynamicColor: Boolean
        @Composable @ReadOnlyComposable
        get() = when (colorSchemeMode) {
            ColorSchemeMode.MonetSystem,
            ColorSchemeMode.MonetLight,
            ColorSchemeMode.MonetDark,
            -> true

            else -> false
        }
}
