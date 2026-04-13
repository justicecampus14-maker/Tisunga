package com.example.tisunga.ui.screens.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.MeetingAttendance
import com.example.tisunga.data.remote.dto.AttendanceEntry
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.MeetingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    groupId: String,
    meetingId: String,
    viewModel: MeetingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val attendanceEntries = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(groupId, meetingId) {
        viewModel.getMeetingAttendance(groupId, meetingId)
    }

    LaunchedEffect(uiState.attendance) {
        uiState.attendance.forEach {
            attendanceEntries[it.userId] = it.status
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mark Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val entries = attendanceEntries.map { (userId, status) ->
                        AttendanceEntry(userId, status)
                    }
                    viewModel.submitBulkAttendance(groupId, meetingId, entries)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Submit Attendance Sheet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            val presentCount = attendanceEntries.values.count { it == "PRESENT" }
            val totalCount = attendanceEntries.size
            
            AttendanceSummaryBanner(presentCount, totalCount)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.attendance) { attendance ->
                    AttendanceMarkRow(
                        attendance = attendance,
                        currentStatus = attendanceEntries[attendance.userId] ?: "ABSENT",
                        onStatusChange = { newStatus ->
                            attendanceEntries[attendance.userId] = newStatus
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryBanner(present: Int, total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Live Tally: $present / $total marked present",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GreenAccent
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) present.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = GreenAccent,
                trackColor = BackgroundGray,
            )
        }
    }
}

@Composable
fun AttendanceMarkRow(
    attendance: MeetingAttendance,
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    val user = attendance.user ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                    Text(user.phone, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("PRESENT", "ABSENT", "EXCUSED").forEach { status ->
                    val isSelected = currentStatus == status
                    val color = when (status) {
                        "PRESENT" -> GreenAccent
                        "ABSENT" -> RedAccent
                        "EXCUSED" -> OrangeTag
                        else -> TextSecondary
                    }
                    
                    OutlinedButton(
                        onClick = { onStatusChange(status) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = if (isSelected) {
                            ButtonDefaults.outlinedButtonColors(containerColor = color.copy(alpha = 0.1f))
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, color)
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        }
                    ) {
                        Text(
                            status.take(1) + status.substring(1).lowercase(),
                            color = if (isSelected) color else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
