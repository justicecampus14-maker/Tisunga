package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.Event

data class LoginRequest(
    val phone: String,
    val password: String
)

/** Returned by POST /auth/register */
data class RegisterResponse(
    @SerializedName("userId") val userId: String
)

/** Returned by POST /auth/verify-otp */
data class VerifyOtpResponse(
    @SerializedName("verified") val verified: Boolean = false
)

/** Returned by POST /auth/forgot-password */
data class ForgotPasswordResponse(
    @SerializedName("userId") val userId: String? = null
)

/** Generic { message } wrapper */
data class MessageResponse(
    @SerializedName("message") val message: String = ""
)

/** Returned by GET /groups/search/member */
data class SearchMemberResponse(
    @SerializedName("found")          val found: Boolean = false,
    @SerializedName("user")           val user: UserSummary? = null,
    @SerializedName("alreadyInGroup") val alreadyInGroup: Boolean = false,
    @SerializedName("groupName")      val groupName: String? = null,
    @SerializedName("phone")          val phone: String = ""
)

data class UserSummary(
    @SerializedName("id")        val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName")  val lastName: String,
    @SerializedName("phone")     val phone: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

/** Item in GET /groups/{groupId}/members list */
data class MembershipResponse(
    @SerializedName("id")       val id: String = "",
    @SerializedName("role")     val role: String = "MEMBER",
    @SerializedName("status")   val status: String = "ACTIVE",
    @SerializedName("joinedAt") val joinedAt: String? = null,
    @SerializedName("user")     val user: UserSummary? = null
)

/** Returned by POST /groups/{groupId}/members */
data class AddMemberResponse(
    @SerializedName("membership") val membership: MembershipResponse? = null,
    @SerializedName("user")       val user: UserSummary? = null
)

/** Returned by GET /groups/{groupId}/dashboard */
data class GroupDashboardResponse(
    @SerializedName("group")              val group: GroupSummary? = null,
    @SerializedName("mySavings")          val mySavings: Double = 0.0,
    @SerializedName("myRole")             val myRole: String? = null,
    @SerializedName("recentTransactions") val recentTransactions: List<Transaction> = emptyList(),
    @SerializedName("activeLoans")        val activeLoans: Int = 0,
    @SerializedName("upcomingMeetings")   val upcomingMeetings: List<Any> = emptyList(),
    @SerializedName("upcomingEvents")     val upcomingEvents: List<Event> = emptyList()
)

data class GroupSummary(
    @SerializedName("id")           val id: String,
    @SerializedName("name")         val name: String,
    @SerializedName("totalSavings") val totalSavings: Double,
    @SerializedName("memberCount")  val memberCount: Int,
    @SerializedName("meetingDay")   val meetingDay: String?,
    @SerializedName("meetingTime")  val meetingTime: String?,
    @SerializedName("groupCode")    val groupCode: String?,
    @SerializedName("endDate")      val endDate: String?
)

/**
 * Backend login returns:
 *   { accessToken, refreshToken, user: { id, firstName, phone } }
 * set-password returns the same shape.
 */
data class LoginResponse(
    @SerializedName("accessToken")  val accessToken: String = "",
    @SerializedName("refreshToken") val refreshToken: String = "",
    @SerializedName("user")         val user: UserPayload? = null
) {
    // Convenience accessors so existing callers keep working
    val token: String get() = accessToken
    val userId: String get() = user?.id ?: ""
    val userName: String get() = user?.firstName ?: ""
    val userPhone: String get() = user?.phone ?: ""
}

data class UserPayload(
    @SerializedName("id")        val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("phone")     val phone: String
)
