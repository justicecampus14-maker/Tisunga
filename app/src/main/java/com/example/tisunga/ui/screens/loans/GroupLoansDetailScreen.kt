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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun GroupLoansDetailScreen(navController: NavController, groupId: Int, viewModel: LoanViewModel) {
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
                        LoanFilterChip(label = "Active", selected = selectedFilter == "Active", onClick = { selectedFilter = "Active" })
                        LoanFilterChip(label = "Pending", selected = selectedFilter == "Pending", onClick = { selectedFilter = "Pending" })
                        LoanFilterChip(label = "History", selected = selectedFilter == "History", onClick = { selectedFilter = "History" })
                    }
                }

                items(uiState.groupLoans) { loan ->
                    MemberLoanCard(loan)
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
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
fun LoanFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) White else Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, DividerColor) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) NavyBlue else TextSecondary
        )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(loan.memberName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Aproved by ${loan.approvedBy ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total borrowed: MK ${com.example.tisunga.utils.FormatUtils.formatNumber(loan.amount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(loan.percentRepaid * 100).toInt()}% repaid", fontSize = 12.sp, color = TextSecondary)
                Text("Remaining: MK ${com.example.tisunga.utils.FormatUtils.formatNumber(loan.remainingAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            LinearProgressIndicator(
                progress = { loan.percentRepaid },
                color = PurpleProgress,
                trackColor = DividerColor,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Due ${loan.dueDate}", color = RedAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
