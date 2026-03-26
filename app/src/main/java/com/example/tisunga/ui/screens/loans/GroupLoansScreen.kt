package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.LoanCard
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun GroupLoansScreen(navController: NavController, groupId: Int, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("Member Loans") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.getGroupLoans(groupId)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, type = "C") },
        containerColor = BackgroundLightGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HomeHeader("Michael", "0882752624", navController)

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp).clickable { navController.popBackStack() },
                    shape = RoundedCornerShape(8.dp),
                    color = White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Loans", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Doman Group", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton("My Loans", selectedTab == "My Loans", Modifier.weight(1f)) { 
                    selectedTab = "My Loans"
                    navController.navigate("my_loans/$groupId")
                }
                TabButton("Member Loans", selectedTab == "Member Loans", Modifier.weight(1f)) { selectedTab = "Member Loans" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(label = "All", isSelected = selectedFilter == "All", onClick = { selectedFilter = "All" })
                FilterChip(label = "Request", isSelected = selectedFilter == "Request", onClick = { selectedFilter = "Request" })
                FilterChip(label = "Rejected", isSelected = selectedFilter == "Rejected", onClick = { selectedFilter = "Rejected" })
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.groupLoans) { loan ->
                    LoanCard(
                        groupName = loan.groupName,
                        approvedBy = "Aproved by ${loan.approvedBy ?: "N/A"}",
                        totalBorrowed = loan.amount,
                        interestRate = loan.interestRate,
                        repayableAmount = loan.repayableAmount,
                        remaining = loan.remainingAmount,
                        percentRepaid = loan.percentRepaid,
                        dueDate = loan.dueDate,
                        status = loan.status,
                        showApproveReject = loan.status == "pending",
                        onApproveClick = { viewModel.approveLoanWithConfirmation(loan.id) },
                        onRejectClick = { viewModel.rejectLoan(loan.id) }
                    )
                }
            }
        }
    }
}
