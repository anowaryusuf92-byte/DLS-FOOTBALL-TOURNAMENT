package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.SportyOutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.data.repository.TournamentRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FixturesLiveScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.NotificationsModal
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegistrationDialog
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TournamentDetailModal
import com.example.ui.screens.TournamentsScreen
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PitchDark
import com.example.ui.theme.PitchSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.TournamentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TournamentRepository(this)
        val authViewModel = AuthViewModel(repository)
        val tournamentViewModel = TournamentViewModel(repository)
        val adminViewModel = AdminViewModel(repository)

        setContent {
            MyApplicationTheme {
                MainAppEntry(
                    authViewModel = authViewModel,
                    tournamentViewModel = tournamentViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppEntry(
    authViewModel: AuthViewModel,
    tournamentViewModel: TournamentViewModel,
    adminViewModel: AdminViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isSplashVisible by remember { mutableStateOf(true) }
    val authState by authViewModel.uiState.collectAsState()
    val isAdminMode by authViewModel.isAdminMode.collectAsState()
    val appConfig by tournamentViewModel.appConfig.collectAsState()

    if (isSplashVisible) {
        SplashScreen(
            onSplashFinished = { isSplashVisible = false }
        )
    } else {
        when (val state = authState) {
            is AuthUiState.Authenticated -> {
                // Initialize user-specific streams (registrations, notifications)
                LaunchedEffect(state.user.uid) {
                    tournamentViewModel.initUser(state.user.uid)
                }

                if (!appConfig.isAppActive && !state.profile.isAdmin()) {
                    AppMaintenanceScreen(
                        noticeMessage = appConfig.noticeMessage,
                        onSignOut = { authViewModel.signOut(context) }
                    )
                } else if (isAdminMode && state.profile.isAdmin()) {
                    BackHandler {
                        authViewModel.setAdminMode(false)
                    }
                    AdminDashboardScreen(
                        userProfile = state.profile,
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel
                    )
                } else {
                    PlayerNavigationContainer(
                        userProfile = state.profile,
                        authViewModel = authViewModel,
                        tournamentViewModel = tournamentViewModel
                    )
                }
            }
            is AuthUiState.Unauthenticated, is AuthUiState.Loading, is AuthUiState.Error -> {
                AuthScreen(
                    authViewModel = authViewModel,
                    authState = authState
                )
            }
        }
    }
}

@Composable
fun AppMaintenanceScreen(
    noticeMessage: String,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, TrophyGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tournament_logo),
                    contentDescription = "Tournament Logo",
                    modifier = Modifier.size(90.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "অ্যাপটি সাময়িক বন্ধ রয়েছে",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = noticeMessage.ifEmpty { "সিস্টেম রক্ষণাবেক্ষণ ও আপগ্রেডের কাজ চলছে। অতি দ্রুত অ্যাপটি পুনরায় চালু করা হবে।" },
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            SportyOutlinedButton(
                text = "লগআউট",
                onClick = onSignOut,
                color = LiveRed,
                modifier = Modifier.width(140.dp)
            )
        }
    }
}

@Composable
fun PlayerNavigationContainer(
    userProfile: com.example.data.model.UserProfile,
    authViewModel: AuthViewModel,
    tournamentViewModel: TournamentViewModel
) {
    var currentTab by remember { mutableIntStateOf(0) }
    // 0 = Home, 1 = Tournaments, 2 = Fixtures/Live, 3 = Notices, 4 = Profile

    var selectedTournamentForDetail by remember { mutableStateOf<Tournament?>(null) }
    var selectedTournamentForRegister by remember { mutableStateOf<Tournament?>(null) }
    var showNotificationsModal by remember { mutableStateOf(false) }

    val notifications by tournamentViewModel.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.read }

    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        containerColor = PitchDark,
        bottomBar = {
            NavigationBar(
                containerColor = PitchSurface,
                tonalElevation = 8.dp
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "হোম",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("হোম", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = TrophyGold,
                        indicatorColor = TrophyGold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // 2. Tournaments
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
                            contentDescription = "টুর্নামেন্ট",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("টুর্নামেন্ট", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = TrophyGold,
                        indicatorColor = TrophyGold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tournaments")
                )

                // 3. Fixtures / Live
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 2) Icons.Default.SportsSoccer else Icons.Outlined.SportsSoccer,
                            contentDescription = "ফিক্সচার",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("ফিক্সচার / লাইভ", fontSize = 10.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = TrophyGold,
                        indicatorColor = TrophyGold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_fixtures")
                )

                // 4. Notices
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 3) Icons.Default.Campaign else Icons.Outlined.Campaign,
                            contentDescription = "নোটিশ",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("নোটিশ", fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = TrophyGold,
                        indicatorColor = TrophyGold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_notices")
                )

                // 5. Profile
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = LiveRed)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == 4) Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "প্রোফাইল",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = { Text("প্রোফাইল", fontSize = 11.sp, fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = TrophyGold,
                        indicatorColor = TrophyGold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeScreen(
                        userProfile = userProfile,
                        authViewModel = authViewModel,
                        tournamentViewModel = tournamentViewModel,
                        onNavigateToTournaments = { currentTab = 1 },
                        onNavigateToFixtures = { currentTab = 2 },
                        onNavigateToNotices = { currentTab = 3 },
                        onSelectTournament = { tourney ->
                            tournamentViewModel.selectTournament(tourney)
                            selectedTournamentForDetail = tourney
                        },
                        onOpenNotifications = { showNotificationsModal = true }
                    )
                    1 -> TournamentsScreen(
                        tournamentViewModel = tournamentViewModel,
                        onSelectTournament = { tourney ->
                            tournamentViewModel.selectTournament(tourney)
                            selectedTournamentForDetail = tourney
                        }
                    )
                    2 -> FixturesLiveScreen(
                        tournamentViewModel = tournamentViewModel
                    )
                    3 -> NoticesScreen(
                        tournamentViewModel = tournamentViewModel
                    )
                    4 -> ProfileScreen(
                        userProfile = userProfile,
                        authViewModel = authViewModel,
                        tournamentViewModel = tournamentViewModel
                    )
                }
            }
        }
    }

    // Tournament Detail Modal
    if (selectedTournamentForDetail != null) {
        TournamentDetailModal(
            tournament = selectedTournamentForDetail!!,
            tournamentViewModel = tournamentViewModel,
            onDismiss = { selectedTournamentForDetail = null },
            onOpenRegister = { tourney ->
                selectedTournamentForDetail = null
                selectedTournamentForRegister = tourney
            }
        )
    }

    // Registration Dialog
    if (selectedTournamentForRegister != null) {
        RegistrationDialog(
            tournament = selectedTournamentForRegister!!,
            userProfile = userProfile,
            tournamentViewModel = tournamentViewModel,
            onDismiss = { selectedTournamentForRegister = null },
            onSuccess = { selectedTournamentForRegister = null }
        )
    }

    // Notifications Modal
    if (showNotificationsModal) {
        NotificationsModal(
            tournamentViewModel = tournamentViewModel,
            onDismiss = { showNotificationsModal = false }
        )
    }
}
