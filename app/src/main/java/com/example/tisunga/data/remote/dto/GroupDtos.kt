package com.example.tisunga.data.remote.dto

import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Transaction
import com.google.gson.annotations.SerializedName

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
    @SerializedName("description")  val description: String? = null,
    @SerializedName("location")     val location: String? = null,
    @SerializedName("totalSavings") val totalSavings: Double,
    @SerializedName("memberCount")  val memberCount: Int,
    @SerializedName("meetingDay")   val meetingDay: String? = null,
    @SerializedName("meetingTime")  val meetingTime: String? = null,
    @SerializedName("groupCode")    val groupCode: String? = null,
    @SerializedName("endDate")      val endDate: String? = null
)

data class MemberSavingsDto(
    @SerializedName("userId")    val userId: String,
    @SerializedName("userName")  val userName: String,
    @SerializedName("userPhone") val userPhone: String,
    @SerializedName("role")      val role: String = "MEMBER",
    @SerializedName("amount")    val amount: Double = 0.0
)
