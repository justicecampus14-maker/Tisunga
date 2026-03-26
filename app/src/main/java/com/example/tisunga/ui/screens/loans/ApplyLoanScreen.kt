package com.example.tisunga.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Loan
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.LoanViewModel

@Composable
fun ApplyLoanScreen(navController: NavController, groupId: Int, viewModel: LoanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("1 Month") }
    var purpose by remember { mutableStateOf("") }
    var periodExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Apply Loan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "The requested loan will be repayed with an interest of 5%.\nAnd the interest will increase by 5% if not paid on time.",
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
                Text("Loan Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        it.toDoubleOrNull()?.let { valAmt -> viewModel.calculateLoan(valAmt) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Mk 20,000") },
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
                                Text("Interest", fontSize = 12.sp, color = TextSecondary)
                                Text("MK ${uiState.calculatedInterest}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = BackgroundGray)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total Repayable", fontSize = 12.sp, color = TextSecondary)
                                Text("MK ${uiState.calculatedRepayable}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Loan Period", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        listOf("1 Month", "2 Months", "3 Months", "6 Months", "12 Months").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { period = it; periodExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Purpose", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text("Friday") },
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
                    Text("Apply Now", color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
