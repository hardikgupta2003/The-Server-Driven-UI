package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableFloatStateOf

/**
 * The `State<Float>` object itself (0f fully expanded .. 1f fully
 * collapsed) — deliberately not the raw `Float`. [SduiPage] provides the
 * *same state instance* for the whole scroll gesture; only its `.value`
 * changes per scroll pixel. Reading `.current` here (the object
 * reference) never triggers recomposition — only reading `.value` does,
 * so every consumer below reads `.value` inside a `Modifier.layout{}` or
 * `graphicsLayer{}` block, which defers that read to the layout/draw
 * phase instead of composition. That's what keeps a collapsing header's
 * scroll animation to remeasure+redraw only, not a recomposition per
 * pixel across every affected composable.
 */
val LocalSduiCollapseFraction = compositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }
