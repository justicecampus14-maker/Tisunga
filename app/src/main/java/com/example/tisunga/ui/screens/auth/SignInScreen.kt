package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun SignInScreen(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = stringResource(R.string.signin_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Text(text = stringResource(R.string.signin_subtitle), fontSize = 14.sp, color = TextSecondary)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.phone_number_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() } && input.length <= 10) {
                            phone = input
                            validationError = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text(stringResource(R.string.phone_number_placeholder)) },
                    leadingIcon = { 
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                            Text("🇲🇼")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    },
                    isError = validationError.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                if (validationError.isNotEmpty()) {
                    Text(
                        text = validationError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = stringResource(R.string.password_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text(stringResource(R.string.password_label)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                
                TextButton(
                    onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.forgot_password_link), color = BlueLink, fontSize = 13.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        val hasUpperCase = password.any { it.isUpperCase() }
                        val hasNumber = password.any { it.isDigit() }
                        val hasSymbol = password.any { !it.isLetterOrDigit() }

                        if (phone.length !in 9..10) {
                            validationError = "Phone number must be 9 or 10 digits"
                        } else if (password.isEmpty()) {
                            validationError = "Password is required"
                        } else if (password.length < 8) {
                            validationError = context.getString(R.string.error_password_length)
                        } else if (!hasUpperCase) {
                            validationError = context.getString(R.string.error_password_uppercase)
                        } else if (!hasNumber) {
                            validationError = context.getString(R.string.error_password_number)
                        } else if (!hasSymbol) {
                            validationError = context.getString(R.string.error_password_symbol)
                        } else {
                            validationError = ""
                            viewModel.login(phone, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.signin_title), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                val errorToShow = if (uiState.errorMessage.isNotEmpty()) uiState.errorMessage else ""
                if (errorToShow.isNotEmpty()) {
                    Text(
                        text = errorToShow,
                        color = RedAccent,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.no_account_text), fontSize = 14.sp, color = TextSecondary)
            TextButton(onClick = { navController.navigate(Routes.CREATE_ACCOUNT) }) {
                Text(stringResource(R.string.signup_link), color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
