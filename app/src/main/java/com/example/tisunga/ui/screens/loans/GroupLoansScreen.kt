package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupLoansScreen(navController: NavController, groupId: String, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatus by remember { mutableStateOf("PENDING") }
    var showRejectDialog by remember { mutableStateOf<String?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(groupId) {
        viewModel.getGroupLoans(groupId)
    }

    if (showRejectDialog != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = null },
            title = { Text("Reject Loan Application") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    placeholder = { Text("Enter reason for rejection") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog?.let { viewModel.rejectLoan(it, rejectReason, groupId) }
                        showRejectDialog = null
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Reject", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Loans", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            // Status Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("PENDING", "ACTIVE", "COMPLETED", "REJECTED").forEach { status ->
                    val isSelected = selectedStatus == status
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) NavyBlue else BackgroundGray,
                        onClick = { selectedStatus = status }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                status.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (isSelected) White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            val filteredLoans = uiState.groupLoans.filter { it.status == selectedStatus }

            if (filteredLoans.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No $selectedStatus loans found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLoans, key = { it.id }) { loan ->
                        GroupLoanItem(
                            loan = loan,
                            onApprove = { viewModel.approveLoan(loan.id, groupId) },
                            onReject = { showRejectDialog = loan.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupLoanItem(
    loan: Loan,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(loan.borrowerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(loan.purpose ?: "Personal Loan", fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    "MK ${String.format("%,.0f", loan.principalAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = NavyBlue
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LoanInfoLabel("Duration", "${loan.durationMonths} Months")
                LoanInfoLabel("Interest", "MK ${String.format("%,.0f", loan.totalRepayable - loan.principalAmount)}")
                LoanInfoLabel("Total", "MK ${String.format("%,.0f", loan.totalRepayable)}")
            }

            if (loan.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", color = White, fontSize = 14.sp)
                    }
                }
            } else if (loan.status == "ACTIVE") {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (1 - (loan.remainingBalance / loan.totalRepayable)).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = GreenAccent,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    "${(progress * 100).toInt()}% repaid (MK ${String.format("%,.0f", loan.remainingBalance)} remaining)",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LoanInfoLabel(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
