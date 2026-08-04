package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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

    width?.let { modifier = applyDimension(modifier, it, isWidth = true) }
    height?.let { modifier = applyDimension(modifier, it, isWidth = false) }

    val shape = cornerRadius?.let { RoundedCornerShape(it.dp) }
    shape?.let { modifier = modifier.clip(it) }

    background?.let { hex ->
        parseHexColor(hex)?.let { color -> modifier = modifier.background(color) }
    }

    borderWidth?.let { width ->
        val color = borderColor?.let { parseHexColor(it) } ?: Color.Gray
        modifier = modifier.border(width.dp, color, shape ?: RoundedCornerShape(0.dp))
    }

    padding?.let { (start, top, end, bottom) ->
        modifier = modifier.padding(start = start.dp, top = top.dp, end = end.dp, bottom = bottom.dp)
    }

    opacity?.let { modifier = modifier.alpha(it) }
    rotation?.let { modifier = modifier.rotate(it) }

    return modifier
}

private fun applyDimension(modifier: Modifier, spec: String, isWidth: Boolean): Modifier = when {
    spec == "match" -> if (isWidth) modifier.fillMaxWidth() else modifier.fillMaxHeight()
    spec == "wrap" -> modifier
    else -> spec.toIntOrNull()?.let { dpValue ->
        if (isWidth) modifier.width(dpValue.dp) else modifier.height(dpValue.dp)
    } ?: modifier
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
