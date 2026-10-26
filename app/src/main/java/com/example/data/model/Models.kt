package com.example.data.model

enum class RideVisibility {
    PUBLIC,
    COMMUNITY_ONLY,
    INVITE_ONLY
}

enum class RideStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
    COMPLETED
}

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

enum class MembershipType {
    OPEN,
    APPROVAL_REQUIRED,
    INVITE_ONLY
}

enum class ConversationType {
    DIRECT,
    RIDE_GROUP,
    COMMUNITY
}

enum class SpotCategory {
    FUEL_STATION,
    REPAIR_WORKSHOP,
    BIKER_CAFE,
    SCENIC_VIEWPOINT,
    MEETPOINT
}

enum class CheckInStatus {
    NOT_STARTED,
    ON_THE_WAY,
    STOPPED_AT_WAYPOINT,
    COMPLETED
}

enum class ReportTargetType {
    USER,
    RIDE,
    COMMUNITY,
    EVENT,
    MESSAGE
}

enum class ReportStatus {
    PENDING,
    RESOLVED_WARNING_ISSUED,
    RESOLVED_CONTENT_REMOVED,
    DISMISSED
}

enum class NotificationType {
    JOIN_REQUEST,
    REQUEST_ACCEPTED,
    REQUEST_DECLINED,
    COMMUNITY_UPDATE,
    RIDE_UPDATE,
    EVENT_REMINDER,
    MESSAGE
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val avatarEmoji: String,
    val bio: String,
    val city: String,
    val languages: List<String> = listOf("English", "Hindi"),
    val motorcycle: String,
    val experienceLevel: String, // "Beginner", "Intermediate", "Seasoned Tourer", "Veteran"
    val pacePreference: String, // "Relaxed (60-80 km/h)", "Balanced (80-100 km/h)", "Brisk (100-120 km/h)"
    val interests: List<String> = emptyList(),
    val preferredTripTypes: List<String> = emptyList(),
    val departureTimePref: String = "Early Morning (5-7 AM)",
    val isContactVerified: Boolean = true,
    val isAdmin: Boolean = false,
    val phoneVisible: Boolean = false,
    val bioVisible: Boolean = true,
    val bikeVisible: Boolean = true
)

data class Ride(
    val id: String,
    val title: String,
    val origin: String,
    val destination: String,
    val departureDate: String, // YYYY-MM-DD
    val departureTime: String,
    val flexibleDate: Boolean = false,
    val capacity: Int,
    val confirmedRiderIds: List<String> = emptyList(),
    val pendingRequestRiderIds: List<String> = emptyList(),
    val declinedRiderIds: List<String> = emptyList(),
    val visibility: RideVisibility = RideVisibility.PUBLIC,
    val status: RideStatus = RideStatus.ACTIVE,
    val tripType: String, // "Weekend Tour", "Scenic Mountain", "Long Distance", "Coastal Run"
    val pace: String,
    val experienceLevel: String,
    val description: String,
    val routeNotes: String,
    val plannedStops: List<String> = emptyList(),
    val meetingPoint: String,
    val fuelBreakNotes: String = "",
    val foodBreakNotes: String = "",
    val rideRules: List<String> = emptyList(),
    val distanceKm: Int,
    val durationHours: Double,
    val organizerId: String,
    val organizerName: String,
    val organizerAvatar: String,
    val communityId: String? = null,
    val isSaved: Boolean = false
)

data class RideRequest(
    val id: String,
    val rideId: String,
    val riderId: String,
    val riderName: String,
    val riderAvatar: String,
    val riderMotorcycle: String,
    val riderExperience: String,
    val riderPace: String,
    val note: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val requestedAt: String
)

data class MeetInMiddleSuggestion(
    val meetingPointName: String,
    val highwayOrRegion: String,
    val distanceFromRiderAKm: Int,
    val distanceFromRiderBKm: Int,
    val estimatedTravelTime: String,
    val facilities: List<String>,
    val recommendationReason: String,
    val isConfirmed: Boolean = false
)

data class Community(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val location: String,
    val memberIds: List<String> = emptyList(),
    val pendingMemberIds: List<String> = emptyList(),
    val adminIds: List<String> = emptyList(),
    val rules: List<String> = emptyList(),
    val coverEmoji: String = "🏍️",
    val themeColorHex: String = "#14B8A6",
    val membershipType: MembershipType = MembershipType.OPEN,
    val announcements: List<String> = emptyList()
)

data class Event(
    val id: String,
    val communityId: String? = null,
    val communityName: String? = null,
    val title: String,
    val description: String,
    val dateTime: String,
    val meetingPoint: String,
    val capacity: Int,
    val organizerId: String,
    val organizerName: String,
    val goingUserIds: List<String> = emptyList(),
    val notGoingUserIds: List<String> = emptyList(),
    val routePlan: String,
    val rules: List<String> = emptyList(),
    val isCancelled: Boolean = false
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isSystem: Boolean = false
)

data class Conversation(
    val id: String,
    val type: ConversationType,
    val title: String,
    val participantIds: List<String>,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val pinnedRideId: String? = null,
    val isMuted: Boolean = false
)

data class BikerSpot(
    val id: String,
    val name: String,
    val category: SpotCategory,
    val location: String,
    val address: String,
    val highlights: List<String>,
    val operatingHours: String,
    val rating: Double,
    val isVerified: Boolean = true,
    val isFavorite: Boolean = false
)

data class PassportBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val unlockedAt: String? = null
)

data class CompletedRideRecord(
    val id: String,
    val rideTitle: String,
    val date: String,
    val route: String,
    val distanceKm: Int,
    val coRidersCount: Int,
    val memoriesNote: String
)

data class TripCheckInState(
    val rideId: String,
    val status: CheckInStatus,
    val currentStop: String,
    val lastUpdated: String,
    val trustedContactAlertEnabled: Boolean = false
)

data class SafetyReport(
    val id: String,
    val reporterId: String,
    val reportedUserId: String? = null,
    val targetType: ReportTargetType,
    val targetId: String,
    val targetTitle: String,
    val reason: String,
    val details: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: String,
    val adminNotes: String? = null
)

data class NotificationItem(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val type: NotificationType
)

data class Motorcycle(
    val id: String,
    val ownerId: String,
    val make: String,
    val model: String,
    val year: Int,
    val displacementCc: Int,
    val odometerKm: Int,
    val licensePlate: String = "",
    val isPrimary: Boolean = true,
    val photoEmoji: String = "🏍️",
    val lastServiceDate: String = "",
    val nextServiceDueKm: Int = 0,
    val insuranceExpiryDate: String = "",
    val notes: String = ""
)

data class MaintenanceRecord(
    val id: String,
    val bikeId: String,
    val serviceType: String, // e.g., "Engine Oil & Filter", "Chain Lube & Tension", "Brake Pad Change"
    val date: String,
    val odometerKm: Int,
    val costAmount: Double,
    val workshopName: String = "",
    val notes: String = ""
)

data class MaintenanceReminder(
    val id: String,
    val bikeId: String,
    val title: String,
    val dueKm: Int,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium" // "High", "Medium", "Low"
)

data class CommunityPost(
    val id: String,
    val communityId: String? = null,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val authorMotorcycle: String,
    val content: String,
    val location: String = "",
    val timestamp: String,
    val likesCount: Int = 0,
    val likedByCurrentUser: Boolean = false,
    val comments: List<PostComment> = emptyList(),
    val tag: String = "General" // "Ride Story", "Maintenance Tip", "Route Review", "General"
)

data class PostComment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val timestamp: String
)

