package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hardik.the_server_driven_ui.sdui.model.objList
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.model.strOrEmpty
import com.hardik.the_server_driven_ui.sdui.renderer.SduiComponent
import com.hardik.the_server_driven_ui.sdui.renderer.toModifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/** A row of selectable chips whose selection drives a DataBinding elsewhere on the page. */
val chipRowComponent: SduiComponent = { node, context ->
    val items = node.props.objList("items")
    val defaultSelected = node.props.str("defaultSelected")
    val onSelect = node.actions["onSelect"]
    val stateKey = onSelect?.target

    // Seed initial state once so a chip row renders "selected" on first
    // frame even before the user taps anything.
    if (stateKey != null && defaultSelected != null) {
        LaunchedEffect(node.id) {
            if (context.currentState[stateKey] == null) {
                context.dispatch(onSelect, JsonObject(mapOf("value" to kotlinx.serialization.json.JsonPrimitive(defaultSelected))))
            }
        }
    }

    val selectedValue = stateKey?.let { context.currentState[it] } ?: defaultSelected

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
