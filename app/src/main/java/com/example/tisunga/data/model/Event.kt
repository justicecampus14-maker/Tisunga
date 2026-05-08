package com.example.tisunga.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val id: String,
    val title: String,
    val description: String,
    @SerializedName("fixedAmount") val targetAmount: Double? = null,
    val currentAmount: Double = 0.0,
    @SerializedName("eventDate") val endDate: String? = null,
    val status: String, // OPEN | CLOSED
    val createdAt: String? = null,
    val contributionsCount: Int = 0
) : Parcelable

@Parcelize
data class EventDetail(
    val id: String,
    val title: String,
    val description: String,
    @SerializedName("fixedAmount") val targetAmount: Double?,
    val currentAmount: Double,
    @SerializedName("eventDate") val endDate: String?,
    val status: String,
    val createdBy: UserSummary?,
    val contributions: List<EventContribution> = emptyList()
) : Parcelable

@Parcelize
data class EventContribution(
    val user: UserSummary,
    val amount: Double,
    val createdAt: String
) : Parcelable

@Parcelize
data class UserSummary(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
) : Parcelable
