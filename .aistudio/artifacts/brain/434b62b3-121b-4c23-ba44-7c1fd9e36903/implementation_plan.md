# NEO — Motorcycle Biker Community Application

NEO is a modern, premium motorcycle touring and community platform designed for riders to find trip partners, discover local biker communities, organize group rides, and match routes in real time.

## User Review & Critical Decisions

> [!IMPORTANT]
> The implementation plan synthesizes the full scope of NEO, including the data layer architecture, authentication model, and visual identity.

- **Confirmed Architecture**: Kotlin + Jetpack Compose with Material Design 3, MVVM pattern, Navigation Compose, and a unified dark charcoal/electric orange visual identity.
- **Confirmed Data Strategy**: Firebase Firestore integration via `NeoFirestoreManager` / `NeoRepository` for real-time synchronization of rides, join requests, rider profiles, communities, and events.
- **Confirmed Auth & Security**: Google Sign-In via Jetpack `CredentialManager` backed by Firestore zero-trust security rules (`firestore.rules`).

---

## 1. Overview & Core Concept

NEO solves the isolation of solo motorcycle touring by matching riders traveling along compatible routes, dates, and riding styles.

- **Target Persona**: Motorcycle enthusiasts, long-distance tourers, weekend group riders, and local biker club organizers.
- **Key Value**:
  1. **Smart Route Matching**: Connects riders traveling along overlapping route segments (e.g. Pune to Goa & Nashik to Goa).
  2. **Meet-in-the-Middle Finder**: Suggests accessible meeting points/checkpoints along shared route segments.
  3. **Group Ride & Community Hub**: Comprehensive management for open rides, join requests, community clubs, and motorcycle events.

---

## 2. User Experience & Visual Design

### Aesthetic & Palette
- **Main Surface / Atmosphere**: Dark Charcoal (`#121212` / `#1E1E1E`) providing a sleek, nocturnal automotive dashboard aesthetic.
- **Primary Accent**: Electric Vivid Orange (`#FF5722` / `#FF7043`) for high-contrast primary actions, route lines, and active indicators.
- **Secondary Surfaces & Cards**: Charcoal Grey (`#2A2A2A`) with subtle border elevation (`#383838`) and M3 tonal elevation.
- **Text & Hierarchy**: High-contrast white (`#FFFFFF`) for primary titles, neutral grey (`#B0B0B0`) for secondary metadata and timestamps.

### Canonical Screens & Navigation Structure
```
┌────────────────────────────────────────────────────────────────────────┐
│                              NEO APP                                   │
├─────────────┬─────────────┬─────────────┬──────────────┬───────────────┤
│    HOME     │  RIDE FIND  │ CREATE RIDE │ COMMUNITIES  │  MY PROFILE   │
│  Dashboard  │ & Route Match│   Planner   │  & Events    │ & Ride Passport│
└─────────────┴─────────────┴─────────────┴──────────────┴───────────────┘
```

1. **Onboarding & Auth Gate**: Welcome hero, Google Sign-In via Credential Manager, and rider profile builder (bike model, pace, interests).
2. **Home Dashboard**: Personalized greeting, quick action cards (Explore Rides, Meet-in-Middle, Create Ride), recommended trips, and activity feed.
3. **Smart Route Match & Finder**: Origin/Destination input, date window filters, riding pace selectors, and matching results with shared segment breakdown.
4. **Ride Detail & Join Flow**: Comprehensive route summary, planned stops, organizer card, and real-time request management for organizers.
5. **Meet-in-the-Middle Tool**: Interactive checkpoint finder suggesting mid-point meeting spots for riders joining mid-route.
6. **Community & Events Directory**: City biker clubs, weekend circles, RSVP management, and discussion feed.
7. **Ride Passport & Profile**: Personal ride statistics, completed tour history, badges, and bike garage specs.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Real-time Snapshot Flows vs. Polling**:
  - *Chosen*: Use Kotlin `Flow` with `Query.snapshots()` in `NeoFirestoreManager`.
  - *Why*: Instant UI updates when join requests are accepted or ride details change without manual pull-to-refresh.
- **Decision 2: Comprehensive Ride Data Model (`Ride.kt`)**:
  - *Chosen*: Rich model including origin, destination, meeting points, capacity, trip type, pace, stops, and participant IDs.
  - *Why*: Supports both simple point-to-point rides and multi-stop touring itineraries with capacity enforcement.
- **Decision 3: Zero-Trust Security Rules (`firestore.rules`)**:
  - *Chosen*: Strict validation asserting `request.auth != null`, participant limits, and organizer ownership for write operations.

---

## 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Jetpack Compose UI Layer                         │
│   [HomeScreen]  [RideSearchScreen]  [CreateRideScreen]  [CommunityScreen] │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         NEO ViewModel & State Flow                        │
│             NeoViewModel (StateFlow<UiState<T>>, Navigation)             │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     NeoRepository / NeoFirestoreManager                  │
│   - observeActiveRides(): Flow<List<Ride>>                              │
│   - createRide(ride: Ride): Result<Unit>                                 │
│   - submitJoinRequest(requestId, rideId): Result<Unit>                   │
│   - observeUserCommunities(): Flow<List<Community>>                      │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                           Firebase Firestore DB                          │
│   /rides/{rideId}  /users/{userId}  /communities/{id}  /requests/{id}    │
└──────────────────────────────────────────────────────────────────────────┘
```

### Primary Data Entities

1. **`Ride.kt`**:
   - `id: String`, `title: String`, `origin: String`, `destination: String`
   - `departureDate: String`, `departureTime: String`, `meetingPoint: String`
   - `organizerId: String`, `organizerName: String`, `organizerAvatar: String`
   - `capacity: Int`, `confirmedParticipants: List<String>`, `pace: String`
   - `tripType: String`, `status: String` ("active", "completed", "cancelled")
   - `plannedStops: List<String>`, `createdAt: Timestamp?`

2. **`RiderProfile.kt`**:
   - `userId: String`, `displayName: String`, `profilePicUrl: String`
   - `bikeModel: String`, `city: String`, `ridingPace: String`, `ridingInterests: List<String>`

3. **`RideRequest.kt`**:
   - `id: String`, `rideId: String`, `riderId: String`, `riderName: String`
   - `status: String` ("pending", "accepted", "declined"), `timestamp: Timestamp?`

4. **`Community.kt` & `Event.kt`**:
   - Biker circles, location details, member count, event schedules, and RSVPs.

---

## Next Steps Upon Approval

1. **Data Model & Firestore Manager**: Verify and update `Ride.kt` and `NeoFirestoreManager.kt` to ensure complete CRUD operations, real-time snapshot flows, and proper exception handling.
2. **Security & Blueprint**: Write `firebase-blueprint.json` and `firestore.rules` for zero-trust Firestore access control.
3. **UI Polish & Navigation**: Enhance screen composables with Skeleton loaders, charcoal/orange aesthetic, and robust error/empty states.
4. **Verification**: Run `compile_applet` and Gradle test suite (`testDebugUnitTest`) to ensure zero compilation or runtime errors.
