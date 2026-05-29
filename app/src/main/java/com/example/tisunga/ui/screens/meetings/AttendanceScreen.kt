package com.example.tisunga.ui.screens.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    var isListExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(groupId, meetingId) {
        viewModel.resetState()
        viewModel.getMeetingAttendance(groupId, meetingId)
    }

    LaunchedEffect(uiState.attendance) {
        if (uiState.attendance.isNotEmpty()) {
            uiState.attendance.forEach {
                val current = attendanceEntries[it.userId]
                if (current == null || current == "PENDING") {
                    val backendStatus = it.status.uppercase()
                    attendanceEntries[it.userId] = backendStatus.ifBlank { "PENDING" }
                }
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmTitle by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }

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

            val presentCount = attendanceEntries.values.count { it == "PRESENT" }
            val lateCount = attendanceEntries.values.count { it == "LATE" }
            val absentCount = attendanceEntries.values.count { it == "ABSENT" }
            val excusedCount = attendanceEntries.values.count { it == "EXCUSED" }
            val pendingCount = uiState.attendance.size - (presentCount + lateCount + absentCount + excusedCount)
            val totalCount = uiState.attendance.size
            
            AttendanceOverviewCard(
                present = presentCount,
                late = lateCount,
                absent = absentCount,
                excused = excusedCount,
                total = totalCount
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        confirmTitle = navController.context.getString(R.string.mark_all_present)
                        confirmMessage = "Are you sure you want to mark all ${uiState.attendance.size} members as PRESENT?"
                        pendingAction = {
                            uiState.attendance.forEach {
                                attendanceEntries[it.userId] = "PRESENT"
                            }
                        }
                        showConfirmDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent)
                ) {
                    Text(stringResource(R.string.mark_all_present))
                }
                
                if (pendingCount > 0) {
                    OutlinedButton(
                        onClick = {
                            confirmTitle = navController.context.getString(R.string.mark_pending_absent)
                            confirmMessage = "Are you sure you want to mark all $pendingCount pending members as ABSENT?"
                            pendingAction = {
                                uiState.attendance.forEach {
                                    if (attendanceEntries[it.userId] == "PENDING" || attendanceEntries[it.userId] == null) {
                                        attendanceEntries[it.userId] = "ABSENT"
                                    }
                                }
                            }
                            showConfirmDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent)
                    ) {
                        Text(stringResource(R.string.mark_pending_absent))
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { isListExpanded = !isListExpanded },
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.attendance_list_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = if (isListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isListExpanded) "Collapse" else "Expand",
                        tint = GreenAccent
                    )
                }
            }

            if (isListExpanded) {
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

    if (showConfirmDialog) {
        com.example.tisunga.ui.components.TisungaConfirmDialog(
            title = confirmTitle,
            message = confirmMessage,
            onConfirm = {
                pendingAction?.invoke()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun AttendanceOverviewCard(present: Int, late: Int, absent: Int, excused: Int, total: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.attendance_overview_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AttendanceLegendItem(GreenAccent, stringResource(R.string.status_present), present)
                    AttendanceLegendItem(OrangeTag, stringResource(R.string.status_late), late)
                    AttendanceLegendItem(RedAccent, stringResource(R.string.status_absent), absent)
                    AttendanceLegendItem(Color.Gray, stringResource(R.string.status_excused), excused)
                }
                
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    if (total > 0) {
                        AttendancePieChart(
                            present = present,
                            late = late,
                            absent = absent,
                            excused = excused,
                            total = total,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier.size(80.dp),
                            color = BackgroundGray,
                            trackColor = BackgroundGray,
                            strokeWidth = 8.dp
                        )
                    }
                }
            }
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
