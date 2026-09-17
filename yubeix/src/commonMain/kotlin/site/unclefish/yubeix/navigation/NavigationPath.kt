// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import site.unclefish.yubeix.navigation.NavKey

enum class SceneLoadingState {
    None,
    WillLoad,
    Loaded,
}

sealed interface SceneTransition {
    data object None : SceneTransition
    data object Drag : SceneTransition
    data class Enter(val velocityX: Float? = null) : SceneTransition
    data class Exit(val velocityX: Float? = null) : SceneTransition
    data class DragEnd(
        val commit: Boolean,
        val velocityProgressPerSecond: Float = 0f,
    ) : SceneTransition
}

@Stable
class Scene<T : NavKey> internal constructor(
    val id: Long,
    val route: T,
    initialTransition: SceneTransition,
    initialProgress: Float,
) {
    var transition by mutableStateOf(initialTransition)
        internal set

    var loadingState by mutableStateOf(
        if (initialTransition is SceneTransition.Enter) {
            SceneLoadingState.WillLoad
        } else {
            SceneLoadingState.Loaded
        }
    )
        internal set

    val transitionAnimation = Animatable(initialProgress.coerceIn(0f, 1f))

    internal var gestureProgress by mutableFloatStateOf(Float.NaN)

    val progress: Float
        get() = overshootProgress.coerceIn(0f, 1f)

    /**
     * [progress] before it is clamped. [transitionAnimation] is unbounded, so a spring with bounce
     * settles by overshooting its target - the elastic part of a shared transition lives in exactly
     * that overshoot, and clamping is what would throw it away. A gesture's own progress is already
     * clamped where it is written, so this only ever leaves 0..1 while a spring is running.
     */
    val overshootProgress: Float
        get() = if (gestureProgress.isNaN()) {
            transitionAnimation.value
        } else {
            gestureProgress
        }

    val isExiting: Boolean
        get() = transition is SceneTransition.Exit ||
            (transition as? SceneTransition.DragEnd)?.commit == true

    /**
     * Hands the scene over from whatever was driving its progress - a finger, or an animation this one
     * is interrupting - to a new animation, and returns the velocity that animation should start with.
     *
     * The velocity is the whole point. [Animatable.snapTo] resets it, so an animation started after it
     * begins from a standstill: reverse a transition halfway and it stops dead before setting off the
     * other way, which reads as a restart rather than as one continuous move. Carrying the velocity
     * across lets the new spring absorb it and turn around the way an object would.
     */
    internal suspend fun beginTransitionAnimation(): Float {
        val handoverVelocity = transitionAnimation.velocity
        if (transitionAnimation.value != progress) {
            // A gesture was driving progress, so the animation has to be caught up to the finger
            // before it can take over. Its own velocity is meaningless in that case; the gesture's is
            // passed in by the caller.
            transitionAnimation.snapTo(progress)
        }
        gestureProgress = Float.NaN
        return handoverVelocity
    }
}

@Stable
class NavigationPath<T : NavKey>(
    restoredRoutes: List<T>,
) {
    constructor(initialRoute: T) : this(listOf(initialRoute))

    private var nextSceneId by mutableLongStateOf(0L)

    val scenes: SnapshotStateList<Scene<T>> = mutableStateListOf()

    init {
        val initialRoutes = restoredRoutes.ifEmpty {
            error("NavigationPath requires at least one route")
        }
        initialRoutes.forEach { route ->
            scenes += newScene(
                route = route,
                transition = SceneTransition.None,
                progress = 1f,
            )
        }
    }

    val currentRoute: T?
        get() = activeScenes.lastOrNull()?.route

    val canNavigateBack: Boolean
        get() = activeScenes.size > 1 ||
            scenes.any { it.isExiting }

    internal val canBeginPredictiveBack: Boolean
        get() = activeScenes.size > 1

    val activeRoutes: List<T>
        get() = activeScenes.map { it.route }

    private val activeScenes: List<Scene<T>>
        get() = scenes.filterNot { it.isExiting }

    fun push(route: T) {
        scenes
            .lastOrNull { scene -> scene.route == route && scene.isExiting }
            ?.let { scene ->
                reenter(scene)
                return
            }

        scenes.removeAll { it.isExiting }
        if (currentRoute == route) {
            return
        }
        scenes += newScene(
            route = route,
            transition = SceneTransition.Enter(),
            progress = 0f,
        )
    }

    /**
     * Turns the scene on its way out back around, from wherever it has got to, and reports whether
     * there was one.
     *
     * [push] does this when it is handed the route that is already leaving, which is how tapping the
     * thing a page came from re-opens it. This is the same door for a caller that knows a transition is
     * running but not what route it is for - a shared transition's own window, standing in for the
     * source it is collapsing onto, so that pressing the source works even while the window is drawn
     * over it.
     */
    fun reenterExitingScene(): Boolean {
        val scene = scenes.lastOrNull()?.takeIf { it.isExiting } ?: return false
        reenter(scene)
        return true
    }

    private fun reenter(scene: Scene<T>) {
        if (scene.loadingState < SceneLoadingState.WillLoad) {
            scene.loadingState = SceneLoadingState.WillLoad
        }
        scene.gestureProgress = Float.NaN
        scene.transition = SceneTransition.Enter()
    }

    fun pop(): Boolean {
        finishCommittedBackTransitions()
        val activeScenesSnapshot = activeScenes
        if (activeScenesSnapshot.size <= 1) {
            return scenes.any { it.isExiting }
        }
        val targetScene = activeScenesSnapshot.last()
        when (val transition = targetScene.transition) {
            is SceneTransition.None,
            is SceneTransition.Enter,
            is SceneTransition.DragEnd -> {
                if (transition is SceneTransition.DragEnd && transition.commit) {
                    return true
                }
                targetScene.gestureProgress = targetScene.progress
                targetScene.transition = SceneTransition.Exit()
            }

            SceneTransition.Drag -> {
                endDrag(scene = targetScene, commit = true)
            }

            is SceneTransition.Exit -> Unit
        }
        return true
    }

    fun popTo(route: T): Boolean {
        val activeScenesSnapshot = activeScenes
        val destinationIndex = activeScenesSnapshot.indexOfLast { scene -> scene.route == route }
        if (destinationIndex < 0 || destinationIndex == activeScenesSnapshot.lastIndex) {
            return false
        }

        val intermediateSceneIds = activeScenesSnapshot
            .subList(destinationIndex + 1, activeScenesSnapshot.lastIndex)
            .mapTo(mutableSetOf()) { scene -> scene.id }
        if (intermediateSceneIds.isNotEmpty()) {
            scenes.removeAll { scene -> scene.id in intermediateSceneIds }
        }
        return pop()
    }

    fun reset(route: T) {
        scenes.clear()
        scenes += newScene(
            route = route,
            transition = SceneTransition.None,
            progress = 1f,
        )
    }

    fun saveableRoutes(): List<T> = activeRoutes.ifEmpty {
        scenes.firstOrNull()?.route?.let(::listOf).orEmpty()
    }

    internal fun remove(scene: Scene<T>) {
        if (scenes.size > 1) {
            scenes.remove(scene)
        } else {
            scene.transition = SceneTransition.None
            scene.gestureProgress = Float.NaN
        }
    }

    internal fun beginDrag(scene: Scene<T>): Boolean {
        if (!canNavigateBack || scenes.lastOrNull() !== scene) {
            return false
        }
        when (val transition = scene.transition) {
            SceneTransition.Drag -> return true
            SceneTransition.None,
            is SceneTransition.Enter -> Unit
            is SceneTransition.DragEnd -> if (transition.commit) return false
            is SceneTransition.Exit -> return false
        }
        scene.gestureProgress = scene.progress
        scene.transition = SceneTransition.Drag
        return true
    }

    internal fun beginBackDrag(): Scene<T>? {
        finishCommittedBackTransitions()
        if (activeScenes.size <= 1) {
            return null
        }
        return scenes.lastOrNull()?.takeIf(::beginDrag)
    }

    internal fun dragBy(scene: Scene<T>, deltaProgress: Float) {
        if (scene.transition !is SceneTransition.Drag) {
            return
        }
        dragTo(scene, scene.progress - deltaProgress)
    }

    internal fun dragTo(scene: Scene<T>, progress: Float) {
        if (scene.transition !is SceneTransition.Drag) {
            return
        }
        scene.gestureProgress = progress.coerceIn(0f, 1f)
    }

    internal fun endDrag(
        scene: Scene<T>,
        commit: Boolean,
        velocityProgressPerSecond: Float = 0f,
    ) {
        if (scene.transition !is SceneTransition.Drag) {
            return
        }
        scene.transition = SceneTransition.DragEnd(
            commit = commit,
            velocityProgressPerSecond = velocityProgressPerSecond,
        )
    }

    internal fun cancelDrag(scene: Scene<T>) {
        if (scene.transition is SceneTransition.Drag) {
            scene.transition = SceneTransition.DragEnd(commit = false)
        }
    }

    private fun finishCommittedBackTransitions() {
        while (scenes.size > 1 && scenes.lastOrNull()?.isExiting == true) {
            scenes.removeAt(scenes.lastIndex)
        }
    }

    private fun newScene(
        route: T,
        transition: SceneTransition,
        progress: Float,
    ): Scene<T> {
        return Scene(
            id = nextSceneId++,
            route = route,
            initialTransition = transition,
            initialProgress = progress,
        )
    }
}

val LocalNavigationPath = staticCompositionLocalOf<NavigationPath<*>?> { null }
