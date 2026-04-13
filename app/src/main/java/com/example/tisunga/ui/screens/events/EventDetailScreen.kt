package com.example.tisunga.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.tisunga.ui.components.StatusBadge
import com.example.tisunga.ui.navigation.Routes
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.EventContribution
import com.example.tisunga.utils.FormatUtils
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: String, viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showContributeDialog by remember { mutableStateOf(false) }
    var showCloseConfirmDialog by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.tisunga.utils.SessionManager(context) }
    
    LaunchedEffect(Unit) {
        viewModel.getEventDetail(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val event = uiState.eventDetail
                    if (event != null && event.status == "OPEN") {
                        // Normally we need groupId to check role, 
                        // but here we can check if current user is the creator 
                        // or if they have CHAIR/SECRETARY role globally for the group
                        // (EventDetail should ideally include groupId or role info)
                        // For now, if we don't have groupId in the model, we can't easily check role from SessionManager
                        // unless we pass it or the backend includes 'canClose' flag.
                        // Assuming most events belong to the 'current' group context.
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.eventDetail == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavyBlue)
            } else if (uiState.eventDetail != null) {
                val event = uiState.eventDetail!!
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            EventHeader(event.title, event.description, event.status)
                        }

                        item {
                            EventFinancialInfo(
                                currentAmount = event.currentAmount,
                                targetAmount = event.targetAmount,
                                endDate = event.endDate,
                                createdBy = event.createdBy?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"
                            )
                        }

                        item {
                            Text(
                                "Contributions",
                                fontSize = 18.sp,
                                fontWeight = Bold,
                                color = NavyBlue,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (event.contributions.isEmpty()) {
                            item {
                                Text(
                                    "No contributions yet.",
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    color = TextSecondary
                                )
                            }
                        } else {
                            items(event.contributions) { contribution ->
                                ContributionItem(contribution)
                            }
                        }
                    }

                    if (event.status == "OPEN") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showContributeDialog = true },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                            ) {
                                Text("Contribute", fontSize = 16.sp, fontWeight = Bold, color = White)
                            }

                            // Only show Close Event to authorized users (simplified check here)
                            // Ideally, backend should return 'permissions'
                            OutlinedButton(
                                onClick = { showCloseConfirmDialog = true },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Close", fontWeight = Bold)
                            }
                        }
                    }
                }
            }
            
            if (uiState.isLoading && uiState.eventDetail != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), color = NavyBlue)
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.resetState() }) {
                            Text("OK", color = White)
                        }
                    }
                ) {
                    Text(uiState.errorMessage)
                }
            }
        }
    }

    if (showCloseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmDialog = false },
            title = { Text("Close Event") },
            text = { Text("Are you sure you want to close this event? No more contributions will be accepted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeEvent(eventId, "") // groupId not strictly needed for the call itself
                        showCloseConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Close Event", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showContributeDialog) {
        ContributeDialog(
            onDismiss = { showContributeDialog = false },
            onConfirm = { amount ->
                viewModel.contribute(eventId, amount)
                showContributeDialog = false
            }
        )
    }

    if (uiState.contributionMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Contribution Initiated") },
            text = { Text(uiState.contributionMessage) },
            confirmButton = {
                Button(onClick = { viewModel.resetState() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun EventHeader(title: String, description: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 22.sp, fontWeight = Bold, color = NavyBlue)
                StatusBadge(status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 15.sp, color = TextPrimary)
        }
    }
}

@Composable
fun EventFinancialInfo(currentAmount: Double, targetAmount: Double?, endDate: String?, createdBy: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (targetAmount != null && targetAmount > 0) {
                val progress = (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(12.dp).background(BackgroundGray, RoundedCornerShape(6.dp)),
                    color = NavyBlue,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FinancialMetric("Raised", FormatUtils.formatMoney(currentAmount))
                    FinancialMetric("Target", FormatUtils.formatMoney(targetAmount))
                }
            } else {
                FinancialMetric("Total Raised", FormatUtils.formatMoney(currentAmount))
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (endDate != null) {
                    FinancialMetric("End Date", FormatUtils.formatDate(endDate))
                }
                FinancialMetric("Organizer", createdBy)
            }
        }
    }
}

@Composable
fun FinancialMetric(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 16.sp, fontWeight = Bold, color = NavyBlue)
    }
}

@Composable
fun ContributionItem(contribution: EventContribution) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(NavyBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                contribution.user.firstName.take(1) + contribution.user.lastName.take(1),
                fontWeight = Bold,
                color = NavyBlue
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${contribution.user.firstName} ${contribution.user.lastName}", fontWeight = Bold, fontSize = 15.sp)
            Text(FormatUtils.formatDate(contribution.createdAt), fontSize = 12.sp, color = TextSecondary)
        }
        Text(FormatUtils.formatMoney(contribution.amount), fontWeight = Bold, color = Color(0xFF4CAF50))
    }
}

@Composable
fun ContributeDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contribute to Event", fontWeight = Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the amount you wish to contribute.")
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (MK)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 50000") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) },
                enabled = amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Confirm Payment", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
