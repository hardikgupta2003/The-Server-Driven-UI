package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

/**
 * Name -> bundled drawable (vector or raster — both work via
 * `painterResource`). Empty until real icon assets are added under
 * `sdui/src/main/res/drawable/`. Every `icon` value already in the JSON
 * payload (emoji strings like "🚗") stays exactly as-is — this map is keyed
 * by whatever string the JSON sends, so "activating" a real icon is just:
 * drop the file in res/drawable, add one line here, and change the JSON's
 * `icon` value to match the same key. No renderer code changes per icon,
 * and nothing regresses for names not yet in this map — see [SduiIcon]'s
 * fallback.
 */
val sduiIconRegistry: Map<String, Int> = mapOf(
    // "car" to R.drawable.ic_car,
    // "key" to R.drawable.ic_key,
    // "wallet" to R.drawable.ic_wallet,
    // "receipt" to R.drawable.ic_receipt,
    // "shield" to R.drawable.ic_shield,
    // "grid" to R.drawable.ic_grid,
    // "refresh" to R.drawable.ic_refresh,
    // "document" to R.drawable.ic_document,
    // "truck" to R.drawable.ic_truck,
)

/**
 * Renders an icon by name: a bundled drawable if one's registered,
 * otherwise the raw string as a literal glyph (today's emoji). Same
 * "degrade instead of break" principle as the unknown-component fallback —
 * an icon name with no bundled asset yet is never a blank box or a crash.
 *
 * Pass [tint] only for simple monochrome vector icons that should recolor
 * to match context (e.g. white on a colored background). Leave it null
 * (the default) for full-color raster images — forcing a tint on a
 * multi-color image flattens it to one flat color and ruins it.
 */
@Composable
fun SduiIcon(name: String, sizeDp: Int, tint: Color? = null, modifier: Modifier = Modifier) {
    val resId = sduiIconRegistry[name]
    if (resId != null) {
        if (tint != null) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = modifier.size(sizeDp.dp),
                tint = tint,
            )
        } else {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = modifier.size(sizeDp.dp),
            )
        }
    } else {
        Text(
            text = name,
            fontSize = TextUnit(sizeDp.toFloat(), TextUnitType.Sp),
            modifier = modifier,
        )
    }
}
