package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.NeoFirestoreManager
import com.example.data.model.*
import com.example.data.repository.NeoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab(val label: String, val iconEmoji: String) {
    HOME("Home", "🏠"),
    FIND_RIDE("Find Ride", "🔍"),
    CREATE_RIDE("Post Ride", "➕"),
    COMMUNITIES("Clubs", "👥"),
    EVENTS("Events", "📅"),
    INBOX("Inbox", "💬"),
    SPOTS("Spots", "⛽"),
    PASSPORT("Passport", "🎖️"),
    PROFILE("Profile", "👤")
}

data class RouteMatchResult(
    val ride: Ride,
    val compatibilityScore: Int,
    val matchReasons: List<String>,
    val sharedCorridor: String?,
    val meetInMiddleSuggestion: MeetInMiddleSuggestion?
)

class NeoViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: NeoRepository = NeoRepository(
        firestoreManager = try {
            NeoFirestoreManager.create(application)
        } catch (e: Exception) {
            null
        }
    )
) : AndroidViewModel(application) {

    // Repository Flows
    val currentUser = repository.currentUser
    val allUsers = repository.allUsers
    val rides = repository.rides
    val requests = repository.requests
    val communities = repository.communities
    val events = repository.events
    val conversations = repository.conversations
    val messages = repository.messages
    val bikerSpots = repository.bikerSpots
    val passportBadges = repository.passportBadges
    val completedRides = repository.completedRides
    val tripCheckIns = repository.tripCheckIns
    val blockedUserIds = repository.blockedUserIds
    val reports = repository.reports
    val notifications = repository.notifications

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedRideId = MutableStateFlow<String?>(null)
    val selectedRideId: StateFlow<String?> = _selectedRideId.asStateFlow()

    private val _selectedCommunityId = MutableStateFlow<String?>(null)
    val selectedCommunityId: StateFlow<String?> = _selectedCommunityId.asStateFlow()

    private val _selectedEventId = MutableStateFlow<String?>(null)
    val selectedEventId: StateFlow<String?> = _selectedEventId.asStateFlow()

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _showSafetyCenter = MutableStateFlow(false)
    val showSafetyCenter: StateFlow<Boolean> = _showSafetyCenter.asStateFlow()

    private val _showAdminDashboard = MutableStateFlow(false)
    val showAdminDashboard: StateFlow<Boolean> = _showAdminDashboard.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _showPersonaSwitcher = MutableStateFlow(false)
    val showPersonaSwitcher: StateFlow<Boolean> = _showPersonaSwitcher.asStateFlow()

    // Reporting modal state
    private val _reportTarget = MutableStateFlow<Triple<ReportTargetType, String, String>?>(null)
    val reportTarget: StateFlow<Triple<ReportTargetType, String, String>?> = _reportTarget.asStateFlow()

    // Search & Filter State
    val searchOrigin = MutableStateFlow("")
    val searchDestination = MutableStateFlow("")
    val searchDate = MutableStateFlow("")
    val flexibleDates = MutableStateFlow(false)
    val filterTripType = MutableStateFlow("All")
    val filterPace = MutableStateFlow("All")
    val filterExperience = MutableStateFlow("All")

    private data class SearchFilters(
        val origin: String,
        val dest: String,
        val date: String,
        val tripType: String,
        val pace: String
    )

    private val filtersFlow = combine(
        searchOrigin,
        searchDestination,
        searchDate,
        filterTripType,
        filterPace
    ) { o, d, dt, t, p ->
        SearchFilters(o, d, dt, t, p)
    }

    // Filtered / Matched Rides Stream
    val searchResults: StateFlow<List<RouteMatchResult>> = combine(
        rides,
        filtersFlow,
        currentUser
    ) { allRides, filters, user ->
        val activeRides = allRides.filter { it.status == RideStatus.ACTIVE }

        activeRides.mapNotNull { ride ->
            var score = 0
            val reasons = mutableListOf<String>()
            var sharedCorridor: String? = null
            var meetSuggestion: MeetInMiddleSuggestion? = null

            val originLower = filters.origin.trim().lowercase()
            val destLower = filters.dest.trim().lowercase()
            val rideOriginLower = ride.origin.lowercase()
            val rideDestLower = ride.destination.lowercase()

            // Destination match
            if (destLower.isNotEmpty()) {
                if (rideDestLower.contains(destLower)) {
                    score += 45
                    reasons.add("Exact destination match: ${ride.destination}")
                } else if (ride.plannedStops.any { it.lowercase().contains(destLower) }) {
                    score += 35
                    reasons.add("Passes through your destination: ${ride.destination}")
                } else {
                    return@mapNotNull null // Strict destination filter when provided
                }
            } else {
                score += 20 // baseline
            }

            // Origin match or Overlap Corridor (Nashik <-> Pune heading to Goa)
            if (originLower.isNotEmpty()) {
                if (rideOriginLower.contains(originLower)) {
                    score += 30
                    reasons.add("Same starting point: ${ride.origin}")
                } else if (
                    (originLower.contains("pune") && rideOriginLower.contains("nashik")) ||
                    (originLower.contains("nashik") && rideOriginLower.contains("pune"))
                ) {
                    // Shared corridor to Goa/Konkan!
                    score += 25
                    sharedCorridor = "NH48 Western Corridor (Nashik & Pune merge at Kolhapur)"
                    reasons.add("Compatible shared route along NH48 Highway")

                    meetSuggestion = MeetInMiddleSuggestion(
                        meetingPointName = "Highway King BPCL Oasis & Biker Hub",
                        highwayOrRegion = "NH48 Kolhapur Bypass",
                        distanceFromRiderAKm = 380, // from Nashik
                        distanceFromRiderBKm = 230, // from Pune
                        estimatedTravelTime = "Sync time ~1:30 PM",
                        facilities = listOf("24/7 Fuel", "Biker Rest Area", "Shaded Parking", "Food Court"),
                        recommendationReason = "Natural intersection point where Nashik & Pune routes converge towards Goa."
                    )
                } else if (ride.plannedStops.any { it.lowercase().contains(originLower) }) {
                    score += 25
                    reasons.add("Pick up along route stop: ${ride.origin}")
                } else {
                    score += 5
                }
            } else {
                score += 15
            }

            // Pace compatibility
            if (user.pacePreference.isNotEmpty() && ride.pace.contains(user.pacePreference.take(6), ignoreCase = true)) {
                score += 15
                reasons.add("Matched riding pace: ${ride.pace}")
            }

            // Experience match
            if (ride.experienceLevel == "All Welcomed" || ride.experienceLevel.contains("Intermediate", true)) {
                score += 10
                reasons.add("Compatible skill expectation")
            }

            // Trip type filter
            if (filters.tripType != "All" && !ride.tripType.contains(filters.tripType, ignoreCase = true)) {
                return@mapNotNull null
            }

            RouteMatchResult(
                ride = ride,
                compatibilityScore = score.coerceIn(10, 100),
                matchReasons = reasons,
                sharedCorridor = sharedCorridor,
                meetInMiddleSuggestion = meetSuggestion
            )
        }.sortedByDescending { it.compatibilityScore }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Navigation Controls ---
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _selectedRideId.value = null
        _selectedCommunityId.value = null
        _selectedEventId.value = null
        _selectedConversationId.value = null
        _showSafetyCenter.value = false
        _showAdminDashboard.value = false
    }

    fun openRideDetail(rideId: String) {
        _selectedRideId.value = rideId
    }

    fun closeRideDetail() {
        _selectedRideId.value = null
    }

    fun openCommunityDetail(communityId: String) {
        _selectedCommunityId.value = communityId
    }

    fun closeCommunityDetail() {
        _selectedCommunityId.value = null
    }

    fun openEventDetail(eventId: String) {
        _selectedEventId.value = eventId
    }

    fun closeEventDetail() {
        _selectedEventId.value = null
    }

    fun openConversation(conversationId: String) {
        _selectedConversationId.value = conversationId
    }

    fun closeConversation() {
        _selectedConversationId.value = null
    }

    fun openSafetyCenter() {
        _showSafetyCenter.value = true
    }

    fun closeSafetyCenter() {
        _showSafetyCenter.value = false
    }

    fun openAdminDashboard() {
        _showAdminDashboard.value = true
    }

    fun closeAdminDashboard() {
        _showAdminDashboard.value = false
    }

    fun toggleNotifications(show: Boolean) {
        _showNotifications.value = show
    }

    fun togglePersonaSwitcher(show: Boolean) {
        _showPersonaSwitcher.value = show
    }

    fun openReportDialog(targetType: ReportTargetType, targetId: String, targetTitle: String) {
        _reportTarget.value = Triple(targetType, targetId, targetTitle)
    }

    fun closeReportDialog() {
        _reportTarget.value = null
    }

    // --- Actions ---
    fun switchUser(userId: String) {
        repository.switchUser(userId)
        _showPersonaSwitcher.value = false
    }

    fun updateProfile(user: User) {
        repository.updateProfile(user)
    }

    fun createRide(
        title: String,
        origin: String,
        destination: String,
        departureDate: String,
        departureTime: String,
        capacity: Int,
        visibility: RideVisibility,
        tripType: String,
        pace: String,
        experienceLevel: String,
        description: String,
        routeNotes: String,
        plannedStops: List<String>,
        meetingPoint: String,
        fuelNotes: String,
        foodNotes: String,
        rules: List<String>,
        distanceKm: Int,
        durationHours: Double
    ) {
        val created = repository.createRide(
            title, origin, destination, departureDate, departureTime, capacity,
            visibility, tripType, pace, experienceLevel, description, routeNotes,
            plannedStops, meetingPoint, fuelNotes, foodNotes, rules, distanceKm, durationHours
        )
        // Navigate to details of created ride
        _selectedRideId.value = created.id
        _currentTab.value = MainTab.FIND_RIDE
    }

    fun cancelRide(rideId: String, reason: String) {
        repository.cancelRide(rideId, reason)
    }

    fun completeRide(rideId: String) {
        repository.completeRide(rideId)
    }

    fun toggleSaveRide(rideId: String) {
        repository.toggleSaveRide(rideId)
    }

    fun requestToJoinRide(rideId: String, note: String): Boolean {
        return repository.requestToJoinRide(rideId, note)
    }

    fun acceptJoinRequest(requestId: String): Boolean {
        return repository.acceptJoinRequest(requestId)
    }

    fun declineJoinRequest(requestId: String) {
        repository.declineJoinRequest(requestId)
    }

    fun toggleJoinCommunity(communityId: String) {
        repository.toggleJoinCommunity(communityId)
    }

    fun approveCommunityMember(communityId: String, applicantId: String) {
        repository.approveCommunityMember(communityId, applicantId)
    }

    fun createCommunity(
        name: String,
        category: String,
        description: String,
        location: String,
        rules: List<String>,
        membershipType: MembershipType
    ) {
        val created = repository.createCommunity(name, category, description, location, rules, membershipType)
        _selectedCommunityId.value = created.id
    }

    fun rsvpEvent(eventId: String, isGoing: Boolean) {
        repository.rsvpEvent(eventId, isGoing)
    }

    fun createEvent(
        title: String,
        description: String,
        dateTime: String,
        meetingPoint: String,
        capacity: Int,
        communityId: String?,
        communityName: String?,
        routePlan: String
    ) {
        val created = repository.createEvent(title, description, dateTime, meetingPoint, capacity, communityId, communityName, routePlan)
        _selectedEventId.value = created.id
    }

    fun sendMessage(conversationId: String, text: String): Boolean {
        return repository.sendMessage(conversationId, text)
    }

    fun updateTripCheckIn(rideId: String, status: CheckInStatus, stop: String) {
        repository.updateTripCheckIn(rideId, status, stop)
    }

    fun toggleFavoriteSpot(spotId: String) {
        repository.toggleFavoriteSpot(spotId)
    }

    fun blockUser(userId: String) {
        repository.blockUser(userId)
    }

    fun unblockUser(userId: String) {
        repository.unblockUser(userId)
    }

    fun submitReport(
        targetType: ReportTargetType,
        targetId: String,
        targetTitle: String,
        reportedUserId: String?,
        reason: String,
        details: String
    ) {
        repository.submitReport(targetType, targetId, targetTitle, reportedUserId, reason, details)
        closeReportDialog()
    }

    fun resolveReport(reportId: String, action: ReportStatus, notes: String) {
        repository.resolveReport(reportId, action, notes)
    }

    fun markAllNotificationsRead() {
        repository.markAllNotificationsRead()
    }

    fun resetDemoData() {
        repository.resetToDemoData()
        _selectedRideId.value = null
        _selectedCommunityId.value = null
        _selectedEventId.value = null
        _selectedConversationId.value = null
        _currentTab.value = MainTab.HOME
    }
}
