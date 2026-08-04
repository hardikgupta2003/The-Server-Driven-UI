package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hardik.the_server_driven_ui.sdui.model.intOr
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.model.strOrEmpty
import com.hardik.the_server_driven_ui.sdui.renderer.SduiComponent
import com.hardik.the_server_driven_ui.sdui.renderer.parseHexColor
import com.hardik.the_server_driven_ui.sdui.renderer.toModifier

/** Generic vertical container. Lays out opaque children — knows nothing about what they are. */
val columnComponent: SduiComponent = { node, context ->
    Column(modifier = node.style.toModifier()) {
        node.children.forEach { child -> context.renderChild(child) }
    }
}

/** Generic horizontal container. */
val rowComponent: SduiComponent = { node, context ->
    Row(modifier = node.style.toModifier()) {
        node.children.forEach { child -> context.renderChild(child) }
    }
}

/** Horizontally scrolling rail — used for banner carousels and car-card rails alike. */
val carouselRailComponent: SduiComponent = { node, context ->
    Column(modifier = node.style.toModifier()) {
        node.props.str("title")?.let { title -> SectionTitle(title) }
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

/** Fixed-column vertical grid, e.g. category tiles or a car-card grid. */
val gridComponent: SduiComponent = { node, context ->
    val columns = node.props.intOr("columns", 2)
    val rows = node.props.intOr("rows", 2)
    Column(modifier = node.style.toModifier()) {
        node.props.str("title")?.let { title -> SectionTitle(title) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.height((rows * 220).dp),
        ) {
            gridItems(node.children, key = { it.id }) { child ->
                context.renderChild(child)
            }
        }
    }
}

val textComponent: SduiComponent = { node, _ ->
    val text = node.props.strOrEmpty("text")
    val sizeSp = node.props.intOr("size", 14)
    val weight = node.props.str("weight")
    val colorHex = node.props.str("color")
    Text(
        text = text,
        modifier = node.style.toModifier(),
        color = colorHex?.let { parseHexColor(it) } ?: Color.Unspecified,
        fontSize = TextUnit(sizeSp.toFloat(), TextUnitType.Sp),
        fontWeight = when (weight) {
            "bold" -> FontWeight.Bold
            "medium" -> FontWeight.Medium
            else -> FontWeight.Normal
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

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
