package com.example.tisunga.data.repository

import com.example.tisunga.data.model.AppNotification
import com.example.tisunga.data.remote.ApiClient

class NotificationRepository {
    private val api = ApiClient.getClient()

    suspend fun getNotifications(unreadOnly: Boolean = false): Result<Pair<List<AppNotification>, Int>> =
        runCatching {
            // Backend expects unreadOnly as a String? ("true" | "false")
            val resp = api.getNotifications(unreadOnly = if (unreadOnly) "true" else "false")
            Pair(resp.notifications, resp.unreadCount)
        }

    suspend fun markAllRead(): Result<Unit> =
        runCatching { 
            api.markAllNotificationsRead()
            Unit 
        }

    suspend fun markOneRead(notifId: String): Result<Unit> =
        runCatching { 
            api.markNotificationRead(notifId)
            Unit 
        }
}
