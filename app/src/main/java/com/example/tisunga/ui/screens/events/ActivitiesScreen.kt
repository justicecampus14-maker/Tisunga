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
import androidx.compose.material.icons.filled.Warning
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

@Composable
fun ActivitiesScreen(
    navController: NavController,
    groupId: String,
    viewModel: ActivitiesViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(groupId) {
        viewModel.load(groupId)
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
                    text = { Text("New") }
                )
            } else if (tab == 1) { // Only show for Events tab
                ExtendedFloatingActionButton(
                    onClick = { showCreateEventDialog = true },
                    containerColor = NavyBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add event") }
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
                        Toast.makeText(context, "Event created successfully!", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "Meeting scheduled successfully!", Toast.LENGTH_SHORT).show()
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
                    text = { Text("Meetings", color = if (tab == 0) MaterialTheme.colorScheme.primary else TextSecondary) }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Other Events", color = if (tab == 1) MaterialTheme.colorScheme.primary else TextSecondary) }
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

/* -------- TOP BAR -------- */

@Composable
fun TopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
        }
        Column {
            Text("Events", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/* -------- MEETINGS -------- */

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
                FilterChip(
                    selected = filter == it,
                    onClick = { onFilterChange(it) },
                    label = { Text(it) },
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
                Text("No meetings found", color = Color.Gray)
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
                    Text("by ${m.creatorName ?: "Admin"}", fontSize = 12.sp, color = Color.Gray)
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

/* -------- EVENTS -------- */

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
                FilterChip(
                    selected = filter == it,
                    onClick = { onFilterChange(it) },
                    label = { Text(it) },
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
                Text("No events found", color = Color.Gray)
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
                    Text("MK ${e.currentAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.tisunga.ui.theme.GreenAccent)
                    Text("Target: MK ${e.targetAmount}", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                Text("Collected: MK ${e.currentAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.tisunga.ui.theme.GreenAccent)
            }

            Spacer(Modifier.height(8.dp))
            if (!e.endDate.isNullOrBlank()) {
                Text(FormatUtils.formatDate(e.endDate), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

/* -------- STATUS BADGE -------- */

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

/* -------- CREATE MEETING DIALOG -------- */

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
        title = { Text("Schedule New Meeting", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Meeting Title") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text("Scheduled At") },
                        readOnly = true,
                        isError = !isDateValid,
                        supportingText = { if (!isDateValid) Text("Invalid format. Use YYYY-MM-DD HH:MM", color = Color.Red) },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, "select date") },
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
                    label = { Text("Location") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

/* -------- CREATE EVENT DIALOG -------- */

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Event", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
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
                                Text(type, color = NavyBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Wedding", "Funeral", "Birthday", "Others").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            type = option
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
                        label = { Text("Date") },
                        readOnly = true,
                        isError = !isDateValid,
                        supportingText = { if (!isDateValid) Text("Invalid format. Use YYYY-MM-DD", color = Color.Red) },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, "select date") },
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
                


                Text("Amount Type", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("FIXED", "FLEXIBLE").forEach {
                        FilterChip(
                            selected = amountType == it,
                            onClick = { amountType = it },
                            label = { Text(it, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
