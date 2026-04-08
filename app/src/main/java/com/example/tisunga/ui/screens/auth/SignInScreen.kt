package com.example.tisunga.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    val passwordFocusRequester = remember { FocusRequester() }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val handleLogin = {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }

        if (phone.length !in 9..10) {
            validationError = "Phone number must be 9 or 10 digits"
        } else if (password.isEmpty()) {
            validationError = "Password is required"
        } else if (password.length < 8) {
            validationError = "Password must be at least 8 characters"
        } else if (!hasUpperCase || !hasNumber || !hasSymbol) {
            validationError = "Password must contain Uppercase, Number and Symbol"
        } else {
            validationError = ""
            viewModel.login(phone, password)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(White, BackgroundGray)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.signin_title),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = NavyBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.signin_subtitle),
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800, 200)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = "Phone Number",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
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
                            placeholder = { Text("e.g. 0882752624", fontSize = 14.sp) },
                            leadingIcon = { MalawiFlag() },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = DividerColor,
                                focusedBorderColor = NavyBlue,
                                unfocusedContainerColor = BackgroundGray.copy(alpha = 0.3f),
                                focusedContainerColor = White
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
                        )

                        if (validationError.isNotEmpty() && !validationError.contains("Password")) {
                            Text(text = validationError, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Password",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it 
                                if (validationError.contains("Password")) validationError = ""
                            },
                            modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
                            placeholder = { Text("Enter password", fontSize = 14.sp) },
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = NavyBlue, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, modifier = Modifier.size(20.dp))
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = DividerColor,
                                focusedBorderColor = NavyBlue,
                                unfocusedContainerColor = BackgroundGray.copy(alpha = 0.3f),
                                focusedContainerColor = White
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { handleLogin() })
                        )
                        
                        if (validationError.contains("Password")) {
                            Text(text = validationError, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }

                        Text(
                            text = stringResource(R.string.forgot_password_link),
                            color = NavyBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End).padding(vertical = 12.dp).clickable { navController.navigate(Routes.FORGOT_PASSWORD) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = handleLogin,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Sign In", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.no_account_text), fontSize = 14.sp, color = TextSecondary)
                            Text(
                                text = stringResource(R.string.signup_link),
                                color = NavyBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { navController.navigate(Routes.CREATE_ACCOUNT) }
                            )
                        }
                    }
                }
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = RedAccent,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MalawiFlag() {
    Box(
        modifier = Modifier.padding(start = 12.dp, end = 4.dp).size(24.dp, 16.dp).clip(RoundedCornerShape(2.dp))
    ) {
        Column {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFD21034)))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF008751)))
        }
    }
}
