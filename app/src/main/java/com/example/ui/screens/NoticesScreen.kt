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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TournamentNotice
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SportyCard
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchDark
import com.example.ui.theme.StadiumCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun NoticesScreen(
    tournamentViewModel: TournamentViewModel
) {
    val notices by tournamentViewModel.notices.collectAsState()

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
                text = "নোটিশ ও ঘোষণা",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "ম্যাচ সিডিউল, নিয়মাবলী ও বিজয়ীদের ঘোষণা",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        if (notices.isEmpty()) {
            EmptyStateView(
                message = "বর্তমানে কোনো নতুন নোটিশ নেই।\nআয়োজক কোনো বার্তা প্রকাশ করলে এখানে দেখতে পাবেন।",
                icon = Icons.Default.Campaign,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notices) { notice ->
                    NoticeCardItem(notice = notice)
                }
            }
        }
    }
}

@Composable
fun NoticeCardItem(notice: TournamentNotice) {
    SportyCard(borderGlow = notice.isUrgent) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (notice.isUrgent) LiveRed.copy(alpha = 0.2f) else StadiumCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (notice.isUrgent) LiveRed else StadiumCyan
                    )
                ) {
                    Text(
                        text = if (notice.isUrgent) "জরুরি • ${notice.category}" else notice.category,
                        color = if (notice.isUrgent) LiveRed else StadiumCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (notice.authorName.isNotBlank()) {
                    Text(
                        text = "পোস্ট করেছেন: ${notice.authorName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notice.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notice.content,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
