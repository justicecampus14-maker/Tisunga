package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    val passwordFocusRequester = remember { FocusRequester() }

    val handleLogin = {
        if (phone.length !in 9..10) {
            validationError = "Phone number must be 9 or 10 digits"
        } else if (password.isEmpty()) {
            validationError = "Password is required"
        } else {
            validationError = ""
            viewModel.login(phone, password)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Explicitly navigating to HOME, which is the main dashboard screen
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
        Spacer(modifier = Modifier.height(60.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, NavyBlue, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "APP\nLOGO",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tisunge",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(2.dp),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.phone_number_label), fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() } && input.length <= 10) {
                            phone = input
                            validationError = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NavyBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    )
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
                
                Text(text = stringResource(R.string.password_label), fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = NavyBlue) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextSecondary
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
                        onDone = { handleLogin() }
                    )
                )
                
                TextButton(
                    onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(stringResource(R.string.forgot_password_link), color = Color.Black, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = handleLogin,
                    modifier = Modifier.align(Alignment.End).height(44.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.signin_title), color = White, fontSize = 16.sp)
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.no_account_text), fontSize = 15.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.signup_link),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.clickable { navController.navigate(Routes.CREATE_ACCOUNT) }
            )
        }
    }
}
