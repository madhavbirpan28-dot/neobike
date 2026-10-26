package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun CommunitiesScreen(
    viewModel: NeoViewModel,
    selectedCommunityId: String?,
    onOpenCommunityDetail: (String) -> Unit,
    onCloseCommunityDetail: () -> Unit,
    onOpenEventDetail: (String) -> Unit
) {
    val communities by viewModel.communities.collectAsState()
    val events by viewModel.events.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Long-Distance", "City", "Adventure", "Sunrise")

    // If detail view is open
    if (selectedCommunityId != null) {
        val community = communities.find { it.id == selectedCommunityId }
        if (community != null) {
            CommunityDetailView(
                community = community,
                allUsers = allUsers,
                currentUser = currentUser,
                communityEvents = events.filter { it.communityId == community.id },
                onBack = onCloseCommunityDetail,
                onToggleJoin = { viewModel.toggleJoinCommunity(community.id) },
                onApproveMember = { applicantId ->
                    viewModel.approveCommunityMember(community.id, applicantId)
                },
                onOpenEventDetail = onOpenEventDetail,
                onReport = {
                    viewModel.openReportDialog(ReportTargetType.COMMUNITY, community.id, community.name)
                }
            )
            return
        }
    }

    // Community Directory
    val filtered = if (selectedCategoryFilter == "All") communities
    else communities.filter { it.category.contains(selectedCategoryFilter, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("communities_screen"),
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
                        text = "Biker Communities",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Join local clubs, touring circles, and weekend brotherhoods.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoTealDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_create_community")
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Club", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
        }

        items(filtered) { comm ->
            val isMember = comm.memberIds.contains(currentUser.id)
            val isPending = comm.pendingMemberIds.contains(currentUser.id)
            val isAdmin = comm.adminIds.contains(currentUser.id)

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCommunityDetail(comm.id) }
                    .testTag("community_card_${comm.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(comm.coverEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = comm.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${comm.category} • ${comm.location}",
                                    fontSize = 12.sp,
                                    color = NeoTealLight
                                )
                            }
                        }
                        if (isAdmin) {
                            StatusBadge("ADMIN", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                        } else if (isMember) {
                            StatusBadge("MEMBER", NeoSuccess.copy(alpha = 0.2f), NeoSuccess)
                        } else if (isPending) {
                            StatusBadge("PENDING", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = comm.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${comm.memberIds.size} Riders • ${comm.membershipType.name}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { viewModel.toggleJoinCommunity(comm.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("toggle_join_comm_${comm.id}")
                        ) {
                            Text(
                                text = when {
                                    isMember -> "Joined ✓"
                                    isPending -> "Pending Review"
                                    comm.membershipType == MembershipType.OPEN -> "Join Club"
                                    else -> "Request Membership"
                                },
                                fontSize = 11.sp,
                                color = if (isMember) NeoSuccess else NeoTealPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Community Dialog
    if (showCreateDialog) {
        var commName by remember { mutableStateOf("") }
        var commDesc by remember { mutableStateOf("") }
        var commLoc by remember { mutableStateOf(currentUser.city) }
        var commCat by remember { mutableStateOf("Weekend Riders") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Biker Community", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = commName,
                        onValueChange = { commName = it },
                        label = { Text("Community Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = commCat,
                        onValueChange = { commCat = it },
                        label = { Text("Category (e.g. Adventure, Touring)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = commLoc,
                        onValueChange = { commLoc = it },
                        label = { Text("Region / City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = commDesc,
                        onValueChange = { commDesc = it },
                        label = { Text("Description & Spirit") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (commName.isNotBlank()) {
                            viewModel.createCommunity(
                                name = commName,
                                category = commCat,
                                description = commDesc.ifBlank { "Passionate motorcyclists sharing the open road." },
                                location = commLoc,
                                rules = listOf("Safety gear mandatory on rides", "Respect all fellow motorcyclists"),
                                membershipType = MembershipType.OPEN
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary)
                ) {
                    Text("Create Community", color = Color.Black, fontWeight = FontWeight.Bold)
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

@Composable
fun CommunityDetailView(
    community: Community,
    allUsers: List<User>,
    currentUser: User,
    communityEvents: List<Event>,
    onBack: () -> Unit,
    onToggleJoin: () -> Unit,
    onApproveMember: (String) -> Unit,
    onOpenEventDetail: (String) -> Unit,
    onReport: () -> Unit
) {
    BackHandler { onBack() }

    val isMember = community.memberIds.contains(currentUser.id)
    val isPending = community.pendingMemberIds.contains(currentUser.id)
    val isAdmin = community.adminIds.contains(currentUser.id)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("community_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back Navigation & Report Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onReport) {
                    Icon(Icons.Default.Report, "Report", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Header Card
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
                        Text(community.coverEmoji, fontSize = 36.sp)
                        StatusBadge(
                            text = "${community.memberIds.size} Members",
                            containerColor = NeoTealDark.copy(alpha = 0.4f),
                            contentColor = NeoTealLight
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${community.category} • ${community.location}",
                        fontSize = 13.sp,
                        color = NeoAmberAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onToggleJoin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMember) NeoSuccess else NeoTealPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when {
                                isMember -> "Member of Club ✓ (Leave)"
                                isPending -> "Application Pending"
                                else -> "Join Community"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Community Admin Requests Approval Queue (Feature 10: Admin Permissions)
        if (isAdmin && community.pendingMemberIds.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeoAmberAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Pending Membership Requests (${community.pendingMemberIds.size})",
                            fontWeight = FontWeight.Bold,
                            color = NeoAmberAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        community.pendingMemberIds.forEach { applicantId ->
                            val user = allUsers.find { it.id == applicantId }
                            if (user != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.avatarEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${user.city} • ${user.motorcycle}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Button(
                                        onClick = { onApproveMember(user.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeoSuccess),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Approve", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Announcements Feed
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📢 Club Announcements", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    community.announcements.forEach { ann ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeoDarkSurfaceCard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = ann,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Community Rules
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🛡️ Community Guidelines & Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    community.rules.forEach { rule ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text("•", color = NeoTealPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(rule, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Upcoming Community Events
        if (communityEvents.isNotEmpty()) {
            item {
                SectionHeader("Upcoming Club Events")
            }
            items(communityEvents) { event ->
                EventMiniCard(
                    event = event,
                    currentUserId = currentUser.id,
                    onOpenDetail = { onOpenEventDetail(event.id) },
                    onRsvpGoing = { }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
