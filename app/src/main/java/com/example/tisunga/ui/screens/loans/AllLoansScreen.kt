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
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.components.LoanCard
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun AllLoansScreen(navController: NavController, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    val filterActive = stringResource(R.string.filter_active)
    val filterPending = stringResource(R.string.filter_pending)
    val filterClosed = stringResource(R.string.filter_closed)

    var selectedTab by remember { mutableStateOf(filterActive) }

    LaunchedEffect(Unit) {
        viewModel.getMyLoans()
    }

    val filteredLoans = when (selectedTab) {
        filterActive -> uiState.myLoans.filter { it.status == "active" }
        filterPending -> uiState.myLoans.filter { it.status == "pending" }
        filterClosed -> uiState.myLoans.filter { it.status == "completed" }
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
            Text(stringResource(R.string.all_loans_title), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoanTab(label = filterActive, isSelected = selectedTab == filterActive, modifier = Modifier.weight(1f)) { selectedTab = filterActive }
            LoanTab(label = filterPending, isSelected = selectedTab == filterPending, modifier = Modifier.weight(1f)) { selectedTab = filterPending }
            LoanTab(label = filterClosed, isSelected = selectedTab == filterClosed, modifier = Modifier.weight(1f)) { selectedTab = filterClosed }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredLoans) { loan ->
                LoanCard(
                    groupName = loan.groupName,
                    approvedBy = stringResource(R.string.loan_approved_by_simple, loan.approvedBy ?: stringResource(R.string.not_available)),
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
