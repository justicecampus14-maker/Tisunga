package com.example.tisunga.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.theme.*

data class NotificationItem(
    val id: Int,
    val senderName: String,
    val initials: String,
    val messagePreview: String,
    val date: String,
    val time: String,
    val unreadCount: Int,
    val avatarColor: Color
)

@Composable
fun NotificationsScreen(navController: NavController) {
    val notifications = listOf(
        NotificationItem(1, "Doman Group", "DG", "Chikula Phiri has contributed MK 20,000...", "3/20", "6:26PM", 5, Color(0xFFCE93D8)),
        NotificationItem(2, "Tisunga", "TA", "Your loan application has been approved...", "3/20", "5:00PM", 1, Color(0xFFEF9A9A)),
        NotificationItem(3, "Chikondano Group", "CG", "Meeting reminder for tomorrow 3:00pm", "3/19", "2:15PM", 0, Color(0xFF90CAF9)),
        NotificationItem(4, "Doman Group", "DG", "New event created: Funeral Contribution", "3/18", "10:00AM", 2, Color(0xFFF48FB1)),
        NotificationItem(5, "Doman Group", "DG", "Disbursement requested by Chairperson", "3/17", "4:30PM", 1, Color(0xFF80CBC4))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Notification",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification)
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
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = notification.avatarColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        notification.initials,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
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
                        notification.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${notification.date} ${notification.time}", fontSize = 12.sp, color = TextSecondary)
                }
                Text(
                    notification.messagePreview,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (notification.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(22.dp),
                    shape = CircleShape,
                    color = notification.avatarColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            notification.unreadCount.toString(),
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
