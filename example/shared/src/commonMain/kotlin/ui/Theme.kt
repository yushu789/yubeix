// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import site.unclefish.yubeix.theme.ColorSchemeMode
import site.unclefish.yubeix.theme.ThemeColorSpec
import site.unclefish.yubeix.theme.ThemeController
import site.unclefish.yubeix.theme.ThemePaletteStyle
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun AppTheme(
    colorMode: Int = 0,
    keyColor: Color? = null,
    paletteStyle: Int = 0,
    colorSpec: Int = 0,
    content: @Composable () -> Unit,
) {
    val spec = ThemeColorSpec.entries.getOrNull(colorSpec) ?: ThemeColorSpec.Spec2021
    val style = ThemePaletteStyle.entries.getOrNull(paletteStyle) ?: ThemePaletteStyle.Content
    val controller = remember(colorMode, keyColor, spec, style) {
        when (colorMode) {
            1 -> ThemeController(ColorSchemeMode.Light)
            2 -> ThemeController(ColorSchemeMode.Dark)
            3 -> ThemeController(ColorSchemeMode.MonetSystem, keyColor = keyColor, colorSpec = spec, paletteStyle = style)
            4 -> ThemeController(ColorSchemeMode.MonetLight, keyColor = keyColor, colorSpec = spec, paletteStyle = style)
            5 -> ThemeController(ColorSchemeMode.MonetDark, keyColor = keyColor, colorSpec = spec, paletteStyle = style)
            else -> ThemeController(ColorSchemeMode.System)
        }
    }
    return YubeixTheme(
        controller = controller,
        content = content,
    )
}

val KeyColors: List<Pair<String, Color>> = listOf(
    "Blue" to Color(0xFF3482FF),
    "Green" to Color(0xFF36D167),
    "Purple" to Color(0xFF7C4DFF),
    "Yellow" to Color(0xFFFFB21D),
    "Orange" to Color(0xFFFF5722),
    "Pink" to Color(0xFFE91E63),
    "Teal" to Color(0xFF00BCD4),
)

fun keyColorFor(index: Int): Color? = if (index <= 0) null else KeyColors.getOrNull(index - 1)?.second
