package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NeoViewModel

object NeoRoutes {
    const val HOME = "home"
    const val FIND_RIDE = "find_ride"
    const val CREATE_RIDE = "create_ride"
    const val COMMUNITIES = "communities"
    const val EVENTS = "events"
    const val FEED = "feed"
    const val GARAGE = "garage"
    const val INBOX = "inbox"
    const val SPOTS = "spots"
    const val PASSPORT = "passport"
    const val PROFILE = "profile"
    const val SAFETY_CENTER = "safety_center"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val RIDE_DETAIL = "ride_detail/{rideId}"

    fun createRideDetailRoute(rideId: String) = "ride_detail/$rideId"
}

@Composable
fun NeoNavGraph(
    navController: NavHostController,
    viewModel: NeoViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCommunityId by viewModel.selectedCommunityId.collectAsState()
    val selectedConversationId by viewModel.selectedConversationId.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NeoRoutes.HOME,
        modifier = modifier
    ) {
        composable(NeoRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateTab = { tab ->
                    viewModel.selectTab(tab)
                    val route = when (tab) {
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
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenRideDetail = { rideId ->
                    viewModel.openRideDetail(rideId)
                    navController.navigate(NeoRoutes.createRideDetailRoute(rideId))
                },
                onOpenCommunityDetail = { communityId ->
                    viewModel.selectTab(MainTab.COMMUNITIES)
                    viewModel.openCommunityDetail(communityId)
                    navController.navigate(NeoRoutes.COMMUNITIES)
                },
                onOpenEventDetail = { eventId ->
                    viewModel.selectTab(MainTab.EVENTS)
                    viewModel.openEventDetail(eventId)
                    navController.navigate(NeoRoutes.EVENTS)
                }
            )
        }

        composable(NeoRoutes.FEED) {
            CommunityFeedScreen(viewModel = viewModel)
        }

        composable(NeoRoutes.GARAGE) {
            GarageScreen(viewModel = viewModel)
        }


        composable(NeoRoutes.FIND_RIDE) {
            FindRideScreen(
                viewModel = viewModel,
                onOpenRideDetail = { rideId ->
                    viewModel.openRideDetail(rideId)
                    navController.navigate(NeoRoutes.createRideDetailRoute(rideId))
                }
            )
        }

        composable(NeoRoutes.CREATE_RIDE) {
            CreateRideScreen(
                viewModel = viewModel,
                onRideCreated = { rideId ->
                    viewModel.openRideDetail(rideId)
                    navController.navigate(NeoRoutes.createRideDetailRoute(rideId))
                }
            )
        }

        composable(NeoRoutes.COMMUNITIES) {
            CommunitiesScreen(
                viewModel = viewModel,
                selectedCommunityId = selectedCommunityId,
                onOpenCommunityDetail = { viewModel.openCommunityDetail(it) },
                onCloseCommunityDetail = { viewModel.closeCommunityDetail() },
                onOpenEventDetail = { eventId ->
                    viewModel.selectTab(MainTab.EVENTS)
                    viewModel.openEventDetail(eventId)
                    navController.navigate(NeoRoutes.EVENTS)
                }
            )
        }

        composable(NeoRoutes.EVENTS) {
            EventsScreen(viewModel = viewModel)
        }

        composable(NeoRoutes.INBOX) {
            InboxScreen(
                viewModel = viewModel,
                selectedConversationId = selectedConversationId,
                onOpenConversation = { viewModel.openConversation(it) },
                onCloseConversation = { viewModel.closeConversation() },
                onOpenRideDetail = { rideId ->
                    viewModel.openRideDetail(rideId)
                    navController.navigate(NeoRoutes.createRideDetailRoute(rideId))
                }
            )
        }

        composable(NeoRoutes.SPOTS) {
            BikerSpotsScreen(viewModel = viewModel)
        }

        composable(NeoRoutes.PASSPORT) {
            PassportScreen(viewModel = viewModel)
        }

        composable(NeoRoutes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onOpenSafetyCenter = {
                    viewModel.openSafetyCenter()
                    navController.navigate(NeoRoutes.SAFETY_CENTER)
                },
                onOpenAdminDashboard = {
                    viewModel.openAdminDashboard()
                    navController.navigate(NeoRoutes.ADMIN_DASHBOARD)
                },
                onOpenPersonaSwitcher = { viewModel.togglePersonaSwitcher(true) }
            )
        }

        composable(NeoRoutes.SAFETY_CENTER) {
            SafetyCenterScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.closeSafetyCenter()
                    navController.popBackStack()
                }
            )
        }

        composable(NeoRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.closeAdminDashboard()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NeoRoutes.RIDE_DETAIL,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
            RideDetailScreen(
                rideId = rideId,
                viewModel = viewModel,
                onBack = {
                    viewModel.closeRideDetail()
                    navController.popBackStack()
                },
                onOpenChat = { convId ->
                    viewModel.closeRideDetail()
                    viewModel.selectTab(MainTab.INBOX)
                    viewModel.openConversation(convId)
                    navController.navigate(NeoRoutes.INBOX) {
                        popUpTo(NeoRoutes.HOME)
                    }
                }
            )
        }
    }
}
