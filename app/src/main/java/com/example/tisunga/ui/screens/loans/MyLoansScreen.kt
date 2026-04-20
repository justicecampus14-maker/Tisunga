package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@Composable
fun MyLoansScreen(
    navController: NavController,
    groupId: String,
    viewModel: LoanViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    
    val filterActive = "Active"
    val filterPending = "Request"
    val filterHistory = "History"

    var selectedTab by remember { mutableStateOf(filterActive) }

    // Repayment Dialog State
    var showRepayDialog by remember { mutableStateOf(false) }
    var selectedLoanForRepay by remember { mutableStateOf<Loan?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (showRepayDialog && selectedLoanForRepay != null) {
        RepayLoanDialog(
            loan = selectedLoanForRepay!!,
            onConfirm = { amount ->
                viewModel.repayLoan(selectedLoanForRepay!!.id, amount, homeUiState.userPhone)
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
                    Text("Continue", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Success") },
            text = { Text(uiState.successMessage) },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    val filteredLoans = when (selectedTab) {
        filterActive -> uiState.myLoans.filter { it.status.lowercase() == "active" }
        filterPending -> uiState.myLoans.filter { it.status.lowercase() == "pending" }
        filterHistory -> uiState.myLoans.filter { it.status.lowercase() == "completed" || it.status.lowercase() == "rejected" }
        else -> uiState.myLoans
    }

    LaunchedEffect(groupId) {
        viewModel.getMyLoans()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply Loan", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
            // Profile Header
            MyLoansHeaderSection(homeUiState.userName, homeUiState.userPhone, navController)

            // Screen Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("My Loans", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MyLoansTabItem(filterActive, selectedTab == filterActive) { selectedTab = filterActive }
                MyLoansTabItem(filterPending, selectedTab == filterPending) { selectedTab = filterPending }
                MyLoansTabItem(filterHistory, selectedTab == filterHistory) { selectedTab = filterHistory }
            }

            // Loan Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredLoans.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No loans found for this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    filteredLoans.forEach { loan ->
                        MySpecificLoanCard(
                            loan = loan,
                            onClear = {
                                selectedLoanForRepay = loan
                                showRepayDialog = true
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MyLoansHeaderSection(userName: String, userPhone: String, navController: NavController) {
    val initials = if (userName.isNotEmpty()) {
        userName.split(" ").filter { it.isNotEmpty() }.let { parts ->
            if (parts.size >= 2) {
                "${parts[0][0]}${parts[1][0]}".uppercase()
            } else if (parts.isNotEmpty()) {
                parts[0][0].toString().uppercase()
            } else ""
        }
    } else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(userPhone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        IconButton(
            onClick = { navController.navigate(Routes.NOTIFICATIONS) },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun MySpecificLoanCard(loan: Loan, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Loan Amount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatMoney(loan.principalAmount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (loan.status.lowercase() == "active") Color(0xFFE8F5E9).copy(alpha = 0.1f) else Color(0xFFFFF3E0).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = loan.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = if (loan.status.lowercase() == "active") Color(0xFF4CAF50) else Color(0xFFF57C00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Repayable", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatMoney(loan.totalRepayable),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Interest", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${loan.interestRate.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (loan.status.lowercase() == "active") {
                Spacer(modifier = Modifier.height(16.dp))
                val progress = if (loan.totalRepayable > 0) (1 - (loan.remainingBalance / loan.totalRepayable)).toFloat() else 0f
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                
                Text(
                    text = "Remaining: ${FormatUtils.formatMoney(loan.remainingBalance)}",
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Due: ${FormatUtils.formatDate(loan.dueDate)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Button(
                        onClick = onClear,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun MyLoansTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RepayLoanDialog(
    loan: Loan,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(loan.remainingBalance.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Clear Loan Balance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Amount to clear:",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter amount") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Pay Now", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
