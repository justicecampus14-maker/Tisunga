package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.tisunga.ui.components.LoanCard
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun AllLoansScreen(navController: NavController, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("Active") }

    LaunchedEffect(Unit) {
        viewModel.getMyLoans()
    }

    val filteredLoans = when (selectedTab) {
        "Active" -> uiState.myLoans.filter { it.status == "active" }
        "Pending" -> uiState.myLoans.filter { it.status == "pending" }
        "Closed" -> uiState.myLoans.filter { it.status == "completed" }
        else -> uiState.myLoans
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLightGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp).clickable { navController.popBackStack() },
                shape = RoundedCornerShape(8.dp),
                color = White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("All Loans", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoanTab(label = "Active", isSelected = selectedTab == "Active", modifier = Modifier.weight(1f)) { selectedTab = "Active" }
            LoanTab(label = "Pending", isSelected = selectedTab == "Pending", modifier = Modifier.weight(1f)) { selectedTab = "Pending" }
            LoanTab(label = "Closed", isSelected = selectedTab == "Closed", modifier = Modifier.weight(1f)) { selectedTab = "Closed" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredLoans) { loan ->
                LoanCard(
                    groupName = loan.groupName,
                    approvedBy = "Approved by ${loan.approvedBy ?: "N/A"}",
                    totalBorrowed = loan.amount,
                    interestRate = loan.interestRate,
                    repayableAmount = loan.repayableAmount,
                    remaining = loan.remainingAmount,
                    percentRepaid = loan.percentRepaid,
                    dueDate = loan.dueDate,
                    status = loan.status,
                    onRepayClick = { /* Navigate to Repay */ }
                )
            }
        }
    }
}

@Composable
fun LoanTab(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() }
            .then(if (!isSelected) Modifier.border(1.dp, DividerColor, RoundedCornerShape(10.dp)) else Modifier),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) NavyBlue else White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) White else TextSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}
