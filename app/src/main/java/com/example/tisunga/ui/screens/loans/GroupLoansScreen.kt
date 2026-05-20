package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.LoanViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupLoansScreen(
    navController: NavController,
    groupId: String,
    viewModel: LoanViewModel,
    groupViewModel: GroupViewModel? = null
) {
    val uiState       by viewModel.uiState.collectAsState()
    val groupUiState  = groupViewModel?.uiState?.collectAsState()

    // Derive the user's role from GroupViewModel if wired up, else fall back to MEMBER
    val myRole = groupUiState?.value?.currentUserRole?.uppercase() ?: "MEMBER"
    val canApprove = myRole == "CHAIR" || myRole == "SECRETARY"

    var selectedStatus  by remember { mutableStateOf("ALL") }
    var rejectingLoanId by remember { mutableStateOf<String?>(null) }
    var rejectReason    by remember { mutableStateOf("") }
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        viewModel.getGroupLoans(groupId)
    }

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

    //Reject dialog
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
                OutlinedButton(onClick = { rejectingLoanId = null; rejectReason = "" }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.group_loans_title), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.getGroupLoans(groupId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
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
                val tabs = listOf("ALL", "ACTIVE", "PENDING", "COMPLETED", "REJECTED")
                items(tabs) { status ->
                    val count = if (status == "ALL") uiState.groupLoans.size else uiState.groupLoans.count { it.status == status }
                    val isSelected = selectedStatus == status
                    val label = when (status) {
                        "ALL" -> stringResource(R.string.filter_all)
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

            // Role notice for non-approvers viewing PENDING tab
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

            val filtered = if (selectedStatus == "ALL") uiState.groupLoans else uiState.groupLoans.filter { it.status == selectedStatus }

            if (filtered.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CreditCard, null,
                            tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        val statusLabel = when (selectedStatus) {
                            "ALL" -> stringResource(R.string.filter_all).lowercase()
                            "ACTIVE" -> stringResource(R.string.filter_active)
                            "PENDING" -> stringResource(R.string.filter_pending)
                            "COMPLETED" -> stringResource(R.string.filter_closed)
                            "REJECTED" -> stringResource(R.string.filter_rejected)
                            else -> selectedStatus.lowercase()
                        }
                        Text(stringResource(R.string.no_loans_found_msg, statusLabel), color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { loan ->
                        FullGroupLoanCard(
                            loan        = loan,
                            canApprove  = canApprove && loan.status == "PENDING",
                            isApproving = uiState.isApproving == loan.id,
                            isRejecting = uiState.isRejecting == loan.id,
                            onApprove   = { viewModel.approveLoan(loan.id, groupId) },
                            onReject    = { rejectingLoanId = loan.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

// Full Group Loan Card

@Composable
fun FullGroupLoanCard(
    loan: Loan,
    canApprove: Boolean,
    isApproving: Boolean,
    isRejecting: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
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
        colors    = CardDefaults.cardColors(containerColor = Color.White),
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
                    val initials = (loan.borrowerName ?: "").split(" ")
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
                        Text(loan.borrowerName ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(loan.purpose?.ifBlank { stringResource(R.string.personal_loan_default) } ?: stringResource(R.string.personal_loan_default),
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
                LoanDetailItem(stringResource(R.string.duration_label), "${loan.durationMonths} ${stringResource(id = if (loan.durationMonths == 1) R.string.period_1_month else R.string.duration_months_label).lowercase()}")
                LoanDetailItem(stringResource(R.string.interest_label),
                    "MK ${String.format(Locale.US, "%,.0f", loan.totalRepayable - loan.principalAmount)}")
                LoanDetailItem(stringResource(R.string.total_label), "MK ${String.format(Locale.US, "%,.0f", loan.totalRepayable)}")
                LoanDetailItem(stringResource(R.string.applied_label), loan.createdAt.take(10))
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
            }
        }
    }
}
