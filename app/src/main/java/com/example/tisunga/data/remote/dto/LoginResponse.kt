package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.tisunga.data.model.Group

/**
 * Shape returned by GET /groups/my
 * Backend returns a single object (or null), not a list.
 */
data class MyGroupResponse(
    @SerializedName("groupId")      val groupId: Int = 0,
    @SerializedName("groupName")    val groupName: String = "",
    @SerializedName("groupCode")    val groupCode: String? = null,
    @SerializedName("role")         val role: String = "MEMBER",
    @SerializedName("totalSavings") val totalSavings: Double = 0.0,
    @SerializedName("mySavings")    val mySavings: Double = 0.0,
    @SerializedName("memberCount")  val memberCount: Int = 0,
    @SerializedName("isActive")     val isActive: Boolean = true,
    @SerializedName("joinedAt")     val joinedAt: String? = null,
    @SerializedName("group")        val group: Group? = null
) {
    /** Convert to the Group model the rest of the app uses */
    fun toGroup(): Group = group?.copy(
        totalSavings = totalSavings,
        mySavings    = mySavings,
        groupCode    = groupCode
    ) ?: Group(
        id              = groupId.toString(),
        name            = groupName,
        description     = "",
        location        = "",
        minContribution = 0.0,
        savingPeriod    = 0,
        maxMembers      = 0,
        visibility      = "PUBLIC",
        startDate       = "",
        endDate         = "",
        meetingDay      = "",
        meetingTime     = "",
        totalSavings    = totalSavings,
        mySavings       = mySavings,
        groupCode       = groupCode
    )
}
