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
import com.example.data.model.*
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: NeoViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currentUser by viewModel.currentUser.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val rides by viewModel.rides.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    // Enforce role-based access control
    if (!currentUser.isAdmin) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Access Denied", fontWeight = FontWeight.Bold, color = NeoError, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("You must be an administrator to access this console.", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = onBack) { Text("Return to App") }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Moderation Console",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge("RBAC ENFORCED", NeoAmberAccent.copy(alpha = 0.2f), NeoAmberAccent)
                    }
                    Text(
                        text = "Review incident reports, maintain platform safety standards, and moderate content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Platform Activity Metrics
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Platform Activity Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AdminMetricItem("Total Rides", "${rides.size}", NeoTealLight)
                        AdminMetricItem("Communities", "${communities.size}", NeoAmberAccent)
                        AdminMetricItem("Active Users", "${users.size}", Color.White)
                        AdminMetricItem("Open Reports", "${reports.count { it.status == ReportStatus.PENDING }}", NeoError)
                    }
                }
            }
        }

        // Moderation Report Queue
        item {
            SectionHeader(
                title = "Incident & Report Queue (${reports.size})",
                subtitle = "User-reported issues requiring administrative action"
            )
        }

        items(reports) { report ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (report.status == ReportStatus.PENDING) NeoError.copy(alpha = 0.5f) else NeoDarkBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("report_item_${report.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Case: ${report.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoAmberAccent
                        )
                        StatusBadge(
                            text = report.status.name.replace("_", " "),
                            containerColor = when (report.status) {
                                ReportStatus.PENDING -> NeoError.copy(alpha = 0.2f)
                                ReportStatus.DISMISSED -> MaterialTheme.colorScheme.surface
                                else -> NeoSuccess.copy(alpha = 0.2f)
                            },
                            contentColor = when (report.status) {
                                ReportStatus.PENDING -> NeoError
                                ReportStatus.DISMISSED -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> NeoSuccess
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${report.targetType.name}: ${report.targetTitle}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reason: ${report.reason}",
                        fontSize = 12.sp,
                        color = NeoError,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Details: \"${report.details}\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Reported at ${report.createdAt}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (report.adminNotes != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin Note: ${report.adminNotes}",
                            fontSize = 11.sp,
                            color = NeoTealLight
                        )
                    }

                    if (report.status == ReportStatus.PENDING) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resolveReport(report.id, ReportStatus.DISMISSED, "Reviewed and dismissed as non-violating.")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dismiss", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel.resolveReport(report.id, ReportStatus.RESOLVED_WARNING_ISSUED, "Official safety warning issued to author.")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeoAmberDark),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Warn User", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    viewModel.resolveReport(report.id, ReportStatus.RESOLVED_CONTENT_REMOVED, "Content removed for safety violation.")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeoError),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdminMetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
