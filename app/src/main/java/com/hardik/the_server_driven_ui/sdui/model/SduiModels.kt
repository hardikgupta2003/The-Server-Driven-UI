package com.hardik.the_server_driven_ui.sdui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Top-level envelope. See SCHEMA.md for the full rationale.
 */
@Serializable
data class PageSchema(
    val schemaVersion: Int,
    val minClientVersion: Int = 1,
    val page: PageContent
)

@Serializable
data class PageContent(
    val id: String,
    val title: String? = null,
    val sections: List<SduiNode> = emptyList()
)

/**
 * The one recursive unit every visible thing on the page is made of.
 * `props` is intentionally a generic map so an unrecognized key never fails
 * parsing — only an unrecognized `type` falls back (see UnknownComponent).
 */
@Serializable
data class SduiNode(
    val id: String,
    val type: String,
    val minClientVersion: Int? = null,
    val style: SduiStyle? = null,
    val props: Map<String, JsonElement> = emptyMap(),
    val children: List<SduiNode> = emptyList(),
    val dataBinding: DataBinding? = null,
    val actions: Map<String, SduiAction> = emptyMap()
)

/**
 * Precomputed content variants keyed by a state value, e.g. the category
 * chip selection swapping which CarCards a CarouselRail shows. The server
 * ships every variant; the client never runs a filter query.
 */
@Serializable
data class DataBinding(
    val stateKey: String,
    val variants: Map<String, List<SduiNode>> = emptyMap()
)

@Serializable
data class SduiAction(
    val type: String,
    val target: String? = null,
    val payload: Map<String, JsonElement> = emptyMap(),
    val payloadFromEvent: String? = null
)

/**
 * Shared, component-agnostic visual props. Every component reads from the
 * same model so styling is never reimplemented per component type.
 * Padding/margin are [start, top, end, bottom] in dp.
 */
@Serializable
data class SduiStyle(
    val padding: List<Int>? = null,
    val margin: List<Int>? = null,
    val background: String? = null,
    val cornerRadius: Int? = null,
    val elevation: Int? = null,
    val alignment: String? = null,
    val width: String? = null,
    val height: String? = null
)
