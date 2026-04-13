package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Contribution(
    val id: String,
    val groupId: String,
    val userId: String,
    val amount: Double,
    val type: String, // "REGULAR", "SHARE_PURCHASE", "SOCIAL_FUND", "LOAN_REPAYMENT", "EVENT"
    val status: String, // "PENDING", "COMPLETED", "FAILED"
    val transactionRef: String?,
    val externalRef: String?,
    val phoneUsed: String?,
    val failureReason: String?,
    val createdAt: String,
    val updatedAt: String,
    
    // Nested group object often returned in "mine" history
    @SerializedName("Group")
    val group: ContributionGroup? = null,
    
    // Nested user object often returned in "group" history
    @SerializedName("User")
    val user: ContributionUser? = null
)

data class ContributionGroup(
    val id: String,
    val name: String
)

data class ContributionUser(
    val id: String,
    val firstName: String,
    val lastName: String
)
