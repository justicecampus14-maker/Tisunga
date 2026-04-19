package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.screens.home.AppDrawerContent
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.GroupSavingsSummary
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.MemberSavingsRow
import com.example.tisunga.viewmodel.NotificationViewModel
import com.example.tisunga.viewmodel.SavingsViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupSavingsScreen(
    navController: NavController,
    viewModel: SavingsViewModel,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel
) {
    val uiState        by viewModel.uiState.collectAsState()
    val homeUiState    by homeViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val drawerState    = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope          = rememberCoroutineScope()

    // Load savings data for the user's current group
    val groupId = homeUiState.myGroups.firstOrNull()?.id
    LaunchedEffect(groupId) {
        groupId?.let { viewModel.loadSavingsData(it) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName      = homeUiState.userName,
                userPhone     = homeUiState.userPhone,
                myGroups      = homeUiState.myGroups,
                myRole        = homeUiState.myRole,
                navController = navController,
                drawerState   = drawerState,
                scope         = scope,
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(com.example.tisunga.ui.navigation.Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeHeader(
                    userPhone     = homeUiState.userPhone,
                    unreadCount   = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick   = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar      = { BottomNavBar(navController) },
            containerColor = BackgroundLightGray
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(stringResource(R.string.savings_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                // ── Top summary card: Group Savings + My Savings ──────────
                item {
                    SavingsSummaryCard(
                        groupTotal = uiState.totalGroupSavings,
                        mySavings  = uiState.mySavings
                    )
                }

                // ── Per-member savings list ────────────────────────────────
                val summary = uiState.groupSavings.firstOrNull()
                if (summary != null && summary.memberSavings.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.group_savings_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(summary.memberSavings) { row ->
                        MemberSavingsCard(row = row)
                    }
                } else if (summary != null) {
                    // No disbursement yet — show basic group card
                    item {
                        Text(stringResource(R.string.group_savings_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        GroupSavingsCard(summary = summary) {
                            groupId?.let { navController.navigate("make_contribution/$it/${summary.groupName}") }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Summary card — Group Savings + My Savings ─────────────────────────────────

@Composable
fun SavingsSummaryCard(groupTotal: Double, mySavings: Double) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyBlue),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Group Savings column
                Column {
                    Text(
                        "Group Savings",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        FormatUtils.formatMoney(groupTotal),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                // My Savings column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "My Savings",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        FormatUtils.formatMoney(mySavings),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFEB3B)  // yellow highlight
                    )
                }
            }
        }
    }
}

// ── Per-member savings row ────────────────────────────────────────────────────

@Composable
fun MemberSavingsCard(row: MemberSavingsRow) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar initials
                val initials = row.userName
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.take(1) }
                    .uppercase()
                    .ifEmpty { "?" }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(NavyBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        row.userName.ifBlank { "Member" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        row.userPhone,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                FormatUtils.formatMoney(row.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyBlue
            )
        }
    }
}

// ── Fallback group card when no disbursement ──────────────────────────────────

@Composable
fun GroupSavingsCard(summary: GroupSavingsSummary, onSaveClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(summary.groupName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Total: ${FormatUtils.formatMoney(summary.totalSavings)}",
                fontSize = 14.sp, color = NavyBlue, fontWeight = FontWeight.SemiBold
            )
            Text(
                "My Savings: ${FormatUtils.formatMoney(summary.mySavings)}",
                fontSize = 13.sp, color = TextSecondary
            )
            if (summary.withdrawDate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.withdraw_date_label, summary.withdrawDate),
                    fontWeight = FontWeight.Bold, fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSaveClick) {
                    Text(stringResource(R.string.save_now_link), color = BlueLink, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
