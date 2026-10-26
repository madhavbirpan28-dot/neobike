package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun PassportScreen(
    viewModel: NeoViewModel
) {
    val badges by viewModel.passportBadges.collectAsState()
    val completedRides by viewModel.completedRides.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val totalKm = completedRides.sumOf { it.distanceKm } + 850 // verified lifetime touring km

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("passport_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Passport Hero Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeoAmberAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎖️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RIDE PASSPORT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentUser.name} • Certified Rider",
                                    fontSize = 12.sp,
                                    color = NeoAmberAccent
                                )
                            }
                        }
                        StatusBadge("VERIFIED", NeoSuccess.copy(alpha = 0.2f), NeoSuccess)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox("Touring Log", "$totalKm km", NeoTealLight)
                        StatBox("Completed Tours", "${completedRides.size + 3}", NeoAmberAccent)
                        StatBox("Badges Earned", "${badges.count { it.isUnlocked }} / ${badges.size}", Color.White)
                    }
                }
            }
        }

        // Achievements Badges
        item {
            SectionHeader(
                title = "Touring Achievements & Badges",
                subtitle = "Earned through verified group rides and community contributions"
            )
        }

        items(badges) { badge ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = if (badge.isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, NeoTealPrimary.copy(alpha = 0.4f)) else null,
                modifier = Modifier.fillMaxWidth()
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
                            .clip(CircleShape)
                            .background(if (badge.isUnlocked) NeoTealDark else MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(badge.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = badge.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (badge.isUnlocked) {
                                StatusBadge("UNLOCKED", NeoSuccess.copy(alpha = 0.2f), NeoSuccess)
                            } else {
                                StatusBadge("IN PROGRESS", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = badge.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (badge.unlockedAt != null) {
                            Text(
                                text = "Unlocked on ${badge.unlockedAt}",
                                fontSize = 10.sp,
                                color = NeoTealLight
                            )
                        }
                    }
                }
            }
        }

        // Completed Rides Memories
        item {
            SectionHeader(
                title = "Tour Logbook & Memories",
                subtitle = "Past group touring milestones"
            )
        }

        items(completedRides) { record ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(record.rideTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(record.date, fontSize = 11.sp, color = NeoAmberAccent)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🛣️ Route: ${record.route}", fontSize = 12.sp, color = NeoTealLight)
                    Text("📊 ${record.distanceKm} km • ${record.coRidersCount} Co-Riders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${record.memoriesNote}\"",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
