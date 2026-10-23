package com.example.uistudio.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

data class AppConfig(
    val isAppActive: Boolean = true,
    val monthlyOfferTitle: String = "",
    val monthlyOfferDetails: String = "",
    val bonusAmount: String = "0",
    val noticeMessage: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppControlScreen(
    onBack: (() -> Unit)? = null
) {
    val db = FirebaseFirestore.getInstance()
    var isAppActive by remember { mutableStateOf(true) }
    var offerTitle by remember { mutableStateOf("") }
    var offerDetails by remember { mutableStateOf("") }
    var bonusAmount by remember { mutableStateOf("") }
    var noticeMessage by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("app_config").document("settings")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    isAppActive = doc.getBoolean("isAppActive") ?: true
                    offerTitle = doc.getString("monthlyOfferTitle") ?: ""
                    offerDetails = doc.getString("monthlyOfferDetails") ?: ""
                    bonusAmount = doc.getString("bonusAmount") ?: "0"
                    noticeMessage = doc.getString("noticeMessage") ?: ""
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("অ্যাপ ও অফার কন্ট্রোল প্যানেল") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("অ্যাপ চালুর অবস্থা (App Active):", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = isAppActive,
                        onCheckedChange = { isAppActive = it }
                    )
                }
            }

            OutlinedTextField(
                value = offerTitle,
                onValueChange = { offerTitle = it },
                label = { Text("মাসিক অফারের টাইটেল") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = offerDetails,
                onValueChange = { offerDetails = it },
                label = { Text("অফারের বিস্তারিত তথ্য") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = bonusAmount,
                onValueChange = { bonusAmount = it },
                label = { Text("বোনাস পয়েন্ট / অ্যামাউন্ট (BDT)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = noticeMessage,
                onValueChange = { noticeMessage = it },
                label = { Text("জরুরি নোটিশ (হোম পেজে দেখাবে)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val config = AppConfig(
                        isAppActive = isAppActive,
                        monthlyOfferTitle = offerTitle,
                        monthlyOfferDetails = offerDetails,
                        bonusAmount = bonusAmount,
                        noticeMessage = noticeMessage
                    )
                    db.collection("app_config").document("settings")
                        .set(config)
                        .addOnSuccessListener {
                            statusMsg = "সফলভাবে আপডেট করা হয়েছে!"
                        }
                        .addOnFailureListener {
                            statusMsg = "আপডেট ব্যর্থ হয়েছে: ${it.localizedMessage}"
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("সেটিংস সেভ করুন")
            }

            if (statusMsg.isNotEmpty()) {
                Text(
                    text = statusMsg,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
