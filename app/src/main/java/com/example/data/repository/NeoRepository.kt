package com.example.data.repository

import com.example.data.firestore.NeoFirestoreManager
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class NeoRepository(
    private val firestoreManager: NeoFirestoreManager? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    init {
        firestoreManager?.let { manager ->
            coroutineScope.launch {
                manager.observeActiveRides().collect { cloudRides ->
                    if (cloudRides.isNotEmpty()) {
                        _rides.update { current ->
                            val cloudIds = cloudRides.map { it.id }.toSet()
                            cloudRides + current.filter { it.id !in cloudIds }
                        }
                    }
                }
            }
        }
    }

    // --- State ---
    private val _currentUser = MutableStateFlow(createInitialUsers().first())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow(createInitialUsers())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _rides = MutableStateFlow(createInitialRides())
    val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    private val _requests = MutableStateFlow(createInitialRequests())
    val requests: StateFlow<List<RideRequest>> = _requests.asStateFlow()

    private val _communities = MutableStateFlow(createInitialCommunities())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _events = MutableStateFlow(createInitialEvents())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _conversations = MutableStateFlow(createInitialConversations())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow(createInitialMessages())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _bikerSpots = MutableStateFlow(createInitialSpots())
    val bikerSpots: StateFlow<List<BikerSpot>> = _bikerSpots.asStateFlow()

    private val _passportBadges = MutableStateFlow(createInitialBadges())
    val passportBadges: StateFlow<List<PassportBadge>> = _passportBadges.asStateFlow()

    private val _completedRides = MutableStateFlow(createInitialCompletedRides())
    val completedRides: StateFlow<List<CompletedRideRecord>> = _completedRides.asStateFlow()

    private val _tripCheckIns = MutableStateFlow<Map<String, TripCheckInState>>(createInitialCheckIns())
    val tripCheckIns: StateFlow<Map<String, TripCheckInState>> = _tripCheckIns.asStateFlow()

    private val _blockedPairs = MutableStateFlow<Set<Pair<String, String>>>(emptySet())
    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val _reports = MutableStateFlow(createInitialReports())
    val reports: StateFlow<List<SafetyReport>> = _reports.asStateFlow()

    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // --- User Management & Persona Switching ---
    fun switchUser(userId: String) {
        val found = _allUsers.value.find { it.id == userId }
        if (found != null) {
            _currentUser.value = found
            _blockedUserIds.value = _blockedPairs.value.filter { it.first == userId }.map { it.second }.toSet()
        }
    }

    fun updateProfile(updated: User) {
        _currentUser.value = updated
        _allUsers.update { list -> list.map { if (it.id == updated.id) updated else it } }
        firestoreManager?.let { manager ->
            coroutineScope.launch {
                manager.saveUserProfile(updated)
            }
        }
    }

    // --- Ride Actions ---
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
    ): Ride {
        val user = _currentUser.value
        val newRide = Ride(
            id = "ride_" + UUID.randomUUID().toString().take(8),
            title = title,
            origin = origin,
            destination = destination,
            departureDate = departureDate,
            departureTime = departureTime,
            capacity = capacity.coerceAtLeast(1),
            confirmedRiderIds = listOf(user.id),
            pendingRequestRiderIds = emptyList(),
            declinedRiderIds = emptyList(),
            visibility = visibility,
            status = RideStatus.ACTIVE,
            tripType = tripType,
            pace = pace,
            experienceLevel = experienceLevel,
            description = description,
            routeNotes = routeNotes,
            plannedStops = plannedStops,
            meetingPoint = meetingPoint,
            fuelBreakNotes = fuelNotes,
            foodBreakNotes = foodNotes,
            rideRules = rules,
            distanceKm = distanceKm,
            durationHours = durationHours,
            organizerId = user.id,
            organizerName = user.name,
            organizerAvatar = user.avatarEmoji
        )
        _rides.update { listOf(newRide) + it }
        firestoreManager?.let { manager ->
            coroutineScope.launch {
                manager.saveRide(newRide)
            }
        }
        return newRide
    }

    fun cancelRide(rideId: String, reason: String = "Cancelled by organizer") {
        val user = _currentUser.value
        _rides.update { list ->
            list.map { ride ->
                if (ride.id == rideId && ride.organizerId == user.id) {
                    val cancelled = ride.copy(status = RideStatus.CANCELLED)
                    firestoreManager?.let { manager ->
                        coroutineScope.launch {
                            manager.saveRide(cancelled)
                        }
                    }
                    cancelled
                } else ride
            }
        }
        // Send notification to participants
        addNotification(
            title = "Ride Cancelled",
            message = "Ride '$rideId' was cancelled: $reason",
            type = NotificationType.RIDE_UPDATE
        )
    }

    fun completeRide(rideId: String) {
        val user = _currentUser.value
        _rides.update { list ->
            list.map { ride ->
                if (ride.id == rideId && ride.organizerId == user.id) {
                    ride.copy(status = RideStatus.COMPLETED)
                } else ride
            }
        }
    }

    fun toggleSaveRide(rideId: String) {
        _rides.update { list ->
            list.map { ride ->
                if (ride.id == rideId) ride.copy(isSaved = !ride.isSaved) else ride
            }
        }
    }

    // --- Join Request Management & Server-side rules ---
    fun requestToJoinRide(rideId: String, note: String): Boolean {
        val user = _currentUser.value
        val ride = _rides.value.find { it.id == rideId } ?: return false

        // Business rules: cannot request if already confirmed, pending, or organizer
        if (ride.organizerId == user.id) return false
        if (ride.confirmedRiderIds.contains(user.id)) return false
        if (ride.pendingRequestRiderIds.contains(user.id)) return false
        if (ride.confirmedRiderIds.size >= ride.capacity) return false // capacity rule

        val newRequest = RideRequest(
            id = "req_" + UUID.randomUUID().toString().take(8),
            rideId = rideId,
            riderId = user.id,
            riderName = user.name,
            riderAvatar = user.avatarEmoji,
            riderMotorcycle = user.motorcycle,
            riderExperience = user.experienceLevel,
            riderPace = user.pacePreference,
            note = note,
            status = RequestStatus.PENDING,
            requestedAt = "Just now"
        )

        _requests.update { listOf(newRequest) + it }
        _rides.update { list ->
            list.map { r ->
                if (r.id == rideId) {
                    r.copy(pendingRequestRiderIds = r.pendingRequestRiderIds + user.id)
                } else r
            }
        }

        // Notify organizer
        addNotification(
            title = "New Join Request",
            message = "${user.name} requested to join '${ride.title}'",
            type = NotificationType.JOIN_REQUEST
        )
        return true
    }

    fun acceptJoinRequest(requestId: String): Boolean {
        val request = _requests.value.find { it.id == requestId } ?: return false
        val ride = _rides.value.find { it.id == request.rideId } ?: return false
        val current = _currentUser.value

        // Only organizer can accept
        if (ride.organizerId != current.id) return false
        // Server-side capacity enforcement
        if (ride.confirmedRiderIds.size >= ride.capacity) return false

        _requests.update { list ->
            list.map { if (it.id == requestId) it.copy(status = RequestStatus.ACCEPTED) else it }
        }

        _rides.update { list ->
            list.map { r ->
                if (r.id == request.rideId) {
                    r.copy(
                        confirmedRiderIds = (r.confirmedRiderIds + request.riderId).distinct(),
                        pendingRequestRiderIds = r.pendingRequestRiderIds.filter { it != request.riderId }
                    )
                } else r
            }
        }

        // Add rider to group conversation for the ride
        _conversations.update { list ->
            list.map { conv ->
                if (conv.pinnedRideId == request.rideId) {
                    conv.copy(participantIds = (conv.participantIds + request.riderId).distinct())
                } else conv
            }
        }

        addNotification(
            title = "Request Accepted!",
            message = "You have been confirmed for '${ride.title}'. You can now chat in the group!",
            type = NotificationType.REQUEST_ACCEPTED
        )
        return true
    }

    fun declineJoinRequest(requestId: String) {
        val request = _requests.value.find { it.id == requestId } ?: return
        val ride = _rides.value.find { it.id == request.rideId } ?: return
        val current = _currentUser.value
        if (ride.organizerId != current.id) return

        _requests.update { list ->
            list.map { if (it.id == requestId) it.copy(status = RequestStatus.DECLINED) else it }
        }

        _rides.update { list ->
            list.map { r ->
                if (r.id == request.rideId) {
                    r.copy(
                        declinedRiderIds = (r.declinedRiderIds + request.riderId).distinct(),
                        pendingRequestRiderIds = r.pendingRequestRiderIds.filter { it != request.riderId }
                    )
                } else r
            }
        }

        addNotification(
            title = "Request Declined",
            message = "Your request to join '${ride.title}' could not be accommodated.",
            type = NotificationType.REQUEST_DECLINED
        )
    }

    // --- Communities & Circles ---
    fun toggleJoinCommunity(communityId: String) {
        val user = _currentUser.value
        _communities.update { list ->
            list.map { c ->
                if (c.id == communityId) {
                    if (c.memberIds.contains(user.id)) {
                        c.copy(memberIds = c.memberIds.filter { it != user.id })
                    } else if (c.membershipType == MembershipType.OPEN) {
                        c.copy(memberIds = c.memberIds + user.id)
                    } else {
                        // Request approval
                        c.copy(pendingMemberIds = (c.pendingMemberIds + user.id).distinct())
                    }
                } else c
            }
        }
    }

    fun approveCommunityMember(communityId: String, applicantId: String) {
        val user = _currentUser.value
        _communities.update { list ->
            list.map { c ->
                if (c.id == communityId && c.adminIds.contains(user.id)) {
                    c.copy(
                        memberIds = (c.memberIds + applicantId).distinct(),
                        pendingMemberIds = c.pendingMemberIds.filter { it != applicantId }
                    )
                } else c
            }
        }
    }

    fun createCommunity(
        name: String,
        category: String,
        description: String,
        location: String,
        rules: List<String>,
        membershipType: MembershipType
    ): Community {
        val user = _currentUser.value
        val newComm = Community(
            id = "comm_" + UUID.randomUUID().toString().take(8),
            name = name,
            category = category,
            description = description,
            location = location,
            memberIds = listOf(user.id),
            pendingMemberIds = emptyList(),
            adminIds = listOf(user.id),
            rules = rules,
            membershipType = membershipType,
            announcements = listOf("Welcome to $name! Ride safe and respect road rules.")
        )
        _communities.update { listOf(newComm) + it }
        return newComm
    }

    // --- Events & RSVP ---
    fun rsvpEvent(eventId: String, isGoing: Boolean) {
        val user = _currentUser.value
        _events.update { list ->
            list.map { event ->
                if (event.id == eventId) {
                    if (isGoing) {
                        event.copy(
                            goingUserIds = (event.goingUserIds + user.id).distinct(),
                            notGoingUserIds = event.notGoingUserIds.filter { it != user.id }
                        )
                    } else {
                        event.copy(
                            notGoingUserIds = (event.notGoingUserIds + user.id).distinct(),
                            goingUserIds = event.goingUserIds.filter { it != user.id }
                        )
                    }
                } else event
            }
        }
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
    ): Event {
        val user = _currentUser.value
        val newEvent = Event(
            id = "evt_" + UUID.randomUUID().toString().take(8),
            communityId = communityId,
            communityName = communityName,
            title = title,
            description = description,
            dateTime = dateTime,
            meetingPoint = meetingPoint,
            capacity = capacity,
            organizerId = user.id,
            organizerName = user.name,
            goingUserIds = listOf(user.id),
            notGoingUserIds = emptyList(),
            routePlan = routePlan,
            rules = listOf("Helmets and riding jackets mandatory", "No rash overtakes")
        )
        _events.update { listOf(newEvent) + it }
        return newEvent
    }

    // --- Messaging ---
    fun sendMessage(conversationId: String, text: String): Boolean {
        val user = _currentUser.value
        val conversation = _conversations.value.find { it.id == conversationId } ?: return false

        // Check if recipient is blocked by sender, or recipient has blocked sender
        val otherParticipant = conversation.participantIds.find { it != user.id }
        if (otherParticipant != null) {
            val isBlockedByMe = _blockedPairs.value.contains(Pair(user.id, otherParticipant))
            val hasBlockedMe = _blockedPairs.value.contains(Pair(otherParticipant, user.id))
            if (isBlockedByMe || hasBlockedMe) {
                return false // Blocked users cannot deliver or receive messages
            }
        }

        val newMsg = Message(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conversationId,
            senderId = user.id,
            senderName = user.name,
            text = text,
            timestamp = "Just now"
        )

        _messages.update { it + newMsg }
        _conversations.update { list ->
            list.map {
                if (it.id == conversationId) it.copy(lastMessage = text, lastMessageTime = "Just now")
                else it
            }
        }
        return true
    }

    // --- Trip Check-In Feature ---
    fun updateTripCheckIn(rideId: String, status: CheckInStatus, stop: String) {
        _tripCheckIns.update { map ->
            map + (rideId to TripCheckInState(
                rideId = rideId,
                status = status,
                currentStop = stop,
                lastUpdated = "Just now",
                trustedContactAlertEnabled = true
            ))
        }
    }

    // --- Biker Spot Favoriting ---
    fun toggleFavoriteSpot(spotId: String) {
        _bikerSpots.update { list ->
            list.map { if (it.id == spotId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    // --- Safety, Reporting & Blocking ---
    fun blockUser(userId: String) {
        val current = _currentUser.value
        _blockedPairs.update { it + Pair(current.id, userId) }
        _blockedUserIds.update { it + userId }
        addNotification(
            title = "User Blocked",
            message = "You will no longer receive messages or requests from this user.",
            type = NotificationType.MESSAGE
        )
    }

    fun unblockUser(userId: String) {
        val current = _currentUser.value
        _blockedPairs.update { it - Pair(current.id, userId) }
        _blockedUserIds.update { it - userId }
    }

    fun submitReport(
        targetType: ReportTargetType,
        targetId: String,
        targetTitle: String,
        reportedUserId: String?,
        reason: String,
        details: String
    ): SafetyReport {
        val user = _currentUser.value
        val report = SafetyReport(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            reporterId = user.id,
            reportedUserId = reportedUserId,
            targetType = targetType,
            targetId = targetId,
            targetTitle = targetTitle,
            reason = reason,
            details = details,
            status = ReportStatus.PENDING,
            createdAt = "Today, 10:30 AM"
        )
        _reports.update { listOf(report) + it }
        addNotification(
            title = "Report Submitted",
            message = "Our moderation team is reviewing your report. Case ID: ${report.id}",
            type = NotificationType.RIDE_UPDATE
        )
        return report
    }

    // Admin Action
    fun resolveReport(reportId: String, action: ReportStatus, notes: String) {
        _reports.update { list ->
            list.map {
                if (it.id == reportId) it.copy(status = action, adminNotes = notes) else it
            }
        }
    }

    // Notifications
    private fun addNotification(title: String, message: String, type: NotificationType) {
        val user = _currentUser.value
        val notif = NotificationItem(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            userId = user.id,
            title = title,
            message = message,
            timeAgo = "Just now",
            isRead = false,
            type = type
        )
        _notifications.update { listOf(notif) + it }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    // Reset Demo Data
    fun resetToDemoData() {
        _allUsers.value = createInitialUsers()
        _currentUser.value = createInitialUsers().first()
        _rides.value = createInitialRides()
        _requests.value = createInitialRequests()
        _communities.value = createInitialCommunities()
        _events.value = createInitialEvents()
        _conversations.value = createInitialConversations()
        _messages.value = createInitialMessages()
        _bikerSpots.value = createInitialSpots()
        _passportBadges.value = createInitialBadges()
        _completedRides.value = createInitialCompletedRides()
        _tripCheckIns.value = createInitialCheckIns()
        _blockedPairs.value = emptySet()
        _blockedUserIds.value = emptySet()
        _reports.value = createInitialReports()
        _notifications.value = createInitialNotifications()
    }

    // --- Helper Seed Data ---
    companion object {
        fun createInitialUsers(): List<User> = listOf(
            User(
                id = "user_vikram",
                name = "Vikram Malhotra",
                username = "vikram_tourer",
                avatarEmoji = "🏍️",
                bio = "Seasoned touring enthusiast. 45,000+ km on two wheels across India. Love dawn departures, Ghat curves, and respectful pace.",
                city = "Nashik",
                languages = listOf("English", "Hindi", "Marathi"),
                motorcycle = "Royal Enfield Himalayan 450 (Kamet White)",
                experienceLevel = "Seasoned Tourer (8+ yrs)",
                pacePreference = "Balanced (80-100 km/h)",
                interests = listOf("Mountain Touring", "Scenic Trails", "Photography", "Campfire Stops"),
                preferredTripTypes = listOf("Multi-day Touring", "Coastal Loops", "Weekend Getaway"),
                departureTimePref = "Early Dawn (5:30 AM)",
                isContactVerified = true,
                isAdmin = true
            ),
            User(
                id = "user_rahul",
                name = "Rahul Sharma",
                username = "rahul_adv",
                avatarEmoji = "⚡",
                bio = "Passionate ADV rider exploring scenic routes and long coastal trails. Always up for good coffee and winding asphalt.",
                city = "Pune",
                languages = listOf("English", "Hindi"),
                motorcycle = "KTM 390 Adventure (Orange/Black)",
                experienceLevel = "Intermediate (3-5 yrs)",
                pacePreference = "Balanced (80-100 km/h)",
                interests = listOf("Fast Touring", "Coffee Stops", "Mountain Passes"),
                preferredTripTypes = listOf("Weekend Ride", "Ghat Circuits"),
                departureTimePref = "Early Morning (6:00 AM)",
                isContactVerified = true,
                isAdmin = false
            ),
            User(
                id = "user_neha",
                name = "Neha Sen",
                username = "neha_tiger",
                avatarEmoji = "🛡️",
                bio = "Trail leader, community organizer, and safety advocate. Believer in ATGATT (All The Gear, All The Time).",
                city = "Mumbai",
                languages = listOf("English", "Hindi", "Bengali"),
                motorcycle = "Triumph Tiger 900 GT",
                experienceLevel = "Veteran Tourer (10+ yrs)",
                pacePreference = "Brisk Highway (100-110 km/h)",
                interests = listOf("Enduro", "First Aid Road Safety", "High Passes"),
                preferredTripTypes = listOf("Trans-Himalayan", "Western Ghats", "Long Distance"),
                departureTimePref = "Early Dawn (5:00 AM)",
                isContactVerified = true,
                isAdmin = true
            ),
            User(
                id = "user_ananya",
                name = "Ananya Roy",
                username = "ananya_gs",
                avatarEmoji = "🌟",
                bio = "Weekend explorer loving nature trails, historic forts, and countryside dhabas. Solo traveller turned group ride lover.",
                city = "Bengaluru",
                languages = listOf("English", "Kannada", "Hindi"),
                motorcycle = "BMW G 310 GS",
                experienceLevel = "Intermediate (4 yrs)",
                pacePreference = "Relaxed (70-90 km/h)",
                interests = listOf("Heritage Routes", "Local Food", "Nature Photography"),
                preferredTripTypes = listOf("Scenic Weekend", "Coffee Estate Trail"),
                departureTimePref = "Morning (6:30 AM)",
                isContactVerified = true,
                isAdmin = false
            )
        )

        fun createInitialRides(): List<Ride> = listOf(
            Ride(
                id = "ride_nashik_goa",
                title = "Nashik to South Goa Scenic Coastal Tour",
                origin = "Nashik",
                destination = "Goa",
                departureDate = "2026-10-31",
                departureTime = "05:30 AM",
                flexibleDate = false,
                capacity = 4,
                confirmedRiderIds = listOf("user_vikram", "user_neha"),
                pendingRequestRiderIds = listOf("user_rahul"),
                declinedRiderIds = emptyList(),
                visibility = RideVisibility.PUBLIC,
                status = RideStatus.ACTIVE,
                tripType = "Coastal & Ghat Tour",
                pace = "Balanced (80-100 km/h)",
                experienceLevel = "Intermediate to Seasoned",
                description = "Scenic 2-day touring route crossing Kasara, Pune bypass, NH48 Kolhapur hub, and descending through breathtaking Amboli Ghat into North Goa.",
                routeNotes = "Smooth 4-lane highway up to Kolhapur, followed by scenic winding mountain descent via Amboli Ghat. Regular fuel breaks every 160-180 km.",
                plannedStops = listOf(
                    "Kasara Valley Viewpoint (Quick tea at 7:00 AM)",
                    "Pune Outer Bypass (Meet point with Pune riders at 10:00 AM)",
                    "Kolhapur Highway Hub (Lunch & fuel refuel at 1:30 PM)",
                    "Amboli Ghat Waterfalls (Chai & photography at 4:30 PM)",
                    "Panaji / South Goa (Estimated arrival 7:00 PM)"
                ),
                meetingPoint = "Dwarka Circle Petrol Plaza, Nashik (Safe public 24/7 plaza)",
                fuelBreakNotes = "IOCL & Shell premium pumps identified every 150 km",
                foodBreakNotes = "Iconic Kolhapur Misal stop + Amboli Ghat hillside snacks",
                rideRules = listOf(
                    "Full riding gear mandatory (ECE/DOT helmet, jacket, gloves, boots)",
                    "Maintain safe staggered riding formation on highways",
                    "No dangerous overtaking on blind ghat corners",
                    "Keep bike documents and PUC up to date"
                ),
                distanceKm = 645,
                durationHours = 12.5,
                organizerId = "user_vikram",
                organizerName = "Vikram Malhotra",
                organizerAvatar = "🏍️",
                isSaved = true
            ),
            Ride(
                id = "ride_pune_goa",
                title = "Pune to Goa Highway Express & Amboli Run",
                origin = "Pune",
                destination = "Goa",
                departureDate = "2026-10-31",
                departureTime = "06:30 AM",
                flexibleDate = true,
                capacity = 3,
                confirmedRiderIds = listOf("user_rahul"),
                pendingRequestRiderIds = emptyList(),
                declinedRiderIds = emptyList(),
                visibility = RideVisibility.PUBLIC,
                status = RideStatus.ACTIVE,
                tripType = "Highway Cruise",
                pace = "Balanced (80-100 km/h)",
                experienceLevel = "All Welcomed",
                description = "Relaxed group ride from Pune towards Goa. Compatible route and date with the Nashik group for a shared meet-in-the-middle at Kolhapur!",
                routeNotes = "NH48 6-lane tollway -> Satara -> Karad -> Kolhapur -> Amboli Ghat.",
                plannedStops = listOf(
                    "Khed Shivapur Plaza (Departure)",
                    "Satara Bypass Tea Point",
                    "Kolhapur Highway Food Hub (Possible sync with Nashik group)",
                    "Amboli Valley Halt"
                ),
                meetingPoint = "McD / BPCL Highway Hub, Khed Shivapur",
                distanceKm = 450,
                durationHours = 8.5,
                organizerId = "user_rahul",
                organizerName = "Rahul Sharma",
                organizerAvatar = "⚡",
                isSaved = false
            ),
            Ride(
                id = "ride_mumbai_lonavala",
                title = "Mumbai to Lonavala Sunrise Breakfast Loop",
                origin = "Mumbai",
                destination = "Lonavala",
                departureDate = "2026-11-01",
                departureTime = "06:00 AM",
                flexibleDate = false,
                capacity = 6,
                confirmedRiderIds = listOf("user_neha", "user_vikram", "user_ananya"),
                pendingRequestRiderIds = emptyList(),
                visibility = RideVisibility.PUBLIC,
                status = RideStatus.ACTIVE,
                tripType = "Sunrise Breakfast Ride",
                pace = "Relaxed (60-80 km/h)",
                experienceLevel = "Beginner Friendly",
                description = "Quick Sunday dawn ride over the old Mumbai-Pune highway via misty Bhor Ghat. South Indian breakfast at Rama Krishna Lonavala.",
                routeNotes = "Old NH48 route. Scenic ghat curves with cool morning air.",
                plannedStops = listOf("Vashi Toll Plaza (06:00 AM)", "Khopoli Ghat Foot (07:15 AM)", "Rama Krishna Lonavala (08:15 AM)"),
                meetingPoint = "Vashi Toll Plaza Food Court (Public lit area)",
                distanceKm = 95,
                durationHours = 2.2,
                organizerId = "user_neha",
                organizerName = "Neha Sen",
                organizerAvatar = "🛡️",
                isSaved = true
            ),
            Ride(
                id = "ride_blr_wayanad",
                title = "Bengaluru to Wayanad Forest & Coffee Trail",
                origin = "Bengaluru",
                destination = "Wayanad",
                departureDate = "2026-11-07",
                departureTime = "05:00 AM",
                flexibleDate = false,
                capacity = 4,
                confirmedRiderIds = listOf("user_ananya"),
                pendingRequestRiderIds = emptyList(),
                visibility = RideVisibility.PUBLIC,
                status = RideStatus.ACTIVE,
                tripType = "Mountain & Forest Trail",
                pace = "Relaxed (70-90 km/h)",
                experienceLevel = "Intermediate",
                description = "Mysuru Expressway -> Bandipur Tiger Reserve buffer zone -> Winding Thamarassery hairpins into serene Wayanad.",
                routeNotes = "Strict speed limits in forest zone (30 km/h). No stopping in tiger reserve.",
                plannedStops = listOf("Bidadi Thatte Idli Halt", "Mysuru Ring Road Fuel Point", "Bandipur Checkpost", "Vythiri Viewpoint"),
                meetingPoint = "Kengeri Metro Station Concourse Parking",
                distanceKm = 280,
                durationHours = 6.0,
                organizerId = "user_ananya",
                organizerName = "Ananya Roy",
                organizerAvatar = "🌟",
                isSaved = false
            )
        )

        fun createInitialRequests(): List<RideRequest> = listOf(
            RideRequest(
                id = "req_1",
                rideId = "ride_nashik_goa",
                riderId = "user_rahul",
                riderName = "Rahul Sharma",
                riderAvatar = "⚡",
                riderMotorcycle = "KTM 390 Adventure",
                riderExperience = "Intermediate (3-5 yrs)",
                riderPace = "Balanced (80-100 km/h)",
                note = "Hey Vikram! I'm starting from Pune around the same time. Can join your group at Pune bypass or Kolhapur for the rest of the Goa ride!",
                status = RequestStatus.PENDING,
                requestedAt = "2 hours ago"
            )
        )

        fun createInitialCommunities(): List<Community> = listOf(
            Community(
                id = "comm_western_ghats",
                name = "Western Ghats Tourers",
                category = "Long-Distance & Mountain Touring",
                description = "For motorcyclists who love twisting mountain hairpins, coastal highways, and multi-day expedition touring across the Sahyadri range.",
                location = "Maharashtra & Goa",
                memberIds = listOf("user_vikram", "user_neha", "user_rahul"),
                pendingMemberIds = emptyList(),
                adminIds = listOf("user_neha", "user_vikram"),
                rules = listOf(
                    "Safety first: Strict ATGATT policy on all official rides",
                    "Respect the sweep and lead rider instructions",
                    "Keep discussions focused on touring, road status, and mechanics",
                    "Zero tolerance for rash riding or reckless stunt videos"
                ),
                coverEmoji = "⛰️",
                themeColorHex = "#14B8A6",
                membershipType = MembershipType.OPEN,
                announcements = listOf(
                    "Monsoon road repair completed on Amboli Ghat. Surface in top shape for touring season!",
                    "Group Ride scheduled for Oct 31st to South Goa."
                )
            ),
            Community(
                id = "comm_nashik_riders",
                name = "Nashik Weekend Bikers Club",
                category = "City Biker Club",
                description = "Local Nashik brotherhood connecting vintage thumps, ADV trail hunters, and sports tourers for weekend camaraderie.",
                location = "Nashik Region",
                memberIds = listOf("user_vikram"),
                pendingMemberIds = listOf("user_rahul"),
                adminIds = listOf("user_vikram"),
                rules = listOf("Family-friendly vibe", "Welcoming to new riders"),
                coverEmoji = "🏙️",
                themeColorHex = "#F59E0B",
                membershipType = MembershipType.APPROVAL_REQUIRED,
                announcements = listOf("Sunday breakfast ride to Bhandardara dam departing 6 AM from Gangapur road.")
            ),
            Community(
                id = "comm_adventure_dualsport",
                name = "ADV & Trail Explorers",
                category = "Adventure Touring",
                description = "Unpaved trails, gravel passes, forest trails, and off-grid exploration. If the tarmac ends, the adventure begins.",
                location = "Pan India",
                memberIds = listOf("user_rahul", "user_ananya", "user_neha"),
                pendingMemberIds = emptyList(),
                adminIds = listOf("user_rahul"),
                rules = listOf("Carry basic puncture kit and portable inflator", "Leave No Trace in wilderness"),
                coverEmoji = "🧭",
                themeColorHex = "#0D9488",
                membershipType = MembershipType.OPEN,
                announcements = listOf("Wayanad trail route recommendations uploaded in discussion feed.")
            ),
            Community(
                id = "comm_sunrise_riders",
                name = "Sunrise Breakfast Riders",
                category = "Sunrise & Weekend Rides",
                description = "Dawn departures, crisp morning chill, empty highways, and steaming hot filter coffee at scenic hillside dhabas.",
                location = "Mumbai - Pune - Nashik Triangle",
                memberIds = listOf("user_neha", "user_ananya"),
                pendingMemberIds = emptyList(),
                adminIds = listOf("user_neha"),
                rules = listOf("Always depart on time (before 6:30 AM)"),
                coverEmoji = "☕",
                themeColorHex = "#D97706",
                membershipType = MembershipType.OPEN,
                announcements = listOf("Next meetup: Tiger Point Lonavala at 7:30 AM this Sunday.")
            )
        )

        fun createInitialEvents(): List<Event> = listOf(
            Event(
                id = "evt_1",
                communityId = "comm_western_ghats",
                communityName = "Western Ghats Tourers",
                title = "Sahyadri Season Kickoff: Amboli Ghat Group Run",
                description = "Join 15+ seasoned riders for a scenic weekend tour celebrating the opening of peak touring season. Group photography, seaside campfire, and coastal Konkani lunch.",
                dateTime = "Sat, Nov 07 • 06:00 AM",
                meetingPoint = "Kolhapur Highway BPCL Oasis Hub",
                capacity = 20,
                organizerId = "user_neha",
                organizerName = "Neha Sen",
                goingUserIds = listOf("user_vikram", "user_neha", "user_rahul"),
                notGoingUserIds = emptyList(),
                routePlan = "Kolhapur -> Sankeshwar -> Gadhinglaj -> Ajara -> Amboli -> Sawantwadi",
                rules = listOf("Mandatory pre-ride briefing at 5:45 AM", "Intercom channel 4 recommended")
            ),
            Event(
                id = "evt_2",
                communityId = "comm_sunrise_riders",
                communityName = "Sunrise Breakfast Riders",
                title = "Dawn Run to Tiger Point & Old Highway Twisties",
                description = "Early Sunday escape from city bustle. Watch the sun illuminate the misty Sahyadri valley followed by breakfast.",
                dateTime = "Sun, Nov 01 • 06:00 AM",
                meetingPoint = "Vashi Toll Plaza Food Court",
                capacity = 15,
                organizerId = "user_neha",
                organizerName = "Neha Sen",
                goingUserIds = listOf("user_neha", "user_ananya"),
                notGoingUserIds = emptyList(),
                routePlan = "Mumbai -> Panvel -> Khopoli -> Lonavala",
                rules = listOf("Headlights on high-beam prohibited in convoy", "Stay behind ride sweep")
            )
        )

        fun createInitialConversations(): List<Conversation> = listOf(
            Conversation(
                id = "conv_ride_nashik_goa",
                type = ConversationType.RIDE_GROUP,
                title = "Nashik to South Goa Tour (Group)",
                participantIds = listOf("user_vikram", "user_neha"),
                lastMessage = "Intercoms paired! Let's meet at 5:15 AM for a sharp 5:30 AM roll-out.",
                lastMessageTime = "10:15 AM",
                unreadCount = 1,
                pinnedRideId = "ride_nashik_goa"
            ),
            Conversation(
                id = "conv_direct_rahul_vikram",
                type = ConversationType.DIRECT,
                title = "Rahul Sharma",
                participantIds = listOf("user_vikram", "user_rahul"),
                lastMessage = "Sent you a join request! We can sync at Kolhapur bypass.",
                lastMessageTime = "11:20 AM",
                unreadCount = 1
            ),
            Conversation(
                id = "conv_comm_western_ghats",
                type = ConversationType.COMMUNITY,
                title = "Western Ghats Tourers (Lounge)",
                participantIds = listOf("user_vikram", "user_neha", "user_rahul", "user_ananya"),
                lastMessage = "Amboli ghat road surface is fresh and buttery smooth!",
                lastMessageTime = "Yesterday",
                unreadCount = 0
            )
        )

        fun createInitialMessages(): List<Message> = listOf(
            Message(
                id = "m1",
                conversationId = "conv_ride_nashik_goa",
                senderId = "user_vikram",
                senderName = "Vikram Malhotra",
                text = "Welcome to the group! Weather looks clear and sunny for the weekend.",
                timestamp = "09:30 AM"
            ),
            Message(
                id = "m2",
                conversationId = "conv_ride_nashik_goa",
                senderId = "user_neha",
                senderName = "Neha Sen",
                text = "Intercoms paired! Let's meet at 5:15 AM for a sharp 5:30 AM roll-out.",
                timestamp = "10:15 AM"
            ),
            Message(
                id = "m3",
                conversationId = "conv_direct_rahul_vikram",
                senderId = "user_rahul",
                senderName = "Rahul Sharma",
                text = "Hey Vikram! Checked your Goa itinerary. I'm leaving Pune around 6:30 AM. Perfect match at Kolhapur!",
                timestamp = "11:18 AM"
            ),
            Message(
                id = "m4",
                conversationId = "conv_direct_rahul_vikram",
                senderId = "user_rahul",
                senderName = "Rahul Sharma",
                text = "Sent you a join request! We can sync at Kolhapur bypass.",
                timestamp = "11:20 AM"
            )
        )

        fun createInitialSpots(): List<BikerSpot> = listOf(
            BikerSpot(
                id = "spot_1",
                name = "Highway King BPCL Oasis & Biker Lounge",
                category = SpotCategory.FUEL_STATION,
                location = "NH48, Kolhapur Bypass",
                address = "KM 610, NH48 Golden Quadrilateral, Kolhapur",
                highlights = listOf("24/7 Speed Petrol", "Tire Pressure & Nitrogen", "Clean Restrooms", "Starbucks & CCD"),
                operatingHours = "Open 24 Hours",
                rating = 4.8,
                isVerified = true,
                isFavorite = true
            ),
            BikerSpot(
                id = "spot_2",
                name = "Apex Motorcycle Clinic & Puncture Care",
                category = SpotCategory.REPAIR_WORKSHOP,
                location = "Satara Highway Junction",
                address = "Shop 4, Old Pune-Bangalore Road, Satara",
                highlights = listOf("Tubeless & Tube Repair", "Chain Lube & Adjustment", "Engine Oil Refills", "Brake Pad Check"),
                operatingHours = "07:00 AM - 10:00 PM",
                rating = 4.7,
                isVerified = true,
                isFavorite = false
            ),
            BikerSpot(
                id = "spot_3",
                name = "The Valley View Biker Dhaba & Cafe",
                category = SpotCategory.BIKER_CAFE,
                location = "Amboli Ghat Summit",
                address = "Main Ghat Viewpoint, Amboli, Sawantwadi",
                highlights = listOf("Spacious Dedicated Bike Parking", "Fresh Misal & Masala Tea", "High-speed Wi-Fi", "Helmet Storage Racks"),
                operatingHours = "06:00 AM - 09:00 PM",
                rating = 4.9,
                isVerified = true,
                isFavorite = true
            ),
            BikerSpot(
                id = "spot_4",
                name = "Kasara Ghat Sunrise Panorama Point",
                category = SpotCategory.SCENIC_VIEWPOINT,
                location = "Thal Ghat, NH160",
                address = "Kasara Hills, Igatpuri Pass",
                highlights = listOf("Panoramic Fog Valley View", "Scenic Curve Pullout", "Safe Paved Berm"),
                operatingHours = "Open Always",
                rating = 4.6,
                isVerified = true,
                isFavorite = true
            )
        )

        fun createInitialBadges(): List<PassportBadge> = listOf(
            PassportBadge(
                id = "badge_first_ride",
                title = "First Community Ride",
                description = "Successfully completed your first verified group ride on NEO.",
                iconEmoji = "🥉",
                isUnlocked = true,
                unlockedAt = "Oct 12, 2026"
            ),
            PassportBadge(
                id = "badge_weekend_explorer",
                title = "Weekend Explorer",
                description = "Completed 3 weekend rides exceeding 200 km each.",
                iconEmoji = "🥈",
                isUnlocked = true,
                unlockedAt = "Oct 20, 2026"
            ),
            PassportBadge(
                id = "badge_scenic_explorer",
                title = "Scenic Route Explorer",
                description = "Crossed 3 certified high mountain passes in Western Ghats.",
                iconEmoji = "🥇",
                isUnlocked = true,
                unlockedAt = "Oct 24, 2026"
            ),
            PassportBadge(
                id = "badge_five_group_rides",
                title = "Five Group Rides Veteran",
                description = "Ride with 5 different verified community circles.",
                iconEmoji = "🏆",
                isUnlocked = false,
                unlockedAt = null
            ),
            PassportBadge(
                id = "badge_community_contributor",
                title = "Community Contributor",
                description = "Organized a ride with 4+ confirmed attendees.",
                iconEmoji = "⭐",
                isUnlocked = true,
                unlockedAt = "Oct 15, 2026"
            )
        )

        fun createInitialCompletedRides(): List<CompletedRideRecord> = listOf(
            CompletedRideRecord(
                id = "rec_1",
                rideTitle = "Igatpuri Monsoon Mist & Dam Circuit",
                date = "Oct 18, 2026",
                route = "Nashik -> Igatpuri -> Vaitarna -> Nashik",
                distanceKm = 185,
                coRidersCount = 4,
                memoriesNote = "Magical morning fog over Upper Vaitarna. Perfect staggered riding formation."
            ),
            CompletedRideRecord(
                id = "rec_2",
                rideTitle = "Bhandardara Waterfall Explorer",
                date = "Oct 11, 2026",
                route = "Nashik -> Ghoti -> Bhandardara",
                distanceKm = 145,
                coRidersCount = 5,
                memoriesNote = "Stopped at Arthur Lake for tea. Clean roads and great gravel detour."
            )
        )

        fun createInitialCheckIns(): Map<String, TripCheckInState> = mapOf(
            "ride_nashik_goa" to TripCheckInState(
                rideId = "ride_nashik_goa",
                status = CheckInStatus.NOT_STARTED,
                currentStop = "Nashik Starting Plaza",
                lastUpdated = "Scheduled for Oct 31, 05:30 AM",
                trustedContactAlertEnabled = true
            )
        )

        fun createInitialReports(): List<SafetyReport> = listOf(
            SafetyReport(
                id = "rep_sample_101",
                reporterId = "user_ananya",
                reportedUserId = null,
                targetType = ReportTargetType.RIDE,
                targetId = "ride_sample_err",
                targetTitle = "Dangerous Top Speed Challenge Ride",
                reason = "Promoting unsafe speeding & aggressive racing",
                details = "User listed a ride aiming to do 160 km/h on two-lane state highway. Violates NEO community safety guidelines.",
                status = ReportStatus.PENDING,
                createdAt = "Yesterday, 04:15 PM"
            )
        )

        fun createInitialNotifications(): List<NotificationItem> = listOf(
            NotificationItem(
                id = "notif_1",
                userId = "user_vikram",
                title = "New Join Request",
                message = "Rahul Sharma requested to join 'Nashik to South Goa Scenic Coastal Tour'",
                timeAgo = "2h ago",
                isRead = false,
                type = NotificationType.JOIN_REQUEST
            ),
            NotificationItem(
                id = "notif_2",
                userId = "user_vikram",
                title = "Upcoming Tour in 5 Days",
                message = "Remember to review your pre-ride checklist for the South Goa trip.",
                timeAgo = "5h ago",
                isRead = true,
                type = NotificationType.RIDE_UPDATE
            )
        )
    }
}
