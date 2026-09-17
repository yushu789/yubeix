// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.blur

import androidx.compose.ui.Modifier

/**
 * Captures the content of this composable into the given [LayerBackdrop]'s graphics layer.
 * Place this modifier on the container whose content should appear as the blurred background.
 */
expect fun Modifier.layerBackdrop(backdrop: LayerBackdrop): Modifier
