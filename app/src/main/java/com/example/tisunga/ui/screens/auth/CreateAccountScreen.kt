package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.AuthViewModel

@Composable
fun CreateAccountScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var phone by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("verification/REGISTRATION")
            viewModel.resetState()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(text = stringResource(R.string.create_account_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Text(text = stringResource(R.string.create_account_subtitle), fontSize = 14.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.phone_number_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { input -> 
                            if (input.all { it.isDigit() } && input.length <= 10) {
                                phone = input
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text(stringResource(R.string.phone_number_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = stringResource(R.string.first_name_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text(stringResource(R.string.first_name_label)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = stringResource(R.string.last_name_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text(stringResource(R.string.last_name_label)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        ),
                        singleLine = true
                    )
                    
                    if (showError) {
                        Text(
                            text = stringResource(R.string.error_fill_all_fields),
                            color = RedAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            focusManager.clearFocus()
                            if (phone.length == 10 && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                                viewModel.register(firstName, middleName, lastName, phone)
                            } else {
                                showError = true
                            }
                        },
                        enabled = phone.length == 10 && firstName.isNotEmpty() && lastName.isNotEmpty() && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyBlue,
                            disabledContainerColor = NavyBlue.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(stringResource(R.string.continue_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.errorMessage.isNotEmpty()) {
                        Text(
                            text = uiState.errorMessage,
                            color = RedAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.already_have_account_text), fontSize = 14.sp, color = TextSecondary)
                        TextButton(onClick = { navController.navigate(Routes.SIGN_IN) }) {
                            Text(stringResource(R.string.signin_link), color = NavyBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = stringResource(R.string.terms_and_privacy),
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
