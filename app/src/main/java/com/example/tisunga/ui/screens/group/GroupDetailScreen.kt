package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun GroupDetailScreen(navController: NavController, groupId: Int, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Placeholder data
    val groupName = stringResource(R.string.placeholder_group_name)
    val userPhone = "0882752624"
    val isChair = true // This should come from SessionManager or ViewModel

    LaunchedEffect(Unit) {
        viewModel.getGroupTransactions(groupId)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, type = "B") },
        containerColor = BackgroundLightGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HomeHeader(userPhone, navController, onMenuClick = { })
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    GroupSummaryCard(groupName)
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                item {
                    QuickActionsHeader(navController, groupId, isChair)
                    Spacer(modifier = Modifier.height(12.dp))
                    QuickActionsGrid(navController, groupId, isChair)
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                item {
                    TransactionsHeader(navController, groupId)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                items(uiState.transactions.take(2)) { transaction ->
                    TransactionSummaryCard(transaction)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun GroupSummaryCard(groupName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(groupName, fontSize = 26.sp, color = TextSecondary, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(stringResource(R.string.group_saving_label), fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(180.dp)
                    .background(Color(0xFFDDDDDD), RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(stringResource(R.string.my_savings_label), fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(140.dp)
                    .background(Color(0xFFDDDDDD), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun QuickActionsHeader(navController: NavController, groupId: Int, isChair: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.quick_actions_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.members_link),
            color = BlueLink,
            modifier = Modifier.clickable {
                if (isChair) {
                    navController.navigate("group_members_chair/$groupId")
                } else {
                    navController.navigate("group_members/$groupId")
                }
            }
        )
    }
}

@Composable
fun QuickActionsGrid(navController: NavController, groupId: Int, isChair: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionCard(Icons.Default.AddCard, stringResource(R.string.action_save), Modifier.weight(1f)) {
            navController.navigate("make_contribution/$groupId")
        }
        ActionCard(Icons.Default.Badge, stringResource(R.string.action_view_savings), Modifier.weight(1f)) {
            navController.navigate(Routes.GROUP_SAVINGS)
        }
        ActionCard(Icons.Default.CalendarMonth, stringResource(R.string.action_view_events), Modifier.weight(1f)) {
            navController.navigate("events/$groupId")
        }
        ActionCard(Icons.Default.SwapHoriz, stringResource(R.string.action_view_loans), Modifier.weight(1f)) {
            if (isChair) {
                navController.navigate("group_loans_detail/$groupId")
            } else {
                navController.navigate("my_loans/$groupId")
            }
        }
    }
}

@Composable
fun ActionCard(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(1f).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = NavyBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun TransactionsHeader(navController: NavController, groupId: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.transactions_history_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.view_all_link),
            color = BlueLink,
            modifier = Modifier.clickable { navController.navigate("transactions/$groupId") }
        )
    }
}

@Composable
fun TransactionSummaryCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.member_name_placeholder), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(stringResource(R.string.trans_id_label, transaction.transId, transaction.type), fontSize = 13.sp, color = TextSecondary)
            Text(transaction.timestamp, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
