package com.example.tisunga.data.model

data class Group(
    val id: Int,
    val name: String,
    val description: String,
    val location: String,
    val minContribution: Double,
    val savingPeriod: Int,
    val maxMembers: Int,
    val visibility: String,
    val startDate: String,
    val endDate: String,
    val meetingDay: String,
    val meetingTime: String,
    val totalSavings: Double = 0.0,
    val mySavings: Double = 0.0,
    val status: String = "active",
    val groupCode: String? = null
)
