package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.SavingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionHistoryScreen(
    navController: NavController,
    groupId: String,
    viewModel: SavingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) viewModel.getMyHistory()
        else viewModel.getGroupHistory(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contribution History", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = White,
                contentColor = NavyBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NavyBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My History") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Group History") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
            } else {
                val list = if (selectedTab == 0) uiState.myHistory else uiState.groupHistory
                
                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No contributions found", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list) { contribution ->
                            ContributionCard(contribution, isMyHistory = selectedTab == 0)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContributionCard(contribution: Contribution, isMyHistory: Boolean) {
    val statusColor = when (contribution.status) {
        "COMPLETED" -> GreenAccent
        "PENDING" -> Color(0xFFFFA500)
        "FAILED" -> Color.Red
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = contribution.type.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isMyHistory) contribution.group?.name ?: "Personal" 
                               else "${contribution.user?.firstName} ${contribution.user?.lastName}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "MK ${String.format("%,.2f", contribution.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = NavyBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BackgroundGray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(contribution.createdAt.take(16).replace("T", " "), fontSize = 12.sp, color = Color.Gray)
                    if (contribution.transactionRef != null) {
                        Text("Ref: ${contribution.transactionRef}", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = contribution.status,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (contribution.status == "FAILED" && contribution.failureReason != null) {
                Text(
                    text = contribution.failureReason,
                    color = Color.Red,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
