package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.AuthViewModel

@Composable
fun CreatePasswordScreen(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.WELCOME) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_desc))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = stringResource(R.string.create_password_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(text = stringResource(R.string.enter_password_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = DividerColor,
                focusedBorderColor = NavyBlue,
                unfocusedContainerColor = BackgroundGray,
                focusedContainerColor = BackgroundGray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = stringResource(R.string.reenter_password_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
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
        
        if (error.isNotEmpty()) {
            Text(text = error, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { 
                val hasUpperCase = password.any { it.isUpperCase() }
                val hasSymbol = password.any { !it.isLetterOrDigit() }

                if (password != confirmPassword) {
                    error = context.getString(R.string.error_passwords_dont_match)
                } else if (password.length < 8 || password.length > 64) {
                    error = context.getString(R.string.error_password_length)
                } else if (!hasUpperCase) {
                    error = context.getString(R.string.error_password_uppercase)
                } else if (!hasSymbol) {
                    error = context.getString(R.string.error_password_symbol)
                } else {
                    error = ""
                    viewModel.createPassword(uiState.userPhone, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.create_password_title), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
