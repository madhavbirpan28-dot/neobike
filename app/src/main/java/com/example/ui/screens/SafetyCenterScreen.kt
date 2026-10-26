package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun SafetyCenterScreen(
    viewModel: NeoViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val blockedUsers by viewModel.blockedUserIds.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("safety_center_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Safety Centre",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Trust, road discipline, and rider protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoTealLight
                    )
                }
            }
        }

        // Safety Core Principles
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoTealPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, "Shield", tint = NeoTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NEO Community Safety Principles", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SafetyRuleItem("Strict ATGATT Policy", "Every verified rider must wear an approved ECE/DOT helmet, armored jacket, riding gloves, and ankle-protective boots.")
                    SafetyRuleItem("Staggered Formation", "On dual-carriageway highways, maintain a two-second staggered following distance. Never tail-gate or perform unsafe slipstream passes.")
                    SafetyRuleItem("Public Meeting Points Only", "Always meet at well-lit, public highway service plazas or reputable fuel stations. Never share residential addresses.")
                    SafetyRuleItem("Zero Stunt or Speed Tolerance", "NEO strictly prohibits dangerous high-speed challenges, wheelies on open roads, or rash overtaking.")
                }
            }
        }

        // Blocked Contacts Management (Feature 20 & 28)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Blocked Users (${blockedUsers.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Blocked users cannot send you messages, join requests, or view your private profile details.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (blockedUsers.isEmpty()) {
                        Text("No users currently blocked.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        blockedUsers.forEach { bId ->
                            val user = allUsers.find { it.id == bId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user?.name ?: "User ID: $bId", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                TextButton(onClick = { viewModel.unblockUser(bId) }) {
                                    Text("Unblock", color = NeoTealPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Contact Support / Urgent Review
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Need Moderation Review?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "If you experience harassment or encounter suspicious listings, report the listing or contact the moderation council.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Emergency Highway Assistance: 1033 (National Highways)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeoAmberAccent)
                }
            }
        }
    }
}

@Composable
fun SafetyRuleItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✓", color = NeoSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp))
    }
}
