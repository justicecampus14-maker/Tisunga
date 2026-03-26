package com.example.tisunga.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils

@Composable
fun SavingsCard(
    totalSavings: Double,
    lastSavedDate: String,
    groupCount: Int,
    withdrawDate: String? = null,
    onSaveNowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Savings:", fontSize = 14.sp)
            Text(FormatUtils.formatMoney(totalSavings), fontSize = 26.sp, color = TextSecondary, fontWeight = Bold)
            
            if (withdrawDate != null) {
                Text("Withdraw date: $withdrawDate", fontWeight = Bold, fontSize = 13.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text("Last saved: $lastSavedDate", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$groupCount saving groups", fontSize = 11.sp, color = TextSecondary)
                }
                Text(
                    "Save Now",
                    color = BlueLink,
                    fontWeight = Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onSaveNowClick() }
                )
            }
        }
    }
}
