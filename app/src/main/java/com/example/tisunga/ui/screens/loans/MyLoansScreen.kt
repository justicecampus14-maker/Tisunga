package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.draw.clip
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
import com.example.tisunga.ui.components.GroupLoansSummaryCard
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
                    Text(stringResource(R.string.loans_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.placeholder_group_name), fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(tabMyLoans, selectedTab == tabMyLoans, Modifier.weight(1f)) { selectedTab = tabMyLoans }
                TabButton(tabMemberLoans, selectedTab == tabMemberLoans, Modifier.weight(1f)) { 
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
                    FilterChip(label = filterActive, isSelected = selectedFilter == filterActive, onClick = { selectedFilter = filterActive })
                    FilterChip(label = filterPending, isSelected = selectedFilter == filterPending, onClick = { selectedFilter = filterPending })
                    FilterChip(label = filterHistory, isSelected = selectedFilter == filterHistory, onClick = { selectedFilter = filterHistory })
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GroupLoansSummaryCard()
                    }
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
fun TabButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
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
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
    Column {
        Text(
            text = "My loan",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Amount Section
                Text("Amount", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    color = BackgroundGray
                ) {
                    Text(
                        text = stringResource(R.string.amount_mk, "10,000"),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Approved By Section
                Text("Approved by : Laston Mzumala", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                            color = BackgroundGray
                        ) {
                            Text(
                                text = stringResource(R.string.amount_mk, "10,500"),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Interest", fontSize = 14.sp)
                        Text("5%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = NavyBlue,
                    trackColor = DividerColor
                )
                
                Text(
                    text = "Remaining: MK 5,250",
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
                    Text("Due : Nov 04, 2026", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    
                    Surface(
                        modifier = Modifier.clickable { onRepayClick() },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue),
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
