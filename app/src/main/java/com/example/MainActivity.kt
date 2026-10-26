package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.*
import com.example.ui.navigation.NeoNavGraph
import com.example.ui.navigation.NeoRoutes
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
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()
    val showPersonaSwitcher by viewModel.showPersonaSwitcher.collectAsState()
    val reportTarget by viewModel.reportTarget.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadNotifs = notifications.count { !it.isRead }

    // Synchronize bottom nav with current tab state
    val isPrimaryTabRoute = currentRoute in listOf(
        NeoRoutes.HOME,
        NeoRoutes.FIND_RIDE,
        NeoRoutes.CREATE_RIDE,
        NeoRoutes.COMMUNITIES,
        NeoRoutes.EVENTS,
        NeoRoutes.FEED,
        NeoRoutes.GARAGE,
        NeoRoutes.INBOX,
        NeoRoutes.SPOTS,
        NeoRoutes.PASSPORT,
        NeoRoutes.PROFILE
    ) || currentRoute == null

    Scaffold(
        topBar = {
            NeoTopBar(
                currentUser = currentUser,
                unreadNotifCount = unreadNotifs,
                onOpenNotifications = { viewModel.toggleNotifications(true) },
                onOpenPersonaSwitcher = { viewModel.togglePersonaSwitcher(true) },
                onOpenSafetyCenter = {
                    viewModel.openSafetyCenter()
                    navController.navigate(NeoRoutes.SAFETY_CENTER)
                },
                onOpenAdminDashboard = {
                    viewModel.openAdminDashboard()
                    navController.navigate(NeoRoutes.ADMIN_DASHBOARD)
                },
                onResetData = { viewModel.resetDemoData() }
            )
        },
        bottomBar = {
            if (isPrimaryTabRoute) {
                NeoBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        viewModel.selectTab(tab)
                        val targetRoute = when (tab) {
                            MainTab.HOME -> NeoRoutes.HOME
                            MainTab.FIND_RIDE -> NeoRoutes.FIND_RIDE
                            MainTab.CREATE_RIDE -> NeoRoutes.CREATE_RIDE
                            MainTab.COMMUNITIES -> NeoRoutes.COMMUNITIES
                            MainTab.EVENTS -> NeoRoutes.EVENTS
                            MainTab.FEED -> NeoRoutes.FEED
                            MainTab.GARAGE -> NeoRoutes.GARAGE
                            MainTab.INBOX -> NeoRoutes.INBOX
                            MainTab.SPOTS -> NeoRoutes.SPOTS
                            MainTab.PASSPORT -> NeoRoutes.PASSPORT
                            MainTab.PROFILE -> NeoRoutes.PROFILE
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(NeoRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
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
            NeoNavGraph(
                navController = navController,
                viewModel = viewModel
            )
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
