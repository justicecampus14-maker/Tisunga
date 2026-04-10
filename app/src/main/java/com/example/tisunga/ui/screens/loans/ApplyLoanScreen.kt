package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.components.SuccessDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun ApplyLoanScreen(navController: NavController, groupId: Int, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    val period1Month = stringResource(R.string.period_1_month)
    val period2Months = stringResource(R.string.period_2_months)
    val period3Months = stringResource(R.string.period_3_months)
    val period6Months = stringResource(R.string.period_6_months)
    val period12Months = stringResource(R.string.period_12_months)

    var amount by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(period1Month) }
    var purpose by remember { mutableStateOf("") }
    var periodExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(
            title = "Loan Applied Successfully",
            message = "Your loan request has been submitted and is awaiting approval from the group officials.",
            onContinue = {
                showSuccessDialog = false
                viewModel.resetState()
                navController.popBackStack()
            }
        )
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = stringResource(R.string.apply_loan_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController, type = "C") },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = NavyBlue.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.loan_notice),
                    color = NavyBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LoanFormField(label = stringResource(R.string.loan_amount_label)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            it.toDoubleOrNull()?.let { valAmt -> viewModel.calculateLoan(valAmt) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.loan_amount_placeholder)) },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = loanFieldColors(),
                        prefix = { Text("MK ", fontWeight = FontWeight.Bold, color = NavyBlue) },
                        singleLine = true
                    )
                }

                if (amount.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LoanInfoCard(
                            label = stringResource(R.string.interest_label),
                            value = stringResource(R.string.amount_mk, com.example.tisunga.utils.FormatUtils.formatNumber(uiState.calculatedInterest)),
                            modifier = Modifier.weight(1f)
                        )
                        LoanInfoCard(
                            label = stringResource(R.string.total_repayable_label),
                            value = stringResource(R.string.amount_mk, com.example.tisunga.utils.FormatUtils.formatNumber(uiState.calculatedRepayable)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                LoanFormField(label = stringResource(R.string.loan_period_label)) {
                    Box {
                        OutlinedTextField(
                            value = period,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().clickable { periodExpanded = true },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                IconButton(onClick = { periodExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = NavyBlue)
                                }
                            },
                            colors = loanFieldColors()
                        )
                        DropdownMenu(
                            expanded = periodExpanded, 
                            onDismissRequest = { periodExpanded = false },
                            modifier = Modifier.background(White).fillMaxWidth(0.8f)
                        ) {
                            listOf(period1Month, period2Months, period3Months, period6Months, period12Months).forEach {
                                DropdownMenuItem(
                                    text = { Text(it) }, 
                                    onClick = { period = it; periodExpanded = false }
                                )
                            }
                        }
                    }
                }

                LoanFormField(label = stringResource(R.string.purpose_label)) {
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text(stringResource(R.string.purpose_placeholder)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = loanFieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    val amtVal = amount.toDoubleOrNull() ?: 0.0
                    if (amtVal > 0) {
                        viewModel.applyForLoan(
                            Loan(
                                id = 0, groupId = groupId, groupName = "", memberId = 0, memberName = "",
                                amount = amtVal, interestRate = 5.0, repayableAmount = uiState.calculatedRepayable,
                                remainingAmount = uiState.calculatedRepayable, percentRepaid = 0f,
                                dueDate = "", status = "pending", purpose = purpose, period = period
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = !uiState.isLoading && amount.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        stringResource(R.string.apply_now_button), 
                        color = White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LoanFormField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun LoanInfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = White.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
        }
    }
}

@Composable
private fun loanFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = DividerColor,
    focusedBorderColor = NavyBlue,
    unfocusedContainerColor = White,
    focusedContainerColor = White
)
