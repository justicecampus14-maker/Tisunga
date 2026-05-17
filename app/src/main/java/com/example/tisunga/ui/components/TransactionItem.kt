package com.example.tisunga.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    val isCredit = transaction.type in listOf(
        com.example.tisunga.data.model.TransactionType.SAVINGS,
        com.example.tisunga.data.model.TransactionType.LOAN_IN,
        com.example.tisunga.data.model.TransactionType.SOCIAL_FUND,
        com.example.tisunga.data.model.TransactionType.SHARE_PURCHASE,
        com.example.tisunga.data.model.TransactionType.JOIN_FEE,
        com.example.tisunga.data.model.TransactionType.INTEREST,
        com.example.tisunga.data.model.TransactionType.SYSTEM
    )
    val color = if (isCredit) com.example.tisunga.ui.theme.GreenAccent else Color(0xFF333333)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(transaction.memberName ?: "Member", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                val amountText = com.example.tisunga.utils.FormatUtils.formatMoney(kotlin.math.abs(transaction.amount))
                Text(
                    text = (if (isCredit) "+" else "") + amountText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            val dateStr = try { transaction.createdAt.take(16).replace("T", " ") } catch (e: Exception) { "" }
            val baseDesc = if (!transaction.description.isNullOrBlank()) transaction.description else transaction.type?.name?.replace("_", " ") ?: ""
            Text(
                text = "$baseDesc ($dateStr)",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
