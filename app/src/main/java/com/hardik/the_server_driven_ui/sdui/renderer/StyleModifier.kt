package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hardik.the_server_driven_ui.sdui.model.SduiStyle

/** Turns the shared, component-agnostic style block into a Compose Modifier. */
@Composable
fun SduiStyle?.toModifier(base: Modifier = Modifier): Modifier {
    if (this == null) return base
    var modifier = base
    margin?.let { (start, top, end, bottom) ->
        modifier = modifier.padding(start = start.dp, top = top.dp, end = end.dp, bottom = bottom.dp)
    }
    cornerRadius?.let { radius ->
        modifier = modifier.clip(RoundedCornerShape(radius.dp))
    }
    background?.let { hex ->
        parseHexColor(hex)?.let { color -> modifier = modifier.background(color) }
    }
    padding?.let { (start, top, end, bottom) ->
        modifier = modifier.padding(start = start.dp, top = top.dp, end = end.dp, bottom = bottom.dp)
    }
    return modifier
}

private operator fun List<Int>.component1() = getOrElse(0) { 0 }
private operator fun List<Int>.component2() = getOrElse(1) { 0 }
private operator fun List<Int>.component3() = getOrElse(2) { 0 }
private operator fun List<Int>.component4() = getOrElse(3) { 0 }

fun parseHexColor(hex: String): Color? = runCatching {
    val cleaned = hex.removePrefix("#")
    val full = when (cleaned.length) {
        6 -> "FF$cleaned"
        8 -> cleaned
        else -> return null
    }
    Color(full.toLong(16))
}.getOrNull()
