package com.hardik.the_server_driven_ui.sdui.renderer

import com.hardik.the_server_driven_ui.sdui.model.ColorBinding
import com.hardik.the_server_driven_ui.sdui.model.DataBinding
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.model.SduiStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * `resolveDataBinding`/`resolveColorBinding` are the two functions that
 * make a chip selection or a nav-tab switch actually change what renders
 * — this is the mechanism behind every `dataBinding`/`colorBinding` node
 * in `landing_page.json`, tested directly rather than only indirectly
 * through a running app.
 */
class DataBindingResolutionTest {

    private val suvCar = SduiNode(id = "car_creta", type = "CarCard")
    private val sedanCar = SduiNode(id = "car_city", type = "CarCard")

    private val railWithBinding = SduiNode(
        id = "rail_popular_by_body_type",
        type = "CarouselRail",
        dataBinding = DataBinding(
            stateKey = "selectedBodyType",
            variants = mapOf(
                "suv" to listOf(suvCar),
                "sedan" to listOf(sedanCar),
                "default" to listOf(suvCar),
            ),
        ),
    )

    @Test
    fun `selects the variant matching current state`() {
        val resolved = resolveDataBinding(railWithBinding, mapOf("selectedBodyType" to "sedan"))
        assertEquals(listOf(sedanCar), resolved.children)
    }

    @Test
    fun `falls back to the default variant when state key is unset`() {
        val resolved = resolveDataBinding(railWithBinding, emptyMap())
        assertEquals(listOf(suvCar), resolved.children)
    }

    @Test
    fun `falls back to default when state value has no matching variant`() {
        val resolved = resolveDataBinding(railWithBinding, mapOf("selectedBodyType" to "hatchback_typo"))
        assertEquals(listOf(suvCar), resolved.children)
    }

    @Test
    fun `node with no dataBinding at all is returned unchanged`() {
        val plain = SduiNode(id = "plain", type = "Text")
        val resolved = resolveDataBinding(plain, mapOf("selectedBodyType" to "suv"))
        assertSame(plain, resolved)
    }

    @Test
    fun `neither selected state nor a default variant leaves the node unchanged`() {
        val noDefault = railWithBinding.copy(
            dataBinding = DataBinding(stateKey = "selectedBodyType", variants = mapOf("suv" to listOf(suvCar))),
        )
        val resolved = resolveDataBinding(noDefault, mapOf("selectedBodyType" to "sedan"))
        assertSame(noDefault, resolved)
    }

    private val headerWithColorBinding = SduiNode(
        id = "header_location_row",
        type = "Row",
        style = SduiStyle(background = "#392BCB"),
        colorBinding = ColorBinding(
            stateKey = "selectedNavTab",
            variants = mapOf(
                "all" to "#392BCB",
                "buy" to "#0A1A6B",
                "default" to "#392BCB",
            ),
        ),
    )

    @Test
    fun `colorBinding overrides style-background from the matching variant`() {
        val resolved = resolveColorBinding(headerWithColorBinding, mapOf("selectedNavTab" to "buy"))
        assertEquals("#0A1A6B", resolved.style?.background)
    }

    @Test
    fun `colorBinding falls back to default when state is unset`() {
        val resolved = resolveColorBinding(headerWithColorBinding, emptyMap())
        assertEquals("#392BCB", resolved.style?.background)
    }

    @Test
    fun `colorBinding preserves the rest of the style block, only swapping background`() {
        val withPadding = headerWithColorBinding.copy(
            style = headerWithColorBinding.style!!.copy(padding = listOf(12, 16, 12, 4)),
        )
        val resolved = resolveColorBinding(withPadding, mapOf("selectedNavTab" to "buy"))
        assertEquals(listOf(12, 16, 12, 4), resolved.style?.padding)
        assertEquals("#0A1A6B", resolved.style?.background)
    }

    @Test
    fun `node with no colorBinding is returned unchanged`() {
        val plain = SduiNode(id = "plain", type = "Row", style = SduiStyle(background = "#FFFFFF"))
        val resolved = resolveColorBinding(plain, mapOf("selectedNavTab" to "buy"))
        assertSame(plain, resolved)
    }
}
