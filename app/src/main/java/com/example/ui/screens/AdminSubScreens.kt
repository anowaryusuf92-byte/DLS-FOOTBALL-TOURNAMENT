package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinanceSummary
import com.example.data.model.MatchFixture
import com.example.data.model.Registration
import com.example.data.model.SUPER_ADMIN_UID
import com.example.data.model.TopScorer
import com.example.data.model.Tournament
import com.example.data.model.TournamentStanding
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.SportyButton
import com.example.ui.components.SportyCard
import com.example.ui.components.SportyOutlinedButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchCard
import com.example.ui.theme.PitchCardElevated
import com.example.ui.theme.PitchDark
import com.example.ui.theme.StadiumCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.AdminViewModel

// ==========================================
// 1. REGISTRATIONS & PAYMENT APPROVALS
// ==========================================
@Composable
fun AdminRegistrationsContent(
    adminViewModel: AdminViewModel,
    adminName: String
) {
    val registrations by adminViewModel.allRegistrations.collectAsState()
    var filterStatus by remember { mutableStateOf("pending") } // pending, approved, rejected, all

    val filtered = when (filterStatus) {
        "pending" -> registrations.filter { it.status == "pending" }
        "approved" -> registrations.filter { it.status == "approved" }
        "rejected" -> registrations.filter { it.status == "rejected" }
        else -> registrations
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("pending" to "অপেক্ষমাণ", "approved" to "অনুমোদিত", "rejected" to "বাতিল", "all" to "সকল").forEach { (key, label) ->
                FilterChip(
                    selected = filterStatus == key,
                    onClick = { filterStatus = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TrophyGold,
                        selectedLabelColor = Color.Black,
                        containerColor = PitchCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                message = "বর্তমানে কোনো $filterStatus রেজিস্ট্রেশন আবেদন নেই।",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { reg ->
                    AdminRegistrationCard(
                        reg = reg,
                        onApprove = {
                            adminViewModel.updateRegistrationStatus(reg, "approved", adminName, {}, {})
                        },
                        onReject = {
                            adminViewModel.updateRegistrationStatus(reg, "rejected", adminName, {}, {})
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminRegistrationCard(
    reg: Registration,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    SportyCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reg.tournamentTitle, color = TrophyGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                StatusBadge(status = reg.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("খেলোয়াড়: ${reg.playerName}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("দল: ${reg.teamName} • ইন-গেম ID: ${reg.inGameId}", color = TextSecondary, fontSize = 12.sp)
            Text("ফোন: ${reg.phone} • হোয়াটসঅ্যাপ: ${reg.whatsapp}", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PitchDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "পেমেন্ট মেথড: ${reg.paymentMethod} • ফি: ${reg.amount.toInt()} ৳",
                        color = StadiumCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "প্রেরক নম্বর: ${reg.senderPhone}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Transaction ID: ${reg.transactionId}",
                        color = TrophyGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (reg.status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SportyOutlinedButton(
                        text = "বাতিল",
                        icon = Icons.Default.Close,
                        onClick = onReject,
                        color = LiveRed,
                        modifier = Modifier.weight(1f)
                    )
                    SportyButton(
                        text = "অনুমোদন করুন",
                        icon = Icons.Default.Check,
                        onClick = onApprove,
                        containerColor = GrassGreen,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. TOURNAMENTS MANAGEMENT
// ==========================================
@Composable
fun AdminTournamentsContent(adminViewModel: AdminViewModel) {
    val tournaments by adminViewModel.allTournaments.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SportyButton(
            text = "+ নতুন টুর্নামেন্ট তৈরি করুন",
            icon = Icons.Default.Add,
            onClick = { showCreateDialog = true },
            modifier = Modifier.testTag("admin_create_tournament_btn")
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (tournaments.isEmpty()) {
            EmptyStateView(
                message = "বর্তমানে কোনো টুর্নামেন্ট ডাটাবেজে নেই।\nউপরের বাটনে ক্লিক করে নতুন টুর্নামেন্ট তৈরি করুন।",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tournaments) { tourney ->
                    AdminTournamentItem(
                        tournament = tourney,
                        onStatusChange = { newStatus ->
                            adminViewModel.updateTournamentStatus(tourney, newStatus) {}
                        },
                        onDelete = {
                            adminViewModel.deleteTournament(tourney.id) {}
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTournamentDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, gameType, fee, prize, startDate, slots, rules ->
                adminViewModel.createTournament(
                    title = title,
                    gameType = gameType,
                    entryFee = fee,
                    prizePool = prize,
                    startDate = startDate,
                    totalSlots = slots,
                    bannerUrl = "",
                    rules = rules,
                    onSuccess = { showCreateDialog = false },
                    onError = {}
                )
            }
        )
    }
}

@Composable
fun AdminTournamentItem(
    tournament: Tournament,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    SportyCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(tournament.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                StatusBadge(status = tournament.status)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${tournament.gameType} • ফি: ${tournament.entryFee.toInt()} ৳ • প্রাইজ: ${tournament.prizePool.toInt()} ৳ • স্লট: ${tournament.registeredCount}/${tournament.totalSlots}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("স্ট্যাটাস পরিবর্তন করুন:", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("open" to "Open", "ongoing" to "Ongoing", "completed" to "Done").forEach { (st, label) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (tournament.status == st) TrophyGold else PitchCardElevated,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onStatusChange(st) }
                        ) {
                            Text(
                                text = label,
                                color = if (tournament.status == st) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTournamentDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, gameType: String, fee: Double, prize: Double, startDate: String, slots: Int, rules: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var gameType by remember { mutableStateOf("DLS 24") }
    var fee by remember { mutableStateOf("50") }
    var prize by remember { mutableStateOf("500") }
    var startDate by remember { mutableStateOf("25 Oct, 8:00 PM") }
    var slots by remember { mutableStateOf("16") }
    var rules by remember { mutableStateOf("১. ফেয়ার প্লে বজায় রাখুন।\n২. নির্ধারিত সময়ের ১০ মিনিট আগে উপস্থিত থাকুন।") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন টুর্নামেন্ট তৈরি", color = TrophyGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("টুর্নামেন্টের নাম *") })
                OutlinedTextField(value = gameType, onValueChange = { gameType = it }, label = { Text("গেম টাইপ (যেমন: DLS 24)") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fee, onValueChange = { fee = it }, label = { Text("এন্ট্রি ফি (৳)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = prize, onValueChange = { prize = it }, label = { Text("প্রাইজপুল (৳)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("শুরুর তারিখ") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = slots, onValueChange = { slots = it }, label = { Text("মোট স্লট") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("নিয়মাবলী") }, minLines = 2)
            }
        },
        confirmButton = {
            SportyButton(
                text = "তৈরি করুন",
                onClick = {
                    val f = fee.toDoubleOrNull() ?: 0.0
                    val p = prize.toDoubleOrNull() ?: 0.0
                    val s = slots.toIntOrNull() ?: 16
                    onCreate(title, gameType, f, p, startDate, s, rules)
                },
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            SportyOutlinedButton(text = "বাতিল", onClick = onDismiss, modifier = Modifier.width(90.dp))
        },
        containerColor = PitchDark
    )
}

// ==========================================
// 3. MATCHES & LIVE SCORE UPDATER
// ==========================================
@Composable
fun AdminMatchesContent(adminViewModel: AdminViewModel) {
    val matches by adminViewModel.allMatches.collectAsState()
    val tournaments by adminViewModel.allTournaments.collectAsState()
    var showAddMatchDialog by remember { mutableStateOf(false) }
    var selectedMatchForEdit by remember { mutableStateOf<MatchFixture?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SportyButton(
            text = "+ ম্যাচ ফিক্সচার যুক্ত করুন",
            icon = Icons.Default.Add,
            onClick = { showAddMatchDialog = true },
            modifier = Modifier.testTag("admin_add_match_btn")
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (matches.isEmpty()) {
            EmptyStateView(
                message = "বর্তমানে কোনো ম্যাচ যুক্ত করা হয়নি।\nফিক্সচার তৈরি করতে উপরের বাটনে ক্লিক করুন।",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(matches) { match ->
                    SportyCard {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${match.tournamentTitle} • ${match.round}", color = StadiumCyan, fontSize = 11.sp)
                                StatusBadge(status = match.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(match.teamA, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${match.scoreA} - ${match.scoreB}", color = TrophyGold, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
                                Text(match.teamB, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("মিনিট: ${match.liveMinute} • সময়: ${match.matchDate}", color = TextSecondary, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SportyOutlinedButton(
                                    text = "ডিলিট",
                                    icon = Icons.Default.Delete,
                                    onClick = { adminViewModel.deleteMatch(match.id) {} },
                                    color = LiveRed,
                                    modifier = Modifier.weight(1f)
                                )
                                SportyButton(
                                    text = "লাইভ স্কোর কন্ট্রোল",
                                    icon = Icons.Default.Scoreboard,
                                    onClick = { selectedMatchForEdit = match },
                                    modifier = Modifier.weight(2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMatchDialog) {
        AddMatchDialog(
            tournaments = tournaments,
            onDismiss = { showAddMatchDialog = false },
            onAdd = { tId, tTitle, teamA, teamB, date, round ->
                adminViewModel.createMatch(
                    tournamentId = tId,
                    tournamentTitle = tTitle,
                    teamA = teamA,
                    teamB = teamB,
                    matchDate = date,
                    round = round,
                    onSuccess = { showAddMatchDialog = false },
                    onError = {}
                )
            }
        )
    }

    if (selectedMatchForEdit != null) {
        LiveScoreControlDialog(
            match = selectedMatchForEdit!!,
            onDismiss = { selectedMatchForEdit = null },
            onSave = { scoreA, scoreB, minute, status, winner ->
                adminViewModel.updateMatchLive(
                    match = selectedMatchForEdit!!,
                    scoreA = scoreA,
                    scoreB = scoreB,
                    liveMinute = minute,
                    status = status,
                    winner = winner,
                    onSuccess = { selectedMatchForEdit = null }
                )
            }
        )
    }
}

@Composable
fun AddMatchDialog(
    tournaments: List<Tournament>,
    onDismiss: () -> Unit,
    onAdd: (tournamentId: String, tournamentTitle: String, teamA: String, teamB: String, date: String, round: String) -> Unit
) {
    var selectedTourney by remember { mutableStateOf(tournaments.firstOrNull()) }
    var teamA by remember { mutableStateOf("") }
    var teamB by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("আজ রাত ৮:০০ টা") }
    var round by remember { mutableStateOf("গ্রুপ পর্ব (Match 1)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ম্যাচ ফিক্সচার যুক্ত করুন", color = TrophyGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("টুর্নামেন্ট: ${selectedTourney?.title ?: "ডিফল্ট"}", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(value = teamA, onValueChange = { teamA = it }, label = { Text("দল A এর নাম *") })
                OutlinedTextField(value = teamB, onValueChange = { teamB = it }, label = { Text("দল B এর নাম *") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ ও সময়") })
                OutlinedTextField(value = round, onValueChange = { round = it }, label = { Text("রাউন্ড (যেমন: সেমিফাইনাল)") })
            }
        },
        confirmButton = {
            SportyButton(
                text = "যুক্ত করুন",
                onClick = {
                    onAdd(selectedTourney?.id ?: "general", selectedTourney?.title ?: "Football Tournament", teamA, teamB, date, round)
                },
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            SportyOutlinedButton(text = "বাতিল", onClick = onDismiss, modifier = Modifier.width(90.dp))
        },
        containerColor = PitchDark
    )
}

@Composable
fun LiveScoreControlDialog(
    match: MatchFixture,
    onDismiss: () -> Unit,
    onSave: (scoreA: Int, scoreB: Int, minute: String, status: String, winner: String) -> Unit
) {
    var scoreA by remember { mutableIntStateOf(match.scoreA) }
    var scoreB by remember { mutableIntStateOf(match.scoreB) }
    var minute by remember { mutableStateOf(match.liveMinute) }
    var status by remember { mutableStateOf(match.status) } // upcoming, live, finished
    var winner by remember { mutableStateOf(match.winner) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("লাইভ স্কোর কন্ট্রোল", color = TrophyGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Score Adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(match.teamA, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("$scoreA", color = TrophyGold, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = PitchCardElevated, modifier = Modifier.clickable { if (scoreA > 0) scoreA-- }) {
                                Text(" - ", color = TextPrimary, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = PitchCardElevated, modifier = Modifier.clickable { scoreA++ }) {
                                Text(" + ", color = TrophyGold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Text("VS", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(match.teamB, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("$scoreB", color = TrophyGold, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = PitchCardElevated, modifier = Modifier.clickable { if (scoreB > 0) scoreB-- }) {
                                Text(" - ", color = TextPrimary, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = PitchCardElevated, modifier = Modifier.clickable { scoreB++ }) {
                                Text(" + ", color = TrophyGold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = minute,
                    onValueChange = { minute = it },
                    label = { Text("ম্যাচ মিনিট (যেমন: 45', HT, 90', FT)") }
                )

                Text("ম্যাচ অবস্থা (Status):", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("upcoming" to "আসন্ন", "live" to "লাইভ", "finished" to "সমাপ্ত").forEach { (st, label) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (status == st) TrophyGold else PitchCardElevated,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                                status = st
                                if (st == "finished") {
                                    winner = if (scoreA > scoreB) match.teamA else if (scoreB > scoreA) match.teamB else "ড্র (Draw)"
                                }
                            }
                        ) {
                            Text(
                                text = label,
                                color = if (status == st) Color.Black else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (status == "finished") {
                    OutlinedTextField(
                        value = winner,
                        onValueChange = { winner = it },
                        label = { Text("বিজয়ী দল") }
                    )
                }
            }
        },
        confirmButton = {
            SportyButton(
                text = "আপডেট করুন",
                onClick = { onSave(scoreA, scoreB, minute, status, winner) },
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            SportyOutlinedButton(text = "বাতিল", onClick = onDismiss, modifier = Modifier.width(90.dp))
        },
        containerColor = PitchDark
    )
}

// ==========================================
// 4. NOTICES & ANNOUNCEMENTS POSTER
// ==========================================
@Composable
fun AdminNoticesContent(
    adminViewModel: AdminViewModel,
    adminName: String
) {
    val notices by adminViewModel.allNotices.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Announcement") }
    var isUrgent by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SportyCard(borderGlow = true) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("নতুন ঘোষণা বা নোটিশ তৈরি", color = TrophyGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("বিস্তারিত বিবরণ *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it }, colors = CheckboxDefaults.colors(checkedColor = LiveRed))
                            Text("জরুরি বার্তা", color = if (isUrgent) LiveRed else TextSecondary, fontSize = 12.sp)
                        }
                        SportyButton(
                            text = "পোস্ট করুন",
                            onClick = {
                                adminViewModel.createNotice(title, content, category, isUrgent, adminName, {
                                    title = ""
                                    content = ""
                                    isUrgent = false
                                }, {})
                            },
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "প্রকাশিত নোটিশসমূহ")
        }

        if (notices.isEmpty()) {
            item {
                EmptyStateView(message = "বর্তমানে কোনো প্রকাশিত নোটিশ নেই।")
            }
        } else {
            items(notices) { notice ->
                SportyCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notice.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(notice.content, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                        }
                        IconButton(onClick = { adminViewModel.deleteNotice(notice.id) {} }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. FINANCIAL MANAGEMENT (Super Admin)
// ==========================================
@Composable
fun AdminFinanceContent(
    adminViewModel: AdminViewModel,
    financeSummary: FinanceSummary
) {
    val payments by adminViewModel.allPayments.collectAsState()
    var showExpenseDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SportyCard(borderGlow = true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("আর্থিক হিসাব বিবরণী (Financial Overview)", color = TrophyGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("সংগৃহীত এন্ট্রি ফি", color = TextSecondary, fontSize = 12.sp)
                            Text("${financeSummary.totalCollected.toInt()} ৳", color = GrassGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("প্রাইজমানি বিতরণ", color = TextSecondary, fontSize = 12.sp)
                            Text("${financeSummary.totalPrizeDistributed.toInt()} ৳", color = LiveRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("অন্যান্য খরচ", color = TextSecondary, fontSize = 12.sp)
                            Text("${financeSummary.totalExpense.toInt()} ৳", color = TrophyGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0x33FFFFFF))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("বর্তমান নেট ব্যালেন্স:", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("${financeSummary.netBalance.toInt()} ৳", color = TrophyGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SportyButton(
                text = "+ খরচ বা প্রাইজমানি এন্ট্রি দিন",
                icon = Icons.Default.Payments,
                onClick = { showExpenseDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "লেনদেনের হিসাব (Payment Records)")
        }

        if (payments.isEmpty()) {
            item {
                EmptyStateView(message = "এখনও কোনো আর্থিক লেনদেনের রেকর্ড তৈরি হয়নি।")
            }
        } else {
            items(payments) { payment ->
                SportyCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(payment.playerName.ifEmpty { "লেনদেন" }, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("মাধ্যম: ${payment.method} (${payment.senderNumber}) • Trx: ${payment.trxId}", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (payment.type == "entry_fee") "+" else "-"}${payment.amount.toInt()} ৳",
                                color = if (payment.type == "entry_fee") GrassGreen else LiveRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(status = payment.status)
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onAdd = { amount, type, desc ->
                adminViewModel.recordFinancialTransaction(
                    tournamentId = "general",
                    amount = amount,
                    type = type,
                    description = desc,
                    onSuccess = { showExpenseDialog = false },
                    onError = {}
                )
            }
        )
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (amount: Double, type: String, description: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("prize_distribution") } // prize_distribution or expense
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন ব্যয়ের রেকর্ড যোগ করুন", color = TrophyGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("বিবরণ (যেমন: চ্যাম্পিয়ন প্রাইজ বা স্ট্রিমিং খরচ)") }
                )
                Text("ধরণ নির্বাচন করুন:", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("prize_distribution" to "প্রাইজমানি", "expense" to "টুর্নামেন্ট খরচ").forEach { (tp, label) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (type == tp) TrophyGold else PitchCardElevated,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { type = tp }
                        ) {
                            Text(
                                text = label,
                                color = if (type == tp) Color.Black else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            SportyButton(
                text = "যোগ করুন",
                onClick = {
                    val am = amount.toDoubleOrNull() ?: 0.0
                    onAdd(am, type, desc)
                },
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            SportyOutlinedButton(text = "বাতিল", onClick = onDismiss, modifier = Modifier.width(90.dp))
        },
        containerColor = PitchDark
    )
}

// ==========================================
// 6. ADMIN USER ROLES MANAGEMENT (Super Admin)
// ==========================================
@Composable
fun AdminUserRolesContent(adminViewModel: AdminViewModel) {
    val users by adminViewModel.allUsers.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(
            title = "ব্যবহারকারী ও এডমিন রোলস",
            subtitle = "সুপার এডমিন হিসেবে রোল নির্ধারণ করুন"
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (users.isEmpty()) {
            EmptyStateView(
                message = "বর্তমানে কোনো নিবন্ধিত ব্যবহারকারী তালিকা নেই।",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    val isSuperAdmin = user.uid == SUPER_ADMIN_UID || user.role == "super_admin"
                    SportyCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(PitchCardElevated, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TrophyGold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(user.name.ifEmpty { "নাম নেই" }, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${user.email} • ${user.phone}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            if (isSuperAdmin) {
                                Surface(shape = RoundedCornerShape(6.dp), color = LiveRed.copy(alpha = 0.2f)) {
                                    Text("SUPER ADMIN", color = LiveRed, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(6.dp))
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (user.role == "admin") TrophyGold else PitchCardElevated,
                                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                                            adminViewModel.setUserRole(user.uid, "admin", {}, {})
                                        }
                                    ) {
                                        Text(
                                            text = "Admin",
                                            color = if (user.role == "admin") Color.Black else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (user.role == "player") StadiumCyan else PitchCardElevated,
                                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                                            adminViewModel.setUserRole(user.uid, "player", {}, {})
                                        }
                                    ) {
                                        Text(
                                            text = "Player",
                                            color = if (user.role == "player") Color.Black else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
