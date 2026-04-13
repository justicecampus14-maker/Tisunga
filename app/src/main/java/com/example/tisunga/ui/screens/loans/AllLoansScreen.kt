package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.HomeViewModel
import com.example.tisunga.viewmodel.LoanViewModel
import java.util.Locale

// ── Tab item ──────────────────────────────────────────────────────────────────

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

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun AllLoansScreen(
    navController: NavController,
    viewModel: LoanViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState     by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf("Active") }

    LaunchedEffect(Unit) { viewModel.getMyLoans() }

    val currentGroup = homeUiState.myGroups.firstOrNull()
    val groupId      = currentGroup?.id ?: ""

    val myLoan = uiState.myLoans.firstOrNull { it.status.equals("ACTIVE", ignoreCase = true) }

    val filteredGroupLoans = when (selectedTab) {
        "Active"  -> uiState.groupLoans.filter { it.status.equals("ACTIVE",    ignoreCase = true) }
        "Pending" -> uiState.groupLoans.filter { it.status.equals("PENDING",   ignoreCase = true) }
        "History" -> uiState.groupLoans.filter { it.status.equals("COMPLETED", ignoreCase = true) }
        else      -> uiState.groupLoans
    }

    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick  = { navController.navigate("apply_loan/$groupId") },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Apply Loan", color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            HeaderSection(homeUiState.userName, homeUiState.userPhone, navController)

            // Back + title row
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape    = RoundedCornerShape(8.dp),
                    color    = Color.LightGray.copy(0.3f)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Loans", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(currentGroup?.name ?: "", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // My Loan section
            Column(modifier = Modifier.padding(16.dp)) {
                Text("My Loan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                if (myLoan != null) {
                    MyLoanCard(
                        loan        = myLoan,
                        onRepayClick = { navController.navigate("my_loans/$groupId") }
                    )
                } else {
                    Text("No active loan.", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // Group Loans section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Group Loans", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Requests",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = NavyBlue,
                        modifier   = Modifier.clickable { navController.navigate("group_loans/$groupId") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Active", "Pending", "History").forEach { tab ->
                        TabItem(label = tab, isSelected = selectedTab == tab) { selectedTab = tab }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (filteredGroupLoans.isEmpty()) {
                    Text("No loans in this category.", fontSize = 13.sp, color = TextSecondary)
                } else {
                    filteredGroupLoans.forEach { loan ->
                        GroupLoanCard(loan)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
fun HeaderSection(userName: String, userPhone: String, navController: NavController) {
    val initials = userName.takeIf { it.isNotEmpty() }?.take(1)?.uppercase() ?: "M"

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier.size(40.dp).background(NavyBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Hi, $userName", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Good morning", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape    = RoundedCornerShape(20.dp),
            color    = Color(0xFFE8E8E8).copy(alpha = 0.5f)
        ) {
            Text(
                userPhone,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 13.sp
            )
        }

        IconButton(
            onClick  = { navController.navigate(Routes.NOTIFICATIONS) },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(28.dp))
        }
    }
}

// ── My loan card ──────────────────────────────────────────────────────────────

@Composable
fun MyLoanCard(loan: Loan, onRepayClick: () -> Unit) {
    val pct = if (loan.totalRepayable > 0)
        ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable).toFloat()
    else 0f

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MK ${String.format(Locale.US, "%,.0f", loan.principalAmount)}",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
                Text(
                    loan.status.lowercase(Locale.US),
                    color    = Color(0xFFE57373),
                    fontSize = 12.sp
                )
            }

            // Approver line — built without escaped quotes
            val approverLine = buildString {
                if (!loan.approverName.isNullOrBlank()) append("Approved by ${loan.approverName}")
                if (!loan.approvedAt.isNullOrBlank())   append("  ${loan.approvedAt.take(10)}")
            }
            if (approverLine.isNotBlank()) {
                Text(approverLine, fontSize = 12.sp, lineHeight = 16.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(8.dp))

            // Progress
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${(pct * 100).toInt()}% repaid", fontSize = 11.sp, color = TextSecondary)
                Text(
                    "Remaining: MK ${String.format(Locale.US, "%,.0f", loan.remainingBalance)}",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress   = { pct.coerceIn(0f, 1f) },
                modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color      = PurpleProgress,
                trackColor = DividerColor
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    if (!loan.dueDate.isNullOrBlank()) "Due ${loan.dueDate.take(10)}" else "",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Red
                )
                Text(
                    "Repay Now",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = NavyBlue,
                    modifier   = Modifier.clickable { onRepayClick() }
                )
            }
        }
    }
}

// ── Group loan card ───────────────────────────────────────────────────────────

@Composable
fun GroupLoanCard(loan: Loan) {
    val pct = if (loan.totalRepayable > 0)
        ((loan.totalRepayable - loan.remainingBalance) / loan.totalRepayable).toFloat()
    else 0f

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                loan.borrowerName ?: "Member",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )

            // Approver line — built without escaped quotes
            val approverLine = buildString {
                if (!loan.approverName.isNullOrBlank()) append("Approved by ${loan.approverName}")
                if (!loan.approvedAt.isNullOrBlank())   append("  ${loan.approvedAt.take(10)}")
            }
            if (approverLine.isNotBlank()) {
                Text(approverLine, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Total borrowed: ", fontSize = 13.sp, color = TextPrimary)
                Text(
                    "MK ${String.format(Locale.US, "%,.2f", loan.principalAmount)}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${(pct * 100).toInt()}% repaid", fontSize = 11.sp, color = TextSecondary)
                Text(
                    "Remaining: MK ${String.format(Locale.US, "%,.0f", loan.remainingBalance)}",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }

            Spacer(Modifier.height(6.dp))

            LinearProgressIndicator(
                progress   = { pct.coerceIn(0f, 1f) },
                modifier   = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color      = Color(0xFF7C4DFF),
                trackColor = Color(0xFFF0F0F0)
            )

            if (!loan.dueDate.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Due ${loan.dueDate.take(10)}",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Red
                )
            }
        }
    }
}
