package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.*
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel
import com.example.ui.viewmodel.RouteMatchResult

@Composable
fun FindRideScreen(
    viewModel: NeoViewModel,
    onOpenRideDetail: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val searchOrigin by viewModel.searchOrigin.collectAsState()
    val searchDestination by viewModel.searchDestination.collectAsState()
    val filterTripType by viewModel.filterTripType.collectAsState()
    val filterPace by viewModel.filterPace.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    var showRequestDialogForRide by remember { mutableStateOf<Ride?>(null) }
    var requestNote by remember { mutableStateOf("Hey, our routes and pace align well! Would love to ride together.") }
    var confirmedMeetInMiddle by remember { mutableStateOf(false) }

    val tripTypeOptions = listOf("All", "Coastal", "Mountain", "Highway", "Breakfast")
    val paceOptions = listOf("All", "Relaxed", "Balanced", "Brisk")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("find_ride_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filter Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Smart Route & Corridor Matcher",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Match by destination, shared highway segment, or compatible riding pace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchOrigin,
                            onValueChange = { viewModel.searchOrigin.value = it },
                            label = { Text("From (e.g. Pune / Nashik)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_origin_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = searchDestination,
                            onValueChange = { viewModel.searchDestination.value = it },
                            label = { Text("To (e.g. Goa)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_destination_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Trip Type Filters
                    Text("Trip Type", style = MaterialTheme.typography.labelSmall, color = NeoTealLight)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(tripTypeOptions) { type ->
                            FilterChip(
                                selected = filterTripType == type,
                                onClick = { viewModel.filterTripType.value = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeoTealDark,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pace Filters
                    Text("Riding Pace", style = MaterialTheme.typography.labelSmall, color = NeoAmberAccent)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(paceOptions) { pace ->
                            FilterChip(
                                selected = filterPace == pace,
                                onClick = { viewModel.filterPace.value = pace },
                                label = { Text(pace, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeoAmberDark,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Meet-in-the-Middle Finder Card (Feature 3)
        // If results contain a shared corridor recommendation (e.g. Nashik & Pune meeting at Kolhapur)
        val meetInMiddleResult = results.firstOrNull { it.meetInMiddleSuggestion != null }
        if (meetInMiddleResult != null) {
            val suggestion = meetInMiddleResult.meetInMiddleSuggestion!!
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeoAmberAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meet_in_middle_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🤝", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MEET-IN-THE-MIDDLE FINDER",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoAmberAccent,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            StatusBadge("Corridor Overlap", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = suggestion.recommendationReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggested meeting spot
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = suggestion.meetingPointName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = suggestion.highwayOrRegion,
                                    fontSize = 12.sp,
                                    color = NeoTealLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "📍 Rider A: ~${suggestion.distanceFromRiderAKm} km",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "📍 Rider B: ~${suggestion.distanceFromRiderBKm} km",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "🕒 ${suggestion.estimatedTravelTime}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeoAmberAccent
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Facilities: ${suggestion.facilities.joinToString(" • ")}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { confirmedMeetInMiddle = !confirmedMeetInMiddle },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (confirmedMeetInMiddle) NeoSuccess else NeoAmberAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (confirmedMeetInMiddle) "Meeting Point Agreed ✓" else "Agree on this Meeting Point",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search Results Header
        item {
            SectionHeader(
                title = "Matched Tours (${results.size})",
                subtitle = "Sorted by compatibility score and shared route corridors"
            )
        }

        if (results.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No active rides match this specific query.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try clearing filters or post your own ride to attract touring partners!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(results) { result ->
            MatchedRideCard(
                result = result,
                currentUserId = currentUser.id,
                onOpenDetail = { onOpenRideDetail(result.ride.id) },
                onSaveToggle = { viewModel.toggleSaveRide(result.ride.id) },
                onRequestJoin = { showRequestDialogForRide = result.ride }
            )
        }
    }

    // Join Request Dialog
    if (showRequestDialogForRide != null) {
        val ride = showRequestDialogForRide!!
        AlertDialog(
            onDismissRequest = { showRequestDialogForRide = null },
            title = {
                Text("Join Request: ${ride.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        "Send a message to ${ride.organizerName} with your experience, bike details, and where you'd like to join:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = requestNote,
                        onValueChange = { requestNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your profile (${currentUser.motorcycle}, ${currentUser.experienceLevel}) will be shared with the organizer.",
                        fontSize = 11.sp,
                        color = NeoTealLight
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestToJoinRide(ride.id, requestNote)
                        showRequestDialogForRide = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary),
                    modifier = Modifier.testTag("submit_join_request_btn")
                ) {
                    Text("Send Request", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialogForRide = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun MatchedRideCard(
    result: RouteMatchResult,
    currentUserId: String,
    onOpenDetail: () -> Unit,
    onSaveToggle: () -> Unit,
    onRequestJoin: () -> Unit
) {
    val ride = result.ride
    val isOrganizer = ride.organizerId == currentUserId
    val isConfirmed = ride.confirmedRiderIds.contains(currentUserId)
    val isPending = ride.pendingRequestRiderIds.contains(currentUserId)
    val availableSlots = (ride.capacity - ride.confirmedRiderIds.size).coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
            .testTag("matched_ride_card_${ride.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Compatibility Score Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeoTealDark.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeoTealPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎯", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${result.compatibilityScore}% Compatibility",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeoTealLight
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSaveToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (ride.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Ride",
                            tint = if (ride.isSaved) NeoError else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusBadge(
                        text = "$availableSlots / ${ride.capacity} slots",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ride.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Origin -> Destination
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ride.origin, fontWeight = FontWeight.Bold, color = NeoAmberAccent, fontSize = 14.sp)
                Icon(Icons.Default.ArrowForward, "to", tint = NeoTealPrimary, modifier = Modifier.padding(horizontal = 6.dp).size(16.dp))
                Text(ride.destination, fontWeight = FontWeight.Bold, color = NeoTealPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${ride.distanceKm} km", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Match Reasons breakdown
            result.matchReasons.take(2).forEach { reason ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, "Match reason", tint = NeoSuccess, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(reason, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Lead & CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ride.organizerAvatar, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lead: ${ride.organizerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                when {
                    isOrganizer -> {
                        StatusBadge("You are Host", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                    }
                    isConfirmed -> {
                        StatusBadge("Confirmed ✓", NeoSuccess.copy(alpha = 0.2f), NeoSuccess)
                    }
                    isPending -> {
                        StatusBadge("Pending Approval", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                    }
                    availableSlots > 0 -> {
                        Button(
                            onClick = onRequestJoin,
                            colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_request_join_${ride.id}")
                        ) {
                            Text("Request to Join", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        StatusBadge("Ride Full", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
