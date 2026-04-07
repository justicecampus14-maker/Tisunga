package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun CreateGroupStep2Screen(navController: NavController, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    var startDate by remember { mutableStateOf("20th May 2026") }
    var endDate by remember { mutableStateOf("21st May 2027") }
    var meetingDay by remember { mutableStateOf("") }
    var meetingTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(stringResource(R.string.create_group_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.chair_notice),
            color = PurpleSubtitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.location_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.location_hint)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(stringResource(R.string.max_members_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = { if(it.all { c -> c.isDigit() }) maxMembers = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 15") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                   Column(modifier = Modifier.weight(1f)) {
                       Text(stringResource(R.string.start_date_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                       OutlinedTextField(
                           value = startDate,
                           onValueChange = { startDate = it },
                           modifier = Modifier.fillMaxWidth(),
                           shape = RoundedCornerShape(10.dp),
                           trailingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) },
                           colors = OutlinedTextFieldDefaults.colors(
                               unfocusedBorderColor = DividerColor,
                               focusedBorderColor = NavyBlue,
                               unfocusedContainerColor = BackgroundGray,
                               focusedContainerColor = BackgroundGray
                           )
                       )
                   }
                   Column(modifier = Modifier.weight(1f)) {
                       Text(stringResource(R.string.end_date_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                       OutlinedTextField(
                           value = endDate,
                           onValueChange = { endDate = it },
                           modifier = Modifier.fillMaxWidth(),
                           shape = RoundedCornerShape(10.dp),
                           trailingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) },
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

                Text(stringResource(R.string.meeting_day_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = meetingDay,
                    onValueChange = { meetingDay = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.meeting_day_hint)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(stringResource(R.string.meeting_time_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = meetingTime,
                    onValueChange = { meetingTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.meeting_time_hint)) },
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = { Icon(Icons.Default.Schedule, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        val currentPending = uiState.pendingGroup
                        if (currentPending != null) {
                            viewModel.updatePendingGroup(
                                currentPending.copy(
                                    location = location,
                                    maxMembers = maxMembers.toIntOrNull() ?: 15,
                                    startDate = startDate,
                                    endDate = endDate,
                                    meetingDay = meetingDay,
                                    meetingTime = meetingTime
                                )
                            )
                        }
                        navController.navigate(Routes.GROUP_SUMMARY) 
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text(stringResource(R.string.continue_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
