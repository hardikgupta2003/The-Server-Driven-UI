package com.hardik.the_server_driven_ui.sdui.renderer

import com.hardik.the_server_driven_ui.sdui.components.bannerCarouselComponent
import com.hardik.the_server_driven_ui.sdui.components.buttonComponent
import com.hardik.the_server_driven_ui.sdui.components.carCardComponent
import com.hardik.the_server_driven_ui.sdui.components.carouselRailComponent
import com.hardik.the_server_driven_ui.sdui.components.chipRowComponent
import com.hardik.the_server_driven_ui.sdui.components.columnComponent
import com.hardik.the_server_driven_ui.sdui.components.dividerComponent
import com.hardik.the_server_driven_ui.sdui.components.footerCtaComponent
import com.hardik.the_server_driven_ui.sdui.components.gridComponent
import com.hardik.the_server_driven_ui.sdui.components.imageComponent
import com.hardik.the_server_driven_ui.sdui.components.rowComponent
import com.hardik.the_server_driven_ui.sdui.components.searchHeaderComponent
import com.hardik.the_server_driven_ui.sdui.components.spacerComponent
import com.hardik.the_server_driven_ui.sdui.components.textComponent
import com.hardik.the_server_driven_ui.sdui.components.valuePropStripComponent

/**
 * Single place that maps a JSON `type` string to its composable. This is
 * the entire "component registry" from the brief — adding a new component
 * is one `register(...)` line, everything else (dispatch, fallback,
 * styling) is generic.
 */
fun buildDefaultRegistry(): ComponentRegistry = ComponentRegistry().apply {
    // Layout primitives
    register("Column", columnComponent)
    register("Row", rowComponent)
    register("Grid", gridComponent)
    register("CarouselRail", carouselRailComponent)

    // Leaves
    register("Text", textComponent)
    register("Image", imageComponent)
    register("Spacer", spacerComponent)
    register("Divider", dividerComponent)

    // Interactive leaves
    register("Button", buttonComponent)
    register("ChipRow", chipRowComponent)

    // Composite / domain
    register("CarCard", carCardComponent)
    register("BannerCarousel", bannerCarouselComponent)
    register("SearchHeader", searchHeaderComponent)
    register("ValuePropStrip", valuePropStripComponent)
    register("FooterCta", footerCtaComponent)
}
