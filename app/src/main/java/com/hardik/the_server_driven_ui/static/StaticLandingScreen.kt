package com.hardik.the_server_driven_ui.static

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hardik.the_server_driven_ui.sdui.perf.PerfTrace

/**
 * Hand-written, hardcoded twin of the SDUI landing page — same visual
 * content as `assets/sdui/landing_page.json`, but every section, every
 * car, and every piece of copy is a literal in this file. No JSON
 * parsing, no component registry, no runtime dispatch. This is the
 * baseline `PERF.md` benchmarks the SDUI version against.
 */
private data class StaticCar(
    val id: String,
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val badge: String? = null,
)

private val suvCars = listOf(
    StaticCar("car_creta", "https://picsum.photos/seed/creta/400/300", "Hyundai Creta SX 2021", "32,400 km · Petrol · Automatic", "₹13.75 Lakh", "Assured"),
    StaticCar("car_seltos", "https://picsum.photos/seed/seltos/400/300", "Kia Seltos HTX 2020", "41,200 km · Diesel · Manual", "₹12.10 Lakh", "Assured"),
    StaticCar("car_nexon", "https://picsum.photos/seed/nexon/400/300", "Tata Nexon XZ+ 2022", "18,900 km · Petrol · Manual", "₹9.85 Lakh"),
    StaticCar("car_xuv700", "https://picsum.photos/seed/xuv700/400/300", "Mahindra XUV700 AX7 2022", "22,050 km · Diesel · Automatic", "₹19.40 Lakh", "New arrival"),
)

private val sedanCars = listOf(
    StaticCar("car_city", "https://picsum.photos/seed/city/400/300", "Honda City ZX 2021", "27,600 km · Petrol · Automatic", "₹12.90 Lakh", "Assured"),
    StaticCar("car_verna", "https://picsum.photos/seed/verna/400/300", "Hyundai Verna SX 2020", "35,300 km · Petrol · Manual", "₹9.60 Lakh"),
    StaticCar("car_slavia", "https://picsum.photos/seed/slavia/400/300", "Skoda Slavia Style 2022", "14,200 km · Petrol · Automatic", "₹14.25 Lakh", "New arrival"),
    StaticCar("car_ciaz", "https://picsum.photos/seed/ciaz/400/300", "Maruti Suzuki Ciaz Alpha 2019", "48,700 km · Petrol · Manual", "₹8.15 Lakh"),
)

private val hatchbackCars = listOf(
    StaticCar("car_swift", "https://picsum.photos/seed/swift/400/300", "Maruti Suzuki Swift VXI 2021", "24,800 km · Petrol · Manual", "₹6.45 Lakh", "Assured"),
    StaticCar("car_i20", "https://picsum.photos/seed/i20/400/300", "Hyundai i20 Sportz 2022", "16,500 km · Petrol · Manual", "₹7.90 Lakh", "New arrival"),
    StaticCar("car_altroz", "https://picsum.photos/seed/altroz/400/300", "Tata Altroz XZ 2021", "21,300 km · Diesel · Manual", "₹6.95 Lakh"),
    StaticCar("car_baleno", "https://picsum.photos/seed/baleno/400/300", "Maruti Suzuki Baleno Delta 2020", "30,100 km · Petrol · Manual", "₹6.10 Lakh", "Assured"),
)

private val muvCars = listOf(
    StaticCar("car_ertiga", "https://picsum.photos/seed/ertiga/400/300", "Maruti Suzuki Ertiga ZXI 2021", "29,400 km · Petrol · Manual", "₹9.35 Lakh", "Assured"),
    StaticCar("car_innova", "https://picsum.photos/seed/innova/400/300", "Toyota Innova Crysta GX 2020", "52,000 km · Diesel · Manual", "₹17.80 Lakh"),
    StaticCar("car_carens", "https://picsum.photos/seed/carens/400/300", "Kia Carens Luxury Plus 2022", "12,700 km · Diesel · Automatic", "₹15.60 Lakh", "New arrival"),
    StaticCar("car_xl6", "https://picsum.photos/seed/xl6/400/300", "Maruti Suzuki XL6 Alpha 2021", "26,900 km · Petrol · Automatic", "₹11.20 Lakh"),
)

private val certifiedGridCars = listOf(
    StaticCar("car_grid_venue", "https://picsum.photos/seed/venue/400/300", "Hyundai Venue S 2021", "19,800 km · Petrol", "₹8.75 Lakh", "Assured"),
    StaticCar("car_grid_brezza", "https://picsum.photos/seed/brezza/400/300", "Maruti Suzuki Brezza ZXI 2022", "9,300 km · Petrol", "₹10.90 Lakh", "New arrival"),
    StaticCar("car_grid_thar", "https://picsum.photos/seed/thar/400/300", "Mahindra Thar LX 2021", "15,600 km · Diesel", "₹14.10 Lakh"),
    StaticCar("car_grid_punch", "https://picsum.photos/seed/punch/400/300", "Tata Punch Creative 2022", "11,200 km · Petrol", "₹7.35 Lakh", "Assured"),
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
    var selectedCategory by remember { mutableStateOf("suv") }
    var selectedTenure by remember { mutableStateOf("36") }
    var showEmiSheet by remember { mutableStateOf(false) }
    var firstFrameReported by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.onGloballyPositioned {
            if (!firstFrameReported) {
                firstFrameReported = true
                PerfTrace.mark("static_first_frame_positioned")
                (context as? Activity)?.reportFullyDrawn()
            }
        },
    ) {
        item { StaticSearchHeader() }
        item { StaticBannerCarousel() }
        item {
            StaticCategoryChipRow(selected = selectedCategory, onSelect = { selectedCategory = it })
        }
        item {
            val cars = when (selectedCategory) {
                "sedan" -> sedanCars
                "hatchback" -> hatchbackCars
                "muv" -> muvCars
                else -> suvCars
            }
            StaticCarRail(title = "Popular near you", cars = cars)
        }
        item { StaticValuePropStrip() }
        item { Button(onClick = { showEmiSheet = true }, modifier = Modifier.padding(16.dp)) { Text("Calculate EMI — Hyundai Creta ₹13.75L") } }
        item { StaticCertifiedGrid() }
        item { StaticFooterCta() }
    }

    if (showEmiSheet) {
        ModalBottomSheet(onDismissRequest = { showEmiSheet = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Hyundai Creta SX 2021 · ₹13.75 Lakh", fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Choose loan tenure", color = Color(0xFF616161))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("12" to "12 months", "36" to "36 months", "60" to "60 months", "84" to "84 months").forEach { (value, label) ->
                        FilterChip(
                            selected = selectedTenure == value,
                            onClick = { selectedTenure = value },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "EMI: ${emiByTenure[selectedTenure]}",
                    fontSize = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                )
            }
        }
    }
}

@Composable
private fun StaticSearchHeader() {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Find your next car", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = { Text("Search by brand, model or budget") },
            modifier = Modifier.fillMaxWidth().clickable { },
        )
    }
}

private data class StaticBanner(val title: String, val background: Color)

@Composable
private fun StaticBannerCarousel() {
    val banners = listOf(
        StaticBanner("CARS24 Assured — 140 point inspected", Color(0xFF2E7D32)),
        StaticBanner("Car loans from 9.4% interest", Color(0xFF1565C0)),
        StaticBanner("Sell your car in 30 minutes", Color(0xFFEF6C00)),
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(banners) { banner ->
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(banner.background)
                    .clickable { },
            ) {
                Text(
                    banner.title,
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

private val categories = listOf("suv" to "SUV", "sedan" to "Sedan", "hatchback" to "Hatchback", "muv" to "MUV")

@Composable
private fun StaticCategoryChipRow(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(categories) { (value, label) ->
            FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) })
        }
    }
}

@Composable
private fun StaticCarRail(title: String, cars: List<StaticCar>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(cars) { car -> StaticCarCard(car) }
        }
    }
}

@Composable
private fun StaticCarCard(car: StaticCar) {
    Card(modifier = Modifier.width(180.dp)) {
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

private val valueProps = listOf(
    "🛡️" to "140-point\ninspected",
    "🔄" to "5-day money\nback",
    "📄" to "1-year\nwarranty",
    "🚚" to "Free home\ndelivery",
)

@Composable
private fun StaticValuePropStrip() {
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        valueProps.forEach { (icon, label) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(icon, style = MaterialTheme.typography.titleLarge)
                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 2)
            }
        }
    }
}

@Composable
private fun StaticCertifiedGrid() {
    Column {
        Text("Certified cars this week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.height(440.dp),
        ) {
            gridItems(certifiedGridCars) { car -> StaticCarCard(car) }
        }
    }
}

@Composable
private fun StaticFooterCta() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFE0B2))
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Have a car to sell?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Get the best price, doorstep pickup, instant payment.", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { }) { Text("Get a free quote") }
    }
}
