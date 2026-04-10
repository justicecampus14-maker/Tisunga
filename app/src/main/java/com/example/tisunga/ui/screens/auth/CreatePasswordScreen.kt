package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    var validationError by remember { mutableStateOf("") }

    // Live validation states
    val isLengthValid = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = stringResource(R.string.enter_password_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it 
                validationError = ""
            },
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
                unfocusedContainerColor = White,
                focusedContainerColor = White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Password Requirements Checklist
        Column(modifier = Modifier.fillMaxWidth()) {
            RequirementItem(text = "At least 8 characters", isMet = isLengthValid)
            RequirementItem(text = "At least one uppercase letter", isMet = hasUpperCase)
            RequirementItem(text = "At least one number", isMet = hasNumber)
            RequirementItem(text = "At least one symbol", isMet = hasSymbol)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = stringResource(R.string.reenter_password_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                validationError = ""
            },
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
                unfocusedContainerColor = White,
                focusedContainerColor = White
            ),
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        if (confirmPassword.isNotEmpty() && !passwordsMatch) {
            Text(
                text = stringResource(R.string.error_passwords_dont_match),
                color = RedAccent,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        val displayError = if (validationError.isNotEmpty()) validationError else uiState.errorMessage
        if (displayError.isNotEmpty()) {
            Text(text = displayError, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val isAllValid = isLengthValid && hasUpperCase && hasNumber && hasSymbol && passwordsMatch
        
        Button(
            onClick = { 
                if (!isAllValid) {
                    if (!isLengthValid) validationError = "Password must be at least 8 characters"
                    else if (!hasUpperCase) validationError = "Password must contain an uppercase letter"
                    else if (!hasNumber) validationError = "Password must contain a number"
                    else if (!hasSymbol) validationError = "Password must contain a symbol"
                    else if (!passwordsMatch) validationError = "Passwords do not match"
                } else {
                    validationError = ""
                    viewModel.createPassword(uiState.userPhone, password)
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,
                disabledContainerColor = NavyBlue.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.create_password_title), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) GreenAccent else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) GreenAccent else TextSecondary
        )
    }
}
