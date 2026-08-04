package com.hardik.the_server_driven_ui.sdui.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Top-level envelope. See SCHEMA.md for the full rationale.
 */
@Immutable
@Serializable
data class PageSchema(
    val schemaVersion: Int,
    val minClientVersion: Int = 1,
    val page: PageContent
)

@Immutable
@Serializable
data class PageContent(
    val id: String,
    val title: String? = null,
    /**
     * Optional collapsing-header region, rendered above the scrollable
     * [sections] rather than as part of them. When present, scrolling
     * [sections] drives a shared collapse fraction (0f expanded, 1f
     * collapsed) that any descendant component may read — e.g. a node
     * with `props.collapseBehavior = "hide"` shrinks away entirely, while
     * `ChipRow`'s icon-tab variant shrinks just its icons and keeps
     * labels. Omit this field entirely for a plain non-collapsing page —
     * existing payloads with no `header` key behave exactly as before.
     */
    val header: SduiNode? = null,
    val sections: List<SduiNode> = emptyList()
)

/**
 * The one recursive unit every visible thing on the page is made of.
 * `props` is intentionally a generic map so an unrecognized key never fails
 * parsing — only an unrecognized `type` falls back (see UnknownComponent).
 *
 * `@Immutable`: every node here is parsed once from JSON and never
 * mutated afterward — changes always go through `.copy()` (see
 * `resolveDataBinding`/`resolveColorBinding` in `SduiRenderer.kt`), never
 * in-place mutation of `props`/`children`/`actions`. Without this
 * annotation the Compose compiler treats `Map`/`List`-bearing classes as
 * unstable by default (it can't prove immutability from the interface
 * alone), which disables recomposition skipping for every component
 * function — they all take an `SduiNode` as their first parameter.
 */
@Immutable
@Serializable
data class SduiNode(
    val id: String,
    val type: String,
    val minClientVersion: Int? = null,
    val style: SduiStyle? = null,
    val props: Map<String, JsonElement> = emptyMap(),
    val children: List<SduiNode> = emptyList(),
    val dataBinding: DataBinding? = null,
    /** Same variant-map idea as [dataBinding], but swaps `style.background` instead of `children` — e.g. the header recoloring per selected nav tab. */
    val colorBinding: ColorBinding? = null,
    val actions: Map<String, SduiAction> = emptyMap()
)

/**
 * Precomputed content variants keyed by a state value, e.g. the category
 * chip selection swapping which CarCards a CarouselRail shows. The server
 * ships every variant; the client never runs a filter query.
 */
@Immutable
@Serializable
data class DataBinding(
    val stateKey: String,
    val variants: Map<String, List<SduiNode>> = emptyMap()
)

/**
 * Precomputed hex-color variants keyed by a state value — the same
 * "server ships every option, client just picks one" principle as
 * [DataBinding], applied to a style property instead of content. Used for
 * things like a header row recoloring to match the selected nav tab's
 * theme, without a general "bind any style field" mechanism.
 */
@Immutable
@Serializable
data class ColorBinding(
    val stateKey: String,
    val variants: Map<String, String> = emptyMap()
)

@Immutable
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
 *
 * `justifyContent`/`alignItems` are generic main-/cross-axis controls any
 * layout primitive (`Row`, `Column`, ...) can read — one flex-style vocabulary
 * shared across container types, instead of one-off props bolted onto a
 * single component (e.g. `Row`'s old `arrangement` prop, still read as a
 * fallback for older payloads — see `Primitives.kt`).
 */
@Immutable
@Serializable
data class SduiStyle(
    val padding: List<Int>? = null,
    val margin: List<Int>? = null,
    val background: String? = null,
    val cornerRadius: Int? = null,
    val elevation: Int? = null,
    /** Text alignment for leaf text nodes: "start" | "center" | "end". */
    val alignment: String? = null,
    /** "match" | "wrap" | a plain number treated as dp. */
    val width: String? = null,
    val height: String? = null,
    /** Main-axis distribution: "start" | "end" | "center" | "spaceBetween" | "spaceAround" | "spaceEvenly". */
    val justifyContent: String? = null,
    /** Cross-axis alignment: "start" | "center" | "end". */
    val alignItems: String? = null,
    val opacity: Float? = null,
    val borderWidth: Int? = null,
    val borderColor: String? = null,
    val rotation: Float? = null,
)
