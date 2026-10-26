package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Ride
import com.example.data.model.RideStatus
import com.example.data.model.RideVisibility
import com.example.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NeoFirestoreManager(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "NeoFirestoreManager"

        fun create(context: Context): NeoFirestoreManager {
            val databaseId = try {
                context.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                Log.w(TAG, "firestore_database_id resource not found", e)
                ""
            }

            val db = if (databaseId.isNotBlank()) {
                FirebaseFirestore.getInstance(databaseId)
            } else {
                FirebaseFirestore.getInstance()
            }
            return NeoFirestoreManager(db)
        }
    }

    private val auth = Firebase.auth

    val currentAuthUser get() = auth.currentUser

    // --- User Profile Cloud Storage ---
    suspend fun saveUserProfile(user: User): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.d(TAG, "No authenticated Firebase user present; skipping cloud profile sync.")
            return Result.success(Unit)
        }
        return try {
            val userMap = hashMapOf(
                "userId" to uid,
                "name" to user.name,
                "username" to user.username,
                "avatarEmoji" to user.avatarEmoji,
                "bio" to user.bio,
                "city" to user.city,
                "motorcycle" to user.motorcycle,
                "experienceLevel" to user.experienceLevel,
                "pacePreference" to user.pacePreference,
                "isContactVerified" to user.isContactVerified,
                "isAdmin" to user.isAdmin,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(uid)
                .set(userMap, SetOptions.merge())
                .await()

            Log.d(TAG, "User profile saved successfully to Firestore: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Notice saving user profile to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<User?> {
        return firestore.collection("users")
            .document(userId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) return@map null
                User(
                    id = snapshot.getString("userId") ?: userId,
                    name = snapshot.getString("name") ?: "",
                    username = snapshot.getString("username") ?: "",
                    avatarEmoji = snapshot.getString("avatarEmoji") ?: "🏍️",
                    bio = snapshot.getString("bio") ?: "",
                    city = snapshot.getString("city") ?: "",
                    motorcycle = snapshot.getString("motorcycle") ?: "",
                    experienceLevel = snapshot.getString("experienceLevel") ?: "Intermediate",
                    pacePreference = snapshot.getString("pacePreference") ?: "Balanced (80-100 km/h)",
                    isContactVerified = snapshot.getBoolean("isContactVerified") ?: true,
                    isAdmin = snapshot.getBoolean("isAdmin") ?: false
                )
            }
            .catch { e ->
                Log.d(TAG, "User profile query finished or unavailable: ${e.message}")
                emit(null)
            }
    }

    // --- Ride Cloud Storage ---
    suspend fun saveRide(ride: Ride): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.d(TAG, "No authenticated Firebase user present; ride retained in local repository.")
            return Result.success(Unit)
        }
        return try {
            val rideMap = hashMapOf(
                "id" to ride.id,
                "title" to ride.title,
                "origin" to ride.origin,
                "destination" to ride.destination,
                "departureDate" to ride.departureDate,
                "departureTime" to ride.departureTime,
                "capacity" to ride.capacity,
                "confirmedRiderIds" to ride.confirmedRiderIds,
                "pendingRequestRiderIds" to ride.pendingRequestRiderIds,
                "visibility" to ride.visibility.name,
                "status" to ride.status.name,
                "tripType" to ride.tripType,
                "pace" to ride.pace,
                "experienceLevel" to ride.experienceLevel,
                "description" to ride.description,
                "routeNotes" to ride.routeNotes,
                "plannedStops" to ride.plannedStops,
                "meetingPoint" to ride.meetingPoint,
                "fuelBreakNotes" to ride.fuelBreakNotes,
                "foodBreakNotes" to ride.foodBreakNotes,
                "distanceKm" to ride.distanceKm,
                "durationHours" to ride.durationHours,
                "organizerId" to uid,
                "organizerName" to ride.organizerName,
                "organizerAvatar" to ride.organizerAvatar,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("rides")
                .document(ride.id)
                .set(rideMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Ride saved successfully to Firestore: ${ride.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Notice saving ride to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeActiveRides(): Flow<List<Ride>> {
        return firestore.collection("rides")
            .whereEqualTo("status", "ACTIVE")
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Ride(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            origin = doc.getString("origin") ?: "",
                            destination = doc.getString("destination") ?: "",
                            departureDate = doc.getString("departureDate") ?: "",
                            departureTime = doc.getString("departureTime") ?: "",
                            capacity = (doc.getLong("capacity") ?: 4L).toInt(),
                            confirmedRiderIds = (doc.get("confirmedRiderIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            pendingRequestRiderIds = (doc.get("pendingRequestRiderIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            visibility = try {
                                RideVisibility.valueOf(doc.getString("visibility") ?: "PUBLIC")
                            } catch (_: Exception) { RideVisibility.PUBLIC },
                            status = try {
                                RideStatus.valueOf(doc.getString("status") ?: "ACTIVE")
                            } catch (_: Exception) { RideStatus.ACTIVE },
                            tripType = doc.getString("tripType") ?: "Touring",
                            pace = doc.getString("pace") ?: "Balanced (80-100 km/h)",
                            experienceLevel = doc.getString("experienceLevel") ?: "Intermediate",
                            description = doc.getString("description") ?: "",
                            routeNotes = doc.getString("routeNotes") ?: "",
                            plannedStops = (doc.get("plannedStops") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            meetingPoint = doc.getString("meetingPoint") ?: "",
                            fuelBreakNotes = doc.getString("fuelBreakNotes") ?: "",
                            foodBreakNotes = doc.getString("foodBreakNotes") ?: "",
                            distanceKm = (doc.getLong("distanceKm") ?: 0L).toInt(),
                            durationHours = (doc.getDouble("durationHours") ?: 0.0),
                            organizerId = doc.getString("organizerId") ?: "",
                            organizerName = doc.getString("organizerName") ?: "",
                            organizerAvatar = doc.getString("organizerAvatar") ?: "🏍️"
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse ride doc: ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Active rides flow cancelled normally.")
                } else {
                    Log.w(TAG, "Active rides snapshot query exception: ${e.message}")
                }
                emit(emptyList())
            }
    }
}
