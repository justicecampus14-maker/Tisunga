package com.example.tisunga.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils

@Composable
fun LoanCard(
    groupName: String,
    approvedBy: String,
    totalBorrowed: Double,
    interestRate: Double,
    repayableAmount: Double,
    remaining: Double,
    percentRepaid: Float,
    dueDate: String,
    status: String,
    showApproveReject: Boolean = false,
    onRepayClick: () -> Unit = {},
    onApproveClick: () -> Unit = {},
    onRejectClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(groupName, fontWeight = Bold, fontSize = 18.sp)
                Text(status, color = Color(0xFFE57373), fontSize = 13.sp)
            }
            Text(approvedBy, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Total borrowed: MK ${FormatUtils.formatNumber(totalBorrowed)}",
                fontWeight = SemiBold,
                fontSize = 14.sp
            )
            Text(
                "Interest rate : ${interestRate}%",
                fontWeight = SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text("Repayable amount", fontWeight = SemiBold, fontSize = 14.sp)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MK ${FormatUtils.formatNumber(repayableAmount)}",
                    fontWeight = Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Remaining: MK ${FormatUtils.formatNumber(remaining)}",
                    fontWeight = SemiBold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentRepaid },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PurpleProgress,
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${(percentRepaid * 100).toInt()}% repaid",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    "Due $dueDate",
                    color = RedAccent,
                    fontWeight = SemiBold,
                    fontSize = 13.sp
                )
            }
            if (showApproveReject) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Approve",
                        fontWeight = Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { onApproveClick() }
                    )
                    Text(
                        "Reject",
                        fontWeight = Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { onRejectClick() }
                    )
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Repay Now →",
                        fontWeight = Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onRepayClick() }
                    )
                }
            }
        }
    }
}
