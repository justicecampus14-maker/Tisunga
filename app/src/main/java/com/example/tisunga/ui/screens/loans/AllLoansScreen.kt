package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@Composable
fun AllLoansScreen(navController: NavController, viewModel: LoanViewModel, homeViewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    
    val filterActive = "Active"
    val filterPending = "Pending"
    val filterHistory = "History"

    var selectedTab by remember { mutableStateOf(filterActive) }

    LaunchedEffect(Unit) {
        viewModel.getMyLoans()
    }

    val currentGroup = homeUiState.myGroups.firstOrNull()
    val myLoan = uiState.myLoans.firstOrNull { it.status == "active" }
    
    val filteredGroupLoans = when (selectedTab) {
        filterActive -> uiState.groupLoans.filter { it.status == "active" }
        filterPending -> uiState.groupLoans.filter { it.status == "pending" }
        filterHistory -> uiState.groupLoans.filter { it.status == "completed" }
        else -> uiState.groupLoans
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
                    onClick = { navController.navigate(Routes.APPLY_LOAN.replace("{groupId}", currentGroup?.id.toString())) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Apply Loan", color = White, fontWeight = FontWeight.Bold)
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
            // Top Profile Header
            HeaderSection(homeUiState.userName, homeUiState.userPhone, navController)

            // Screen Title & Group Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray.copy(0.3f)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Loans", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(currentGroup?.name ?: "Doman Group", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // My Loan Section
            Column(modifier = Modifier.padding(16.dp)) {
                Text("My Loan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                if (myLoan != null) {
                    MyLoanCard(myLoan)
                }
            }

            // Group Loans Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Group Loans", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Requests", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = NavyBlue,
                        modifier = Modifier.clickable { /* Navigate to requests */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
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
                filteredGroupLoans.forEach { loan ->
                    GroupLoanCard(loan)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB/Bottom button
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, userPhone: String, navController: NavController) {
    val initials = if (userName.isNotEmpty()) userName.take(1).uppercase() else "M"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NavyBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hi, $userName", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Good morning", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE8E8E8).copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(userPhone, fontSize = 13.sp)
            }
        }

        IconButton(
            onClick = { navController.navigate(Routes.NOTIFICATIONS) },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun MyLoanCard(loan: Loan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MK ${String.format("%,.0f", loan.amount)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text("active", color = Color(0xFFE57373), fontSize = 12.sp)
            }
            Text(
                "Approved by ${loan.approvedBy}\nchairperson. ${loan.approvalDate}",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    "Remaining: MK${String.format("%,.0f", loan.remainingAmount)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Due  ${loan.dueDate}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    "Repay Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    modifier = Modifier.clickable { /* Action */ }
                )
            }
        }
    }
}

@Composable
fun GroupLoanCard(loan: Loan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(loan.memberName ?: "Mphatso Phiri", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                "Approved by ${loan.approvedBy}\nchairperson. ${loan.approvalDate}",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Total borrowed: ", fontSize = 13.sp, color = TextPrimary)
                Text("MK ${String.format("%,.2f", loan.amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${(loan.percentRepaid * 100).toInt()}% repaid", fontSize = 11.sp, color = TextSecondary)
                Text(
                    "Remaining: MK${String.format("%,.0f", loan.remainingAmount)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            LinearProgressIndicator(
                progress = { loan.percentRepaid },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF7C4DFF),
                trackColor = Color(0xFFF0F0F0)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Due ${loan.dueDate}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(85.dp)
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) White else Color.LightGray.copy(alpha = 0.2f),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label, 
                fontSize = 13.sp, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextPrimary else TextSecondary
            )
        }
    }
}
