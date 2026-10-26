package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun ProfileScreen(
    viewModel: NeoViewModel,
    onOpenSafetyCenter: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenPersonaSwitcher: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(currentUser.name) }
    var editBio by remember { mutableStateOf(currentUser.bio) }
    var editCity by remember { mutableStateOf(currentUser.city) }
    var editBike by remember { mutableStateOf(currentUser.motorcycle) }
    var editPace by remember { mutableStateOf(currentUser.pacePreference) }

    // Privacy settings
    var phoneVisible by remember { mutableStateOf(currentUser.phoneVisible) }
    var bikeVisible by remember { mutableStateOf(currentUser.bikeVisible) }
    var bioVisible by remember { mutableStateOf(currentUser.bioVisible) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(NeoTealDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(currentUser.avatarEmoji, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentUser.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (currentUser.isContactVerified) {
                                        Icon(Icons.Default.Verified, "Verified", tint = NeoTealPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text("@${currentUser.username} • ${currentUser.city}", fontSize = 12.sp, color = NeoAmberAccent)
                            }
                        }

                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = NeoTealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editCity,
                                onValueChange = { editCity = it },
                                label = { Text("Home City") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editBike,
                                onValueChange = { editBike = it },
                                label = { Text("Motorcycle Make & Model") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editBio,
                                onValueChange = { editBio = it },
                                label = { Text("Short Bio") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        currentUser.copy(
                                            name = editName,
                                            city = editCity,
                                            motorcycle = editBike,
                                            bio = editBio,
                                            phoneVisible = phoneVisible,
                                            bikeVisible = bikeVisible,
                                            bioVisible = bioVisible
                                        )
                                    )
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        if (bioVisible) {
                            Text(
                                text = currentUser.bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (bikeVisible) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏍️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(currentUser.motorcycle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${currentUser.experienceLevel} • ${currentUser.pacePreference}", fontSize = 11.sp, color = NeoTealLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interests Chips
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Riding Interests", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentUser.interests) { interest ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeoDarkSurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder)
                            ) {
                                Text(
                                    interest,
                                    fontSize = 11.sp,
                                    color = NeoAmberAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Profile Privacy Controls (Feature 23 & 26)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔒 Privacy & Visibility Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "You control what personal info is visible to other riders on the platform.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Motorcycle Details", fontSize = 13.sp)
                        Switch(
                            checked = bikeVisible,
                            onCheckedChange = { bikeVisible = it; viewModel.updateProfile(currentUser.copy(bikeVisible = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeoTealPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Rider Bio & Interests", fontSize = 13.sp)
                        Switch(
                            checked = bioVisible,
                            onCheckedChange = { bioVisible = it; viewModel.updateProfile(currentUser.copy(bioVisible = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeoTealPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Share Phone with Accepted Hosts", fontSize = 13.sp)
                            Text("Only for emergency convoy contact", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = phoneVisible,
                            onCheckedChange = { phoneVisible = it; viewModel.updateProfile(currentUser.copy(phoneVisible = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeoTealPrimary)
                        )
                    }
                }
            }
        }

        // Demo Actions (Switch User, Safety, Admin)
        item {
            SectionHeader("Account & Quick Switches")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenPersonaSwitcher,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.SwitchAccount, "Switch", tint = NeoAmberAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch Demo Persona (Vikram, Rahul, Neha, Ananya)", color = NeoAmberAccent)
                }

                OutlinedButton(
                    onClick = onOpenSafetyCenter,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, "Safety", tint = NeoTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Safety Centre & Guidelines", color = NeoTealPrimary)
                }

                if (currentUser.isAdmin) {
                    Button(
                        onClick = onOpenAdminDashboard,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeoAmberDark)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, "Admin", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Admin Moderation Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.resetDemoData() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeoError)
                ) {
                    Icon(Icons.Default.Refresh, "Reset", tint = NeoError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Demo Data to Initial State", color = NeoError)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
