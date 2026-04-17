package com.example.tisunga.data.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

enum class TransactionType {
    @SerializedName("SAVINGS") SAVINGS,
    @SerializedName("LOAN_OUT") LOAN_OUT,
    @SerializedName("LOAN_IN") LOAN_IN,
    @SerializedName("SOCIAL_FUND") SOCIAL_FUND,
    @SerializedName("SHARE_PURCHASE") SHARE_PURCHASE,
    @SerializedName("EXPENSE") EXPENSE,
    @SerializedName("JOIN_FEE") JOIN_FEE,
    @SerializedName("INTEREST") INTEREST,
    @SerializedName("SYSTEM") SYSTEM,
    @SerializedName("DISBURSEMENT") DISBURSEMENT
}

data class Transaction(
    val id: String,
    val groupId: String,
    val userId: String?,
    val type: TransactionType?,
    
    @JsonAdapter(StringToDouble::class)
    val amount: Double,
    
    @JsonAdapter(StringToDouble::class)
    val balanceAfter: Double,

    val description: String,
    val tisuRef: String,
    val createdAt: String,
    val updatedAt: String,
    
    // Enriched field from backend (joined via User)
    val memberName: String? = "System"
)
