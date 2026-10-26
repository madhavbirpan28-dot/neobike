package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.MaintenanceRecord
import com.example.data.model.MaintenanceReminder
import com.example.data.model.Motorcycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun GarageScreen(
    viewModel: NeoViewModel
) {
    val motorcycles by viewModel.motorcycles.collectAsState()
    val maintenanceRecords by viewModel.maintenanceRecords.collectAsState()
    val maintenanceReminders by viewModel.maintenanceReminders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedBikeId by remember(motorcycles) {
        mutableStateOf(motorcycles.firstOrNull { it.ownerId == currentUser.id }?.id ?: motorcycles.firstOrNull()?.id ?: "")
    }

    var showAddBikeDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showUpdateKmDialog by remember { mutableStateOf(false) }

    val activeBike = motorcycles.find { it.id == selectedBikeId } ?: motorcycles.firstOrNull()
    val bikeRecords = maintenanceRecords.filter { it.bikeId == activeBike?.id }
    val bikeReminders = maintenanceReminders.filter { it.bikeId == activeBike?.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("garage_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Garage",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Track mileage, service records & maintenance reminders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddBikeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoOrangePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_bike_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bike", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Motorcycle Selector Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                motorcycles.forEach { bike ->
                    val isSelected = bike.id == activeBike?.id
                    Card(
                        onClick = { selectedBikeId = bike.id },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NeoOrangePrimary.copy(alpha = 0.15f) else NeoDarkSurfaceCard
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeoOrangePrimary else NeoDarkBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bike_card_${bike.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(bike.photoEmoji, fontSize = 24.sp)
                                if (bike.isPrimary) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeoAmberAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "PRIMARY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeoAmberAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${bike.make} ${bike.model}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${bike.displacementCc} cc • ${bike.odometerKm} km",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Active Bike Telemetry & Stats Card
        if (activeBike != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                    border = BorderStroke(1.dp, NeoDarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_bike_details_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(activeBike.photoEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${activeBike.make} ${activeBike.model} (${activeBike.year})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Reg: ${activeBike.licensePlate.ifBlank { "N/A" }}",
                                        fontSize = 12.sp,
                                        color = NeoTealLight
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showUpdateKmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, NeoOrangePrimary),
                                modifier = Modifier.testTag("update_km_button")
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = NeoOrangePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Update Odometer", fontSize = 11.sp, color = NeoOrangePrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BikeStatItem(title = "Odometer", value = "${activeBike.odometerKm} km", emoji = "🛣️")
                            BikeStatItem(title = "Last Serviced", value = activeBike.lastServiceDate, emoji = "🔧")
                            BikeStatItem(title = "Next Service", value = "${activeBike.nextServiceDueKm} km", emoji = "⏱️")
                            BikeStatItem(title = "Insurance", value = activeBike.insuranceExpiryDate, emoji = "📄")
                        }

                        if (activeBike.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "💡 Note: ${activeBike.notes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Maintenance Reminders
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Maintenance Reminders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (bikeReminders.isEmpty()) {
                item {
                    Text(
                        "No pending reminders for this motorcycle.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(bikeReminders) { rem ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                        border = BorderStroke(1.dp, if (rem.priority == "High") NeoError.copy(alpha = 0.5f) else NeoDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rem.isCompleted,
                                    onCheckedChange = { viewModel.toggleReminderCompleted(rem.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = NeoTealPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = rem.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Due: ${rem.dueKm} km • ${rem.dueDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (rem.priority) {
                                    "High" -> NeoError.copy(alpha = 0.2f)
                                    "Medium" -> NeoAmberAccent.copy(alpha = 0.2f)
                                    else -> NeoTealPrimary.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = rem.priority.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (rem.priority) {
                                        "High" -> NeoError
                                        "Medium" -> NeoAmberAccent
                                        else -> NeoTealPrimary
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Service History Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Service History Log",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { showAddServiceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("log_service_button")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Service", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (bikeRecords.isEmpty()) {
                item {
                    Text(
                        "No service records logged yet.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(bikeRecords) { record ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                        border = BorderStroke(1.dp, NeoDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = record.serviceType,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${record.costAmount.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NeoAmberAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📅 ${record.date} • 🛣️ ${record.odometerKm} km • 🏭 ${record.workshopName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (record.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = record.notes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Bike Dialog
    if (showAddBikeDialog) {
        AddBikeDialog(
            onDismiss = { showAddBikeDialog = false },
            onConfirm = { make, model, year, displacement, km, plate, emoji, ins, notes ->
                viewModel.addMotorcycle(make, model, year, displacement, km, plate, emoji, ins, notes)
                showAddBikeDialog = false
            }
        )
    }

    // Update Odometer Dialog
    if (showUpdateKmDialog && activeBike != null) {
        UpdateOdometerDialog(
            currentKm = activeBike.odometerKm,
            onDismiss = { showUpdateKmDialog = false },
            onConfirm = { newKm ->
                viewModel.updateOdometer(activeBike.id, newKm)
                showUpdateKmDialog = false
            }
        )
    }

    // Add Service Record Dialog
    if (showAddServiceDialog && activeBike != null) {
        AddServiceRecordDialog(
            bikeId = activeBike.id,
            onDismiss = { showAddServiceDialog = false },
            onConfirm = { serviceType, date, km, cost, workshop, notes ->
                viewModel.addMaintenanceRecord(activeBike.id, serviceType, date, km, cost, workshop, notes)
                showAddServiceDialog = false
            }
        )
    }
}

@Composable
private fun BikeStatItem(title: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddBikeDialog(
    onDismiss: () -> Unit,
    onConfirm: (make: String, model: String, year: Int, displacement: Int, km: Int, plate: String, emoji: String, ins: String, notes: String) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("2024") }
    var ccStr by remember { mutableStateOf("400") }
    var kmStr by remember { mutableStateOf("5000") }
    var plate by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🏍️") }
    var insurance by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Motorcycle to Garage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = { Text("Make (e.g. Royal Enfield, KTM)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_bike_make_field")
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model (e.g. Himalayan 450, Duke 390)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_bike_model_field")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ccStr,
                        onValueChange = { ccStr = it },
                        label = { Text("Capacity (cc)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = kmStr,
                    onValueChange = { kmStr = it },
                    label = { Text("Odometer Reading (km)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("License Plate (e.g., MH 12 AB 1234)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (make.isNotBlank() && model.isNotBlank()) {
                        onConfirm(
                            make, model,
                            yearStr.toIntOrNull() ?: 2024,
                            ccStr.toIntOrNull() ?: 400,
                            kmStr.toIntOrNull() ?: 0,
                            plate, emoji, insurance, notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeoOrangePrimary),
                modifier = Modifier.testTag("save_bike_confirm_button")
            ) {
                Text("Save Bike")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun UpdateOdometerDialog(
    currentKm: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var kmStr by remember { mutableStateOf(currentKm.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Odometer Reading") },
        text = {
            OutlinedTextField(
                value = kmStr,
                onValueChange = { kmStr = it },
                label = { Text("Current Odometer (km)") },
                modifier = Modifier.fillMaxWidth().testTag("odometer_input_field")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmStr.toIntOrNull()
                    if (km != null) onConfirm(km)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeoOrangePrimary)
            ) {
                Text("Update")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddServiceRecordDialog(
    bikeId: String,
    onDismiss: () -> Unit,
    onConfirm: (serviceType: String, date: String, odometerKm: Int, cost: Double, workshop: String, notes: String) -> Unit
) {
    var serviceType by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Oct 26, 2026") }
    var kmStr by remember { mutableStateOf("8500") }
    var costStr by remember { mutableStateOf("2500") }
    var workshop by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Maintenance Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Description (e.g. Oil Change)") },
                    modifier = Modifier.fillMaxWidth().testTag("service_desc_field")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kmStr,
                        onValueChange = { kmStr = it },
                        label = { Text("Odometer (km)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Cost (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = workshop,
                    onValueChange = { workshop = it },
                    label = { Text("Workshop / Service Centre Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Service Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (serviceType.isNotBlank()) {
                        onConfirm(
                            serviceType, date,
                            kmStr.toIntOrNull() ?: 0,
                            costStr.toDoubleOrNull() ?: 0.0,
                            workshop, notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeoTealPrimary)
            ) {
                Text("Log Service")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
