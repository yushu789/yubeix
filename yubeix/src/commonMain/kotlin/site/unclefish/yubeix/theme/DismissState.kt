// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.runtime.staticCompositionLocalOf
import site.unclefish.yubeix.extra.SuperBottomSheet
import site.unclefish.yubeix.extra.SuperDialog
import site.unclefish.yubeix.extra.SuperListPopup
import site.unclefish.yubeix.extra.WindowBottomSheet
import site.unclefish.yubeix.extra.WindowDialog
import site.unclefish.yubeix.extra.WindowListPopup

/**
 * CompositionLocal that provides a dismiss request function for overlay components.
 *
 * This is automatically provided by all overlay components ([SuperDialog], [WindowDialog],
 * [SuperBottomSheet], [WindowBottomSheet], [SuperListPopup], [WindowListPopup]).
 *
 * Call the provided function to request dismissal from inside overlay content:
 * ```kotlin
 * val dismiss = LocalDismissState.current
 * Button(onClick = { dismiss?.invoke() }) { Text("Close") }
 * ```
 */
val LocalDismissState = staticCompositionLocalOf<(() -> Unit)?> { null }
