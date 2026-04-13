package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
fun MyLoansScreen(
    navController: NavController,
    groupId: String,
    viewModel: LoanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatus by remember { mutableStateOf("ACTIVE") }

    LaunchedEffect(Unit) {
        viewModel.getMyLoans()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Loans", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate("group_loans/$groupId") }) {
                        Text("Group Ledger", color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("apply_loan/$groupId") },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Apply for Loan") },
                containerColor = NavyBlue,
                contentColor = White,
                shape = RoundedCornerShape(16.dp)
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
                listOf("ACTIVE", "PENDING", "COMPLETED", "REJECTED").forEach { status ->
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

            val filteredLoans = uiState.myLoans.filter { it.status == selectedStatus }

            if (filteredLoans.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No $selectedStatus loans found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredLoans, key = { it.id }) { loan ->
                        MyLoanItem(
                            loan = loan,
                            onRepayClick = { 
                                // We'll navigate to RepayLoanScreen
                                // Assuming we pass loan object via navigation or shared viewmodel
                                // For simplicity, we'll use a route that takes loanId and amount
                                navController.navigate("repay_loan/${loan.id}")
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun MyLoanItem(loan: Loan, onRepayClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(loan.group?.name ?: "Group Loan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                    Text("MK ${String.format("%,.0f", loan.principalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = TextPrimary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when(loan.status) {
                        "ACTIVE" -> GreenAccent.copy(alpha = 0.1f)
                        "PENDING" -> Color.Blue.copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        loan.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(loan.status) {
                            "ACTIVE" -> GreenAccent
                            "PENDING" -> Color.Blue
                            else -> Color.Gray
                        }
                    )
                }
            }

            if (loan.status == "ACTIVE") {
                Spacer(modifier = Modifier.height(16.dp))
                val progress = (1 - (loan.remainingBalance / loan.totalRepayable)).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = GreenAccent,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MK ${String.format("%,.0f", loan.remainingBalance)} left", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Due: ${loan.dueDate.take(10)}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onRepayClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Make a Repayment", color = White, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Applied on: ${loan.createdAt.take(10)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (loan.purpose != null) {
                    Text(
                        "Purpose: ${loan.purpose}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
