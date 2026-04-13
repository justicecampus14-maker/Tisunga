package com.example.tisunga.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(status: String) {
    val color = when (status.uppercase()) {
        "OPEN", "ACTIVE", "ONGOING", "COMPLETED", "PRESENT" -> Color(0xFF4CAF50)
        "SCHEDULED", "PENDING" -> Color(0xFF2196F3)
        "CANCELLED", "ABSENT", "CLOSED" -> Color(0xFFF44336)
        "REMINDER", "EXCUSED" -> Color(0xFFFFA000)
        else -> Color(0xFF757575)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
