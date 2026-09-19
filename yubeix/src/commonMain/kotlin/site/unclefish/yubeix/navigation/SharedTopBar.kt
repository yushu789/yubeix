// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex

/*
 * The shared top-bar transition: the iOS navigation-bar behavior where the chrome stays put
 * while the pages move under it.
 *
 * Scenes do not transform their own bars - a scene is one sliding layer, and a bar inside it
 * slides (and is bitmap-clipped) with it. Instead, while a transition runs, each participating
 * scene's [site.unclefish.yubeix.basic.ScreenScaffold] hands its chrome bar over to a
 * [SharedTopBarSlot] and suppresses the inline copy, and [SceneDisplay] draws the pair's bars
 * once, fixed at the top of the window, cross-fading between them on the same progress the
 * scenes slide by. The bar region therefore never moves; only the page content does.
 */

/**
 * The donated chrome bar of one scene. The slot is written by the scene's outermost
 * [site.unclefish.yubeix.basic.ScreenScaffold] and read by [SharedTopBarOverlay]; the
 * content is the scene's own bar composable, so its collapse state, actions and popups stay
 * live through the transition. The receiver lets the bar keep using `Modifier.align`, which
 * is scoped to the host [Box].
 */
internal class SharedTopBarSlot {
    var content by mutableStateOf<(@Composable BoxScope.() -> Unit)?>(null)

    /**
     * Whether the scene currently takes part in a transition whose bars are drawn by the
     * host-level overlay. This lives on the slot as observable state - not in a
     * CompositionLocal - so the host writes it synchronously in its own recomposition scope
     * while the observing scaffold picks the change up in whichever later pass it runs in.
     */
    var inOverlay by mutableStateOf(false)

    /**
     * Whether the overlay has actually composed this scene's donated bar. Written by
     * [SharedTopBarOverlay] during its own composition. The scaffold keeps drawing the inline
     * copy until this is true: the overlay's scope and the scaffold's scope can execute in
     * either order within a recomposition pass, and if the scaffold suppressed its inline bar
     * before the overlay had picked the donation up, one frame would draw no bar anywhere -
     * the whole chrome vanishing for a frame at every transition edge.
     */
    var overlayPickedUp by mutableStateOf(false)

    /**
     * Set by the host when this scene enters a transition, cleared by the scaffold when it
     * (re)donates its bar. The overlay must not compose a PARTIAL pair: the two scenes' scopes
     * donate in unrelated orders, and drawing whichever bar arrived first would flash the wrong
     * scene's chrome for a frame at the transition edge. Until both scenes have cleared this
     * flag the overlay draws nothing and the scenes keep their inline bars - identical pixels
     * to the pre-transition frame.
     */
    var awaitingDonation by mutableStateOf(false)
}

/** One [SharedTopBarSlot] per live scene id, for the lifetime of the host's composition. */
internal class SharedTopBarSlots {
    private val slots = mutableMapOf<Long, SharedTopBarSlot>()

    fun slotFor(sceneId: Long): SharedTopBarSlot = slots.getOrPut(sceneId) { SharedTopBarSlot() }

    /** Drops the slots of scenes that left the stack. */
    fun prune(retainedSceneIds: Set<Long>) {
        slots.keys.retainAll(retainedSceneIds)
    }
}

/**
 * The slot a scene's outermost [site.unclefish.yubeix.basic.ScreenScaffold] donates its chrome
 * bar into; null outside a scene host or when the host did not opt into shared top-bar
 * transitions.
 */
internal val LocalSceneTopBarSlot = compositionLocalOf<SharedTopBarSlot?> { null }

/**
 * How many [site.unclefish.yubeix.basic.ScreenScaffold]s wrap the current position inside its
 * scene. Only depth zero donates, so a nested scaffold keeps its inline bar and slides with the
 * content like any other content-level chrome.
 */
internal val LocalSceneTopBarNesting = compositionLocalOf { 0 }

/**
 * Draws a transition pair's donated chrome bars fixed at the top of the window while the scenes
 * slide underneath.
 *
 * Only ONE of the pair ever fades, and it is always drawn on top of the other: on a push the
 * outgoing bar stays fully opaque while the incoming one fades in above it; on a pop the roles
 * flip and the outgoing bar fades out above a fully opaque incoming bar. Cross-fading both bars
 * instead would composite two partial covers over the sliding scene - at mid transition roughly a
 * quarter of the sharp content would bleed through the pair - which reads as the frosted bar
 * blinking off and back on at the edges of every transition.
 *
 * Before composing, the overlay publishes whether it actually has each scene's bar
 * ([SharedTopBarSlot.overlayPickedUp]); the donating scaffold keeps its inline copy until its
 * pickup flag is set. The overlay's scope and the donating scaffold's scope can execute in
 * either order within a recomposition pass, so without this handshake the overlay could commit
 * a frame with no bar while the scaffold had already dropped its inline copy. A scene that has
 * just entered the transition keeps its inline copy even longer - until it has (re)donated
 * ([SharedTopBarSlot.awaitingDonation]) - so the overlay never composes a partial pair whose
 * missing half would flash the wrong chrome for a frame.
 *
 * Once [transitionActive] is false the pair is dissolving: the scaffolds clear their slots in
 * unrelated passes, so only the surviving front scene's bar is drawn (the outgoing slot is
 * ignored) and the overlay dissolves into the restored inline bar without ever showing a half
 * pair.
 *
 * The alphas are read inside [graphicsLayer] blocks, so the fade never recomposes - every
 * animated frame is render-side only. Each bar is keyed by its scene id, so a new pair composes
 * fresh bar state instead of inheriting the previous pair's.
 */
@Composable
internal fun SharedTopBarOverlay(
    slots: SharedTopBarSlots,
    outgoingSceneId: Long?,
    incomingSceneId: Long,
    transitionActive: Boolean,
    outgoingAlpha: () -> Float,
    incomingAlpha: () -> Float,
    incomingOnTop: Boolean,
    zIndex: Float,
) {
    val outgoingSlot = outgoingSceneId?.let { slots.slotFor(it) }
    val incomingSlot = slots.slotFor(incomingSceneId)
    val outgoingBar = if (transitionActive) outgoingSlot?.content else null
    val incomingBar = incomingSlot.content
    // A scene that just entered the transition has not re-donated its bar yet; drawing the pair
    // before BOTH scenes have donated would flash whichever bar arrived first. Until then the
    // overlay draws nothing and the scenes keep their inline bars (identical pixels).
    val pairReady = incomingSlot.awaitingDonation.not() && outgoingSlot?.awaitingDonation != true
    // Publish the pickup state before the early return so a scaffold donating into an overlay
    // that currently draws nothing (e.g. the first pass of a fresh transition pair) always
    // learns that its bar is not being drawn yet and keeps its inline copy.
    val drawOutgoing = pairReady && outgoingBar != null
    val drawIncoming = pairReady && incomingBar != null
    outgoingSlot?.overlayPickedUp = drawOutgoing
    incomingSlot.overlayPickedUp = drawIncoming
    if (!drawOutgoing && !drawIncoming) {
        return
    }
    Box(modifier = Modifier.fillMaxSize().zIndex(zIndex)) {
        val bottomSceneId: Long?
        val topSceneId: Long?
        val bottomBar: (@Composable BoxScope.() -> Unit)?
        val topBar: (@Composable BoxScope.() -> Unit)?
        val bottomAlpha: () -> Float
        val topAlpha: () -> Float
        if (incomingOnTop) {
            bottomSceneId = outgoingSceneId
            bottomBar = outgoingBar
            bottomAlpha = outgoingAlpha
            topSceneId = incomingSceneId
            topBar = incomingBar
            topAlpha = incomingAlpha
        } else {
            bottomSceneId = incomingSceneId
            bottomBar = incomingBar
            bottomAlpha = incomingAlpha
            topSceneId = outgoingSceneId
            topBar = outgoingBar
            topAlpha = outgoingAlpha
        }
        if (bottomBar != null && bottomSceneId != null) {
            key(bottomSceneId) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = bottomAlpha() },
                ) {
                    bottomBar()
                }
            }
        }
        if (topBar != null && topSceneId != null) {
            key(topSceneId) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = topAlpha() },
                ) {
                    topBar()
                }
            }
        }
    }
}
