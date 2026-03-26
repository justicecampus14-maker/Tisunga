package com.example.tisunga.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.ui.theme.TextSecondary
import com.example.tisunga.ui.theme.White

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Member Name", fontWeight = FontWeight.Bold, fontSize = 14.sp) // In real app, transaction should have member name
            Text("Trans ID: ${transaction.transId} - ${transaction.type}", fontSize = 13.sp, color = TextSecondary)
            Text(transaction.timestamp, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
