package com.hardik.the_server_driven_ui.sdui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.hardik.the_server_driven_ui.sdui.model.boolOr
import com.hardik.the_server_driven_ui.sdui.model.intOr
import com.hardik.the_server_driven_ui.sdui.model.objList
import com.hardik.the_server_driven_ui.sdui.model.str
import com.hardik.the_server_driven_ui.sdui.model.strOrEmpty
import com.hardik.the_server_driven_ui.sdui.renderer.CollapsibleOnHide
import com.hardik.the_server_driven_ui.sdui.renderer.LocalSduiCollapseFraction
import com.hardik.the_server_driven_ui.sdui.renderer.SduiComponent
import com.hardik.the_server_driven_ui.sdui.renderer.parseHexColor
import com.hardik.the_server_driven_ui.sdui.renderer.toModifier
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private fun fontWeightOf(name: String?): FontWeight = when (name) {
    "bold" -> FontWeight.Bold
    "normal" -> FontWeight.Normal
    else -> FontWeight.Medium
}

/**
 * Smooth-scrolls the tapped item into view — same feel as the real app's
 * nav row. `LazyListState.animateScrollToItem` alone looks fine for long
 * jumps but snaps almost instantly for short ones (its internal spring
 * reaches target velocity too fast over a small distance), which is
 * exactly the common case in a chip row. Computing the actual pixel
 * distance to the target and animating that with a fixed-duration tween
 * gives a consistently smooth glide regardless of how close the tapped
 * item already is.
 */
private fun scrollToItem(scope: kotlinx.coroutines.CoroutineScope, listState: LazyListState, index: Int) {
    scope.launch {
        val visible = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        if (visible != null) {
            val viewportStart = listState.layoutInfo.viewportStartOffset
            val delta = (visible.offset - viewportStart).toFloat()
            listState.animateScrollBy(delta, animationSpec = tween(durationMillis = 800))
        } else {
            // Target isn't laid out yet (far off-screen) — no pixel offset
            // to compute a delta from, so fall back to the built-in jump,
            // which does look smooth over genuinely long distances.
            listState.animateScrollToItem(index)
        }
    }
}

/**
 * A row of selectable chips whose selection drives a DataBinding elsewhere
 * on the page. `variant: "iconTab"` switches to an icon-above-label tab
 * look with a bottom selection indicator (e.g. the Cars24 nav row) instead
 * of Material3's default pill `FilterChip` — same node type, same
 * selection/action plumbing, just a different prop.
 *
 * Every visual knob below (spacing, colors, sizes, indicator) is a `props`
 * key with a default matching the original hardcoded look — set any of
 * them from JSON, change nothing else.
 */
val chipRowComponent: SduiComponent = { node, context ->
    val items = node.props.objList("items")
    val defaultSelected = node.props.str("defaultSelected")
    val onSelect = node.actions["onSelect"]
    val stateKey = onSelect?.target
    val variant = node.props.str("variant", "chip")
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

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
        val labelColor = node.props.str("labelColor")?.let { parseHexColor(it) } ?: Color.White
        val labelWeight = fontWeightOf(node.props.str("labelWeight"))
        val itemSpacingDp = node.props.intOr("itemSpacing", 24)
        val contentPaddingHorizontalDp = node.props.intOr("contentPaddingHorizontal", 12)
        val contentPaddingTopDp = node.props.intOr("contentPaddingTop", 8)
        val itemInnerSpacingDp = node.props.intOr("itemInnerSpacing", 6)
        val itemTopPaddingDp = node.props.intOr("itemTopPadding", 4)
        val indicatorHeightDp = node.props.intOr("indicatorHeight", 3)
        val indicatorColor = node.props.str("indicatorColor")?.let { parseHexColor(it) } ?: Color.White
        val indicatorUnselectedColor = node.props.str("indicatorUnselectedColor")?.let { parseHexColor(it) } ?: Color.Transparent

        // Icon-only collapse on a collapsing header (real app: tab icons
        // fade + slide away, labels + indicator stay put). `fractionState`
        // is a stable, never-changing State(0f) whenever this ChipRow
        // isn't nested inside a page-level collapsing header (or when
        // `collapseIconOnScroll` opts a specific payload out) — reading
        // `.current`/this fallback does not recompose on scroll; only
        // CollapsibleOnHide's internal layout/draw-phase reads do. See
        // LocalSduiCollapseFraction's kdoc for why this matters.
        val collapseIconOnScroll = node.props.boolOr("collapseIconOnScroll", true)
        val inertFractionState = remember { mutableFloatStateOf(0f) }
        val iconFractionState = if (collapseIconOnScroll) LocalSduiCollapseFraction.current else inertFractionState
        val naturalIconBoxDp = iconSizeDp + iconBoxPaddingDp * 2

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(itemSpacingDp.dp),
            contentPadding = PaddingValues(start = contentPaddingHorizontalDp.dp, top = contentPaddingTopDp.dp, end = contentPaddingHorizontalDp.dp),
            modifier = node.style.toModifier(),
        ) {
            itemsIndexed(items, key = { _, item -> item.str("value").ifBlank { item.str("label") } }) { index, item ->
                val label = item.str("label")
                val value = item.str("value")
                val icon = item.str("icon", "•")
                val isSelected = value == selectedValue
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // IntrinsicSize.Max: sizes this tab to its widest child
                    // (the label) instead of a fixed width, while giving
                    // the indicator below a real width to fillMaxWidth()
                    // against — without this, fillMaxWidth() inside a
                    // plain wrap-content Column has nothing concrete to
                    // resolve to and the indicator effectively vanishes.
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clickable {
                            onSelect?.let { action -> context.dispatch(action, item) }
                            scrollToItem(scope, listState, index)
                        }
                        .padding(top = itemTopPaddingDp.dp),
                ) {
                    CollapsibleOnHide(
                        fractionState = iconFractionState,
                        fade = true,
                        modifier = Modifier.width(naturalIconBoxDp.dp),
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(naturalIconBoxDp.dp)
                                    .clip(RoundedCornerShape(iconBoxCornerRadiusDp.dp))
                                    .background(iconBoxBackground)
                                    .border(1.dp, iconBoxBorder, RoundedCornerShape(iconBoxCornerRadiusDp.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                SduiIcon(name = icon, sizeDp = iconSizeDp - iconBoxPaddingDp / 2)
                            }
                            Spacer(modifier = Modifier.height(itemInnerSpacingDp.dp))
                        }
                    }
                    Text(
                        label,
                        fontSize = TextUnit(labelSizeSp.toFloat(), TextUnitType.Sp),
                        fontWeight = labelWeight,
                        color = labelColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(itemInnerSpacingDp.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(indicatorHeightDp.dp)
                            .background(if (isSelected) indicatorColor else indicatorUnselectedColor),
                    )
                }
            }
        }
    } else {
        val itemSpacingDp = node.props.intOr("itemSpacing", 8)
        val contentPaddingHorizontalDp = node.props.intOr("contentPaddingHorizontal", 16)
        val contentPaddingVerticalDp = node.props.intOr("contentPaddingVertical", 8)

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(itemSpacingDp.dp),
            contentPadding = PaddingValues(horizontal = contentPaddingHorizontalDp.dp, vertical = contentPaddingVerticalDp.dp),
            modifier = node.style.toModifier(),
        ) {
            itemsIndexed(items, key = { _, item -> item.str("value").ifBlank { item.str("label") } }) { index, item ->
                val label = item.str("label")
                val value = item.str("value")
                FilterChip(
                    selected = value == selectedValue,
                    onClick = {
                        onSelect?.let { action -> context.dispatch(action, item) }
                        scrollToItem(scope, listState, index)
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
