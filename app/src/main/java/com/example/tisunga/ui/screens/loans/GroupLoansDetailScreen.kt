package com.example.tisunga.ui.screens.loans

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils.formatNumber
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) White else Color(0xFFDDDDDD),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NavyBlue else TextSecondary
        )
    }
}

@Composable
fun GroupLoansDetailScreen(navController: NavController, groupId: String, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("Active") }

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
            HomeHeader("0882752624", 0, navController, {})

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

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("My Loan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactMyLoanCard()
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Group Loans", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Requests",
                            color = BlueLink,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { navController.navigate("group_loans/$groupId") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(label = stringResource(R.string.filter_active), isSelected = selectedFilter == "Active", onClick = { selectedFilter = "Active" })
                        FilterChip(label = stringResource(R.string.filter_pending), isSelected = selectedFilter == "Pending", onClick = { selectedFilter = "Pending" })
                        FilterChip(label = stringResource(R.string.filter_history), isSelected = selectedFilter == "History", onClick = { selectedFilter = "History" })
                    }
                }

                items(uiState.groupLoans) { loan ->
                    MemberLoanCard(loan)
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
                Text("Apply Loan", color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun CompactMyLoanCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(color = Color(0xFFFCE4EC), shape = RoundedCornerShape(4.dp)) {
                    Text("active", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFE91E63), fontSize = 12.sp)
                }
            }
            Text("MK 650,000", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("Aproved by Laston Mzumala\nchairperson. Feb 01 2026", fontSize = 12.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Remaining: MK 50,000", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Due nov 04, 2026", color = RedAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Repay Now", color = BlueLink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MemberLoanCard(loan: com.example.tisunga.data.model.Loan) {
    val percentRepaid = if (loan.totalRepayable > 0) {
        (loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(loan.borrowerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Approved by ${loan.approverName ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total borrowed: MK ${formatNumber(loan.principalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(percentRepaid * 100).toInt()}% repaid", fontSize = 12.sp, color = TextSecondary)
                Text("Remaining: MK ${formatNumber(loan.remainingBalance)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            LinearProgressIndicator(
                progress = { percentRepaid.toFloat() },
                color = PurpleProgress,
                trackColor = DividerColor,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Due ${loan.dueDate}", color = RedAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
