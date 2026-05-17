package com.example.tisunga.ui.screens.events

import android.widget.Toast
import com.example.tisunga.utils.FormatUtils
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
<<<<<<< HEAD
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowDropDown
=======
import androidx.compose.material.icons.filled.Warning
>>>>>>> 37e8b804868ff74a2f43b72ea3dfbfdb34251134
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.*

import androidx.compose.ui.res.stringResource
import com.example.tisunga.R

import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun ActivitiesScreen(
    navController: NavController,
    groupId: String,
    viewModel: ActivitiesViewModel
) {
    val context = LocalContext.current
    val navBackStackEntryState = navController.currentBackStackEntryAsState()
    val navBackStackEntry = navBackStackEntryState.value

    val selectedTabState = remember { mutableIntStateOf(0) }
    val selectedTab = selectedTabState.intValue
    
    val meetingsState = viewModel.meetings.collectAsState()
    val meetings = meetingsState.value
    
    val eventsState = viewModel.events.collectAsState()
    val events = eventsState.value
    
    val isLoadingState = viewModel.isLoading.collectAsState()
    val isLoading = isLoadingState.value
    
    val errorState = viewModel.error.collectAsState()
    val error = errorState.value

    val meetingFilterState = viewModel.meetingFilter.collectAsState()
    val meetingFilter = meetingFilterState.value
    
    val eventFilterState = viewModel.eventFilter.collectAsState()
    val eventFilter = eventFilterState.value

    var showCreateMeetingDialog by remember { mutableStateOf(false) }
    var showCreateEventDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        viewModel.loadData(groupId)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(NavyBlue)) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.events_title), color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = NavyBlue)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = NavyBlue,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTabState.intValue = 0 },
                        text = { Text(stringResource(R.string.tab_meetings), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTabState.intValue = 1 },
                        text = { Text(stringResource(R.string.tab_other_events), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showCreateMeetingDialog = true
                    else showCreateEventDialog = true
                },
                containerColor = NavyBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavyBlue)
            } else if (error != null) {
                Text(
                    text = error ?: "Unknown Error",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    color = Color.Red
                )
            } else {
                if (selectedTab == 0) {
                    MeetingsContent(
                        meetings = meetings,
                        filter = meetingFilter,
                        onFilterChange = { viewModel.setMeetingFilter(it) },
                        onMeetingClick = { m ->
                            navController.navigate("meeting_detail/${m.id}")
                        }
                    )
                } else {
                    EventsContent(
                        events = events,
                        filter = eventFilter,
                        onFilterChange = { viewModel.setEventFilter(it) }
                    )
                }
            }
        }
    }

    if (showCreateMeetingDialog) {
        CreateMeetingDialog(
            onDismiss = { showCreateMeetingDialog = false },
            onCreate = { title, agenda, scheduledAt, location ->
                viewModel.createMeeting(groupId, title, agenda, scheduledAt, location)
                showCreateMeetingDialog = false
            }
        )
    }

    if (showCreateEventDialog) {
        CreateEventDialog(
            onDismiss = { showCreateEventDialog = false },
            onCreate = { title, desc, type, date ->
                viewModel.createEvent(groupId, title, desc, type, date)
                showCreateEventDialog = false
            }
        )
    }
}

// MEETINGS

@Composable
fun MeetingsContent(
    meetings: List<Meeting>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onMeetingClick: (Meeting) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            listOf("ALL", "UPCOMING", "CLOSED").forEach {
                val labelId = when(it) {
                    "ALL" -> R.string.filter_all_caps
                    "UPCOMING" -> R.string.filter_upcoming
                    "CLOSED" -> R.string.filter_closed
                    else -> R.string.filter_all_caps
                }
                FilterChip(
                    selected = filter == it,
                    onClick = { onFilterChange(it) },
                    label = { Text(stringResource(labelId)) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(meetings) { m ->
                val show = when (filter) {
                    "UPCOMING" -> m.status.uppercase() == "UPCOMING" || m.status.uppercase() == "OPEN"
                    "CLOSED" -> m.status.uppercase() == "CLOSED" || m.status.uppercase() == "COMPLETED"
                    else -> true
                }
                if (show) {
                    MeetingCard(m, onClick = { onMeetingClick(m) })
                }
            }
        }
    }
}

@Composable
fun MeetingCard(m: Meeting, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = m.title,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue,
                        fontSize = 15.sp
                    )
                    
                    if (!m.agenda.isNullOrBlank()) {
                        Text(
                            text = m.agenda,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (!m.location.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = m.location,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    val dateStr = try { m.scheduledAt.take(16).replace("T", " ") } catch (e: Exception) { m.scheduledAt }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = dateStr,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                StatusBadge(m.status)
            }
        }
    }
}

// EVENTS

@Composable
fun EventsContent(
    events: List<Event>,
    filter: String,
    onFilterChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            listOf("ALL", "UPCOMING", "CLOSED").forEach {
                val labelId = when(it) {
                    "ALL" -> R.string.filter_all_caps
                    "UPCOMING" -> R.string.filter_upcoming
                    "CLOSED" -> R.string.filter_closed
                    else -> R.string.filter_all_caps
                }
                FilterChip(
                    selected = filter == it,
                    onClick = { onFilterChange(it) },
                    label = { Text(stringResource(labelId)) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(events) { e ->
                val show = when (filter) {
                    "UPCOMING" -> e.status.uppercase() == "UPCOMING" || e.status.uppercase() == "OPEN"
                    "CLOSED" -> e.status.uppercase() == "CLOSED" || e.status.uppercase() == "COMPLETED"
                    else -> true
                }
                if (show) {
                    EventCard(e)
                }
            }
        }
    }
}

@Composable
fun EventCard(e: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(e.title, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                    // Note: 'type' is not in the Event model, using a placeholder or common field if available
                    Text("EVENT", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
                StatusBadge(e.status)
            }

            if (!e.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(e.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(FormatUtils.formatDate(e.endDate ?: ""), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.uppercase()) {
        "UPCOMING", "OPEN" -> GreenAccent
        "CLOSED", "COMPLETED" -> Color.Gray
        "CANCELLED" -> RedAccent
        else -> NavyBlue
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun CreateMeetingDialog(onDismiss: () -> Unit, onCreate: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var agenda by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isDateValid by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_meeting_title), fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.meeting_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text(stringResource(R.string.agenda_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.location_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    val context = LocalContext.current
                    OutlinedTextField(
                        value = date,
                        onValueChange = { 
                            date = it
                            isDateValid = true 
                        },
                        label = { Text(stringResource(R.string.schedule_datetime_label)) },
                        placeholder = { Text("YYYY-MM-DD HH:MM") },
                        supportingText = { if (!isDateValid) Text(stringResource(R.string.invalid_date_format), color = Color.Red) },
                        trailingIcon = { 
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(context, { _, y, m, d ->
                                    val datePart = String.format("%04d-%02d-%02d", y, m + 1, d)
                                    TimePickerDialog(context, { _, hh, mm ->
                                        date = "$datePart ${String.format("%02d:%02d", hh, mm)}"
                                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }) {
                                Icon(Icons.Default.CalendarMonth, stringResource(R.string.select_date_label))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank()) {
                        onCreate(title, agenda, date.replace(" ", "T"), location)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(stringResource(R.string.create_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), color = Color.Gray)
            }
        }
    )
}

@Composable
fun CreateEventDialog(onDismiss: () -> Unit, onCreate: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("SOCIAL") }
    var eventDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isDateValid by remember { mutableStateOf(true) }

    val types = listOf("SOCIAL", "CONTRIBUTION", "EMERGENCY", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_event_title), fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.event_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box {
                    val displayType = eventType.lowercase().replaceFirstChar { it.uppercase() }
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(displayType, color = NavyBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        types.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    eventType = label
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    val context = LocalContext.current
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { 
                            eventDate = it
                            isDateValid = true 
                        },
                        label = { Text(stringResource(R.string.date_label)) },
                        placeholder = { Text("YYYY-MM-DD") },
                        supportingText = { if (!isDateValid) Text(stringResource(R.string.invalid_date_format), color = Color.Red) },
                        trailingIcon = { 
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(context, { _, y, m, d ->
                                    eventDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }) {
                                Icon(Icons.Default.CalendarMonth, stringResource(R.string.select_date_label))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && eventDate.isNotBlank()) {
                        onCreate(title, desc, eventType, eventDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(stringResource(R.string.create_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), color = Color.Gray)
            }
        }
    )
}
