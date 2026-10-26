package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BikerSpot
import com.example.data.model.SpotCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun BikerSpotsScreen(
    viewModel: NeoViewModel
) {
    val context = LocalContext.current
    val spots by viewModel.bikerSpots.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SpotCategory?>(null) }
    var showEmergencySheet by remember { mutableStateOf(false) }

    val filtered = spots.filter { spot ->
        val matchesCategory = selectedCategory == null || spot.category == selectedCategory
        val matchesSearch = searchKeyword.isBlank() ||
                spot.name.contains(searchKeyword, ignoreCase = true) ||
                spot.location.contains(searchKeyword, ignoreCase = true) ||
                spot.address.contains(searchKeyword, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("biker_spots_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Emergency Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Explore & Biker Map",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "24/7 Fuel pumps, repair clinics, rider cafes & emergency points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showEmergencySheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoError),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("emergency_helpline_button")
                ) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Emergency", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                placeholder = { Text("Search by highway, pass, town, or spot name...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search", tint = NeoOrangePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spot_search_field"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Interactive Map Visual Preview Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = BorderStroke(1.dp, NeoOrangePrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("interactive_map_preview_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🗺️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Google Maps Navigation Active",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap 'Navigate' on any spot to open turn-by-turn directions",
                                    fontSize = 11.sp,
                                    color = NeoTealLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stylized map coordinates strip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍 Current Hub: Western Ghats Corridor (NH160 / NH66)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("GPS Ready", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeoTealPrimary)
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Spots (${spots.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeoOrangePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(SpotCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = when (cat) {
                                    SpotCategory.FUEL_STATION -> "⛽ Fuel 24/7"
                                    SpotCategory.REPAIR_WORKSHOP -> "🔧 Workshop"
                                    SpotCategory.BIKER_CAFE -> "☕ Rider Cafe"
                                    SpotCategory.SCENIC_VIEWPOINT -> "⛰️ Viewpoint"
                                    SpotCategory.MEETPOINT -> "📍 Meetpoint"
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeoOrangePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Spots List
        items(filtered) { spot ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biker_spot_${spot.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (spot.category) {
                                    SpotCategory.FUEL_STATION -> "⛽"
                                    SpotCategory.REPAIR_WORKSHOP -> "🔧"
                                    SpotCategory.BIKER_CAFE -> "☕"
                                    SpotCategory.SCENIC_VIEWPOINT -> "⛰️"
                                    SpotCategory.MEETPOINT -> "📍"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = spot.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = spot.location,
                                    fontSize = 12.sp,
                                    color = NeoTealLight
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.toggleFavoriteSpot(spot.id) }) {
                            Icon(
                                imageVector = if (spot.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (spot.isFavorite) NeoError else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 ${spot.address}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🕒 ${spot.operatingHours} • ★ ${spot.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoAmberAccent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        spot.highlights.take(3).forEach { hl ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = hl,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = NeoDarkBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Google Maps Navigation Action Button
                    Button(
                        onClick = { launchGoogleMapsDirections(context, spot.name, spot.address) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeoOrangePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("navigate_maps_button_${spot.id}")
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigate with Google Maps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Emergency Helpline Sheet
    if (showEmergencySheet) {
        AlertDialog(
            onDismissRequest = { showEmergencySheet = false },
            title = { Text("Emergency Rider Support Helplines") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("In case of highway breakdown or medical emergency:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EmergencyContactRow("National Highway Patrol", "1033", context)
                    EmergencyContactRow("Medical Ambulance Emergency", "108", context)
                    EmergencyContactRow("NEO Highway Assistance Volunteers", "1800-NEO-RIDE", context)
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmergencySheet = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun EmergencyContactRow(label: String, number: String, context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(number, fontSize = 11.sp, color = NeoOrangePrimary)
        }
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeoError),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("Call", fontSize = 11.sp)
        }
    }
}

private fun launchGoogleMapsDirections(context: Context, spotName: String, address: String) {
    val query = Uri.encode("$spotName $address")
    val gmmIntentUri = Uri.parse("google.navigation:q=$query")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback to web maps intent
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$query"))
        try {
            context.startActivity(webIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}
