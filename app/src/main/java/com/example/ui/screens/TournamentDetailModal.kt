package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.SportyButton
import com.example.ui.components.SportyCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.PitchCard
import com.example.ui.theme.PitchCardElevated
import com.example.ui.theme.PitchDark
import com.example.ui.theme.PitchSurface
import com.example.ui.theme.StadiumCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailModal(
    tournament: Tournament,
    tournamentViewModel: TournamentViewModel,
    onDismiss: () -> Unit,
    onOpenRegister: (Tournament) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val standings by tournamentViewModel.tournamentStandings.collectAsState()
    val scorers by tournamentViewModel.tournamentScorers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Overview & Rules, 1 = Standings, 2 = Top Scorers

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PitchSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                StatusBadge(status = tournament.status)

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tournament.title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Bar
            SportyCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("এন্ট্রি ফি", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = if (tournament.entryFee > 0) "${tournament.entryFee.toInt()} ৳" else "ফ্রি",
                            color = TrophyGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("প্রাইজপুল", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${tournament.prizePool.toInt()} ৳",
                            color = GrassGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("স্লট ফিল্ড", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${tournament.registeredCount}/${tournament.totalSlots}",
                            color = StadiumCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("শুরুর তারিখ", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = tournament.startDate.ifEmpty { "শীঘ্রই" },
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PitchCard,
                contentColor = TrophyGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TrophyGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("নিয়মাবলী", color = if (selectedTab == 0) TrophyGold else TextSecondary, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("পয়েন্ট টেবিল", color = if (selectedTab == 1) TrophyGold else TextSecondary, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("শীর্ষ স্কোরার", color = if (selectedTab == 2) TrophyGold else TextSecondary, fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        LazyColumn {
                            item {
                                SectionHeader(title = "টুর্নামেন্টের নিয়মাবলী")
                                SportyCard(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = tournament.rules.ifEmpty {
                                                "১. প্রতি ম্যাচের সময়সূচী মেনে খেলতে হবে।\n২. কোনো প্রকার অসদুপায় বা মডারেশন গ্রহণযোগ্য নয়।\n৩. ম্যাচ শেষে ফলাফলের স্ক্রিনশট আয়োজকদের পাঠাতে হবে।\n৪. আয়োজকদের সিদ্ধান্তই চূড়ান্ত।"
                                            },
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (standings.isEmpty()) {
                            EmptyStateView(
                                message = "বর্তমানে কোনো পয়েন্ট টেবিল তথ্য নেই।\nম্যাচ শুরুর পর আপডেট করা হবে।",
                                icon = Icons.Default.FormatListNumbered
                            )
                        } else {
                            LazyColumn {
                                item {
                                    // Standings Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(PitchCardElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#", color = TrophyGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                        Text("দল (Team)", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                        Text("P", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                                        Text("W", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                                        Text("D", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                                        Text("L", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                                        Text("GD", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(28.dp))
                                        Text("PTS", color = TrophyGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                items(standings) { standing ->
                                    val index = standings.indexOf(standing) + 1
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("$index", color = if (index <= 2) TrophyGold else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                        Text(standing.teamName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                        Text("${standing.played}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                                        Text("${standing.won}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                                        Text("${standing.drawn}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                                        Text("${standing.lost}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                                        Text("${standing.goalDifference}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(28.dp))
                                        Text("${standing.points}", color = TrophyGold, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                                    }
                                    HorizontalDivider(color = Color(0x1AFFFFFF))
                                }
                            }
                        }
                    }
                    2 -> {
                        if (scorers.isEmpty()) {
                            EmptyStateView(
                                message = "বর্তমানে কোনো শীর্ষ গোলদাতা তালিকা নেই।",
                                icon = Icons.Default.EmojiEvents
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(scorers) { scorer ->
                                    SportyCard {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(TrophyGold.copy(alpha = 0.2f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TrophyGold, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(scorer.playerName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    Text(scorer.teamName, color = TextSecondary, fontSize = 11.sp)
                                                }
                                            }
                                            Text(
                                                text = "⚽ ${scorer.goals} গোল",
                                                color = GrassGreen,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tournament.status == "open") {
                SportyButton(
                    text = "টুর্নামেন্টে যোগ দিন (Register)",
                    onClick = {
                        onDismiss()
                        onOpenRegister(tournament)
                    },
                    icon = Icons.Default.HowToReg,
                    modifier = Modifier.testTag("tournament_detail_register_button")
                )
            }
        }
    }
}
