package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.theme.NavyBlue
import com.example.tisunga.ui.theme.TextSecondary
import com.example.tisunga.viewmodel.GroupViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.selectedGroup
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var meetingTime by remember { mutableStateOf("") }
    var meetingDay by remember { mutableStateOf("Monday") }

    var showTimePicker by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    val onSave = {
        if (name.isNotBlank()) {
            viewModel.updateGroup(groupId, name, description, location, meetingTime, meetingDay)
        }
    }

    LaunchedEffect(groupId) {
        viewModel.getGroupDashboard(groupId)
    }

    LaunchedEffect(group) {
        group?.let {
            name = it.name
            description = it.description ?: ""
            location = it.location ?: ""
            meetingTime = it.meetingTime ?: ""
            meetingDay = it.meetingDay ?: "Monday"
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.successMessage.isNotEmpty()) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Group", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp).size(24.dp),
                            strokeWidth = 2.dp,
                            color = NavyBlue
                        )
                    } else {
                        IconButton(
                            onClick = { onSave() },
                            enabled = name.isNotBlank()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Section 1: Basic Info
            EditSection(title = "BASIC INFORMATION") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EditInputField(
                        label = "Group Name*",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Enter group name",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    EditInputField(
                        label = "Location",
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "Enter group location",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }
            }

            // Section 2: Schedule
            EditSection(title = "MEETING SCHEDULE") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Meeting Day", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                OutlinedTextField(
                                    value = meetingDay,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue)
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { dayExpanded = true })
                                DropdownMenu(
                                    expanded = dayExpanded,
                                    onDismissRequest = { dayExpanded = false }
                                ) {
                                    listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEach { day ->
                                        DropdownMenuItem(
                                            text = { Text(day) },
                                            onClick = { meetingDay = day; dayExpanded = false }
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Meeting Time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                OutlinedTextField(
                                    value = meetingTime,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Select time") },
                                    readOnly = true,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { Icon(Icons.Default.Schedule, null, tint = NavyBlue) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue)
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                            }
                        }
                    }
                }
            }

            // Section 3: Description
            EditSection(title = "DESCRIPTION") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("What is this group about?") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        focusManager.clearFocus()
                        onSave()
                    })
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = !uiState.isLoading && name.isNotBlank()
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
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
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
fun EditSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun EditInputField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}
