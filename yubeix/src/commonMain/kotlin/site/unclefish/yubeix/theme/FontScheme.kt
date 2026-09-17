// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * A role-based font scheme: [display] drives oversized titles, [text] drives everything else.
 * Hosts install their own [FontFamily]s via [LocalYubeixFontScheme]; the default is the platform
 * default family, so the library never ships or resolves font resources itself.
 */
@Immutable
data class YubeixFontScheme(
    val display: FontFamily = FontFamily.Default,
    val text: FontFamily = FontFamily.Default,
)

enum class YubeixFontRole {
    Display,
    Text,
}

val LocalYubeixFontScheme = staticCompositionLocalOf { YubeixFontScheme() }

/**
 * Material-role sized styles that respect [LocalYubeixFontScheme]. These complement
 * [YubeixTheme.textStyles] (the component styles): use them for app-level typography like
 * headlines and stat numbers that miuix-style roles don't cover.
 */
object YubeixTextStyles {
    val displayLarge: TextStyle
        @Composable get() = displayStyle(fontSize = 57, lineHeight = 64)

    val displayMedium: TextStyle
        @Composable get() = displayStyle(fontSize = 45, lineHeight = 52)

    val displaySmall: TextStyle
        @Composable get() = textStyle(fontSize = 36, lineHeight = 44)

    val headlineLarge: TextStyle
        @Composable get() = textStyle(fontSize = 32, lineHeight = 40)

    val headlineMedium: TextStyle
        @Composable get() = textStyle(fontSize = 28, lineHeight = 36)

    val headlineSmall: TextStyle
        @Composable get() = textStyle(fontSize = 24, lineHeight = 32)

    val titleLarge: TextStyle
        @Composable get() = textStyle(fontSize = 22, lineHeight = 28)

    val titleMedium: TextStyle
        @Composable get() = textStyle(
            fontSize = 16,
            lineHeight = 24,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.15f,
        )

    val titleSmall: TextStyle
        @Composable get() = textStyle(
            fontSize = 14,
            lineHeight = 20,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1f,
        )

    val bodyLarge: TextStyle
        @Composable get() = textStyle(fontSize = 16, lineHeight = 24, letterSpacing = 0.5f)

    val bodyMedium: TextStyle
        @Composable get() = textStyle(fontSize = 14, lineHeight = 20, letterSpacing = 0.25f)

    val bodySmall: TextStyle
        @Composable get() = textStyle(fontSize = 12, lineHeight = 16, letterSpacing = 0.4f)

    val labelLarge: TextStyle
        @Composable get() = textStyle(
            fontSize = 14,
            lineHeight = 20,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1f,
        )

    val labelMedium: TextStyle
        @Composable get() = textStyle(
            fontSize = 12,
            lineHeight = 16,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5f,
        )

    val labelSmall: TextStyle
        @Composable get() = textStyle(
            fontSize = 11,
            lineHeight = 16,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5f,
        )
}

fun TextStyle.withYubeixFont(
    fontScheme: YubeixFontScheme,
    role: YubeixFontRole = YubeixFontRole.Text,
): TextStyle = copy(
    fontFamily = when (role) {
        YubeixFontRole.Display -> fontScheme.display
        YubeixFontRole.Text -> fontScheme.text
    },
)

@Composable
private fun displayStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
): TextStyle = textStyle(
    fontSize = fontSize,
    lineHeight = lineHeight,
    fontFamily = LocalYubeixFontScheme.current.display,
    fontWeight = fontWeight,
    letterSpacing = letterSpacing,
)

@Composable
private fun textStyle(
    fontSize: Int,
    lineHeight: Int,
    fontFamily: FontFamily = LocalYubeixFontScheme.current.text,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = fontWeight,
    letterSpacing = letterSpacing.sp,
)
