package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hardik.the_server_driven_ui.sdui.action.ActionDispatcher
import com.hardik.the_server_driven_ui.sdui.model.PageContent
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.state.SduiStateStore

/**
 * Renders a whole page: a scrollable column of top-level sections. Each
 * section is rendered through [RenderNode], which is the single recursive
 * dispatch point every nested child also goes through.
 */
@Composable
fun SduiPage(
    page: PageContent,
    registry: ComponentRegistry,
    stateStore: SduiStateStore,
    actionDispatcher: ActionDispatcher,
) {
    val currentState by stateStore.values.collectAsState()

    LazyColumn {
        items(page.sections, key = { it.id }) { section ->
            RenderNode(
                node = section,
                registry = registry,
                currentState = currentState,
                actionDispatcher = actionDispatcher,
            )
        }
    }
}

/**
 * The one recursive dispatch point. Resolves any dataBinding against
 * current state, looks the node's type up in the registry, and falls back
 * to [UnknownComponent] on a miss — this is the line that guarantees the
 * page never crashes on a type it doesn't recognize.
 */
@Composable
fun RenderNode(
    node: SduiNode,
    registry: ComponentRegistry,
    currentState: Map<String, String>,
    actionDispatcher: ActionDispatcher,
) {
    val resolved = resolveDataBinding(node, currentState)
    val component = registry.resolve(resolved.type) ?: unknownComponent

    val context = RenderContext(
        currentState = currentState,
        dispatch = { action, eventPayload -> actionDispatcher.dispatch(action, eventPayload) },
        renderChild = { child ->
            RenderNode(
                node = child,
                registry = registry,
                currentState = currentState,
                actionDispatcher = actionDispatcher,
            )
        },
    )

    component(resolved, context)
}

/**
 * Precomputed content variants (e.g. category-chip selection swapping a
 * rail's cars) resolve here: the server ships every variant, the client
 * only picks one. No runtime filter/query logic on the client.
 */
private fun resolveDataBinding(node: SduiNode, state: Map<String, String>): SduiNode {
    val binding = node.dataBinding ?: return node
    val selected = state[binding.stateKey]
    val variant = binding.variants[selected] ?: binding.variants["default"] ?: return node
    return node.copy(children = variant)
}
