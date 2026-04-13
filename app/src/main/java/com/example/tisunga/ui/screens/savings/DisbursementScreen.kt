package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Disbursement
import com.example.tisunga.data.model.MemberSharePayout
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.viewmodel.SavingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisbursementScreen(
    navController: NavController,
    groupId: String,
    viewModel: SavingsViewModel,
    sessionManager: SessionManager
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") } // "request" or "approve"
    var rejectionReason by remember { mutableStateOf("") }
    
    val userRole = sessionManager.getGroupRole(groupId)?.lowercase() ?: "member"
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            viewModel.resetState()
        }
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
            viewModel.resetState()
        }
    }

    LaunchedEffect(groupId) {
        viewModel.getGroupSavingsData(groupId)
        viewModel.loadDisbursementHistory(groupId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Disbursement", fontWeight = Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Current Status Section
                    item {
                        CurrentDisbursementCard(
                            disbursement = uiState.currentDisbursement,
                            role = userRole,
                            totalGroupSavings = uiState.totalSavings,
                            onRequest = {
                                dialogType = "request"
                                showConfirmDialog = true
                            },
                            onApprove = {
                                dialogType = "approve"
                                showConfirmDialog = true
                            },
                            onReject = {
                                showRejectDialog = true
                            }
                        )
                    }

                    // 2. Member Payouts List (if any)
                    uiState.currentDisbursement?.let { current ->
                        if (current.memberShares.isNotEmpty()) {
                            item {
                                Text(stringResource(R.string.member_shares_title), fontWeight = Bold, fontSize = 16.sp)
                            }
                            items(current.memberShares) { payout ->
                                PayoutItem(payout)
                            }
                        }
                    }

                    // 3. History Section
                    if (uiState.history.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("History", fontWeight = Bold, fontSize = 16.sp)
                        }
                        items(uiState.history) { past ->
                            HistoryItem(past)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        val count = if (dialogType == "request") uiState.memberCount else uiState.currentDisbursement?.memberShares?.size ?: 0
        val amountStr = if (dialogType == "request") FormatUtils.formatMoney(uiState.totalSavings) else FormatUtils.formatMoney(uiState.currentDisbursement?.amount ?: 0.0)
        
        TisungaConfirmDialog(
            title = if (dialogType == "request") 
                stringResource(R.string.request_disbursement_button) 
            else stringResource(R.string.approve_disbursement_button),
            message = if (dialogType == "request") 
                stringResource(R.string.request_disbursement_confirm_msg, amountStr, count)
                else stringResource(R.string.approve_disbursement_confirm_msg, amountStr, count),
            isDestructive = dialogType == "approve",
            onConfirm = {
                if (dialogType == "request") {
                    viewModel.requestDisbursement(groupId)
                } else {
                    uiState.currentDisbursement?.let {
                        viewModel.approveDisbursement(groupId, it.id)
                    }
                }
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Reject Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Request", fontWeight = Bold) },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this disbursement request.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("e.g. Incomplete records") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        uiState.currentDisbursement?.let {
                            viewModel.rejectDisbursement(groupId, it.id, rejectionReason)
                        }
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    enabled = rejectionReason.isNotBlank()
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun CurrentDisbursementCard(
    disbursement: Disbursement?,
    role: String,
    totalGroupSavings: Double,
    onRequest: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (disbursement == null) {
                Text(stringResource(R.string.total_to_disburse_label), fontSize = 14.sp, color = TextSecondary)
                Text(FormatUtils.formatMoney(totalGroupSavings), fontSize = 28.sp, fontWeight = Bold, color = NavyBlue)
                Spacer(Modifier.height(16.dp))
                if (role == "chairperson") {
                    Button(
                        onClick = onRequest,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text(stringResource(R.string.request_disbursement_button), color = White, fontWeight = SemiBold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Waiting for Chairperson to initiate disbursement.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Disbursement Request", fontSize = 14.sp, color = TextSecondary)
                        Text(FormatUtils.formatMoney(disbursement.amount), fontSize = 24.sp, fontWeight = Bold, color = NavyBlue)
                    }
                    StatusBadge(disbursement.status)
                }
                
                Spacer(Modifier.height(8.dp))
                Text("Requested by ${disbursement.requestedByName ?: "Chairperson"}", fontSize = 12.sp, color = TextSecondary)
                
                if (disbursement.status == "PENDING") {
                    Spacer(Modifier.height(16.dp))
                    if (role == "treasurer") {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Text(stringResource(R.string.approve_disbursement_button), color = White, fontWeight = SemiBold)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent)
                        ) {
                            Text(stringResource(R.string.reject_button), color = RedAccent, fontWeight = SemiBold)
                        }
                    } else {
                        Text("Awaiting Treasurer's approval to release funds.", fontSize = 13.sp, color = NavyBlue, fontWeight = SemiBold)
                    }
                } else if (disbursement.status == "REJECTED") {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().background(RedAccent.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text("Reason: ${disbursement.rejectionReason}", color = RedAccent, fontSize = 13.sp)
                    }
                    if (role == "chairperson") {
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onRequest, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)) {
                            Text("Resubmit Request")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayoutItem(payout: MemberSharePayout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(payout.userName, fontWeight = Bold, fontSize = 15.sp)
                Text("Savings: ${FormatUtils.formatMoney(payout.memberSavings)}", fontSize = 12.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(FormatUtils.formatMoney(payout.shareAmount), fontWeight = Bold, color = GreenAccent)
                if (payout.status != "PENDING") {
                    Text(payout.status, fontSize = 10.sp, color = if (payout.status == "SENT") GreenAccent else RedAccent)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(disbursement: Disbursement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(FormatUtils.formatMoney(disbursement.amount), fontWeight = Bold)
                Text(disbursement.requestedAt.split("T")[0], fontSize = 12.sp, color = TextSecondary)
            }
            StatusBadge(disbursement.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "PENDING" -> Color(0xFFFF9800)
        "APPROVED" -> GreenAccent
        "REJECTED" -> RedAccent
        "COMPLETED" -> Color(0xFF4CAF50)
        else -> TextSecondary
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = Bold
        )
    }
}
