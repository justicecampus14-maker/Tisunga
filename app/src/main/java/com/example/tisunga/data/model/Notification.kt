package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id")        val id: String,
    @SerializedName("userId")    val userId: String,
    @SerializedName("groupId")   val groupId: Int?,
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
    @SerializedName("loanId")         val loanId: Int?,
    @SerializedName("groupId")        val groupId: Int?,
    @SerializedName("eventId")        val eventId: Int?,
    @SerializedName("meetingId")      val meetingId: Int?,
    @SerializedName("disbursementId") val disbursementId: Int?,
    @SerializedName("transactionRef") val transactionRef: String?,
    @SerializedName("amount")         val amount: String?
)

data class NotificationGroup(
    @SerializedName("id")   val id: Int,
    @SerializedName("name") val name: String
)
