// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package appnavigation

import site.unclefish.yubeix.navigation.NavigationPath

/**
 * Small facade over the library's [NavigationPath] so the example screens keep their old
 * push/pop API while the scene navigation engine does the real work underneath.
 */
class Navigator(
    val navigationPath: NavigationPath<Route>,
) {
    /**
     * Push a key onto the back stack.
     */
    fun push(key: Route) {
        navigationPath.push(key)
    }

    /**
     * Pop the top key if present.
     */
    fun pop(): Boolean = navigationPath.pop()

    fun current(): Route? = navigationPath.currentRoute

    fun backStackSize(): Int = navigationPath.activeRoutes.size
}
