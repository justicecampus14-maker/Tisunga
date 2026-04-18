package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@Composable
fun AllLoansScreen(
    navController: NavController,
    viewModel: LoanViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    
    val filterActive = "Active"
    val filterPending = "Request"
    val filterHistory = "History"

    var selectedTab by remember { mutableStateOf(filterActive) }

    // Dialog States
    var showRepayDialog by remember { mutableStateOf(false) }
    var showConfirmActionDialog by remember { mutableStateOf(false) }
    var confirmActionType by remember { mutableStateOf("") } // "Approve", "Reject", "Clear"
    var selectedLoanForAction by remember { mutableStateOf<Loan?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (showConfirmActionDialog && selectedLoanForAction != null) {
        val title = when (confirmActionType) {
            "Approve" -> "Approve Loan"
            "Reject" -> "Reject Loan"
            "Clear" -> "Clear Loan"
            else -> ""
        }
        val message = when (confirmActionType) {
            "Approve" -> "Are you sure you want to approve this loan request for ${FormatUtils.formatMoney(selectedLoanForAction!!.principalAmount)}?"
            "Reject" -> "Are you sure you want to reject this loan request for ${FormatUtils.formatMoney(selectedLoanForAction!!.principalAmount)}?"
            "Clear" -> "Are you sure you want to clear the remaining balance of ${FormatUtils.formatMoney(selectedLoanForAction!!.remainingBalance)} for this loan?"
            else -> ""
        }
        val confirmText = when (confirmActionType) {
            "Approve" -> "Approve"
            "Reject" -> "Reject"
            "Clear" -> "Confirm"
            else -> "Confirm"
        }
        val isDestructive = confirmActionType == "Reject"

        TisungaConfirmDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            isDestructive = isDestructive,
            onConfirm = {
                when (confirmActionType) {
                    "Approve" -> viewModel.approveLoan(selectedLoanForAction!!.id, homeUiState.userPhone)
                    "Reject" -> viewModel.rejectLoan(selectedLoanForAction!!.id, "Rejected", homeUiState.userPhone)
                    "Clear" -> viewModel.repayLoan(selectedLoanForAction!!.id, selectedLoanForAction!!.remainingBalance, homeUiState.userPhone)
                }
                showConfirmActionDialog = false
            },
            onDismiss = { showConfirmActionDialog = false }
        )
    }

    if (showRepayDialog && selectedLoanForAction != null) {
        AllLoansRepayDialog(
            loan = selectedLoanForAction!!,
            onConfirm = { amount: Double ->
                viewModel.repayLoan(selectedLoanForAction!!.id, amount, homeUiState.userPhone)
                showRepayDialog = false
            },
            onDismiss = { showRepayDialog = false }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                viewModel.resetState()
            },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    viewModel.resetState()
                }) {
                    Text("Continue", color = NavyBlue, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Success") },
            text = { Text(uiState.successMessage) },
            shape = RoundedCornerShape(16.dp),
            containerColor = White
        )
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    val currentGroup = homeUiState.myGroups.firstOrNull()
    val groupId = currentGroup?.id ?: ""
    
    val myLoan = uiState.myLoans.firstOrNull { it.status.lowercase() == "active" }
    
    val filteredGroupLoans = when (selectedTab) {
        filterActive -> uiState.groupLoans.filter { it.status.lowercase() == "active" }
        filterPending -> uiState.groupLoans.filter { it.status.lowercase() == "pending" }
        filterHistory -> uiState.groupLoans.filter { it.status.lowercase() == "completed" || it.status.lowercase() == "rejected" }
        else -> uiState.groupLoans
    }

    LaunchedEffect(groupId) {
        viewModel.getMyLoans()
        if (groupId.isNotEmpty()) {
            viewModel.getGroupLoans(groupId)
        }
    }

    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { navController.navigate(Routes.APPLY_LOAN.replace("{groupId}", groupId)) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Apply Loan", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.LightGray.copy(0.2f)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Loans", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(homeUiState.userName, fontSize = 14.sp, color = TextSecondary)
                }
            }

            // Summary Card
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AllLoansSummaryCard(uiState.groupLoans)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("My Loan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                if (myLoan != null) {
                    AllLoansMySpecificLoanCard(myLoan) {
                        selectedLoanForAction = myLoan
                        showRepayDialog = true
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(0.3f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No active loan.", fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Members Loans Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Members Loans", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Requests", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = NavyBlue,
                        modifier = Modifier.clickable { selectedTab = filterPending }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AllLoansTabItem(filterActive, selectedTab == filterActive) { selectedTab = filterActive }
                    AllLoansTabItem(filterPending, selectedTab == filterPending) { selectedTab = filterPending }
                    AllLoansTabItem(filterHistory, selectedTab == filterHistory) { selectedTab = filterHistory }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Group Loan Cards
                if (filteredGroupLoans.isEmpty()) {
                    Text("No loans found.", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 20.dp))
                } else {
                    filteredGroupLoans.forEach { loan ->
                        AllLoansMembersSpecificLoanCard(
                            loan = loan,
                            onApprove = {
                                selectedLoanForAction = loan
                                confirmActionType = "Approve"
                                showConfirmActionDialog = true
                            },
                            onReject = {
                                selectedLoanForAction = loan
                                confirmActionType = "Reject"
                                showConfirmActionDialog = true
                            },
                            onClear = {
                                selectedLoanForAction = loan
                                confirmActionType = "Clear"
                                showConfirmActionDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AllLoansSummaryCard(loans: List<Loan>) {
    val totalRendered = loans.sumOf { it.principalAmount }
    val totalPayable = loans.sumOf { it.totalRepayable }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Rendered", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = FormatUtils.formatMoney(totalRendered),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Total payable", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = FormatUtils.formatMoney(totalPayable),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun AllLoansMySpecificLoanCard(loan: Loan, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Amount", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = FormatUtils.formatMoney(loan.principalAmount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Repayable", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundGray, RoundedCornerShape(10.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatMoney(loan.totalRepayable),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Interest", fontSize = 12.sp, color = TextSecondary)
                    Text("${loan.interestRate.toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val progress = if (loan.totalRepayable > 0) (1 - (loan.remainingBalance / loan.totalRepayable)).toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = NavyBlue,
                trackColor = Color.LightGray.copy(0.3f)
            )
            
            Text(
                text = "Remaining: ${FormatUtils.formatMoney(loan.remainingBalance)}",
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Due : ${FormatUtils.formatDate(loan.dueDate)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                
                Surface(
                    onClick = onClear,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clear", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AllLoansMembersSpecificLoanCard(
    loan: Loan,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClear: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loan.borrowerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isExpanded) 180f else 0f),
                    tint = TextSecondary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Text("Amount", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundGray, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = FormatUtils.formatMoney(loan.principalAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (loan.status.lowercase() == "pending") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                        ) {
                            Text("Approve")
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Repayable", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundGray, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = FormatUtils.formatMoney(loan.totalRepayable),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Interest", fontSize = 14.sp)
                            Text("${loan.interestRate.toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val progress = if (loan.totalRepayable > 0) (1 - (loan.remainingBalance / loan.totalRepayable)).toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = NavyBlue,
                        trackColor = Color.LightGray.copy(0.3f)
                    )

                    Text(
                        text = "Remaining: ${FormatUtils.formatMoney(loan.remainingBalance)}",
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Due : ${FormatUtils.formatDate(loan.dueDate)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)

                        Surface(
                            onClick = onClear,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.LightGray.copy(0.6f)),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Clear", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllLoansTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) NavyBlue else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(0.5f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) White else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun AllLoansRepayDialog(
    loan: Loan,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(loan.remainingBalance.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Clear Loan Balance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Amount to clear:",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter amount") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextPrimary)
                    }
                    Button(
                        onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Pay Now", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
