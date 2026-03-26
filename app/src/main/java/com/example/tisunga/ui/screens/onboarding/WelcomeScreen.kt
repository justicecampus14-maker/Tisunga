package com.example.tisunga.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WELCOME TO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp,
            color = TextPrimary
        )
        Text(
            text = "TISUNGA",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "A smarter and safer way for communities to save, grow, and manage money together. Contribute securely, track your savings in real-time, apply for loans, and build financial strength as a group — all in one simple app.",
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            color = TextPrimary
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Button(
                onClick = { navController.navigate(Routes.CREATE_ACCOUNT) },
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("GET STARTED", color = White, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { navController.navigate(Routes.CREATE_ACCOUNT) },
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00))
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = White)
            }
        }
    }
}
