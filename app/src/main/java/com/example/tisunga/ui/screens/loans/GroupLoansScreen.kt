package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.utils.FormatUtils
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
    val focusManager  = LocalFocusManager.current

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Loading bar
            if (uiState.isLoading) {
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

            // Role notice for non-approvers viewing PENDING tab
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

            val filtered = if (selectedStatus == "ALL") uiState.groupLoans else uiState.groupLoans.filter { it.status == selectedStatus }

            if (filtered.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CreditCard, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        val statusLabel = when (selectedStatus) {
                            "ALL" -> stringResource(R.string.filter_all).lowercase()
                            "ACTIVE" -> stringResource(R.string.filter_active)
                            "PENDING" -> stringResource(R.string.filter_pending)
                            "COMPLETED" -> stringResource(R.string.filter_closed)
                            "REJECTED" -> stringResource(R.string.filter_rejected)
                            else -> selectedStatus.lowercase()
                        }
                        Text(stringResource(R.string.no_loans_found_msg, statusLabel), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            } else {
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
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
    var expanded by remember { mutableStateOf(false) }
    val statusColor = when (loan.status) {
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
                LoanDetailItem(stringResource(R.string.duration_label), "${loan.durationMonths} ${stringResource(id = if (loan.durationMonths == 1) R.string.period_1_month else R.string.duration_months_label).lowercase()}")
                LoanDetailItem(stringResource(R.string.interest_label),
                    FormatUtils.formatMoney(loan.totalRepayable - loan.principalAmount))
                LoanDetailItem(stringResource(R.string.total_label), FormatUtils.formatMoney(loan.totalRepayable))
                LoanDetailItem(stringResource(R.string.applied_label), FormatUtils.formatDate(loan.createdAt))
            }

            // Progress (ACTIVE loans)
            if (loan.status == "ACTIVE") {
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
                    Text(stringResource(R.string.amount_left_label, String.format(Locale.US, "%,.0f", loan.remainingBalance)),
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
            if (loan.status != "PENDING") {
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
                        imageVector = if (loan.status == "REJECTED") Icons.Default.Close else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    val actionText = if (loan.status == "REJECTED") {
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
            }
        }
    }
}
