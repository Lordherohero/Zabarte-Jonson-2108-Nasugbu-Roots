package com.mygroup.nasugburoots.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mygroup.nasugburoots.data.AccountRepository
import com.mygroup.nasugburoots.data.PendingAccount
import com.mygroup.nasugburoots.ui.auth.AccountProfile
import com.mygroup.nasugburoots.ui.theme.Cream
import com.mygroup.nasugburoots.ui.theme.DeepForest
import com.mygroup.nasugburoots.ui.theme.Forest
import com.mygroup.nasugburoots.ui.theme.Muted

@Composable
fun ManagerDashboardScreen(
    profile: AccountProfile,
    onLogout: () -> Unit
) {
    val guides = rememberGuides()

    var pending by remember { mutableStateOf(emptyList<PendingAccount>()) }

    DisposableEffect(Unit) {
        val registration = AccountRepository.listenToPendingAccounts { pending = it }
        onDispose { registration.remove() }
    }

    val nextGuideId = (guides.maxOfOrNull { guide -> guide.guideId } ?: 0) + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Forest)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Manager dashboard",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = profile.fullName,
                    color = Cream,
                    fontSize = 13.sp
                )
            }

            TextButton(onClick = onLogout) {
                Text(text = "Log out", color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Accounts waiting for approval",
                    color = DeepForest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif
                )
            }

            if (pending.isEmpty()) {
                item {
                    Text(text = "No accounts are waiting.", color = Muted, fontSize = 13.sp)
                }
            } else {
                items(pending) { account ->
                    PendingAccountCard(
                        account = account,
                        onApprove = {
                            AccountRepository.approveAccount(account, nextGuideId)
                        },
                        onReject = {
                            AccountRepository.rejectAccount(account.userId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingAccountCard(
    account: PendingAccount,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val roleName = if (account.role == "LOCAL_GUIDE") "Local guide" else "Site owner"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = account.fullName,
                color = DeepForest,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = roleName, color = Forest, fontWeight = FontWeight.SemiBold)
            Text(text = account.email, color = Muted, fontSize = 13.sp)
            Text(text = account.details, color = Muted, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove) {
                    Text("Approve")
                }

                OutlinedButton(onClick = onReject) {
                    Text("Reject")
                }
            }
        }
    }
}