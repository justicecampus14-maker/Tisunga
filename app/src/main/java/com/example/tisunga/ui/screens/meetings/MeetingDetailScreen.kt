package com.example.tisunga.ui.screens.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.tisunga.ui.components.StatusBadge
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils.formatDate
import com.example.tisunga.viewmodel.MeetingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    navController: NavController,
    groupId: String,
    meetingId: String,
    viewModel: MeetingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val meeting = uiState.selectedMeeting
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.tisunga.utils.SessionManager(context) }
    
    val groupRole = sessionManager.getGroupRole(groupId)
    val isChair = groupRole == "CHAIR" || groupRole == "SECRETARY"

    LaunchedEffect(groupId, meetingId) {
        viewModel.getMeeting(groupId, meetingId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meeting Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (isChair && meeting?.status != "COMPLETED" && meeting?.status != "CANCELLED") {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("attendance/$groupId/${meeting?.id}") },
                    containerColor = GreenAccent,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    text = { Text("Mark Attendance") }
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else if (meeting != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundGray),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MeetingHeaderCard(meeting.title, meeting.status, meeting.scheduledAt, meeting.location)
                }

                if (!meeting.agenda.isNullOrBlank()) {
                    item {
                        MeetingAgendaCard(meeting.agenda)
                    }
                }

                item {
                    MeetingAttendanceSummaryCard(meeting.presentCount, meeting.totalCount, meeting.attendancePercent)
                }

                item {
                    Text(
                        "Attendance List",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(meeting.attendance) { attendance ->
                    AttendanceMemberItem(attendance)
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun MeetingHeaderCard(title: String, status: String, scheduledAt: String, location: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                StatusBadge(status)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(formatDate(scheduledAt), color = TextSecondary)
            }
            if (!location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(location, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun MeetingAgendaCard(agenda: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp), tint = GreenAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agenda", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(agenda, color = TextPrimary)
        }
    }
}

@Composable
fun MeetingAttendanceSummaryCard(present: Int, total: Int, percent: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Attendance Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("$present / $total Present", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                    Text("Attendance Rate", fontSize = 12.sp, color = TextSecondary)
                }
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.size(60.dp),
                        color = GreenAccent,
                        trackColor = BackgroundGray,
                        strokeWidth = 6.dp
                    )
                    Text("$percent%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AttendanceMemberItem(attendance: MeetingAttendance) {
    val user = attendance.user ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                Text(user.phone, fontSize = 12.sp, color = TextSecondary)
            }
            
            val statusColor = when (attendance.status) {
                "PRESENT" -> GreenAccent
                "ABSENT" -> RedAccent
                "EXCUSED" -> OrangeTag
                else -> TextSecondary
            }
            
            Text(
                attendance.status,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
