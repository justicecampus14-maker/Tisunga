package com.example.tisunga.ui.screens.group

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.TransactionType
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.screens.home.AppDrawerContent
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // UI state derived from group dashboard
    val groupName = uiState.selectedGroup?.name ?: homeUiState.myGroups.firstOrNull { it.id == groupId }?.name ?: stringResource(R.string.placeholder_group_name)
    
    // Check if user is Chair or Secretary for this group
    val groupRole = homeUiState.myRole?.uppercase() ?: "MEMBER"
    val isChair = groupRole == "CHAIRPERSON" || groupRole == "SECRETARY"

    LaunchedEffect(groupId) {
        viewModel.getGroupDashboard(groupId)
        viewModel.getGroupTransactions(groupId)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName   = homeUiState.userName,
                userPhone  = homeUiState.userPhone,
                myGroups   = homeUiState.myGroups,
                myRole     = homeUiState.myRole,
                navController = navController,
                drawerState   = drawerState,
                scope         = scope,
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = { BottomNavBar(navController) },
            containerColor = BackgroundLightGray
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                HomeHeader(
                    userPhone = homeUiState.userPhone,
                    unreadCount = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        GroupSummaryCard(
                            groupName = groupName,
                            totalSavings = uiState.selectedGroup?.totalSavings ?: 0.0,
                            mySavings = uiState.selectedGroup?.mySavings ?: 0.0
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    
                    item {
                        QuickActionsHeader(navController, groupId, isChair)
                        Spacer(modifier = Modifier.height(12.dp))
                        QuickActionsGrid(navController, groupId, isChair, groupName)
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
}

@Composable
fun GroupSummaryCard(groupName: String, totalSavings: Double, mySavings: Double) {
    var isGroupSavingsVisible by remember { mutableStateOf(false) }
    var isMySavingsVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(groupName, fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(stringResource(R.string.group_saving_label), fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isGroupSavingsVisible) com.example.tisunga.utils.FormatUtils.formatMoney(totalSavings) else "MWK XXXXXX",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { isGroupSavingsVisible = !isGroupSavingsVisible },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isGroupSavingsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(stringResource(R.string.my_savings_label), fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isMySavingsVisible) com.example.tisunga.utils.FormatUtils.formatMoney(mySavings) else "MWK XXXXXX",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { isMySavingsVisible = !isMySavingsVisible },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isMySavingsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsHeader(navController: NavController, groupId: String, isChair: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        @Suppress("DEPRECATION")
        Text(stringResource(R.string.quick_actions_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.members_link),
            color = BlueLink,
            modifier = Modifier.clickable {
                if (isChair) {
                    navController.navigate(Routes.GROUP_MEMBERS_CHAIR.replace("{groupId}", groupId))
                } else {
                    navController.navigate(Routes.GROUP_MEMBERS.replace("{groupId}", groupId))
                }
            }
        )
    }
}

@Composable
fun QuickActionsGrid(navController: NavController, groupId: String, isChair: Boolean, groupName: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(Icons.Default.AddCard, stringResource(R.string.action_save), Modifier.weight(1f)) {
                navController.navigate(Routes.MAKE_CONTRIBUTION.replace("{groupId}", groupId))
            }
            ActionCard(Icons.Default.Badge, stringResource(R.string.action_view_savings), Modifier.weight(1f)) {
                navController.navigate(Routes.GROUP_SAVINGS)
            }
            ActionCard(Icons.Default.Event, "Activities", Modifier.weight(1f)) {
                navController.navigate(Routes.ACTIVITIES.replace("{groupId}", groupId))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(Icons.Default.SwapHoriz, stringResource(R.string.action_view_loans), Modifier.weight(1f)) {
                navController.navigate(Routes.GROUP_LOANS.replace("{groupId}", groupId))
            }
            ActionCard(Icons.Default.Groups, "History", Modifier.weight(1f)) {
                navController.navigate(Routes.CONTRIBUTION_HISTORY.replace("{groupId}", groupId))
            }
            // Spacer to keep the grid balanced
            Box(modifier = Modifier.weight(1f))
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
fun TransactionsHeader(navController: NavController, groupId: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.transactions_history_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.view_all_link),
            color = BlueLink,
            modifier = Modifier.clickable { navController.navigate(Routes.TRANSACTIONS.replace("{groupId}", groupId)) }
        )
    }
}

@Composable
fun TransactionSummaryCard(transaction: Transaction) {
    val isCredit = transaction.type in listOf(
        TransactionType.SAVINGS,
        TransactionType.LOAN_IN,
        TransactionType.SOCIAL_FUND,
        TransactionType.SHARE_PURCHASE,
        TransactionType.JOIN_FEE,
        TransactionType.INTEREST,
        TransactionType.SYSTEM
    )
    val color = if (isCredit) GreenAccent else Color(0xFF333333)
    val icon = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.type?.name?.replace("_", " ") ?: "Transaction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val amountText = com.example.tisunga.utils.FormatUtils.formatMoney(kotlin.math.abs(transaction.amount))
                    Text(
                        text = (if (isCredit) "+" else "") + amountText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = color
                    )
                }
            }

            val displayDescription = remember(transaction) {
                val raw = transaction.description.replace("null", "", true).trim()
                if (raw.isEmpty() || raw == "null null") {
                    when (transaction.type) {
                        TransactionType.SAVINGS, TransactionType.SOCIAL_FUND, TransactionType.SHARE_PURCHASE -> 
                            "Contribution to the group"
                        TransactionType.JOIN_FEE -> 
                            "Joining fee payment"
                        TransactionType.LOAN_IN -> 
                            "Loan repayment"
                        else -> "Transaction completed"
                    }
                } else {
                    transaction.description
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            val dateStr = try { transaction.createdAt.take(16).replace("T", " ") } catch (e: Exception) { "" }
            Text(
                text = "${if (displayDescription.isNotBlank()) "$displayDescription\n" else ""}$dateStr",
                modifier = Modifier.fillMaxWidth(0.5f),
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
