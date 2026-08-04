package com.hardik.the_server_driven_ui.sdui.renderer

import androidx.compose.runtime.compositionLocalOf

/**
 * 0f (fully expanded) .. 1f (fully collapsed) — how far the page's
 * optional collapsing header has been scrolled away. Provided by
 * [SduiPage] only when the page defines a `header`; every other context
 * sees the default `0f`, so a component reading this never has to check
 * "am I even inside a collapsing header" — it just always gets a sane,
 * inert value when there isn't one.
 */
val LocalSduiCollapseFraction = compositionLocalOf { 0f }
