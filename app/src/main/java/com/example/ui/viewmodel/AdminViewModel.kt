package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FinanceSummary
import com.example.data.model.MatchFixture
import com.example.data.model.PaymentRecord
import com.example.data.model.Registration
import com.example.data.model.TopScorer
import com.example.data.model.Tournament
import com.example.data.model.TournamentNotice
import com.example.data.model.TournamentStanding
import com.example.data.model.UserProfile
import com.example.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: TournamentRepository
) : ViewModel() {

    val allTournaments: StateFlow<List<Tournament>> = repository.observeTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRegistrations: StateFlow<List<Registration>> = repository.observeAllRegistrations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchFixture>> = repository.observeMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentRecord>> = repository.observePayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserProfile>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<TournamentNotice>> = repository.observeNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial & Stats calculation
    val financeSummary: StateFlow<FinanceSummary> = combine(
        allPayments,
        allRegistrations,
        allTournaments,
        allMatches,
        allUsers
    ) { payments, regs, tournaments, matches, users ->
        val collected = payments.filter { it.type == "entry_fee" && it.status == "verified" }
            .sumOf { it.amount }
        val prizeDistributed = payments.filter { it.type == "prize_distribution" && it.status == "verified" }
            .sumOf { it.amount }
        val expenses = payments.filter { it.type == "expense" && it.status == "verified" }
            .sumOf { it.amount }
        val balance = collected - prizeDistributed - expenses

        val pendingRegs = regs.count { it.status == "pending" }
        val activeTourneys = tournaments.count { it.status == "open" || it.status == "ongoing" }
        val upcomingM = matches.count { it.status == "upcoming" || it.status == "live" }

        FinanceSummary(
            totalCollected = collected,
            totalPrizeDistributed = prizeDistributed,
            totalExpense = expenses,
            netBalance = balance,
            totalPlayers = users.size,
            pendingRegistrations = pendingRegs,
            activeTournaments = activeTourneys,
            upcomingMatches = upcomingM
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceSummary())

    // --- Tournament Management ---
    fun createTournament(
        title: String,
        gameType: String,
        entryFee: Double,
        prizePool: Double,
        startDate: String,
        totalSlots: Int,
        bannerUrl: String,
        rules: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("টুর্নামেন্টের নাম দিন")
            return
        }
        viewModelScope.launch {
            val tournament = Tournament(
                title = title.trim(),
                gameType = gameType.trim().ifEmpty { "DLS 24" },
                entryFee = entryFee,
                prizePool = prizePool,
                startDate = startDate.trim(),
                totalSlots = totalSlots,
                bannerUrl = bannerUrl.trim(),
                rules = rules.trim(),
                status = "open"
            )
            val res = repository.createTournament(tournament)
            if (res.isSuccess) onSuccess() else onError(res.exceptionOrNull()?.localizedMessage ?: "ব্যর্থ হয়েছে")
        }
    }

    fun updateTournamentStatus(tournament: Tournament, newStatus: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateTournament(tournament.copy(status = newStatus))
            onSuccess()
        }
    }

    fun deleteTournament(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTournament(id)
            onSuccess()
        }
    }

    // --- Registration Approval ---
    fun updateRegistrationStatus(
        registration: Registration,
        newStatus: String,
        adminName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.updateRegistrationStatus(registration, newStatus, adminName)
            if (res.isSuccess) onSuccess() else onError(res.exceptionOrNull()?.localizedMessage ?: "ব্যর্থ হয়েছে")
        }
    }

    // --- Match Management ---
    fun createMatch(
        tournamentId: String,
        tournamentTitle: String,
        teamA: String,
        teamB: String,
        matchDate: String,
        round: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (teamA.isBlank() || teamB.isBlank() || matchDate.isBlank()) {
            onError("দলের নাম এবং তারিখ পূরণ করুন")
            return
        }
        viewModelScope.launch {
            val match = MatchFixture(
                tournamentId = tournamentId,
                tournamentTitle = tournamentTitle,
                teamA = teamA.trim(),
                teamB = teamB.trim(),
                matchDate = matchDate.trim(),
                round = round.trim().ifEmpty { "Group Stage" },
                status = "upcoming"
            )
            val res = repository.createMatch(match)
            if (res.isSuccess) onSuccess() else onError(res.exceptionOrNull()?.localizedMessage ?: "ম্যাচ তৈরি ব্যর্থ")
        }
    }

    fun updateMatchLive(
        match: MatchFixture,
        scoreA: Int,
        scoreB: Int,
        liveMinute: String,
        status: String,
        winner: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val updated = match.copy(
                scoreA = scoreA,
                scoreB = scoreB,
                liveMinute = liveMinute,
                status = status,
                winner = winner
            )
            repository.updateMatch(updated)
            onSuccess()
        }
    }

    fun deleteMatch(matchId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
            onSuccess()
        }
    }

    // --- Standings & Scorers ---
    fun saveStanding(standing: TournamentStanding, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveStanding(standing)
            onSuccess()
        }
    }

    fun saveScorer(scorer: TopScorer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveScorer(scorer)
            onSuccess()
        }
    }

    // --- Notices ---
    fun createNotice(
        title: String,
        content: String,
        category: String,
        isUrgent: Boolean,
        authorName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank() || content.isBlank()) {
            onError("শিরোনাম এবং বিবরণ পূরণ করুন")
            return
        }
        viewModelScope.launch {
            val notice = TournamentNotice(
                title = title.trim(),
                content = content.trim(),
                category = category,
                isUrgent = isUrgent,
                authorName = authorName
            )
            val res = repository.createNotice(notice)
            if (res.isSuccess) onSuccess() else onError(res.exceptionOrNull()?.localizedMessage ?: "বিজ্ঞপ্তি পোস্ট ব্যর্থ")
        }
    }

    fun deleteNotice(noticeId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteNotice(noticeId)
            onSuccess()
        }
    }

    // --- Finance Records ---
    fun recordFinancialTransaction(
        tournamentId: String,
        amount: Double,
        type: String, // "prize_distribution" or "expense"
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0 || description.isBlank()) {
            onError("সঠিক পরিমাণ এবং বিবরণ লিখুন")
            return
        }
        viewModelScope.launch {
            val res = repository.addFinancialRecord(tournamentId, amount, type, description)
            if (res.isSuccess) onSuccess() else onError(res.exceptionOrNull()?.localizedMessage ?: "ব্যর্থ হয়েছে")
        }
    }

    // --- Super Admin: User Roles ---
    fun setUserRole(uid: String, role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.updateUserRole(uid, role)
            if (res.isSuccess) onSuccess() else onError(res.exceptionOrNull()?.localizedMessage ?: "রোল পরিবর্তন ব্যর্থ")
        }
    }
}
