package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun RideDetailScreen(
    rideId: String,
    viewModel: NeoViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    BackHandler { onBack() }

    val allRides by viewModel.rides.collectAsState()
    val allRequests by viewModel.requests.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val checkIns by viewModel.tripCheckIns.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val ride = allRides.find { it.id == rideId }
    if (ride == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Ride not found.")
        }
        return
    }

    val isOrganizer = ride.organizerId == currentUser.id
    val isConfirmed = ride.confirmedRiderIds.contains(currentUser.id)
    val isPending = ride.pendingRequestRiderIds.contains(currentUser.id)
    val rideRequests = allRequests.filter { it.rideId == rideId && it.status == RequestStatus.PENDING }
    val availableSlots = (ride.capacity - ride.confirmedRiderIds.size).coerceAtLeast(0)

    val currentCheckIn = checkIns[ride.id]
    val relatedConversation = conversations.find { it.pinnedRideId == ride.id }

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("Weather conditions unfavourable") }

    // Pre-ride checklist interactive toggles
    var checkTires by remember { mutableStateOf(true) }
    var checkGear by remember { mutableStateOf(true) }
    var checkContact by remember { mutableStateOf(false) }
    var checkFuel by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ride_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav Back Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("ride_detail_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleSaveRide(ride.id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (ride.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Ride",
                            tint = if (ride.isSaved) NeoError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.openReportDialog(ReportTargetType.RIDE, ride.id, ride.title)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Report, "Report", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Title and Status Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = ride.status.name,
                            containerColor = when (ride.status) {
                                RideStatus.ACTIVE -> NeoSuccess.copy(alpha = 0.2f)
                                RideStatus.CANCELLED -> NeoError.copy(alpha = 0.2f)
                                RideStatus.COMPLETED -> NeoInfo.copy(alpha = 0.2f)
                                RideStatus.PAUSED -> NeoAmberAccent.copy(alpha = 0.2f)
                            },
                            contentColor = when (ride.status) {
                                RideStatus.ACTIVE -> NeoSuccess
                                RideStatus.CANCELLED -> NeoError
                                RideStatus.COMPLETED -> NeoInfo
                                RideStatus.PAUSED -> NeoAmberAccent
                            }
                        )
                        Text(
                            text = "$availableSlots / ${ride.capacity} slots left",
                            fontWeight = FontWeight.Bold,
                            color = NeoTealLight,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = ride.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Route Summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ride.origin, fontWeight = FontWeight.Bold, color = NeoAmberAccent, fontSize = 16.sp)
                        Icon(Icons.Default.ArrowForward, "to", tint = NeoTealPrimary, modifier = Modifier.padding(horizontal = 8.dp).size(20.dp))
                        Text(ride.destination, fontWeight = FontWeight.Bold, color = NeoTealPrimary, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🗓️ ${ride.departureDate} at ${ride.departureTime} • ~${ride.durationHours} hrs (${ride.distanceKm} km)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📍 Meeting Point: ${ride.meetingPoint}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoAmberAccent
                    )
                }
            }
        }

        // Host and Confirmed Riders
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tour Host & Participants", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Host Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeoTealDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ride.organizerAvatar, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ride.organizerName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge("HOST", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                            }
                            Text(
                                "Pace: ${ride.pace}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Confirmed Co-riders
                    val otherConfirmed = ride.confirmedRiderIds.filter { it != ride.organizerId }
                    if (otherConfirmed.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = NeoDarkBorder)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Confirmed Co-Riders (${otherConfirmed.size})", fontSize = 12.sp, color = NeoTealLight, fontWeight = FontWeight.SemiBold)
                        otherConfirmed.forEach { riderId ->
                            val u = allUsers.find { it.id == riderId }
                            if (u != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(u.avatarEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(u.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${u.motorcycle})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Group Chat Button if confirmed or organizer
                    if (isConfirmed || isOrganizer) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (relatedConversation != null) {
                                    onOpenChat(relatedConversation.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_ride_group_chat_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, "Chat", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Confirmed Ride Chat", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Host Pending Requests Management Section (Feature 8: View/Accept/Decline Requests)
        if (isOrganizer && rideRequests.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeoAmberAccent),
                    modifier = Modifier.fillMaxWidth().testTag("pending_requests_section")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Join Requests (${rideRequests.size})", fontWeight = FontWeight.Bold, color = NeoAmberAccent)
                            StatusBadge("Organizer Action", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Review applicant riding pace and bike before confirming partner slots.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        rideRequests.forEach { req ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(req.riderAvatar, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(req.riderName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${req.riderMotorcycle} • ${req.riderExperience}", fontSize = 11.sp, color = NeoTealLight)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("\"${req.note}\"", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.declineJoinRequest(req.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeoError),
                                            modifier = Modifier.testTag("decline_req_${req.id}")
                                        ) {
                                            Text("Decline", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { viewModel.acceptJoinRequest(req.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NeoSuccess),
                                            modifier = Modifier.testTag("accept_req_${req.id}")
                                        ) {
                                            Text("Accept & Confirm", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trip Check-In & Live Status (Feature 15 & 21)
        if (isConfirmed || isOrganizer) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeoTealPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("trip_checkin_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trip Status & Check-In", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            StatusBadge(
                                currentCheckIn?.status?.name ?: "SCHEDULED",
                                NeoTealDark.copy(alpha = 0.4f),
                                NeoTealLight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Safety feature: Keep trusted contacts and co-riders updated at each planned waypoint without invasive background battery drain.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateTripCheckIn(ride.id, CheckInStatus.ON_THE_WAY, "Departed ${ride.origin}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeoTealDark),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mark Started", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    viewModel.updateTripCheckIn(ride.id, CheckInStatus.STOPPED_AT_WAYPOINT, "Midpoint Rest Stop")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeoAmberDark),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Check-in Stop", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    viewModel.updateTripCheckIn(ride.id, CheckInStatus.COMPLETED, "Reached ${ride.destination}")
                                    viewModel.completeRide(ride.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeoSuccess),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Complete", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Planned Stops and Itinerary
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Planned Stops & Corridors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    ride.plannedStops.forEachIndexed { index, stop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeoTealDark,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stop, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (ride.fuelBreakNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("⛽ Fuel Breaks: ${ride.fuelBreakNotes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (ride.foodBreakNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🍽️ Meals & Rest: ${ride.foodBreakNotes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Pre-Ride Safety Checklist (Feature 7 & 13)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Checklist, "Checklist", tint = NeoTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pre-Ride Safety Checklist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Complete before turning the ignition:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    ChecklistItem("Tire cold pressures, chain tension & engine oil level", checkTires) { checkTires = it }
                    ChecklistItem("Certified helmet (DOT/ECE), armored jacket, gloves & boots", checkGear) { checkGear = it }
                    ChecklistItem("Emergency trusted contact briefed about route & checkpoints", checkContact) { checkContact = it }
                    ChecklistItem("Emergency cash & offline maps downloaded on phone", checkFuel) { checkFuel = it }
                }
            }
        }

        // Ride Rules & Guidelines
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ride Guidelines & Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ride.rideRules.forEach { rule ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text("•", color = NeoAmberAccent, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(rule, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Organizer destructive actions (Cancel Ride)
        if (isOrganizer && ride.status == RideStatus.ACTIVE) {
            item {
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeoError),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Cancel, "Cancel", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Ride Listing")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Cancel Ride confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Ride Listing?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "All confirmed riders will be notified. This cannot be undone.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelRide(ride.id, cancelReason)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoError)
                ) {
                    Text("Confirm Cancel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Go Back")
                }
            }
        )
    }
}

@Composable
fun ChecklistItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = NeoTealPrimary)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
