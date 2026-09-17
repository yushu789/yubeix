// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.navigation

import androidx.compose.runtime.Composable

/*
 * Minimal, API-compatible port of the `androidx.navigation3.runtime` essentials the scene
 * navigation is built on: [NavKey], [NavEntry], and the [entryProvider] DSL. Hosts declaring
 * routes with `sealed interface Screen : NavKey` and registering them with
 * `entryProvider<Screen> { entry(Screen.Main) { ... } }` keep the exact same code shape they
 * would write against navigation3 itself.
 *
 * NavKey itself is a marker interface, so routes stay `@Serializable` data classes/objects and
 * hosts keep owning their persistence story (e.g. a kotlinx.serialization based Saver).
 */

/**
 * Marker interface for navigation destinations. Convention: declare routes as a sealed
 * hierarchy implementing [NavKey], so the back stack ([NavigationPath]) can hold any route.
 */
interface NavKey

/**
 * The content and metadata for a single navigation destination, resolved from a route by
 * [entryProvider]. Hosts rarely construct these directly - use the [entry] DSL.
 */
class NavEntry<T : NavKey>(
    val route: T,
    val metadata: Map<String, Any> = emptyMap(),
    private val content: @Composable (T) -> Unit,
) {
    /**
     * Renders the entry's content. Called by [SceneDisplay] while the entry's scene is on
     * screen; the composition is scoped so `rememberSaveable` and ViewModels resolve per scene.
     */
    @Composable
    fun Content() {
        content(route)
    }
}

/**
 * DSL receiver for [entryProvider]. Register one [entry] per destination.
 */
class EntryProviderBuilder<T : NavKey> {

    private val entriesByClass = mutableMapOf<kotlin.reflect.KClass<out T>, (T) -> NavEntry<T>>()
    private val entriesByInstance = mutableMapOf<T, NavEntry<T>>()
    var onNoRouteFound: ((T) -> NavEntry<T>)? = null

    /**
     * Registers content for every route that is an instance of [A]. Best for data routes:
     * `entry<Screen.WordList> { screen -> ... }`.
     */
    inline fun <reified A : T> entry(
        metadata: Map<String, Any> = emptyMap(),
        noinline content: @Composable (A) -> Unit,
    ) {
        entry(A::class, metadata, content)
    }

    /**
     * Registers content for the route class [klass]. The non-reified form of [entry].
     */
    fun <A : T> entry(
        klass: kotlin.reflect.KClass<A>,
        metadata: Map<String, Any> = emptyMap(),
        content: @Composable (A) -> Unit,
    ) {
        entriesByClass[klass] = { route -> NavEntry(route, metadata) { content(route as A) } }
    }

    /**
     * Registers content for the exact route instance [route]. Best for object routes:
     * `entry(Screen.Main) { ... }`.
     */
    fun entry(
        route: T,
        metadata: Map<String, Any> = emptyMap(),
        content: @Composable (T) -> Unit,
    ) {
        entriesByInstance[route] = NavEntry(route, metadata) { content(route) }
    }

    internal fun build(): (T) -> NavEntry<T> = { route ->
        entriesByInstance[route]
            ?: entriesByClass[route::class]?.invoke(route)
            ?: onNoRouteFound?.invoke(route)
            ?: throw IllegalArgumentException(
                "No entry registered for route ${route::class.simpleName} - " +
                    "add entry(route) { } or entry<${route::class.simpleName}> { } in entryProvider.",
            )
    }
}

/**
 * Builds a route-to-entry resolver for [SceneDisplay]. Inside the block, register one entry
 * per destination with [EntryProviderBuilder.entry].
 */
fun <T : NavKey> entryProvider(
    builder: EntryProviderBuilder<T>.() -> Unit,
): (T) -> NavEntry<T> {
    val spec = EntryProviderBuilder<T>().apply(builder)
    return spec.build()
}
