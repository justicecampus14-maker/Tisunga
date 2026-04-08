package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.theme.NavyBlue
import com.example.tisunga.ui.theme.White
import com.example.tisunga.ui.theme.TextPrimary

@Composable
fun GroupSummaryDialog(
    group: Group,
    isFromHome: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isFromHome) "Group Information" else "Group Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    SummaryItem("Group Name", group.name)
                    SummaryItem("Description", group.description)
                    SummaryItem("Location", group.location)
                    SummaryItem("Minimum Contribution", "MK ${group.minContribution.toInt()}")
                    SummaryItem("Saving Period", "${group.savingPeriod} Months")
                    SummaryItem("Max Members", group.maxMembers.toString())
                    SummaryItem("Visibility", group.visibility)
                    SummaryItem("Start Date", group.startDate)
                    SummaryItem("End Date", group.endDate)
                    SummaryItem("Meeting Day", group.meetingDay)
                    SummaryItem("Meeting Time", group.meetingTime)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isFromHome) {
                    Button(
                        onClick = { onDone?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Done", color = White, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = { onEdit?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Edit", color = White, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onConfirm?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Confirm", color = White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "$label: $value",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
