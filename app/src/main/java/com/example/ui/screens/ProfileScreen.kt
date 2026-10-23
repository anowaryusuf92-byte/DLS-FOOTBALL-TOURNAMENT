package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Registration
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
import com.example.ui.theme.PitchDark
import com.example.ui.theme.PitchSurfaceVariant
import com.example.ui.theme.StadiumCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    tournamentViewModel: TournamentViewModel
) {
    val context = LocalContext.current
    val registrations by tournamentViewModel.userRegistrations.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        item {
            // Profile Card
            SportyCard(borderGlow = true) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(2.dp, TrophyGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_tournament_logo),
                            contentDescription = "Player Avatar",
                            modifier = Modifier.size(76.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.name.ifEmpty { "খেলোয়াড়" },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userProfile.email,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (userProfile.role) {
                            "super_admin" -> LiveRed.copy(alpha = 0.2f)
                            "admin" -> TrophyGold.copy(alpha = 0.2f)
                            else -> StadiumCyan.copy(alpha = 0.2f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (userProfile.role) {
                                "super_admin" -> LiveRed
                                "admin" -> TrophyGold
                                else -> StadiumCyan
                            }
                        )
                    ) {
                        Text(
                            text = when (userProfile.role) {
                                "super_admin" -> "👑 SUPER ADMIN"
                                "admin" -> "🛡️ ADMIN"
                                else -> "⚽ PLAYER"
                            },
                            color = when (userProfile.role) {
                                "super_admin" -> LiveRed
                                "admin" -> TrophyGold
                                else -> StadiumCyan
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchDark, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ফোন নম্বর", color = TextSecondary, fontSize = 11.sp)
                            Text(userProfile.phone.ifEmpty { "দেওয়া হয়নি" }, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("ইন-গেম ID", color = TextSecondary, fontSize = 11.sp)
                            Text(userProfile.inGameId.ifEmpty { "N/A" }, color = TrophyGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("টিমের নাম", color = TextSecondary, fontSize = 11.sp)
                            Text(userProfile.teamName.ifEmpty { "N/A" }, color = StadiumCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SportyOutlinedButton(
                            text = "প্রোফাইল এডিট",
                            icon = Icons.Default.Edit,
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        if (userProfile.isAdmin()) {
                            SportyButton(
                                text = "এডমিন প্যানেল",
                                icon = Icons.Default.AdminPanelSettings,
                                onClick = { authViewModel.toggleAdminMode() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "আমার টুর্নামেন্ট নিবন্ধনসমূহ",
                subtitle = "আবেদনের অবস্থা ও পেমেন্ট ট্র্যাকিং"
            )
        }

        if (registrations.isEmpty()) {
            item {
                EmptyStateView(
                    message = "আপনি এখনও কোনো টুর্নামেন্টে নিবন্ধন করেননি।\nটুর্নামেন্ট পেজ থেকে সহজে নিবন্ধন করুন।",
                    icon = Icons.Default.SportsSoccer
                )
            }
        } else {
            items(registrations) { reg ->
                MyRegistrationCard(registration = reg)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            SportyOutlinedButton(
                text = "লগআউট করুন (Sign Out)",
                icon = Icons.Default.ExitToApp,
                onClick = { authViewModel.signOut(context) },
                color = LiveRed,
                modifier = Modifier.testTag("profile_sign_out_button")
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, whatsapp, inGameId, teamName ->
                authViewModel.updateProfile(
                    name = name,
                    phone = phone,
                    whatsapp = whatsapp,
                    inGameId = inGameId,
                    teamName = teamName,
                    photoUrl = userProfile.photoUrl,
                    onSuccess = { showEditDialog = false },
                    onError = {}
                )
            }
        )
    }
}

@Composable
fun MyRegistrationCard(registration: Registration) {
    SportyCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = registration.tournamentTitle,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = registration.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("টিম: ${registration.teamName}", color = TextSecondary, fontSize = 12.sp)
                Text("ফি: ${registration.amount.toInt()} ৳", color = TrophyGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("পদ্ধতি: ${registration.paymentMethod} (${registration.senderPhone})", color = TextSecondary, fontSize = 12.sp)
                Text("TrxID: ${registration.transactionId}", color = StadiumCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, whatsapp: String, inGameId: String, teamName: String) -> Unit
) {
    var name by remember { mutableStateOf(userProfile.name) }
    var phone by remember { mutableStateOf(userProfile.phone) }
    var whatsapp by remember { mutableStateOf(userProfile.whatsapp) }
    var inGameId by remember { mutableStateOf(userProfile.inGameId) }
    var teamName by remember { mutableStateOf(userProfile.teamName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("প্রোফাইল আপডেট", color = TrophyGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("খেলোয়াড়ের নাম") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrophyGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ফোন নম্বর") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrophyGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("হোয়াটসঅ্যাপ নম্বর") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrophyGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = inGameId,
                    onValueChange = { inGameId = it },
                    label = { Text("ইন-গেম ID") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrophyGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("দলের নাম") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrophyGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            SportyButton(
                text = "সংরক্ষণ করুন",
                onClick = { onSave(name, phone, whatsapp, inGameId, teamName) },
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            SportyOutlinedButton(
                text = "বাতিল",
                onClick = onDismiss,
                modifier = Modifier.width(90.dp)
            )
        },
        containerColor = PitchDark
    )
}
