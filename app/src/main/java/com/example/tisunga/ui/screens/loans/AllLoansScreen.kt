package com.example.tisunga.ui.screens.loans

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.screens.home.AppDrawerContent
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllLoansScreen(
    navController: NavController,
    viewModel: LoanViewModel,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel
) {
    val uiState           by viewModel.uiState.collectAsState()
    val homeUiState       by homeViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val drawerState       = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope             = rememberCoroutineScope()
    val snackbarHost      = remember { SnackbarHostState() }

    val pullToRefreshState = rememberPullToRefreshState()

    val currentGroup = homeUiState.myGroups.firstOrNull()
    val groupId      = currentGroup?.id ?: ""
    val myRole       = homeUiState.myRole?.uppercase() ?: ""
    val canApprove   = myRole == "CHAIR" || myRole == "SECRETARY"
    val isLeader     = canApprove || myRole == "TREASURER"

    // Reject-reason dialog state
    var rejectingLoanId by remember { mutableStateOf<String?>(null) }
    var rejectReason    by remember { mutableStateOf("") }

    val refreshData = {
        viewModel.getMyLoans()
        if (groupId.isNotEmpty()) viewModel.getGroupLoans(groupId)
    }

    // Load both my loans + group loans on entry
    LaunchedEffect(groupId) {
        refreshData()
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            refreshData()
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    // Show snackbar on success or error
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHost.showSnackbar(uiState.successMessage)
            viewModel.resetState()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHost.showSnackbar(uiState.errorMessage)
            viewModel.resetState()
        }
    }

    val pendingLoans = uiState.groupLoans.filter { it.status.uppercase() == "PENDING" }
    val activeMyLoan = uiState.myLoans.firstOrNull { it.status.uppercase() == "ACTIVE" }
    val pendingMyLoan = uiState.myLoans.firstOrNull { it.status.uppercase() == "PENDING" }

    // Reject dialog
    if (rejectingLoanId != null) {
        AlertDialog(
            onDismissRequest = { 
                rejectingLoanId = null 
                rejectReason = ""
            },
            icon = {
                Icon(Icons.Default.DoNotDisturb, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = { Text(stringResource(R.string.reject_loan_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.reject_reason_instruction), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text(stringResource(R.string.enter_reason_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        rejectingLoanId?.let { viewModel.rejectLoan(it, rejectReason, groupId) }
                        rejectingLoanId = null
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = rejectReason.isNotBlank()
                ) { Text(stringResource(R.string.reject_button_label), color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    rejectingLoanId = null
                    rejectReason = "" 
                }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName = homeUiState.userName,
                userPhone = homeUiState.userPhone,
                myGroups = homeUiState.myGroups,
                myRole = homeUiState.myRole,
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                HomeHeader(
                    userPhone = homeUiState.userPhone,
                    unreadCount = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = { BottomNavBar(navController) },
            snackbarHost = { SnackbarHost(snackbarHost) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (uiState.isLoading && !pullToRefreshState.isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (isLeader && currentGroup != null) {
                        GroupBalanceCard(
                            totalSavings = currentGroup.totalSavings,
                            actualAvailable = currentGroup.availableBalance
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (canApprove && pendingLoans.isNotEmpty()) {
                        SectionHeader(
                            title   = stringResource(R.string.pending_approvals_title),
                            badge   = pendingLoans.size.toString(),
                            action  = null
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pendingLoans.forEach { loan ->
                                PendingApprovalCard(
                                    loan       = loan,
                                    isApproving = uiState.isApproving == loan.id,
                                    isRejecting = uiState.isRejecting == loan.id,
                                    onApprove  = { viewModel.approveLoan(loan.id, groupId) },
                                    onReject   = { rejectingLoanId = loan.id }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    SectionHeader(
                        title  = stringResource(R.string.my_loan_title),
                        action = if (uiState.myLoans.isNotEmpty()) Pair(stringResource(R.string.all_loans_link)) {
                            navController.navigate("my_loans/$groupId")
                        } else null
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        when {
                            activeMyLoan != null -> {
                                ActiveLoanCard(
                                    loan         = activeMyLoan,
                                    onRepayClick = { navController.navigate("repay_loan/${activeMyLoan.id}") }
                                )
                            }
                            pendingMyLoan != null -> {
                                PendingOwnLoanCard(loan = pendingMyLoan)
                            }
                            uiState.myLoans.none { it.status.uppercase() == "ACTIVE" || it.status.uppercase() == "PENDING" } -> {
                                NoLoanCard(
                                    hasGroup = groupId.isNotEmpty(),
                                    onApply  = { navController.navigate("apply_loan/$groupId") }
                                )
                            }
                        }
                    }

                    val activeGroupLoans = uiState.groupLoans.filter { it.status.uppercase() == "ACTIVE" }
                    if (activeGroupLoans.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(
                            title  = stringResource(R.string.group_loan_activity_title),
                            action = null
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            activeGroupLoans.take(3).forEach { loan ->
                                GroupLoanCard(loan)
                            }
                            if (activeGroupLoans.size > 3) {
                                TextButton(
                                    onClick = { navController.navigate("group_loans/$groupId") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.more_loans_link, activeGroupLoans.size - 3), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }

                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GroupBalanceCard(totalSavings: Double, actualAvailable: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.group_balance_overview_title),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.total_rendered_label),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = FormatUtils.formatMoney(totalSavings),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    @Suppress("DEPRECATION")
                    Text(
                        text = stringResource(R.string.actual_available_label),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = FormatUtils.formatMoney(actualAvailable),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    badge: String? = null,
    action: Pair<String, () -> Unit>?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 11.sp, color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (action != null) {
            Text(
                action.first,
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { action.second() }
            )
        }
    }
}

@Composable
private fun PendingApprovalCard(
    loan: Loan,
    isApproving: Boolean,
    isRejecting: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val cleanedBorrowerName = remember(loan.borrower, loan.borrowerName) {
                        val first = loan.borrower?.firstName?.replace("null", "", true)?.trim() ?: ""
                        val last = loan.borrower?.lastName?.replace("null", "", true)?.trim() ?: ""
                        val fullName = "$first $last".trim()

                        fullName.ifEmpty {
                            loan.borrowerName?.replace("null", "", true)?.trim()?.ifEmpty { "Member" } ?: "Member"
                        }
                    }

                    val initials = cleanedBorrowerName.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials.ifEmpty { "?" }, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            cleanedBorrowerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        @Suppress("DEPRECATION")
                        Text(
                            loan.purpose?.ifBlank { stringResource(R.string.personal_loan_default) } ?: stringResource(R.string.personal_loan_default),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    FormatUtils.formatMoney(loan.principalAmount),
                    modifier = Modifier.weight(0.9f),
                    fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.applied_on_label, FormatUtils.formatDateTime(loan.createdAt)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!loan.purpose.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = loan.purpose,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LoanDetail(stringResource(R.string.duration_label), "${loan.durationMonths} mo.")
                LoanDetail(stringResource(R.string.interest_label), FormatUtils.formatMoney(loan.totalRepayable - loan.principalAmount))
                LoanDetail(stringResource(R.string.total_repay_label), FormatUtils.formatMoney(loan.totalRepayable))
                LoanDetail(stringResource(R.string.applied_label), FormatUtils.formatDate(loan.createdAt))
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onReject,
                    enabled  = !isRejecting && !isApproving,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    if (isRejecting) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.error, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        @Suppress("DEPRECATION")
                        Text(stringResource(R.string.decline_button), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick  = onApprove,
                    enabled  = !isApproving && !isRejecting,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    if (isApproving) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondary)
                        Spacer(Modifier.width(4.dp))
                        @Suppress("DEPRECATION")
                        Text(stringResource(R.string.approve_button), color = MaterialTheme.colorScheme.onSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveLoanCard(loan: Loan, onRepayClick: () -> Unit) {
    val pct = if (loan.totalRepayable > 0)
        ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable).toFloat()
            .coerceIn(0f, 1f)
    else 0f

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.outstanding_balance_label), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        FormatUtils.formatMoney(loan.principalAmount),
                        color = MaterialTheme.colorScheme.onPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Column {
                        Text(
                            "Month: ${loan.durationMonths}",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Interest: ${loan.interestRate.toInt()}%",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)) {
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.status_active_caps), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress   = { pct },
                modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color      = Color.White,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                @Suppress("DEPRECATION")
                Text(stringResource(R.string.loan_repaid_percent_alt, (pct * 100).toInt()), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
                if (!loan.dueDate.isNullOrBlank())
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.due_label, FormatUtils.formatDate(loan.dueDate)), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            val cleanedApprover = remember(loan.approverName) {
                loan.approverName?.replace("null", "", true)?.trim()
            }
            if (!loan.approvedAt.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.approved_by_label, cleanedApprover ?: "System"), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text("  ${FormatUtils.formatDate(loan.approvedAt)}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick  = onRepayClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor   = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Payment, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                @Suppress("DEPRECATION")
                Text(stringResource(R.string.make_repayment_button), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun PendingOwnLoanCard(loan: Loan) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(1.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Default.HourglassTop, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                @Suppress("DEPRECATION")
                Text(stringResource(R.string.pending_review_title), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                @Suppress("DEPRECATION")
                Text(
                    stringResource(R.string.awaiting_approval_msg, FormatUtils.formatMoney(loan.principalAmount)),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp
                )
                loan.purpose?.let {
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.purpose_display_label, it), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun NoLoanCard(hasGroup: Boolean, onApply: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CreditCard, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            @Suppress("DEPRECATION")
            Text(stringResource(R.string.no_active_loan_title), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            @Suppress("DEPRECATION")
            Text(
                if (hasGroup) stringResource(R.string.apply_funds_msg)
                else stringResource(R.string.join_group_loan_msg),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)
            )
            if (hasGroup) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onApply,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.apply_loan_button), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun GroupLoanCard(loan: Loan) {
    var expanded by remember { mutableStateOf(false) }
    val pct = if (loan.totalRepayable > 0)
        ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable)
            .toFloat().coerceIn(0f, 1f)
    else 0f

    val statusColor = when (loan.status.uppercase()) {
        "ACTIVE"    -> MaterialTheme.colorScheme.secondary
        "PENDING"   -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> MaterialTheme.colorScheme.primary
        "REJECTED"  -> MaterialTheme.colorScheme.error
        else        -> Color.Gray
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    @Suppress("DEPRECATION")
                    val defaultName = stringResource(R.string.member_default_name)
                    val cleanedBorrowerName = remember(loan.borrower, loan.borrowerName) {
                        val first = loan.borrower?.firstName?.replace("null", "", true)?.trim() ?: ""
                        val last = loan.borrower?.lastName?.replace("null", "", true)?.trim() ?: ""
                        val fullName = "$first $last".trim()

                        fullName.ifEmpty {
                            loan.borrowerName?.replace("null", "", true)?.trim()?.ifEmpty { defaultName } ?: defaultName
                        }
                    }
                    Text(
                        cleanedBorrowerName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val cleanedApprover = remember(loan.approver, loan.approverName) {
                        val first = loan.approver?.firstName?.replace("null", "", true)?.trim() ?: ""
                        val last = loan.approver?.lastName?.replace("null", "", true)?.trim() ?: ""
                        val fullName = "$first $last".trim()
                        
                        fullName.ifEmpty {
                            loan.approverName?.replace("null", "", true)?.trim()?.ifEmpty { "System" } ?: "System"
                        }
                    }
                    if (loan.status.uppercase() != "PENDING") {
                        @Suppress("DEPRECATION")
                        val actionText = when (loan.status.uppercase()) {
                            "REJECTED" -> stringResource(R.string.loan_rejected_by_simple, cleanedApprover)
                            else       -> stringResource(R.string.loan_approved_by_simple, cleanedApprover)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.height(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (loan.status.uppercase() == "REJECTED") Icons.Default.Close else Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = statusColor
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = actionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Column {
                        Text(
                            "Month: ${loan.durationMonths}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Interest: ${loan.interestRate.toInt()}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        loan.status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = stringResource(R.string.applied_on_label, FormatUtils.formatDateTime(loan.createdAt)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!loan.purpose.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = loan.purpose,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    FormatUtils.formatMoney(loan.principalAmount),
                    fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary
                )
            }

            if (loan.status == "ACTIVE") {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress   = { pct },
                    modifier   = Modifier.fillMaxWidth().height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color      = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.loan_repaid_percent_alt, (pct * 100).toInt()), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    @Suppress("DEPRECATION")
                    Text(
                        stringResource(R.string.amount_left_label, FormatUtils.formatMoney(loan.remainingBalance)),
                        fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LoanDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
