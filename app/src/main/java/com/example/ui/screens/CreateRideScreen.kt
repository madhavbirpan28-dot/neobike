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
import com.example.data.model.RideVisibility
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun CreateRideScreen(
    viewModel: NeoViewModel,
    onRideCreated: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("2026-11-05") }
    var departureTime by remember { mutableStateOf("06:00 AM") }
    var capacity by remember { mutableIntStateOf(4) }
    var visibility by remember { mutableStateOf(RideVisibility.PUBLIC) }
    var tripType by remember { mutableStateOf("Weekend Tour") }
    var pace by remember { mutableStateOf("Balanced (80-100 km/h)") }
    var experienceLevel by remember { mutableStateOf("Intermediate") }
    var description by remember { mutableStateOf("") }
    var meetingPoint by remember { mutableStateOf("") }
    var plannedStopsText by remember { mutableStateOf("Midway Fuel Hub, Hillside Viewpoint") }
    var distanceKm by remember { mutableIntStateOf(240) }
    var durationHours by remember { mutableDoubleStateOf(4.5) }
    var fuelNotes by remember { mutableStateOf("IOCL / HP pumps available every 80 km") }
    var foodNotes by remember { mutableStateOf("Breakfast stop at highway dhaba") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tripTypes = listOf("Weekend Tour", "Scenic Mountain", "Coastal Run", "Long Distance", "Breakfast Ride")
    val paces = listOf("Relaxed (60-80 km/h)", "Balanced (80-100 km/h)", "Brisk (100-120 km/h)")
    val experiences = listOf("All Welcomed", "Beginner Friendly", "Intermediate", "Seasoned Tourers Only")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("create_ride_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Post a New Ride",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Share your touring itinerary, route corridor, and welcome compatible co-riders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NeoError.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = NeoError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Essential Info Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Essential Ride Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Ride Title *") },
                        placeholder = { Text("e.g. Pune to Mahabaleshwar Strawberry Run") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_ride_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = origin,
                            onValueChange = { origin = it },
                            label = { Text("Origin City *") },
                            placeholder = { Text("e.g. Pune") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_ride_origin_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text("Destination *") },
                            placeholder = { Text("e.g. Mahabaleshwar") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_ride_dest_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = departureDate,
                            onValueChange = { departureDate = it },
                            label = { Text("Date (YYYY-MM-DD) *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_ride_date_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = departureTime,
                            onValueChange = { departureTime = it },
                            label = { Text("Departure Time *") },
                            placeholder = { Text("06:00 AM") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_ride_time_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = meetingPoint,
                        onValueChange = { meetingPoint = it },
                        label = { Text("Public Meeting Point *") },
                        placeholder = { Text("e.g. Chandani Chowk BPCL Pump, Pune") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_ride_meeting_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Capacity and Preferences Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Capacity & Riding Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Capacity stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Rider Capacity", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Including yourself as lead", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (capacity > 1) capacity-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, "Decrease", tint = NeoTealPrimary)
                            }
                            Text(
                                text = "$capacity riders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { if (capacity < 12) capacity++ }) {
                                Icon(Icons.Default.AddCircleOutline, "Increase", tint = NeoTealPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trip Type selection
                    Text("Trip Type", fontSize = 12.sp, color = NeoTealLight)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(tripTypes) { type ->
                            FilterChip(
                                selected = tripType == type,
                                onClick = { tripType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pace selection
                    Text("Preferred Group Pace", fontSize = 12.sp, color = NeoAmberAccent)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(paces) { p ->
                            FilterChip(
                                selected = pace == p,
                                onClick = { pace = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Experience selection
                    Text("Target Experience Level", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(experiences) { exp ->
                            FilterChip(
                                selected = experienceLevel == exp,
                                onClick = { experienceLevel = exp },
                                label = { Text(exp, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Route & Stops Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Route & Stops", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = plannedStopsText,
                        onValueChange = { plannedStopsText = it },
                        label = { Text("Planned Stops (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = distanceKm.toString(),
                            onValueChange = { distanceKm = it.toIntOrNull() ?: distanceKm },
                            label = { Text("Distance (km)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = durationHours.toString(),
                            onValueChange = { durationHours = it.toDoubleOrNull() ?: durationHours },
                            label = { Text("Est. Duration (hrs)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Trip Description & Road Rules") },
                        placeholder = { Text("Tell co-riders about highway conditions, scenic photo stops, and safety rules.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Publish Button
        item {
            Button(
                onClick = {
                    if (title.isBlank() || origin.isBlank() || destination.isBlank() || meetingPoint.isBlank()) {
                        errorMessage = "Please fill in title, origin, destination, and meeting point."
                        return@Button
                    }
                    errorMessage = null

                    val stopsList = plannedStopsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    viewModel.createRide(
                        title = title,
                        origin = origin,
                        destination = destination,
                        departureDate = departureDate,
                        departureTime = departureTime,
                        capacity = capacity,
                        visibility = visibility,
                        tripType = tripType,
                        pace = pace,
                        experienceLevel = experienceLevel,
                        description = description.ifBlank { "Exciting group touring ride with fellow riders." },
                        routeNotes = "Scenic highways and mountain passes. Regular fuel and chai breaks.",
                        plannedStops = stopsList,
                        meetingPoint = meetingPoint,
                        fuelNotes = fuelNotes,
                        foodNotes = foodNotes,
                        rules = listOf("Full riding gear mandatory (helmet, jacket, gloves)", "Maintain staggered convoy spacing"),
                        distanceKm = distanceKm,
                        durationHours = durationHours
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_ride_submit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary)
            ) {
                Icon(Icons.Default.Publish, "Publish", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Ride & Match Partners", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
