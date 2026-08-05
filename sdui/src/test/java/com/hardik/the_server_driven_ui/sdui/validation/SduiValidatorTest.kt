package com.hardik.the_server_driven_ui.sdui.validation

import com.hardik.the_server_driven_ui.sdui.model.DataBinding
import com.hardik.the_server_driven_ui.sdui.model.SduiAction
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests `SduiValidator.validate` — the pure function, not `validateAndLog`,
 * since the latter calls `android.util.Log`, which throws in a plain JVM
 * unit test without Robolectric. That's a deliberate scoping choice, not
 * an oversight: `validate`'s `Report` is what actually carries the
 * behavior worth testing (which issues fire, what gets sanitized);
 * `validateAndLog` is a thin logging wrapper around it.
 */
class SduiValidatorTest {

    private fun leaf(id: String, type: String = "Text") = SduiNode(id = id, type = type)

    @Test
    fun `valid page produces no issues`() {
        val sections = listOf(
            SduiNode(id = "root", type = "Column", children = listOf(leaf("child_1"), leaf("child_2"))),
        )
        val report = SduiValidator.validate(sections)
        assertTrue(report.issues.isEmpty())
        assertEquals(1, report.sanitizedSections.size)
    }

    @Test
    fun `duplicate id is dropped and reported as an error`() {
        val sections = listOf(
            SduiNode(id = "root", type = "Column", children = listOf(leaf("dup"), leaf("dup"))),
        )
        val report = SduiValidator.validate(sections)
        val dupIssue = report.issues.singleOrNull { it.message.contains("Duplicate id") }
        assertTrue("expected a duplicate-id issue", dupIssue != null)
        assertEquals(SduiValidator.Severity.ERROR, dupIssue!!.severity)
        // second "dup" child is dropped, not just flagged
        assertEquals(1, report.sanitizedSections.single().children.size)
    }

    @Test
    fun `missing type falls back to the sentinel and is reported`() {
        val sections = listOf(SduiNode(id = "no_type", type = ""))
        val report = SduiValidator.validate(sections)
        assertTrue(report.issues.any { it.nodeId == "no_type" && it.message.contains("Missing/blank 'type'") })
        assertEquals(SduiValidator.MISSING_TYPE_SENTINEL, report.sanitizedSections.single().type)
    }

    @Test
    fun `openSheet action with no target is an error`() {
        val node = SduiNode(
            id = "btn",
            type = "Button",
            actions = mapOf("onClick" to SduiAction(type = "openSheet", target = null)),
        )
        val report = SduiValidator.validate(listOf(node))
        assertTrue(report.issues.any { it.nodeId == "btn" && it.message.contains("openSheet action has no target") })
    }

    @Test
    fun `openSheet action pointing at a nonexistent id is an error`() {
        val node = SduiNode(
            id = "btn",
            type = "Button",
            actions = mapOf("onClick" to SduiAction(type = "openSheet", target = "does_not_exist")),
        )
        val report = SduiValidator.validate(listOf(node))
        assertTrue(report.issues.any { it.severity == SduiValidator.Severity.ERROR && it.message.contains("does not exist anywhere in the page") })
    }

    @Test
    fun `openSheet target that exists elsewhere in the tree is valid`() {
        val sheetContent = leaf("sheet_content")
        val button = SduiNode(
            id = "btn",
            type = "Button",
            actions = mapOf("onClick" to SduiAction(type = "openSheet", target = "sheet_content")),
            children = listOf(sheetContent),
        )
        val report = SduiValidator.validate(listOf(button))
        assertTrue(report.issues.none { it.message.contains("openSheet") })
    }

    @Test
    fun `updateState action with no target is a warning, not an error`() {
        val node = SduiNode(
            id = "chip_row",
            type = "ChipRow",
            actions = mapOf("onSelect" to SduiAction(type = "updateState", target = null)),
        )
        val report = SduiValidator.validate(listOf(node))
        val issue = report.issues.single { it.message.contains("updateState action has no target") }
        assertEquals(SduiValidator.Severity.WARNING, issue.severity)
    }

    @Test
    fun `ChipRow with no onSelect target is flagged`() {
        val node = SduiNode(id = "chip_row", type = "ChipRow")
        val report = SduiValidator.validate(listOf(node))
        assertTrue(report.issues.any { it.message.contains("ChipRow has no actions.onSelect.target") })
    }

    @Test
    fun `Grid with non-positive columns is flagged`() {
        val node = SduiNode(id = "grid", type = "Grid")
        // columns defaults to 2 when absent, so use props to force an invalid value
        val invalidGrid = node.copy(props = mapOf("columns" to kotlinx.serialization.json.JsonPrimitive(0)))
        val report = SduiValidator.validate(listOf(invalidGrid))
        assertTrue(report.issues.any { it.message.contains("Grid columns=0 is invalid") })
    }

    @Test
    fun `empty dataBinding variants is flagged`() {
        val node = SduiNode(id = "tab_body", type = "Column", dataBinding = DataBinding(stateKey = "selectedTab", variants = emptyMap()))
        val report = SduiValidator.validate(listOf(node))
        assertTrue(report.issues.any { it.message.contains("dataBinding.variants is empty") })
    }

    @Test
    fun `duplicate id inside a dataBinding variant is caught too`() {
        val node = SduiNode(
            id = "tab_body",
            type = "Column",
            dataBinding = DataBinding(
                stateKey = "selectedTab",
                variants = mapOf("all" to listOf(leaf("row_a"), leaf("row_a"))),
            ),
        )
        val report = SduiValidator.validate(listOf(node))
        assertTrue(report.issues.any { it.message.contains("Duplicate id 'row_a'") })
    }
}
