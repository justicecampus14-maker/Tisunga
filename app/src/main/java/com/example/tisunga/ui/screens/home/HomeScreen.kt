package com.example.tisunga.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.utils.MockDataProvider
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    // Determine if user has groups
    val hasGroups = uiState.myGroups.isNotEmpty()

    Scaffold(
        bottomBar = { BottomNavBar(navController, type = "C") },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            HomeHeader(
                userName = uiState.userName,
                userPhone = uiState.userPhone,
                navController = navController
            )

            // Loading state
            if (uiState.isLoading && uiState.myGroups.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
            }

            // Sliding Banner Cards
            BannerSection()

            // Quick Actions
            QuickActionsSection(navController)

            // My Groups
            MyGroupsSection(
                navController = navController,
                groups = uiState.myGroups,
                hasGroups = hasGroups
            )

            // Recent Transactions (only if has groups)
            if (hasGroups) {
                RecentTransactionsSection(
                    transactions = uiState.recentTransactions
                )
            }
        }
    }
}

@Composable
fun HomeHeader(userName: String, userPhone: String, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.hi_user, userName), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(stringResource(R.string.good_morning), fontSize = 12.sp, color = TextSecondary)
        }
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8E8E8)) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(userPhone, fontSize = 13.sp)
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(16.dp))
            }
        }
        Box {
            Icon(
                Icons.Filled.Notifications,
                null,
                modifier = Modifier.size(28.dp).clickable { navController.navigate(Routes.NOTIFICATIONS) }
            )
            Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun BannerSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    // Auto-slide effect every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(end = 12.dp),
            pageSpacing = 12.dp
        ) { page ->
            BannerCard(page)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Pager dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(
                            if (pagerState.currentPage == index) NavyBlue
                            else Color(0xFFCCCCCC),
                            CircleShape
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BannerCard(page: Int) {
    // Background color/brush based on page
    val brush = when (page) {
        0 -> Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
        1 -> Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF4A148C), Color(0xFF7B1FA2)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when(page) {
                        0 -> stringResource(R.string.banner_tisunga)
                        1 -> stringResource(R.string.banner_save_more)
                        else -> stringResource(R.string.banner_grow_fast)
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = when(page) {
                        0 -> stringResource(R.string.banner_desc_0)
                        1 -> stringResource(R.string.banner_desc_1)
                        else -> stringResource(R.string.banner_desc_2)
                    },
                    color = Color.White.copy(0.8f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if(page == 0) stringResource(R.string.banner_savings_static) else stringResource(R.string.banner_join_groups),
                    color = Color(0xFFFFEB3B),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Stylized "Group of people" icon placeholder
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.size(80.dp).alpha(0.3f),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun QuickActionsSection(navController: NavController) {
    Column(modifier=Modifier.padding(horizontal=16.dp)) {
        Surface(
            shape=RoundedCornerShape(8.dp),
            color=Color.White
        ) {
            Text(stringResource(R.string.quick_action_title),
                 modifier=Modifier.padding(horizontal=12.dp, vertical=6.dp),
                 fontSize=14.sp, fontWeight=FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                icon = Icons.Filled.GroupAdd,
                label = stringResource(R.string.create_group_label),
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.CREATE_GROUP_STEP1)
                }
            )
            QuickActionCard(
                icon = Icons.Filled.Groups,
                label = stringResource(R.string.join_group_label),
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.JOIN_GROUP)
                }
            )
            QuickActionCard(
                icon = Icons.Filled.SwapHoriz,
                label = stringResource(R.string.view_loans_label),
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.ALL_LOANS)
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(85.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(Modifier.fillMaxSize(),
                contentAlignment=Alignment.Center) {
                Icon(icon, null,
                     modifier=Modifier.size(32.dp),
                     tint=NavyBlue)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize=11.sp,
             textAlign=TextAlign.Center,
             color=TextPrimary)
    }
}

@Composable
private fun MyGroupsSection(
    navController: NavController,
    groups: List<Group>,
    hasGroups: Boolean
) {
    Column(modifier=Modifier.padding(horizontal=16.dp)) {
        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically
        ) {
            Surface(shape=RoundedCornerShape(8.dp), color=White) {
                Text(stringResource(R.string.my_groups_title),
                     modifier=Modifier.padding(horizontal=12.dp, vertical=6.dp),
                     fontWeight=FontWeight.SemiBold)
            }
            if (hasGroups) {
                TextButton(onClick={
                    navController.navigate(Routes.DISCOVER_GROUPS)
                }) {
                    Text(stringResource(R.string.discover_groups_link),
                         color=GreenAccent,
                         fontWeight=FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!hasGroups) {
            Card(
                modifier=Modifier.fillMaxWidth().height(160.dp),
                shape=RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundLightGray)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.no_groups_msg), color = TextSecondary, textAlign = TextAlign.Center)
                    TextButton(onClick = { navController.navigate(Routes.DISCOVER_GROUPS) }) {
                        Text(stringResource(R.string.discover_groups_link), color = GreenAccent)
                    }
                }
            }
        } else {
            groups.forEach { group ->
                GroupListItem(group, navController)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun GroupListItem(group: Group, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("group_detail/${group.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD4E6B5)
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(stringResource(R.string.group_total_savings, group.totalSavings), fontSize = 12.sp, color = TextSecondary)
                    Text(stringResource(R.string.group_my_savings, group.mySavings), fontSize = 12.sp, color = TextSecondary)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, NavyBlue),
                    color = Color.Transparent
                ) {
                    Text(
                        group.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = NavyBlue,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = BackgroundGray, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        group.description,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    stringResource(R.string.save_now_link),
                    color = BlueLink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("make_contribution/${group.id}") }
                )
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<com.example.tisunga.data.model.Transaction>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = White) {
            Text(stringResource(R.string.recent_transactions_title),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        transactions.forEach { transaction ->
            TransactionListItem(transaction)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TransactionListItem(transaction: com.example.tisunga.data.model.Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "contribution") Icons.Default.Add else Icons.Default.Remove,
                    contentDescription = null,
                    tint = if (transaction.type == "contribution") Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(transaction.timestamp, fontSize = 12.sp, color = TextSecondary)
            }
            Text(
                stringResource(R.string.amount_mk, transaction.amount),
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "contribution") Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
