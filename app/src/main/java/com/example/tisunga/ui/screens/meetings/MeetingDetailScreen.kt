package com.example.tisunga.ui.screens.meetings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tisunga.R
import com.example.tisunga.ViewModelFactory
import com.example.tisunga.data.model.MeetingAttendance
import com.example.tisunga.ui.components.StatusBadge
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils.formatDate
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.viewmodel.MeetingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    navController: NavController,
    groupId: String,
    meetingId: String,
    viewModel: MeetingViewModel = viewModel(
        factory = ViewModelFactory(
            SessionManager(LocalContext.current),
            LocalSavedStateRegistryOwner.current
        )
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val meeting = uiState.selectedMeeting
    val sessionManager = remember { SessionManager(context) }
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isAttendanceExpanded by remember { mutableStateOf(false) }
    
    var discussionText by remember(meetingId, meeting?.notes) { mutableStateOf(meeting?.notes ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val groupRole = sessionManager.getGroupRole(groupId)?.uppercase() ?: "MEMBER"
    val isChair = groupRole == "CHAIR" || groupRole == "CHAIRPERSON" || groupRole == "SECRETARY" || groupRole == "ADMIN"

    LaunchedEffect(groupId, meetingId) {
        viewModel.getMeeting(groupId, meetingId)
    }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
        if (uiState.isSuccess && uiState.successMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
        if (uiState.errorMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.resetState()
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
                        navController.navigate("attendance/$groupId/$meetingId") 
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
                    viewModel.completeMeeting(groupId, meetingId, discussionText, selectedImageUri, context)
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
                    MeetingHeaderCard(
                        title = meeting.title,
                        status = meeting.status,
                        scheduledAt = meeting.scheduledAt,
                        location = meeting.location,
                        agenda = meeting.agenda ?: ""
                    )
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
                    MeetingDiscussionCard(
                        notes = discussionText,
                        onNotesChange = { discussionText = it },
                        isEditable = isChair && (statusUpper == "SCHEDULED" || statusUpper == "ONGOING"),
                        onSave = { 
                            viewModel.updateMeetingNotes(groupId, meetingId, discussionText) 
                        },
                        onImagePick = { imagePickerLauncher.launch("image/*") },
                        onImageRemove = { selectedImageUri = null },
                        imageUri = selectedImageUri,
                        imageUrl = meeting.imageUrl
                    )
                }

                val isAttendanceTaken = meeting.totalCount > 0 && 
                    meeting.status.uppercase().trim() != "SCHEDULED" &&
                    meeting.attendance.any { it.status.isNotBlank() && it.status.uppercase().trim() != "PENDING" }

                item {
                    MeetingAttendanceSummaryCard(
                        present = meeting.presentCount,
                        total = meeting.totalCount,
                        percent = meeting.attendancePercent,
                        isExpanded = isAttendanceExpanded,
                        isAttendanceTaken = isAttendanceTaken,
                        hasMembers = meeting.attendance.isNotEmpty(),
                        onExpandClick = { isAttendanceExpanded = !isAttendanceExpanded }
                    )
                }

                if (isAttendanceExpanded && meeting.attendance.isNotEmpty()) {
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
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun MeetingHeaderCard(
    title: String,
    status: String,
    scheduledAt: String,
    location: String?,
    agenda: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                StatusBadge(status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp), tint = GreenAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.agenda_label), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GreenAccent)
            }
            
            Text(
                text = agenda.ifBlank { "No agenda specified." },
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 14.sp
            )

            if (!location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(location, color = TextSecondary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(formatDate(scheduledAt), color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MeetingActionsCard(
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Complete Meeting")
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun MeetingDiscussionCard(
    notes: String,
    onNotesChange: (String) -> Unit,
    isEditable: Boolean,
    onSave: () -> Unit,
    onImagePick: () -> Unit,
    onImageRemove: () -> Unit,
    imageUri: Uri? = null,
    imageUrl: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.meeting_notes_label),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isEditable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 14.sp),
                        decorationBox = { innerTextField ->
                            if (notes.isEmpty()) {
                                Text("Type meeting discussion/notes here...", color = TextSecondary, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onImagePick) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Add image", tint = GreenAccent)
                        }
                        if (notes.isNotBlank()) {
                            Button(
                                onClick = onSave,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Save Notes", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else if (notes.isNotBlank()) {
                Text(notes, color = TextPrimary, fontSize = 14.sp)
            } else {
                Text("No notes available for this meeting.", color = TextSecondary, fontSize = 14.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
            }

            // Image display logic
            if (imageUri != null || !imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = imageUri ?: imageUrl,
                        contentDescription = "Meeting photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (isEditable && imageUri != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clickable { onImageRemove() },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingAttendanceSummaryCard(
    present: Int,
    total: Int,
    percent: Int,
    isExpanded: Boolean,
    isAttendanceTaken: Boolean,
    hasMembers: Boolean,
    onExpandClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasMembers) { onExpandClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.attendance_overview_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (hasMembers) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isAttendanceTaken) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GreenAccent,
                            trackColor = BackgroundGray,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$present out of $total members present", fontSize = 14.sp, color = TextSecondary)
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(50.dp)
                            .border(2.dp, GreenAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$percent%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenAccent)
                    }
                }
            } else {
                Text(
                    text = if (total > 0) "Attendance has not been marked yet." else "No members in this group.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AttendanceMemberItem(attendance: MeetingAttendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BackgroundGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val user = attendance.user
                if (user != null) {
                    if (!user.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.firstName.take(1),
                            color = GreenAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "?",
                        color = GreenAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val fullName = if (attendance.user != null) {
                    "${attendance.user.firstName} ${attendance.user.lastName}"
                } else {
                    "Unknown Member"
                }
                Text(
                    text = fullName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (!attendance.note.isNullOrBlank()) {
                    Text(attendance.note, fontSize = 12.sp, color = TextSecondary)
                }
            }
            
            StatusBadge(attendance.status)
        }
    }
}
