package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.MatchFixture
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TournamentViewModel(
    private val repository: TournamentRepository
) : ViewModel() {

    val tournaments: StateFlow<List<Tournament>> = repository.observeTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchFixture>> = repository.observeMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<TournamentNotice>> = repository.observeNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appConfig: StateFlow<com.example.data.model.AppConfig> = repository.observeAppConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.model.AppConfig())

    private val _userRegistrations = MutableStateFlow<List<Registration>>(emptyList())
    val userRegistrations: StateFlow<List<Registration>> = _userRegistrations.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _selectedTournament = MutableStateFlow<Tournament?>(null)
    val selectedTournament: StateFlow<Tournament?> = _selectedTournament.asStateFlow()

    private val _tournamentStandings = MutableStateFlow<List<TournamentStanding>>(emptyList())
    val tournamentStandings: StateFlow<List<TournamentStanding>> = _tournamentStandings.asStateFlow()

    private val _tournamentScorers = MutableStateFlow<List<TopScorer>>(emptyList())
    val tournamentScorers: StateFlow<List<TopScorer>> = _tournamentScorers.asStateFlow()

    fun initUser(userId: String) {
        viewModelScope.launch {
            repository.observeUserRegistrations(userId).collectLatest {
                _userRegistrations.value = it
            }
        }
        viewModelScope.launch {
            repository.observeNotifications(userId).collectLatest {
                _notifications.value = it
            }
        }
    }

    fun selectTournament(tournament: Tournament) {
        _selectedTournament.value = tournament
        loadTournamentDetails(tournament.id)
    }

    private fun loadTournamentDetails(tournamentId: String) {
        viewModelScope.launch {
            repository.observeStandings(tournamentId).collectLatest {
                _tournamentStandings.value = it
            }
        }
        viewModelScope.launch {
            repository.observeScorers(tournamentId).collectLatest {
                _tournamentScorers.value = it
            }
        }
    }

    fun registerForTournament(
        tournament: Tournament,
        userProfile: UserProfile,
        playerName: String,
        phone: String,
        whatsapp: String,
        inGameId: String,
        teamName: String,
        paymentMethod: String,
        senderPhone: String,
        transactionId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (playerName.isBlank() || phone.isBlank() || teamName.isBlank() ||
            senderPhone.isBlank() || transactionId.isBlank()) {
            onError("দয়া করে সকল প্রয়োজনীয় তথ্য পূরণ করুন")
            return
        }

        viewModelScope.launch {
            val reg = Registration(
                tournamentId = tournament.id,
                tournamentTitle = tournament.title,
                userId = userProfile.uid,
                playerName = playerName.trim(),
                phone = phone.trim(),
                whatsapp = whatsapp.trim().ifEmpty { phone.trim() },
                inGameId = inGameId.trim(),
                teamName = teamName.trim(),
                paymentMethod = paymentMethod,
                senderPhone = senderPhone.trim(),
                transactionId = transactionId.trim().uppercase(),
                amount = tournament.entryFee,
                status = "pending"
            )

            val res = repository.submitRegistration(reg)
            if (res.isSuccess) {
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.localizedMessage ?: "নিবন্ধন সফল হয়নি, পুনরায় চেষ্টা করুন")
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }
}
