package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.PitchCard
import com.example.ui.theme.PitchDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun TournamentsScreen(
    tournamentViewModel: TournamentViewModel,
    onSelectTournament: (Tournament) -> Unit
) {
    val tournaments by tournamentViewModel.tournaments.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") } // all, open, ongoing, completed

    val filtered = when (selectedFilter) {
        "open" -> tournaments.filter { it.status == "open" }
        "ongoing" -> tournaments.filter { it.status == "ongoing" }
        "completed" -> tournaments.filter { it.status == "completed" }
        else -> tournaments
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "টুর্নামেন্ট সমূহ (Tournaments)",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "DLS, FIFA ও অন্যান্য ফুটবল টুর্নামেন্টে অংশ নিন",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Filter Tabs
        val filters = listOf(
            "all" to "সকল টুর্নামেন্ট",
            "open" to "নিবন্ধন চলছে",
            "ongoing" to "চলমান",
            "completed" to "সমাপ্ত"
        )

        ScrollableTabRow(
            selectedTabIndex = filters.indexOfFirst { it.first == selectedFilter },
            containerColor = PitchDark,
            contentColor = TrophyGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                val index = filters.indexOfFirst { it.first == selectedFilter }
                if (index >= 0 && index < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = TrophyGold
                    )
                }
            }
        ) {
            filters.forEach { (key, label) ->
                Tab(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    text = {
                        Text(
                            text = label,
                            color = if (selectedFilter == key) TrophyGold else TextSecondary,
                            fontWeight = if (selectedFilter == key) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                message = when (selectedFilter) {
                    "open" -> "বর্তমানে কোনো উন্মুক্ত টুর্নামেন্ট চালু নেই।\nশীঘ্রই নতুন টুর্নামেন্ট ঘোষণা করা হবে।"
                    "ongoing" -> "বর্তমানে কোনো চলমান টুর্নামেন্ট নেই।"
                    "completed" -> "এখনও কোনো টুর্নামেন্ট সম্পন্ন হয়নি।"
                    else -> "বর্তমানে কোনো টুর্নামেন্ট পাওয়া যায়নি।"
                },
                icon = Icons.Default.EmojiEvents,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { tournament ->
                    TournamentCardItem(
                        tournament = tournament,
                        onClick = { onSelectTournament(tournament) }
                    )
                }
            }
        }
    }
}
