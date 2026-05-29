package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContributionRequest(
    @SerializedName("groupId") val groupId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("phone") val phone: String,
    @SerializedName("type") val type: String, // "SAVINGS", "SOCIAL_FUND", etc.
    @SerializedName("externalRef") val externalRef: String? = null
)

data class ContributionInitResponse(
    @SerializedName("transactionRef") val transactionRef: String,
    @SerializedName("externalRef") val externalRef: String?,
    @SerializedName("status") val status: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("message") val message: String? = null
)
