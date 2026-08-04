package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hardik.the_server_driven_ui.sdui.model.SduiAction
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

/** Full-bleed rotating banner rail, e.g. promo/offer creatives. */
val bannerCarouselComponent: SduiComponent = { node, context ->
    val banners = node.props.objList("items")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = node.style.toModifier(),
    ) {
        items(banners) { banner ->
            val imageUrl = banner.str("imageUrl").ifBlank { null }
            val bg = banner.str("background").ifBlank { null }?.let { parseHexColor(it) }
            val route = banner.str("route").ifBlank { null }
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(120.dp)
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

/** Top search bar + logo strip. */
val searchHeaderComponent: SduiComponent = { node, context ->
    val placeholder = node.props.strOrEmpty("placeholder", "Search cars, brands...")
    val title = node.props.str("title")
    val onClick = node.actions["onClick"]
    Column(modifier = node.style.toModifier(Modifier.fillMaxWidth().padding(16.dp))) {
        title?.let { Text(it, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = { Text(placeholder) },
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
                Text(item.str("icon", "★"), style = MaterialTheme.typography.titleLarge)
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
