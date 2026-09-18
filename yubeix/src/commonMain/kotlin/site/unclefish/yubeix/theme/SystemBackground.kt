// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse

/**
 * The iOS system-grouped-background color the app chrome draws grouped content on, as opposed to
 * [Colors.background] which is what cards themselves are drawn with. Hosts that present sheets or
 * large-title scroll views install this alongside [YubeixTheme] so every component that needs the
 * "page behind the cards" color resolves it the same way.
 */
val LocalSystemGroupedBackground = staticCompositionLocalOf { Color.Unspecified }

val Colors.systemGroupedBackground: Color
    @Composable
    get() = LocalSystemGroupedBackground.current.takeOrElse { background }

/**
 * Set to true by hosts that need to tone down decorative motion (blurs, fluid backgrounds,
 * parallax) - components read this instead of carrying their own reduce-motion plumbing.
 */
val LocalReducedDynamicEffectsEnabled = staticCompositionLocalOf { false }
