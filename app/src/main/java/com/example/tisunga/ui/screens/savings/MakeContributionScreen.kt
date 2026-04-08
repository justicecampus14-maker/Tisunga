package com.example.tisunga.ui.screens.savings

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
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.SavingsViewModel

@Composable
fun MakeContributionScreen(navController: NavController, groupId: Int, viewModel: SavingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("+265 882752624") }
    
    val contributionTypes = listOf("Savings", "Funeral", "Welfare", "Wedding")
    var contributionType by remember { mutableStateOf(contributionTypes[0]) }
    var amount by remember { mutableStateOf("2000") }

    var phoneExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = stringResource(R.string.make_contribution_title),
                onBackClick = { navController.popBackStack() }
            )
        },
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
                color = GreenLight.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.active_number_notice),
                    color = GreenAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Phone Number Field
                ContributionFormField(label = stringResource(R.string.enter_phone_label)) {
                    Box {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().clickable { phoneExpanded = true },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                IconButton(onClick = { phoneExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = NavyBlue)
                                }
                            },
                            colors = contributionFieldColors()
                        )
                        DropdownMenu(
                            expanded = phoneExpanded,
                            onDismissRequest = { phoneExpanded = false },
                            modifier = Modifier.background(White).fillMaxWidth(0.8f)
                        ) {
                            listOf("+265 882752624", "+265 999782230").forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = TextPrimary) },
                                    onClick = { phoneNumber = it; phoneExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Contribution Type Field
                ContributionFormField(label = stringResource(R.string.contribution_type_label)) {
                    Box {
                        OutlinedTextField(
                            value = contributionType,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                IconButton(onClick = { typeExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = NavyBlue)
                                }
                            },
                            colors = contributionFieldColors()
                        )
                        DropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
                            modifier = Modifier.background(White).fillMaxWidth(0.8f)
                        ) {
                            contributionTypes.forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = TextPrimary) },
                                    onClick = { contributionType = it; typeExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Amount Field
                ContributionFormField(label = stringResource(R.string.amount_mk_input_label)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = contributionFieldColors(),
                        prefix = { Text("MK ", fontWeight = FontWeight.Bold, color = NavyBlue) },
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        stringResource(R.string.send_button),
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.receive_pin_msg),
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ContributionFormField(label: String, content: @Composable () -> Unit) {
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
private fun contributionFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = DividerColor,
    focusedBorderColor = NavyBlue,
    unfocusedContainerColor = White,
    focusedContainerColor = White
)
