// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import dev.chrisbanes.haze.HazeState
import site.unclefish.yubeix.basic.ScrollBehavior
import site.unclefish.yubeix.basic.TopAppBar

@Composable
fun AdaptiveTopAppBar(
    title: String,
    showTopAppBar: Boolean,
    isWideScreen: Boolean,
    scrollBehavior: ScrollBehavior,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    hazeState: HazeState? = null,
) {
    if (showTopAppBar) {
        // Both layouts use the yubeix collapsing large-title bar; the wide layout just drops
        // the default window-insets padding because the split pane already pads the top.
        // When [hazeState] is set the bar blurs the content marked with it instead of drawing
        // a flat translucent fill.
        TopAppBar(
            title = title,
            scrollBehavior = scrollBehavior,
            defaultWindowInsetsPadding = !isWideScreen,
            navigationIcon = navigationIcon,
            actions = actions,
            hazeState = hazeState,
        )
    }
}
