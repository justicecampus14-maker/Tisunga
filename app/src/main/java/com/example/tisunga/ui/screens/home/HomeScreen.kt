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
import com.example.tisunga.viewmodel.NotificationViewModel
import com.example.tisunga.ui.screens.group.GroupDetailScreen
import com.example.tisunga.viewmodel.GroupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AppDrawerContent(
    userName: String,
    userPhone: String,
    myGroups: List<com.example.tisunga.data.model.Group>,
    myRole: String?,
    navController: NavController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(0.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Drawer Header with Hamburger (to match SideBar.png)
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = NavyBlue
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("My Profile", fontSize = 16.sp) },
            selected = false,
            onClick = {
                scope.launch {
                    drawerState.close()
                    navController.navigate(Routes.PROFILE)
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Group Members", fontSize = 16.sp) },
            selected = false,
            onClick = {
                scope.launch {
                    drawerState.close()
                    val currentGroup = myGroups.firstOrNull()
                    if (currentGroup != null) {
                        val role = myRole?.uppercase() ?: "MEMBER"
                        val isPrivileged = role == "CHAIR" || role == "SECRETARY"
                        if (isPrivileged) {
                            navController.navigate(Routes.GROUP_MEMBERS_CHAIR.replace("{groupId}", currentGroup.id))
                        } else {
                            navController.navigate(Routes.GROUP_MEMBERS.replace("{groupId}", currentGroup.id))
                        }
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Settings", fontSize = 16.sp) },
            selected = false,
            onClick = {
                scope.launch {
                    drawerState.close()
                    navController.navigate(Routes.SETTINGS)
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Theme", fontSize = 16.sp) },
            selected = false,
            onClick = {
                scope.launch {
                    drawerState.close()
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp)) },
            label = { Text("Logout", color = RedAccent, fontSize = 16.sp) },
            selected = false,
            onClick = { 
                scope.launch {
                    drawerState.close()
                    onLogout()
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel,
    groupViewModel: GroupViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
        notificationViewModel.load()
    }

    val myGroup = uiState.myGroups.firstOrNull()
    LaunchedEffect(myGroup?.id) {
        myGroup?.let { group ->
            groupViewModel.seedSelectedGroup(group, uiState.myRole ?: "MEMBER")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName   = uiState.userName,
                userPhone  = uiState.userPhone,
                myGroups   = uiState.myGroups,
                myRole     = uiState.myRole,
                navController = navController,
                drawerState   = drawerState,
                scope         = scope,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    ) {
        if (uiState.isLoading && uiState.myGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyBlue)
            }
        } else {
            Scaffold(
                topBar = {
                    HomeHeader(
                        userPhone   = uiState.userPhone,
                        unreadCount = notificationState.unreadCount,
                        navController = navController,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                },
                bottomBar  = { BottomNavBar(navController) },
                containerColor = BackgroundGray
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (myGroup != null) {
                        GroupInfoCard(myGroup)
                    } else {
                        BannerSection()
                    }
                    
                    QuickActionsSection(navController, myGroup)
                    
                    RecentTransactionsSection(
                        transactions = if (myGroup != null) uiState.recentTransactions else emptyList(),
                        hasGroup = myGroup != null
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsNoGroup(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Text(
                stringResource(R.string.quick_action_title),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                icon = Icons.Filled.Search,
                label = "Join Group",
                modifier = Modifier.weight(1f),
                onClick = {
                    // Logic for joining group
                }
            )

            QuickActionCard(
                icon = Icons.Filled.Help,
                label = "Help",
                modifier = Modifier.weight(1f),
                onClick = {
                    // Logic for help
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun GroupInfoCard(group: com.example.tisunga.data.model.Group) {
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
            // Decorative Circle
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = 180.dp, y = (-40).dp)
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
                    fontSize = 32.sp,
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
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format(Locale.US, "MK %,.2f", group.totalSavings),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Your Balance",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format(Locale.US, "MK %,.2f", group.mySavings),
                            color = Color(0xFFFFEB3B), // Yellow for emphasis
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(userPhone: String, unreadCount: Int, navController: NavController, onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray)
            .statusBarsPadding()
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
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
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
                        navController.navigate(Routes.MAKE_CONTRIBUTION.replace("{groupId}", group.id))
                    }
                )
            }

            QuickActionCard(
                icon = Icons.Filled.Payments,
                label = stringResource(R.string.contributions_label),
                modifier = Modifier.weight(1f),
                enabled = hasGroups,
                onClick = {
                    if (hasGroups) {
                        navController.navigate(Routes.CONTRIBUTION_HISTORY.replace("{groupId}", group.id))
                    }
                }
            )

            QuickActionCard(
                icon = Icons.Filled.Event,
                label = stringResource(R.string.events_label),
                modifier = Modifier.weight(1f),
                enabled = hasGroups,
                onClick = {
                    if (hasGroups) {
                        navController.navigate(Routes.EVENTS.replace("{groupId}", group.id))
                    }
                }
            )

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
fun RecentTransactionsSection(transactions: List<Transaction>, hasGroup: Boolean) {
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
            if (!hasGroup) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You don't belong to any group yet",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions",
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
        val (icon, color) = when (transaction.type) {
            com.example.tisunga.data.model.TransactionType.SAVINGS,
            com.example.tisunga.data.model.TransactionType.LOAN_IN,
            com.example.tisunga.data.model.TransactionType.SHARE_PURCHASE -> Icons.Default.AddCircle to Color(0xFF4CAF50)
            
            com.example.tisunga.data.model.TransactionType.LOAN_OUT,
            com.example.tisunga.data.model.TransactionType.EXPENSE -> Icons.Default.RemoveCircle to Color(0xFFF44336)
            
            else -> Icons.Default.SwapHoriz to Color(0xFF757575)
        }
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = color)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.type?.name?.replace("_", " ") ?: "Transaction",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = transaction.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
        
        Text(
            text = String.format(Locale.US, "MK %,.0f", transaction.amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
