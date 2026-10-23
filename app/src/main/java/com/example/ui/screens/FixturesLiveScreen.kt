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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchFixture
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.PitchDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun FixturesLiveScreen(
    tournamentViewModel: TournamentViewModel
) {
    val matches by tournamentViewModel.allMatches.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") } // all, live, upcoming, finished

    val filtered = when (selectedFilter) {
        "live" -> matches.filter { it.status == "live" }
        "upcoming" -> matches.filter { it.status == "upcoming" }
        "finished" -> matches.filter { it.status == "finished" }
        else -> matches
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "ম্যাচ ফিক্সচার ও লাইভ স্কোর",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "লাইভ ম্যাচ, সময়সূচী ও পূর্ববর্তী ফলাফল",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        val tabs = listOf(
            "all" to "সকল ম্যাচ (${matches.size})",
            "live" to "লাইভ (${matches.count { it.status == "live" }})",
            "upcoming" to "আসন্ন (${matches.count { it.status == "upcoming" }})",
            "finished" to "সমাপ্ত (${matches.count { it.status == "finished" }})"
        )

        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == selectedFilter },
            containerColor = PitchDark,
            contentColor = TrophyGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                val index = tabs.indexOfFirst { it.first == selectedFilter }
                if (index >= 0 && index < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = TrophyGold
                    )
                }
            }
        ) {
            tabs.forEach { (key, label) ->
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
                    "live" -> "বর্তমানে কোনো লাইভ ম্যাচ চলছে না।\nপরবর্তী ম্যাচের জন্য ফিক্সচার দেখুন।"
                    "upcoming" -> "বর্তমানে কোনো নির্ধারিত আসন্ন ম্যাচ নেই।"
                    "finished" -> "এখনও কোনো ম্যাচ সমাপ্ত হয়নি।"
                    else -> "বর্তমানে কোনো ম্যাচ ফিক্সচার নেই।"
                },
                icon = Icons.Default.SportsSoccer,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { match ->
                    MatchRowItem(
                        match = match,
                        onClick = {}
                    )
                }
            }
        }
    }
}
