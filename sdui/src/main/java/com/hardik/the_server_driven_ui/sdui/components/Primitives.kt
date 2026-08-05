package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import com.hardik.the_server_driven_ui.sdui.model.intOr
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.model.strOrEmpty
import com.hardik.the_server_driven_ui.sdui.renderer.CollapsibleOnHide
import com.hardik.the_server_driven_ui.sdui.renderer.LocalSduiCollapseFraction
import com.hardik.the_server_driven_ui.sdui.renderer.SduiComponent
import com.hardik.the_server_driven_ui.sdui.renderer.parseHexColor
import com.hardik.the_server_driven_ui.sdui.renderer.toModifier

/**
 * `props.collapseBehavior = "hide"` opts any node into shrinking away as
 * a page's collapsing header collapses (see [CollapsibleOnHide]) — read
 * generically here rather than in the renderer, so any component could
 * adopt the same prop, not just Row/Column.
 */
@Composable
private fun collapseAwareWrapper(node: SduiNode, content: @Composable () -> Unit) {
    if (node.props.str("collapseBehavior") == "hide") {
        // .current here reads the stable State *object* (see
        // LocalSduiCollapseFraction's kdoc) — this composable does not
        // recompose as the user scrolls, only CollapsibleOnHide's
        // internal layout/draw-phase reads react per frame.
        CollapsibleOnHide(fractionState = LocalSduiCollapseFraction.current, content = content)
    } else {
        content()
    }
}

/**
 * Generic vertical container. `style.justifyContent`/`alignItems` control
 * main-/cross-axis layout — the same flex-style vocabulary `Row` reads, so
 * neither container needs its own one-off layout props.
 *
 * Defaults to full width (matching `Row`'s existing default) rather than
 * wrap-content: a Column whose only children are narrower-than-screen
 * leaves (e.g. a footer with just centered/left text, no full-width image
 * or rail underneath) would otherwise collapse to wrap just those
 * children's intrinsic width instead of spanning the page — `style.width`
 * still overrides this explicitly when a node actually wants wrap-content.
 */
val columnComponent: SduiComponent = { node, context ->
    collapseAwareWrapper(node) {
        // Any explicit `style.width` — not just "wrap" — must suppress the
        // full-width default rather than compose with it: chaining
        // `Modifier.fillMaxWidth().width(100.dp)` doesn't shrink to 100dp,
        // because fillMaxWidth already fixed min=max=parent width, and a
        // narrower fixed width afterward gets coerced back up to satisfy
        // that incoming constraint. Skipping the default whenever width is
        // set at all lets "wrap"/"match"/a fixed dp value fully own sizing.
        val base = if (node.style?.width != null) Modifier else Modifier.fillMaxWidth()
        Column(
            modifier = node.style.toModifier(base),
            verticalArrangement = verticalArrangementOf(node.style?.justifyContent),
            horizontalAlignment = horizontalAlignmentOf(node.style?.alignItems),
        ) {
            node.children.forEach { child -> context.renderChild(child) }
        }
    }
}

private fun horizontalArrangementOf(name: String?): Arrangement.Horizontal = when (name) {
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceEvenly" -> Arrangement.SpaceEvenly
    "spaceAround" -> Arrangement.SpaceAround
    "center" -> Arrangement.Center
    "end" -> Arrangement.End
    "start" -> Arrangement.Start
    else -> Arrangement.Start
}

private fun verticalArrangementOf(name: String?): Arrangement.Vertical = when (name) {
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceEvenly" -> Arrangement.SpaceEvenly
    "spaceAround" -> Arrangement.SpaceAround
    "center" -> Arrangement.Center
    "end" -> Arrangement.Bottom
    "start" -> Arrangement.Top
    else -> Arrangement.Top
}

private fun verticalAlignmentOf(name: String?): Alignment.Vertical = when (name) {
    "center" -> Alignment.CenterVertically
    "end" -> Alignment.Bottom
    "start" -> Alignment.Top
    else -> Alignment.CenterVertically
}

private fun horizontalAlignmentOf(name: String?): Alignment.Horizontal = when (name) {
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    "start" -> Alignment.Start
    else -> Alignment.Start
}

/**
 * Generic horizontal container. Prefers `style.justifyContent`/`alignItems`
 * (shared with `Column`); falls back to the older `props.arrangement`/
 * `verticalAlignment` so a payload written before this generalization still
 * renders unchanged — the same "old prop, new behavior lights up on
 * upgrade" forward-compatibility story `SCHEMA.md` describes for `props`.
 */
val rowComponent: SduiComponent = { node, context ->
    collapseAwareWrapper(node) {
        val justify = node.style?.justifyContent ?: node.props.str("arrangement")
        val align = node.style?.alignItems ?: node.props.str("verticalAlignment", "center")?.let {
            if (it == "top") "start" else "center"
        }
        // Same explicit-width fix as columnComponent — see its kdoc.
        val base = if (node.style?.width != null) Modifier else Modifier.fillMaxWidth()
        Row(
            modifier = node.style.toModifier(base),
            horizontalArrangement = horizontalArrangementOf(justify),
            verticalAlignment = verticalAlignmentOf(align),
        ) {
            node.children.forEach { child -> context.renderChild(child) }
        }
    }
}

/** Horizontally scrolling rail — used for banner carousels and car-card rails alike. */
val carouselRailComponent: SduiComponent = { node, context ->
    val trailingAction = node.actions["onTrailingAction"]
    Column(modifier = node.style.toModifier()) {
        node.props.str("title")?.let { title ->
            SectionTitle(
                title = title,
                badgeLabel = node.props.str("titleBadgeLabel"),
                trailingLabel = node.props.str("trailingActionLabel"),
                onTrailingClick = trailingAction?.let { action -> { context.dispatch(action, null) } },
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(node.children, key = { it.id }) { child ->
                context.renderChild(child)
            }
        }
    }
}

/**
 * Fixed-column vertical grid, e.g. category tiles or a car-card grid.
 *
 * Deliberately NOT a `LazyVerticalGrid`: laziness is only worth paying for
 * when items scroll into view on demand, but this grid already sets
 * `userScrollEnabled = false` (so it never becomes its own independently-
 * scrollable region nested inside the page's outer `LazyColumn` — a
 * same-direction nested-scroll conflict) — meaning every child composes
 * regardless. A lazy grid with laziness turned off still demands a fixed
 * pixel height up front (it can't measure "wrap content" without knowing
 * an item's size ahead of scrolling to it), which is what forced an
 * earlier version of this component to guess a per-cell height in dp and
 * multiply by row count — a guess that was wrong for every cell whose
 * real content (icon size, 1 vs 2 lines of label, a subtitle) didn't
 * match whatever cell the constant was tuned against, and would need a
 * new guess every time row/column count changed.
 *
 * Chunking children into plain `Row`s inside a `Column` sidesteps the
 * problem entirely: normal (non-lazy) layout measures each row's actual
 * content and wraps to it, so this scales to any row count, any column
 * count, any cell content, with no per-instance tuning.
 */
val gridComponent: SduiComponent = { node, context ->
    val columns = node.props.intOr("columns", 2).coerceAtLeast(1)
    val trailingAction = node.actions["onTrailingAction"]
    Column(modifier = node.style.toModifier()) {
        node.props.str("title")?.let { title ->
            SectionTitle(
                title = title,
                badgeLabel = node.props.str("titleBadgeLabel"),
                trailingLabel = node.props.str("trailingActionLabel"),
                onTrailingClick = trailingAction?.let { action -> { context.dispatch(action, null) } },
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            node.children.chunked(columns).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { child ->
                        Box(modifier = Modifier.weight(1f)) {
                            context.renderChild(child)
                        }
                    }
                    // Last row may have fewer items than `columns` — pad
                    // with empty weighted space so those cells keep their
                    // intended column width instead of stretching wider.
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

val textComponent: SduiComponent = { node, _ ->
    val text = node.props.strOrEmpty("text")
    val sizeSp = node.props.intOr("size", 14)
    // Unlimited unless a node opts into truncation. `1` was the old
    // default and silently clipped every "\n"-joined two-line label in
    // the JSON to just its first line, since Compose's Text(maxLines=1)
    // hard-clips regardless of embedded newlines.
    val maxLines = node.props.intOr("maxLines", Int.MAX_VALUE)
    val weight = node.props.str("weight")
    val colorHex = node.props.str("color")
    Text(
        text = text,
        maxLines = maxLines,
        modifier = node.style.toModifier(),
        color = colorHex?.let { parseHexColor(it) } ?: Color.Unspecified,
        fontSize = TextUnit(sizeSp.toFloat(), TextUnitType.Sp),
        fontWeight = when (weight) {
            "bold" -> FontWeight.Bold
            "medium" -> FontWeight.Medium
            else -> FontWeight.Normal
        },
        textAlign = when (node.style?.alignment) {
            "center" -> androidx.compose.ui.text.style.TextAlign.Center
            "end" -> androidx.compose.ui.text.style.TextAlign.End
            "start" -> androidx.compose.ui.text.style.TextAlign.Start
            else -> null
        },
    )
}

val imageComponent: SduiComponent = { node, _ ->
    val url = node.props.str("url")
    val widthDp = node.props.intOr("width", 120)
    val heightDp = node.props.intOr("height", 80)
    val baseModifier = node.style.toModifier(Modifier.width(widthDp.dp).height(heightDp.dp))
    if (url != null) {
        AsyncImage(model = url, contentDescription = node.props.str("alt"), modifier = baseModifier)
    } else {
        // No URL provided — a stable gray placeholder so a missing image
        // never reads as a broken layout.
        Box(modifier = baseModifier.background(Color(0xFFE0E0E0)))
    }
}

val spacerComponent: SduiComponent = { node, _ ->
    val heightDp = node.props.intOr("height", 8)
    Spacer(modifier = Modifier.height(heightDp.dp))
}

val dividerComponent: SduiComponent = { node, _ ->
    Divider(modifier = node.style.toModifier())
}

/**
 * A section's title row — shared by every component with a `title` prop
 * (`CarouselRail`, `Grid`, ...), not reimplemented per component. The
 * trailing slot (label + tap action) is generic on purpose: it used to be
 * component-specific dead props (`CarouselRail.viewAllLabel`, `Grid.
 * addVehicleLabel`) that were declared in the schema but never rendered —
 * one `trailingActionLabel`/`onTrailingAction` pair here covers "View all"
 * on a rail and "+ Add vehicle" on a grid identically, and any future
 * section with a header link needs no new client code, just these two
 * existing keys.
 */
@Composable
fun SectionTitle(
    title: String,
    badgeLabel: String? = null,
    trailingLabel: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            badgeLabel?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        trailingLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = onTrailingClick?.let { onClick -> Modifier.clickable { onClick() } } ?: Modifier,
            )
        }
    }
}
