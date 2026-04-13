package com.example.tisunga.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.AppNotification
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.NotificationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    vm: NotificationViewModel
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = { vm.markAllRead() }) {
                            Text("Mark all read", fontSize = 13.sp, color = NavyBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
            }
            state.errorMessage.isNotEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.errorMessage, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)) { 
                            Text("Retry") 
                        }
                    }
                }
            }
            state.notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No notifications yet", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.notifications, key = { it.id }) { notif ->
                        NotificationCard(
                            notification = notif,
                            onClick = {
                                if (!notif.isRead) vm.markOneRead(notif.id)
                                // Routing logic can be added here if needed
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) White else Color(0xFFEEF4FF)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = NavyBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        notification.title.take(1).uppercase(),
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimestamp(notification.createdAt),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = notification.body,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!notification.isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(NavyBlue, CircleShape)
                )
            }
        }
    }
}

fun formatTimestamp(iso: String): String = try {
    val instant = Instant.parse(iso)
    val now = Instant.now()
    val diffSeconds = now.epochSecond - instant.epochSecond
    when {
        diffSeconds < 60            -> "Just now"
        diffSeconds < 3600          -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400         -> "${diffSeconds / 3600}h ago"
        diffSeconds < 604800        -> "${diffSeconds / 86400}d ago"
        else -> DateTimeFormatter
            .ofPattern("MMM d")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }
} catch (e: Exception) { "" }
