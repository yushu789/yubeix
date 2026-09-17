// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.theme

import androidx.compose.runtime.Composable

@Composable
actual fun platformDynamicColors(dark: Boolean): Colors = monetSystemColors(dark)
