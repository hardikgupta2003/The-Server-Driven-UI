package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Wraps [content] so it shrinks + slides upward (and optionally fades) as
 * [fractionState] goes 0f -> 1f. Every `.value` read happens inside a
 * layout or draw phase block ([androidx.compose.ui.layout.layout] /
 * [graphicsLayer]) rather than in the composable body — that's the whole
 * point: it makes this remeasure-and-redraw-only, never a recomposition,
 * no matter how many scroll-driven updates land per second. See
 * [LocalSduiCollapseFraction]'s kdoc for why the caller passes a stable
 * `State` object rather than a plain `Float`.
 *
 * [fade] distinguishes the two looks this project actually needs: a
 * solid-colored row (e.g. the location/profile row) should slide away
 * intact with no fade — fading a solid color while its box also shrinks
 * visibly washes the color out against whatever's behind it. Icons
 * (`ChipRow`'s icon-tab variant) explicitly want fade *and* slide.
 */
@Composable
fun CollapsibleOnHide(
    fractionState: State<Float>,
    fade: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                // Measure content at its natural size regardless of
                // collapse state (loose height constraint) — text/icons
                // never reflow mid-collapse — then report a shrinking
                // height derived from a layout-phase-only state read.
                val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                val placeable = measurable.measure(loose)
                val height = (placeable.height * (1f - fractionState.value)).roundToInt().coerceAtLeast(0)
                layout(placeable.width, height) {
                    placeable.placeRelative(0, 0)
                }
            }
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                val f = fractionState.value
                translationY = -f * size.height
                if (fade) alpha = (1f - f).coerceIn(0f, 1f)
            },
        ) {
            content()
        }
    }
}
