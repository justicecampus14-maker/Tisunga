package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.SavingsViewModel

@Composable
fun ContributionHistoryScreen(navController: NavController, groupId: Int, viewModel: SavingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMyContributions()
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = stringResource(R.string.contribution_history_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController, type = "B") },
        containerColor = BackgroundGray
    ) { padding ->
        if (uiState.contributions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "No contributions found", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.contributions) { contribution ->
                    ContributionHistoryCard(contribution)
                }
            }
        }
    }
}

@Composable
fun ContributionHistoryCard(contribution: Contribution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(NavyBlue.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    null,
                    modifier = Modifier.size(22.dp),
                    tint = NavyBlue
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contribution.type.replaceFirstChar { it.uppercase() },
                    fontWeight = Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    contribution.timestamp,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.amount_mk, com.example.tisunga.utils.FormatUtils.formatNumber(contribution.amount)),
                    fontWeight = Bold,
                    fontSize = 15.sp,
                    color = GreenAccent
                )
                Surface(
                    color = when(contribution.status.lowercase()) {
                        "completed" -> GreenLight
                        "pending" -> Color(0xFFFFF9C4)
                        else -> Color(0xFFFFEBEE)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = contribution.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = Bold,
                        color = when(contribution.status.lowercase()) {
                            "completed" -> GreenAccent
                            "pending" -> Color(0xFFFBC02D)
                            else -> RedAccent
                        }
                    )
                }
            }
        }
    }
}
