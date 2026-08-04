package com.hardik.the_server_driven_ui.sdui.validation

import android.util.Log
import com.hardik.the_server_driven_ui.sdui.model.SduiAction
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.model.intOr
import com.hardik.the_server_driven_ui.sdui.renderer.ComponentRegistry

private const val TAG = "SduiValidator"

/**
 * Validates a parsed page tree *before* it reaches the renderer, the same
 * "catch bad data before rendering, degrade instead of crash" idea as
 * AmniX/SDUI.kt's SduiValidator — adapted to our own node shape rather than
 * ported. UnknownComponent already makes an unrecognized `type` safe at
 * render time; this catches the classes of bad JSON that would otherwise
 * either crash (duplicate ids under the same `key()` in a lazy list) or
 * silently do nothing with no way to tell why (a ChipRow with no
 * `onSelect.target`, an `openSheet` pointing at an id that doesn't exist).
 */
object SduiValidator {

    enum class Severity { WARNING, ERROR }

    data class Issue(val nodeId: String, val severity: Severity, val message: String)

    data class Report(val issues: List<Issue>, val sanitizedSections: List<SduiNode>)

    /** Type used for a node whose own `type` field was missing/blank — routes to UnknownComponent. */
    const val MISSING_TYPE_SENTINEL = "__invalid_missing_type__"

    /**
     * @param registry optional — when passed, an unregistered `type` is
     * reported as a WARNING here (in addition to falling back safely at
     * render time), so a bad payload shows up in logs immediately instead
     * of only as a quiet blank section.
     */
    fun validate(sections: List<SduiNode>, registry: ComponentRegistry? = null): Report {
        val allIds = mutableSetOf<String>()
        fun collectIds(node: SduiNode) {
            allIds += node.id
            node.children.forEach(::collectIds)
            node.dataBinding?.variants?.values?.forEach { variant -> variant.forEach(::collectIds) }
        }
        sections.forEach(::collectIds)

        val issues = mutableListOf<Issue>()
        val seenIds = mutableSetOf<String>()

        fun validateAction(node: SduiNode, action: SduiAction) {
            when (action.type) {
                "openSheet" -> {
                    val target = action.target
                    if (target.isNullOrBlank()) {
                        issues += Issue(node.id, Severity.ERROR, "openSheet action has no target")
                    } else if (target !in allIds) {
                        issues += Issue(node.id, Severity.ERROR, "openSheet target '$target' does not exist anywhere in the page")
                    }
                }
                "updateState" -> {
                    if (action.target.isNullOrBlank()) {
                        issues += Issue(node.id, Severity.WARNING, "updateState action has no target state key")
                    }
                }
            }
        }

        fun sanitize(node: SduiNode): SduiNode? {
            if (node.id in seenIds) {
                issues += Issue(node.id, Severity.ERROR, "Duplicate id '${node.id}' — second occurrence dropped (duplicate keys under the same parent would crash a lazy list)")
                return null
            }
            seenIds += node.id

            val effectiveType = node.type.ifBlank {
                issues += Issue(node.id, Severity.ERROR, "Missing/blank 'type' — falls back to UnknownComponent")
                MISSING_TYPE_SENTINEL
            }

            if (registry != null && effectiveType != MISSING_TYPE_SENTINEL && effectiveType !in registry.registeredTypes) {
                issues += Issue(node.id, Severity.WARNING, "Unrecognized type '$effectiveType' — will render via UnknownComponent fallback")
            }

            if (effectiveType == "ChipRow") {
                val onSelect = node.actions["onSelect"]
                if (onSelect?.target.isNullOrBlank()) {
                    issues += Issue(node.id, Severity.WARNING, "ChipRow has no actions.onSelect.target — selection won't update any state")
                }
            }

            if (effectiveType == "Grid") {
                val columns = node.props.intOr("columns", 2)
                if (columns <= 0) {
                    issues += Issue(node.id, Severity.WARNING, "Grid columns=$columns is invalid, will be clamped to 1 at render time")
                }
            }

            node.dataBinding?.let { binding ->
                if (binding.variants.isEmpty()) {
                    issues += Issue(node.id, Severity.WARNING, "dataBinding.variants is empty for stateKey '${binding.stateKey}' — node will render no children until a variant is added")
                }
            }

            node.actions.values.forEach { action -> validateAction(node, action) }

            val sanitizedChildren = node.children.mapNotNull(::sanitize)
            val sanitizedVariants = node.dataBinding?.variants?.mapValues { (_, variantNodes) -> variantNodes.mapNotNull(::sanitize) }

            return node.copy(
                type = effectiveType,
                children = sanitizedChildren,
                dataBinding = node.dataBinding?.copy(variants = sanitizedVariants ?: emptyMap()),
            )
        }

        val sanitizedSections = sections.mapNotNull(::sanitize)
        return Report(issues, sanitizedSections)
    }

    /** Convenience: validate and log every issue, returning only the sanitized tree. */
    fun validateAndLog(sections: List<SduiNode>, registry: ComponentRegistry? = null): List<SduiNode> {
        val report = validate(sections, registry)
        report.issues.forEach { issue ->
            val line = "[${issue.nodeId}] ${issue.message}"
            if (issue.severity == Severity.ERROR) Log.e(TAG, line) else Log.w(TAG, line)
        }
        return report.sanitizedSections
    }
}
