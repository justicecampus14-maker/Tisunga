package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
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
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupSavingsSummary
import com.example.tisunga.viewmodel.SavingsViewModel
import com.example.tisunga.utils.FormatUtils

@Composable
fun GroupSavingsScreen(navController: NavController, viewModel: SavingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMyContributions()
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = stringResource(R.string.savings_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController, type = "B") },
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SavingsSummaryCard(uiState.totalSavings)
            }

            item {
                Text(
                    text = stringResource(R.string.group_savings_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            }

            items(uiState.groupSavings) { summary ->
                GroupSavingsCard(summary) {
                    navController.navigate("make_contribution/${summary.groupId}")
                }
            }
            
            if (uiState.groupSavings.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No group savings found", color = TextSecondary)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyBlue),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.total_savings_label),
                fontSize = 14.sp,
                color = White.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.amount_mk, FormatUtils.formatNumber(totalSavings)),
                fontSize = 32.sp,
                color = White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Last Activity", fontSize = 11.sp, color = White.copy(alpha = 0.6f))
                    Text("Feb 23, 2026", fontSize = 13.sp, color = White, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Groups", fontSize = 11.sp, color = White.copy(alpha = 0.6f))
                    Text("2 Active", fontSize = 13.sp, color = White, fontWeight = FontWeight.Medium)
                }
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
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(summary.groupName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(stringResource(R.string.total_savings_label), fontSize = 12.sp, color = TextSecondary)
                }
                Surface(
                    color = BackgroundGray,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.amount_mk, FormatUtils.formatNumber(summary.totalSavings)),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (summary.withdrawDate != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Withdraw Date: ", fontSize = 12.sp, color = TextSecondary)
                    Text(summary.withdrawDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Last saved: ${summary.lastSavedDate}", fontSize = 11.sp, color = TextSecondary)
                    Text("${summary.memberCount} members", fontSize = 11.sp, color = TextSecondary)
                }
                Button(
                    onClick = onSaveClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Save Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
