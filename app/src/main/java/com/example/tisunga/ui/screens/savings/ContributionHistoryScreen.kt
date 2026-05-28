package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
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
        if (selectedTab == 0) viewModel.getMyHistory(groupId)
        else viewModel.getGroupHistory(groupId)
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.contribution_history_title),
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = White,
                contentColor = NavyBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NavyBlue
                    )
                },
                divider = { HorizontalDivider(color = DividerColor, thickness = 0.5.dp) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            stringResource(R.string.my_history_tab),
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            stringResource(R.string.group_history_tab),
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
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
                            Icon(
                                Icons.Default.History, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp), 
                                tint = TextSecondary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.no_contributions_found), 
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
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
        "COMPLETED", "CONFIRMED" -> GreenAccent
        "PENDING" -> Color(0xFFFFA500)
        "FAILED" -> RedAccent
        else -> Color.Gray
    }

    // Hide status for completed/confirmed contributions as requested
    val isFinalized = contribution.status == "COMPLETED" || contribution.status == "CONFIRMED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NavyBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NavyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val message = remember(contribution, isMyHistory) {
                        val gName = contribution.group?.name?.replace("null", "", true)?.trim()
                        val groupPart = if (gName.isNullOrEmpty()) "the group" else gName
                        val rawName = contribution.memberName?.replace("null", "", true)?.trim()
                        val name = if (!rawName.isNullOrEmpty()) rawName else {
                            val first = contribution.user?.firstName?.replace("null", "", true)?.trim() ?: ""
                            val last = contribution.user?.lastName?.replace("null", "", true)?.trim() ?: ""
                            "$first $last".trim().ifEmpty { "A member" }
                        }

                        if (isMyHistory) {
                            if (contribution.type == "LOAN_REPAYMENT") "Loan repayment to $groupPart"
                            else "Contribution to $groupPart"
                        } else {
                            if (contribution.type == "LOAN_REPAYMENT") "$name repayment"
                            else "$name contribution"
                        }
                    }

                    Text(
                        text = message,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = FormatUtils.formatDateTime(contribution.createdAt),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FormatUtils.formatMoney(contribution.amount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = NavyBlue,
                        textAlign = TextAlign.End
                    )
                    
                    if (!isFinalized) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = contribution.status,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            if (contribution.status == "FAILED" && !contribution.failureReason.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reason: ${contribution.failureReason}",
                    color = RedAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
