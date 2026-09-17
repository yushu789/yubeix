// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.utils

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ButtonReleaseHapticDelayMillis = 220L
private const val ButtonDuplicatePressSuppressMillis = 80L

/**
 * Button press haptics with a release confirmation: a tick on press-down, and a confirm tick when
 * the release comes long enough after the press ([releaseFeedbackDelayMillis]) - a quick tap
 * intentionally gets only the press tick. Duplicate presses within
 * [ButtonDuplicatePressSuppressMillis] are suppressed so rapid re-taps don't machine-gun.
 */
@Composable
fun ButtonHapticFeedback(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    releaseFeedbackDelayMillis: Long = ButtonReleaseHapticDelayMillis
) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentReleaseFeedbackDelayMillis by rememberUpdatedState(releaseFeedbackDelayMillis)

    LaunchedEffect(interactionSource) {
        var isPressSessionActive = false
        var releaseFeedbackReady = false
        var releaseFeedbackJob: Job? = null
        var suppressDuplicatePressJob: Job? = null

        fun endPressSession() {
            releaseFeedbackJob?.cancel()
            releaseFeedbackJob = null
            isPressSessionActive = false
            releaseFeedbackReady = false
            suppressDuplicatePressJob?.cancel()
            suppressDuplicatePressJob = launch {
                delay(ButtonDuplicatePressSuppressMillis)
                suppressDuplicatePressJob = null
            }
        }

        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    if (currentEnabled && !isPressSessionActive && suppressDuplicatePressJob == null) {
                        isPressSessionActive = true
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        releaseFeedbackJob = launch {
                            delay(currentReleaseFeedbackDelayMillis)
                            if (currentEnabled && isPressSessionActive) {
                                releaseFeedbackReady = true
                            }
                        }
                    }
                }
                is PressInteraction.Release -> {
                    if (isPressSessionActive) {
                        if (releaseFeedbackReady && currentEnabled) {
                            currentHapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        }
                        endPressSession()
                    }
                }
                is PressInteraction.Cancel -> {
                    if (isPressSessionActive) {
                        endPressSession()
                    }
                }
            }
        }
    }
}
