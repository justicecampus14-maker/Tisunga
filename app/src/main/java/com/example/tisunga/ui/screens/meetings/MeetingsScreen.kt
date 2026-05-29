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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
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
    
    val pullToRefreshState = rememberPullToRefreshState()
    
    val groupRole = sessionManager.getGroupRole(groupId)
    val isChair = groupRole == "CHAIR" || groupRole == "SECRETARY"
    
    val refreshData = {
        viewModel.getGroupMeetings(groupId)
    }

    LaunchedEffect(groupId) {
        refreshData()
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            refreshData()
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    if (showCreateDialog) {
        CreateMeetingDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, scheduledAt, location, agenda ->
                viewModel.createMeeting(groupId, title, scheduledAt, location, agenda)
                showCreateDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.meetings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.schedule_meeting_desc))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            if (uiState.isLoading && !pullToRefreshState.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (uiState.meetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.no_meetings_msg), color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
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
            
            @Suppress("DEPRECATION")
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(formatDate(meeting.scheduledAt), fontSize = 14.sp, color = TextSecondary)
            }
            
            if (!meeting.location.isNullOrBlank()) {
                @Suppress("DEPRECATION")
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(meeting.location, fontSize = 14.sp, color = TextSecondary)
                }
            }
            
            if (meeting.status == "COMPLETED") {
                @Suppress("DEPRECATION")
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { meeting.attendancePercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = GreenAccent,
                    trackColor = BackgroundGray,
                )
                @Suppress("DEPRECATION")
                Text(
                    text = stringResource(R.string.attendance_percent_label, meeting.attendancePercent),
                    fontSize = 12.sp,
                    color = GreenAccent,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
