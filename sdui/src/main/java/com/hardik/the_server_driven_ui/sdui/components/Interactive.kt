package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.hardik.the_server_driven_ui.sdui.model.intOr
import com.hardik.the_server_driven_ui.sdui.model.objList
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.model.strOrEmpty
import com.hardik.the_server_driven_ui.sdui.renderer.SduiComponent
import com.hardik.the_server_driven_ui.sdui.renderer.parseHexColor
import com.hardik.the_server_driven_ui.sdui.renderer.toModifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * A row of selectable chips whose selection drives a DataBinding elsewhere
 * on the page. `variant: "iconTab"` switches to an icon-above-label tab
 * look with a bottom selection indicator (e.g. the Cars24 nav row) instead
 * of Material3's default pill `FilterChip` — same node type, same
 * selection/action plumbing, just a different prop.
 */
val chipRowComponent: SduiComponent = { node, context ->
    val items = node.props.objList("items")
    val defaultSelected = node.props.str("defaultSelected")
    val onSelect = node.actions["onSelect"]
    val stateKey = onSelect?.target
    val variant = node.props.str("variant", "chip")

    // Seed initial state once so a chip row renders "selected" on first
    // frame even before the user taps anything.
    if (stateKey != null && defaultSelected != null) {
        LaunchedEffect(node.id) {
            if (context.currentState[stateKey] == null) {
                context.dispatch(onSelect, JsonObject(mapOf("value" to JsonPrimitive(defaultSelected))))
            }
        }
    }

    val selectedValue = stateKey?.let { context.currentState[it] } ?: defaultSelected

    if (variant == "iconTab") {
        val iconSizeDp = node.props.intOr("iconSize", 24)
        val iconBoxPaddingDp = node.props.intOr("iconBoxPadding", 8)
        val iconBoxCornerRadiusDp = node.props.intOr("iconBoxCornerRadius", 12)
        val iconBoxBackground = node.props.str("iconBoxBackgroundColor")?.let { parseHexColor(it) } ?: Color(0x33FFFFFF)
        val iconBoxBorder = node.props.str("iconBoxBorderColor")?.let { parseHexColor(it) } ?: Color(0xFFE0E0E0)
        val labelSizeSp = node.props.intOr("labelSize", 14)
        val tabItemWidthDp = iconSizeDp + iconBoxPaddingDp * 2 + 16

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = node.style.toModifier(),
        ) {
            items(items) { item ->
                val label = item.str("label")
                val value = item.str("value")
                val icon = item.str("icon", "•")
                val isSelected = value == selectedValue
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(tabItemWidthDp.dp)
                        .clickable { onSelect?.let { action -> context.dispatch(action, item) } }
                        .padding(vertical = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size((iconSizeDp + iconBoxPaddingDp * 2).dp)
                            .clip(RoundedCornerShape(iconBoxCornerRadiusDp.dp))
                            .background(iconBoxBackground)
                            .border(1.dp, iconBoxBorder, RoundedCornerShape(iconBoxCornerRadiusDp.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(icon, fontSize = TextUnit(iconSizeDp.toFloat(), TextUnitType.Sp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        label,
                        fontSize = TextUnit(labelSizeSp.toFloat(), TextUnitType.Sp),
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (isSelected) Color.White else Color.Transparent),
                    )
                }
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = node.style.toModifier(),
        ) {
            items(items) { item ->
                val label = item.str("label")
                val value = item.str("value")
                FilterChip(
                    selected = value == selectedValue,
                    onClick = {
                        onSelect?.let { action ->
                            context.dispatch(action, item)
                        }
                    },
                    label = { Text(label) },
                )
            }
        }
    }
}

/** A tappable button firing whatever action is bound to `onClick`. */
val buttonComponent: SduiComponent = { node, context ->
    val label = node.props.strOrEmpty("label", "Button")
    val onClick = node.actions["onClick"]
    Button(
        onClick = { onClick?.let { context.dispatch(it, null) } },
        modifier = node.style.toModifier(),
    ) {
        Text(label)
    }
}
