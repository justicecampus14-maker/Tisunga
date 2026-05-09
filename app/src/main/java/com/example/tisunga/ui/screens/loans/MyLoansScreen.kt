package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CreditCard
import java.util.Locale

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

    // Derive the user's role
    val myRole = groupUiState?.value?.currentUserRole?.uppercase() ?: homeUiState.myRole?.uppercase() ?: "MEMBER"
    val canApprove = myRole == "CHAIR" || myRole == "CHAIRPERSON" || myRole == "SECRETARY"

    var selectedStatus by remember { mutableStateOf("ACTIVE") }
    var rejectingLoanId by remember { mutableStateOf<String?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    // Repayment Dialog State
    var showRepayDialog by remember { mutableStateOf(false) }
    var selectedLoanForRepay by remember { mutableStateOf<Loan?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Reject dialog
    if (rejectingLoanId != null) {
        AlertDialog(
            onDismissRequest = { rejectingLoanId = null },
            icon = {
                Icon(Icons.Default.DoNotDisturb, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = { Text(stringResource(R.string.reject_loan_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.reject_reason_instruction), fontSize = 13.sp, color = TextSecondary)
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
                        rejectingLoanId = null; rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = rejectReason.isNotBlank()
                ) { Text(stringResource(R.string.reject_button_label), color = Color.White) }
            },
            dismissButton = {
                @Suppress("DEPRECATION")
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

    val filteredLoans = uiState.groupLoans.filter { it.status.uppercase() == selectedStatus }

    LaunchedEffect(groupId) {
        viewModel.getGroupLoans(groupId)
    }

    val currentUserIdParam = userId // Capture param to avoid potential shadowing issues

    Scaffold(
        containerColor = BackgroundGray,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.APPLY_LOAN.replace("{groupId}", groupId)) },
                containerColor = NavyBlue,
                contentColor = White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.apply_loan_button), modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
                .statusBarsPadding()
        ) {
            val isViewingOtherMember = !currentUserIdParam.isNullOrBlank() && currentUserIdParam != homeUiState.userId

            // Header Section
            if (isViewingOtherMember) {
                MyLoansMemberHeader(
                    userName = userName ?: uiState.memberLoansName ?: "",
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
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NavyBlue)
            }

            // Status Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("ACTIVE", "PENDING", "COMPLETED", "REJECTED")
                items(tabs) { status ->
                    val count = uiState.groupLoans.count { it.status == status }
                    val isSelected = selectedStatus == status
                    val label = when (status) {
                        "PENDING" -> stringResource(R.string.filter_pending)
                        "ACTIVE" -> stringResource(R.string.filter_active)
                        "COMPLETED" -> stringResource(R.string.filter_closed)
                        "REJECTED" -> stringResource(R.string.filter_rejected)
                        else -> status.lowercase().replaceFirstChar { it.uppercase() }
                    }
                    
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
                            selectedContainerColor = NavyBlue,
                            selectedLabelColor     = Color.White,
                            containerColor = BackgroundGray.copy(alpha = 0.5f),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.Transparent,
                            selectedBorderColor = NavyBlue
                        )
                    )
                }
            }

            // Role notice
            if (selectedStatus == "PENDING" && !canApprove) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.approve_permission_notice), fontSize = 11.sp, color = TextSecondary)
                }
            }

            if (filteredLoans.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CreditCard, null,
                            tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.no_loans_category_msg), color = TextSecondary, fontSize = 14.sp)
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
                            loan        = loan,
                            canApprove  = canApprove && loan.status == "PENDING",
                            isApproving = uiState.isApproving == loan.id,
                            isRejecting = uiState.isRejecting == loan.id,
                            onApprove   = { viewModel.approveLoan(loan.id, groupId) },
                            onReject    = { rejectingLoanId = loan.id },
                            onRepay     = {
                                selectedLoanForRepay = loan
                                showRepayDialog = true
                            },
                            isOwner     = loan.borrowerName == homeUiState.userName
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
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
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_desc),
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White),
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
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
    val statusColor = when (loan.status) {
        "ACTIVE"    -> GreenAccent
        "PENDING"   -> NavyBlue
        "COMPLETED" -> NavyBlue
        "REJECTED"  -> RedAccent
        else        -> Color.Gray
    }
    val statusBg = statusColor.copy(alpha = 0.1f)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val initials = loan.borrowerName.split(" ")
                        .filter { it.isNotEmpty() }.take(2)
                        .joinToString("") { it.first().uppercase() }
                    Box(
                        modifier = Modifier.size(38.dp)
                            .background(NavyBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials.ifEmpty { "?" }, fontWeight = FontWeight.Bold,
                            color = NavyBlue, fontSize = 13.sp)
                    }
                    Column {
                        Text(loan.borrowerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(loan.purpose?.ifBlank { "Personal loan" } ?: "Personal loan",
                            fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("MK ${String.format(Locale.US, "%,.0f", loan.principalAmount)}",
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyBlue)
                    Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                        Text(loan.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BackgroundGray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LoanInfo(stringResource(R.string.duration_label), "${loan.durationMonths} mo.")
                LoanInfo(stringResource(R.string.interest_label),
                    "MK ${String.format(Locale.US, "%,.0f", loan.totalRepayable - loan.principalAmount)}")
                LoanInfo(stringResource(R.string.total_label), "MK ${String.format(Locale.US, "%,.0f", loan.totalRepayable)}")
                LoanInfo(stringResource(R.string.applied_label), loan.createdAt.take(10))
            }

            // Progress (ACTIVE loans)
            if (loan.status == "ACTIVE") {
                Spacer(Modifier.height(12.dp))
                val pct = ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable)
                    .toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress   = { pct },
                    modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color      = GreenAccent,
                    trackColor = BackgroundGray
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.loan_repaid_percent_alt, (pct * 100).toInt()), fontSize = 10.sp, color = TextSecondary)
                    Text(stringResource(R.string.amount_left_label, String.format(Locale.US, "%,.0f", loan.remainingBalance)),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            // Rejection reason
            if (loan.status == "REJECTED" && !loan.purpose.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = RedAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.purpose_display_label, loan.purpose), fontSize = 11.sp, color = RedAccent)
                }
            }

            // Approver (ACTIVE/COMPLETED)
            if (!loan.approverName.isNullOrBlank() && loan.status != "PENDING") {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null,
                        tint = GreenAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.approved_by_label, loan.approverName), fontSize = 11.sp, color = TextSecondary)
                    if (!loan.approvedAt.isNullOrBlank())
                        Text("  ${loan.approvedAt.take(10)}", fontSize = 10.sp, color = TextSecondary)
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
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.4f))
                    ) {
                        if (isRejecting)
                            CircularProgressIndicator(Modifier.size(14.dp), color = RedAccent, strokeWidth = 2.dp)
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
                        colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        if (isApproving)
                            CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.approve_button), color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            } else if (isOwner && loan.status == "ACTIVE") {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onRepay,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Icon(Icons.Default.CreditCard, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.clear_button), color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LoanInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
fun RepayLoanDialog(
    loan: Loan,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(loan.remainingBalance.toString()) }

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
                    )
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
                        onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
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
