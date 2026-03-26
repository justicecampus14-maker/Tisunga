package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.SavingsViewModel

@Composable
fun MakeContributionScreen(navController: NavController, groupId: Int, viewModel: SavingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("+256..................") }
    var groupName by remember { mutableStateOf("Mphatso Group") }
    var contributionType by remember { mutableStateOf("Save") }
    var amount by remember { mutableStateOf("2000") }

    var phoneExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

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
            Text("Make Contribution", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "The active number will be used for the transaction",
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
                Text("Enter Phone_Number", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Box {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { phoneExpanded = true },
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { phoneExpanded = true }) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )
                    DropdownMenu(expanded = phoneExpanded, onDismissRequest = { phoneExpanded = false }) {
                        listOf("+265 882752624", "+265 999782230").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { phoneNumber = it; phoneExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Group Name", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Box {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { groupExpanded = true },
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { groupExpanded = true }) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )
                    DropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                        listOf("Mphatso Group", "Doman Group").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { groupName = it; groupExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Contribution Type", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Box {
                    OutlinedTextField(
                        value = contributionType,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true },
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { typeExpanded = true }) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        listOf("Save", "Special", "Event Contribution").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { contributionType = it; typeExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Amount (MK)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            viewModel.makeContribution(
                                Contribution(
                                    id = 0, groupId = groupId, userId = 0, userName = "",
                                    amount = amtVal, type = contributionType.lowercase(),
                                    timestamp = "", status = "pending", phoneNumber = phoneNumber
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Send", color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "You will receive a massage to enter your pin on your phone",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
