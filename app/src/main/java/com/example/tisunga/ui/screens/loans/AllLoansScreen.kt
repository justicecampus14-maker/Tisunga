package com.example.tisunga.ui.screens.loans

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.screens.home.AppDrawerContent
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.LoanViewModel
import com.example.tisunga.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import java.util.Locale

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

    val currentGroup = homeUiState.myGroups.firstOrNull()
    val groupId      = currentGroup?.id ?: ""
    val myRole       = homeUiState.myRole?.uppercase() ?: ""
    val canApprove   = myRole == "CHAIR" || myRole == "SECRETARY"

    // Reject-reason dialog state
    var rejectingLoanId by remember { mutableStateOf<String?>(null) }
    var rejectReason    by remember { mutableStateOf("") }

    // Load both my loans + group loans on entry
    LaunchedEffect(groupId) {
        viewModel.getMyLoans()
        if (groupId.isNotEmpty()) viewModel.getGroupLoans(groupId)
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

    val pendingLoans = uiState.groupLoans.filter { it.status == "PENDING" }
    val activeMyLoan = uiState.myLoans.firstOrNull { it.status == "ACTIVE" }
    val pendingMyLoan = uiState.myLoans.firstOrNull { it.status == "PENDING" }

    // ── Reject dialog ─────────────────────────────────────────────────────────

    if (rejectingLoanId != null) {
        AlertDialog(
            onDismissRequest = { rejectingLoanId = null },
            icon = {
                Icon(Icons.Default.DoNotDisturb, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = { Text("Reject Loan?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide a reason so the member understands the decision.", fontSize = 13.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Enter reason...") },
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
                ) { Text("Reject", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectingLoanId = null; rejectReason = "" }) { Text("Cancel") }
            }
        )
    }

    // ── Screen ────────────────────────────────────────────────────────────────

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
            containerColor = BackgroundGray,
            topBar = {
                HomeHeader(
                    userPhone = homeUiState.userPhone,
                    unreadCount = notificationState.unreadCount,
                    navController = navController,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = { BottomNavBar(navController) },
            snackbarHost = { SnackbarHost(snackbarHost) },
            floatingActionButton = {
                if (groupId.isNotEmpty() && activeMyLoan == null && pendingMyLoan == null) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate("apply_loan/$groupId") },
                        icon    = { Icon(Icons.Default.Add, null) },
                        text    = { Text("Apply for Loan") },
                        containerColor = NavyBlue,
                        contentColor   = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Loading ───────────────────────────────────────────────────
                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = NavyBlue
                    )
                }

                // ── Section: Pending Approvals (CHAIR / SECRETARY only) ────────
                if (canApprove && pendingLoans.isNotEmpty()) {
                    SectionHeader(
                        title   = "Pending Approvals",
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

                // ── Section: My Loan status ───────────────────────────────────
                SectionHeader(
                    title  = "My Loan",
                    action = if (uiState.myLoans.isNotEmpty()) Pair("History") {
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
                        uiState.myLoans.none { it.status == "ACTIVE" || it.status == "PENDING" } -> {
                            NoLoanCard(
                                hasGroup = groupId.isNotEmpty(),
                                onApply  = { navController.navigate("apply_loan/$groupId") }
                            )
                        }
                    }
                }

                // ── Section: Group Loan Activity ──────────────────────────────
                val activeGroupLoans = uiState.groupLoans.filter { it.status == "ACTIVE" }
                if (activeGroupLoans.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(
                        title  = "Group Loan Activity",
                        action = Pair("All") { navController.navigate("group_loans/$groupId") }
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
                                Text("+${activeGroupLoans.size - 3} more loans", color = NavyBlue)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

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
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .background(RedAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (action != null) {
            Text(
                action.first,
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyBlue,
                modifier = Modifier.clickable { action.second() }
            )
        }
    }
}

// ─── Pending Approval Card (for CHAIR / SECRETARY) ────────────────────────────

@Composable
private fun PendingApprovalCard(
    loan: Loan,
    isApproving: Boolean,
    isRejecting: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Borrower + amount row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Initials avatar
                    val initials = loan.borrowerName.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NavyBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials.ifEmpty { "?" }, fontWeight = FontWeight.Bold,
                            color = NavyBlue, fontSize = 14.sp)
                    }
                    Column {
                        Text(loan.borrowerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            loan.purpose?.ifBlank { "Personal loan" } ?: "Personal loan",
                            fontSize = 12.sp, color = TextSecondary
                        )
                    }
                }
                Text(
                    "MK ${String.format(Locale.US, "%,.0f", loan.principalAmount)}",
                    fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NavyBlue
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BackgroundGray)

            // Loan details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LoanDetail("Duration", "${loan.durationMonths} mo.")
                LoanDetail("Interest", "MK ${String.format(Locale.US, "%,.0f",
                    loan.totalRepayable - loan.principalAmount)}")
                LoanDetail("Total Repay", "MK ${String.format(Locale.US, "%,.0f",
                    loan.totalRepayable)}")
                LoanDetail("Applied", loan.createdAt.take(10))
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onReject,
                    enabled  = !isRejecting && !isApproving,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.4f))
                ) {
                    if (isRejecting) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = RedAccent, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Decline", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick  = onApprove,
                    enabled  = !isApproving && !isRejecting,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    if (isApproving) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Approve", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── Active Loan Card (borrower's own active loan) ────────────────────────────

@Composable
private fun ActiveLoanCard(loan: Loan, onRepayClick: () -> Unit) {
    val pct = if (loan.totalRepayable > 0)
        ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable).toFloat()
            .coerceIn(0f, 1f)
    else 0f

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyBlue),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Outstanding Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "MK ${String.format(Locale.US, "%,.0f", loan.remainingBalance)}",
                        color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = GreenAccent.copy(alpha = 0.2f)) {
                    Text("ACTIVE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress   = { pct },
                modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color      = Color(0xFFFFEB3B),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(pct * 100).toInt()}% repaid", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                if (!loan.dueDate.isNullOrBlank())
                    Text("Due ${loan.dueDate.take(10)}", color = Color(0xFFFFEB3B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // Approver row
            if (!loan.approverName.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = GreenAccent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Approved by ${loan.approverName}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick  = onRepayClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = NavyBlue
                )
            ) {
                Icon(Icons.Default.Payment, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Make a Repayment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ─── Pending Own Loan Card ────────────────────────────────────────────────────

@Composable
private fun PendingOwnLoanCard(loan: Loan) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(1.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC02).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Default.HourglassTop, null,
                tint = Color(0xFFE65100), modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Pending Review", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "MK ${String.format(Locale.US, "%,.0f", loan.principalAmount)} — awaiting CHAIR / SECRETARY approval.",
                    fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp
                )
                loan.purpose?.let {
                    Text("Purpose: $it", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

// ─── No Loan Card ─────────────────────────────────────────────────────────────

@Composable
private fun NoLoanCard(hasGroup: Boolean, onApply: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CreditCard, null,
                tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("No active loan", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Text(
                if (hasGroup) "You can apply once you need funds from the group."
                else "Join or create a group to access loans.",
                fontSize = 12.sp, color = TextSecondary,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)
            )
            if (hasGroup) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onApply,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = NavyBlue)
                    Spacer(Modifier.width(6.dp))
                    Text("Apply for a Loan", color = NavyBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


// ─── Group Loan Summary Card (read-only, used in AllLoansScreen overview) ─────
// Full card with approve/reject is in GroupLoansScreen → FullGroupLoanCard

@Composable
fun GroupLoanCard(loan: Loan) {
    val pct = if (loan.totalRepayable > 0)
        ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable)
            .toFloat().coerceIn(0f, 1f)
    else 0f

    val statusColor = when (loan.status) {
        "ACTIVE"    -> GreenAccent
        "PENDING"   -> Color(0xFFF59E0B)
        "COMPLETED" -> NavyBlue
        "REJECTED"  -> RedAccent
        else        -> Color.Gray
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    loan.borrowerName.ifBlank { "Member" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    modifier   = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "MK ${String.format(Locale.US, "%,.0f", loan.principalAmount)}",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue
                    )
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            loan.status,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor
                        )
                    }
                }
            }

            if (loan.status == "ACTIVE") {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress   = { pct },
                    modifier   = Modifier.fillMaxWidth().height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color      = GreenAccent,
                    trackColor = BackgroundGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${(pct * 100).toInt()}% repaid", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        "MK ${String.format(Locale.US, "%,.0f", loan.remainingBalance)} left",
                        fontSize = 10.sp, color = TextSecondary
                    )
                }
            }
        }
    }
}

// ─── Reusable helpers (also used by GroupLoansScreen) ─────────────────────────

@Composable
fun LoanDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier        = Modifier.width(85.dp).height(36.dp).clickable { onClick() },
        shape           = RoundedCornerShape(8.dp),
        color           = if (isSelected) White else Color.LightGray.copy(alpha = 0.2f),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                fontSize   = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = if (isSelected) TextPrimary else TextSecondary
            )
        }
    }
}
