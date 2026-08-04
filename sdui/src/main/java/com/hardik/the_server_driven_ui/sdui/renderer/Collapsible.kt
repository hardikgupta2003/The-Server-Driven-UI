package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Wraps [content] so it slides upward and out as [fraction] goes 0f -> 1f,
 * then reports its natural size again once expanded. Used by any node
 * with `props.collapseBehavior = "hide"` inside a page's collapsing
 * `header` (e.g. the location/profile row that disappears on scroll).
 *
 * Deliberately no alpha fade here: fading a *solid-colored* row while its
 * height also shrinks makes the color visibly wash out against whatever's
 * behind it (translucent purple blending toward white mid-collapse read as
 * "the color changes" — not what the real app does). Translating the
 * content upward inside a clipped, shrinking box reads as it sliding away
 * intact, with no color shift. `graphicsLayer { translationY = ... }` is a
 * draw-only transform — no remeasure of `content()` itself, which is what
 * keeps this from adding per-frame layout cost during a scroll gesture.
 */
@Composable
fun CollapsibleOnHide(fraction: Float, content: @Composable () -> Unit) {
    var naturalHeightPx by remember { mutableFloatStateOf(-1f) }
    val density = LocalDensity.current

    val heightModifier = if (naturalHeightPx >= 0f) {
        val collapsedHeight: Dp = with(density) { (naturalHeightPx * (1f - fraction)).toDp() }
        Modifier.height(collapsedHeight)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier.fillMaxWidth().then(heightModifier).clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    if (naturalHeightPx < 0f) naturalHeightPx = coordinates.size.height.toFloat()
                }
                .graphicsLayer {
                    translationY = -fraction * size.height
                },
        ) {
            content()
        }
    }
}
