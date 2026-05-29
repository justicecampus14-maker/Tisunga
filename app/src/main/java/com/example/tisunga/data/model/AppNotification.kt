package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class AppNotification(
    @SerializedName("id")        val id: String,
    @SerializedName("userId")    val userId: String,
    @SerializedName("groupId")   val groupId: String? = null,
    @SerializedName("title")     val title: String,
    @SerializedName("body")      val body: String,
    @SerializedName("type")      val type: String,
    @SerializedName("isRead")    val isRead: Boolean = false,
    @SerializedName("data")      val data: AppNotificationData? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("group")     val group: AppNotificationGroup? = null
)

data class AppNotificationData(
    @SerializedName("disbursementId") val disbursementId: String? = null,
    @SerializedName("loanId")         val loanId: String? = null,
    @SerializedName("eventId")        val eventId: String? = null,
    @SerializedName("meetingId")      val meetingId: String? = null,
    @SerializedName("amount")         val amount: String? = null,
    @SerializedName("transactionRef") val transactionRef: String? = null
)

data class AppNotificationGroup(
    @SerializedName("id")   val id: String,
    @SerializedName("name") val name: String
)

data class NotificationsResponse(
    @SerializedName("notifications") val notifications: List<AppNotification>,
    @SerializedName("unreadCount")   val unreadCount: Int
)
