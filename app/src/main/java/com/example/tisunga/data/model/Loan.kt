package com.example.tisunga.data.model

data class Loan(
    val id: Int,
    val groupId: Int,
    val groupName: String,
    val memberId: Int,
    val memberName: String,
    val amount: Double,
    val interestRate: Double,
    val repayableAmount: Double,
    val remainingAmount: Double,
    val percentRepaid: Float,
    val dueDate: String,
    val status: String, // "pending", "active", "completed", "rejected"
    val approvedBy: String? = null,
    val approvalDate: String? = null,
    val purpose: String? = null,
    val period: String? = null
)
