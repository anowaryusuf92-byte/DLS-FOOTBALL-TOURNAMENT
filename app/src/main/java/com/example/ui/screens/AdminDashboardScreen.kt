package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uistudio.screens.AppControlScreen
import com.example.data.model.UserProfile
import com.example.ui.components.SectionHeader
import com.example.ui.components.SportyCard
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchCard
import com.example.ui.theme.PitchCardElevated
import com.example.ui.theme.PitchDark
import com.example.ui.theme.PitchSurface
import com.example.ui.theme.StadiumCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AdminDashboardScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel
) {
    val financeSummary by adminViewModel.financeSummary.collectAsState()
    var selectedSection by remember { mutableIntStateOf(0) }
    // 0 = Dashboard Stats & Quick Actions, 1 = Registrations & Payments, 2 = Tournaments, 3 = Matches & Scores, 4 = Notices, 5 = Financials, 6 = User Roles

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PitchSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { authViewModel.setAdminMode(false) },
                    modifier = Modifier.testTag("admin_back_to_player_view")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back to Player View", tint = TrophyGold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (userProfile.isSuperAdmin()) "SUPER ADMIN PANEL" else "ORGANIZER ADMIN PANEL",
                        color = TrophyGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Football Arena Management Suite",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PitchCardElevated,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { authViewModel.setAdminMode(false) }
            ) {
                Text(
                    text = "প্লেয়ার ভিউ ➔",
                    color = StadiumCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Section Tabs
        val tabList = mutableListOf(
            "ড্যাশবোর্ড",
            "নিবন্ধন অনুমোদন",
            "টুর্নামেন্ট",
            "ম্যাচ ও লাইভ স্কোর",
            "ঘোষণা পোস্ট",
            "অ্যাপ কন্ট্রোল"
        )
        if (userProfile.isSuperAdmin()) {
            tabList.add("আর্থিক হিসাব")
            tabList.add("এডমিন রোল")
        }

        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = PitchDark,
            contentColor = TrophyGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedSection in tabPositions.indices) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = TrophyGold
                    )
                }
            }
        ) {
            tabList.forEachIndexed { index, label ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = label,
                            color = if (selectedSection == index) TrophyGold else TextSecondary,
                            fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Main Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedSection) {
                0 -> AdminOverviewContent(
                    financeSummary = financeSummary,
                    onNavigateSection = { selectedSection = it }
                )
                1 -> AdminRegistrationsContent(adminViewModel = adminViewModel, adminName = userProfile.name)
                2 -> AdminTournamentsContent(adminViewModel = adminViewModel)
                3 -> AdminMatchesContent(adminViewModel = adminViewModel)
                4 -> AdminNoticesContent(adminViewModel = adminViewModel, adminName = userProfile.name)
                5 -> AppControlScreen(onBack = { selectedSection = 0 })
                6 -> if (userProfile.isSuperAdmin()) AdminFinanceContent(adminViewModel = adminViewModel, financeSummary = financeSummary)
                7 -> if (userProfile.isSuperAdmin()) AdminUserRolesContent(adminViewModel = adminViewModel)
            }
        }
    }
}

@Composable
fun AdminOverviewContent(
    financeSummary: com.example.data.model.FinanceSummary,
    onNavigateSection: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "বাস্তব সময় পরিসংখ্যান (Live Statistics)",
                subtitle = "ক্লাউড ফায়ারস্টোর থেকে সরাসরি প্রাপ্ত তথ্য"
            )
        }

        // Primary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "মোট খেলোয়াড়",
                    value = "${financeSummary.totalPlayers}",
                    icon = Icons.Default.Group,
                    accentColor = StadiumCyan,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "অপেক্ষমাণ নিবন্ধন",
                    value = "${financeSummary.pendingRegistrations}",
                    icon = Icons.Default.HowToReg,
                    accentColor = TrophyGold,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(1) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "সক্রিয় টুর্নামেন্ট",
                    value = "${financeSummary.activeTournaments}",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = GrassGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(2) }
                )
                AdminMetricCard(
                    title = "আসন্ন ম্যাচ",
                    value = "${financeSummary.upcomingMatches}",
                    icon = Icons.Default.SportsSoccer,
                    accentColor = StadiumCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(3) }
                )
            }
        }

        // Finance Overview
        item {
            SectionHeader(
                title = "আর্থিক সারাংশ (Finance Overview)",
                subtitle = "ভেরিফায়েড এন্ট্রি ফি ও প্রাইজমানি"
            )
        }

        item {
            SportyCard(borderGlow = true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("মোট সংগৃহীত ফি", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "${financeSummary.totalCollected.toInt()} ৳",
                                color = GrassGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("প্রাইজমানি বিতরণ", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "${financeSummary.totalPrizeDistributed.toInt()} ৳",
                                color = LiveRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("নেট ব্যালেন্স", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "${financeSummary.netBalance.toInt()} ৳",
                                color = TrophyGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            SectionHeader(
                title = "দ্রুত অ্যাকশন (Quick Actions)",
                subtitle = "সরাসরি টুর্নামেন্ট পরিচালনা করুন"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminActionRow(
                    title = "প্লেয়ার রেজিস্ট্রেশন ও পেমেন্ট অনুমোদন",
                    subtitle = "${financeSummary.pendingRegistrations} টি আবেদন যাচাইকরণ বাকি",
                    icon = Icons.Default.HowToReg,
                    color = TrophyGold,
                    onClick = { onNavigateSection(1) }
                )
                AdminActionRow(
                    title = "নতুন টুর্নামেন্ট যোগ করুন",
                    subtitle = "স্লট, প্রাইজপুল ও নিয়ম নির্ধারণ করুন",
                    icon = Icons.Default.EmojiEvents,
                    color = StadiumCyan,
                    onClick = { onNavigateSection(2) }
                )
                AdminActionRow(
                    title = "ম্যাচ ফিক্সচার ও লাইভ স্কোর পরিচালনা",
                    subtitle = "স্কোর আপডেট, মিনিট এবং ফলাফল ঘোষণা",
                    icon = Icons.Default.Scoreboard,
                    color = GrassGreen,
                    onClick = { onNavigateSection(3) }
                )
                AdminActionRow(
                    title = "জরুরি ঘোষণা বা নোটিশ প্রকাশ করুন",
                    subtitle = "সকল প্লেয়ারদের ইন-অ্যাপ বার্তা পাঠান",
                    icon = Icons.Default.Campaign,
                    color = LiveRed,
                    onClick = { onNavigateSection(4) }
                )
                AdminActionRow(
                    title = "অ্যাপ ও অফার কন্ট্রোল প্যানেল",
                    subtitle = "অ্যাপ অ্যাক্টিভ স্ট্যাটাস, অফার ও বোনাস আপডেট",
                    icon = Icons.Default.ManageAccounts,
                    color = TrophyGold,
                    onClick = { onNavigateSection(5) }
                )
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
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
                Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AdminActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    SportyCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Text("➔", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
