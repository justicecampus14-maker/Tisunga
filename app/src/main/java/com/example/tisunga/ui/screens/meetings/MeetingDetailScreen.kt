package com.example.tisunga.ui.screens.meetings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
    var isAttendanceExpanded by remember { mutableStateOf(false) }
    
    var isEditingNotes by remember { mutableStateOf(false) }
    var discussionText by remember(meetingId) { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(meeting?.notes) {
        if (meeting?.notes != null && !isEditingNotes) {
            discussionText = meeting.notes
        }
    }

    LaunchedEffect(meeting?.status) {
        val s = meeting?.status?.uppercase()
        if (s == "COMPLETED" || s == "CANCELLED") {
            isEditingNotes = false
            selectedImageUri = null
        }
    }

    val groupRole = sessionManager.getGroupRole(groupId)?.uppercase() ?: "MEMBER"
    val isChair = groupRole == "CHAIR" || groupRole == "CHAIRPERSON" || groupRole == "SECRETARY" || groupRole == "TREASURER" || groupRole == "ADMIN"

    LaunchedEffect(groupId, meetingId) {
        if (meeting == null || meeting.id != meetingId) {
            viewModel.getMeeting(groupId, meetingId)
        }
    }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
        if (uiState.isSuccess && uiState.successMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
        // Do NOT resetState() here for errorMessage, as we want to show it in the UI with a Retry button
        if (uiState.errorMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
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
                    MeetingHeaderCard(
                        title = meeting.title,
                        status = meeting.status,
                        scheduledAt = meeting.scheduledAt,
                        location = meeting.location,
                        agenda = meeting.agenda ?: "",
                        isEditable = isChair && (meeting.status.uppercase() == "SCHEDULED" || meeting.status.uppercase() == "ONGOING"),
                        onSaveAgenda = { newAgenda ->
                            viewModel.updateMeetingAgenda(groupId, meetingId, newAgenda)
                        }
                    )
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
                    MeetingDiscussionCard(
                        notes = discussionText,
                        onNotesChange = { discussionText = it },
                        isEditing = isEditingNotes,
                        onEditingChange = { isEditingNotes = it },
                        isEditable = isChair && (statusUpper == "SCHEDULED" || statusUpper == "ONGOING"),
                        onSave = { 
                            viewModel.updateMeetingNotes(groupId, meetingId, discussionText) 
                        },
                        onImagePick = { imagePickerLauncher.launch("image/*") },
                        onImageRemove = { selectedImageUri = null },
                        onImageUpload = {
                            selectedImageUri?.let { uri ->
                                viewModel.uploadMeetingImage(groupId, meetingId, uri, context)
                                selectedImageUri = null
                            }
                        },
                        imageUri = selectedImageUri,
                        imageUrl = meeting.imageUrl ?: meeting.image,
                        originalNotes = meeting.notes ?: "",
                        isChair = isChair
                    )
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
        } else {
            // Error or Initial State
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    if (uiState.errorMessage.isNotEmpty()) {
                        Text(
                            text = uiState.errorMessage,
                            color = Color.Red,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.getMeeting(groupId, meetingId) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Text("Retry")
                        }
                    } else {
                        // If no meeting and no error, we are likely still loading or it truly doesn't exist.
                        // Since we cleared state on entry, it's normal for it to be null for a few ms.
                        CircularProgressIndicator(color = GreenAccent)
                        
                        // If after some time it's still null and not loading, it might be a 404
                        var showNotFound by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(3000)
                            if (meeting == null && !uiState.isLoading) {
                                showNotFound = true
                            }
                        }
                        
                        if (showNotFound) {
                            Text(stringResource(R.string.meeting_not_found), color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                            ) {
                                Text(stringResource(R.string.go_back_button))
                            }
                        }
                    }
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
    agenda: String,
    isEditable: Boolean = false,
    onSaveAgenda: (String) -> Unit = {}
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                StatusBadge(status)
            }
            
            // Description (Agenda)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp), tint = GreenAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.agenda_label), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GreenAccent)
                }

                if (isEditable) {
                    IconButton(onClick = {
                        if (isEditing) {
                            onSaveAgenda(editedAgenda)
                        }
                        isEditing = !isEditing
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            if (isEditing) {
                OutlinedTextField(
                    value = editedAgenda,
                    onValueChange = { editedAgenda = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        cursorColor = GreenAccent
                    )
                )
            } else {
                Text(
                    text = agenda.ifBlank { "No agenda specified." },
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 14.sp
                )
            }

            // Location
            if (!location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(location, color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Date
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
fun MeetingDiscussionCard(
    notes: String,
    onNotesChange: (String) -> Unit,
    isEditing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    isEditable: Boolean,
    onSave: () -> Unit,
    onImagePick: () -> Unit,
    onImageRemove: () -> Unit,
    onImageUpload: () -> Unit,
    imageUri: Uri? = null,
    imageUrl: String? = null,
    originalNotes: String = "",
    isChair: Boolean = false
) {
    val hasChanges = notes != originalNotes

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
                Text(
                    text = stringResource(R.string.meeting_notes_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (isEditable && !isEditing) {
                    TextButton(onClick = { onEditingChange(true) }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isEditing) {
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
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onImagePick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Image",
                                tint = GreenAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { 
                                onEditingChange(false) 
                                onNotesChange(originalNotes)
                            }) {
                                Text("Cancel", color = Color.Gray)
                            }
                            if (hasChanges) {
                                Button(
                                    onClick = { 
                                        onSave()
                                        onEditingChange(false)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Submit", color = Color.White)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = notes.ifBlank { "No notes recorded." },
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }

            if (isEditable) {
                if (imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, BackgroundGray, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Meeting Minutes Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .clickable { onImageRemove() },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }

                        Button(
                            onClick = onImageUpload,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Image")
                        }
                    }
                }
            }

                if (isChair && !imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Uploaded Minutes:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl 
                                     else "${com.example.tisunga.utils.Constants.BASE_URL.removeSuffix("api/v1/")}uploads/meetings/$imageUrl"

                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Meeting Minutes",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, BackgroundGray, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    
                    if (isEditable) {
                        TextButton(
                            onClick = onImagePick,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Replace Image", fontSize = 12.sp, color = GreenAccent)
                        }
                    }
                }
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
