package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AppNotification
import com.example.data.model.MatchFixture
import com.example.data.model.PaymentRecord
import com.example.data.model.Registration
import com.example.data.model.SUPER_ADMIN_UID
import com.example.data.model.TopScorer
import com.example.data.model.Tournament
import com.example.data.model.TournamentNotice
import com.example.data.model.TournamentStanding
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TournamentRepository(context: Context) {
    private val TAG = "TournamentRepo"

    val db: FirebaseFirestore = try {
        val dbIdRes = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        if (dbIdRes != 0) {
            val dbId = context.getString(dbIdRes)
            if (dbId.isNotBlank() && dbId != "(default)") {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } else {
            FirebaseFirestore.getInstance()
        }
    } catch (e: Exception) {
        FirebaseFirestore.getInstance()
    }

    val auth: FirebaseAuth = Firebase.auth

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- USER PROFILE ---
    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing user profile: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                val finalRole = if (uid == SUPER_ADMIN_UID) "super_admin" else (profile?.role ?: "player")
                trySend(profile?.copy(role = finalRole))
            } else {
                val fallbackRole = if (uid == SUPER_ADMIN_UID) "super_admin" else "player"
                trySend(UserProfile(uid = uid, role = fallbackRole))
            }
        }
        awaitClose { listener.remove() }
    }.catch { e ->
        Log.e(TAG, "observeUserProfile failed", e)
        emit(null)
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val role = if (profile.uid == SUPER_ADMIN_UID) "super_admin" else profile.role
        val data = mapOf(
            "uid" to profile.uid,
            "name" to profile.name,
            "email" to profile.email,
            "phone" to profile.phone,
            "whatsapp" to profile.whatsapp,
            "inGameId" to profile.inGameId,
            "teamName" to profile.teamName,
            "photoUrl" to profile.photoUrl,
            "role" to role,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(profile.uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing users: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(UserProfile::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun updateUserRole(uid: String, newRole: String): Result<Unit> = runCatching {
        db.collection("users").document(uid)
            .update("role", newRole, "updatedAt", FieldValue.serverTimestamp())
            .await()
    }

    // --- TOURNAMENTS ---
    fun observeTournaments(): Flow<List<Tournament>> = callbackFlow {
        val listener = db.collection("tournaments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing tournaments: ${error.message}")
                    // Fallback without orderBy in case of unindexed documents
                    db.collection("tournaments").get().addOnSuccessListener { s ->
                        val items = s.documents.mapNotNull { it.toObject(Tournament::class.java) }
                        trySend(items)
                    }.addOnFailureListener {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                val tournaments = snapshot?.documents?.mapNotNull { it.toObject(Tournament::class.java) } ?: emptyList()
                trySend(tournaments)
            }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    fun observeTournament(tournamentId: String): Flow<Tournament?> = callbackFlow {
        val listener = db.collection("tournaments").document(tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing tournament: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Tournament::class.java))
            }
        awaitClose { listener.remove() }
    }.catch { emit(null) }

    suspend fun createTournament(tournament: Tournament): Result<String> = runCatching {
        val docRef = db.collection("tournaments").document()
        val data = mapOf(
            "id" to docRef.id,
            "title" to tournament.title,
            "gameType" to tournament.gameType,
            "entryFee" to tournament.entryFee,
            "prizePool" to tournament.prizePool,
            "startDate" to tournament.startDate,
            "totalSlots" to tournament.totalSlots,
            "registeredCount" to 0,
            "status" to tournament.status,
            "bannerUrl" to tournament.bannerUrl,
            "rules" to tournament.rules,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        docRef.id
    }

    suspend fun updateTournament(tournament: Tournament): Result<Unit> = runCatching {
        db.collection("tournaments").document(tournament.id)
            .set(
                mapOf(
                    "title" to tournament.title,
                    "gameType" to tournament.gameType,
                    "entryFee" to tournament.entryFee,
                    "prizePool" to tournament.prizePool,
                    "startDate" to tournament.startDate,
                    "totalSlots" to tournament.totalSlots,
                    "status" to tournament.status,
                    "bannerUrl" to tournament.bannerUrl,
                    "rules" to tournament.rules,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
    }

    suspend fun deleteTournament(id: String): Result<Unit> = runCatching {
        db.collection("tournaments").document(id).delete().await()
    }

    // --- REGISTRATIONS ---
    fun observeUserRegistrations(userId: String): Flow<List<Registration>> = callbackFlow {
        val listener = db.collection("registrations")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing user registrations: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Registration::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    fun observeAllRegistrations(): Flow<List<Registration>> = callbackFlow {
        val listener = db.collection("registrations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing all registrations: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Registration::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun submitRegistration(reg: Registration): Result<String> = runCatching {
        val docRef = db.collection("registrations").document()
        val data = mapOf(
            "id" to docRef.id,
            "tournamentId" to reg.tournamentId,
            "tournamentTitle" to reg.tournamentTitle,
            "userId" to reg.userId,
            "playerName" to reg.playerName,
            "phone" to reg.phone,
            "whatsapp" to reg.whatsapp,
            "inGameId" to reg.inGameId,
            "teamName" to reg.teamName,
            "paymentMethod" to reg.paymentMethod,
            "senderPhone" to reg.senderPhone,
            "transactionId" to reg.transactionId,
            "amount" to reg.amount,
            "status" to "pending",
            "registeredAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()

        // Also add pending payment record for finance tracking
        val paymentRef = db.collection("payments").document()
        paymentRef.set(
            mapOf(
                "id" to paymentRef.id,
                "tournamentId" to reg.tournamentId,
                "registrationId" to docRef.id,
                "userId" to reg.userId,
                "playerName" to reg.playerName,
                "method" to reg.paymentMethod,
                "senderNumber" to reg.senderPhone,
                "trxId" to reg.transactionId,
                "amount" to reg.amount,
                "type" to "entry_fee",
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Send confirmation notification to user
        sendNotification(
            AppNotification(
                userId = reg.userId,
                title = "নিবন্ধন জমা হয়েছে",
                message = "${reg.tournamentTitle} টুর্নামেন্টে আপনার নিবন্ধন ও ট্রানজেকশন ID (${reg.transactionId}) যাচাইকরণ প্রক্রিয়াধীন রয়েছে।",
                type = "payment"
            )
        )

        docRef.id
    }

    suspend fun updateRegistrationStatus(
        registration: Registration,
        newStatus: String,
        adminName: String
    ): Result<Unit> = runCatching {
        db.collection("registrations").document(registration.id)
            .update(
                mapOf(
                    "status" to newStatus,
                    "verifiedAt" to FieldValue.serverTimestamp(),
                    "verifiedBy" to adminName
                )
            ).await()

        // Update corresponding payment status
        val paymentSnap = db.collection("payments")
            .whereEqualTo("registrationId", registration.id)
            .get().await()
        for (doc in paymentSnap.documents) {
            val paymentStatus = if (newStatus == "approved") "verified" else "rejected"
            doc.reference.update("status", paymentStatus).await()
        }

        // If approved, increment registered count in tournament
        if (newStatus == "approved") {
            db.collection("tournaments").document(registration.tournamentId)
                .update("registeredCount", FieldValue.increment(1))
                .await()
        }

        // Send Notification to user
        val title = if (newStatus == "approved") "নিবন্ধন অনুমোদিত হয়েছে! 🎉" else "নিবন্ধন বাতিল হয়েছে"
        val msg = if (newStatus == "approved") {
            "অভিনন্দন! ${registration.tournamentTitle} টুর্নামেন্টে আপনার নিবন্ধন সফলভাবে অনুমোদিত হয়েছে।"
        } else {
            "দুঃখিত, আপনার দেওয়া পেমেন্ট তথ্যের অসঙ্গতির কারণে নিবন্ধন বাতিল করা হয়েছে।"
        }
        sendNotification(
            AppNotification(
                userId = registration.userId,
                title = title,
                message = msg,
                type = "approval"
            )
        )
    }

    // --- MATCHES & FIXTURES ---
    fun observeMatches(tournamentId: String? = null): Flow<List<MatchFixture>> = callbackFlow {
        val query = if (tournamentId != null && tournamentId.isNotBlank()) {
            db.collection("matches").whereEqualTo("tournamentId", tournamentId)
        } else {
            db.collection("matches")
        }
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing matches: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val matches = snapshot?.documents?.mapNotNull { it.toObject(MatchFixture::class.java) } ?: emptyList()
            trySend(matches)
        }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun createMatch(match: MatchFixture): Result<String> = runCatching {
        val docRef = db.collection("matches").document()
        val data = mapOf(
            "id" to docRef.id,
            "tournamentId" to match.tournamentId,
            "tournamentTitle" to match.tournamentTitle,
            "teamA" to match.teamA,
            "teamB" to match.teamB,
            "teamALogo" to match.teamALogo,
            "teamBLogo" to match.teamBLogo,
            "scoreA" to match.scoreA,
            "scoreB" to match.scoreB,
            "matchDate" to match.matchDate,
            "round" to match.round,
            "status" to match.status,
            "winner" to match.winner,
            "liveMinute" to match.liveMinute,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()

        // Send match alert notification
        sendNotification(
            AppNotification(
                userId = "all",
                title = "নতুন ম্যাচের সময়সূচী ⚽",
                message = "${match.tournamentTitle}: ${match.teamA} বনাম ${match.teamB} - ${match.matchDate}",
                type = "match"
            )
        )

        docRef.id
    }

    suspend fun updateMatch(match: MatchFixture): Result<Unit> = runCatching {
        db.collection("matches").document(match.id)
            .set(
                mapOf(
                    "scoreA" to match.scoreA,
                    "scoreB" to match.scoreB,
                    "liveMinute" to match.liveMinute,
                    "status" to match.status,
                    "winner" to match.winner,
                    "matchDate" to match.matchDate,
                    "round" to match.round,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

        if (match.status == "live") {
            sendNotification(
                AppNotification(
                    userId = "all",
                    title = "লাইভ স্কোর আপডেট: ${match.teamA} ${match.scoreA} - ${match.scoreB} ${match.teamB}",
                    message = "${match.tournamentTitle} (${match.liveMinute})",
                    type = "score"
                )
            )
        }
    }

    suspend fun deleteMatch(matchId: String): Result<Unit> = runCatching {
        db.collection("matches").document(matchId).delete().await()
    }

    // --- STANDINGS & SCORERS ---
    fun observeStandings(tournamentId: String): Flow<List<TournamentStanding>> = callbackFlow {
        val listener = db.collection("standings")
            .whereEqualTo("tournamentId", tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing standings: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(TournamentStanding::class.java) }
                    ?.sortedByDescending { it.points * 100 + it.goalDifference } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun saveStanding(standing: TournamentStanding): Result<Unit> = runCatching {
        val docRef = if (standing.id.isNotBlank()) db.collection("standings").document(standing.id)
                     else db.collection("standings").document()
        val data = mapOf(
            "id" to docRef.id,
            "tournamentId" to standing.tournamentId,
            "teamName" to standing.teamName,
            "played" to standing.played,
            "won" to standing.won,
            "drawn" to standing.drawn,
            "lost" to standing.lost,
            "goalsFor" to standing.goalsFor,
            "goalsAgainst" to standing.goalsAgainst,
            "goalDifference" to (standing.goalsFor - standing.goalsAgainst),
            "points" to standing.points
        )
        docRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    fun observeScorers(tournamentId: String): Flow<List<TopScorer>> = callbackFlow {
        val listener = db.collection("scorers")
            .whereEqualTo("tournamentId", tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing scorers: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(TopScorer::class.java) }
                    ?.sortedByDescending { it.goals } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun saveScorer(scorer: TopScorer): Result<Unit> = runCatching {
        val docRef = if (scorer.id.isNotBlank()) db.collection("scorers").document(scorer.id)
                     else db.collection("scorers").document()
        val data = mapOf(
            "id" to docRef.id,
            "tournamentId" to scorer.tournamentId,
            "playerName" to scorer.playerName,
            "teamName" to scorer.teamName,
            "goals" to scorer.goals,
            "matches" to scorer.matches
        )
        docRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // --- PAYMENTS & FINANCE ---
    fun observePayments(): Flow<List<PaymentRecord>> = callbackFlow {
        val listener = db.collection("payments").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing payments: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(PaymentRecord::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun addFinancialRecord(
        tournamentId: String,
        amount: Double,
        type: String, // "prize_distribution", "expense"
        description: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("payments").document()
        val data = mapOf(
            "id" to docRef.id,
            "tournamentId" to tournamentId,
            "registrationId" to "",
            "userId" to (currentUserId ?: ""),
            "playerName" to description,
            "method" to "Direct",
            "senderNumber" to "",
            "trxId" to "EXP-${System.currentTimeMillis() % 100000}",
            "amount" to amount,
            "type" to type,
            "status" to "verified",
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
    }

    // --- NOTICES ---
    fun observeNotices(): Flow<List<TournamentNotice>> = callbackFlow {
        val listener = db.collection("notices").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing notices: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(TournamentNotice::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun createNotice(notice: TournamentNotice): Result<String> = runCatching {
        val docRef = db.collection("notices").document()
        val data = mapOf(
            "id" to docRef.id,
            "title" to notice.title,
            "content" to notice.content,
            "category" to notice.category,
            "isUrgent" to notice.isUrgent,
            "authorName" to notice.authorName,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()

        sendNotification(
            AppNotification(
                userId = "all",
                title = "বিজ্ঞপ্তি: ${notice.title}",
                message = notice.content.take(80) + if (notice.content.length > 80) "..." else "",
                type = "notice"
            )
        )
        docRef.id
    }

    suspend fun deleteNotice(noticeId: String): Result<Unit> = runCatching {
        db.collection("notices").document(noticeId).delete().await()
    }

    // --- NOTIFICATIONS ---
    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = db.collection("notifications")
            .whereIn("userId", listOf(userId, "all"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing notifications: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(AppNotification::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.catch { emit(emptyList()) }

    suspend fun sendNotification(notification: AppNotification): Result<Unit> = runCatching {
        val docRef = db.collection("notifications").document()
        val data = mapOf(
            "id" to docRef.id,
            "userId" to notification.userId,
            "title" to notification.title,
            "message" to notification.message,
            "type" to notification.type,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
    }

    suspend fun markNotificationRead(id: String): Result<Unit> = runCatching {
        db.collection("notifications").document(id).update("read", true).await()
    }

    // --- APP CONFIGURATION & CONTROL ---
    fun observeAppConfig(): Flow<com.example.data.model.AppConfig> = callbackFlow {
        val listener = db.collection("app_config").document("settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing app config: ${error.message}")
                    trySend(com.example.data.model.AppConfig())
                    return@addSnapshotListener
                }
                val config = snapshot?.toObject(com.example.data.model.AppConfig::class.java) ?: com.example.data.model.AppConfig()
                trySend(config)
            }
        awaitClose { listener.remove() }
    }.catch { emit(com.example.data.model.AppConfig()) }

    suspend fun saveAppConfig(config: com.example.data.model.AppConfig): Result<Unit> = runCatching {
        db.collection("app_config").document("settings")
            .set(config, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }
}
