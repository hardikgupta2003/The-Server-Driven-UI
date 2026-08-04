package com.hardik.the_server_driven_ui.sdui.action

import android.util.Log
import com.hardik.the_server_driven_ui.sdui.model.SduiAction
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.state.SduiStateStore
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val TAG = "SduiActionDispatcher"

/**
 * Interprets SduiAction payloads. The renderer never contains any
 * component-specific interaction logic — every tap/select routes through
 * here so new interaction types are additive, not a rewrite.
 */
class ActionDispatcher(
    private val stateStore: SduiStateStore,
    private val nodeIndex: Map<String, SduiNode>,
    private val onNavigate: (route: String, params: Map<String, String>) -> Unit = { _, _ -> },
) {

    fun dispatch(action: SduiAction, eventPayload: JsonElement? = null) {
        when (action.type) {
            "updateState" -> handleUpdateState(action, eventPayload)
            "navigate" -> handleNavigate(action)
            "openSheet" -> handleOpenSheet(action)
            "openUrl", "apiCall", "none" -> {
                Log.d(TAG, "Action type '${action.type}' is reserved / no-op in this build.")
            }
            else -> Log.w(TAG, "Unknown action type '${action.type}' — ignored, no crash.")
        }
    }

    private fun handleUpdateState(action: SduiAction, eventPayload: JsonElement?) {
        val target = action.target ?: return
        val value = action.payloadFromEvent?.let { field ->
            (eventPayload as? JsonObject)?.get(field)?.let { (it as? JsonPrimitive)?.contentOrNull }
        } ?: (eventPayload as? JsonPrimitive)?.contentOrNull
        if (value != null) {
            stateStore.update(target, value)
        }
    }

    private fun handleNavigate(action: SduiAction) {
        val route = action.payload.str("route") ?: return
        val params = action.payload
            .filterKeys { it != "route" }
            .mapNotNull { (k, v) -> (v as? JsonPrimitive)?.contentOrNull?.let { k to it } }
            .toMap()
        onNavigate(route, params)
    }

    private fun handleOpenSheet(action: SduiAction) {
        val nodeId = action.target
        stateStore.openSheet(nodeId?.let { nodeIndex[it] })
    }
}

/** Flattens the page tree into an id -> node index once, for openSheet lookups. */
fun buildNodeIndex(nodes: List<SduiNode>): Map<String, SduiNode> {
    val index = mutableMapOf<String, SduiNode>()
    fun visit(node: SduiNode) {
        index[node.id] = node
        node.children.forEach(::visit)
        node.dataBinding?.variants?.values?.forEach { variant -> variant.forEach(::visit) }
    }
    nodes.forEach(::visit)
    return index
}
