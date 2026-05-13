package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.example.tisunga.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun ApplyLoanScreen(
    navController: NavController,
    groupId: String,
    viewModel: LoanViewModel,
    groupViewModel: GroupViewModel,
    homeViewModel: com.example.tisunga.viewmodel.HomeViewModel? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupState by groupViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Priority: live dashboard → seeded selectedGroup → HomeViewModel group
    val totalSavings = groupState.groupDashboard?.group?.totalSavings
        ?: groupState.selectedGroup?.totalSavings
        ?: homeViewModel?.uiState?.collectAsState()?.value?.myGroups?.firstOrNull()?.totalSavings
        ?: 0.0

    var amount by remember { mutableStateOf("") }
    var durationValue by remember { mutableIntStateOf(1) }
    var purpose by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Automatically synchronize calculations whenever inputs change
    LaunchedEffect(amount, durationValue) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        viewModel.calculateInterest(amt, durationValue)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(GreenAccent.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenAccent, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(stringResource(R.string.application_sent_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.application_sent_msg),
                        fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.resetState()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text(stringResource(R.string.back_to_loans_button), color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apply_for_loan_title), fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LaunchedEffect(groupId) {
                groupViewModel.getGroupDashboard(groupId)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = NavyBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.loan_interest_notice),
                            fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.group_balance_label, String.format("%,.0f", totalSavings)),
                            fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.loan_amount_mk_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.loan_amount_hint)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                                keyboardController?.hide()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = White,
                            focusedBorderColor = NavyBlue
                        )
                    )

                    if (amount.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Repayment Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 2x2 Grid of Boxes
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InfoBox(
                                    label = "Rate",
                                    value = "${String.format("%.1f", uiState.calcRate)}%",
                                    modifier = Modifier.weight(1f)
                                )
                                InfoBox(
                                    label = "Total Interest",
                                    value = FormatUtils.formatMoney(uiState.calcInterest),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InfoBox(
                                    label = "Per Month",
                                    value = FormatUtils.formatMoney(uiState.calcMonthly),
                                    modifier = Modifier.weight(1f)
                                )
                                InfoBox(
                                    label = "Repayable",
                                    value = FormatUtils.formatMoney(uiState.calcRepayable),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.duration_months_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(1, 2, 3, 4)
                        options.forEach { value ->
                            val isSelected = durationValue == value
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NavyBlue else BackgroundGray,
                                onClick = { durationValue = value }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "$value",
                                        color = if (isSelected) White else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(stringResource(R.string.purpose_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text(stringResource(R.string.loan_purpose_hint)) },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = White,
                            focusedBorderColor = NavyBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val amtVal = amount.toDoubleOrNull() ?: 0.0
                    val isInsufficient = amtVal > totalSavings

                    Button(
                        onClick = {
                            if (amtVal > 0 && !isInsufficient) {
                                viewModel.applyForLoan(groupId, amtVal, durationValue, purpose)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInsufficient) Color.Gray else NavyBlue
                        ),
                        enabled = !uiState.isLoading && amount.isNotEmpty() && !isInsufficient
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                if (isInsufficient) stringResource(R.string.insufficient_balance_button) else stringResource(R.string.submit_application_button),
                                color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isInsufficient) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.insufficient_balance_error, String.format("%,.0f", amtVal), String.format("%,.0f", totalSavings)),
                                    color = Color.Red, fontSize = 12.sp, lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (uiState.errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null,
                                    tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    uiState.errorMessage,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundGray.copy(alpha = 0.4f)),
        border = BorderStroke(0.5.dp, DividerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = NavyBlue,
                maxLines = 1
            )
        }
    }
}
