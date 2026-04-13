package com.example.tisunga.data.remote.dto

import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Loan
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.data.model.MeetingAttendance
import com.example.tisunga.data.model.AttendanceSummary
import com.google.gson.annotations.SerializedName

// ── Auth ─────────────────────────────────────────────────────────────────

// ── Groups ────────────────────────────────────────────────────────────────

// ── Meetings ──────────────────────────────────────────────────────────────

data class MeetingDetailResponse(
    @SerializedName("id")                val id: String,
    @SerializedName("title")             val title: String,
    @SerializedName("agenda")            val agenda: String? = null,
    @SerializedName("location")          val location: String? = null,
    @SerializedName("scheduledAt")       val scheduledAt: String,
    @SerializedName("status")            val status: String,
    @SerializedName("notes")             val notes: String? = null,
    @SerializedName("creatorName")       val creatorName: String? = null,
    @SerializedName("attendance")        val attendance: List<MeetingAttendance> = emptyList(),
    @SerializedName("presentCount")      val presentCount: Int = 0,
    @SerializedName("totalCount")        val totalCount: Int = 0,
    @SerializedName("attendancePercent") val attendancePercent: Int = 0
)

data class MeetingAttendanceResponse(
    @SerializedName("attendance") val attendance: List<MeetingAttendance>,
    @SerializedName("summary")    val summary: AttendanceSummary
)

data class BulkAttendanceRequest(
    @SerializedName("attendance") val attendance: List<AttendanceEntry>
)

data class AttendanceEntry(
    @SerializedName("userId") val userId: String,
    @SerializedName("status") val status: String,   // "PRESENT" | "ABSENT" | "EXCUSED"
    @SerializedName("note")   val note: String? = null
)

data class BulkAttendanceResponse(
    @SerializedName("updated")      val updated: Int,
    @SerializedName("presentCount") val presentCount: Int
)

data class ReminderResponse(
    @SerializedName("sentTo") val sentTo: Int
)

data class DisbursementRequestResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("groupId")       val groupId: String,
    @SerializedName("amount")        val amount: Double,
    @SerializedName("status")        val status: String,
    @SerializedName("requestedBy")   val requestedBy: String,
    @SerializedName("requestedAt")   val requestedAt: String
)

data class DisbursementResponse(
    @SerializedName("id")              val id: String,
    @SerializedName("groupId")         val groupId: String,
    @SerializedName("amount")          val amount: Double,
    @SerializedName("status")          val status: String,
    @SerializedName("requestedBy")     val requestedBy: String,
    @SerializedName("requestedByName") val requestedByName: String?,
    @SerializedName("requestedAt")     val requestedAt: String,
    @SerializedName("approvedBy")      val approvedBy: String?,
    @SerializedName("approvedByName")  val approvedByName: String?,
    @SerializedName("approvedAt")      val approvedAt: String?,
    @SerializedName("rejectionReason") val rejectionReason: String?,
    @SerializedName("memberShares")    val memberShares: List<MemberSharePayoutDto>
)

data class MemberSharePayoutDto(
    @SerializedName("userId")        val userId: String,
    @SerializedName("userName")      val userName: String,
    @SerializedName("userPhone")     val userPhone: String,
    @SerializedName("memberSavings") val memberSavings: Double,
    @SerializedName("shareAmount")   val shareAmount: Double,
    @SerializedName("status")        val status: String
)

data class CreateEventRequest(
    @SerializedName("type")        val type: String,
    @SerializedName("title")       val title: String,
    @SerializedName("date")        val date: String,
    @SerializedName("amountType")  val amountType: String,
    @SerializedName("amount")      val amount: Double,
    @SerializedName("description") val description: String? = null
)

data class ContributeRequest(
    @SerializedName("amount") val amount: Double
)

data class RejectDisbursementRequest(
    @SerializedName("reason") val reason: String
)

// ── Loans ────────────────────────────────────────────────────────────────

data class ApplyLoanRequest(
    @SerializedName("groupId")        val groupId: String,
    @SerializedName("amount")         val amount: Double,
    @SerializedName("durationMonths") val durationMonths: Int,
    @SerializedName("purpose")        val purpose: String? = null
)

data class RepayLoanRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("phone")  val phone: String
)

data class RejectLoanRequest(
    @SerializedName("reason") val reason: String
)

data class ApproveLoanResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("status")        val status: String,
    @SerializedName("approvedAt")    val approvedAt: String,
    @SerializedName("approverName")  val approverName: String
)

data class RepayLoanResponse(
    @SerializedName("transactionRef")   val transactionRef: String,
    @SerializedName("externalRef")      val externalRef: String?,
    @SerializedName("amount")           val amount: Double,
    @SerializedName("remainingBalance") val remainingBalance: Double,
    @SerializedName("status")           val status: String
)

