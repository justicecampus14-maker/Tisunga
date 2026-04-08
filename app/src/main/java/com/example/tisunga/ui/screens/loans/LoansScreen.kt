package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.components.GroupLoansSummaryCard
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.components.SuccessDialog
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.HomeViewModel
import java.util.Locale

@Composable
fun AllLoansScreen(navController: NavController, viewModel: LoanViewModel, homeViewModel: HomeViewModel, groupId: Int? = null) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    
    val filterActive = "Active"
    val filterPending = "Requests"
    val filterHistory = "History"

    var selectedTab by remember { mutableStateOf(filterActive) }

    // Dialog States
    var showRepayDialog by remember { mutableStateOf(false) }
    var showConfirmActionDialog by remember { mutableStateOf(false) }
    var confirmActionType by remember { mutableStateOf("") } 
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
            "Approve" -> "Are you sure you want to approve this loan request for MK ${String.format(Locale.US, "%,.0f", selectedLoanForAction!!.amount)}?"
            "Reject" -> "Are you sure you want to reject this loan request for MK ${String.format(Locale.US, "%,.0f", selectedLoanForAction!!.amount)}?"
            "Clear" -> "Are you sure you want to clear the remaining balance of MK ${String.format(Locale.US, "%,.0f", selectedLoanForAction!!.remainingAmount)} for this loan?"
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
                    "Approve" -> viewModel.approveLoanWithConfirmation(selectedLoanForAction!!.id)
                    "Reject" -> viewModel.rejectLoan(selectedLoanForAction!!.id)
                    "Clear" -> viewModel.repayLoan(selectedLoanForAction!!.id, selectedLoanForAction!!.remainingAmount)
                }
                showConfirmActionDialog = false
            },
            onDismiss = { showConfirmActionDialog = false }
        )
    }

    if (showRepayDialog && selectedLoanForAction != null) {
        RepayLoanDialog(
            loan = selectedLoanForAction!!,
            onConfirm = { amount ->
                viewModel.repayLoan(selectedLoanForAction!!.id, amount)
                showRepayDialog = false
            },
            onDismiss = { showRepayDialog = false }
        )
    }

    if (showSuccessDialog) {
        SuccessDialog(
            message = uiState.successMessage,
            onContinue = {
                showSuccessDialog = false
                viewModel.resetState()
            }
        )
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    val currentGroup = if (groupId != null) {
        homeUiState.myGroups.find { it.id == groupId } ?: homeUiState.myGroups.firstOrNull()
    } else {
        homeUiState.myGroups.firstOrNull()
    }
    val myLoan = uiState.myLoans.firstOrNull { it.status.lowercase() == "active" }
    
    val filteredGroupLoans = when (selectedTab) {
        filterActive -> uiState.groupLoans.filter { it.status.lowercase() == "active" }
        filterPending -> uiState.groupLoans.filter { it.status.lowercase() == "pending" }
        filterHistory -> uiState.groupLoans.filter { it.status.lowercase() == "completed" }
        else -> uiState.groupLoans
    }

    LaunchedEffect(currentGroup?.id) {
        viewModel.getMyLoans()
        currentGroup?.id?.let { viewModel.getGroupLoans(it) }
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Loans",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = BackgroundGray,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { navController.navigate(Routes.APPLY_LOAN.replace("{groupId}", currentGroup?.id.toString())) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Apply for a Loan", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = currentGroup?.name ?: "No Group Selected",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
            )

            GroupLoansSummaryCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (myLoan != null) {
                Text(
                    "My Active Loan", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = NavyBlue,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                MyLoanCard(myLoan) {
                    selectedLoanForAction = myLoan
                    showRepayDialog = true
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                "Members Loans", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold, 
                color = NavyBlue,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabItem(filterActive, selectedTab == filterActive) { selectedTab = filterActive }
                TabItem(filterPending, selectedTab == filterPending) { selectedTab = filterPending }
                TabItem(filterHistory, selectedTab == filterHistory) { selectedTab = filterHistory }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group Loan Cards
            if (filteredGroupLoans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No loans found in this category", color = TextSecondary)
                }
            } else {
                filteredGroupLoans.forEach { loan ->
                    GroupLoanCard(
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
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MyLoanCard(loan: Loan, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Borrowed Amount", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = "MK ${String.format(Locale.US, "%,.0f", loan.amount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Surface(color = GreenLight, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "${loan.interestRate.toInt()}% Interest",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Repayable", fontSize = 12.sp, color = TextSecondary)
                    Text("MK ${String.format(Locale.US, "%,.0f", loan.repayableAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Due Date", fontSize = 12.sp, color = TextSecondary)
                    Text(loan.dueDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { loan.percentRepaid },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = NavyBlue,
                trackColor = BackgroundGray
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${(loan.percentRepaid * 100).toInt()}% Repaid", fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = "Remaining: MK ${String.format(Locale.US, "%,.0f", loan.remainingAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue.copy(alpha = 0.1f), contentColor = NavyBlue)
            ) {
                Text("Make Repayment", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun GroupLoanCard(
    loan: Loan,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(NavyBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(loan.memberName.take(1).uppercase(), color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(loan.memberName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("MK ${String.format(Locale.US, "%,.0f", loan.amount)}", fontSize = 13.sp, color = TextSecondary)
                    }
                }
                
                Surface(
                    color = when(loan.status.lowercase()) {
                        "active" -> GreenLight
                        "pending" -> Color(0xFFFFF9C4)
                        else -> BackgroundGray
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = loan.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(loan.status.lowercase()) {
                            "active" -> GreenAccent
                            "pending" -> Color(0xFFFBC02D)
                            else -> TextSecondary
                        }
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                if (loan.status.lowercase() == "pending") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                        ) {
                            Text("Approve")
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Repayable", fontSize = 11.sp, color = TextSecondary)
                            Text("MK ${String.format(Locale.US, "%,.0f", loan.repayableAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Due Date", fontSize = 11.sp, color = TextSecondary)
                            Text(loan.dueDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { loan.percentRepaid },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = NavyBlue,
                        trackColor = BackgroundGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClear,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BackgroundGray, contentColor = TextPrimary)
                    ) {
                        Text("Record Repayment", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RepayLoanDialog(
    loan: Loan,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(loan.remainingAmount.toString()) }

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
                    text = "Loan Repayment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Member: ${loan.memberName}",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount to Repay") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray.copy(alpha = 0.5f),
                        focusedContainerColor = BackgroundGray.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = NavyBlue
                    ),
                    prefix = { Text("MK ", fontWeight = FontWeight.Bold) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Confirm", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) NavyBlue else White,
        border = if (isSelected) null else BorderStroke(1.dp, DividerColor),
        shadowElevation = if (isSelected) 2.dp else 0.dp
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
