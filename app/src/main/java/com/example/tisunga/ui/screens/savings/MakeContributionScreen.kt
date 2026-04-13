package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.ContributionViewModel
import com.example.tisunga.viewmodel.SavingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeContributionScreen(
    navController: NavController,
    groupId: String,
    viewModel: ContributionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(viewModel.getUserPhone()) }
    
    val contributionTypes = listOf(
        "REGULAR" to "Regular Savings",
        "SHARE_PURCHASE" to "Buy Shares",
        "SOCIAL_FUND" to "Social Fund",
        "LOAN_REPAYMENT" to "Loan Repayment"
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(contributionTypes[0]) }

    if (uiState.showPendingDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.dismissPendingDialog()
                    navController.popBackStack() 
                }) {
                    Text("OK", color = NavyBlue)
                }
            },
            title = { Text("Payment Initiated", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please check your phone ($phone) for a PIN prompt to authorize the payment of MK${amount}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ref: ${uiState.initResponse?.transactionRef}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = NavyBlue) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Make Contribution", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("New Contribution", fontWeight = FontWeight.Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Contribution Type", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType.second,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            contributionTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.second) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Phone Number", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("+265...") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Amount (MK)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Enter amount") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1000", "5000", "10000").forEach { quickAmount ->
                            SuggestionChip(
                                onClick = { amount = quickAmount },
                                label = { Text("MK $quickAmount") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.errorMessage != null) {
                        Text(uiState.errorMessage!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(
                        onClick = { 
                            val amtVal = amount.toDoubleOrNull() ?: 0.0
                            if (amtVal > 0 && phone.isNotEmpty()) {
                                viewModel.makeContribution(groupId, amtVal, phone, selectedType.first)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        enabled = !uiState.isLoading && amount.isNotEmpty() && phone.isNotEmpty()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Make Contribution", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "You will receive a PIN prompt on your phone to authorize this transaction.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
