package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Disbursement(
    val id: Int,
    val groupId: Int,
    val amount: Double,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val requestedBy: Int,
    val requestedByName: String? = null,
    val requestedAt: String,
    val approvedBy: Int? = null,
    val approvedByName: String? = null,
    val approvedAt: String? = null,
    val rejectedBy: Int? = null,
    val rejectedAt: String? = null,
    val rejectionReason: String? = null,
    val memberShares: List<MemberSharePayout> = emptyList()
)

data class MemberSharePayout(
    val userId: Int,
    val userName: String,
    val userPhone: String,
    val memberSavings: Double,
    val shareAmount: Double,
    val status: String // "PENDING", "SENT", "FAILED"
)
