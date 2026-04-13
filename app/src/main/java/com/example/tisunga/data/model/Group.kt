package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("minContribution") val minContribution: Double,
    @SerializedName("savingPeriodMonths") val savingPeriod: Int,
    @SerializedName("visibility") val visibility: String = "PUBLIC",
    @SerializedName("maxMembers") val maxMembers: Int,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("meetingDay") val meetingDay: String,
    @SerializedName("meetingTime") val meetingTime: String,
    @SerializedName("totalSavings") val totalSavings: Double = 0.0,
    @SerializedName("mySavings") val mySavings: Double = 0.0,
    @SerializedName("status") val status: String = "ACTIVE",
    @SerializedName("groupCode") val groupCode: String? = null
)
