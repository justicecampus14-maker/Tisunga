package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoanScreen(
    navController: NavController,
    groupId: String,
    viewModel: LoanViewModel,
    groupViewModel: GroupViewModel,
    @Suppress("UNUSED_PARAMETER") homeViewModel: com.example.tisunga.viewmodel.HomeViewModel? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupState by groupViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Derive values from state
    val totalSavings = groupState.groupDashboard?.totalSavings 
        ?: groupState.selectedGroup?.totalSavings 
        ?: 0.0
    
    val savingPeriod = groupState.selectedGroup?.savingPeriod?.takeIf { it > 0 } ?: 12

    var amount by remember { mutableStateOf("") }
    var durationValue by remember { mutableIntStateOf(1) }
    var purpose by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Validation logic
    val amtVal = amount.toDoubleOrNull() ?: 0.0
    val isInsufficient = amtVal > totalSavings
    val isValidAmount = amount.isNotEmpty() && amtVal > 0

    // Synchronize local interest calculations
    LaunchedEffect(amount, durationValue) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        viewModel.calculateInterest(amt, durationValue)
    }

    // Success Navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessDialog = true
        }
    }

    // Error Handling: Show transient errors in a Snackbar
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage,
                duration = SnackbarDuration.Long
            )
            // Error has been communicated, clear it
            viewModel.resetState()
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(stringResource(R.string.application_sent_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.application_sent_msg),
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.resetState()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.back_to_loans_button), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apply_for_loan_title), fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LaunchedEffect(groupId) {
                groupViewModel.getGroupDashboard(groupId)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Interest is 5% compounded monthly",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.group_balance_label, FormatUtils.formatMoney(totalSavings)),
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.loan_amount_mk_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) amount = it 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.loan_amount_hint)) },
                        isError = isInsufficient,
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
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (amount.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Repayment Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InfoBox(
                                    label = "Effective Rate",
                                    value = "${String.format(Locale.US, "%.1f", uiState.calculatedInterestRate)}%",
                                    modifier = Modifier.weight(1f)
                                )
                                InfoBox(
                                    label = "Total Interest",
                                    value = FormatUtils.formatMoney(uiState.calculatedInterest),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InfoBox(
                                    label = "Monthly Pay",
                                    value = FormatUtils.formatMoney(uiState.calculatedPeriodicRepayment),
                                    modifier = Modifier.weight(1f)
                                )
                                InfoBox(
                                    label = "Total Repayable",
                                    value = FormatUtils.formatMoney(uiState.calculatedRepayable),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Duration (Months)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    val monthOptions = (1..savingPeriod).toList()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(monthOptions) { value ->
                            val isSelected = durationValue == value
                            Surface(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(48.dp)
                                    .clickable { durationValue = value },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "$value",
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.purpose_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
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
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (isValidAmount && !isInsufficient) {
                                viewModel.applyForLoan(groupId, amtVal, durationValue, purpose)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInsufficient || !isValidAmount) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                        ),
                        enabled = !uiState.isLoading && isValidAmount && !isInsufficient
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isInsufficient) stringResource(R.string.insufficient_balance_button) 
                                       else stringResource(R.string.submit_application_button),
                                color = MaterialTheme.colorScheme.onPrimary, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isInsufficient) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.insufficient_balance_error, FormatUtils.formatMoney(amtVal), FormatUtils.formatMoney(totalSavings)),
                                    color = MaterialTheme.colorScheme.error, fontSize = 12.sp, lineHeight = 16.sp
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}
