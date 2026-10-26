package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun HomeScreen(
    viewModel: NeoViewModel,
    onNavigateTab: (MainTab) -> Unit,
    onOpenRideDetail: (String) -> Unit,
    onOpenCommunityDetail: (String) -> Unit,
    onOpenEventDetail: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allRides by viewModel.rides.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val events by viewModel.events.collectAsState()

    var quickOrigin by remember { mutableStateOf("") }
    var quickDestination by remember { mutableStateOf("") }

    val activeRides = allRides.filter { it.status == RideStatus.ACTIVE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Welcome section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(NeoTealDark.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentUser.avatarEmoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Welcome back, ${currentUser.name}!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentUser.motorcycle} • ${currentUser.city}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeoTealLight,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pace: ${currentUser.pacePreference}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // B. Main Ride Search Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoTealPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_ride_search_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Route, "Route", tint = NeoTealPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SMART ROUTE MATCH",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NeoTealPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Find riders travelling in your direction or sharing highway corridors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickOrigin,
                            onValueChange = { quickOrigin = it },
                            placeholder = { Text("Starting (e.g. Pune)") },
                            leadingIcon = { Icon(Icons.Default.MyLocation, "From", tint = NeoAmberAccent, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_origin_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = quickDestination,
                            onValueChange = { quickDestination = it },
                            placeholder = { Text("Destination (e.g. Goa)") },
                            leadingIcon = { Icon(Icons.Default.Place, "To", tint = NeoTealPrimary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_dest_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.searchOrigin.value = quickOrigin
                            viewModel.searchDestination.value = quickDestination
                            onNavigateTab(MainTab.FIND_RIDE)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("find_ride_partner_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary)
                    ) {
                        Icon(Icons.Default.Search, "Search", tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Find My Ride Partner",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // C. Quick Actions
        item {
            Column {
                SectionHeader("Quick Actions")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        emoji = "🔍",
                        title = "Find Ride",
                        modifier = Modifier.weight(1f),
                        testTag = "quick_find_ride",
                        onClick = { onNavigateTab(MainTab.FIND_RIDE) }
                    )
                    QuickActionCard(
                        emoji = "➕",
                        title = "Create Ride",
                        modifier = Modifier.weight(1f),
                        testTag = "quick_create_ride",
                        onClick = { onNavigateTab(MainTab.CREATE_RIDE) }
                    )
                    QuickActionCard(
                        emoji = "👥",
                        title = "Clubs",
                        modifier = Modifier.weight(1f),
                        testTag = "quick_clubs",
                        onClick = { onNavigateTab(MainTab.COMMUNITIES) }
                    )
                    QuickActionCard(
                        emoji = "📅",
                        title = "Events",
                        modifier = Modifier.weight(1f),
                        testTag = "quick_events",
                        onClick = { onNavigateTab(MainTab.EVENTS) }
                    )
                }
            }
        }

        // D. Recommended Rides
        item {
            SectionHeader(
                title = "Recommended Active Rides",
                subtitle = "Touring plans seeking co-riders",
                actionText = "View All",
                onActionClick = { onNavigateTab(MainTab.FIND_RIDE) }
            )
        }

        items(activeRides.take(3)) { ride ->
            RideSummaryCard(
                ride = ride,
                currentUserId = currentUser.id,
                onOpenDetail = { onOpenRideDetail(ride.id) },
                onRequestJoin = {
                    viewModel.requestToJoinRide(ride.id, "Hey! Would love to ride together!")
                }
            )
        }

        // E. Nearby Communities
        item {
            SectionHeader(
                title = "Biker Communities & Circles",
                subtitle = "Ride with local clubs and enthusiast groups",
                actionText = "Explore All",
                onActionClick = { onNavigateTab(MainTab.COMMUNITIES) }
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(communities) { comm ->
                    CommunityMiniCard(
                        community = comm,
                        isMember = comm.memberIds.contains(currentUser.id),
                        onClick = { onOpenCommunityDetail(comm.id) },
                        onToggleJoin = { viewModel.toggleJoinCommunity(comm.id) }
                    )
                }
            }
        }

        // F. Upcoming Events
        item {
            SectionHeader(
                title = "Upcoming Group Runs & Meets",
                subtitle = "RSVP and join fellow riders",
                actionText = "Calendar",
                onActionClick = { onNavigateTab(MainTab.EVENTS) }
            )
        }

        items(events.take(2)) { event ->
            EventMiniCard(
                event = event,
                currentUserId = currentUser.id,
                onOpenDetail = { onOpenEventDetail(event.id) },
                onRsvpGoing = { viewModel.rsvpEvent(event.id, true) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickActionCard(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun RideSummaryCard(
    ride: Ride,
    currentUserId: String,
    onOpenDetail: () -> Unit,
    onRequestJoin: () -> Unit
) {
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
            .testTag("ride_card_${ride.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Pace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ride.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ride.tripType} • Pace: ${ride.pace}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeoTealLight
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = "$availableSlots slots left",
                    containerColor = if (availableSlots > 0) NeoTealDark.copy(alpha = 0.4f) else NeoError.copy(alpha = 0.3f),
                    contentColor = if (availableSlots > 0) NeoTealLight else NeoError
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Origin -> Destination row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, "Origin", tint = NeoAmberAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(ride.origin, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Icon(
                    Icons.Default.ArrowForward,
                    "to",
                    tint = NeoTealPrimary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(18.dp)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Flag, "Dest", tint = NeoTealPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(ride.destination, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, "Date", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ride.departureDate} @ ${ride.departureTime}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${ride.distanceKm} km • ~${ride.durationHours} hrs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeoAmberAccent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Organizer & Join CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ride.organizerAvatar, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lead: ${ride.organizerName.split(" ").first()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                when {
                    isOrganizer -> {
                        StatusBadge("You are Host", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                    }
                    isConfirmed -> {
                        StatusBadge("Confirmed Rider", NeoSuccess.copy(alpha = 0.2f), NeoSuccess)
                    }
                    isPending -> {
                        StatusBadge("Request Pending", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                    }
                    availableSlots > 0 -> {
                        Button(
                            onClick = onRequestJoin,
                            colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("request_join_btn_${ride.id}")
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

@Composable
fun CommunityMiniCard(
    community: Community,
    isMember: Boolean,
    onClick: () -> Unit,
    onToggleJoin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("community_mini_${community.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(community.coverEmoji, fontSize = 24.sp)
                StatusBadge(
                    text = "${community.memberIds.size} riders",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = community.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = community.category,
                fontSize = 11.sp,
                color = NeoTealLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onToggleJoin,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(
                    text = if (isMember) "Joined ✓" else "+ Join Club",
                    fontSize = 11.sp,
                    color = if (isMember) NeoSuccess else NeoTealPrimary
                )
            }
        }
    }
}

@Composable
fun EventMiniCard(
    event: Event,
    currentUserId: String,
    onOpenDetail: () -> Unit,
    onRsvpGoing: () -> Unit
) {
    val isGoing = event.goingUserIds.contains(currentUserId)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
            .testTag("event_mini_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeoAmberAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏁", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.dateTime} • ${event.meetingPoint}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.goingUserIds.size} Going",
                    fontSize = 11.sp,
                    color = NeoTealLight,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRsvpGoing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGoing) NeoSuccess else NeoTealPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (isGoing) "Going ✓" else "RSVP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
