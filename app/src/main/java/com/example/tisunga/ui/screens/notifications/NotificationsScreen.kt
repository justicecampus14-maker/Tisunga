package com.example.tisunga.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.ui.theme.*

data class NotificationItem(
    val id: Int,
    val senderName: String,
    val initials: String,
    val messagePreview: String,
    val date: String,
    val time: String,
    val isPriority: Boolean = false,
    val avatarColor: Color = NavyBlue
)

@Composable
fun NotificationsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    
    val notifications = remember {
        listOf(
            NotificationItem(0, "Tisunga", "TS", "You have successfully created the Mphatso Group!", "Today", "Just now", true, GreenAccent),
            NotificationItem(1, "Doman Group", "DG", "Chikula Phiri has contributed MK 20,000...", "3/20", "6:26PM"),
            NotificationItem(2, "Tisunga", "TS", "Your loan application has been approved...", "3/20", "5:00PM", false, NavyBlue),
            NotificationItem(3, "Chikondano Group", "CG", "Meeting reminder for tomorrow 3:00pm", "3/19", "2:15PM", false, Color(0xFF1565C0)),
            NotificationItem(4, "Doman Group", "DG", "New event created: Funeral Contribution", "3/18", "10:00AM"),
            NotificationItem(5, "Doman Group", "DG", "Disbursement requested by Chairperson", "3/17", "4:30PM")
        )
    }

    val filteredNotifications = notifications.filter {
        it.senderName.contains(searchQuery, ignoreCase = true) ||
        it.messagePreview.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                    Text(
                        stringResource(R.string.notifications_title),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    // Notification Icon
                    IconButton(onClick = { /* Clear All logic */ }) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NavyBlue)
                    }
                }

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search notifications...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = NavyBlue
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredNotifications) { notification ->
                NotificationCard(notification)
            }
            
            if (filteredNotifications.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications found", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = notification.avatarColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.initials,
                    color = notification.avatarColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${notification.date} • ${notification.time}",
                        fontSize = 11.sp,
                        color = if (notification.isPriority) GreenAccent else TextSecondary,
                        fontWeight = if (notification.isPriority) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = notification.messagePreview,
                    fontSize = 13.sp,
                    color = if (notification.isPriority) TextPrimary else TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (notification.isPriority) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
