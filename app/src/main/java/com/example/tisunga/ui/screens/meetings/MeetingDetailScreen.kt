package com.example.tisunga.ui.screens.meetings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
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
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    
    var isEditingNotes by remember { mutableStateOf(false) }
    var discussionText by remember(meetingId) { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    
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
    val isChair = groupRole == "CHAIR" || groupRole == "CHAIRPERSON" || groupRole == "SECRETARY" || groupRole == "ADMIN"
    val isTreasurer = groupRole == "TREASURER"
    val hasAdminPrivileges = isChair || isTreasurer

    LaunchedEffect(groupId, meetingId) {
        if (meeting == null || meeting.id != meetingId) {
            viewModel.getMeeting(groupId, meetingId)
        }
    }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
        if (uiState.isSuccess && uiState.successMessage.isNotEmpty()) {
            android.widget.Toast.makeText(context, uiState.successMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
        if (uiState.errorMessage.isNotEmpty()) {
            android.widget.Toast.makeText(context, uiState.errorMessage, android.widget.Toast.LENGTH_LONG).show()
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
            val attendance = meeting.attendance
            val totalCount = attendance.size
            val presentCount = attendance.count { it.status.uppercase() == "PRESENT" }
            val lateCount = attendance.count { it.status.uppercase() == "LATE" }
            val absentCount = attendance.count { it.status.uppercase() == "ABSENT" }
            val excusedCount = attendance.count { it.status.uppercase() == "EXCUSED" }
            
            val presentPercent = if (totalCount > 0) ((presentCount + lateCount) * 100) / totalCount else 0

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
                        isEditing = isEditingNotes,
                        onEditingChange = { isEditingNotes = it },
                        isEditable = hasAdminPrivileges && (statusUpper == "SCHEDULED" || statusUpper == "ONGOING"),
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
                        onImageClick = { showFullScreenImage = true },
                        imageUri = selectedImageUri,
                        imageUrl = meeting.imageUrl ?: meeting.image,
                        originalNotes = meeting.notes ?: "",
                        hasAdminPrivileges = hasAdminPrivileges
                    )
                }

                item {
                    MeetingAttendanceSummaryCard(
                        present = presentCount,
                        late = lateCount,
                        absent = absentCount,
                        excused = excusedCount,
                        total = totalCount
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAttendanceExpanded = !isAttendanceExpanded }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.attendance_list_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isAttendanceExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isAttendanceExpanded) "Collapse" else "Expand",
                            tint = GreenAccent
                        )
                    }
                }

                if (isAttendanceExpanded) {
                    items(meeting.attendance, key = { it.userId }) { attendance ->
                        AttendanceMemberItem(attendance)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else {
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
                        CircularProgressIndicator(color = GreenAccent)
                        var showNotFound by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(3000)
                            showNotFound = true
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

    val displayImageUrl = meeting?.imageUrl ?: meeting?.image
    if (showFullScreenImage && !displayImageUrl.isNullOrBlank()) {
        val fullImageUrl = if (displayImageUrl.startsWith("http")) displayImageUrl 
                         else "${com.example.tisunga.utils.Constants.BASE_URL.removeSuffix("api/v1/")}uploads/meetings/$displayImageUrl"
        
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = "Full Screen Meeting Minutes",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = { showFullScreenImage = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
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
fun MeetingNotesCard(notes: String) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = notes, fontSize = 14.sp, color = TextPrimary)
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
    onImageClick: () -> Unit = {},
    imageUri: Uri? = null,
    imageUrl: String? = null,
    originalNotes: String = "",
    hasAdminPrivileges: Boolean = false
) {
    val hasChanges = notes != originalNotes
    val focusManager = LocalFocusManager.current

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
                            Box {
                                if (notes.isEmpty()) {
                                    Text("Type meeting discussion/notes here...", color = TextSecondary, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (hasChanges) {
                                onSave()
                                onEditingChange(false)
                            }
                            focusManager.clearFocus()
                        })
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

            if (isEditable && imageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
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

            if (!imageUrl.isNullOrBlank()) {
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
                        .border(1.dp, BackgroundGray, RoundedCornerShape(8.dp))
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Fit
                )
                
                if (hasAdminPrivileges && isEditable) {
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
fun MeetingAttendanceSummaryCard(present: Int, late: Int, absent: Int, excused: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.attendance_overview_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                "LATE" -> OrangeTag to stringResource(R.string.status_late)
                "ABSENT" -> RedAccent to stringResource(R.string.status_absent)
                "EXCUSED" -> Color.Gray to stringResource(R.string.status_excused)
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
