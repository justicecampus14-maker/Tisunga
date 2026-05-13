package com.example.tisunga.ui.screens.events

import android.widget.Toast
import com.example.tisunga.utils.FormatUtils
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.runtime.*
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(groupId, navBackStackEntry) {
        val isCurrent = navBackStackEntry?.destination?.route?.startsWith("activities") == true || 
                       navBackStackEntry?.destination?.route?.startsWith("events") == true
        if (isCurrent) {
            viewModel.load(groupId, forceRefresh = true)
        }
    }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    var tab by remember { mutableIntStateOf(0) }
    var meetingFilter by remember { mutableStateOf("ALL") }
    var eventFilter by remember { mutableStateOf("ALL") }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var showCreateMeetingDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(navController)
        },
        floatingActionButton = {
            if (tab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateMeetingDialog = true },
                    containerColor = NavyBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.add_button)) }
                )
            } else if (tab == 1) { // Only show for Events tab
                ExtendedFloatingActionButton(
                    onClick = { showCreateEventDialog = true },
                    containerColor = NavyBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.add_event_button)) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = BackgroundGray
    ) { padding ->
        if (showCreateEventDialog) {
            CreateEventDialog(
                onDismiss = { showCreateEventDialog = false },
                onConfirm = { type, title, date, amountType, amount, desc ->
                    viewModel.createEvent(groupId, type, title, date, amountType, amount, desc) {
                        showCreateEventDialog = false
                        Toast.makeText(context, context.getString(R.string.event_created_success), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        if (showCreateMeetingDialog) {
            CreateMeetingDialog(
                onDismiss = { showCreateMeetingDialog = false },
                onConfirm = { title, date, location, desc ->
                    viewModel.createMeeting(groupId, title, date, location, desc) {
                        showCreateMeetingDialog = false
                        Toast.makeText(context, context.getString(R.string.meeting_scheduled_success), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[tab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text(stringResource(R.string.tab_meetings), color = if (tab == 0) MaterialTheme.colorScheme.primary else TextSecondary) }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text(stringResource(R.string.tab_other_events), color = if (tab == 1) MaterialTheme.colorScheme.primary else TextSecondary) }
                )
            }

            if (viewModel.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
            } else {
                if (tab == 0) {
                    MeetingsContent(viewModel.meetings, meetingFilter, { meetingFilter = it }) { m ->
                        navController.navigate("meeting_detail/$groupId/${m.id}")
                    }
                } else {
                    EventsContent(viewModel.events, eventFilter) {
                        eventFilter = it
                    }
                }
            }
        }
    }
}

/*  TOP BAR */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.events_title), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

//MEETINGS 

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
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            listOf("ALL", "SCHEDULED", "ONGOING", "COMPLETED", "CANCELLED").forEach {
                val labelId = when(it) {
                    "ALL" -> R.string.filter_all_caps
                    "SCHEDULED" -> R.string.filter_scheduled
                    "ONGOING" -> R.string.filter_ongoing
                    "COMPLETED" -> R.string.filter_completed
                    "CANCELLED" -> R.string.filter_cancelled
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

        if (meetings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_meetings_msg), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredMeetings = if (filter == "ALL") meetings else meetings.filter { it.status == filter }
                items(filteredMeetings, key = { it.id }) { m ->
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
                    Text(m.title, fontWeight = FontWeight.Bold, color = NavyBlue)
                    val creator = m.creatorName ?: stringResource(R.string.member_default_name)
                    Text(stringResource(R.string.by_user_placeholder, creator), fontSize = 12.sp, color = Color.Gray)
                }
                com.example.tisunga.ui.components.StatusBadge(m.status)
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(FormatUtils.formatDate(m.scheduledAt), fontSize = 12.sp, color = Color.DarkGray)
                if (!m.location.isNullOrEmpty()) {
                    Text(" • ", fontSize = 12.sp, color = Color.Gray)
                    Text(m.location, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
            if (!m.agenda.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(m.agenda, fontSize = 12.sp, color = Color.Gray)
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
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            listOf("ALL", "OPEN", "UPCOMING", "CLOSED").forEach {
                val labelId = when(it) {
                    "ALL" -> R.string.filter_all_caps
                    "OPEN" -> R.string.filter_open
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

        if (events.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_events_found), color = Color.Gray)
            }
        } else {
            val filteredEvents = if (filter == "ALL") events else events.filter { it.status.uppercase() == filter }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEvents) { e ->
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
                Text(e.title, fontWeight = FontWeight.Bold, color = NavyBlue, modifier = Modifier.weight(1f))
                com.example.tisunga.ui.components.StatusBadge(e.status)
            }

            Spacer(Modifier.height(8.dp))

            if (e.targetAmount != null && e.targetAmount > 0) {
                val progress = (e.currentAmount / e.targetAmount).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = com.example.tisunga.ui.theme.GreenAccent,
                    trackColor = Color(0xFFEEEEEE),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.amount_mk, e.currentAmount.toString()), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.tisunga.ui.theme.GreenAccent)
                    Text(stringResource(R.string.target_amount_label, e.targetAmount.toString()), fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                Text(stringResource(R.string.collected_amount_label, e.currentAmount.toString()), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.tisunga.ui.theme.GreenAccent)
            }

            Spacer(Modifier.height(8.dp))
            if (!e.endDate.isNullOrBlank()) {
                Text(FormatUtils.formatDate(e.endDate), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// STATUS BADGE

@Composable
fun StatusBadge(status: String) {
    val color = when (status.uppercase()) {
        "ONGOING", "OPEN" -> GreenAccent
        "SCHEDULED", "UPCOMING" -> BlueLink
        "COMPLETED", "CLOSED" -> TextSecondary
        "CANCELLED" -> RedAccent
        else -> MaterialTheme.colorScheme.primary
    }

    Text(
        text = status,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

// CREATE MEETING DIALOG

@Composable
fun CreateMeetingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val isDateValid = remember(date) {
        if (date.isBlank()) true 
        else try {
            val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                .withResolverStyle(ResolverStyle.STRICT)
            LocalDateTime.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_meeting_title), fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.meeting_title_label)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text(stringResource(R.string.schedule_datetime_label)) },
                        readOnly = true,
                        isError = !isDateValid,
                        supportingText = { if (!isDateValid) Text(stringResource(R.string.invalid_datetime_format), color = Color.Red) },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, stringResource(R.string.select_date_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val c = Calendar.getInstance()
                                DatePickerDialog(context, { _, y, m, d ->
                                    val datePart = String.format("%04d-%02d-%02d", y, m + 1, d)
                                    TimePickerDialog(context, { _, h, min ->
                                        date = String.format("%s %02d:%02d", datePart, h, min)
                                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                            }
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.location_label)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_label)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isDateValid && title.isNotBlank() && date.isNotBlank() && location.isNotBlank() && description.isNotBlank(),
                onClick = {
                    onConfirm(title, date, location, description)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(stringResource(R.string.schedule_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), color = Color.Gray)
            }
        }
    )
}

// CREATE EVENT DIALOG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Double, String?) -> Unit
) {
    var type by remember { mutableStateOf("Wedding") }
    var expanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var amountType by remember { mutableStateOf("FIXED") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val isDateValid = remember(date) {
        if (date.isBlank()) true
        else try {
            val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT)
            LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    val eventTypes = listOf(
        stringResource(R.string.event_type_wedding) to "Wedding",
        stringResource(R.string.event_type_funeral) to "Funeral",
        stringResource(R.string.event_type_birthday) to "Birthday",
        stringResource(R.string.event_type_other) to "Others"
    )
    val displayType = eventTypes.find { it.second == type }?.first ?: type

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_event_title), fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.event_title_label)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    trailingIcon = {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { expanded = true }
                                    .padding(end = 8.dp)
                            ) {
                                Text(displayType, color = NavyBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                eventTypes.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            type = value
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text(stringResource(R.string.date_label)) },
                        readOnly = true,
                        isError = !isDateValid,
                        supportingText = { if (!isDateValid) Text(stringResource(R.string.invalid_date_format), color = Color.Red) },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, stringResource(R.string.select_date_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val c = Calendar.getInstance()
                                DatePickerDialog(context, { _, y, m, d ->
                                    date = String.format("%04d-%02d-%02d", y, m + 1, d)
                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                            }
                    )
                }
                


                Text(stringResource(R.string.amount_type_label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("FIXED", "FLEXIBLE").forEach {
                        val labelId = if (it == "FIXED") R.string.amount_type_fixed else R.string.amount_type_flexible
                        FilterChip(
                            selected = amountType == it,
                            onClick = { amountType = it },
                            label = { Text(stringResource(labelId), fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.event_target_amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_label)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isDateValid && title.isNotBlank() && date.isNotBlank() && amount.isNotBlank(),
                onClick = {
                    onConfirm(type, title, date, amountType, amount.toDoubleOrNull() ?: 0.0, description.takeIf { it.isNotBlank() })
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
