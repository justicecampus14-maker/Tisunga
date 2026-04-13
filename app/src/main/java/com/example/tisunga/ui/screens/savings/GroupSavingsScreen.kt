package com.example.tisunga.ui.screens.savings

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.screens.home.AppDrawerContent
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupSavingsSummary
import com.example.tisunga.viewmodel.HomeViewModel
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
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName = homeUiState.userName,
                userPhone = homeUiState.userPhone,
                myGroups = homeUiState.myGroups,
                myRole = homeUiState.myRole,
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeHeader(
                    userPhone = homeUiState.userPhone,
                    unreadCount = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = { BottomNavBar(navController) },
            containerColor = BackgroundLightGray
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.savings_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        SavingsSummaryCard(uiState.totalSavings)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.group_savings_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    items(uiState.groupSavings) { summary ->
                        GroupSavingsCard(summary) {
                            navController.navigate("make_contribution/${summary.groupId}/${summary.groupName}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsSummaryCard(totalSavings: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)), // Light gray
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.total_savings_label), fontSize = 14.sp)
            Text(
                stringResource(R.string.amount_mk, com.example.tisunga.utils.FormatUtils.formatNumber(totalSavings)),
                fontSize = 28.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.last_saved_label, "02/23/26"), fontSize = 12.sp, color = TextSecondary)
                Text(stringResource(R.string.saving_groups_count, 2), fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun GroupSavingsCard(summary: GroupSavingsSummary, onSaveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.total_savings_label), fontSize = 14.sp)
            Text(
                stringResource(R.string.amount_mk, com.example.tisunga.utils.FormatUtils.formatNumber(summary.totalSavings)),
                fontSize = 26.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            
            if (summary.withdrawDate != null) {
                Text(stringResource(R.string.withdraw_date_label, summary.withdrawDate), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    Text(stringResource(R.string.last_saved_label, summary.lastSavedDate), fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.members_count, summary.memberCount), fontSize = 11.sp, color = TextSecondary)
                }
                Text(
                    stringResource(R.string.save_now_link),
                    color = BlueLink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onSaveClick() }
                )
            }
        }
    }
}
