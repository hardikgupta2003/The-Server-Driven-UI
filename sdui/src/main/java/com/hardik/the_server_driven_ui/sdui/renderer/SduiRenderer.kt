package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import com.hardik.the_server_driven_ui.sdui.action.ActionDispatcher
import com.hardik.the_server_driven_ui.sdui.model.PageContent
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.model.SduiStyle
import com.hardik.the_server_driven_ui.sdui.state.SduiStateStore

/**
 * Renders a whole page. Plain case: a scrollable column of top-level
 * sections. When the page also defines a `header`, that header renders
 * above the scroll and a nested-scroll connection drives
 * [LocalSduiCollapseFraction] from the body's scroll delta — see
 * [PageContent.header]'s kdoc for the mechanism and why it's generic
 * rather than page-specific.
 */
@Composable
fun SduiPage(
    page: PageContent,
    registry: ComponentRegistry,
    stateStore: SduiStateStore,
    actionDispatcher: ActionDispatcher,
    modifier: Modifier = Modifier,
) {
    val currentState by stateStore.values.collectAsState()
    val header = page.header

    if (header == null) {
        LazyColumn(modifier = modifier) {
            items(page.sections, key = { it.id }) { section ->
                RenderNode(node = section, registry = registry, currentState = currentState, actionDispatcher = actionDispatcher)
            }
        }
        return
    }

    // Natural (fully expanded) header height, measured once on first
    // layout — collapsing only ever shrinks reported height from there,
    // never the other way, so this capture happens while fraction is
    // still 0f. Plain (non-`by`) state references throughout this
    // function deliberately: SduiPage's own body never reads `.value` on
    // any of these, so scrolling never recomposes SduiPage itself either
    // — only the nestedScrollConnection callback (layout-phase, not
    // composition) writes them, and only the derivedStateOf below reads
    // them, whose *own* consumers only recompose if its calculated
    // output actually changes class (it never does here, by design —
    // see LocalSduiCollapseFraction's kdoc).
    val maxHeaderHeightPx = remember { mutableFloatStateOf(0f) }
    val headerOffsetPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val maxHeight = maxHeaderHeightPx.value
                if (maxHeight <= 0f) return Offset.Zero
                val current = headerOffsetPx.value
                val newOffset = (current + available.y).coerceIn(-maxHeight, 0f)
                headerOffsetPx.value = newOffset
                return Offset(0f, newOffset - current)
            }
        }
    }

    val collapseFractionState = remember {
        derivedStateOf {
            val maxHeight = maxHeaderHeightPx.value
            if (maxHeight > 0f) (-headerOffsetPx.value / maxHeight).coerceIn(0f, 1f) else 0f
        }
    }

    CompositionLocalProvider(LocalSduiCollapseFraction provides collapseFractionState) {
        Column(modifier = modifier) {
            Box(
                modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                    if (maxHeaderHeightPx.value == 0f) maxHeaderHeightPx.value = coordinates.size.height.toFloat()
                },
            ) {
                RenderNode(node = header, registry = registry, currentState = currentState, actionDispatcher = actionDispatcher)
            }
            LazyColumn(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
                items(page.sections, key = { it.id }) { section ->
                    RenderNode(node = section, registry = registry, currentState = currentState, actionDispatcher = actionDispatcher)
                }
            }
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
    val resolved = resolveColorBinding(resolveDataBinding(node, currentState), currentState)
    val component = registry.resolve(resolved.type) ?: unknownComponent

    // Memoized: registry/actionDispatcher are stable references from
    // MainActivity for the app's whole lifetime, and currentState keeps
    // the same Map instance except on a real state write (never during
    // pure scrolling). So during a scroll gesture this returns the exact
    // same RenderContext — same object, same dispatch/renderChild lambda
    // instances — letting every already-composed component below skip
    // recomposition instead of being handed "new" (by reference) lambdas
    // every frame. See RenderContext's kdoc for why that distinction is
    // what actually matters here.
    val context = remember(registry, currentState, actionDispatcher) {
        RenderContext(
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
    }

    component(resolved, context)
}

/**
 * Precomputed content variants (e.g. category-chip selection swapping a
 * rail's cars) resolve here: the server ships every variant, the client
 * only picks one. No runtime filter/query logic on the client.
 *
 * `internal` rather than `private` specifically so this pure function is
 * reachable from a plain JVM unit test without needing Compose test
 * infrastructure — see `sdui/src/test`.
 */
internal fun resolveDataBinding(node: SduiNode, state: Map<String, String>): SduiNode {
    val binding = node.dataBinding ?: return node
    val selected = state[binding.stateKey]
    val variant = binding.variants[selected] ?: binding.variants["default"] ?: return node
    return node.copy(children = variant)
}

/**
 * Resolves [com.hardik.the_server_driven_ui.sdui.model.ColorBinding] into
 * `style.background` — see its kdoc. `internal` for the same
 * unit-testability reason as [resolveDataBinding].
 */
internal fun resolveColorBinding(node: SduiNode, state: Map<String, String>): SduiNode {
    val binding = node.colorBinding ?: return node
    val selected = state[binding.stateKey]
    val hex = binding.variants[selected] ?: binding.variants["default"] ?: return node
    return node.copy(style = (node.style ?: SduiStyle()).copy(background = hex))
}
