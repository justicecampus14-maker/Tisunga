package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Disbursement(
    val id: String,
    val groupId: String,
    val amount: Double,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val requestedBy: String,
    val requestedByName: String? = null,
    val requestedAt: String,
    val approvedBy: String? = null,
    val approvedByName: String? = null,
    val approvedAt: String? = null,
    val rejectedBy: String? = null,
    val rejectedAt: String? = null,
    val rejectionReason: String? = null,
    val memberShares: List<MemberSharePayout> = emptyList()
)

data class MemberSharePayout(
    val userId: String,
    val userName: String,
    val userPhone: String,
    val memberSavings: Double,
    val shareAmount: Double,
    val status: String // "PENDING", "SENT", "FAILED"
)
