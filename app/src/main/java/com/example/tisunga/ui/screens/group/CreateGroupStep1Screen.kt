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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun CreateGroupStep1Screen(navController: NavController, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var minContribution by remember { mutableStateOf("2000") }
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
                    minContribution = minContribution.toDoubleOrNull() ?: 2000.0,
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
        containerColor = BackgroundGray,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onCreateGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.group_name_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { 
                            groupName = it
                            if (it.isNotEmpty()) groupNameError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.group_name_hint), color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(10.dp),
                        isError = groupNameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    if (groupNameError != null) {
                        Text(
                            text = groupNameError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.description_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { 
                            description = it
                            if (it.isNotEmpty()) descriptionError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text(stringResource(R.string.description_hint), color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(10.dp),
                        isError = descriptionError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    if (descriptionError != null) {
                        Text(
                            text = descriptionError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.location_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { 
                            location = it
                            if (it.isNotEmpty()) locationError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.location_hint), color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(10.dp),
                        isError = locationError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    if (locationError != null) {
                        Text(
                            text = locationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.min_contribution_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = minContribution,
                        onValueChange = { 
                            minContribution = it
                            if (it.isNotEmpty()) minContributionError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("2000", color = TextSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(10.dp),
                        isError = minContributionError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onCreateGroup() })
                    )
                    if (minContributionError != null) {
                        Text(
                            text = minContributionError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.saving_period_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Box {
                        OutlinedTextField(
                            value = savingPeriod,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { periodExpanded = true },
                            readOnly = true,
                            placeholder = { Text("Select period", color = TextSecondary.copy(alpha = 0.5f)) },
                            shape = RoundedCornerShape(10.dp),
                            isError = savingPeriodError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = DividerColor,
                                focusedBorderColor = NavyBlue,
                                unfocusedContainerColor = BackgroundGray,
                                focusedContainerColor = BackgroundGray,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            ),
                            trailingIcon = {
                                IconButton(onClick = { periodExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = periodExpanded,
                            onDismissRequest = { periodExpanded = false }) {
                            listOf("1", "2", "3", "4", "5", "6", "9", "12", "18", "24").forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = { 
                                        savingPeriod = it
                                        savingPeriodError = null
                                        periodExpanded = false 
                                    })
                            }
                        }
                    }
                    if (savingPeriodError != null) {
                        Text(
                            text = savingPeriodError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
