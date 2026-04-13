package com.example.tisunga.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Event
import com.example.tisunga.ui.components.StatusBadge
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.EventViewModel
import com.example.tisunga.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavController, groupId: String, viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.tisunga.utils.SessionManager(context) }
    
    val groupRole = sessionManager.getGroupRole(groupId)
    val canCreate = groupRole == "CHAIR" || groupRole == "SECRETARY"

    LaunchedEffect(Unit) {
        viewModel.getGroupEvents(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Events", fontWeight = Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = NavyBlue,
                    contentColor = White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Event")
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.events.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavyBlue)
            } else if (uiState.events.isEmpty() && !uiState.isLoading) {
                Text(
                    "No events found for this group.",
                    modifier = Modifier.align(Alignment.Center),
                    color = TextSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.events) { event ->
                        EventListItem(event) {
                            navController.navigate("event_detail/${event.id}")
                        }
                    }
                }
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

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, target, date ->
                viewModel.createEvent(groupId, title, desc, target, date)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun EventListItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                Text(event.title, fontWeight = Bold, fontSize = 18.sp, color = NavyBlue)
                StatusBadge(event.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                event.description,
                maxLines = 2,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (event.targetAmount != null && event.targetAmount > 0) {
                val progress = (event.currentAmount / event.targetAmount).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).background(BackgroundGray, RoundedCornerShape(4.dp)),
                    color = NavyBlue,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(FormatUtils.formatMoney(event.currentAmount), fontSize = 12.sp, fontWeight = Bold)
                    Text("Target: ${FormatUtils.formatMoney(event.targetAmount)}", fontSize = 12.sp, color = TextSecondary)
                }
            } else {
                Text("Collected: ${FormatUtils.formatMoney(event.currentAmount)}", fontSize = 14.sp, fontWeight = Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${event.contributionsCount} contributions",
                    fontSize = 12.sp,
                    color = BlueLink,
                    fontWeight = Bold
                )
                if (event.endDate != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Ends: ${FormatUtils.formatDate(event.endDate)}", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun CreateEventDialog(onDismiss: () -> Unit, onCreate: (String, String, Double?, String?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Event", fontWeight = Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = targetAmount, onValueChange = { targetAmount = it }, label = { Text("Target Amount (Optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, description, targetAmount.toDoubleOrNull(), endDate.ifBlank { null }) },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Create", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
