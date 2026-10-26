# NEO — Ride Together. Discover More.

NEO is a modern, premium motorcycle rider matching, shared-trip planning, local biker communities, and group ride discovery platform built with Kotlin and Jetpack Compose on Android.

---

## 🏍️ 1. Product Identity

- **App Name**: NEO
- **Tagline**: Ride Together. Discover More.
- **Visual Design**: Dark touring aesthetic with charcoal/midnight surfaces (`#090D16`, `#111827`), restrained teal accents (`#14B8A6`), warm amber highlights (`#F59E0B`), and accessible high-contrast typography.
- **Core Mission**: Solving the rider fragmentation problem by connecting motorcyclists travelling along the same highway corridors, sharing riding paces, organizing weekend breakfast runs, and fostering club communities.

---

## ⚡ 2. Key Implemented Features

### Feature 1: NEO Smart Route & Corridor Matcher
- Multi-factor compatibility matching comparing origin, destination, planned corridor stops, riding pace preference (Relaxed, Balanced, Brisk), and experience level.
- Compatibility score calculation with transparent reasoning (e.g., "Exact destination match", "Compatible pace: 80-100 km/h", "Shared highway segment").
- Partial corridor matching (e.g., Nashik and Pune riders heading to Goa merge at the NH48 corridor).

### Feature 2: Create & Manage Rides
- Full publishing workflow: Ride title, origin, destination, departure date/time, rider capacity (stepper), trip visibility (Public, Community-only, Invite-only), pace, description, fuel break notes, and food stops.
- Server-side capacity enforcement preventing overbooking.
- Organizer controls: Accept/decline join requests, cancel ride with notification, mark ride completed.

### Feature 3: Meet-in-the-Middle Finder
- Automatically detects overlapping routes (e.g., Nashik -> Goa and Pune -> Goa).
- Proposes certified public meeting spots (such as *Highway King BPCL Oasis & Biker Hub at Kolhapur Bypass*), calculates travel distances for both riders, provides sync times, and highlights 24/7 facilities.
- One-tap "Agree on Meeting Point" confirmation.

### Feature 4: Local Biker Communities & Ride Circles
- Discovery directory categorized into Long-Distance Touring, City Clubs, Adventure/Off-Road, and Sunrise Breakfast Riders.
- Open vs. Approval-Required membership workflows.
- Community admin moderation tools: pending membership queue approval, broadcast announcements feed, and club guidelines.

### Feature 5: Rider Profile & Privacy Controls
- Profile management with motorcycle make/model, experience tier, bio, and riding pace.
- Granular privacy toggles: control public visibility of phone number, bio, and bike details.
- Demo Persona Switcher: switch instantly between **Vikram** (Tour Lead), **Rahul** (ADV rider), **Neha** (Admin/Triumph Tiger), and **Ananya** (BMW GS) to test multiple perspectives.

### Feature 6: Ride Buddy Compatibility
- Analyzes travel style, preferred cruising speeds, and dawn departure preferences to give an honest compatibility breakdown.

### Feature 7: Ride Planner & Pre-Ride Safety Checklist
- Interactive pre-ride checklist before turning the ignition: tire cold pressures, chain tension, certified DOT/ECE gear, emergency contact briefing, and offline maps.

### Feature 8: Open Rides & Spontaneous Ride Mode
- Real-time slot availability badges (`slots left`), trip visibility flags, and immediate join requests with personalized rider notes.

### Feature 9: Ride Passport & Achievements
- Lifetime verified touring logbook (total kilometers, completed tours).
- Achievement badges: *First Community Ride*, *Weekend Explorer*, *Scenic Route Explorer*, *Community Contributor*, and *Five Group Rides Veteran*.

### Feature 10: Biker Spot Finder
- Curated directory for 24/7 highway fuel stations, motorcycle repair workshops, biker cafes, and scenic viewpoint turnouts with ratings and amenities.
- Save to favorites toggle.

### Feature 11: Events & RSVP Calendar
- Community group runs, breakfast rides, and safety workshops.
- Real-time RSVP (`Going ✓` / `Decline`) with live attendee counters and host creation dialog.

### Feature 12: In-App Messaging & Security Restrictions
- Direct chats, confirmed ride group chats with **Pinned Ride Details banner**, and community lounges.
- Security restriction: blocked contacts cannot send messages to blockers.

### Feature 13: In-App Notification Center
- Notifications for join requests, host approvals, cancellations, and announcements with unread badge indicators and mark-all-read action.

### Feature 14 & 20: Safety Centre & Trust Controls
- Strict ATGATT (All The Gear All The Time) guidelines, staggered formation rules, and public meeting point safety tips.
- Incident reporting dialog for Rides, Users, Communities, Events, and Messages.
- User blocking and unblocking management.

### Feature 15 & 21: Trip Status Check-In
- Step-by-step progress check-in: *Mark Started*, *Check-in at Stop*, and *Complete Ride* to keep trusted contacts informed without continuous battery-draining GPS tracking.

### Feature 24: Admin Moderation Console
- Role-based access control (RBAC): only authorized administrators (e.g., Neha, Vikram) can access the console.
- Report queue with case ID, violation reason, details, and action buttons (*Dismiss*, *Warn User*, *Remove Content*).
- Platform activity metrics (Total Rides, Active Communities, Open Reports).

---

## 🏗️ 3. Architecture & Tech Stack

- **Platform**: Android (minSdk 24, compileSdk / targetSdk 36)
- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **State Management**: Reactive MVVM with Kotlin Coroutines and `MutableStateFlow` / `StateFlow`
- **Navigation**: Clean state-based screen stack with mandatory Android `BackHandler` integration
- **Data Layer**: Repository pattern (`NeoRepository`) with comprehensive domain validation rules, thread-safe state mutators, and instant demo data reset.

---

## 🧪 4. Testing & Verification

Automated unit and CUJ tests cover all key business rules in `NeoBusinessRulesTest.kt`:
- **Test A**: Profile management and persona switching.
- **Test B**: Ride creation and appearance in search queries.
- **Test C & D**: Join request submission, organizer approval, and slot count decrements.
- **Test K**: Capacity enforcement preventing overbooking.
- **Test L**: Ride cancellation and exclusion from active queries.
- **Test F & G**: Community membership request and admin approval workflow.
- **Test H**: Event RSVP Going and Not Going states.
- **Test I**: Blocked user message suppression.
- **Test J**: Incident report submission and administrator resolution.

To run tests in terminal:
```bash
gradle :app:testDebugUnitTest
```

---

## ⚙️ 5. Cloud Database & External Services

### Firebase Firestore Backend
- **Project**: `gen-lang-client-0209218760`
- **Database ID**: `ai-studio-android-neo-434b62b3-121b-4c23-ba44-7c1fd9e36903`
- **Configuration**: `app/google-services.json` & `res/values/firebase_applet_config.xml`
- **Collections**:
  - `/users/{userId}`: Rider profiles, bikes, pace, and verification status.
  - `/rides/{rideId}`: Published rides, route notes, capacity, and status.
- **Security Rules**: Deployed via `firestore.rules` enforcing user-authenticated access and organizer-only mutations.
- **Data Layer**: `NeoFirestoreManager` provides reactive Flow streams and asynchronous cloud writes with seamless offline/demo data fallback.

### API Keys (AI Studio Secrets Panel)
- `GEMINI_API_KEY`: Injected automatically by AI Studio for Gemini AI Logic features.
- `MAPS_API_KEY`: (Optional) For Google Maps Platform live geocoding & route geometry.
- `WEATHER_API_KEY`: (Optional) For real-time mountain pass weather updates.
