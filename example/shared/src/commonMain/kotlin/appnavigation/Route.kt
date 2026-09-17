// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package appnavigation

import kotlinx.serialization.Serializable
import site.unclefish.yubeix.navigation.NavKey

/**
 * Type-safe navigation keys for the example app's scene navigation.
 * Each destination is a NavKey (data object/data class) and can be saved/restored in the back stack.
 */
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data object About : Route

    @Serializable
    data object License : Route

    @Serializable
    data class NavTest(val id: String) : Route

    @Serializable
    data object MultiScaffoldTest : Route
}
