package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.AuthViewModel

@Composable
fun SignInScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    val invalidPhoneMsg = stringResource(R.string.error_invalid_phone_format)
    val performLogin = {
        val isValidPrefix = phone.startsWith("09") || phone.startsWith("08")
        if (phone.length == 10 && isValidPrefix) {
            if (password.isNotEmpty()) {
                viewModel.login(phone, password)
            }
        } else {
            phoneError = invalidPhoneMsg
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.signin_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.phone_number_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { input -> 
                                if (input.all { it.isDigit() } && input.length <= 10) {
                                    phone = input
                                    phoneError = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.phone_number_placeholder), color = Color.Gray.copy(alpha = 0.5f)) },
                            leadingIcon = { 
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                                    Text("🇲🇼")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (phoneError.isNotEmpty()) RedAccent else DividerColor,
                                focusedBorderColor = if (phoneError.isNotEmpty()) RedAccent else NavyBlue,
                                unfocusedContainerColor = BackgroundGray,
                                focusedContainerColor = BackgroundGray
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            isError = phoneError.isNotEmpty()
                        )
                        
                        if (phoneError.isNotEmpty()) {
                            Text(
                                text = phoneError,
                                color = RedAccent,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = stringResource(R.string.password_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.password_label), color = Color.Gray.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { performLogin() }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        TextButton(
                            onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = stringResource(R.string.forgot_password_link),
                                color = BlueLink,
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { performLogin() },
                        enabled = phone.length == 10 && password.isNotEmpty() && !uiState.isLoading,
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
                            Text(
                                text = stringResource(R.string.signin_title),
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
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

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_account_text),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        TextButton(onClick = { navController.navigate(Routes.CREATE_ACCOUNT) }) {
                            Text(
                                text = stringResource(R.string.signup_link),
                                color = NavyBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
