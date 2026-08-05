package com.hardik.the_server_driven_ui.sdui.action

import com.hardik.the_server_driven_ui.sdui.model.SduiAction
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.state.SduiStateStore
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers the three action types with real, non-Log-calling code paths
 * (`updateState`, `navigate`, `openSheet`). The `"openUrl"/"apiCall"/
 * "none"` reserved branch and the unknown-type branch both call
 * `android.util.Log`, which throws in a plain JVM unit test without
 * Robolectric — deliberately not exercised here for that reason, same
 * scoping call as `SduiValidatorTest`.
 */
class ActionDispatcherTest {

    private fun dispatcher(
        stateStore: SduiStateStore = SduiStateStore(),
        nodeIndex: Map<String, SduiNode> = emptyMap(),
        onNavigate: (String, Map<String, String>) -> Unit = { _, _ -> },
    ) = ActionDispatcher(stateStore, nodeIndex, onNavigate)

    @Test
    fun `updateState writes payloadFromEvent field to the target state key`() {
        val stateStore = SduiStateStore()
        val action = SduiAction(type = "updateState", target = "selectedBodyType", payloadFromEvent = "value")
        dispatcher(stateStore).dispatch(action, JsonObject(mapOf("value" to JsonPrimitive("suv"))))
        assertEquals("suv", stateStore.get("selectedBodyType"))
    }

    @Test
    fun `updateState falls back to a raw JsonPrimitive event payload when payloadFromEvent is unset`() {
        val stateStore = SduiStateStore()
        val action = SduiAction(type = "updateState", target = "challanRegNumber")
        dispatcher(stateStore).dispatch(action, JsonPrimitive("KA01AB1234"))
        assertEquals("KA01AB1234", stateStore.get("challanRegNumber"))
    }

    @Test
    fun `updateState with no target is a no-op, not a crash`() {
        val stateStore = SduiStateStore()
        val action = SduiAction(type = "updateState", target = null)
        dispatcher(stateStore).dispatch(action, JsonPrimitive("value"))
        assertNull(stateStore.get("anything"))
    }

    @Test
    fun `navigate extracts route and forwards the remaining payload as params`() {
        var capturedRoute: String? = null
        var capturedParams: Map<String, String>? = null
        val action = SduiAction(
            type = "navigate",
            payload = mapOf(
                "route" to JsonPrimitive("car_detail"),
                "carId" to JsonPrimitive("used_car_swift_2017"),
            ),
        )
        dispatcher(onNavigate = { route, params -> capturedRoute = route; capturedParams = params }).dispatch(action, null)
        assertEquals("car_detail", capturedRoute)
        assertEquals(mapOf("carId" to "used_car_swift_2017"), capturedParams)
    }

    @Test
    fun `navigate with no route is a no-op`() {
        var called = false
        val action = SduiAction(type = "navigate", payload = emptyMap())
        dispatcher(onNavigate = { _, _ -> called = true }).dispatch(action, null)
        assertEquals(false, called)
    }

    @Test
    fun `openSheet looks the target up in the node index and opens it`() {
        val sheetNode = SduiNode(id = "sheet_emi_creta", type = "Column")
        val stateStore = SduiStateStore()
        val action = SduiAction(type = "openSheet", target = "sheet_emi_creta")
        dispatcher(stateStore, nodeIndex = mapOf("sheet_emi_creta" to sheetNode)).dispatch(action, null)
        assertEquals(sheetNode, stateStore.sheetContent.value)
    }

    @Test
    fun `openSheet with an unresolvable target closes any existing sheet instead of crashing`() {
        val stateStore = SduiStateStore()
        val action = SduiAction(type = "openSheet", target = "does_not_exist")
        dispatcher(stateStore).dispatch(action, null)
        assertNull(stateStore.sheetContent.value)
    }

    @Test
    fun `buildNodeIndex flattens children and dataBinding variants`() {
        val variantChild = SduiNode(id = "variant_child", type = "Text")
        val regularChild = SduiNode(id = "regular_child", type = "Text")
        val root = SduiNode(
            id = "root",
            type = "Column",
            children = listOf(regularChild),
            dataBinding = com.hardik.the_server_driven_ui.sdui.model.DataBinding(
                stateKey = "tab",
                variants = mapOf("all" to listOf(variantChild)),
            ),
        )
        val index = buildNodeIndex(listOf(root))
        assertEquals(root, index["root"])
        assertEquals(regularChild, index["regular_child"])
        assertEquals(variantChild, index["variant_child"])
    }
}
