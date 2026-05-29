package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.tisunga.ui.screens.home.AppDrawerContent
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.GroupSavingsSummary
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.MemberSavingsRow
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
    val uiState        by viewModel.uiState.collectAsState()
    val homeUiState    by homeViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val drawerState    = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope          = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Role normalization - Prioritize SavingsViewModel's role as it's fetched with dashboard
    val effectiveRole = uiState.userRole.ifEmpty { homeUiState.myRole ?: "MEMBER" }.uppercase()
    val isChair = effectiveRole.contains("CHAIR") || effectiveRole == "ADMIN"
    val isTreasurer = effectiveRole.contains("TREAS")
    val isSecretary = effectiveRole.contains("SEC")

    // Load savings data for the user's current group
    val groupId = homeUiState.myGroups.firstOrNull()?.id
    LaunchedEffect(groupId) {
        groupId?.let { 
            viewModel.loadSavingsData(it)
            viewModel.loadDisbursementHistory(it)
        }
    }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
        if (uiState.isSuccess && uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            viewModel.resetState()
            groupId?.let { viewModel.loadDisbursementHistory(it) }
        }
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
            viewModel.resetState()
        }
    }

    var showRejectDialog by remember { mutableStateOf<String?>(null) } // disbursementId
    var rejectionReason by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName      = homeUiState.userName,
                userPhone     = homeUiState.userPhone,
                myGroups      = homeUiState.myGroups,
                myRole        = homeUiState.myRole,
                navController = navController,
                drawerState   = drawerState,
                scope         = scope,
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(com.example.tisunga.ui.navigation.Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeHeader(
                    userPhone     = homeUiState.userPhone,
                    unreadCount   = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick   = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar      = { BottomNavBar(navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(
                        text = stringResource(R.string.savings_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Top summary card: Group Savings + My Savings
                item {
                    SavingsSummaryCard(
                        groupTotal = uiState.totalGroupSavings,
                        mySavings  = uiState.mySavings,
                        isChair = isChair,
                        isTreasurer = isTreasurer,
                        disbursementStatus = uiState.currentDisbursement?.status,
                        onInitiateDisbursement = {
                            groupId?.let { navController.navigate("disbursement/$it") }
                        },
                        onViewDetails = {
                            groupId?.let { navController.navigate("disbursement/$it") }
                        }
                    )
                }

                // Treasurer Approval Card (Popping up below the savings card)
                if (isTreasurer && uiState.currentDisbursement?.status?.equals("PENDING", ignoreCase = true) == true) {
                    item {
                        TreasurerApprovalCard(
                            disbursement = uiState.currentDisbursement!!,
                            isApproving = uiState.isLoading,
                            isRejecting = uiState.isLoading,
                            onApprove = {
                                groupId?.let { viewModel.approveDisbursement(it, uiState.currentDisbursement!!.id) }
                            },
                            onReject = {
                                showRejectDialog = uiState.currentDisbursement!!.id
                            }
                        )
                    }
                }

                // Per-member savings list
                val summary = uiState.groupSavings.firstOrNull()
                if (summary != null && summary.memberSavings.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.group_savings_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    items(summary.memberSavings) { row ->
                        MemberSavingsCard(row = row)
                    }
                } else if (summary != null) {
                    // No disbursement yet — show basic group card
                    item {
                        Text(
                            text = stringResource(R.string.group_savings_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GroupSavingsCard(summary = summary) {
                            groupId?.let { navController.navigate("make_contribution/$it/${summary.groupName}") }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Rejection Dialog
    if (showRejectDialog != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = null },
            title = { Text("Reject Disbursement", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this disbursement request.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("Enter reason...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        groupId?.let { 
                            viewModel.rejectDisbursement(it, showRejectDialog!!, rejectionReason)
                        }
                        showRejectDialog = null
                        rejectionReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    enabled = rejectionReason.isNotBlank()
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun TreasurerApprovalCard(
    disbursement: com.example.tisunga.data.model.Disbursement,
    isApproving: Boolean = false,
    isRejecting: Boolean = false,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "Disbursement Request",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Requested by ${disbursement.requestedByName ?: "Chairperson"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    FormatUtils.formatMoney(disbursement.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isRejecting && !isApproving,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    )
                ) {
                    if (isRejecting) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reject", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onApprove,
                    enabled = !isApproving && !isRejecting,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    if (isApproving) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text("Approve", color = MaterialTheme.colorScheme.onSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// Summary card: Group Savings + My Savings

@Composable
fun SavingsSummaryCard(
    groupTotal: Double,
    mySavings: Double,
    isChair: Boolean = false,
    isTreasurer: Boolean = false,
    disbursementStatus: String? = null,
    onInitiateDisbursement: () -> Unit,
    onViewDetails: () -> Unit = {}
) {
    var isGroupSavingsVisible by remember { mutableStateOf(false) }
    var isMySavingsVisible by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyBlue),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Savings column
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.group_savings_title),
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium
                    )

                    if (disbursementStatus?.equals("PENDING", ignoreCase = true) == true) {
                        TextButton(
                            onClick = onViewDetails,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = if (isTreasurer) "Review Request" else "Awaiting Approval",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    } else if (isChair) {
                        TextButton(
                            onClick = onInitiateDisbursement,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "Disbursement",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isGroupSavingsVisible) FormatUtils.formatMoney(groupTotal) else "MWK XXXXXX",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isGroupSavingsVisible = !isGroupSavingsVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isGroupSavingsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            // My Savings column
            Column {
                Text(
                    stringResource(R.string.my_savings_label),
                    fontSize = 12.sp,
                    color = White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isMySavingsVisible) FormatUtils.formatMoney(mySavings) else "MWK XXXXXX",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isMySavingsVisible = !isMySavingsVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMySavingsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Per-member savings row

@Composable
fun MemberSavingsCard(row: MemberSavingsRow) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar initials
                val initials = row.userName
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.take(1) }
                    .uppercase()
                    .ifEmpty { "?" }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(NavyBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyBlue
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = row.userName.ifBlank { stringResource(R.string.member_default_name) },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = row.userPhone,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = FormatUtils.formatMoney(row.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyBlue
            )
        }
    }
}

//Fallback group card when no disbursement

@Composable
fun GroupSavingsCard(summary: GroupSavingsSummary, onSaveClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = summary.groupName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.total_amount_with_label, FormatUtils.formatMoney(summary.totalSavings)),
                fontSize = 14.sp,
                color = NavyBlue,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.my_savings_amount_label, FormatUtils.formatMoney(summary.mySavings)),
                fontSize = 13.sp,
                color = TextSecondary
            )
            if (summary.withdrawDate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.withdraw_date_label, summary.withdrawDate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSaveClick) {
                    Text(
                        text = stringResource(R.string.save_now_link),
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
