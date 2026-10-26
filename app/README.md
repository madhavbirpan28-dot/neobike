# NEO — Biker Community Android Application

**NEO** is a premium, modern biker community application built for motorcycle riders, touring clubs, and outdoor adventure enthusiasts. Designed with a sleek dark charcoal aesthetic (`#0C0E14`, `#1C2230`) and electric orange accents (`#FF6B00`), NEO combines group ride planning, bike maintenance tracking, route matching, community stories, and Google Maps integration into an intuitive mobile experience.

---

## 🌟 Key Features & Capabilities

### 1. 🏍️ My Garage & Maintenance Tracking
- **Multi-Bike Management**: Store and switch between primary and secondary motorcycles.
- **Odometer Logging**: Easily update bike mileage and track total distance.
- **Service History Log**: Track maintenance records (oil changes, brake pad replacements, chain lubing) with costs, dates, and workshop details.
- **Maintenance Reminders**: Priority-coded reminders for upcoming major services, chain waxing, and insurance renewals.

### 2. 🗺️ Google Maps Navigation & Explore Spots
- **Interactive Spot Finder**: Search and filter 24/7 fuel pumps, emergency repair clinics, rider cafes, scenic viewpoints, and meetpoints.
- **Turn-by-Turn Google Maps Intents**: One-tap navigation launching turn-by-turn directions directly in the official Google Maps app.
- **Emergency Support Helplines**: Instant one-tap access to National Highway Patrol (1033), Medical Ambulance (108), and NEO Highway Assistance.

### 3. 🏁 Group Rides & Smart Route Matcher
- **Find & Create Rides**: Filter group tours by trip type, riding pace, origin/destination, and experience level.
- **Capacity Controls**: Automatic rider capacity enforcement and organizer request management (Accept/Decline).
- **Meet-in-the-Middle Route Suggestions**: Recommends optimal meeting spots based on co-riders' starting points.

### 4. 📰 Rider Feed & Community Clubs
- **Community Feed**: Share route conditions, weather warnings, cornering tips, and motorcycle photo stories.
- **Interactions**: Like and comment on posts with real-time updates.
- **Clubs & Events**: Join verified biker circles and stay updated on community rallies and weekend breakfasts.

### 5. 🎖️ Digital Rider Passport & Badges
- **Achievement Badges**: Unlock badges for completing first group rides, scenic mountain passes, and weekend tours.
- **Completed Ride Records**: Preserve ride memories with distance statistics and co-rider counts.

### 6. 🛡️ Safety Shield & Moderation
- **Trusted Contacts**: Pre-ride check-ins and emergency contact notifications.
- **Community Moderation**: Safety report dialogs, content reporting, and admin dashboard for reviewing flagged content.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3 dark theme)
- **State Management**: Jetpack `ViewModel`, `StateFlow`, Kotlin Coroutines
- **Navigation**: Jetpack `Navigation Compose` with type-safe route handling
- **Database & Backend**: Firebase Firestore (cloud sync for user profiles and active rides) & local fallback repository
- **Secrets Management**: Secrets Gradle Plugin using `.env` / `.env.example`

---

## 🔑 Environment & Secrets Configuration

1. Copy `.env.example` to `.env` in the root project directory:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and set your API key:
   ```env
   GOOGLE_MAPS_API_KEY=AIzaSy_YOUR_ACTUAL_GOOGLE_MAPS_KEY
   ```
3. Alternatively, enter secrets in the **Secrets panel in AI Studio**.

---

## 🧪 Testing & Build Verification

- **Compilation**: Verified using `compile_applet` (0 errors).
- **Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

---

*Built with passion for the motorcycle riding community on NEO.*
