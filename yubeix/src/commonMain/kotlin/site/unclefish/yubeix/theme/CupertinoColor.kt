// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.ui.graphics.Color

/**
 * iOS system palette ported from alexzhirkevich/compose-cupertino (`ColorSchemeTokens`), using its
 * default (non high contrast) values. See [elevatedSheetSurface] for the layering rule the bottom
 * sheets build on top of it.
 */
object CupertinoColor {
    val LightLabel = Color(0xFF000000)
    val LightSecondaryLabel = Color(0x993C3C43)
    val LightTertiaryLabel = Color(0x4C3C3C43)
    val LightQuaternaryLabel = Color(0x2D3C3C43)
    val LightPlaceholderText = Color(0x4C3C3C43)
    val LightSystemFill = Color(0x5B787880)
    val LightSecondarySystemFill = Color(0x51787880)
    val LightTertiarySystemFill = Color(0x3D767680)
    val LightQuaternarySystemFill = Color(0x2D767680)
    val LightSeparator = Color(0x493C3C43)
    val LightOpaqueSeparator = Color(0xFFC6C6C8)
    val LightLink = Color(0xFF007AFF)
    val LightSystemBackground = Color(0xFFFFFFFF)
    val LightSecondarySystemBackground = Color(0xFFF2F2F7)
    val LightTertiarySystemBackground = Color(0xFFFFFFFF)
    val LightSystemGroupedBackground = Color(0xFFF2F2F7)
    val LightSecondarySystemGroupedBackground = Color(0xFFFFFFFF)
    val LightTertiarySystemGroupedBackground = Color(0xFFF2F2F7)

    val DarkLabel = Color(0xFFFFFFFF)
    val DarkSecondaryLabel = Color(0x99EBEBF5)
    val DarkTertiaryLabel = Color(0x4CEBEBF5)
    val DarkQuaternaryLabel = Color(0x28EBEBF5)
    val DarkPlaceholderText = Color(0x4CEBEBF5)
    val DarkSystemFill = Color(0x5B787880)
    val DarkSecondarySystemFill = Color(0x51787880)
    val DarkTertiarySystemFill = Color(0x3D767680)
    val DarkQuaternarySystemFill = Color(0x2D767680)
    val DarkSeparator = Color(0x99545458)
    val DarkOpaqueSeparator = Color(0xFF38383A)
    val DarkLink = Color(0xFF0984FF)
    val DarkSystemBackground = Color(0xFF000000)
    val DarkSecondarySystemBackground = Color(0xFF1C1C1E)
    val DarkTertiarySystemBackground = Color(0xFF2C2C2E)
    val DarkSystemGroupedBackground = Color(0xFF000000)
    val DarkSecondarySystemGroupedBackground = Color(0xFF1C1C1E)
    val DarkTertiarySystemGroupedBackground = Color(0xFF2C2C2E)

    // Elevated levels: while a sheet is presented iOS resolves every dark background one step
    // lighter, which is what lets a sheet separate from the backdrop without a border.
    val DarkSystemBackgroundElevated = Color(0xFF1C1C1E)
    val DarkSecondarySystemBackgroundElevated = Color(0xFF2C2C2E)
    val DarkTertiarySystemBackgroundElevated = Color(0xFF3A3A3C)
}
