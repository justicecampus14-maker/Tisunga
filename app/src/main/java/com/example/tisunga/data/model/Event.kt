package com.example.tisunga.data.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double? = null,
    val currentAmount: Double = 0.0,
    val endDate: String? = null,
    val status: String, // OPEN | CLOSED
    val createdAt: String? = null,
    val contributionsCount: Int = 0
)

data class EventDetail(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double?,
    val currentAmount: Double,
    val endDate: String?,
    val status: String,
    val createdBy: UserSummary?,
    val contributions: List<EventContribution> = emptyList()
)

data class EventContribution(
    val user: UserSummary,
    val amount: Double,
    val createdAt: String
)

data class UserSummary(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)
