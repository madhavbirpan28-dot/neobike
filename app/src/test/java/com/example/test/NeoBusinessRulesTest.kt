package com.example.test

import com.example.data.model.*
import com.example.data.repository.NeoRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NeoBusinessRulesTest {

    private lateinit var repository: NeoRepository

    @Before
    fun setUp() {
        repository = NeoRepository()
    }

    @Test
    fun testA_userProfileAndPersonaSwitching() {
        val initialUser = repository.currentUser.value
        assertEquals("user_vikram", initialUser.id)
        assertTrue(initialUser.isContactVerified)

        // Switch to Rahul
        repository.switchUser("user_rahul")
        assertEquals("user_rahul", repository.currentUser.value.id)
        assertEquals("KTM 390 Adventure (Orange/Black)", repository.currentUser.value.motorcycle)

        // Update profile
        val updated = repository.currentUser.value.copy(city = "Lonavala")
        repository.updateProfile(updated)
        assertEquals("Lonavala", repository.currentUser.value.city)
    }

    @Test
    fun testB_createRideAndSearchResults() {
        repository.switchUser("user_vikram")
        val ride = repository.createRide(
            title = "Pune to Mahabaleshwar Weekend Run",
            origin = "Pune",
            destination = "Mahabaleshwar",
            departureDate = "2026-11-10",
            departureTime = "06:00 AM",
            capacity = 3,
            visibility = RideVisibility.PUBLIC,
            tripType = "Weekend Tour",
            pace = "Balanced (80-100 km/h)",
            experienceLevel = "Intermediate",
            description = "Strawberry country tour via Wai ghat.",
            routeNotes = "Smooth 4 lane highway followed by Wai twisties.",
            plannedStops = listOf("Shirwal Hub", "Wai Ghat Foot", "Panchgani Tableland"),
            meetingPoint = "Chandani Chowk BPCL, Pune",
            fuelNotes = "Good pumps at Shirwal",
            foodNotes = "Mapro Garden breakfast",
            rules = listOf("Helmets mandatory"),
            distanceKm = 120,
            durationHours = 2.5
        )

        assertNotNull(ride.id)
        val activeRides = repository.rides.value.filter { it.status == RideStatus.ACTIVE }
        assertTrue(activeRides.any { it.id == ride.id })
    }

    @Test
    fun testC_and_D_joinRequestAndOrganizerApprovalWorkflow() {
        // Find existing Goa ride hosted by Vikram
        val ride = repository.rides.value.find { it.id == "ride_nashik_goa" }!!
        val initialConfirmed = ride.confirmedRiderIds.size

        // Rahul requests to join
        repository.switchUser("user_rahul")
        // Rahul already has request in seed, let's switch to Ananya
        repository.switchUser("user_ananya")
        val success = repository.requestToJoinRide(ride.id, "Hey Vikram, would love to join with my GS310!")
        assertTrue(success)

        // Verify request is pending
        val req = repository.requests.value.find { it.rideId == ride.id && it.riderId == "user_ananya" }
        assertNotNull(req)
        assertEquals(RequestStatus.PENDING, req!!.status)

        // Vikram (Host) accepts request
        repository.switchUser("user_vikram")
        val accepted = repository.acceptJoinRequest(req.id)
        assertTrue(accepted)

        // Verify updated status and participant list
        val updatedRide = repository.rides.value.find { it.id == ride.id }!!
        assertTrue(updatedRide.confirmedRiderIds.contains("user_ananya"))
        assertEquals(initialConfirmed + 1, updatedRide.confirmedRiderIds.size)
    }

    @Test
    fun testK_capacityEnforcementPreventsOverbooking() {
        repository.switchUser("user_vikram")
        val ride = repository.createRide(
            title = "Solo Buddy Ride",
            origin = "Mumbai",
            destination = "Goa",
            departureDate = "2026-11-20",
            departureTime = "05:00 AM",
            capacity = 1, // Only 1 spot total (host fills it)
            visibility = RideVisibility.PUBLIC,
            tripType = "Express",
            pace = "Brisk",
            experienceLevel = "Veteran",
            description = "Quick run",
            routeNotes = "NH66",
            plannedStops = emptyList(),
            meetingPoint = "Vashi",
            fuelNotes = "",
            foodNotes = "",
            rules = emptyList(),
            distanceKm = 580,
            durationHours = 10.0
        )

        // Confirmed rider size is 1, capacity is 1 -> Available is 0
        repository.switchUser("user_rahul")
        val canRequest = repository.requestToJoinRide(ride.id, "Can I join?")
        assertFalse("Should not allow join request when ride is at capacity", canRequest)
    }

    @Test
    fun testL_cancelledRideExcludedFromActive() {
        repository.switchUser("user_vikram")
        val ride = repository.rides.value.find { it.id == "ride_nashik_goa" }!!

        repository.cancelRide(ride.id, "Severe cyclone alert along Konkan coast")

        val updated = repository.rides.value.find { it.id == ride.id }!!
        assertEquals(RideStatus.CANCELLED, updated.status)

        val activeList = repository.rides.value.filter { it.status == RideStatus.ACTIVE }
        assertFalse(activeList.any { it.id == ride.id })
    }

    @Test
    fun testF_and_G_communityJoinAndAdminApproval() {
        // Open community: ADV explorers
        repository.switchUser("user_vikram")
        val advComm = repository.communities.value.find { it.id == "comm_adventure_dualsport" }!!
        assertFalse(advComm.memberIds.contains("user_vikram"))

        repository.toggleJoinCommunity(advComm.id)
        val updatedAdv = repository.communities.value.find { it.id == advComm.id }!!
        assertTrue(updatedAdv.memberIds.contains("user_vikram"))

        // Restricted community: Nashik Bikers (Approval required)
        repository.switchUser("user_ananya")
        val restrictedComm = repository.communities.value.find { it.id == "comm_nashik_riders" }!!
        repository.toggleJoinCommunity(restrictedComm.id)

        val pendingComm = repository.communities.value.find { it.id == restrictedComm.id }!!
        assertTrue(pendingComm.pendingMemberIds.contains("user_ananya"))
        assertFalse(pendingComm.memberIds.contains("user_ananya"))

        // Admin Vikram approves Ananya
        repository.switchUser("user_vikram")
        repository.approveCommunityMember(restrictedComm.id, "user_ananya")

        val approvedComm = repository.communities.value.find { it.id == restrictedComm.id }!!
        assertTrue(approvedComm.memberIds.contains("user_ananya"))
    }

    @Test
    fun testH_eventRsvpStatus() {
        repository.switchUser("user_ananya")
        val event = repository.events.value.find { it.id == "evt_1" }!!
        assertFalse(event.goingUserIds.contains("user_ananya"))

        repository.rsvpEvent(event.id, true)
        val updated = repository.events.value.find { it.id == event.id }!!
        assertTrue(updated.goingUserIds.contains("user_ananya"))

        // Decline RSVP
        repository.rsvpEvent(event.id, false)
        val declined = repository.events.value.find { it.id == event.id }!!
        assertFalse(declined.goingUserIds.contains("user_ananya"))
        assertTrue(declined.notGoingUserIds.contains("user_ananya"))
    }

    @Test
    fun testI_blockedUserMessagingRestrictions() {
        repository.switchUser("user_vikram")
        // Vikram blocks Rahul
        repository.blockUser("user_rahul")
        assertTrue(repository.blockedUserIds.value.contains("user_rahul"))

        // Rahul attempts to send message to Vikram in direct conversation
        repository.switchUser("user_rahul")
        val directConv = repository.conversations.value.find { it.id == "conv_direct_rahul_vikram" }!!
        val messageSent = repository.sendMessage(directConv.id, "Hey Vikram, are you there?")
        assertFalse("Message from blocked user must be blocked by repository rules", messageSent)
    }

    @Test
    fun testJ_reportSubmissionAndAdminResolution() {
        repository.switchUser("user_rahul")
        val report = repository.submitReport(
            targetType = ReportTargetType.RIDE,
            targetId = "ride_nashik_goa",
            targetTitle = "Nashik to Goa Coastal",
            reportedUserId = "user_vikram",
            reason = "Misleading route info",
            details = "Route description mentions open expressway but road is under construction."
        )

        assertNotNull(report.id)
        assertEquals(ReportStatus.PENDING, report.status)

        // Admin Neha resolves the report
        repository.switchUser("user_neha")
        repository.resolveReport(report.id, ReportStatus.RESOLVED_WARNING_ISSUED, "Contacted organizer to clarify route.")

        val resolved = repository.reports.value.find { it.id == report.id }!!
        assertEquals(ReportStatus.RESOLVED_WARNING_ISSUED, resolved.status)
        assertEquals("Contacted organizer to clarify route.", resolved.adminNotes)
    }
}
