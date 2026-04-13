package com.example.tisunga.data.repository

import com.example.tisunga.data.model.AppNotification
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.model.NotificationsResponse

class NotificationRepository {
    private val api = ApiClient.getClient()

    suspend fun getNotifications(unreadOnly: Boolean = false): Result<Pair<List<AppNotification>, Int>> =
        runCatching {
            val resp = api.getNotifications(unreadOnly = unreadOnly)
            Pair(resp.notifications, resp.unreadCount)
        }

    suspend fun markAllRead(): Result<Unit> =
        runCatching { api.markAllNotificationsRead(); Unit }

    suspend fun markOneRead(notifId: String): Result<Unit> =
        runCatching { api.markNotificationRead(notifId); Unit }
}
