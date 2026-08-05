package com.hardik.the_server_driven_ui.static

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hardik.the_server_driven_ui.sdui.perf.PerfTrace
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Hand-written, hardcoded twin of the SDUI landing page — same header,
 * same collapsing-scroll behavior, same 7-tab body content and colors as
 * `assets/sdui/landing_page.json`, but every section/car/copy string is a
 * literal in this file. No JSON parsing, no component registry, no
 * runtime dispatch. This is the baseline `PERF.md` benchmarks the SDUI
 * version against.
 */
private object Img {
    const val P100653 = "https://images.pexels.com/photos/100653/pexels-photo-100653.jpeg"
    const val P1149137 = "https://images.pexels.com/photos/1149137/pexels-photo-1149137.jpeg"
    const val P120049 = "https://images.pexels.com/photos/120049/pexels-photo-120049.jpeg"
    const val P1592384 = "https://images.pexels.com/photos/1592384/pexels-photo-1592384.jpeg"
    const val P170811 = "https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg"
    const val P210019 = "https://images.pexels.com/photos/210019/pexels-photo-210019.jpeg"
    const val P244206 = "https://images.pexels.com/photos/244206/pexels-photo-244206.jpeg"
    const val P358070 = "https://images.pexels.com/photos/358070/pexels-photo-358070.jpeg"
    const val P3802510 = "https://images.pexels.com/photos/3802510/pexels-photo-3802510.jpeg"
    const val P919073 = "https://images.pexels.com/photos/919073/pexels-photo-919073.jpeg"
}

private data class NavTab(val id: String, val label: String, val icon: String)

private val navTabs = listOf(
    NavTab("all", "All", "▦"),
    NavTab("buy", "Buy used car", "🚗"),
    NavTab("sell", "Sell car", "🔑"),
    NavTab("loans", "Loans", "💰"),
    NavTab("challan", "Challan", "🧾"),
    NavTab("car_check", "Car check", "🛡️"),
    NavTab("insurance", "Insurance", "🔒"),
)

private val tabColor: Map<String, Color> = mapOf(
    "all" to Color(0xFF392BCB),
    "buy" to Color(0xFF0A1A6B),
    "sell" to Color(0xFF1B5E20),
    "loans" to Color(0xFF1565C0),
    "challan" to Color(0xFF00695C),
    "car_check" to Color(0xFF6B2E12),
    "insurance" to Color(0xFF1565C0),
)

private data class StaticCarListing(
    val id: String,
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val badge: String? = null,
)

private data class TrendingCar(val id: String, val imageUrl: String, val title: String, val subtitle: String, val badge: String)
private data class ShowroomInfo(val id: String, val imageUrl: String, val count: String, val name: String, val location: String, val distance: String, val hours: String)
private data class Banner(val text: String, val background: Color, val route: String)

private val buyCarRailItems = listOf(
    "All used cars" to "buy_all_used_cars",
    "Budget used cars" to "buy_budget_used_cars",
    "Premium used cars" to "buy_premium_used_cars",
    "Nearly new cars" to "buy_nearly_new_cars",
)

private val sellCarRailItems = listOf(
    "Sell your\ncar" to "sell_flow",
    "Check car\nvaluation" to "car_valuation",
    "Scrap your\ncar" to "scrap_car",
    "Exchange your\ncar" to "exchange_car",
)

private val loansRailItems = listOf(
    Triple(Img.P1592384, "Used car loan", "used_car_loan"),
    Triple(Img.P919073, "Loan against car", "loan_against_car"),
    Triple(Img.P1592384, "Personal loan", "personal_loan"),
    Triple(Img.P100653, "Check credit score", "check_credit_score"),
)

private val carCheckGridItems = listOf(
    Triple(Img.P170811, "New car PDI", "new_car_pdi"),
    Triple(Img.P3802510, "Used car check", "used_car_check"),
    Triple(Img.P244206, "Vehicle history", "vehicle_history"),
    Triple(Img.P1592384, "Check challan", "check_challan"),
    Triple(Img.P210019, "Check car insurance", "check_car_insurance"),
    Triple(Img.P210019, "Odometer tampering", "odometer_tampering"),
)

private val manageVehicleGridItems = listOf(
    Triple(Img.P3802510, "Pay challan", "pay_challan"),
    Triple(Img.P170811, "Recharge FASTag", "recharge_fastag"),
    Triple(Img.P1149137, "Get insurance", "get_insurance"),
    Triple(Img.P3802510, "Cash against car", "cash_against_car"),
    Triple(Img.P244206, "Road side assistance", "road_side_assistance"),
    Triple(Img.P210019, "Get warranty", "get_warranty"),
)

private val usedCarsRail = listOf(
    StaticCarListing("used_car_swift_2017", Img.P358070, "2017 Maruti Swift VXI", "71,846 km · Petrol · Manual · KA01", "₹4.65 Lakh", "Cars24 Owned stock"),
    StaticCarListing("used_car_kwid_2015", Img.P100653, "2015 Renault Kwid RXL", "44,515 km · Petrol · Manual", "₹2.32 Lakh"),
    StaticCarListing("used_car_i10_2019", Img.P3802510, "2019 Hyundai Grand i10", "38,210 km · Petrol · Manual", "₹5.10 Lakh"),
    StaticCarListing("used_car_wagonr_2018", Img.P919073, "2018 Maruti WagonR VXI", "52,900 km · Petrol · Manual", "₹4.20 Lakh"),
)

private val bodyTypeChips = listOf("suv" to "SUV", "sedan" to "Sedan", "hatchback" to "Hatchback", "muv" to "MUV")

private val bodyTypeRails: Map<String, List<StaticCarListing>> = mapOf(
    "suv" to listOf(
        StaticCarListing("car_creta", Img.P919073, "Hyundai Creta SX 2021", "32,400 km · Petrol · Automatic", "₹13.75 Lakh", "Assured"),
        StaticCarListing("car_seltos_used", Img.P919073, "Kia Seltos HTX 2020", "41,200 km · Diesel · Manual", "₹12.10 Lakh", "Assured"),
        StaticCarListing("car_nexon", Img.P358070, "Tata Nexon XZ+ 2022", "18,900 km · Petrol · Manual", "₹9.85 Lakh"),
    ),
    "sedan" to listOf(
        StaticCarListing("car_city", Img.P244206, "Honda City ZX 2021", "27,600 km · Petrol · Automatic", "₹12.90 Lakh", "Assured"),
        StaticCarListing("car_verna", Img.P100653, "Hyundai Verna SX 2020", "35,300 km · Petrol · Manual", "₹9.60 Lakh"),
        StaticCarListing("car_slavia", Img.P120049, "Skoda Slavia Style 2022", "14,200 km · Petrol · Automatic", "₹14.25 Lakh", "New arrival"),
    ),
    "hatchback" to listOf(
        StaticCarListing("car_swift_used", Img.P1149137, "Maruti Suzuki Swift VXI 2021", "24,800 km · Petrol · Manual", "₹6.45 Lakh", "Assured"),
        StaticCarListing("car_i20", Img.P1592384, "Hyundai i20 Sportz 2022", "16,500 km · Petrol · Manual", "₹7.90 Lakh", "New arrival"),
        StaticCarListing("car_altroz", Img.P170811, "Tata Altroz XZ 2021", "21,300 km · Diesel · Manual", "₹6.95 Lakh"),
    ),
    "muv" to listOf(
        StaticCarListing("car_ertiga", Img.P120049, "Maruti Suzuki Ertiga ZXI 2021", "29,400 km · Petrol · Manual", "₹9.35 Lakh", "Assured"),
        StaticCarListing("car_innova", Img.P244206, "Toyota Innova Crysta GX 2020", "52,000 km · Diesel · Manual", "₹17.80 Lakh"),
        StaticCarListing("car_carens_used", Img.P1149137, "Kia Carens Luxury Plus 2022", "12,700 km · Diesel · Automatic", "₹15.60 Lakh", "New arrival"),
    ),
)

private val trendingNewCars = listOf(
    TrendingCar("new_car_seltos", Img.P100653, "Seltos", "Kia", "#1"),
    TrendingCar("new_car_sonet", Img.P170811, "Sonet", "Kia", "#2"),
    TrendingCar("new_car_syros", Img.P210019, "Syros", "Kia", "#3"),
    TrendingCar("new_car_carens", Img.P358070, "Carens", "Kia", "#4"),
)

private val showrooms = listOf(
    ShowroomInfo("showroom_nexus_mall", Img.P1149137, "70+ cars", "Nexus Mall", "Koramangala, Bangalore", "1.5 km away · Get directions", "Open · Closes at 08:00 PM"),
    ShowroomInfo("showroom_ganganagar", Img.P358070, "90+ cars", "Cars24 Ganganagar Hub", "Ganganagar, Bangalore", "7.0 km away · Get directions", "Open · Closes at 09:00 PM"),
)

private val buyLovedRail = listOf(
    StaticCarListing("buy_loved_baleno", Img.P3802510, "2015 Maruti Baleno", "DELTA CVT PETROL 1.2 · 92,838 km · Petrol · Auto · KA05", "₹3.87 Lakh", "Cars24 Owned stock"),
    StaticCarListing("buy_loved_xuv300", Img.P1592384, "2023 Mahindra XUV300", "W6 1.2 PETROL · 25,335 km · Petrol · Manual", "₹6.60 Lakh"),
)

private val buyTabGridItems = listOf(
    Triple(Img.P170811, "All cars", "Cars at best price"),
    Triple(Img.P244206, "Budget cars", "Under ₹7 Lakhs"),
    Triple(Img.P100653, "Mid range cars", "₹7 to ₹15 Lakhs"),
    Triple(Img.P919073, "Premium cars", "Above ₹15 Lakhs"),
)

private val sellActionItems = listOf("Sell your car" to Img.P244206, "Check car valuation" to Img.P100653, "Scrap & earn" to Img.P919073)
private val sellWhyItems = listOf(
    "We give best price guarantee" to "1500+ dealers will bid on your car.",
    "We take care of everything" to "RC transfer • Paperwork",
)

private val loansTabGridItems = listOf(
    Triple(Img.P1592384, "Used car loan", "starting at 10.99%"),
    Triple(Img.P919073, "Loan against car", "Instant Cash @ 10.99%"),
    Triple(Img.P100653, "Credit score", "Get free report"),
    Triple(Img.P1592384, "Personal loan", "up to ₹50 lakh"),
)
private val loansWhyItems = listOf(
    "Fast approval processes" to "Get your loan approved faster than ever in just a few minutes. No long waits.",
    "Competitive interest rates" to "Get the best rates in the market.",
)

private val challanWhyItems = listOf(
    Img.P170811 to "No court visit required",
    Img.P3802510 to "One portal for all challans",
    Img.P244206 to "No hassle to hire a lawyer",
)

private val carCheckTiles = listOf("New car PDI" to "Pre delivery inspection", "Used car check" to "300+ point evaluation")
private val carCheckFraudItems = listOf(
    Triple(Img.P244206, "Vehicle history report", "Service records and Accidental check"),
    Triple(Img.P1592384, "Odometer fraud check", "20% cars show odometer fraud"),
    Triple(Img.P919073, "RTO check", "15% cars have RC mismatches"),
)

private val insuranceTabGridItems = listOf(
    Triple(Img.P100653, "Car insurance", "upto 85% off"),
    Triple(Img.P919073, "Bike insurance", "upto 85% off"),
    Triple(Img.P244206, "Health insurance", "upto 25% off"),
    Triple(Img.P170811, "Life insurance", "full coverage"),
)
private val insuranceWhyItems = listOf(
    "Affordable premiums" to "Compare and get best prices from 20+ trusted insurance partners",
    "Customized coverage" to "Plans built around your lifestyle with flexible add-ons.",
)

private val emiByTenure = mapOf(
    "12" to "₹1,20,850 / month",
    "36" to "₹44,150 / month",
    "60" to "₹28,900 / month",
    "84" to "₹21,750 / month",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticLandingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("all") }
    var selectedBodyType by remember { mutableStateOf("suv") }
    var selectedTenure by remember { mutableStateOf("36") }
    var showEmiSheet by remember { mutableStateOf(false) }
    var firstFrameReported by remember { mutableStateOf(false) }

    // Same mechanism as SduiPage/CollapsibleOnHide: a manual nested-scroll
    // connection lets the header eat scroll pixels (up to its own height)
    // before the body LazyColumn scrolls at all. Plain (non-`by`) state
    // refs so this composable itself never recomposes on scroll — only
    // the layout/draw-phase reads inside CollapsibleRow do.
    val maxHeaderHeightPx = remember { mutableFloatStateOf(0f) }
    val headerOffsetPx = remember { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val maxHeight = maxHeaderHeightPx.value
                if (maxHeight <= 0f) return Offset.Zero
                val current = headerOffsetPx.value
                val newOffset = (current + available.y).coerceIn(-maxHeight, 0f)
                headerOffsetPx.value = newOffset
                return Offset(0f, newOffset - current)
            }
        }
    }
    val collapseFraction = remember {
        derivedStateOf {
            val maxHeight = maxHeaderHeightPx.value
            if (maxHeight > 0f) (-headerOffsetPx.value / maxHeight).coerceIn(0f, 1f) else 0f
        }
    }

    val onNavigate: (String) -> Unit = { route ->
        Toast.makeText(context, "navigate → $route", Toast.LENGTH_SHORT).show()
    }

    // Perf marker sits on this outer Column — the same header+body wrapper
    // `SduiPage` applies its own `modifier` (and this same callback) to
    // whenever the JSON page defines a `header`, which `landing_page.json`
    // does — so both variants report "fully drawn" at the same structural
    // point (header + first body frame positioned), not one measuring a
    // narrower region than the other. See `MainActivity.kt`/`SduiRenderer.kt`.
    Column(
        modifier = modifier.onGloballyPositioned {
            if (!firstFrameReported) {
                firstFrameReported = true
                PerfTrace.mark("static_first_frame_positioned")
                (context as? Activity)?.reportFullyDrawn()
            }
        },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                if (maxHeaderHeightPx.value == 0f) maxHeaderHeightPx.value = coordinates.size.height.toFloat()
            },
        ) {
            StaticHeader(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                collapseFraction = collapseFraction,
                onSearchClick = { onNavigate("search") },
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
        ) {
            when (selectedTab) {
                "buy" -> buyTabContent(onNavigate)
                "sell" -> sellTabContent(onNavigate)
                "loans" -> loansTabContent(onNavigate)
                "challan" -> challanTabContent(onNavigate)
                "car_check" -> carCheckTabContent(onNavigate)
                "insurance" -> insuranceTabContent(onNavigate)
                else -> allTabContent(
                    selectedBodyType = selectedBodyType,
                    onSelectBodyType = { selectedBodyType = it },
                    onOpenEmiSheet = { showEmiSheet = true },
                    onNavigate = onNavigate,
                )
            }
        }
    }

    if (showEmiSheet) {
        ModalBottomSheet(onDismissRequest = { showEmiSheet = false }) {
            Column(modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 24.dp)) {
                Text("Hyundai Creta SX 2021 · ₹13.75 Lakh", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Choose loan tenure", color = Color(0xFF616161), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                StaticChipRow(
                    items = listOf("12" to "12 months", "36" to "36 months", "60" to "60 months", "84" to "84 months"),
                    selected = selectedTenure,
                    onSelect = { selectedTenure = it },
                    contentPaddingHorizontal = 0.dp,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "EMI: ${emiByTenure[selectedTenure]}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                )
            }
        }
    }
}

/** Local reimplementation of `CollapsibleOnHide` — shrinks + slides [content] as [fractionState] goes 0f -> 1f, every `.value` read confined to layout/draw phase. */
@Composable
private fun CollapsibleRow(
    fractionState: State<Float>,
    fade: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                val placeable = measurable.measure(loose)
                val height = (placeable.height * (1f - fractionState.value)).roundToInt().coerceAtLeast(0)
                layout(placeable.width, height) { placeable.placeRelative(0, 0) }
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

@Composable
private fun StaticHeader(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    collapseFraction: State<Float>,
    onSearchClick: () -> Unit,
) {
    val background = tabColor[selectedTab] ?: tabColor.getValue("all")
    Column(modifier = Modifier.fillMaxWidth().background(background)) {
        CollapsibleRow(fractionState = collapseFraction, fade = false) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("📍 Bangalore", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text("👤", fontSize = 16.sp, color = Color.White)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(6.dp, 4.dp, 6.dp, 4.dp)) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = { Text("Search Buy Used Cars", color = Color(0xFFD8D5F5)) },
                leadingIcon = { Text("🔍") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color(0xFF4E42D8),
                    disabledBorderColor = Color(0xFFD8D5F5),
                    disabledTextColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSearchClick),
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp),
        ) {
            items(navTabs, key = { it.id }) { tab ->
                StaticNavTabItem(tab = tab, selected = selectedTab == tab.id, fractionState = collapseFraction, onClick = { onSelectTab(tab.id) })
            }
        }
    }
}

@Composable
private fun StaticNavTabItem(tab: NavTab, selected: Boolean, fractionState: State<Float>, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max).padding(top = 4.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CollapsibleRow(fractionState = fractionState, fade = true, modifier = Modifier.width(74.dp)) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4E42D8))
                    .border(1.dp, Color(0xFFD8D5F5), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.icon, fontSize = 44.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(tab.label, fontSize = 14.sp, color = Color.White, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(if (selected) Color.White else Color.Transparent))
    }
}

@Composable
private fun StaticSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}

@Composable
private fun StaticTabHeadline(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
}

@Composable
private fun <T> StaticRail(title: String?, items: List<T>, itemContent: @Composable (T) -> Unit) {
    Column {
        title?.let { StaticSectionTitle(it) }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(items) { item -> itemContent(item) }
        }
    }
}

@Composable
private fun <T> StaticGrid(title: String?, columns: Int, items: List<T>, rowHeight: Dp = 220.dp, itemContent: @Composable (T) -> Unit) {
    Column {
        title?.let { StaticSectionTitle(it) }
        val rows = ceil(items.size / columns.toFloat()).toInt().coerceAtLeast(1)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
            userScrollEnabled = false,
            modifier = Modifier.height(rowHeight * rows),
        ) {
            gridItems(items) { item -> itemContent(item) }
        }
    }
}

@Composable
private fun StaticColorButton(label: String, background: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(background, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp, 20.dp, 16.dp, 20.dp),
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
    }
}

@Composable
private fun StaticImageLabelItem(imageUrl: String, label: String, imageSize: Dp = 72.dp, onClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(8.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(model = imageUrl, contentDescription = label, modifier = Modifier.size(imageSize).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun StaticGridTile(
    imageUrl: String,
    title: String,
    subtitle: String? = null,
    background: Color,
    subtitleColor: Color = Color(0xFF5B5B7A),
    imageSize: Dp = 56.dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(background, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp, 16.dp, 12.dp, 16.dp),
    ) {
        AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.size(imageSize).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2)
        subtitle?.let { Text(it, fontSize = 12.sp, color = subtitleColor, maxLines = 2) }
    }
}

@Composable
private fun StaticCarCard(car: StaticCarListing, onClick: () -> Unit) {
    Card(modifier = Modifier.width(180.dp).clickable(onClick = onClick)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                AsyncImage(model = car.imageUrl, contentDescription = car.title, modifier = Modifier.fillMaxWidth().height(110.dp))
                car.badge?.let {
                    Text(
                        it,
                        color = Color.White,
                        modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF00A86B)).padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(car.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(car.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                Text(car.price, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StaticTrendingCarCard(car: TrendingCar, onClick: () -> Unit) {
    Card(modifier = Modifier.width(180.dp).clickable(onClick = onClick)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                AsyncImage(model = car.imageUrl, contentDescription = car.title, modifier = Modifier.fillMaxWidth().height(110.dp))
                Text(
                    car.badge,
                    color = Color.White,
                    modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF00A86B)).padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(car.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(car.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

@Composable
private fun StaticShowroomRail(showrooms: List<ShowroomInfo>, onNavigate: (String) -> Unit) {
    Column {
        StaticSectionTitle("7 showrooms in your city")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            items(showrooms, key = { it.id }) { s ->
                Column(modifier = Modifier.width(220.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    AsyncImage(model = s.imageUrl, contentDescription = s.name, modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(8.dp))
                    Text(s.count, fontWeight = FontWeight.Bold)
                    Text(s.name, fontWeight = FontWeight.SemiBold)
                    Text(s.location, color = Color(0xFF616161), fontSize = 12.sp)
                    Text(s.distance, color = Color(0xFF4527A0), fontSize = 12.sp)
                    Text(s.hours, color = Color(0xFF2E7D32), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onNavigate("call_showroom") }) { Text("Call") }
                        Button(onClick = { onNavigate("showroom_detail") }) { Text("View") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticRecommendedMatchCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 8.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(model = Img.P120049, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Recommended", color = Color(0xFF4527A0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Let us find your match", fontWeight = FontWeight.SemiBold)
            Text("Answer a few simple questions and get your perfect car match in 60 seconds.", color = Color(0xFF616161), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
                Text("Find my perfect match", color = Color(0xFF4527A0), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Text("→", color = Color(0xFF4527A0))
            }
        }
    }
}

@Composable
private fun StaticBannerBox(text: String, background: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(280.dp).height(120.dp).clip(RoundedCornerShape(12.dp)).background(background).clickable(onClick = onClick),
    ) {
        Text(text, modifier = Modifier.padding(16.dp).align(Alignment.BottomStart), fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun StaticBannerRail(banners: List<Banner>, onNavigate: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(banners) { b -> StaticBannerBox(text = b.text, background = b.background, onClick = { onNavigate(b.route) }) }
    }
}

@Composable
private fun StaticWhyRow(imageUrl: String, title: String, subtitle: String, subtitleColor: Color = Color(0xFF5B5B7A), trailingChevron: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 13.sp, color = subtitleColor)
        }
        if (trailingChevron) {
            Text("›", fontSize = 20.sp, color = Color(0xFF9E9E9E))
        }
    }
}

@Composable
private fun StaticFooter(background: Color, eyebrow: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().background(background).padding(24.dp, 40.dp, 24.dp, 40.dp)) {
        eyebrow?.let {
            Text(it, color = Color.White, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
        }
        Text("better drives,\nbetter lives", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Made with ❤️ in Gurugram", color = Color.White)
    }
}

@Composable
private fun StaticChipRow(items: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit, contentPaddingHorizontal: Dp = 16.dp) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = contentPaddingHorizontal, vertical = 8.dp)) {
        items(items) { (value, label) -> FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) }) }
    }
}

@Composable
private fun StaticChallanFormCard(onSubmit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 8.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text("Enter your car's registration number", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFF392BCB), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("IND", color = Color.White, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text("(e.g. AB 12 CD 3456)", color = Color(0xFFB0B0B0))
        }
        Spacer(Modifier.height(4.dp))
        Text("This is the same as your registration number", color = Color(0xFF5B5B7A), fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSubmit) { Text("Check challan") }
    }
}

@Composable
private fun StaticCarCheckTrustStats() {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFFBE9E4)).padding(24.dp, 32.dp, 24.dp, 32.dp)) {
        Text("Trusted by 10 lakh customers across India", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row {
            Text("⭐ 4.6", color = Color(0xFFC0392B), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("42K+ Ratings", color = Color(0xFFC0392B))
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf("1 Cr+" to "Inspections completed", "10+" to "Years of experience", "200+" to "Cities covered nationwide").forEach { (num, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(num, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(label, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun LazyListScope.allTabContent(
    selectedBodyType: String,
    onSelectBodyType: (String) -> Unit,
    onOpenEmiSheet: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    item {
        StaticRail(title = "Buy car", items = buyCarRailItems) { (label, route) ->
            StaticColorButton(label = label, background = Color(0xFF1A3AAF), onClick = { onNavigate(route) })
        }
    }
    item {
        StaticRail(title = "Sell your car", items = sellCarRailItems) { (label, route) ->
            StaticColorButton(label = label, background = Color(0xFF1B5E20), onClick = { onNavigate(route) })
        }
    }
    item {
        StaticRail(title = "Get loans", items = loansRailItems) { (imageUrl, label, route) ->
            StaticImageLabelItem(imageUrl = imageUrl, label = label, onClick = { onNavigate(route) })
        }
    }
    item {
        StaticGrid(title = "Car check services", columns = 3, items = carCheckGridItems) { (imageUrl, label, route) ->
            StaticGridTile(imageUrl = imageUrl, title = label, background = Color(0xFFFFF3E0), onClick = { onNavigate(route) })
        }
    }
    item {
        StaticRail(title = "Used cars you'll love", items = usedCarsRail) { car -> StaticCarCard(car, onClick = { onNavigate("car_detail") }) }
    }
    item {
        StaticChipRow(items = bodyTypeChips, selected = selectedBodyType, onSelect = onSelectBodyType)
    }
    item {
        val cars = bodyTypeRails[selectedBodyType].orEmpty()
        StaticRail(title = "Popular by body type", items = cars) { car -> StaticCarCard(car, onClick = { onNavigate("car_detail") }) }
    }
    item {
        Button(onClick = onOpenEmiSheet, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)) {
            Text("Calculate EMI — Hyundai Creta ₹13.75L")
        }
    }
    item {
        // unknown_type_demo_live_auction equivalent: the real payload sends a
        // deliberately-unregistered "LiveAuctionWidget" type here to exercise
        // the unknown-component fallback; there's nothing to render for it.
    }
    item {
        Column(modifier = Modifier.background(Color(0xFF4527A0))) {
            StaticGrid(title = "Manage your vehicle", columns = 3, items = manageVehicleGridItems) { (imageUrl, label, route) ->
                StaticGridTile(imageUrl = imageUrl, title = label, background = Color.White, onClick = { onNavigate(route) })
            }
        }
    }
    item { StaticBannerRail(listOf(Banner("Cars24 x Spotify Premium\nAdd your car to Orbit — 3 months Spotify Premium free", Color(0xFF1B3A2E), "spotify_orbit")), onNavigate) }
    item { StaticShowroomRail(showrooms, onNavigate) }
    item {
        StaticRail(title = "Trending new cars", items = trendingNewCars) { car -> StaticTrendingCarCard(car, onClick = { onNavigate("new_car_detail") }) }
    }
    item { StaticRecommendedMatchCard(onClick = { onNavigate("car_match_quiz") }) }
    item {
        StaticBannerRail(
            listOf(
                Banner("30 DAY RETURN GUARANTEE\nWe take it back as easily as we deliver it", Color(0xFF1565C0), "return_policy"),
                Banner("200+ point inspection\nIndia's largest car inspection network", Color(0xFF00695C), "inspection_network"),
            ),
            onNavigate,
        )
    }
    item {
        StaticBannerRail(listOf(Banner("CRASHFREE INDIA\nControl. Judgment. Patience.\nIndia drives safer with M.S Dhoni", Color(0xFF4527A0), "crashfree_campaign")), onNavigate)
    }
    item { StaticFooter(background = Color(0xFF4527A0)) }
}

private fun LazyListScope.buyTabContent(onNavigate: (String) -> Unit) {
    item { StaticTabHeadline("Buy used car") }
    item {
        StaticGrid(title = null, columns = 2, items = buyTabGridItems) { (imageUrl, title, subtitle) ->
            StaticGridTile(imageUrl = imageUrl, title = title, subtitle = subtitle, background = Color(0xFFE7E4FB), onClick = { onNavigate("buy_tab_$title") })
        }
    }
    item {
        StaticRail(title = "Cars you'll love", items = buyLovedRail) { car -> StaticCarCard(car, onClick = { onNavigate("car_detail") }) }
    }
    item { StaticFooter(background = Color(0xFF0A1A6B)) }
}

private fun LazyListScope.sellTabContent(onNavigate: (String) -> Unit) {
    item { StaticTabHeadline("What would you like to do?") }
    item {
        StaticRail(title = null, items = sellActionItems) { (label, imageUrl) ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .background(Color(0xFF1B5E20), RoundedCornerShape(12.dp))
                    .clickable { onNavigate("sell_action_$label") }
                    .padding(16.dp),
            ) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                AsyncImage(model = imageUrl, contentDescription = label, modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp)))
            }
        }
    }
    item { StaticTabHeadline("Why choose Cars24?") }
    item {
        Column {
            sellWhyItems.forEach { (title, subtitle) -> StaticWhyRow(imageUrl = Img.P1149137, title = title, subtitle = subtitle) }
        }
    }
    item { StaticFooter(background = Color(0xFF1B5E20)) }
}

private fun LazyListScope.loansTabContent(onNavigate: (String) -> Unit) {
    item { StaticTabHeadline("Get loans for all your needs") }
    item {
        StaticGrid(title = null, columns = 2, items = loansTabGridItems) { (imageUrl, title, subtitle) ->
            StaticGridTile(imageUrl = imageUrl, title = title, subtitle = subtitle, background = Color(0xFFDCEBFC), onClick = { onNavigate("loans_tab_$title") })
        }
    }
    item { StaticTabHeadline("Why take a loan from us?") }
    item {
        Column {
            loansWhyItems.forEach { (title, subtitle) -> StaticWhyRow(imageUrl = Img.P210019, title = title, subtitle = subtitle) }
        }
    }
    item { StaticFooter(background = Color(0xFF1565C0)) }
}

private fun LazyListScope.challanTabContent(onNavigate: (String) -> Unit) {
    item { StaticTabHeadline("Check and pay traffic challans") }
    item { StaticChallanFormCard(onSubmit = { onNavigate("challan_lookup") }) }
    item { StaticTabHeadline("Choose Cars24 to pay challans") }
    item {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            challanWhyItems.forEach { (imageUrl, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                    AsyncImage(model = imageUrl, contentDescription = label, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
    item { StaticFooter(background = Color(0xFF00695C)) }
}

private fun LazyListScope.carCheckTabContent(onNavigate: (String) -> Unit) {
    item { StaticTabHeadline("Buy smarter with our checks") }
    item {
        StaticRail(title = null, items = carCheckTiles) { (title, subtitle) ->
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .background(Color(0xFFFBE9E4), RoundedCornerShape(12.dp))
                    .clickable { onNavigate("car_check_$title") }
                    .padding(12.dp),
            ) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF5B5B7A))
            }
        }
    }
    item { StaticTabHeadline("Uncover frauds before you buy") }
    item {
        Column {
            carCheckFraudItems.forEach { (imageUrl, title, subtitle) -> StaticWhyRow(imageUrl = imageUrl, title = title, subtitle = subtitle, trailingChevron = true) }
        }
    }
    item {
        StaticBannerRail(
            listOf(Banner("VEHICLE HISTORY REPORT\n30% cars have hidden accidental history\nGet full accidental history", Color(0xFFB2EBF2), "vehicle_history_report")),
            onNavigate,
        )
    }
    item { StaticCarCheckTrustStats() }
    item { StaticFooter(background = Color(0xFF6B2E12), eyebrow = "Driven by truth. Backed by data.") }
}

private fun LazyListScope.insuranceTabContent(onNavigate: (String) -> Unit) {
    item { StaticTabHeadline("Get insurance for all your needs") }
    item {
        StaticGrid(title = null, columns = 2, items = insuranceTabGridItems) { (imageUrl, title, subtitle) ->
            StaticGridTile(imageUrl = imageUrl, title = title, subtitle = subtitle, background = Color(0xFFDCEBFC), onClick = { onNavigate("insurance_tab_$title") })
        }
    }
    item { StaticTabHeadline("Why take insurance from us?") }
    item {
        Column {
            insuranceWhyItems.forEach { (title, subtitle) -> StaticWhyRow(imageUrl = Img.P170811, title = title, subtitle = subtitle) }
        }
    }
    item { StaticFooter(background = Color(0xFF1565C0)) }
}
