package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class AppNotification(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("type") val type: String, // "GENERAL", "CONTRIBUTION", "LOAN", "EVENT", "MEETING"
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class NotificationsResponse(
    @SerializedName("notifications") val notifications: List<AppNotification>,
    @SerializedName("unreadCount") val unreadCount: Int
)
