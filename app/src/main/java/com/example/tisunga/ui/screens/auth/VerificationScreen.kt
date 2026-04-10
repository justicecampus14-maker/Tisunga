package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun VerificationScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var otpCode by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var isPlaced by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.CREATE_PASSWORD)
            viewModel.resetState()
        }
    }

    LaunchedEffect(isPlaced) {
        if (isPlaced) {
            delay(1000) // Longer delay to avoid "BringIntoViewRequester" crash during transition
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hidden TextField to capture input, placed safely
        BasicTextField(
            value = otpCode,
            onValueChange = { input -> 
                if (input.length <= 6 && input.all { it.isDigit() }) {
                    otpCode = input
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .focusRequester(focusRequester)
                .onGloballyPositioned { isPlaced = true }
                .alpha(0f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(16.dp)
                .clickable { focusRequester.requestFocus() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = stringResource(R.string.verification_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = stringResource(R.string.verification_subtitle), fontSize = 14.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(6) { index ->
                    val digit = otpCode.getOrNull(index)?.toString() ?: ""
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { focusRequester.requestFocus() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (digit.isNotEmpty()) {
                                Text(
                                    text = digit,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            } else {
                                Box(modifier = Modifier.size(8.dp, 2.dp).background(DividerColor))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { 
                    if (otpCode.length == 6) {
                        viewModel.verifyOtp(uiState.userPhone, otpCode) 
                    }
                },
                enabled = otpCode.length == 6 && !uiState.isLoading,
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
                    Text(stringResource(R.string.verify_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            TextButton(
                onClick = { viewModel.sendOtp(uiState.userPhone) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.resend_code_link), color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = RedAccent,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
