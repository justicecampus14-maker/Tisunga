package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun CreateGroupStep1Screen(navController: NavController, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var minContribution by remember { mutableStateOf("") }
    var savingPeriod by remember { mutableStateOf("6") }
    
    var groupNameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var minContributionError by remember { mutableStateOf<String?>(null) }
    var savingPeriodError by remember { mutableStateOf<String?>(null) }

    var periodExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val onCreateGroup = {
        groupNameError = if (groupName.isEmpty()) "Group name is required" else null
        descriptionError = if (description.isEmpty()) "Description is required" else null
        locationError = if (location.isEmpty()) "Location is required" else null
        minContributionError = if (minContribution.isEmpty()) "Minimum contribution is required" else null
        savingPeriodError = if (savingPeriod.isEmpty()) "Saving period is required" else null

        if (groupNameError == null && 
            descriptionError == null && 
            locationError == null && 
            minContributionError == null && 
            savingPeriodError == null) {
            viewModel.createGroup(
                Group(
                    id = 0,
                    name = groupName,
                    description = description,
                    savingPeriod = savingPeriod.toIntOrNull() ?: 6,
                    location = location,
                    minContribution = minContribution.toDoubleOrNull() ?: 0.0,
                    maxMembers = 15,
                    visibility = "Public",
                    startDate = "",
                    endDate = "",
                    meetingDay = "Not specified",
                    meetingTime = "Not specified"
                )
            )
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.ADD_MEMBERS.replace("{groupId}", (uiState.selectedGroup?.id ?: 0).toString()))
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = stringResource(R.string.create_group_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = BackgroundGray,
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                GroupFormField(
                    label = stringResource(R.string.group_name_label),
                    error = groupNameError
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { 
                            groupName = it
                            if (it.isNotEmpty()) groupNameError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.group_name_hint), color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(12.dp),
                        isError = groupNameError != null,
                        colors = fieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true
                    )
                }

                GroupFormField(
                    label = stringResource(R.string.description_label),
                    error = descriptionError
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { 
                            description = it
                            if (it.isNotEmpty()) descriptionError = null
                        },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text(stringResource(R.string.description_hint), color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(12.dp),
                        isError = descriptionError != null,
                        colors = fieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                GroupFormField(
                    label = stringResource(R.string.location_label),
                    error = locationError
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { 
                            location = it
                            if (it.isNotEmpty()) locationError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.location_hint), color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(12.dp),
                        isError = locationError != null,
                        colors = fieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        GroupFormField(
                            label = stringResource(R.string.min_contribution_label),
                            error = minContributionError
                        ) {
                            OutlinedTextField(
                                value = minContribution,
                                onValueChange = { 
                                    minContribution = it
                                    if (it.isNotEmpty()) minContributionError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("MK 2000", color = TextSecondary.copy(alpha = 0.5f)) },
                                shape = RoundedCornerShape(12.dp),
                                isError = minContributionError != null,
                                colors = fieldColors(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                singleLine = true
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        GroupFormField(
                            label = stringResource(R.string.saving_period_label),
                            error = savingPeriodError
                        ) {
                            Box {
                                OutlinedTextField(
                                    value = "$savingPeriod Months",
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth().clickable { periodExpanded = true },
                                    readOnly = true,
                                    shape = RoundedCornerShape(12.dp),
                                    isError = savingPeriodError != null,
                                    colors = fieldColors(),
                                    trailingIcon = {
                                        IconButton(onClick = { periodExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null, tint = NavyBlue)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = periodExpanded,
                                    onDismissRequest = { periodExpanded = false },
                                    modifier = Modifier.background(White)
                                ) {
                                    listOf("1", "2", "3", "4", "5", "6", "9", "12", "18", "24").forEach {
                                        DropdownMenuItem(
                                            text = { Text("$it Months") },
                                            onClick = { 
                                                savingPeriod = it
                                                savingPeriodError = null
                                                periodExpanded = false 
                                            })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onCreateGroup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Create Group",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GroupFormField(label: String, error: String? = null, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = DividerColor,
    focusedBorderColor = NavyBlue,
    unfocusedContainerColor = White,
    focusedContainerColor = White,
    errorBorderColor = MaterialTheme.colorScheme.error
)
