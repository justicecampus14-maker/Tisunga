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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.MeetingAttendance
import com.example.tisunga.ui.components.StatusBadge
import com.example.tisunga.ui.components.TisungaConfirmDialog
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
    val context = LocalContext.current
    val sessionManager = remember { com.example.tisunga.utils.SessionManager(context) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val groupRole = sessionManager.getGroupRole(groupId)?.uppercase() ?: "MEMBER"
    val isChair = groupRole == "CHAIR" || groupRole == "CHAIRPERSON" || groupRole == "SECRETARY" || groupRole == "ADMIN"

    LaunchedEffect(groupId, meetingId, navBackStackEntry) {
        // Refresh data whenever we are on this screen
        if (navBackStackEntry?.destination?.route?.startsWith("meeting_detail") == true) {
            viewModel.getMeeting(groupId, meetingId)
        }
    }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage, navBackStackEntry) {
        val isCurrent = navBackStackEntry?.destination?.route?.startsWith("meeting_detail") == true
        if (isCurrent) {
            if (uiState.isSuccess && uiState.successMessage.isNotEmpty()) {
                Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            if (uiState.errorMessage.isNotEmpty()) {
                Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.meeting_details_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            val status = meeting?.status?.uppercase() ?: ""
            if (isChair && status != "COMPLETED" && status != "CANCELLED") {
                ExtendedFloatingActionButton(
                    onClick = { 
                        viewModel.resetState()
                        navController.navigate("attendance/$groupId/${meeting?.id}") 
                    },
                    containerColor = GreenAccent,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    text = { Text(stringResource(R.string.mark_attendance_button)) }
                )
            }
        }
    ) { padding ->
        if (showCompleteDialog) {
            TisungaConfirmDialog(
                title = stringResource(R.string.complete_meeting_title),
                message = stringResource(R.string.complete_meeting_msg),
                onConfirm = {
                    showCompleteDialog = false
                    viewModel.updateStatus(groupId, meetingId, "COMPLETED")
                },
                onDismiss = { showCompleteDialog = false }
            )
        }
        if (showCancelDialog) {
            TisungaConfirmDialog(
                title = stringResource(R.string.cancel_meeting_title),
                message = stringResource(R.string.cancel_meeting_msg),
                confirmText = stringResource(R.string.cancel_meeting_title),
                isDestructive = true,
                onConfirm = {
                    showCancelDialog = false
                    viewModel.updateStatus(groupId, meetingId, "CANCELLED")
                },
                onDismiss = { showCancelDialog = false }
            )
        }

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

                if (!meeting.agenda.isNullOrBlank() || isChair) {
                    item {
                        MeetingAgendaCard(
                            agenda = meeting.agenda ?: "",
                            isEditable = isChair && (meeting.status.uppercase() == "SCHEDULED" || meeting.status.uppercase() == "ONGOING"),
                            onSave = { newAgenda ->
                                viewModel.updateMeetingAgenda(groupId, meetingId, newAgenda)
                            }
                        )
                    }
                }

                if (!meeting.notes.isNullOrBlank()) {
                    item {
                        MeetingNotesCard(meeting.notes)
                    }
                }

                val statusUpper = meeting.status.uppercase()
                if (isChair && (statusUpper == "SCHEDULED" || statusUpper == "ONGOING")) {
                    item {
                        MeetingActionsCard(
                            onComplete = { showCompleteDialog = true },
                            onCancel = { showCancelDialog = true }
                        )
                    }
                }

                item {
                    MeetingAttendanceSummaryCard(meeting.presentCount, meeting.totalCount, meeting.attendancePercent)
                }

                item {
                    Text(
                        stringResource(R.string.attendance_list_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(meeting.attendance, key = { it.userId }) { attendance ->
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
fun MeetingAgendaCard(
    agenda: String,
    isEditable: Boolean = false,
    onSave: (String) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedAgenda by remember { mutableStateOf(agenda) }

    LaunchedEffect(agenda) {
        editedAgenda = agenda
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp), tint = GreenAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.agenda_label), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (isEditable) {
                    IconButton(onClick = {
                        if (isEditing) {
                            onSave(editedAgenda)
                        }
                        isEditing = !isEditing
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit",
                            tint = GreenAccent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = editedAgenda,
                    onValueChange = { editedAgenda = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        cursorColor = GreenAccent
                    )
                )
            } else {
                Text(agenda.ifBlank { "No agenda specified." }, color = TextPrimary)
            }
        }
    }
}

@Composable
fun MeetingNotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp), tint = OrangeTag)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.meeting_notes_label), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(notes, color = TextPrimary)
        }
    }
}

@Composable
fun MeetingActionsCard(onComplete: () -> Unit, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.admin_actions_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.complete_button), color = Color.White)
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cancel_button), color = Color.White)
                }
            }
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
            Text(stringResource(R.string.attendance_overview_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.present_count_label, present, total), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                    Text(stringResource(R.string.attendance_rate_label), fontSize = 12.sp, color = TextSecondary)
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
    val status = attendance.status.uppercase()
    
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
            Column(modifier = Modifier.weight(1f)) {
                Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                Text(user.phone, fontSize = 12.sp, color = TextSecondary)
            }
            
            val (statusColor, statusLabel) = when (status) {
                "PRESENT" -> GreenAccent to stringResource(R.string.status_present)
                "ABSENT" -> RedAccent to stringResource(R.string.status_absent)
                "EXCUSED" -> OrangeTag to stringResource(R.string.status_excused)
                "LATE" -> Color(0xFFFFB300) to "LATE"
                else -> TextSecondary to status
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
