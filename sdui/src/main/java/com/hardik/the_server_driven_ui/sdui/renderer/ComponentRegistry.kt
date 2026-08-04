package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.runtime.Composable
import com.hardik.the_server_driven_ui.sdui.model.SduiNode

/**
 * Context passed to every component composable: the shared bits a
 * component needs without knowing anything about its siblings or the page.
 */
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
