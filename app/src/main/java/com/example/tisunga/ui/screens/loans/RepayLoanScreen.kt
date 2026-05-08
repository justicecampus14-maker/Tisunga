package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepayLoanScreen(
    navController: NavController,
    loan: Loan,
    userPhone: String,
    viewModel: LoanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(userPhone) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            viewModel.resetState()
            navController.popBackStack()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.repay_loan_title), fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
            // Loan Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.remaining_balance_label), fontSize = 14.sp, color = TextSecondary)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GreenAccent.copy(alpha = 0.1f)
                        ) {
                            Text(
                                stringResource(R.string.status_active_caps),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.amount_mk, String.format("%,.2f", loan.remainingBalance)),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val progress = (1 - (loan.remainingBalance / loan.totalRepayable)).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).background(BackgroundGray, RoundedCornerShape(4.dp)),
                        color = GreenAccent,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.loan_repaid_percent, (progress * 100).toInt()), fontSize = 12.sp, color = TextSecondary)
                        Text(stringResource(R.string.total_amount_label, String.format("%,.0f", loan.totalRepayable)), fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.amount_to_repay_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.enter_amount_hint)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.clearFocus()
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

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickAmountButton(stringResource(R.string.full_payment_button), onClick = { amount = loan.remainingBalance.toString() }, modifier = Modifier.weight(1f))
                        QuickAmountButton(stringResource(R.string.half_payment_button), onClick = { amount = (loan.remainingBalance / 2).toString() }, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(stringResource(R.string.phone_number_input_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.phone_number_hint)) },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, null, tint = NavyBlue) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
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

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && phone.isNotEmpty()) {
                                viewModel.repayLoan(loan.id, amt, phone)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        enabled = !uiState.isRepaying && amount.isNotEmpty() && phone.isNotEmpty()
                    ) {
                        if (uiState.isRepaying) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(stringResource(R.string.initiate_payment_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.errorMessage.isNotEmpty()) {
                        Text(
                            uiState.errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.stk_push_notice),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuickAmountButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue.copy(alpha = 0.3f))
    ) {
        Text(text, fontSize = 12.sp, color = NavyBlue)
    }
}
