package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.LoanCard
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun GroupLoansScreen(navController: NavController, groupId: Int, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    val tabMyLoans = stringResource(R.string.tab_my_loans)
    val tabMemberLoans = stringResource(R.string.tab_member_loans)
    val filterAll = stringResource(R.string.filter_all)
    val filterRequest = stringResource(R.string.filter_request)
    val filterRejected = stringResource(R.string.filter_rejected)

    var selectedTab by remember { mutableStateOf(tabMemberLoans) }
    var selectedFilter by remember { mutableStateOf(filterAll) }

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
            HomeHeader(userPhone = "0882752624", navController = navController, onMenuClick = { })

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
                    Text(stringResource(R.string.loans_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.placeholder_group_name), fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupLoanTabButton(tabMyLoans, selectedTab == tabMyLoans, Modifier.weight(1f)) { 
                    selectedTab = tabMyLoans
                    navController.navigate("my_loans/$groupId")
                }
                GroupLoanTabButton(tabMemberLoans, selectedTab == tabMemberLoans, Modifier.weight(1f)) { selectedTab = tabMemberLoans }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupLoanFilterChip(label = filterAll, isSelected = selectedFilter == filterAll, onClick = { selectedFilter = filterAll })
                GroupLoanFilterChip(label = filterRequest, isSelected = selectedFilter == filterRequest, onClick = { selectedFilter = filterRequest })
                GroupLoanFilterChip(label = filterRejected, isSelected = selectedFilter == filterRejected, onClick = { selectedFilter = filterRejected })
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.groupLoans) { loan ->
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
                        showApproveReject = loan.status == "pending",
                        onApproveClick = { viewModel.approveLoanWithConfirmation(loan.id) },
                        onRejectClick = { viewModel.rejectLoan(loan.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupLoanTabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) NavyBlue else White,
            contentColor = if (isSelected) White else TextPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(if (isSelected) 4.dp else 0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun GroupLoanFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) NavyBlue else White,
        border = if (isSelected) null else BorderStroke(1.dp, DividerColor)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = if (isSelected) White else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
