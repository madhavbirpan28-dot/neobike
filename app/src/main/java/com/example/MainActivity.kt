package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.NeoTheme
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NeoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeoTheme(darkTheme = true) {
                NeoAppRoot()
            }
        }
    }
}

@Composable
fun NeoAppRoot(viewModel: NeoViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedRideId by viewModel.selectedRideId.collectAsState()
    val selectedCommunityId by viewModel.selectedCommunityId.collectAsState()
    val selectedConversationId by viewModel.selectedConversationId.collectAsState()
    val showSafetyCenter by viewModel.showSafetyCenter.collectAsState()
    val showAdminDashboard by viewModel.showAdminDashboard.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()
    val showPersonaSwitcher by viewModel.showPersonaSwitcher.collectAsState()
    val reportTarget by viewModel.reportTarget.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val unreadNotifs = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            NeoTopBar(
                currentUser = currentUser,
                unreadNotifCount = unreadNotifs,
                onOpenNotifications = { viewModel.toggleNotifications(true) },
                onOpenPersonaSwitcher = { viewModel.togglePersonaSwitcher(true) },
                onOpenSafetyCenter = { viewModel.openSafetyCenter() },
                onOpenAdminDashboard = { viewModel.openAdminDashboard() },
                onResetData = { viewModel.resetDemoData() }
            )
        },
        bottomBar = {
            // Hide bottom bar on sub-screens like chat or ride detail if desired, or keep it accessible
            if (selectedRideId == null && !showSafetyCenter && !showAdminDashboard && selectedConversationId == null) {
                NeoBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                // Secondary Full-Screen Destinations with BackHandler
                showSafetyCenter -> {
                    SafetyCenterScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSafetyCenter() }
                    )
                }
                showAdminDashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeAdminDashboard() }
                    )
                }
                selectedRideId != null -> {
                    RideDetailScreen(
                        rideId = selectedRideId!!,
                        viewModel = viewModel,
                        onBack = { viewModel.closeRideDetail() },
                        onOpenChat = { convId ->
                            viewModel.closeRideDetail()
                            viewModel.selectTab(MainTab.INBOX)
                            viewModel.openConversation(convId)
                        }
                    )
                }
                // Primary Tab Destinations
                currentTab == MainTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateTab = { viewModel.selectTab(it) },
                        onOpenRideDetail = { viewModel.openRideDetail(it) },
                        onOpenCommunityDetail = {
                            viewModel.selectTab(MainTab.COMMUNITIES)
                            viewModel.openCommunityDetail(it)
                        },
                        onOpenEventDetail = {
                            viewModel.selectTab(MainTab.EVENTS)
                            viewModel.openEventDetail(it)
                        }
                    )
                }
                currentTab == MainTab.FIND_RIDE -> {
                    FindRideScreen(
                        viewModel = viewModel,
                        onOpenRideDetail = { viewModel.openRideDetail(it) }
                    )
                }
                currentTab == MainTab.CREATE_RIDE -> {
                    CreateRideScreen(
                        viewModel = viewModel,
                        onRideCreated = { newRideId ->
                            viewModel.openRideDetail(newRideId)
                        }
                    )
                }
                currentTab == MainTab.COMMUNITIES -> {
                    CommunitiesScreen(
                        viewModel = viewModel,
                        selectedCommunityId = selectedCommunityId,
                        onOpenCommunityDetail = { viewModel.openCommunityDetail(it) },
                        onCloseCommunityDetail = { viewModel.closeCommunityDetail() },
                        onOpenEventDetail = {
                            viewModel.selectTab(MainTab.EVENTS)
                            viewModel.openEventDetail(it)
                        }
                    )
                }
                currentTab == MainTab.EVENTS -> {
                    EventsScreen(viewModel = viewModel)
                }
                currentTab == MainTab.INBOX -> {
                    InboxScreen(
                        viewModel = viewModel,
                        selectedConversationId = selectedConversationId,
                        onOpenConversation = { viewModel.openConversation(it) },
                        onCloseConversation = { viewModel.closeConversation() },
                        onOpenRideDetail = { viewModel.openRideDetail(it) }
                    )
                }
                currentTab == MainTab.SPOTS -> {
                    BikerSpotsScreen(viewModel = viewModel)
                }
                currentTab == MainTab.PASSPORT -> {
                    PassportScreen(viewModel = viewModel)
                }
                currentTab == MainTab.PROFILE -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onOpenSafetyCenter = { viewModel.openSafetyCenter() },
                        onOpenAdminDashboard = { viewModel.openAdminDashboard() },
                        onOpenPersonaSwitcher = { viewModel.togglePersonaSwitcher(true) }
                    )
                }
            }
        }
    }

    // Modal Overlays
    if (showPersonaSwitcher) {
        PersonaSwitcherDialog(
            allUsers = allUsers,
            currentUser = currentUser,
            onSelectUser = { viewModel.switchUser(it) },
            onDismiss = { viewModel.togglePersonaSwitcher(false) }
        )
    }

    if (showNotifications) {
        NotificationsModal(
            notifications = notifications,
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDismiss = { viewModel.toggleNotifications(false) }
        )
    }

    if (reportTarget != null) {
        val (targetType, targetId, targetTitle) = reportTarget!!
        ReportDialog(
            targetType = targetType,
            targetTitle = targetTitle,
            onSubmitReport = { reason, details ->
                viewModel.submitReport(
                    targetType = targetType,
                    targetId = targetId,
                    targetTitle = targetTitle,
                    reportedUserId = null,
                    reason = reason,
                    details = details
                )
            },
            onDismiss = { viewModel.closeReportDialog() }
        )
    }
}
