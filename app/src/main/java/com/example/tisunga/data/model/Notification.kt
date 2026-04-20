package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id")        val id: String,
    @SerializedName("userId")    val userId: String,
    @SerializedName("groupId")   val groupId: String?,
    @SerializedName("type")      val type: NotificationType,
    @SerializedName("title")     val title: String,
    @SerializedName("body")      val body: String,
    @SerializedName("isRead")    val isRead: Boolean,
    @SerializedName("data")      val data: NotificationData?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("group")     val group: NotificationGroup?
)

enum class NotificationType {
    LOAN_APPROVED, LOAN_REJECTED, LOAN_DUE,
    CONTRIBUTION_RECEIVED,
    EVENT_CREATED, EVENT_CLOSED,
    MEMBER_JOINED,
    DISBURSEMENT_REQUESTED, DISBURSEMENT_APPROVED, DISBURSEMENT_REJECTED,
    MEETING_REMINDER, GENERAL
}

data class NotificationData(
    @SerializedName("loanId")         val loanId: String?,
    @SerializedName("groupId")        val groupId: String?,
    @SerializedName("eventId")        val eventId: String?,
    @SerializedName("meetingId")      val meetingId: String?,
    @SerializedName("disbursementId") val disbursementId: String?,
    @SerializedName("transactionRef") val transactionRef: String?,
    @SerializedName("amount")         val amount: String?
)

data class NotificationGroup(
    @SerializedName("id")   val id: String,
    @SerializedName("name") val name: String
)
