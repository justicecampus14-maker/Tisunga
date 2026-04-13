package com.example.tisunga.ui.screens.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
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
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.ui.components.StatusBadge
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils.formatDate
import com.example.tisunga.viewmodel.MeetingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(navController: NavController, groupId: String, viewModel: MeetingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.tisunga.utils.SessionManager(context) }
    
    val groupRole = sessionManager.getGroupRole(groupId)
    val isChair = groupRole == "CHAIR" || groupRole == "SECRETARY"
    
    LaunchedEffect(groupId) {
        viewModel.getGroupMeetings(groupId)
    }

    if (showCreateDialog) {
        // Dialog implementation could go here, for now just show a simple one or handle it
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Group Meetings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isChair) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = GreenAccent,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Schedule Meeting")
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
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (uiState.meetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No meetings scheduled yet", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.meetings) { meeting ->
                        MeetingItem(meeting) {
                            navController.navigate("meeting_detail/$groupId/${meeting.id}")
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MeetingItem(meeting: Meeting, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meeting.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                StatusBadge(meeting.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(formatDate(meeting.scheduledAt), fontSize = 14.sp, color = TextSecondary)
            }
            
            if (!meeting.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(meeting.location, fontSize = 14.sp, color = TextSecondary)
                }
            }
            
            if (meeting.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { meeting.attendancePercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = GreenAccent,
                    trackColor = BackgroundGray,
                )
                Text(
                    text = "${meeting.attendancePercent}% Attendance",
                    fontSize = 12.sp,
                    color = GreenAccent,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
