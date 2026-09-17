// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.collect
import site.unclefish.yubeix.utils.rememberDeviceCornerRadius
import kotlin.time.TimeSource

/**
 * The scene navigation host: renders every live scene of a [NavigationPath] as stacked layers
 * and drives the iOS-style push/pop motion between them.
 *
 * - New pages slide in from the right while the page below shifts left by a third of its width
 *   ([horizontalSlideSceneTransform]); a dark dim (alpha [NavigationDimAmount]) sits between the
 *   layers so the stack reads as depth.
 * - [predictiveBackEnabled] wires the system back gesture to a live drag of the front scene
 *   through the navigationevent pipeline, so the page follows the finger where the platform
 *   reports progress and settles with [navigationDragSettleSpring].
 * - Every scene owns a [ViewModelStore] and a saveable-state slot, so a popped scene releases
 *   its ViewModels instead of leaking them behind the stack.
 * - [sharedTransitionEnabled]/[sharedTransitionElastic] opt route pairs into shared-element
 *   motion; [customSceneTransform] lets a host substitute its own transform for the standard
 *   page slide on selected transitions (a horizontal onboarding slide, for instance).
 *
 * Hosts declare destinations with [entryProvider] and hold the back stack in a [NavigationPath]:
 * ```
 * val navigationPath = rememberSaveable(saver = ...) { NavigationPath(Route.Main) }
 * SceneDisplay(
 *     navigationPath = navigationPath,
 *     entryProvider = entryProvider {
 *         entry(Route.Main) { MainPage(onNavigate = navigationPath::push) }
 *         entry<Route.Detail> { route -> DetailPage(route, onBack = navigationPath::pop) }
 *     },
 *     predictiveBackEnabled = true,
 * )
 * ```
 */
@Composable
fun <T : NavKey> SceneDisplay(
    navigationPath: NavigationPath<T>,
    entryProvider: (T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
    predictiveBackEnabled: Boolean = false,
    sharedTransitionEnabled: (from: T?, to: T?) -> Boolean = { _, _ -> false },
    // The subset of shared pairs whose landing keeps the elastic settle. Every other shared pair
    // rides the plain no-bounce springs: a terminal overshoot reads as a second jump on small
    // sources, however it is tuned.
    sharedTransitionElastic: (from: T?, to: T?) -> Boolean = { _, _ -> false },
    customSceneTransform: ((
        scene: Scene<T>,
        followingScenes: List<Scene<T>>,
        zIndex: Float,
    ) -> Modifier)? = null,
) {
    val scenes = navigationPath.scenes
    if (scenes.isEmpty()) {
        return
    }

    val saveableStateHolder = rememberSaveableStateHolder()
    val sceneCornerRadius = rememberDeviceCornerRadius()
    val visibleScenes = scenes.filter { it.loadingState > SceneLoadingState.None }
    val retainedSceneIds = scenes.map { scene -> scene.id }.toSet()
    var knownSceneIds by remember { mutableStateOf(retainedSceneIds) }
    // One ViewModelStore per scene so ViewModel creation inside an entry is scoped to the scene
    // rather than the host. Without this, a screen's ViewModel (and everything it holds - cached
    // PagingData, StateFlows, etc.) outlives the scene and is only released when the host dies.
    val sceneViewModelStores = remember { mutableMapOf<Any, ViewModelStore>() }

    LaunchedEffect(retainedSceneIds, saveableStateHolder) {
        (knownSceneIds - retainedSceneIds).forEach { id ->
            saveableStateHolder.removeState(id)
            sceneViewModelStores.remove(id)?.clear()
        }
        knownSceneIds = retainedSceneIds
    }

    val frontScene = visibleScenes.lastOrNull() ?: return
    val previousFrontScene = visibleScenes.getOrNull(visibleScenes.lastIndex - 1)
    val renderScenes = visibleScenes.renderWindow()
    val transitionActive = previousFrontScene != null &&
        frontScene.transition !is SceneTransition.None
    val isPopTransition = frontScene.transition is SceneTransition.Exit ||
        frontScene.transition is SceneTransition.Drag ||
        frontScene.transition is SceneTransition.DragEnd
    val transitionFromRoute = if (isPopTransition) {
        frontScene.route
    } else {
        previousFrontScene?.route
    }
    val transitionToRoute = if (isPopTransition) {
        previousFrontScene?.route
    } else {
        frontScene.route
    }
    val sharedTransitionPairEnabled = sharedTransitionEnabled(
        transitionFromRoute,
        transitionToRoute,
    )
    val sharedTransitionPairElastic = sharedTransitionPairEnabled &&
        sharedTransitionElastic(
            transitionFromRoute,
            transitionToRoute,
        )
    val sharedTransitionActive = transitionActive && sharedTransitionPairEnabled
    visibleScenes
        .filter { scene ->
            scene.loadingState == SceneLoadingState.WillLoad ||
                scene.transition !is SceneTransition.None
        }
        .forEach { scene ->
            key(scene.id) {
                SceneTransitionEffect(
                    navigationPath = navigationPath,
                    scene = scene,
                    sharedElement = sharedTransitionPairEnabled &&
                        (scene === frontScene || scene === previousFrontScene),
                    elasticSharedElement = sharedTransitionPairElastic &&
                        (scene === frontScene || scene === previousFrontScene),
                )
            }
        }

    val sharedScope = remember(frontScene.id, previousFrontScene?.id, sharedTransitionActive) { Any() }
    // Handed over with the spring's elastic settle still on it - clamped to a band rather than to
    // 0..1 - so the shared transition can land its window with a squeeze. Everything that must not
    // overshoot (the dim below, the scene slide, the bitmap clips) keeps reading scene.progress,
    // which is still clamped at the source. See [sharedTransitionExpansion].
    val sharedProgress = if (!sharedTransitionActive) {
        1f
    } else if (isPopTransition) {
        1f - frontScene.overshootProgress
    } else {
        frontScene.overshootProgress
    }.coerceIn(-SharedProgressOvershootBand, 1f + SharedProgressOvershootBand)
    val sharedDirection = when {
        !sharedTransitionActive -> SharedTransitionDirection.None
        isPopTransition -> SharedTransitionDirection.Pop
        else -> SharedTransitionDirection.Push
    }
    val predictiveBackHandlingEnabled = predictiveBackEnabled && navigationPath.canBeginPredictiveBack

    PlatformBackAnimationHandler(
        enabled = predictiveBackHandlingEnabled,
        navigationPath = navigationPath,
    )

    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = navigationPath.canNavigateBack && !predictiveBackHandlingEnabled,
        onBackCompleted = { navigationPath.pop() },
    )

    Box(modifier = modifier.fillMaxSize()) {
        renderScenes.forEachIndexed { index, scene ->
            key(scene.id) {
                if (index > 0) {
                    val sceneTransitionActive = transitionActive &&
                        (scene === frontScene || scene === previousFrontScene)
                    val useCustomTransform = customSceneTransform != null && sceneTransitionActive
                    if (!useCustomTransform) {
                        SceneOverlay(
                            scene = scene,
                            zIndex = index * 2f - 1f,
                        )
                    }
                }
                val followingScenes = remember(renderScenes, index) {
                    renderScenes.drop(index + 1)
                }
                val sceneTransitionActive = transitionActive &&
                    (scene === frontScene || scene === previousFrontScene)
                val useCustomTransform = customSceneTransform != null && sceneTransitionActive
                // Keep the pair on the same RenderNode configuration while the destination stays
                // stacked, so the layer tree does not flip under a landing shared transition.
                val useSharedSceneTransform = shouldUseSharedSceneTransform(
                    sharedTransitionPairEnabled = sharedTransitionPairEnabled,
                    isFrontPairScene = scene === frontScene || scene === previousFrontScene,
                )
                val sceneSharedHostTargetVisible = when {
                    !sharedTransitionActive || !sceneTransitionActive -> true
                    scene === previousFrontScene -> isPopTransition
                    else -> !isPopTransition
                }
                val sceneModifier = if (useCustomTransform) {
                    customSceneTransform!!.invoke(
                        scene,
                        followingScenes,
                        index * 2f,
                    )
                } else {
                    Modifier
                        .sceneTransform(
                            scene = scene,
                            followingScenes = followingScenes,
                            zIndex = index * 2f,
                            enabled = !useSharedSceneTransform,
                        )
                        .navigationSceneBitmapClip(
                            scene = scene,
                            cornerRadius = sceneCornerRadius,
                            enabled = !useSharedSceneTransform,
                        )
                }

                RenderScene(
                    navigationPath = navigationPath,
                    scene = scene,
                    entryProvider = entryProvider,
                    saveableStateHolder = saveableStateHolder,
                    saveableKey = scene.id,
                    sceneViewModelStores = sceneViewModelStores,
                    modifier = sceneModifier,
                    sharedScope = sharedScope,
                    sharedTransitionActive = sharedTransitionActive && sceneTransitionActive,
                    sharedHostTargetVisible = sceneSharedHostTargetVisible,
                    sharedTransitionProgress = sharedProgress,
                    sharedTransitionDirection = sharedDirection,
                    isTransitionBackground = sceneTransitionActive && scene === previousFrontScene,
                    isForeground = scene === frontScene,
                )
            }
        }
    }
}

internal fun shouldUseSharedSceneTransform(
    sharedTransitionPairEnabled: Boolean,
    isFrontPairScene: Boolean,
): Boolean = sharedTransitionPairEnabled && isFrontPairScene

/**
 * The standard page-slide transform, exposed for hosts building their own [customSceneTransform]:
 * the scene slides from the leading edge by [Scene.progress] while the scenes above it shift it
 * left by a third of the width per unit of their combined progress.
 */
fun Modifier.horizontalSlideSceneTransform(
    scene: Scene<*>,
    followingScenes: List<Scene<*>>,
    zIndex: Float,
    enabled: Boolean = true,
): Modifier = then(
    Modifier
        .fillMaxSize()
        .zIndex(zIndex)
        .graphicsLayer {
            translationX = if (enabled) {
                val width = size.width
                val progress = scene.progress
                val followingProgress = followingScenes
                    .sumOf { it.progress.toDouble() }
                    .toFloat()
                lerp(width, 0f, progress) +
                    lerp(0f, -width / 3f, followingProgress)
            } else {
                0f
            }
            clip = false
        }
)

@Composable
private fun <T : NavKey> PlatformBackAnimationHandler(
    enabled: Boolean,
    navigationPath: NavigationPath<T>,
) {
    val currentNavigationPath = rememberUpdatedState(navigationPath)
    val navigationEventState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    val session = remember { BackDragSession<T>() }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = enabled,
        onBackCancelled = {
            session.scene?.let { currentNavigationPath.value.cancelDrag(it) }
            session.reset()
        },
        onBackCompleted = {
            val path = currentNavigationPath.value
            val scene = session.scene
            if (scene == null) {
                path.pop()
            } else {
                path.endDrag(
                    scene = scene,
                    commit = true,
                    velocityProgressPerSecond =
                        session.velocityProgressPerSecond * session.dragStartProgress,
                )
            }
            session.reset()
        },
    )

    LaunchedEffect(navigationEventState.transitionState) {
        val transitionState = navigationEventState.transitionState
        if (
            transitionState is NavigationEventTransitionState.InProgress &&
            transitionState.direction == NavigationEventTransitionState.TRANSITIONING_BACK
        ) {
            if (!session.started) {
                session.start(currentNavigationPath.value.beginBackDrag())
            }
            val scene = session.scene ?: return@LaunchedEffect
            val progress = transitionState.latestEvent.progress.coerceIn(0f, 1f)
            val now = TimeSource.Monotonic.markNow()
            val lastFrame = session.lastFrame
            if (lastFrame != null) {
                val dtSeconds = lastFrame.elapsedNow().inWholeMilliseconds / 1000f
                if (dtSeconds > 0f) {
                    session.velocityProgressPerSecond =
                        (progress - session.lastProgress) / dtSeconds
                }
            }
            session.lastProgress = progress
            session.lastFrame = now
            currentNavigationPath.value.dragTo(
                scene = scene,
                progress = session.dragStartProgress * (1f - progress),
            )
        }
    }
}

/** Gesture-scoped drag state for the predictive back handler. */
private class BackDragSession<T : NavKey> {
    var started = false
        private set
    var scene: Scene<T>? = null
        private set
    var dragStartProgress = 1f
        private set
    var lastProgress = 0f
    var lastFrame: TimeSource.Monotonic.ValueTimeMark? = null
    var velocityProgressPerSecond = 0f

    fun start(scene: Scene<T>?) {
        started = true
        this.scene = scene
        dragStartProgress = scene?.progress ?: 1f
    }

    fun reset() {
        started = false
        scene = null
        lastProgress = 0f
        lastFrame = null
        velocityProgressPerSecond = 0f
    }
}

private fun <T : NavKey> List<Scene<T>>.renderWindow(): List<Scene<T>> {
    if (isEmpty()) {
        return emptyList()
    }

    var firstRenderedIndex = (size - IdleRenderedScenes).coerceAtLeast(0)
    var renderedCount = size - firstRenderedIndex
    while (
        firstRenderedIndex > 0 &&
        renderedCount < MaxRenderedScenes &&
        this[firstRenderedIndex].transition !is SceneTransition.None
    ) {
        firstRenderedIndex -= 1
        renderedCount += 1
    }

    return subList(firstRenderedIndex, size)
}

@Composable
private fun SceneOverlay(
    scene: Scene<*>,
    zIndex: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(zIndex)
            .graphicsLayer {
                alpha = NavigationDimAmount * scene.progress.coerceIn(0f, 1f)
            }
            .background(Color.Black),
    )
}

@Composable
private fun <T : NavKey> SceneTransitionEffect(
    navigationPath: NavigationPath<T>,
    scene: Scene<T>,
    sharedElement: Boolean,
    elasticSharedElement: Boolean,
) {
    val transition = scene.transition
    LaunchedEffect(scene.id, transition, sharedElement, elasticSharedElement) {
        when (transition) {
            is SceneTransition.Enter -> {
                scene.loadingState = SceneLoadingState.Loaded
                val handoverVelocity = scene.beginTransitionAnimation()
                scene.transitionAnimation.animateTo(
                    targetValue = 1f,
                    animationSpec = if (elasticSharedElement) {
                        navigationSharedElementEnterSpring()
                    } else {
                        navigationEnterExitSpring()
                    },
                    initialVelocity = handoverVelocity,
                )
                if (scene.transition === transition) {
                    scene.transition = SceneTransition.None
                }
            }

            is SceneTransition.Exit -> {
                val handoverVelocity = scene.beginTransitionAnimation()
                scene.transitionAnimation.animateTo(
                    targetValue = 0f,
                    animationSpec = if (elasticSharedElement) {
                        navigationSharedElementExitSpring()
                    } else {
                        navigationEnterExitSpring()
                    },
                    initialVelocity = handoverVelocity,
                )
                if (scene.transition === transition) {
                    navigationPath.remove(scene)
                }
            }

            is SceneTransition.DragEnd -> {
                val target = if (transition.commit) 0f else 1f
                scene.beginTransitionAnimation()
                scene.transitionAnimation.animateTo(
                    targetValue = target,
                    animationSpec = when {
                        !elasticSharedElement -> navigationDragSettleSpring()
                        // Closing lands elastically; snapping back open does not, the same way the
                        // open itself does not.
                        transition.commit -> navigationSharedElementDragSettleSpring()
                        else -> navigationSharedElementDragCancelSpring()
                    },
                    // The gesture's velocity is reported in back-progress units, which run
                    // opposite to the scene's own progress, so hand it over negated and let
                    // the spring carry the fling through.
                    initialVelocity = -transition.velocityProgressPerSecond,
                )
                if (scene.transition === transition) {
                    if (transition.commit) {
                        navigationPath.remove(scene)
                    } else {
                        scene.transition = SceneTransition.None
                    }
                }
            }

            SceneTransition.Drag,
            SceneTransition.None -> Unit
        }
    }
}

@Composable
private fun <T : NavKey> RenderScene(
    navigationPath: NavigationPath<T>,
    scene: Scene<T>,
    entryProvider: (T) -> NavEntry<T>,
    saveableStateHolder: SaveableStateHolder,
    saveableKey: Any,
    sceneViewModelStores: MutableMap<Any, ViewModelStore>,
    modifier: Modifier,
    sharedScope: Any,
    sharedTransitionActive: Boolean,
    sharedHostTargetVisible: Boolean,
    sharedTransitionProgress: Float,
    sharedTransitionDirection: SharedTransitionDirection,
    isTransitionBackground: Boolean,
    isForeground: Boolean,
) {
    val entry = remember(scene.route, entryProvider) {
        entryProvider(scene.route)
    }
    // Scope ViewModel creation inside the scene to this scene's own ViewModelStore, which
    // SceneDisplay clears when the scene is popped. The factory/extras are delegated to the
    // enclosing owner so Hilt-style injection keeps working exactly as before.
    val delegateOwner = LocalViewModelStoreOwner.current
    val delegateFactory = delegateOwner as? HasDefaultViewModelProviderFactory
    val sceneStore = remember(scene.id, sceneViewModelStores) {
        sceneViewModelStores.getOrPut(scene.id) { ViewModelStore() }
    }
    val viewModelStoreOwner: ViewModelStoreOwner = remember(scene.id, delegateFactory) {
        val factory = delegateFactory
        if (factory != null) {
            SceneViewModelStoreOwner(sceneStore, factory)
        } else {
            // The host always exposes a ViewModelProvider.Factory, so this branch is only
            // reached if there is no ViewModelStoreOwner at all - in which case ViewModel
            // creation would have thrown regardless. Fail loudly instead of providing null.
            delegateOwner ?: error("No ViewModelStoreOwner available for scene ${scene.id}")
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        key(scene.id) {
            saveableStateHolder.SaveableStateProvider(saveableKey) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides viewModelStoreOwner,
                    LocalNavigationPath provides navigationPath,
                    LocalSharedTransitionScope provides
                        if (sharedTransitionActive) sharedScope else null,
                    LocalSharedHostTargetVisible provides sharedHostTargetVisible,
                    LocalSharedTransitionActive provides sharedTransitionActive,
                    LocalSharedTransitionProgress provides sharedTransitionProgress,
                    LocalSharedTransitionDirection provides sharedTransitionDirection,
                    LocalHostIsTransitionBackground provides isTransitionBackground,
                    LocalSceneIsForeground provides isForeground,
                ) {
                    entry.Content()
                }
            }
        }
    }
}

/**
 * A [ViewModelStoreOwner] backed by a per-scene [ViewModelStore] but delegating
 * [HasDefaultViewModelProviderFactory] (the Hilt factory and creation extras) to the enclosing
 * owner. This lets `hiltViewModel()` create VMs through Hilt as usual while storing them in the
 * scene's own store, so they are cleared when the scene leaves the backstack.
 */
private class SceneViewModelStoreOwner(
    override val viewModelStore: ViewModelStore,
    private val factoryDelegate: HasDefaultViewModelProviderFactory,
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = factoryDelegate.defaultViewModelProviderFactory
    override val defaultViewModelCreationExtras: CreationExtras
        get() = factoryDelegate.defaultViewModelCreationExtras
}

internal fun Modifier.sceneTransform(
    scene: Scene<*>,
    followingScenes: List<Scene<*>>,
    zIndex: Float,
    enabled: Boolean = true,
): Modifier = horizontalSlideSceneTransform(
    scene = scene,
    followingScenes = followingScenes,
    zIndex = zIndex,
    enabled = enabled,
)

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private const val IdleRenderedScenes = 2
private const val MaxRenderedScenes = 3
private const val NavigationDimAmount = 0.42f

/**
 * How far past 0..1 a shared transition's progress may run. Wide enough for the elastic settle of
 * [navigationSharedElementExitSpring] plus whatever a released fling adds, and narrow enough that a
 * host reading it cannot be handed a wild value.
 */
private const val SharedProgressOvershootBand = 0.35f
