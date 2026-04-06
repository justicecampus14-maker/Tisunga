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
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.LoanCard
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun MyLoansScreen(navController: NavController, groupId: Int, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    val tabMyLoans = stringResource(R.string.tab_my_loans)
    val tabMemberLoans = stringResource(R.string.tab_member_loans)
    val filterActive = stringResource(R.string.filter_active)
    val filterPending = stringResource(R.string.filter_pending)
    val filterHistory = stringResource(R.string.filter_history)

    var selectedTab by remember { mutableStateOf(tabMyLoans) }
    var selectedFilter by remember { mutableStateOf(filterActive) }

    LaunchedEffect(Unit) {
        viewModel.getMyLoans()
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
                MyLoanTabButton(tabMyLoans, selectedTab == tabMyLoans, Modifier.weight(1f)) { selectedTab = tabMyLoans }
                MyLoanTabButton(tabMemberLoans, selectedTab == tabMemberLoans, Modifier.weight(1f)) { 
                    selectedTab = tabMemberLoans
                    navController.navigate("group_loans/$groupId")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == tabMyLoans) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MyLoanFilterChip(label = filterActive, isSelected = selectedFilter == filterActive, onClick = { selectedFilter = filterActive })
                    MyLoanFilterChip(label = filterPending, isSelected = selectedFilter == filterPending, onClick = { selectedFilter = filterPending })
                    MyLoanFilterChip(label = filterHistory, isSelected = selectedFilter == filterHistory, onClick = { selectedFilter = filterHistory })
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Featured Loan Card placeholder as per PDF page 2
                    item {
                        FeaturedLoanCard(onRepayClick = { /* Repay dialog */ })
                    }
                }
            }
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            Button(
                onClick = { navController.navigate("apply_loan/$groupId") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(stringResource(R.string.apply_loan_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MyLoanTabButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(48.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) White else Color(0xFFD1D5DB)),
        elevation = if (isSelected) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.SemiBold, color = if (isSelected) NavyBlue else TextSecondary)
        }
    }
}

@Composable
fun MyLoanFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) White else Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, DividerColor) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NavyBlue else TextSecondary
        )
    }
}

@Composable
fun FeaturedLoanCard(onRepayClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.loan_group_name_label, stringResource(R.string.placeholder_group_name)), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Surface(color = Color(0xFFFCE4EC), shape = RoundedCornerShape(4.dp)) {
                    Text(stringResource(R.string.loan_status_active), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFE91E63), fontSize = 12.sp)
                }
            }
            Text(stringResource(R.string.loan_approved_by, "Laston Mzumala", "chairperson", "Feb 01 2026"), fontSize = 12.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(stringResource(R.string.amount_mk, "650,000"), fontWeight = FontWeight.Bold, fontSize = 32.sp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(stringResource(R.string.loan_repaid_percent, 50), fontSize = 12.sp, color = TextSecondary)
                    LinearProgressIndicator(progress = { 0.5f }, color = PurpleProgress, trackColor = DividerColor, modifier = Modifier.width(100.dp).height(6.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.loan_remaining_amount, "50,000"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(stringResource(R.string.loan_due_date, "nov 04, 2026"), color = RedAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.repay_now_link),
                modifier = Modifier.align(Alignment.End).clickable { onRepayClick() },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}
