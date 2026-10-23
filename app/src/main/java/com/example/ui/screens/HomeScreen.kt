package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MatchFixture
import com.example.data.model.Tournament
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LivePulsingBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.SportyButton
import com.example.ui.components.SportyCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchCard
import com.example.ui.theme.PitchCardElevated
import com.example.ui.theme.PitchDark
import com.example.ui.theme.PitchSurfaceVariant
import com.example.ui.theme.StadiumCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.theme.TrophyGoldLight
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    tournamentViewModel: TournamentViewModel,
    onNavigateToTournaments: () -> Unit,
    onNavigateToFixtures: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onSelectTournament: (Tournament) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val tournaments by tournamentViewModel.tournaments.collectAsState()
    val matches by tournamentViewModel.allMatches.collectAsState()
    val notices by tournamentViewModel.notices.collectAsState()
    val notifications by tournamentViewModel.notifications.collectAsState()
    val appConfig by tournamentViewModel.appConfig.collectAsState()
    val unreadCount = notifications.count { !it.read }

    val liveMatches = matches.filter { it.status == "live" }
    val upcomingMatches = matches.filter { it.status == "upcoming" }.take(4)
    val activeTournaments = tournaments.filter { it.status == "open" || it.status == "ongoing" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Player Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchSurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, TrophyGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_tournament_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.name.ifEmpty { "খেলোয়াড়" },
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (userProfile.isAdmin()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (userProfile.isSuperAdmin()) LiveRed.copy(alpha = 0.2f) else TrophyGold.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (userProfile.isSuperAdmin()) LiveRed else TrophyGold
                                    )
                                ) {
                                    Text(
                                        text = if (userProfile.isSuperAdmin()) "SUPER ADMIN" else "ADMIN",
                                        color = if (userProfile.isSuperAdmin()) LiveRed else TrophyGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (userProfile.teamName.isNotBlank()) "টিম: ${userProfile.teamName}" else "DLS Football Arena",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (userProfile.isAdmin()) {
                        IconButton(
                            onClick = { authViewModel.toggleAdminMode() },
                            modifier = Modifier.testTag("admin_panel_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = TrophyGold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = LiveRed) {
                                        Text("$unreadCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Urgent Admin Notice Message Banner (from AppControlScreen)
        if (appConfig.noticeMessage.isNotBlank()) {
            item {
                SportyCard(
                    borderGlow = true,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(LiveRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = LiveRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("জরুরি ঘোষণা", color = LiveRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(appConfig.noticeMessage, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        // Monthly Special Offer & Bonus Banner (from AppControlScreen)
        if (appConfig.monthlyOfferTitle.isNotBlank() || (appConfig.bonusAmount.isNotBlank() && appConfig.bonusAmount != "0")) {
            item {
                SportyCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF2E1065).copy(alpha = 0.6f), Color(0xFF1E1B4B).copy(alpha = 0.6f)))
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎁 মাসিক বিশেষ অফার",
                                color = TrophyGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (appConfig.bonusAmount.isNotBlank() && appConfig.bonusAmount != "0") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GrassGreen.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrassGreen)
                                ) {
                                    Text(
                                        text = "+${appConfig.bonusAmount} BDT বোনাস",
                                        color = GrassGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        if (appConfig.monthlyOfferTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = appConfig.monthlyOfferTitle,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (appConfig.monthlyOfferDetails.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appConfig.monthlyOfferDetails,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // Hero Spotlight Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1F293D),
                                Color(0xFF162032),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(TrophyGold.copy(alpha = 0.7f), StadiumCyan.copy(alpha = 0.5f))),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TrophyGold.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TrophyGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "🏆 DLS TOURNAMENT 2026",
                                color = TrophyGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = TrophyGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "সহজেই টুর্নামেন্টে যোগ দিন,\nখেলুন এবং জিতুন!",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "DLS, FIFA ও ফুটবল টুর্নামেন্টে অংশগ্রহণ করুন, লাইভ স্কোর দেখুন ও প্রাইজমানি জিতে নিন।",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SportyButton(
                        text = "টুর্নামেন্ট সমূহ দেখুন",
                        onClick = onNavigateToTournaments,
                        modifier = Modifier.testTag("hero_explore_tournaments_button")
                    )
                }
            }
        }

        // Live Matches Section
        if (liveMatches.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "লাইভ ম্যাচ (Live Matches)",
                    actionText = "সকল ম্যাচ",
                    onActionClick = onNavigateToFixtures,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(liveMatches) { match ->
                        LiveMatchCard(match = match, onClick = onNavigateToFixtures)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Active Tournaments Section
        item {
            SectionHeader(
                title = "চলমান টুর্নামেন্ট (Active Tournaments)",
                subtitle = "অনলাইন ও লোকাল টুর্নামেন্ট সমূহ",
                actionText = "সব দেখুন",
                onActionClick = onNavigateToTournaments,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (activeTournaments.isEmpty()) {
            item {
                EmptyStateView(
                    message = "বর্তমানে কোনো টুর্নামেন্ট চালু নেই।\nনতুন টুর্নামেন্টের জন্য অপেক্ষা করুন।",
                    actionButtonText = "টুর্নামেন্ট পেজে যান",
                    onActionClick = onNavigateToTournaments,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(activeTournaments.take(3)) { tournament ->
                TournamentCardItem(
                    tournament = tournament,
                    onClick = { onSelectTournament(tournament) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Upcoming Matches
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(
                title = "আসন্ন ম্যাচ ফিক্সচার (Upcoming Fixtures)",
                actionText = "সব দেখুন",
                onActionClick = onNavigateToFixtures,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (upcomingMatches.isEmpty() && liveMatches.isEmpty()) {
            item {
                EmptyStateView(
                    message = "বর্তমানে কোনো নির্ধারিত ম্যাচ নেই।\nআয়োজক ফিক্সচার প্রকাশ করলে এখানে দেখতে পাবেন।",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(upcomingMatches) { match ->
                MatchRowItem(
                    match = match,
                    onClick = onNavigateToFixtures,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Recent Notices
        if (notices.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    title = "সাম্প্রতিক নোটিশ ও ঘোষণা",
                    actionText = "সব দেখুন",
                    onActionClick = onNavigateToNotices,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(notices.take(2)) { notice ->
                SportyCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    onClick = onNavigateToNotices
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (notice.isUrgent) LiveRed.copy(alpha = 0.2f) else StadiumCyan.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = if (notice.isUrgent) LiveRed else StadiumCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notice.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = notice.content,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMatchCard(
    match: MatchFixture,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, LiveRed.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LivePulsingBadge()
                Text(
                    text = match.liveMinute.ifEmpty { "LIVE" },
                    color = TrophyGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = match.teamA,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${match.scoreA} - ${match.scoreB}",
                    color = TrophyGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = match.teamB,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${match.tournamentTitle} • ${match.round}",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MatchRowItem(
    match: MatchFixture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SportyCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${match.tournamentTitle} • ${match.round}",
                    color = StadiumCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(status = match.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.teamA,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamB,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (match.status == "finished" || match.status == "live") {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "${match.scoreA}",
                            color = TrophyGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${match.scoreB}",
                            color = TrophyGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = match.matchDate,
                            color = TrophyGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (match.winner.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏆 বিজয়ী: ${match.winner}",
                    color = GrassGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TournamentCardItem(
    tournament: Tournament,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SportyCard(
        modifier = modifier,
        borderGlow = tournament.status == "open",
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StadiumCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = tournament.gameType,
                        color = StadiumCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                StatusBadge(status = tournament.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tournament.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "এন্ট্রি ফি", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = if (tournament.entryFee > 0) "${tournament.entryFee.toInt()} ৳" else "ফ্রি (Free)",
                        color = TrophyGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "প্রাইজপুল", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${tournament.prizePool.toInt()} ৳",
                        color = GrassGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "স্লট সংখ্যা", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${tournament.registeredCount}/${tournament.totalSlots}",
                        color = StadiumCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
