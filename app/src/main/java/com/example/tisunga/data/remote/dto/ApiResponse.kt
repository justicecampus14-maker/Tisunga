package com.example.tisunga.data.remote.dto

import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.StringToDouble
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data")    val data: T? = null,
    @SerializedName("error")   val error: String? = null
)

data class MyGroupResponse(
    @SerializedName("groupId")      val groupId: String? = null,
    @SerializedName("groupName")    val groupName: String? = null,
    @SerializedName("groupCode")    val groupCode: String? = null,
    @SerializedName("role")         val role: String? = "MEMBER",

    @JsonAdapter(StringToDouble::class)
    @SerializedName("totalSavings") val totalSavings: Double = 0.0,

    @JsonAdapter(StringToDouble::class)
    @SerializedName("mySavings")    val mySavings: Double = 0.0,

    @SerializedName("memberCount")  val memberCount: Int? = 0,
    @SerializedName("isActive")     val isActive: Boolean? = true,
    @SerializedName("joinedAt")     val joinedAt: String? = null,

    // ← THIS WAS THE MAIN PROBLEM
    @SerializedName("group")        val group: Group? = null
) {

    /**
     * Returns true ONLY if the user genuinely has NO group
     */
    fun hasNoGroup(): Boolean {
        // Best check: backend sent the full group object
        if (group != null && !group.id.isNullOrBlank()) return false

        // Fallback: check top-level fields
        if (!groupId.isNullOrBlank() && !groupName.isNullOrBlank()) return false

        return true
    }

    /**
     * Convert to our domain Group model (used everywhere in UI)
     */
    fun toGroup(): Group {
        return group?.copy(
            totalSavings = totalSavings,
            mySavings = mySavings,
            groupCode = groupCode ?: group.groupCode
        ) ?: Group(
            id = groupId ?: "",
            name = groupName ?: "",
            description = null,
            location = null,
            groupCode = groupCode ?: "",
            minContribution = 0.0,
            savingPeriod = 0,
            maxMembers = memberCount ?: 0,
            startDate = null,
            endDate = null,
            meetingDay = null,
            meetingTime = null,
            totalSavings = totalSavings,
            isActive = isActive ?: true,
            mySavings = mySavings
        )
    }
}

/**
 * Shape returned by GET /groups/my after the UnwrappingGsonConverterFactory
 * strips the { success, message, data } envelope.
 *
 * The backend sends totalSavings and mySavings as JSON strings ("0"), not numbers.
 * @JsonAdapter(StringToDouble::class) handles both cases transparently.

data class MyGroupResponse(
    @SerializedName("groupId")      val groupId: String? = null,
    @SerializedName("groupName")    val groupName: String? = null,
    @SerializedName("groupCode")    val groupCode: String? = null,
    @SerializedName("role")         val role: String? = "MEMBER",

    // These arrive as strings from the backend: "0", "1500", etc.
    @JsonAdapter(StringToDouble::class)
    @SerializedName("totalSavings") val totalSavings: Double = 0.0,

    @JsonAdapter(StringToDouble::class)
    @SerializedName("mySavings")    val mySavings: Double = 0.0,

    @SerializedName("memberCount")  val memberCount: Int? = 0,
    @SerializedName("isActive")     val isActive: Boolean? = true,
    @SerializedName("joinedAt")     val joinedAt: String? = null,
    @SerializedName("group")        val group: Group? = null
) {
    /** True only when the backend genuinely returned no group data */
    fun hasNoGroup(): Boolean {
        if (group != null)                                   return false
        if (!groupId.isNullOrBlank() && !groupName.isNullOrBlank()) return false
        return true
    }

    fun toGroup(): Group = group?.copy(
        totalSavings = totalSavings,
        mySavings    = mySavings,
        groupCode    = groupCode ?: group.groupCode
    ) ?: Group(
        id              = groupId  ?: "",
        name            = groupName ?: "",
        description     = null,
        location        = null,
        groupCode       = groupCode ?: "",
        minContribution = 0.0,
        savingPeriod    = 0,
        maxMembers      = 0,
        startDate       = null,
        endDate         = null,
        meetingDay      = null,
        meetingTime     = null,
        totalSavings    = totalSavings,
        mySavings       = mySavings,
        isActive        = isActive ?: true
    )
}
 */