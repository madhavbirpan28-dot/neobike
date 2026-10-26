package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Event
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun EventsScreen(
    viewModel: NeoViewModel
) {
    val events by viewModel.events.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val communities by viewModel.communities.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("events_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rides & Meetup Events",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Group touring runs, breakfast meetups, and safety workshops.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoAmberDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_create_event")
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Host Event", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        items(events) { event ->
            val isGoing = event.goingUserIds.contains(currentUser.id)
            val isNotGoing = event.notGoingUserIds.contains(currentUser.id)
            val availableCapacity = (event.capacity - event.goingUserIds.size).coerceAtLeast(0)

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_card_${event.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.communityName ?: "Open Community Ride",
                            fontSize = 12.sp,
                            color = NeoAmberAccent,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(
                            text = "$availableCapacity / ${event.capacity} spots",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "🗓️ ${event.dateTime}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoTealLight
                    )
                    Text(
                        text = "📍 Meeting: ${event.meetingPoint}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (event.routePlan.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Route: ${event.routePlan}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // RSVP Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${event.goingUserIds.size} Riders Confirmed",
                            fontSize = 12.sp,
                            color = NeoTealLight,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row {
                            OutlinedButton(
                                onClick = { viewModel.rsvpEvent(event.id, false) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isNotGoing) NeoError else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("rsvp_not_going_${event.id}")
                            ) {
                                Text(if (isNotGoing) "Can't Make It ✓" else "Decline", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.rsvpEvent(event.id, true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isGoing) NeoSuccess else NeoTealPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("rsvp_going_${event.id}")
                            ) {
                                Text(
                                    text = if (isGoing) "Going ✓" else "RSVP Going",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Host Event Dialog
    if (showCreateDialog) {
        var eventTitle by remember { mutableStateOf("") }
        var eventDesc by remember { mutableStateOf("") }
        var eventDate by remember { mutableStateOf("Sat, Nov 14 • 06:00 AM") }
        var eventMeet by remember { mutableStateOf("") }
        var eventRoute by remember { mutableStateOf("") }
        var eventCap by remember { mutableIntStateOf(15) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Host a Motorcycle Event", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Event Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Date & Time") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventMeet,
                        onValueChange = { eventMeet = it },
                        label = { Text("Meeting Point *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventRoute,
                        onValueChange = { eventRoute = it },
                        label = { Text("Route Plan (e.g. Pune -> Lavasa)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventDesc,
                        onValueChange = { eventDesc = it },
                        label = { Text("Description & Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (eventTitle.isNotBlank() && eventMeet.isNotBlank()) {
                            viewModel.createEvent(
                                title = eventTitle,
                                description = eventDesc.ifBlank { "Weekend ride organized by ${currentUser.name}" },
                                dateTime = eventDate,
                                meetingPoint = eventMeet,
                                capacity = eventCap,
                                communityId = communities.firstOrNull()?.id,
                                communityName = communities.firstOrNull()?.name,
                                routePlan = eventRoute
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary)
                ) {
                    Text("Publish Event", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
