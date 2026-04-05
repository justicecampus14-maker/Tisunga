package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun ForgotPasswordScreen(navController: NavController, viewModel: AuthViewModel) {
    var phone by remember { mutableStateOf("") }

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
        
        Text(text = stringResource(R.string.forgot_password_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = stringResource(R.string.enter_phone_subtitle), fontSize = 14.sp, color = TextSecondary)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text(stringResource(R.string.phone_placeholder)) },
            leadingIcon = { 
                Row(modifier = Modifier.padding(start = 8.dp)) {
                    Text("🇲🇼")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Phone, contentDescription = null)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = DividerColor,
                focusedBorderColor = NavyBlue,
                unfocusedContainerColor = White,
                focusedContainerColor = White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                if (phone.isNotEmpty()) {
                    viewModel.sendOtp(phone)
                    navController.navigate(Routes.VERIFICATION)
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text(stringResource(R.string.send_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
