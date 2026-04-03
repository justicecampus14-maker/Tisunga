package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun GroupSummaryScreen(navController: NavController, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Assuming we have the group data in the ViewModel from previous steps
    val group = uiState.selectedGroup ?: Group(
        id = 0, name = "Mphatso Group", description = "...", location = "Zomba, Chikanda",
        minContribution = 2000.0, savingPeriod = 6, maxMembers = 6, visibility = "Public",
        startDate = "20th May 2026", endDate = "21st May 2027", meetingDay = "Friday", meetingTime = "3:00pm"
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("group_created_success/${group.id}")
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.group_summary_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(24.dp))

                SummaryRow(stringResource(R.string.group_name_label_simple), group.name)
                SummaryRow(stringResource(R.string.location_label), group.location)
                SummaryRow(stringResource(R.string.min_contribution_label), stringResource(R.string.amount_mk, group.minContribution.toString()))
                SummaryRow(stringResource(R.string.saving_period_label), stringResource(R.string.saving_period_months, group.savingPeriod))
                SummaryRow(stringResource(R.string.max_members_label), "${group.maxMembers}")
                SummaryRow(stringResource(R.string.visibility_label), group.visibility)
                SummaryRow(stringResource(R.string.meeting_day_label), group.meetingDay)
                SummaryRow(stringResource(R.string.meeting_time_label), group.meetingTime)

                Spacer(modifier = Modifier.weight(1f))

                if (uiState.errorMessage.isNotEmpty()) {
                    Text(uiState.errorMessage, color = RedAccent, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text(stringResource(R.string.edit_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.createGroup(group) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.confirm_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(stringResource(R.string.summary_row_label_value, label, value), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
