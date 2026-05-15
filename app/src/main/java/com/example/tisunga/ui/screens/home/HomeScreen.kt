package com.example.tisunga.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.NavyBlue
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName = uiState.userName,
                userPhone = uiState.userPhone,
                myGroups = uiState.myGroups,
                myRole = uiState.myRole,
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                HomeHeader(
                    userPhone = uiState.userPhone,
                    unreadCount = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = { BottomNavBar(navController) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                WelcomeSection(uiState.userName)
                Spacer(modifier = Modifier.height(20.dp))
                PromoBannerPager()
                Spacer(modifier = Modifier.height(24.dp))
                
                val currentGroup = uiState.myGroups.firstOrNull()
                QuickActionsSection(navController, currentGroup, uiState.myRole)
                
                Spacer(modifier = Modifier.height(24.dp))
                MyGroupsSection(navController, uiState.myGroups)
                Spacer(modifier = Modifier.height(24.dp))
                RecentTransactionsSection(navController, currentGroup)
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    userName: String,
    userPhone: String,
    myGroups: List<Group>,
    myRole: String?,
    navController: NavController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drawer Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue)
                    .padding(24.dp)
            ) {
                Column {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                userName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(userPhone, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items
            DrawerItem(Icons.Default.Home, "Home", true) {
                scope.launch { drawerState.close() }
            }
            DrawerItem(Icons.Default.Person, "Profile", false) {
                scope.launch { drawerState.close() }
                navController.navigate(Routes.PROFILE)
            }
            
            val currentGroup = myGroups.firstOrNull()
            if (currentGroup != null) {
                DrawerItem(Icons.Default.Groups, "My Group", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("group_detail/${currentGroup.id}")
                }
                DrawerItem(Icons.Default.AccountBalanceWallet, "Savings", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.GROUP_SAVINGS)
                }
                DrawerItem(Icons.Default.CreditCard, "Loans", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.ALL_LOANS)
                }
            }

            DrawerItem(Icons.Default.Settings, "Settings", false) {
                scope.launch { drawerState.close() }
                navController.navigate(Routes.SETTINGS)
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            DrawerItem(Icons.Default.Logout, "Logout", false) {
                scope.launch { drawerState.close() }
                onLogout()
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = NavyBlue.copy(alpha = 0.1f),
            selectedIconColor = NavyBlue,
            selectedTextColor = NavyBlue,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun HomeHeader(
    userPhone: String,
    unreadCount: Int,
    navController: NavController,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            onClick = { /* Optional profile navigation */ }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(userPhone, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(unreadCount.toString(), color = Color.White)
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
fun WelcomeSection(userName: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.hi_user, userName),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.good_morning),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PromoBannerPager() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    // Auto-scroll logic
    LaunchedEffect(Unit) {
        while(true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            PromoBannerItem(page)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Pager Indicators
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { iteration ->
                val color = if (pagerState.currentPage == iteration) NavyBlue else NavyBlue.copy(alpha = 0.2f)
                val width by animateDpAsState(if (pagerState.currentPage == iteration) 24.dp else 8.dp, label = "")
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .width(width)
                        .height(8.dp)
                )
            }
        }
    }
}

@Composable
fun PromoBannerItem(page: Int) {
    val brush = when(page) {
        0 -> Brush.horizontalGradient(listOf(NavyBlue, NavyBlue.copy(alpha = 0.8f)))
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
                    text = when(page) {
                        0 -> stringResource(R.string.banner_savings_label)
                        1 -> stringResource(R.string.banner_contributions_label)
                        else -> stringResource(R.string.banner_loans_label)
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                when(page) {
                    0 -> Icons.Default.TrendingUp
                    1 -> Icons.Default.AccountBalance
                    else -> Icons.Default.Payments
                },
                contentDescription = null,
                modifier = Modifier.size(80.dp).alpha(0.3f),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun QuickActionsSection(navController: NavController, group: Group?, myRole: String?) {
    val hasGroups = group != null
    Column(modifier=Modifier.padding(horizontal=16.dp)) {
        Surface(
            shape=RoundedCornerShape(8.dp),
            color=MaterialTheme.colorScheme.surface
        ) {
            @Suppress("DEPRECATION")
            Text(stringResource(R.string.quick_action_title),
                modifier=Modifier.padding(horizontal=12.dp, vertical=6.dp),
                fontSize=14.sp, fontWeight=FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionItem(
                icon = if (hasGroups) Icons.Default.AddCard else Icons.Default.GroupAdd,
                label = if (hasGroups) stringResource(R.string.save_label) else stringResource(R.string.create_group_label),
                color = Color(0xFF4CAF50)
            ) {
                if (hasGroups) navController.navigate("make_contribution/${group!!.id}")
                else navController.navigate(Routes.CREATE_GROUP_STEP1)
            }
            QuickActionItem(
                icon = Icons.Default.Event,
                label = stringResource(R.string.events_label),
                color = Color(0xFF2196F3)
            ) {
                if (hasGroups) navController.navigate("events/${group!!.id}")
                else navController.navigate(Routes.HOME) // Fallback
            }
            QuickActionItem(
                icon = if (hasGroups) Icons.Default.CreditCard else Icons.Default.Search,
                label = if (hasGroups) stringResource(R.string.view_loans_label) else stringResource(R.string.join_group_label),
                color = Color(0xFFFF9800)
            ) {
                if (hasGroups) navController.navigate(Routes.ALL_LOANS)
                else navController.navigate(Routes.HOME) // Fallback for join
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun MyGroupsSection(navController: NavController, myGroups: List<Group>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.my_groups_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (myGroups.isNotEmpty()) {
                TextButton(onClick = { /* View All Groups */ }) {
                    Text(stringResource(R.string.view_all_link), color = NavyBlue)
                }
            }
        }
        
        if (myGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_groups_msg), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myGroups) { group ->
                    GroupCard(navController, group)
                }
            }
        }
    }
}

@Composable
fun GroupCard(navController: NavController, group: Group) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { navController.navigate("group_detail/${group.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = NavyBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(group.name.take(1), fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(group.location ?: "Unknown", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.group_total_savings, String.format(Locale.US, "%,.0f", group.totalSavings)), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.group_my_savings, "0.00"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { navController.navigate("make_contribution/${group.id}") }) {
                    Text(stringResource(R.string.save_now_link), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(navController: NavController, group: Group?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.recent_transactions_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (group != null) {
                TextButton(onClick = { navController.navigate("transactions/${group.id}") }) {
                    Text(stringResource(R.string.view_all_link), color = NavyBlue)
                }
            }
        }
        
        // Mock Transactions
        TransactionItem("Group Contribution", "24 Mar 2024", "MK 5,000", true)
        TransactionItem("Loan Repayment", "20 Mar 2024", "MK 12,000", true)
        TransactionItem("Loan Disbursement", "15 Mar 2024", "MK 50,000", false)
    }
}

@Composable
fun TransactionItem(title: String, date: String, amount: String, isCredit: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (isCredit) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isCredit) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            if (isCredit) "+$amount" else "-$amount",
            fontWeight = FontWeight.Bold,
            color = if (isCredit) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 14.sp
        )
    }
}
