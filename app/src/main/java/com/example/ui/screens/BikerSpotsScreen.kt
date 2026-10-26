package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BikerSpot
import com.example.data.model.SpotCategory
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun BikerSpotsScreen(
    viewModel: NeoViewModel
) {
    val spots by viewModel.bikerSpots.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SpotCategory?>(null) }

    val filtered = spots.filter { spot ->
        val matchesCategory = selectedCategory == null || spot.category == selectedCategory
        val matchesSearch = searchKeyword.isBlank() ||
                spot.name.contains(searchKeyword, ignoreCase = true) ||
                spot.location.contains(searchKeyword, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("biker_spots_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Biker Spot Finder",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Verified 24/7 fuel pumps, emergency workshops, rider cafes, and scenic passes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                placeholder = { Text("Search by highway, town, or spot name...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search", tint = NeoTealPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spot_search_field"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Spots", fontSize = 11.sp) }
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
                        }
                    )
                }
            }
        }

        items(filtered) { spot ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
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
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                        fontSize = 11.sp,
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
                }
            }
        }
    }
}
