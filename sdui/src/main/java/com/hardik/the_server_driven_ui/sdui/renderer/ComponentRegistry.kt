package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.hardik.the_server_driven_ui.sdui.model.SduiNode

/**
 * Context passed to every component composable: the shared bits a
 * component needs without knowing anything about its siblings or the page.
 *
 * `@Immutable` + memoized construction (see `RenderNode` in
 * `SduiRenderer.kt`) matter together here: every `SduiComponent` lambda
 * takes this as its second parameter, so if a *new* `RenderContext` (with
 * fresh `dispatch`/`renderChild` lambda instances) were built on every
 * single recomposition — which a naive `RenderContext(...)` call inside a
 * `@Composable` function body does — Compose could never skip
 * recomposing any component, since the lambda fields would never compare
 * equal to the previous call's. `RenderNode` remembers this object keyed
 * on the few things that actually change it, so the same instance (and
 * same lambdas) survive across recompositions where nothing relevant did.
 */
@Immutable
data class RenderContext(
    val currentState: Map<String, String>,
    val dispatch: (com.hardik.the_server_driven_ui.sdui.model.SduiAction, kotlinx.serialization.json.JsonElement?) -> Unit,
    val renderChild: @Composable (SduiNode) -> Unit
)

typealias SduiComponent = @Composable (node: SduiNode, context: RenderContext) -> Unit

/**
 * Server names a component type; client maps it to a native composable.
 * A miss returns null — callers fall back to UnknownComponent, never crash.
 */
class ComponentRegistry {
    private val components = mutableMapOf<String, SduiComponent>()

    fun register(type: String, component: SduiComponent) {
        components[type] = component
    }

    fun resolve(type: String): SduiComponent? = components[type]

    val registeredTypes: Set<String> get() = components.keys
}
