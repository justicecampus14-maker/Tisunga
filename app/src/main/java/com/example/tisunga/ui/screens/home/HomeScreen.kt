package com.example.tisunga.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(260.dp),
                drawerContainerColor = BackgroundGray,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                // Hamburger menu to close the drawer
                IconButton(
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Close Menu",
                        tint = NavyBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = DividerColor)
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("User Profile") },
                    selected = false,
                    onClick = { /* Navigate to profile */ },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                val currentGroupId = uiState.myGroups.firstOrNull()?.id ?: 0
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    label = { Text("Add Members") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Routes.ADD_MEMBERS.replace("{groupId}", currentGroupId.toString()))
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { /* Navigate to settings */ },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    label = { Text("Theme") },
                    selected = false,
                    onClick = { /* Toggle theme */ },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text("Group Members") },
                    selected = false,
                    onClick = {
                        uiState.myGroups.firstOrNull()?.let { group ->
                            val role = viewModel.getUserGroupRole(group.id)
                            if (role.equals("chairperson", ignoreCase = true) || role.equals("secretary", ignoreCase = true)) {
                                navController.navigate(Routes.GROUP_MEMBERS_CHAIR.replace("{groupId}", group.id.toString()))
                            } else {
                                navController.navigate(Routes.GROUP_MEMBERS.replace("{groupId}", group.id.toString()))
                            }
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = RedAccent) },
                    label = { Text("Logout", color = RedAccent) },
                    selected = false,
                    onClick = { 
                        viewModel.logout()
                        navController.navigate(Routes.SIGN_IN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                Spacer(Modifier.weight(1f))
            }
        }
    ) {
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
                // Header with hamburger to open drawer
                HomeHeader(
                    userPhone = uiState.userPhone,
                    navController = navController,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )

                if (uiState.myGroups.isEmpty()) {
                    BannerSection()
                } else {
                    GroupInfoCard(uiState.myGroups.first())
                }

                QuickActionsSection(navController, uiState.myGroups.firstOrNull())
                RecentTransactionsSection(uiState.recentTransactions)
            }
        }
    }
}

@Composable
fun GroupInfoCard(group: Group) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyBlue),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 220.dp, y = (-50).dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = group.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Group Wallet",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.US, "MK %,.2f", group.totalSavings),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Your Balance",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.US, "MK %,.2f", group.mySavings),
                            color = Color(0xFFFFEB3B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(userPhone: String, navController: NavController, onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = NavyBlue,
                modifier = Modifier.size(32.dp)
            )
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE8E8E8)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(userPhone, fontSize = 13.sp)
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(16.dp))
            }
        }

        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                Icons.Filled.Notifications,
                null,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.navigate(Routes.NOTIFICATIONS) }
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun BannerSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
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
private fun QuickActionsSection(navController: NavController, group: Group?) {
    val hasGroups = group != null
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
            horizontalArrangement=Arrangement.spacedBy(8.dp)
        ) {
            // First button: Create Group or Save
            if (!hasGroups) {
                QuickActionCard(
                    icon = Icons.Filled.GroupAdd,
                    label = stringResource(R.string.create_group_label),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.CREATE_GROUP_STEP1)
                    }
                )
            } else {
                QuickActionCard(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = stringResource(R.string.save_label),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.MAKE_CONTRIBUTION.replace("{groupId}", group?.id.toString()))
                    }
                )
            }

            // Second button: Contributions (Disabled if no group)
            QuickActionCard(
                icon = Icons.Filled.Payments,
                label = stringResource(R.string.contributions_label),
                modifier = Modifier.weight(1f),
                enabled = hasGroups,
                onClick = {
                    if (hasGroups) {
                        navController.navigate(Routes.CONTRIBUTION_HISTORY.replace("{groupId}", group?.id.toString()))
                    }
                }
            )

            // Third button: Events (Disabled if no group)
            QuickActionCard(
                icon = Icons.Filled.Event,
                label = stringResource(R.string.events_label),
                modifier = Modifier.weight(1f),
                enabled = hasGroups,
                onClick = {
                    if (hasGroups) {
                        navController.navigate(Routes.EVENTS.replace("{groupId}", group?.id.toString()))
                    }
                }
            )

            // Fourth button: Loans (Disabled if no group)
            QuickActionCard(
                icon = Icons.Filled.SwapHoriz,
                label = stringResource(R.string.view_loans_label),
                modifier = Modifier.weight(1f),
                enabled = hasGroups,
                onClick = {
                    if (hasGroups) {
                        navController.navigate(Routes.ALL_LOANS)
                    }
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(85.dp)
                .clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(if (enabled) 2.dp else 0.dp)
        ) {
            Box(Modifier.fillMaxSize(),
                contentAlignment=Alignment.Center) {
                Icon(icon, null,
                     modifier=Modifier.size(32.dp).alpha(if (enabled) 1f else 0.3f),
                     tint=NavyBlue)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize=11.sp,
             textAlign=TextAlign.Center,
             color=if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f))
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<Transaction>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Text(
                text = stringResource(R.string.recent_transactions_title),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_groups_msg),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    transactions.take(5).forEachIndexed { index, transaction ->
                        TransactionRow(transaction)
                        if (index < transactions.take(5).size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, color) = when (transaction.type.lowercase()) {
            "contribution" -> Icons.Default.AddCircle to Color(0xFF4CAF50)
            "loan_withdrawal" -> Icons.Default.RemoveCircle to Color(0xFFF44336)
            "loan_repayment" -> Icons.Default.KeyboardArrowUp to Color(0xFF2196F3)
            else -> Icons.Default.SwapHoriz to Color(0xFF757575)
        }
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.type.replace("_", " ").uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = transaction.description,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
        
        Text(
            text = String.format(Locale.US, "MK %,.0f", transaction.amount),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
