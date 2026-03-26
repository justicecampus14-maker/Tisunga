package com.example.tisunga.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.utils.MockDataProvider

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

            // Banner
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
            Text("Hi, $userName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Good morning", fontSize = 12.sp, color = TextSecondary)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal=16.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF388E3C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TISUNGA", color=Color.White,
                 fontSize=24.sp, fontWeight=FontWeight.Bold,
                 letterSpacing=4.sp)
            Text("Save Together. Grow Together.",
                 color=Color.White.copy(0.8f), fontSize=13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("My Savings: MK 12,500.00",
                 color=Color(0xFFFFEB3B),
                 fontSize=16.sp, fontWeight=FontWeight.SemiBold)
        }
    }
    // Pager dots
    Row(
        modifier=Modifier.fillMaxWidth().padding(top=8.dp),
        horizontalArrangement=Arrangement.Center
    ) {
        repeat(3) { index ->
            Box(
                modifier=Modifier
                    .padding(horizontal=3.dp)
                    .size(if(index==0) 8.dp else 6.dp)
                    .background(
                        if(index==0) NavyBlue
                        else Color(0xFFCCCCCC),
                        CircleShape
                    )
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun QuickActionsSection(navController: NavController) {
    Column(modifier=Modifier.padding(horizontal=16.dp)) {
        Surface(
            shape=RoundedCornerShape(8.dp),
            color=Color.White
        ) {
            Text("Quick Action",
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
                label = "Create Group",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.CREATE_GROUP_STEP1)
                }
            )
            QuickActionCard(
                icon = Icons.Filled.Groups,
                label = "Join Group",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.JOIN_GROUP)
                }
            )
            QuickActionCard(
                icon = Icons.Filled.SwapHoriz,
                label = "View Loans",
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
                Text("My Groups",
                     modifier=Modifier.padding(horizontal=12.dp, vertical=6.dp),
                     fontWeight=FontWeight.SemiBold)
            }
            if (hasGroups) {
                TextButton(onClick={
                    navController.navigate(Routes.DISCOVER_GROUPS)
                }) {
                    Text("Discover groups →",
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
                    Text("You don't belong to any group yet", color = TextSecondary, textAlign = TextAlign.Center)
                    TextButton(onClick = { navController.navigate(Routes.DISCOVER_GROUPS) }) {
                        Text("Discover groups →", color = GreenAccent)
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
                    Text("Group savings : MK ${group.totalSavings}", fontSize = 12.sp, color = TextSecondary)
                    Text("My Savings: MK ${group.mySavings}", fontSize = 12.sp, color = TextSecondary)
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
                    "Save Now",
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
            Text("Recent Transactions",
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
                "MK ${transaction.amount}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "contribution") Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
