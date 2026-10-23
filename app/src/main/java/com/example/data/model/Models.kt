package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val SUPER_ADMIN_UID = "YNxF7YAv0XYqkZHgaX6mtxAbG103"

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val inGameId: String = "",
    val teamName: String = "",
    val photoUrl: String = "",
    val role: String = "player", // "player", "admin", "super_admin"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isSuperAdmin(): Boolean = uid == SUPER_ADMIN_UID || role == "super_admin"
    fun isAdmin(): Boolean = isSuperAdmin() || role == "admin"
}

@IgnoreExtraProperties
data class Tournament(
    @DocumentId val id: String = "",
    val title: String = "",
    val gameType: String = "DLS 24", // e.g. DLS, FIFA Mobile, Real Football
    val entryFee: Double = 0.0,
    val prizePool: Double = 0.0,
    val startDate: String = "",
    val totalSlots: Int = 16,
    val registeredCount: Int = 0,
    val status: String = "open", // "open", "ongoing", "completed"
    val bannerUrl: String = "",
    val rules: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Registration(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val userId: String = "",
    val playerName: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val inGameId: String = "",
    val teamName: String = "",
    val paymentMethod: String = "bKash", // bKash, Nagad, Rocket
    val senderPhone: String = "",
    val transactionId: String = "",
    val amount: Double = 0.0,
    val status: String = "pending", // "pending", "approved", "rejected"
    @ServerTimestamp val registeredAt: Timestamp? = null,
    val verifiedAt: Timestamp? = null,
    val verifiedBy: String? = null
)

@IgnoreExtraProperties
data class MatchFixture(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val teamALogo: String = "",
    val teamBLogo: String = "",
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val matchDate: String = "",
    val round: String = "Group Stage",
    val status: String = "upcoming", // "upcoming", "live", "finished"
    val winner: String = "",
    val liveMinute: String = "0'",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TournamentStanding(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val teamName: String = "",
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0
)

@IgnoreExtraProperties
data class TopScorer(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val playerName: String = "",
    val teamName: String = "",
    val goals: Int = 0,
    val matches: Int = 0
)

@IgnoreExtraProperties
data class TournamentNotice(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "Announcement", // Announcement, Rules, Schedule, Winner
    val isUrgent: Boolean = false,
    val authorName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "", // specific uid or "all"
    val title: String = "",
    val message: String = "",
    val type: String = "general", // "approval", "match", "score", "notice", "payment"
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PaymentRecord(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val registrationId: String = "",
    val userId: String = "",
    val playerName: String = "",
    val method: String = "bKash",
    val senderNumber: String = "",
    val trxId: String = "",
    val amount: Double = 0.0,
    val type: String = "entry_fee", // entry_fee, prize_distribution, expense
    val status: String = "pending", // pending, verified, rejected
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class FinanceSummary(
    val totalCollected: Double = 0.0,
    val totalPrizeDistributed: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalPlayers: Int = 0,
    val pendingRegistrations: Int = 0,
    val activeTournaments: Int = 0,
    val upcomingMatches: Int = 0
)

@IgnoreExtraProperties
data class AppConfig(
    val isAppActive: Boolean = true,
    val monthlyOfferTitle: String = "",
    val monthlyOfferDetails: String = "",
    val bonusAmount: String = "0",
    val noticeMessage: String = ""
)
