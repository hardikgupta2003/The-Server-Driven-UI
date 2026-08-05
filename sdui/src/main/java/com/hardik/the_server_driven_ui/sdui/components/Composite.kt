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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hardik.the_server_driven_ui.sdui.model.SduiAction
import com.hardik.the_server_driven_ui.sdui.model.intOr
import com.hardik.the_server_driven_ui.sdui.model.objList
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.model.strOrEmpty
import com.hardik.the_server_driven_ui.sdui.renderer.SduiComponent
import com.hardik.the_server_driven_ui.sdui.renderer.parseHexColor
import com.hardik.the_server_driven_ui.sdui.renderer.toModifier
import kotlinx.serialization.json.JsonPrimitive

/** A single car listing: image, title, subtitle, price, tappable for a nav intent. */
val carCardComponent: SduiComponent = { node, context ->
    val imageUrl = node.props.str("imageUrl")
    val title = node.props.strOrEmpty("title")
    val subtitle = node.props.str("subtitle")
    val price = node.props.str("price")
    val badge = node.props.str("badge")
    val onClick = node.actions["onClick"]

    Card(
        modifier = node.style.toModifier(Modifier.width(180.dp)).let {
            if (onClick != null) it.clickableAction { context.dispatch(onClick, null) } else it
        },
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                if (imageUrl != null) {
                    AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.fillMaxWidth().height(110.dp))
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFE0E0E0)))
                }
                badge?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00A86B))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1) }
                price?.let { Text(it, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

/**
 * Full-bleed rotating banner rail, e.g. promo/offer creatives. A real
 * center-focused carousel, not a plain equal-width list row: the item
 * nearest the viewport center renders at full scale, neighbors shrink the
 * further they sit from center, and releasing a swipe snaps to the
 * nearest item rather than settling wherever momentum happened to stop.
 *
 * The per-item scale is computed from `listState.layoutInfo` *inside* the
 * `graphicsLayer{}` block, not read in the composable body — same
 * discipline as `CollapsibleOnHide`'s scroll-driven state (see its kdoc):
 * `layoutInfo` changes on every scroll frame, and reading it in a
 * layout/draw-phase lambda makes this remeasure/redraw-only instead of a
 * recomposition of every visible banner on every scroll pixel.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
val bannerCarouselComponent: SduiComponent = { node, context ->
    val banners = node.props.objList("items")
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(listState)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = node.style.toModifier()) {
        val itemWidth = 280.dp
        val sidePadding = ((maxWidth - itemWidth) / 2).coerceAtLeast(16.dp)
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = sidePadding, vertical = 8.dp),
        ) {
            items(banners, key = { it.str("id").ifBlank { it.toString() } }) { banner ->
                val itemKey = banner.str("id").ifBlank { banner.toString() }
                val imageUrl = banner.str("imageUrl").ifBlank { null }
                val bg = banner.str("background").ifBlank { null }?.let { parseHexColor(it) }
                val route = banner.str("route").ifBlank { null }
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .height(120.dp)
                        .graphicsLayer {
                            val info = listState.layoutInfo
                            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                            val itemInfo = info.visibleItemsInfo.firstOrNull { it.key == itemKey }
                            val scale = if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val maxDistance = (info.viewportEndOffset - info.viewportStartOffset) / 2f
                                val fraction = (kotlin.math.abs(itemCenter - viewportCenter) / maxDistance).coerceIn(0f, 1f)
                                1f - fraction * 0.18f
                            } else {
                                0.82f
                            }
                            scaleX = scale
                            scaleY = scale
                            alpha = (0.55f + 0.45f * ((scale - 0.82f) / 0.18f)).coerceIn(0.55f, 1f)
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg ?: Color(0xFFEFEFEF))
                        .let { m ->
                            if (route != null) {
                                m.clickableAction {
                                    context.dispatch(
                                        SduiAction(type = "navigate", payload = mapOf("route" to JsonPrimitive(route))),
                                        null,
                                    )
                                }
                            } else m
                        },
                ) {
                    if (imageUrl != null) {
                        AsyncImage(model = imageUrl, contentDescription = banner.str("title"), modifier = Modifier.fillMaxWidth().height(120.dp))
                    } else {
                        Text(
                            text = banner.str("title"),
                            modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/** Top search bar + logo strip. */
val searchHeaderComponent: SduiComponent = { node, context ->
    val placeholder = node.props.strOrEmpty("placeholder", "Search cars, brands...")
    val title = node.props.str("title")
    val onClick = node.actions["onClick"]
    val backgroundColor = node.props.str("backgroundColor")?.let { parseHexColor(it) } ?: Color(0x33FFFFFF)
    val borderColor = node.props.str("borderColor")?.let { parseHexColor(it) } ?: Color(0xFFE0E0E0)
    val hintColor = node.props.str("hintColor")?.let { parseHexColor(it) } ?: Color(0xFFE0E0E0)

    Column(modifier = node.style.toModifier(Modifier.fillMaxWidth().padding(16.dp))) {
        title?.let { Text(it, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = { Text(placeholder, color = hintColor) },
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                disabledContainerColor = backgroundColor,
                disabledBorderColor = borderColor,
                disabledPlaceholderColor = hintColor,
                disabledLeadingIconColor = hintColor,
            ),
            leadingIcon = { Text("🔍", color = hintColor) },
            modifier = Modifier.fillMaxWidth().let {
                if (onClick != null) it.clickableAction { context.dispatch(onClick, null) } else it
            },
        )
    }
}

/** Row of trust/value-prop badges — "0% down payment", "5-day money back", etc. */
val valuePropStripComponent: SduiComponent = { node, _ ->
    val items = node.props.objList("items")
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = node.style.toModifier(Modifier.fillMaxWidth().padding(vertical = 12.dp)),
    ) {
        items.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                SduiIcon(name = item.str("icon", "★"), sizeDp = 22)
                Text(item.str("label"), style = MaterialTheme.typography.labelSmall, maxLines = 2)
            }
        }
    }
}

/** Full-width closing CTA banner, e.g. "Sell your car" strip. */
val footerCtaComponent: SduiComponent = { node, context ->
    val title = node.props.strOrEmpty("title")
    val subtitle = node.props.str("subtitle")
    val buttonLabel = node.props.str("buttonLabel", "Explore")
    val onClick = node.actions["onClick"]
    val bg = node.props.str("background")?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.primaryContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = node.style.toModifier(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .padding(16.dp),
        ),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        Button(onClick = { onClick?.let { context.dispatch(it, null) } }) {
            Text(buttonLabel ?: "Explore")
        }
    }
}

private fun Modifier.clickableAction(onClick: () -> Unit): Modifier =
    this.clickable { onClick() }

/**
 * Generic small pill/badge — a label on its own background/border, sized
 * to its own content (no default width/background baked in; the JSON's
 * `style` block fully controls appearance, same as `Button`/`TextField`).
 * Replaces the hand-composed "small Column used as a chip" pattern (e.g.
 * the challan form's "IND" country-code pill) that caused the
 * `fillMaxWidth()` layout bug once already (see `AI_WORKFLOW.md`'s third
 * story) — a purpose-built leaf component can't repeat that mistake since
 * it never defaults to filling its parent's width in the first place.
 */
val chipComponent: SduiComponent = { node, context ->
    val label = node.props.strOrEmpty("label")
    val textColor = node.props.str("textColor")?.let { parseHexColor(it) } ?: Color.Unspecified
    val weight = when (node.props.str("weight")) {
        "bold" -> FontWeight.Bold
        "normal" -> FontWeight.Normal
        else -> FontWeight.Medium
    }
    val sizeSp = node.props.intOr("size", 13)
    val onClick = node.actions["onClick"]
    Box(
        modifier = node.style.toModifier().let { m ->
            if (onClick != null) m.clickableAction { context.dispatch(onClick, null) } else m
        },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = textColor, fontWeight = weight, fontSize = TextUnit(sizeSp.toFloat(), TextUnitType.Sp))
    }
}

/**
 * Generic list row — image/icon, title, optional subtitle, optional
 * trailing accessory (a chevron or short badge text). Generalizes the
 * hand-composed "image + text column + trailing chevron" `Row` pattern
 * that appeared 3 times in the car-check "uncover frauds" section
 * (~40 lines of raw primitives each) into ~10 lines of JSON, and closes
 * off the same fragility class the `Chip` component's kdoc describes —
 * this Row's layout is written and tested once, not re-derived by hand
 * at every call site.
 */
val listRowComponent: SduiComponent = { node, context ->
    val imageUrl = node.props.str("imageUrl")
    val title = node.props.strOrEmpty("title")
    val subtitle = node.props.str("subtitle")
    val subtitleColor = node.props.str("subtitleColor")?.let { parseHexColor(it) } ?: Color(0xFF5B5B7A)
    val trailing = node.props.str("trailing")
    val imageSize = node.props.intOr("imageSize", 48).dp
    val onClick = node.actions["onClick"]

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = node.style.toModifier(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)).let { m ->
            if (onClick != null) m.clickableAction { context.dispatch(onClick, null) } else m
        },
    ) {
        if (imageUrl != null) {
            AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.size(imageSize).clip(RoundedCornerShape(8.dp)))
        } else {
            Box(modifier = Modifier.size(imageSize).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, fontSize = TextUnit(13f, TextUnitType.Sp), color = subtitleColor) }
        }
        when {
            trailing == "chevron" -> Text("›", fontSize = TextUnit(20f, TextUnitType.Sp), color = Color(0xFF9E9E9E))
            !trailing.isNullOrBlank() -> Text(trailing, fontSize = TextUnit(12f, TextUnitType.Sp), color = Color(0xFF9E9E9E))
        }
    }
}
