package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
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
    
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var meetingDay by remember { mutableStateOf("Monday") }
    var meetingTime by remember { mutableStateOf("10:00") }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { 
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    var periodExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    val isFormValid = groupName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()

    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
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
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyBlue,
                            disabledContainerColor = NavyBlue.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            stringResource(R.string.continue_button),
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_desc)
                    )
                }
                Text(
                    stringResource(R.string.create_group_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Basic Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BASIC INFO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(stringResource(R.string.group_name_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.group_name_hint)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(stringResource(R.string.description_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        placeholder = { Text(stringResource(R.string.description_hint)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("LOCATION", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Area 18, Lilongwe") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financials Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FINANCIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MIN CONTRIBUTION", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            OutlinedTextField(
                                value = minContribution,
                                onValueChange = { minContribution = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = DividerColor,
                                    focusedBorderColor = NavyBlue,
                                    unfocusedContainerColor = BackgroundGray,
                                    focusedContainerColor = BackgroundGray
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MAX MEMBERS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            OutlinedTextField(
                                value = maxMembers,
                                onValueChange = { maxMembers = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = DividerColor,
                                    focusedBorderColor = NavyBlue,
                                    unfocusedContainerColor = BackgroundGray,
                                    focusedContainerColor = BackgroundGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(stringResource(R.string.saving_period_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box {
                        OutlinedTextField(
                            value = "$savingPeriod Months",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = DividerColor,
                                disabledTextColor = TextPrimary,
                                disabledContainerColor = BackgroundGray
                            ),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary) }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Schedule Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SCHEDULE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("START DATE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box {
                                OutlinedTextField(
                                    value = startDate,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    placeholder = { Text("YYYY-MM-DD") },
                                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = TextSecondary) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = DividerColor,
                                        disabledTextColor = TextPrimary,
                                        disabledContainerColor = BackgroundGray
                                    )
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("END DATE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box {
                                OutlinedTextField(
                                    value = endDate,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    placeholder = { Text("YYYY-MM-DD") },
                                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = TextSecondary) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = DividerColor,
                                        disabledTextColor = TextPrimary,
                                        disabledContainerColor = BackgroundGray
                                    )
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MEETING DAY", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box {
                                OutlinedTextField(
                                    value = meetingDay,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = DividerColor,
                                        disabledTextColor = TextPrimary,
                                        disabledContainerColor = BackgroundGray
                                    ),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary) }
                                )
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
                            Text("MEETING TIME", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            OutlinedTextField(
                                value = meetingTime,
                                onValueChange = { meetingTime = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("e.g. 10:00 AM") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = DividerColor,
                                    focusedBorderColor = NavyBlue,
                                    unfocusedContainerColor = BackgroundGray,
                                    focusedContainerColor = White
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Material 3 Date Pickers with Forced White Theme
    val datePickerColors = DatePickerDefaults.colors(
        containerColor = White,
        titleContentColor = NavyBlue,
        headlineContentColor = TextPrimary,
        weekdayContentColor = TextSecondary,
        subheadContentColor = TextSecondary,
        yearContentColor = TextPrimary,
        currentYearContentColor = NavyBlue,
        selectedYearContentColor = White,
        selectedYearContainerColor = NavyBlue,
        dayContentColor = TextPrimary,
        selectedDayContentColor = White,
        selectedDayContainerColor = NavyBlue,
        todayContentColor = NavyBlue,
        todayDateBorderColor = NavyBlue
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        startDate = dateFormatter.format(Date(it))
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = NavyBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            },
            colors = DatePickerDefaults.colors(containerColor = White)
        ) {
            DatePicker(
                state = startDatePickerState,
                colors = datePickerColors
            )
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        endDate = dateFormatter.format(Date(it))
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = NavyBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            },
            colors = DatePickerDefaults.colors(containerColor = White)
        ) {
            DatePicker(
                state = endDatePickerState,
                colors = datePickerColors
            )
        }
    }
}
