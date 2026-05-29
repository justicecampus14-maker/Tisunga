package com.example.tisunga.ui.screens.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tisunga.ui.theme.*

@Composable
fun AttendanceLegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: $count", fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
fun AttendancePieChart(
    present: Int,
    late: Int,
    absent: Int,
    excused: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        // Draw background to ensure it's a "filled" circle even if statuses don't sum to total
        drawCircle(color = Color.LightGray.copy(alpha = 0.3f))

        val presentAngle = (present.toFloat() / total) * 360f
        val lateAngle = (late.toFloat() / total) * 360f
        val absentAngle = (absent.toFloat() / total) * 360f
        val excusedAngle = (excused.toFloat() / total) * 360f

        var startAngle = -90f
        
        drawArc(
            color = GreenAccent,
            startAngle = startAngle,
            sweepAngle = presentAngle,
            useCenter = true
        )
        startAngle += presentAngle

        drawArc(
            color = OrangeTag,
            startAngle = startAngle,
            sweepAngle = lateAngle,
            useCenter = true
        )
        startAngle += lateAngle

        drawArc(
            color = RedAccent,
            startAngle = startAngle,
            sweepAngle = absentAngle,
            useCenter = true
        )
        startAngle += absentAngle

        drawArc(
            color = Color.Gray,
            startAngle = startAngle,
            sweepAngle = excusedAngle,
            useCenter = true
        )
    }
}
