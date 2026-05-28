package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupStep1Screen(navController: NavController, viewModel: GroupViewModel) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var savingPeriod by remember { mutableStateOf("6") }
    var location by remember { mutableStateOf("") }
    var minContribution by remember { mutableStateOf("2000") }
    var maxMembers by remember { mutableStateOf("10") }

    val focusManager = LocalFocusManager.current

    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }

    val todayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    var startDate by remember { mutableStateOf(dateFormatter.format(Date(todayMillis))) }
    var endDate by remember { mutableStateOf("") }
    var meetingDay by remember { mutableStateOf("Monday") }
    
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var meetingTime by remember { mutableStateOf(timeFormatter.format(Date())) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)
    val endDatePickerState = rememberDatePickerState()
    
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    var periodExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    // Automatic End Date Calculation
    LaunchedEffect(startDate, savingPeriod) {
        try {
            val start = dateFormatter.parse(startDate)
            if (start != null) {
                val cal = Calendar.getInstance()
                cal.time = start
                cal.add(Calendar.MONTH, savingPeriod.toIntOrNull() ?: 6)
                endDate = dateFormatter.format(cal.time)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val isFormValid = groupName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()
    val fadedColor = Color.Gray.copy(alpha = 0.4f)

    val onContinue = {
        if (isFormValid) {
            viewModel.updateDraft {
                copy(
                    name = groupName,
                    description = description,
                    location = location,
                    minContribution = minContribution.toDoubleOrNull() ?: 0.0,
                    savingPeriodMonths = savingPeriod.toIntOrNull() ?: 6,
                    maxMembers = maxMembers.toIntOrNull() ?: 10,
                    startDate = startDate,
                    endDate = endDate,
                    meetingDay = meetingDay,
                    meetingTime = meetingTime
                )
            }
            navController.navigate(Routes.GROUP_SUMMARY)
        }
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                title = { Text("Create Group", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = White) {
                Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
                    Button(
                        onClick = { onContinue() },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Continue", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Identity
            SectionCard(title = "IDENTITY", icon = Icons.Default.Badge) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomInputField(
                        label = "Group Name",
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = "e.g. Lilongwe Savings Club",
                        fadedColor = fadedColor,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Blended Description Field
                    Column {
                        Text("Description (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            placeholder = { Text("What is this group about?", color = fadedColor, fontSize = 14.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BackgroundGray.copy(alpha = 0.5f),
                                unfocusedContainerColor = BackgroundGray.copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = NavyBlue
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                    }

                    CustomInputField(
                        label = "Location",
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "e.g. Area 47, Lilongwe",
                        fadedColor = fadedColor,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }
            }

            // Section 2: Financial Rules
            SectionCard(title = "FINANCIAL RULES", icon = Icons.Default.AccountBalanceWallet) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomInputField(
                                label = "Min Contribution",
                                value = minContribution,
                                onValueChange = { minContribution = it },
                                placeholder = "2000",
                                fadedColor = fadedColor,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CustomInputField(
                                label = "Max Members",
                                value = maxMembers,
                                onValueChange = { maxMembers = it },
                                placeholder = "10",
                                fadedColor = fadedColor,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                    }

                    Column {
                        Text("Saving Period", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            ReadOnlyField(
                                value = "$savingPeriod Months",
                                icon = Icons.Default.History,
                                fadedColor = fadedColor,
                                trailingIcon = Icons.Default.ArrowDropDown
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { periodExpanded = true })
                            DropdownMenu(
                                expanded = periodExpanded,
                                onDismissRequest = { periodExpanded = false },
                                modifier = Modifier.background(White)
                            ) {
                                listOf("3", "6", "9", "12", "18", "24").forEach {
                                    DropdownMenuItem(
                                        text = { Text("$it Months", color = TextPrimary) },
                                        onClick = { savingPeriod = it; periodExpanded = false })
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Schedule
            SectionCard(title = "SCHEDULE", icon = Icons.Default.Event) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Date", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                ReadOnlyField(value = startDate, icon = Icons.Default.CalendarMonth, fadedColor = fadedColor)
                                Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Date", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            ReadOnlyField(value = endDate, icon = Icons.Default.CalendarMonth, fadedColor = fadedColor, isHighlight = true)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Meeting Day", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                ReadOnlyField(value = meetingDay, icon = Icons.Default.CalendarToday, fadedColor = fadedColor, trailingIcon = Icons.Default.ArrowDropDown)
                                Box(modifier = Modifier.matchParentSize().clickable { dayExpanded = true })
                                DropdownMenu(
                                    expanded = dayExpanded,
                                    onDismissRequest = { dayExpanded = false },
                                    modifier = Modifier.background(White)
                                ) {
                                    listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEach {
                                        DropdownMenuItem(
                                            text = { Text(it, color = TextPrimary) },
                                            onClick = { meetingDay = it; dayExpanded = false })
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Meeting Time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                ReadOnlyField(value = meetingTime, icon = Icons.Default.Schedule, fadedColor = fadedColor)
                                Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { startDate = dateFormatter.format(Date(it)) }
                    showStartDatePicker = false
                }) { Text("OK", color = NavyBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel", color = TextSecondary) } }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = if (timePickerState.hour < 10) "0${timePickerState.hour}" else "${timePickerState.hour}"
                    val m = if (timePickerState.minute < 10) "0${timePickerState.minute}" else "${timePickerState.minute}"
                    meetingTime = "$h:$m"
                    showTimePicker = false
                }) { Text("OK", color = NavyBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = TextSecondary) } },
            text = { TimePicker(state = timePickerState) },
            modifier = Modifier.background(White, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = NavyBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun CustomInputField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String, 
    fadedColor: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = fadedColor, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = DividerColor.copy(alpha = 0.5f),
                focusedBorderColor = NavyBlue,
                unfocusedContainerColor = BackgroundGray.copy(alpha = 0.3f),
                focusedContainerColor = BackgroundGray.copy(alpha = 0.3f)
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

@Composable
fun ReadOnlyField(value: String, icon: ImageVector, fadedColor: Color, isHighlight: Boolean = false, trailingIcon: ImageVector? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isHighlight) NavyBlue.copy(alpha = 0.05f) else BackgroundGray.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isHighlight) NavyBlue.copy(alpha = 0.2f) else DividerColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = if (isHighlight) NavyBlue else fadedColor)
            Spacer(modifier = Modifier.width(10.dp))
            Text(value, fontSize = 14.sp, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium, color = if (isHighlight) NavyBlue else TextPrimary, modifier = Modifier.weight(1f))
            if (trailingIcon != null) {
                Icon(trailingIcon, null, modifier = Modifier.size(20.dp), tint = fadedColor)
            }
        }
    }
}
