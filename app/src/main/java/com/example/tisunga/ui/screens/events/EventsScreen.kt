package com.example.tisunga.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Event
import com.example.tisunga.ui.components.BottomNavBar
import com.example.tisunga.ui.components.SecondaryTopBar
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.EventViewModel
import java.util.Locale

@Composable
fun EventsScreen(navController: NavController, groupId: Int, viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val filterAll = stringResource(R.string.filter_all_events)
    val filterActive = stringResource(R.string.filter_active)
    val filterUpcoming = stringResource(R.string.filter_upcoming)
    val filterClosed = stringResource(R.string.filter_closed)
    
    var selectedFilter by remember { mutableStateOf(filterAll) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getGroupEvents(groupId)
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = stringResource(R.string.events_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController, type = "C") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NavyBlue,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filters = listOf(filterAll, filterActive, filterUpcoming, filterClosed)
                items(filters) { filter ->
                    EventFilterChip(label = filter, isSelected = selectedFilter == filter) { selectedFilter = filter }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.active_events_count, uiState.activeEvents.size),
                        fontSize = 14.sp,
                        fontWeight = Bold,
                        color = NavyBlue,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                items(uiState.activeEvents) { event ->
                    EventItemCard(event, isChair = true) {
                        viewModel.closeEvent(event.id)
                    }
                }

                if (uiState.closedEvents.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.closed_events_title),
                            fontSize = 14.sp,
                            fontWeight = Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    items(uiState.closedEvents) { event ->
                        EventItemCard(event, isChair = true, isClosed = true) {}
                    }
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
fun EventFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) NavyBlue else White,
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) Bold else FontWeight.Medium,
            color = if (isSelected) White else TextSecondary
        )
    }
}

@Composable
fun EventItemCard(event: Event, isChair: Boolean, isClosed: Boolean = false, onCloseClick: () -> Unit) {
    var showConfirmClose by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (bgColor, textColor) = when (event.type) {
                    "Wedding" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
                    "Birthday" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
                    "Funeral" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
                    else -> BackgroundGray to TextSecondary
                }
                
                Surface(color = if (isClosed) BackgroundGray else bgColor, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        event.type.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = if (isClosed) TextSecondary else textColor,
                        fontWeight = Bold
                    )
                }
                
                if (isClosed) {
                    Text("CLOSED", color = RedAccent, fontSize = 10.sp, fontWeight = Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(event.title, fontWeight = Bold, fontSize = 17.sp, color = if (isClosed) TextSecondary else TextPrimary)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(event.date, fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (event.amount != null) "MK ${String.format(Locale.US, "%,.0f", event.amount)}" else "Flexible",
                    fontSize = 13.sp,
                    fontWeight = Bold,
                    color = if (isClosed) TextSecondary else NavyBlue
                )
            }
            
            if (isChair && !isClosed) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.close_event_button),
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
            title = stringResource(R.string.close_event_button),
            message = stringResource(R.string.close_event_confirm_msg),
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
    val typeWedding = stringResource(R.string.event_type_wedding)
    val typeBirthday = stringResource(R.string.event_type_birthday)
    val typeFuneral = stringResource(R.string.event_type_funeral)
    val typeOther = stringResource(R.string.event_type_other)
    
    val amountFixed = stringResource(R.string.amount_type_fixed)
    val amountFlexible = stringResource(R.string.amount_type_flexible)

    var type by remember { mutableStateOf(typeWedding) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var amountType by remember { mutableStateOf(amountFixed) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var typeExpanded by remember { mutableStateOf(false) }
    var amountTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_event_title), fontWeight = Bold, color = NavyBlue) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(stringResource(R.string.event_type_label), fontSize = 12.sp, fontWeight = Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = DividerColor, focusedBorderColor = NavyBlue, unfocusedContainerColor = White, focusedContainerColor = White)
                        )
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            listOf(typeWedding, typeBirthday, typeFuneral, typeOther).forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { type = it; typeExpanded = false })
                            }
                        }
                    }
                }

                Column {
                    Text(stringResource(R.string.event_title_label), fontSize = 12.sp, fontWeight = Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = DividerColor, focusedBorderColor = NavyBlue, unfocusedContainerColor = White, focusedContainerColor = White)
                    )
                }

                Column {
                    Text(stringResource(R.string.date_label), fontSize = 12.sp, fontWeight = Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = date, 
                        onValueChange = { date = it }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("YYYY-MM-DD") },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = DividerColor, focusedBorderColor = NavyBlue, unfocusedContainerColor = White, focusedContainerColor = White)
                    )
                }

                Column {
                    Text(stringResource(R.string.amount_type_label), fontSize = 12.sp, fontWeight = Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = amountType,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { amountTypeExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = DividerColor, focusedBorderColor = NavyBlue, unfocusedContainerColor = White, focusedContainerColor = White)
                        )
                        DropdownMenu(expanded = amountTypeExpanded, onDismissRequest = { amountTypeExpanded = false }) {
                            listOf(amountFixed, amountFlexible).forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { amountType = it; amountTypeExpanded = false })
                            }
                        }
                    }
                }

                if (amountType == amountFixed) {
                    Column {
                        Text(stringResource(R.string.amount_mk_label), fontSize = 12.sp, fontWeight = Bold, color = NavyBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amount, 
                            onValueChange = { amount = it }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = DividerColor, focusedBorderColor = NavyBlue, unfocusedContainerColor = White, focusedContainerColor = White)
                        )
                    }
                }

                Column {
                    Text(stringResource(R.string.description_label), fontSize = 12.sp, fontWeight = Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description, 
                        onValueChange = { description = it }, 
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = DividerColor, focusedBorderColor = NavyBlue, unfocusedContainerColor = White, focusedContainerColor = White)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        Event(
                            id = 0,
                            groupId = 0,
                            type = type,
                            title = title,
                            date = date,
                            amountType = amountType,
                            amount = amount.toDoubleOrNull(),
                            description = description,
                            status = "active",
                            raisedAmount = 0.0
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(stringResource(R.string.ok_button), color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = White
    )
}
