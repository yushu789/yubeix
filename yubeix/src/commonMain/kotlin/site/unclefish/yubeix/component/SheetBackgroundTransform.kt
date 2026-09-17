// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.navigation.navigationBitmapClip
import site.unclefish.yubeix.utils.rememberDeviceCornerRadius
import site.unclefish.yubeix.utils.rememberSafeTopInset

// Matches the iOS sheet presentation: the page behind the sheet shrinks a little, drops below the
// status bar and picks up rounded corners over a black backdrop.
private const val SheetBackgroundScaleDivisor = 11f
private const val SheetBackgroundTranslationMultiplier = 2.75f
private val SheetBackgroundTopSpacing = 10.dp
private val SheetBackgroundMinTopInset = 10.dp

/**
 * Tracks how far the page behind the bottom sheets should be pushed back.
 *
 * Sheets that live in their own dialog window cannot transform the page themselves. Each visible
 * sheet instead registers a progress source via [SheetBackgroundTransformSource] and
 * [SheetBackgroundTransformHost] applies the resulting transform to the hosted content.
 */
@Stable
class SheetBackgroundTransformState internal constructor() {

    private val sources = mutableStateListOf<() -> Float>()

    /** Whether at least one sheet is currently driving the transform. */
    val isActive: Boolean
        get() = sources.isNotEmpty()

    /** The strongest progress reported by the visible sheets, in `0f..1f`. */
    fun progress(): Float {
        var progress = 0f
        for (index in sources.indices) {
            val sourceProgress = sources[index].invoke()
            if (sourceProgress > progress) {
                progress = sourceProgress
            }
        }
        return progress.coerceIn(0f, 1f)
    }

    internal fun register(source: () -> Float) {
        sources.add(source)
    }

    internal fun unregister(source: () -> Float) {
        sources.remove(source)
    }
}

/** Provides the [SheetBackgroundTransformState] that sheets read and register against. */
val LocalSheetBackgroundTransform =
    staticCompositionLocalOf<SheetBackgroundTransformState?> { null }

/**
 * Registers [progress] as a driver of the background transform for as long as this call stays in
 * composition. [progress] is read from the draw phase, so it may change every frame without
 * triggering recomposition. Does nothing when no [SheetBackgroundTransformHost] is above this call.
 */
@Composable
fun SheetBackgroundTransformSource(
    enabled: Boolean,
    progress: () -> Float,
) {
    val state = LocalSheetBackgroundTransform.current ?: return
    val currentProgress by rememberUpdatedState(progress)

    DisposableEffect(state, enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        val source: () -> Float = { currentProgress() }
        state.register(source)
        onDispose { state.unregister(source) }
    }
}

/**
 * Applies the iOS-style sheet presentation transform to the content behind a sheet: the content
 * scales down, slides below the top inset, and gets clipped to the device's rounded-corner shape
 * while any progress is showing.
 *
 * The progress is observed from the layer and draw phases, so the animation advances without
 * recomposing the content on every frame.
 */
fun Modifier.sheetBackgroundTransform(
    progress: () -> Float,
    topInsetPx: Float,
    cornerRadius: Dp,
): Modifier = graphicsLayer {
    val currentProgress = progress().coerceIn(0f, 1f)
    val scale = 1f - currentProgress / SheetBackgroundScaleDivisor
    scaleX = scale
    scaleY = scale
    translationY =
        (1f - scale) * topInsetPx * SheetBackgroundTranslationMultiplier
}
    .navigationBitmapClip(
        cornerRadius = cornerRadius,
        enabled = { progress() > 0f },
    )

/**
 * The distance from the window top at which the transformed page comes to rest: the safe top inset
 * plus a small spacing, never less than a minimum so freeform windows without insets still show a
 * gap above the shrunken page.
 */
@Composable
fun rememberSheetBackgroundTopInsetPx(): Float {
    val density = LocalDensity.current
    val safeTopInset = rememberSafeTopInset()
    return remember(density, safeTopInset) {
        with(density) {
            maxOf(safeTopInset + SheetBackgroundTopSpacing, SheetBackgroundMinTopInset).toPx()
        }
    }
}

/**
 * Hosts content and shrinks it behind any visible sheet that registers through
 * [SheetBackgroundTransformSource].
 *
 * The area uncovered by the shrunken page is filled with [backdropColor].
 *
 * TODO: The Android app build darkens the status bar icons once the progress passes a threshold;
 *  that system-bar control is app/platform specific and has no commonMain equivalent yet, so it is
 *  not applied here.
 *
 * @param backdropColor Fills the strips exposed above and beside the pushed-back page.
 * @param cornerRadius The device corner radius the transformed page is clipped to.
 */
@Composable
fun SheetBackgroundTransformHost(
    modifier: Modifier = Modifier,
    backdropColor: Color = Color.Black,
    cornerRadius: Dp = rememberDeviceCornerRadius(),
    content: @Composable () -> Unit,
) {
    val state = remember { SheetBackgroundTransformState() }
    val topInsetPx = rememberSheetBackgroundTopInsetPx()

    CompositionLocalProvider(LocalSheetBackgroundTransform provides state) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(if (state.isActive) Modifier.background(backdropColor) else Modifier),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sheetBackgroundTransform(
                        progress = state::progress,
                        topInsetPx = topInsetPx,
                        cornerRadius = cornerRadius,
                    ),
            ) {
                content()
            }
        }
    }
}
