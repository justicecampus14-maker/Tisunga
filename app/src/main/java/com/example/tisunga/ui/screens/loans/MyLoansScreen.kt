package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.GroupViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    navController: NavController,
    groupId: String,
    viewModel: LoanViewModel,
    homeViewModel: HomeViewModel,
    groupViewModel: GroupViewModel? = null,
    userName: String? = null,
    userId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val groupUiState = groupViewModel?.uiState?.collectAsState()
    val focusManager = LocalFocusManager.current

    val pullToRefreshState = rememberPullToRefreshState()

    // Derive the user's role
    val myRole = groupUiState?.value?.currentUserRole?.uppercase() ?: homeUiState.myRole?.uppercase() ?: "MEMBER"
    val canApprove = myRole == "CHAIR" || myRole == "CHAIRPERSON" || myRole == "SECRETARY"

    var selectedStatus by remember { mutableStateOf("ALL") }
    var rejectingLoanId by remember { mutableStateOf<String?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    // Repayment Dialog State
    var showRepayDialog by remember { mutableStateOf(false) }
    var selectedLoanForRepay by remember { mutableStateOf<Loan?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val refreshData = {
        viewModel.getGroupLoans(groupId)
    }

    // Reject dialog
    if (rejectingLoanId != null) {
        val performReject = {
            if (rejectReason.isNotBlank()) {
                rejectingLoanId?.let { viewModel.rejectLoan(it, rejectReason, groupId) }
                rejectingLoanId = null; rejectReason = ""
            }
        }

        AlertDialog(
            onDismissRequest = { rejectingLoanId = null },
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
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { 
                            focusManager.clearFocus()
                            performReject() 
                        })
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { performReject() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = rejectReason.isNotBlank()
                ) { Text(stringResource(R.string.reject_button_label), color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectingLoanId = null; rejectReason = "" }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }

    if (showRepayDialog && selectedLoanForRepay != null) {
        RepayLoanDialog(
            loan = selectedLoanForRepay!!,
            onConfirm = { amount ->
                viewModel.repayLoan(selectedLoanForRepay!!.id, amount, homeUiState.userPhone)
                showRepayDialog = false
            },
            onDismiss = { showRepayDialog = false }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                viewModel.resetState()
            },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    viewModel.resetState()
                }) {
                    Text(stringResource(R.string.continue_button), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(stringResource(R.string.success_title)) },
            text = { Text(uiState.successMessage) },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    val filteredLoans = uiState.groupLoans.filter { loan ->
        val statusMatches = if (selectedStatus == "ALL") true 
                           else loan.status.uppercase() == selectedStatus.uppercase()
        val userMatches = if (userId.isNullOrBlank()) true 
                          else loan.borrowerId == userId
        statusMatches && userMatches
    }

    val totalCountForCurrentView = uiState.groupLoans.count { loan ->
        if (userId.isNullOrBlank()) true else loan.borrowerId == userId
    }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            val isMemberView = !userId.isNullOrBlank()

            // Header Section
            if (isMemberView) {
                MyLoansMemberHeader(
                    userName = if (userId == homeUiState.userId) homeUiState.userName 
                              else (userName ?: uiState.memberLoansName ?: ""),
                    navController = navController
                )
            } else {
                MyLoansHeaderSection(
                    userName = homeUiState.userName,
                    userPhone = homeUiState.userPhone,
                    navController = navController
                )
            }

            // Loading bar
            if (uiState.isLoading && !pullToRefreshState.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }

            // Status Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("ALL", "ACTIVE", "PENDING", "COMPLETED", "REJECTED")
                items(tabs) { status ->
                    val count = if (status == "ALL") totalCountForCurrentView 
                               else uiState.groupLoans.count { 
                                   val statusMatch = it.status.uppercase() == status.uppercase()
                                   val userMatch = if (userId.isNullOrBlank()) true else it.borrowerId == userId
                                   statusMatch && userMatch
                               }
                    val isSelected = selectedStatus == status
                    val labelRes = when (status) {
                        "ALL" -> R.string.filter_all
                        "PENDING" -> R.string.filter_pending
                        "ACTIVE" -> R.string.filter_active
                        "COMPLETED" -> R.string.filter_closed
                        "REJECTED" -> R.string.filter_rejected
                        else -> 0
                    }
                    val label = if (labelRes != 0) stringResource(labelRes) else status.lowercase().replaceFirstChar { it.uppercase() }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick  = { selectedStatus = status },
                        label    = {
                            Text(
                                if (count > 0) "$label ($count)" else label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.height(34.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.Transparent,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Role notice
            if (selectedStatus == "PENDING" && !canApprove) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.approve_permission_notice), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                if (filteredLoans.isEmpty() && !uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.no_loans_category_msg), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredLoans, key = { it.id }) { loan ->
                            FullGroupLoanCard(
                                loan = loan,
                                canApprove = canApprove && loan.status.uppercase() == "PENDING",
                                isApproving = uiState.isApproving == loan.id,
                                isRejecting = uiState.isRejecting == loan.id,
                                onApprove = { viewModel.approveLoan(loan.id, groupId) },
                                onReject = { rejectingLoanId = loan.id },
                                onRepay = {
                                    selectedLoanForRepay = loan
                                    showRepayDialog = true
                                },
                                isOwner = loan.borrowerId == homeUiState.userId
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
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
fun MyLoansMemberHeader(userName: String, navController: NavController) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                userName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_desc),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MyLoansHeaderSection(userName: String, userPhone: String, navController: NavController) {
    val initials = if (userName.isNotEmpty()) {
        userName.split(" ").filter { it.isNotEmpty() }.let { parts ->
            if (parts.size >= 2) {
                "${parts[0][0]}${parts[1][0]}".uppercase()
            } else if (parts.isNotEmpty()) {
                parts[0][0].toString().uppercase()
            } else ""
        }
    } else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(userPhone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        IconButton(
            onClick = { navController.navigate(Routes.NOTIFICATIONS) },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun FullGroupLoanCard(
    loan: Loan,
    canApprove: Boolean,
    isApproving: Boolean,
    isRejecting: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRepay: () -> Unit,
    isOwner: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor = when (loan.status.uppercase()) {
        "ACTIVE"    -> MaterialTheme.colorScheme.secondary
        "PENDING"   -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> MaterialTheme.colorScheme.primary
        "REJECTED"  -> MaterialTheme.colorScheme.error
        else        -> Color.Gray
    }
    val statusBg = statusColor.copy(alpha = 0.1f)

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
            // Header row
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

                        if (fullName.isNotEmpty()) fullName
                        else loan.borrowerName?.replace("null", "", true)?.trim()?.ifEmpty { "Member" } ?: "Member"
                    }
                    val initials = cleanedBorrowerName.split(" ")
                        .filter { it.isNotEmpty() }.take(2)
                        .joinToString("") { it.first().uppercase() }
                    Box(
                        modifier = Modifier.size(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials.ifEmpty { "?" }, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            cleanedBorrowerName, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            loan.purpose?.ifBlank { stringResource(R.string.personal_loan_default) } ?: stringResource(R.string.personal_loan_default),
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(FormatUtils.formatMoney(loan.principalAmount),
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                        Text(loan.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LoanInfo(stringResource(R.string.duration_label), "${loan.durationMonths} ${stringResource(id = if (loan.durationMonths == 1) R.string.period_1_month else R.string.duration_months_label).lowercase()}")
                LoanInfo(stringResource(R.string.interest_label), FormatUtils.formatMoney(loan.totalRepayable - loan.principalAmount))
                LoanInfo(stringResource(R.string.total_label), FormatUtils.formatMoney(loan.totalRepayable))
                LoanInfo(stringResource(R.string.applied_label), FormatUtils.formatDate(loan.createdAt))
            }

            // Progress (ACTIVE loans)
            if (loan.status.uppercase() == "ACTIVE") {
                Spacer(Modifier.height(12.dp))
                val pct = ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable)
                    .toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress   = { pct },
                    modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color      = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.loan_repaid_percent_alt, (pct * 100).toInt()), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.amount_left_label, FormatUtils.formatMoney(loan.remainingBalance)),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Rejection reason
            if (loan.status == "REJECTED" && !loan.purpose.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.purpose_display_label, loan.purpose), fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            // Approver (ACTIVE/COMPLETED/REJECTED)
            if (loan.status.uppercase() != "PENDING") {
                val cleanedApprover = remember(loan.approver, loan.approverName) {
                    val first = loan.approver?.firstName?.replace("null", "", true)?.trim() ?: ""
                    val last = loan.approver?.lastName?.replace("null", "", true)?.trim() ?: ""
                    val fullName = "$first $last".trim()

                    if (fullName.isNotEmpty()) fullName
                    else loan.approverName?.replace("null", "", true)?.trim()?.ifEmpty { "System" } ?: "System"
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (loan.status.uppercase() == "REJECTED") Icons.Default.Close else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (loan.status.uppercase() == "REJECTED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    val actionText = if (loan.status.uppercase() == "REJECTED") {
                        stringResource(R.string.loan_rejected_by_simple, cleanedApprover)
                    } else {
                        stringResource(R.string.loan_approved_by_simple, cleanedApprover)
                    }
                    Text(actionText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!loan.approvedAt.isNullOrBlank())
                        Text("  ${FormatUtils.formatDate(loan.approvedAt)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Approve / Reject buttons (CHAIR or SECRETARY only)
            if (canApprove) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick  = onReject,
                        enabled  = !isRejecting && !isApproving,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        if (isRejecting)
                            CircularProgressIndicator(Modifier.size(14.dp), color = MaterialTheme.colorScheme.error, strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.decline_button), fontSize = 13.sp)
                        }
                    }
                    Button(
                        onClick  = onApprove,
                        enabled  = !isApproving && !isRejecting,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isApproving)
                            CircularProgressIndicator(Modifier.size(14.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.approve_button), color = MaterialTheme.colorScheme.onSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else if (isOwner && loan.status.uppercase() == "ACTIVE") {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onRepay,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CreditCard, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.clear_button), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LoanInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun RepayLoanDialog(
    loan: Loan,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(loan.remainingBalance.toString()) }
    val focusManager = LocalFocusManager.current
    val onPay = {
        amount.toDoubleOrNull()?.let { onConfirm(it) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.clear_loan_balance_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.amount_to_clear_label),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.enter_amount_hint)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        focusManager.clearFocus()
                        onPay() 
                    })
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { onPay() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.pay_now_button), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
