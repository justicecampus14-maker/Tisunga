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
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
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
        viewModel.resetState()
        viewModel.getMeetingAttendance(groupId, meetingId)
    }

    LaunchedEffect(uiState.attendance) {
        if (uiState.attendance.isNotEmpty()) {
            uiState.attendance.forEach {
                if (!attendanceEntries.containsKey(it.userId) || attendanceEntries[it.userId] == "PENDING") {
                    attendanceEntries[it.userId] = it.status.uppercase().ifBlank { "PENDING" }
                }
            }
        }
    }

    // Remove the redundant if(attendanceEntries.isEmpty()...) block below to avoid conflicts


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.attendance_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val entries = uiState.attendance.map { attendance ->
                        AttendanceEntry(
                            id = attendance.id,
                            userId = attendance.userId,
                            status = attendanceEntries[attendance.userId] ?: attendance.status
                        )
                    }
                    viewModel.submitBulkAttendance(groupId, meetingId, entries)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(8.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.submit_attendance_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            if (uiState.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        uiState.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            val presentCount = attendanceEntries.values.count { 
                it.uppercase() == "PRESENT" || it.uppercase() == "LATE" 
            }
            val totalCount = uiState.attendance.size
            
            AttendanceSummaryBanner(presentCount, totalCount)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.attendance) { attendance ->
                    AttendanceMarkRow(
                        attendance = attendance,
                        currentStatus = attendanceEntries[attendance.userId] ?: attendance.status,
                        onStatusChange = { newStatus ->
                            attendanceEntries[attendance.userId] = newStatus.uppercase()
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
                stringResource(R.string.attendance_live_tally, present, total),
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
                listOf("PRESENT", "LATE", "ABSENT", "EXCUSED").forEach { status ->
                    val isSelected = currentStatus.uppercase() == status
                    val color = when (status) {
                        "PRESENT" -> GreenAccent
                        "LATE" -> OrangeTag
                        "ABSENT" -> RedAccent
                        "EXCUSED" -> Color.Gray
                        else -> TextSecondary
                    }
                    
                    val label = when (status) {
                        "PRESENT" -> stringResource(R.string.status_present)
                        "LATE" -> stringResource(R.string.status_late)
                        "ABSENT" -> stringResource(R.string.status_absent)
                        "EXCUSED" -> stringResource(R.string.status_excused)
                        else -> status
                    }

                    Button(
                        onClick = { onStatusChange(status) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = if (isSelected) {
                            ButtonDefaults.buttonColors(containerColor = color)
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = TextSecondary
                            )
                        },
                        border = if (isSelected) {
                            null
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
