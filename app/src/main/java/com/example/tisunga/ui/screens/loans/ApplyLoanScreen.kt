package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Loan
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(stringResource(R.string.apply_loan_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.loan_notice),
            color = PurpleSubtitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.loan_amount_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        it.toDoubleOrNull()?.let { valAmt -> viewModel.calculateLoan(valAmt) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.loan_amount_placeholder)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                if (amount.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = BackgroundGray)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(stringResource(R.string.interest_label), fontSize = 12.sp, color = TextSecondary)
                                Text(stringResource(R.string.amount_mk, uiState.calculatedInterest), fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = BackgroundGray)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(stringResource(R.string.total_repayable_label), fontSize = 12.sp, color = TextSecondary)
                                Text(stringResource(R.string.amount_mk, uiState.calculatedRepayable), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.loan_period_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box {
                    OutlinedTextField(
                        value = period,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { periodExpanded = true },
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { periodExpanded = true }) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )
                    DropdownMenu(expanded = periodExpanded, onDismissRequest = { periodExpanded = false }) {
                        listOf(period1Month, period2Months, period3Months, period6Months, period12Months).forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { period = it; periodExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.purpose_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text(stringResource(R.string.purpose_placeholder)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.apply_now_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
