package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.toGroup

@Composable
fun GroupSummaryScreen(navController: NavController, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.draft.toGroup()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val createdId = uiState.selectedGroup?.id ?: group.id
            navController.navigate(Routes.GROUP_CREATED_SUCCESS.replace("{groupId}", createdId))
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.createGroup() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                stringResource(R.string.confirm_button), 
                                color = White, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue)
                    ) {
                        Text(
                            stringResource(R.string.edit_button), 
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

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.group_summary_title), 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    SummaryRow(stringResource(R.string.group_name_label_simple), group.name)
                    SummaryRow(stringResource(R.string.location_label), group.location ?: "N/A")
                    SummaryRow(
                        stringResource(R.string.min_contribution_label), 
                        FormatUtils.formatMoney(group.minContribution),
                        isFinancial = true
                    )
                    SummaryRow(stringResource(R.string.saving_period_label), stringResource(R.string.saving_period_months, group.savingPeriod))
                    SummaryRow(stringResource(R.string.max_members_label), "${group.maxMembers}")
                    SummaryRow(stringResource(R.string.meeting_day_label), group.meetingDay ?: "N/A")
                    SummaryRow(stringResource(R.string.meeting_time_label), group.meetingTime ?: "N/A")

                    if (uiState.errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.errorMessage, color = RedAccent, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isFinancial: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = PurpleSubtitle,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isFinancial) {
            Box(
                modifier = Modifier
                    .background(BackgroundGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
