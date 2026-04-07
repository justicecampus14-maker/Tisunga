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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.components.GroupLoansSummaryCard
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@Composable
fun AllLoansScreen(navController: NavController, viewModel: LoanViewModel, homeViewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    
    val filterActive = "Active"
    val filterPending = "Request"
    val filterHistory = "History"

    var selectedTab by remember { mutableStateOf(filterActive) }

    val currentGroup = homeUiState.myGroups.firstOrNull()
    val myLoan = uiState.myLoans.firstOrNull { it.status == "active" }
    
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

            // Summary and My Loan Section
            Column(modifier = Modifier.padding(16.dp)) {
                GroupLoansSummaryCard()
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("My Loan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                if (myLoan != null) {
                    MyLoanCard(myLoan) {
                        // On Clear
                    }
                }
            }

            // Group Loans Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Members Loans", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                    TabItem(filterActive, selectedTab == filterActive) { selectedTab = filterActive }
                    TabItem(filterPending, selectedTab == filterPending) { selectedTab = filterPending }
                    TabItem(filterHistory, selectedTab == filterHistory) { selectedTab = filterHistory }
                }

                if (selectedTab == filterActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Member Selector Pill (from sketch)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DividerColor),
                        color = White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Laston Mzumala", fontSize = 15.sp, color = TextPrimary)
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Group Loan Cards
                filteredGroupLoans.forEach { loan ->
                    GroupLoanCard(
                        loan = loan,
                        onApprove = { viewModel.approveLoanWithConfirmation(loan.id) },
                        onReject = { viewModel.rejectLoan(loan.id) },
                        onClear = { /* Clear logic */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB/Bottom button
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, userPhone: String, navController: NavController) {
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
                .background(NavyBlue, CircleShape)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = White, fontWeight = FontWeight.Bold)
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
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
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
fun MyLoanCard(loan: Loan, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Amount Section
            Text("Amount", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = BackgroundGray
            ) {
                Text(
                    text = "MK ${String.format("%,.0f", loan.amount)}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Approved By Section
            Text("Approved by : ${loan.approvedBy ?: "N/A"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("chairperson", fontSize = 12.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            // Repayable & Interest Section
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Repayable", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        color = BackgroundGray
                    ) {
                        Text(
                            text = "MK ${String.format("%,.0f", loan.repayableAmount)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
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

            // Progress Bar
            LinearProgressIndicator(
                progress = { loan.percentRepaid },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = NavyBlue,
                trackColor = DividerColor
            )
            
            Text(
                text = "Remaining: MK ${String.format("%,.0f", loan.remainingAmount)}",
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Due : ${loan.dueDate}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                
                Surface(
                    modifier = Modifier.clickable { onClear() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NavyBlue),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clear", color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = NavyBlue, modifier = Modifier.size(16.dp))
                    }
                }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Member Name and Expand/Collapse Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loan.memberName ?: "Unknown Member",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).rotate(180f), // Flipped to match "up" chevron in sketch
                    tint = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Amount Section
            Text("Amount", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = BackgroundGray
            ) {
                Text(
                    text = "MK ${String.format("%,.0f", loan.amount)}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (loan.status.lowercase() == "pending") {
                // Pending Style (Requests)
                Text("Approved by : chairperson", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("role", fontSize = 12.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                // Active Style (Matches Sketch)
                Text("Approved by : ${loan.approvedBy ?: "N/A"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("chairperson", fontSize = 12.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(16.dp))

                // Repayable & Interest Section
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Repayable", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            color = BackgroundGray
                        ) {
                            Text(
                                text = "MK ${String.format("%,.0f", loan.repayableAmount)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
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

                // Progress Bar
                LinearProgressIndicator(
                    progress = { loan.percentRepaid },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = NavyBlue,
                    trackColor = DividerColor
                )
                
                Text(
                    text = "Remaining: MK ${String.format("%,.0f", loan.remainingAmount)}",
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Due : ${loan.dueDate}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    
                    Surface(
                        modifier = Modifier.clickable { onClear() },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NavyBlue),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Clear", color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = NavyBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
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
