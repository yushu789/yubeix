// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * The iOS sheet layering compose-cupertino reproduces.
 *
 * A presented sheet runs at the *elevated* interface level, so every opaque background inside it
 * resolves one step up the system ladder: the sheet body goes from pure black to `#1C1C1E` and the
 * rows on it go from `#1C1C1E` to `#2C2C2E`. That is what keeps a dark sheet readable over the
 * black backdrop without drawing a border around it. iOS has no elevated palette in light mode, so
 * light colors are returned untouched.
 */

// Straight off the compose-cupertino tokens: #000000 -> #1C1C1E -> #2C2C2E -> #3A3A3C.
private val DarkElevationLadder = listOf(
    CupertinoColor.DarkSystemBackground to CupertinoColor.DarkSystemBackgroundElevated,
    CupertinoColor.DarkSecondarySystemBackground to CupertinoColor.DarkSecondarySystemBackgroundElevated,
    CupertinoColor.DarkTertiarySystemBackground to CupertinoColor.DarkTertiarySystemBackgroundElevated,
)

// Dark fills that are not on the ladder - Monet tints, the search field grey - are lifted by the
// average step of the ladder instead.
private const val ElevatedSurfaceLightenRatio = 0.08f
private const val DarkSurfaceLuminanceThreshold = 0.5f

/**
 * Raises an opaque dark background one level. Light and translucent colors are returned as they
 * are: a translucent fill already composites over whatever layer it is drawn on.
 */
fun Color.elevatedSheetSurface(): Color {
    if (alpha < 1f || luminance() >= DarkSurfaceLuminanceThreshold) return this
    DarkElevationLadder.forEach { (base, elevated) ->
        if (isVisuallyCloseTo(base)) return elevated
    }
    return mixWith(Color.White, ElevatedSurfaceLightenRatio)
}

/** The palette a sheet's own content is drawn with. Foreground colors are left alone. */
fun Colors.elevatedSheetColors(): Colors = copy(
    background = background.elevatedSheetSurface(),
    surface = surface.elevatedSheetSurface(),
    surfaceVariant = surfaceVariant.elevatedSheetSurface(),
    surfaceContainer = surfaceContainer.elevatedSheetSurface(),
    surfaceContainerHigh = surfaceContainerHigh.elevatedSheetSurface(),
    surfaceContainerHighest = surfaceContainerHighest.elevatedSheetSurface(),
    secondary = secondary.elevatedSheetSurface(),
    secondaryVariant = secondaryVariant.elevatedSheetSurface(),
    secondaryContainer = secondaryContainer.elevatedSheetSurface(),
    secondaryContainerVariant = secondaryContainerVariant.elevatedSheetSurface(),
    tertiaryContainer = tertiaryContainer.elevatedSheetSurface(),
    tertiaryContainerVariant = tertiaryContainerVariant.elevatedSheetSurface(),
    disabledSecondary = disabledSecondary.elevatedSheetSurface(),
    disabledSecondaryVariant = disabledSecondaryVariant.elevatedSheetSurface(),
    outline = outline.elevatedSheetSurface(),
)

/**
 * Runs [content] at the elevated interface level, so anything a sheet hosts picks up the raised
 * palette on its own instead of every sheet having to pass explicit colors.
 */
@Composable
fun YubeixSheetElevatedTheme(content: @Composable () -> Unit) {
    val elevatedColors = YubeixTheme.colorScheme.elevatedSheetColors()
    val elevatedGroupedBackground = LocalSystemGroupedBackground.current.elevatedSheetSurface()
    // YubeixTheme puts its own overscroll factory back; keep whatever one the host installed.
    val overscrollFactory = LocalOverscrollFactory.current

    YubeixTheme(colors = elevatedColors) {
        CompositionLocalProvider(
            LocalSystemGroupedBackground provides elevatedGroupedBackground,
            LocalOverscrollFactory provides overscrollFactory,
            content = content,
        )
    }
}
