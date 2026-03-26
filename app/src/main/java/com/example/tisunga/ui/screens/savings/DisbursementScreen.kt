package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.SavingsViewModel

@Composable
fun DisbursementScreen(navController: NavController, groupId: Int, viewModel: SavingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") } // "request" or "approve"
    
    // Placeholder role
    val userRole = "chairperson" // Should come from SessionManager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Disburse Funds", fontSize = 20.sp, fontWeight = Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning card if cycle ended
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️ Savings cycle ended 21st May 2027", fontWeight = Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total to Disburse", fontSize = 14.sp, color = TextSecondary)
                Text("MK 2,000,000.00", fontSize = 32.sp, fontWeight = Bold, color = NavyBlue)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Member Shares", fontWeight = Bold, fontSize = 16.sp)
        
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(uiState.memberShares) { share ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(share.userName)
                        Text("MK ${com.example.tisunga.utils.FormatUtils.formatNumber(share.shareAmount)}", fontWeight = Bold)
                    }
                    HorizontalDivider(color = DividerColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (userRole == "chairperson") {
            Button(
                onClick = { 
                    dialogType = "request"
                    showConfirmDialog = true 
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Request Disbursement", color = White, fontSize = 16.sp, fontWeight = SemiBold)
            }
        } else if (userRole == "treasurer") {
            Column {
                Button(
                    onClick = { 
                        dialogType = "approve"
                        showConfirmDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Approve Disbursement", color = White, fontSize = 16.sp, fontWeight = SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* Reject logic */ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent)
                ) {
                    Text("Reject", color = RedAccent, fontSize = 16.sp, fontWeight = SemiBold)
                }
            }
        }
    }

    if (showConfirmDialog) {
        if (dialogType == "request") {
            TisungaConfirmDialog(
                title = "Request Disbursement",
                message = "Request disbursement of MK 2,000,000.00 to 6 members? Treasurer must approve before funds are sent.",
                onConfirm = {
                    viewModel.requestDisbursement(groupId)
                    showConfirmDialog = false
                },
                onDismiss = { showConfirmDialog = false }
            )
        } else {
            TisungaConfirmDialog(
                title = "Approve Disbursement",
                message = "Send MK 333,333.33 to all 6 members mobile wallets? This cannot be undone.",
                onConfirm = {
                    viewModel.approveDisbursement(groupId)
                    showConfirmDialog = false
                },
                onDismiss = { showConfirmDialog = false }
            )
        }
    }
}
