package com.example.tisunga.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
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
import com.example.tisunga.data.model.Event
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.screens.home.HomeHeader
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.EventViewModel

@Composable
fun EventsScreen(navController: NavController, groupId: Int, viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All events") }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getGroupEvents(groupId)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, type = "C") },
        containerColor = BackgroundLightGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HomeHeader("Michael", "0882752624", navController)

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
                IconButton(onClick = { /* Forward? */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Events", fontSize = 20.sp, fontWeight = Bold)
                    Text("Doman Group", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All events", "Active", "Upcoming", "Closed")
                items(filters) { filter ->
                    FilterChip(label = filter, isSelected = selectedFilter == filter) { selectedFilter = filter }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active Events(${uiState.activeEvents.size})", fontSize = 15.sp, fontWeight = Bold)
                Text(
                    "Create",
                    color = BlueLink,
                    fontWeight = Bold,
                    modifier = Modifier.clickable { showCreateDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.activeEvents) { event ->
                    EventCard(event, isChair = true) {
                        viewModel.closeEvent(event.id)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Closed Events", fontSize = 15.sp, fontWeight = Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(uiState.closedEvents) { event ->
                    EventCard(event, isChair = true, isClosed = true) {}
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { event ->
                viewModel.createEvent(event.copy(groupId = groupId))
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) White else Color(0xFFDDDDDD),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) Bold else FontWeight.Normal,
            color = if (isSelected) NavyBlue else TextSecondary
        )
    }
}

@Composable
fun EventCard(event: Event, isChair: Boolean, isClosed: Boolean = false, onCloseClick: () -> Unit) {
    var showConfirmClose by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isClosed) {
                    Box(modifier = Modifier.size(6.dp).background(Color.Black, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                val bgColor = when (event.type) {
                    "Wedding", "Birthday" -> Color(0xFF64B5F6)
                    "Funeral" -> Color(0xFFFFCDD2)
                    else -> Color(0xFFEEEEEE)
                }
                val textColor = when (event.type) {
                    "Funeral" -> OrangeTag
                    else -> BlueLink
                }
                Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        event.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(event.title, fontWeight = Bold, fontSize = 15.sp)
            Text("${event.date} • MK ${event.amount ?: "Flexible"}", fontSize = 12.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (isChair && !isClosed) {
                Text(
                    "Close Event",
                    color = RedAccent,
                    fontWeight = Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.End).clickable { showConfirmClose = true }
                )
            }
        }
    }

    if (showConfirmClose) {
        TisungaConfirmDialog(
            title = "Close Event",
            message = "Are you sure you want to close this event?",
            onConfirm = {
                onCloseClick()
                showConfirmClose = false
            },
            onDismiss = { showConfirmClose = false }
        )
    }
}

@Composable
fun CreateEventDialog(onDismiss: () -> Unit, onCreate: (Event) -> Unit) {
    var type by remember { mutableStateOf("Wedding") }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var amountType by remember { mutableStateOf("Fixed") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var typeExpanded by remember { mutableStateOf(false) }
    var amountTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Event", fontWeight = Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Event Type", fontSize = 12.sp, fontWeight = Bold)
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true }
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        listOf("Wedding", "Birthday", "Funeral", "Other").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { type = it; typeExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("Amount Type", fontSize = 12.sp, fontWeight = Bold)
                Box {
                    OutlinedTextField(
                        value = amountType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { amountTypeExpanded = true }
                    )
                    DropdownMenu(expanded = amountTypeExpanded, onDismissRequest = { amountTypeExpanded = false }) {
                        listOf("Fixed", "Flexible").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { amountType = it; amountTypeExpanded = false })
                        }
                    }
                }
                if (amountType == "Fixed") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (MK)") }, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        Event(
                            id = 0, groupId = 0, type = type, title = title, date = date,
                            amountType = amountType, amount = amount.toDoubleOrNull(),
                            description = description, status = "active"
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Create Event", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
